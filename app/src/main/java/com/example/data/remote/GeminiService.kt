package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeneratedLetterResult(
    val subject: String,
    val body: String,
    val politeForm: String,
    val legalReferences: String = ""
)

data class AiClarificationQuestion(
    val id: String,
    val question: String,
    val placeholder: String,
    val hint: String = "",
    val suggestedOptions: List<String> = emptyList(),
    val answer: String = ""
)

class GeminiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateClarificationQuestions(
        userPrompt: String,
        category: String
    ): Result<List<AiClarificationQuestion>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.success(getDefaultQuestionsForCategory(category, userPrompt))
            }

            val systemInstruction = """
                Tu es un expert juriste et écrivain public d'élite spécialisé dans les courriers administratifs français.
                Pour rédiger un courrier administratif ou juridique parfait selon la norme AFNOR, tu dois poser entre 2 et 4 questions de clarification précises, courtes et ciblées à l'utilisateur, adaptées au domaine '$category' et au contenu exact de sa demande : '$userPrompt'.
                
                Exemples d'informations clés selon le contexte :
                - Numéro de contrat, de police d'assurance, de dossier, d'abonné ou référence client
                - Date clé (date de souscription, date du sinistre, date de remise des clés, date de notification, fin de préavis...)
                - Montant en jeu ou montant contesté
                - Adresse précise ou matricule
                - Motif légal spécifique (ex: vente de véhicule, déménagement, panne non résolue...)
                
                Pour chaque question, fournis :
                - id : identifiant court en minuscules (ex: "contract_number", "incident_date", "reason")
                - question : la question claire et polie (ex: "Quel est votre numéro de contrat ou référence client ?")
                - placeholder : un exemple de réponse réaliste (ex: "Ex: CONTRAT-88392-AXA")
                - hint : une courte explication de l'utilité juridique/administrative
                - suggestedOptions : 2 à 4 choix ou suggestions rapides cliquables pour l'utilisateur
                
                Tu DOIS répondre STRICTEMENT au format JSON avec un tableau d'objets :
                [
                  {
                    "id": "contract_number",
                    "question": "Quel est votre numéro de contrat ou de dossier ?",
                    "placeholder": "Ex : CT-2024-9812",
                    "hint": "Permet au destinataire d'identifier instantanément votre dossier",
                    "suggestedOptions": ["Non précisé", "En attente"]
                  }
                ]
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
                put("contents", JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(
                            JSONObject().put("text", "Analyse cette situation et génère les questions clés nécessaires : Domaine '$category', Demande : '$userPrompt'")
                        ))
                    }
                ))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("maxOutputTokens", 2048)
                    put("responseMimeType", "application/json")
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(getDefaultQuestionsForCategory(category, userPrompt))
            }

            val responseBodyStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseBodyStr)
            val candidates = jsonResp.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.success(getDefaultQuestionsForCategory(category, userPrompt))
            }

            val textResult = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            val questions = parseQuestionsJson(textResult, category, userPrompt)
            if (questions.isNotEmpty()) {
                Result.success(questions)
            } else {
                Result.success(getDefaultQuestionsForCategory(category, userPrompt))
            }
        } catch (e: Exception) {
            Result.success(getDefaultQuestionsForCategory(category, userPrompt))
        }
    }

    private fun parseQuestionsJson(rawText: String, category: String, userPrompt: String): List<AiClarificationQuestion> {
        val clean = rawText
            .replace("```json", "")
            .replace("```", "")
            .trim()

        try {
            val array = JSONArray(clean)
            val list = mutableListOf<AiClarificationQuestion>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "q_$i")
                val question = obj.optString("question", "").trim()
                val placeholder = obj.optString("placeholder", "Votre réponse...").trim()
                val hint = obj.optString("hint", "").trim()
                val optionsJson = obj.optJSONArray("suggestedOptions")
                val optionsList = mutableListOf<String>()
                if (optionsJson != null) {
                    for (j in 0 until optionsJson.length()) {
                        val opt = optionsJson.optString(j)
                        if (opt.isNotBlank()) optionsList.add(opt)
                    }
                }
                if (question.isNotBlank()) {
                    list.add(
                        AiClarificationQuestion(
                            id = id,
                            question = question,
                            placeholder = placeholder,
                            hint = hint,
                            suggestedOptions = optionsList
                        )
                    )
                }
            }
            if (list.isNotEmpty()) return list
        } catch (_: Exception) {
            // Fallback to default questions
        }
        return getDefaultQuestionsForCategory(category, userPrompt)
    }

    fun getDefaultQuestionsForCategory(category: String, userPrompt: String = ""): List<AiClarificationQuestion> {
        val lowerCategory = category.lowercase()
        val lowerPrompt = userPrompt.lowercase()

        return when {
            lowerCategory.contains("logement") || lowerCategory.contains("immobilier") -> listOf(
                AiClarificationQuestion(
                    id = "address",
                    question = "Quelle est l'adresse exacte du logement concerné ?",
                    placeholder = "Ex : 12 rue de la Paix, 75002 Paris (Bâtiment B, Apt 14)",
                    hint = "Indispensable pour identifier précisément le bien loué ou possédé",
                    suggestedOptions = listOf("Adresse de mon profil")
                ),
                AiClarificationQuestion(
                    id = "dates",
                    question = if (lowerPrompt.contains("résiliation") || lowerPrompt.contains("preavis") || lowerPrompt.contains("quitter")) {
                        "Quelle est la date souhaitée de départ ou de remise des clés ?"
                    } else if (lowerPrompt.contains("dépôt") || lowerPrompt.contains("caution")) {
                        "À quelle date avez-vous rendu les clés et réalisé l'état des lieux ?"
                    } else {
                        "Quelle est la date clé de la situation (début de bail, incident, départ) ?"
                    },
                    placeholder = "Ex : 31 octobre 2024",
                    hint = "Permet de calculer les délais légaux de préavis ou de prescription",
                    suggestedOptions = listOf("Dans 1 mois (Zone tendue)", "Dans 3 mois (Classique)", "Effet immédiat")
                ),
                AiClarificationQuestion(
                    id = "contract_ref",
                    question = "Quel est le numéro de contrat de bail ou référence locataire ?",
                    placeholder = "Ex : BAIL-LOC-2023-094",
                    hint = "Facilite le traitement administratif par le bailleur ou l'agence",
                    suggestedOptions = listOf("Sans numéro de référence")
                ),
                AiClarificationQuestion(
                    id = "amount",
                    question = "Quel est le montant en jeu si applicable (dépôt de garantie, loyer, devis) ?",
                    placeholder = "Ex : 850,00 €",
                    hint = "À mentionner pour toute réclamation financière ou retenue",
                    suggestedOptions = listOf("Non applicable")
                )
            )

            lowerCategory.contains("banque") || lowerCategory.contains("assurance") -> listOf(
                AiClarificationQuestion(
                    id = "contract_number",
                    question = "Quel est votre numéro de contrat, police d'assurance ou compte ?",
                    placeholder = "Ex : Contrat Auto n° AXA-77492049",
                    hint = "Obligatoire pour que la banque ou l'assureur retrouve votre dossier",
                    suggestedOptions = listOf("Contrat d'assurance", "Compte bancaire")
                ),
                AiClarificationQuestion(
                    id = "legal_reason",
                    question = "Quel est le motif précis ou le cadre légal invoqué ?",
                    placeholder = "Ex : Résiliation Loi Hamon après 1 an / Contestation de frais abusifs",
                    hint = "Donne un poids juridique direct à votre courrier",
                    suggestedOptions = listOf("Loi Hamon (> 1 an)", "Loi Chatel (Échéance)", "Vente / Cession du bien", "Frais indus / Erreur bancaire")
                ),
                AiClarificationQuestion(
                    id = "event_date_amount",
                    question = "Quelle est la date de l'incident / souscription ou le montant contesté ?",
                    placeholder = "Ex : Opération du 14 mars 2024 d'un montant de 120 €",
                    hint = "Précise les faits matériels pour le service réclamations",
                    suggestedOptions = listOf("Date du jour", "Sans montant spécifique")
                )
            )

            lowerCategory.contains("travail") || lowerCategory.contains("emploi") -> listOf(
                AiClarificationQuestion(
                    id = "job_title",
                    question = "Quel est l'intitulé exact de votre poste dans l'entreprise ?",
                    placeholder = "Ex : Technicien Support / Responsable Commercial / Employé administratif",
                    hint = "Mention obligatoire pour la conformité avec votre contrat de travail",
                    suggestedOptions = listOf("Cadre", "Employé / Agent de maîtrise", "Technicien")
                ),
                AiClarificationQuestion(
                    id = "employment_dates",
                    question = "Quelle est votre date d'embauche ou la date de fin / départ souhaitée ?",
                    placeholder = "Ex : En poste depuis le 15/02/2021, départ prévu le 30/11/2024",
                    hint = "Nécessaire pour le calcul des indemnités, du préavis et des congés",
                    suggestedOptions = listOf("Préavis conventionnel de 1 mois", "Préavis conventionnel de 3 mois", "Départ négocié")
                ),
                AiClarificationQuestion(
                    id = "employee_matricule",
                    question = "Avez-vous un matricule salarié ou convention collective applicable ?",
                    placeholder = "Ex : Matricule RH-8842 / Convention Syntec",
                    hint = "Aide les Ressources Humaines à traiter votre demande rapidement",
                    suggestedOptions = listOf("Convention collective nationale", "Sans matricule")
                )
            )

            lowerCategory.contains("impot") || lowerCategory.contains("administratif") || lowerCategory.contains("caf") -> listOf(
                AiClarificationQuestion(
                    id = "tax_allocataire_id",
                    question = "Quel est votre numéro fiscal / numéro d'allocataire CAF / N° Sécurité Sociale ?",
                    placeholder = "Ex : N° Allocataire 4892049 / N° Fiscal 12 34 567 890",
                    hint = "Indispensable pour l'administration publique pour accéder à votre compte",
                    suggestedOptions = listOf("Numéro d'allocataire CAF", "Numéro fiscal", "N° de Sécurité Sociale")
                ),
                AiClarificationQuestion(
                    id = "reference_doc",
                    question = "Quelle est la référence du courrier, de l'avis ou du dossier concerné ?",
                    placeholder = "Ex : Avis d'imposition n° 24-09842 / Dossier APL-9842",
                    hint = "Permet de rattacher votre lettre au bon agent instructeur",
                    suggestedOptions = listOf("Avis d'imposition", "Notification de décision", "Sans référence")
                ),
                AiClarificationQuestion(
                    id = "period_year",
                    question = "Quelle est l'année ou la période concernée par la réclamation ?",
                    placeholder = "Ex : Année fiscale 2023 / Trimestre en cours",
                    hint = "Délimite la période d'examen de vos droits",
                    suggestedOptions = listOf("Année 2024", "Année 2023", "Période en cours")
                )
            )

            lowerCategory.contains("conso") || lowerCategory.contains("service") || lowerCategory.contains("achat") -> listOf(
                AiClarificationQuestion(
                    id = "customer_line_id",
                    question = "Quel est votre numéro de client, d'abonné ou numéro de ligne ?",
                    placeholder = "Ex : Client n° 984029482 / Ligne 06 12 34 56 78",
                    hint = "Obligatoire pour que l'opérateur ou le commerçant vous identifie",
                    suggestedOptions = listOf("Numéro de ligne mobile", "Numéro d'abonné Box / Internet", "Numéro de compte client")
                ),
                AiClarificationQuestion(
                    id = "order_ref",
                    question = "Quel est le numéro de commande, de facture ou de matériel ?",
                    placeholder = "Ex : Commande n° CMD-2024-88492 / Facture F-9842",
                    hint = "Preuve de l'achat ou du contrat de prestation",
                    suggestedOptions = listOf("Numéro de commande en ligne", "Numéro de facture")
                ),
                AiClarificationQuestion(
                    id = "order_date_motif",
                    question = "Quelle est la date d'achat/livraison et le motif exact ?",
                    placeholder = "Ex : Acheté le 12/04/2024 - Rétractation 14 jours / Panne récurrente",
                    hint = "Définit les articles applicables du Code de la consommation (L221-18, L217-4...)",
                    suggestedOptions = listOf("Rétractation sous 14 jours", "Défaut de conformité / Panne", "Retard de livraison non honoré", "Résiliation pour hausse de tarif")
                )
            )

            lowerCategory.contains("justice") || lowerCategory.contains("sante") -> listOf(
                AiClarificationQuestion(
                    id = "file_number",
                    question = "Quel est le numéro de dossier, de plainte ou référence judiciaire / CPAM ?",
                    placeholder = "Ex : Dossier RG n° 24/00194 / N° Sinistre CPAM-984",
                    hint = "Essentiel pour le greffe ou le service médical",
                    suggestedOptions = listOf("Numéro de dossier", "Référence de plainte", "Sans référence")
                ),
                AiClarificationQuestion(
                    id = "jurisdiction_date",
                    question = "Quelle est la date des faits ou de la convocation et l'organisme concerné ?",
                    placeholder = "Ex : Faits survenus le 10/01/2024 / Tribunal Judiciaire de Paris",
                    hint = "Précise la temporalité et l'autorité saisie",
                    suggestedOptions = listOf("Tribunal Judiciaire", "Médiateur de la République", "Caisse Primaire d'Assurance Maladie")
                )
            )

            else -> listOf(
                AiClarificationQuestion(
                    id = "contract_ref",
                    question = "Quel est le numéro de contrat, de dossier ou référence client ?",
                    placeholder = "Ex : Réf : 2024-CTR-9842",
                    hint = "Permet au destinataire d'identifier instantanément votre dossier",
                    suggestedOptions = listOf("Sans numéro de référence")
                ),
                AiClarificationQuestion(
                    id = "event_date",
                    question = "Quelle est la date clé de votre situation ?",
                    placeholder = "Ex : 15 janvier 2024",
                    hint = "Donne un repère temporel précis aux faits",
                    suggestedOptions = listOf("Date du jour", "Ce mois-ci")
                ),
                AiClarificationQuestion(
                    id = "recipient_service",
                    question = "Quel est le nom ou le service du destinataire ?",
                    placeholder = "Ex : Service Réclamations / Direction Clientèle",
                    hint = "Assure l'acheminement direct vers le bon service",
                    suggestedOptions = listOf("Service Réclamations", "Direction des Ressources Humaines", "Service Clientèle")
                )
            )
        }
    }

    suspend fun generateLetter(
        userPrompt: String,
        tone: String = "Formel et Courtois",
        category: String = "Général",
        clarificationDetails: List<Pair<String, String>> = emptyList()
    ): Result<GeneratedLetterResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(Exception("Clé API Gemini non configurée dans les Secrets AI Studio."))
            }

            val formattedClarifications = if (clarificationDetails.isNotEmpty()) {
                val answeredItems = clarificationDetails.filter { it.second.isNotBlank() }
                if (answeredItems.isNotEmpty()) {
                    val itemsText = answeredItems.joinToString("\n") { (q, a) -> "- $q : $a" }
                    "\n\nINFORMATIONS PRÉCISES ET RÉFÉRENCES FOURNIES PAR L'UTILISATEUR (À OBLIGATOIREMENT INTÉGRER DANS LE COURRIER, DANS L'OBJET, LE CORPS DU TEXTE ET DANS LE CHAMP 'legalReferences' OU 'referencesText') :\n$itemsText"
                } else ""
            } else ""

            val systemInstruction = """
                Tu es un expert juriste et écrivain public d'élite spécialisé dans les courriers administratifs officiels français respectant la norme AFNOR NF Z 11-001.
                Ta mission est de rédiger une lettre formelle parfaite selon la demande de l'utilisateur.
                
                Ton niveau d'exigence :
                - Vocabulaire administratif et juridique français impeccable.
                - Ton souhaité : $tone
                - Catégorie : $category
                - Intègre les formules de politesse adaptées (ex: 'Veuillez agréer, Madame, Monsieur...').
                - Si applicable, mentionne les articles de loi ou décrets français pertinents (ex: Loi du 6 juillet 1989, Loi Hamon, Code du travail, Code de la consommation, etc.).
                - INTÈGRE NATURELLEMENT TOUS LES NUMÉROS DE CONTRATS, DATES, RÉFÉRENCES ET PRÉCISIONS FOURNIS.
                
                Tu DOIS répondre STRICTEMENT au format JSON avec la structure exacte suivante (sans balises markdown ```json autour si possible, ou du JSON pur) :
                {
                  "subject": "Objet clair et concis (incluant si pertinent la référence ou le numéro de contrat)",
                  "body": "Corps du texte de la lettre structuré avec alinéas et paragraphes bien aérés intégrant les détails fournis.",
                  "politeForm": "Formule de politesse formelle finale",
                  "legalReferences": "Numéros de contrat, références client et textes de loi mentionnés (ex: 'Contrat n° AXA-1234 - Art. L121-11 Code des assurances')"
                }
            """.trimIndent()

            val completeUserPrompt = "Rédige une lettre officielle pour la demande suivante : $userPrompt$formattedClarifications"

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
                put("contents", JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(
                            JSONObject().put("text", completeUserPrompt)
                        ))
                    }
                ))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("maxOutputTokens", 8192)
                    put("responseMimeType", "application/json")
                    put("responseSchema", JSONObject().apply {
                        put("type", "OBJECT")
                        put("properties", JSONObject().apply {
                            put("subject", JSONObject().put("type", "STRING"))
                            put("body", JSONObject().put("type", "STRING"))
                            put("politeForm", JSONObject().put("type", "STRING"))
                            put("legalReferences", JSONObject().put("type", "STRING"))
                        })
                        put("required", JSONArray().put("subject").put("body").put("politeForm"))
                    })
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorCode = response.code
                val errBody = response.body?.string() ?: ""
                val errorMessage = when (errorCode) {
                    429 -> "Limite de requêtes ou quota de l'API Gemini atteint. Veuillez patienter quelques secondes avant de réessayer."
                    403, 401 -> "Clé API Gemini non valide ou restreinte. Veuillez vérifier votre clé dans les Secrets AI Studio."
                    500, 503 -> "Le service Gemini est momentanément indisponible. Veuillez réessayer dans quelques instants."
                    else -> "Erreur de communication avec l'API Gemini ($errorCode)"
                }
                // Si l'API échoue (ex quota 429), on utilise le générateur expert local de secours
                val fallbackLetter = generateSmartFallbackLetter(userPrompt, tone, category, clarificationDetails)
                return@withContext Result.success(fallbackLetter)
            }

            val responseBodyStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseBodyStr)
            val candidates = jsonResp.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                val fallbackLetter = generateSmartFallbackLetter(userPrompt, tone, category, clarificationDetails)
                return@withContext Result.success(fallbackLetter)
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val textResult = parts.getJSONObject(0).getString("text")

            val result = parseGeneratedLetterJson(textResult, userPrompt)
            Result.success(result)
        } catch (e: Exception) {
            // Fallback intelligent en cas d'absence de réseau ou d'erreur
            val fallback = generateSmartFallbackLetter(userPrompt, tone, category, clarificationDetails)
            Result.success(fallback)
        }
    }

    private fun generateSmartFallbackLetter(
        prompt: String,
        tone: String,
        category: String,
        clarificationDetails: List<Pair<String, String>>
    ): GeneratedLetterResult {
        val detailsMap = clarificationDetails.toMap()
        val contractRef = detailsMap["contract_number"] ?: detailsMap["contract_ref"] ?: detailsMap["policy_number"] ?: ""
        val dates = detailsMap["dates"] ?: detailsMap["incident_date"] ?: detailsMap["employment_dates"] ?: ""
        val amount = detailsMap["amount"] ?: detailsMap["disputed_amount"] ?: ""
        val address = detailsMap["address"] ?: ""

        val subject = when {
            prompt.contains("résiliation", ignoreCase = true) -> "Demande de résiliation ${if (contractRef.isNotBlank()) "de contrat réf. $contractRef" else ""}"
            prompt.contains("contestation", ignoreCase = true) -> "Contestation officielle et demande de régularisation"
            prompt.contains("mise en demeure", ignoreCase = true) -> "Mise en demeure formelle"
            prompt.contains("dépôt", ignoreCase = true) || prompt.contains("caution", ignoreCase = true) -> "Demande de restitution du dépôt de garantie"
            prompt.contains("aménagement", ignoreCase = true) -> "Demande d'aménagement de planning professionnel"
            else -> "Courrier officiel relatif à : ${prompt.take(60)}"
        }

        val legalRef = when {
            category.contains("logement", ignoreCase = true) -> "Loi n° 89-462 du 6 juillet 1989 ${if (contractRef.isNotBlank()) "- Réf: $contractRef" else ""}"
            category.contains("banque", ignoreCase = true) || category.contains("assurance", ignoreCase = true) -> "Code des assurances (Loi Hamon) ${if (contractRef.isNotBlank()) "- Contrat: $contractRef" else ""}"
            category.contains("travail", ignoreCase = true) -> "Code du travail ${if (contractRef.isNotBlank()) "- Matricule: $contractRef" else ""}"
            category.contains("consommation", ignoreCase = true) -> "Art. L. 221-18 du Code de la consommation (Droit de rétractation)"
            else -> if (contractRef.isNotBlank()) "Réf. dossier : $contractRef" else "Courrier officiel AFNOR"
        }

        val polite = when {
            tone.contains("Strict", ignoreCase = true) || tone.contains("Ferme", ignoreCase = true) ->
                "Je vous saurais gré de bien vouloir traiter cette demande dans les plus brefs délais et vous prie d'agréer mes salutations distinguées."
            tone.contains("Conciliant", ignoreCase = true) ->
                "En espérant une résolution amiable et rapide de cette situation, je vous prie d'agréer l'expression de mes sentiments les meilleurs."
            else ->
                "Veuillez agréer, Madame, Monsieur, l'expression de ma considération distinguée."
        }

        val bodyBuilder = StringBuilder()
        bodyBuilder.append("Madame, Monsieur,\n\n")
        bodyBuilder.append("Par la présente, je me permets de vous contacter concernant la situation suivante :\n")
        bodyBuilder.append("${prompt.trim()}.\n\n")

        if (clarificationDetails.isNotEmpty()) {
            bodyBuilder.append("Voici les éléments et références attachés à mon dossier :\n")
            clarificationDetails.forEach { (k, v) ->
                if (v.isNotBlank()) {
                    bodyBuilder.append("• $k : $v\n")
                }
            }
            bodyBuilder.append("\n")
        }

        if (dates.isNotBlank()) {
            bodyBuilder.append("À cet effet, je vous précise que la date d'effet souhaitée ou de l'événement concerné est fixée au $dates.\n\n")
        }

        if (amount.isNotBlank()) {
            bodyBuilder.append("Le montant faisant l'objet de cette demande s'élève à $amount.\n\n")
        }

        bodyBuilder.append("Je vous remercie par avance pour l'attention que vous porterez à ma requête et pour la confirmation écrite de sa bonne prise en compte.")

        return GeneratedLetterResult(
            subject = subject.trim(),
            body = bodyBuilder.toString().trim(),
            politeForm = polite,
            legalReferences = legalRef.trim()
        )
    }

    private fun parseGeneratedLetterJson(rawText: String, defaultPrompt: String): GeneratedLetterResult {
        val clean = rawText
            .replace("```json", "")
            .replace("```", "")
            .trim()

        // 1. Essai de parsing JSON standard
        try {
            val json = JSONObject(clean)
            return GeneratedLetterResult(
                subject = json.optString("subject", "Demande officielle").trim(),
                body = json.optString("body", defaultPrompt).trim(),
                politeForm = json.optString("politeForm", "Veuillez agréer, Madame, Monsieur, l'expression de mes salutations distinguées.").trim(),
                legalReferences = json.optString("legalReferences", "").trim()
            )
        } catch (_: Exception) {
            // Poursuite vers la réparation / extraction regex
        }

        // 2. Réparation automatique si le JSON a été coupé avant l'accolade finale
        try {
            var repaired = clean
            if (!repaired.endsWith("}")) {
                repaired = if (repaired.endsWith("\"")) {
                    "$repaired\n}"
                } else {
                    "$repaired\"\n}"
                }
            }
            val json = JSONObject(repaired)
            return GeneratedLetterResult(
                subject = json.optString("subject", "Demande officielle").trim(),
                body = json.optString("body", defaultPrompt).trim(),
                politeForm = json.optString("politeForm", "Veuillez agréer, Madame, Monsieur, l'expression de mes salutations distinguées.").trim(),
                legalReferences = json.optString("legalReferences", "").trim()
            )
        } catch (_: Exception) {
            // Poursuite vers l'extraction regex
        }

        // 3. Extraction tolérante par expressions régulières
        fun extractField(fieldName: String): String? {
            val pattern = Regex("\"$fieldName\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"", RegexOption.DOT_MATCHES_ALL)
            val match = pattern.find(clean)
            if (match != null) {
                return unescapeJsonString(match.groupValues[1])
            }
            val unclosedPattern = Regex("\"$fieldName\"\\s*:\\s*\"([^\"]*)$", RegexOption.DOT_MATCHES_ALL)
            val unclosedMatch = unclosedPattern.find(clean)
            if (unclosedMatch != null) {
                return unescapeJsonString(unclosedMatch.groupValues[1])
            }
            return null
        }

        val extractedSubject = extractField("subject")?.ifBlank { null } ?: "Courrier officiel"
        val extractedBody = extractField("body")?.ifBlank { null } ?: clean
        val extractedPolite = extractField("politeForm")?.ifBlank { null }
            ?: "Veuillez agréer, Madame, Monsieur, l'expression de mes salutations distinguées."
        val extractedLegal = extractField("legalReferences") ?: ""

        return GeneratedLetterResult(
            subject = extractedSubject,
            body = extractedBody,
            politeForm = extractedPolite,
            legalReferences = extractedLegal
        )
    }

    private fun unescapeJsonString(str: String): String {
        return str
            .replace("\\n", "\n")
            .replace("\\r", "")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .trim()
    }

    suspend fun rewriteLetterText(
        currentText: String,
        instruction: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(Exception("Clé API Gemini non configurée dans les Secrets AI Studio."))
            }

            val systemInstruction = """
                Tu es un assistant de rédaction administrative française. 
                Reçois le texte d'un courrier et réécris-le selon la consigne : '$instruction'.
                Retourne uniquement le texte amélioré sans commentaire autour.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
                put("contents", JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(
                            JSONObject().put("text", currentText)
                        ))
                    }
                ))
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorCode = response.code
                val errMessage = when (errorCode) {
                    429 -> "Limite de requêtes atteinte. Veuillez patienter quelques secondes."
                    503, 500 -> "Service IA temporairement surchargé. Veuillez réessayer dans quelques instants."
                    else -> "Erreur de communication avec l'IA ($errorCode)"
                }
                return@withContext Result.failure(Exception(errMessage))
            }

            val responseBodyStr = response.body?.string() ?: ""
            val jsonResp = JSONObject(responseBodyStr)
            val candidates = jsonResp.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("Aucune réponse générée par l'IA."))
            }
            val text = candidates
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")

            Result.success(text.trim())
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Erreur inattendue"))
        }
    }
}
