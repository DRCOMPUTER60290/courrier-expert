package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

data class LetterTemplate(
    val id: String,
    val title: String,
    val category: Category,
    val shortDescription: String,
    val defaultSubject: String,
    val defaultBody: String,
    val defaultRecipientType: String = "",
    val isPopular: Boolean = false
) {
    enum class Category(val displayName: String, val icon: ImageVector) {
        LOGEMENT("Logement & Immobilier", Icons.Default.Home),
        TRAVAIL("Travail & Emploi", Icons.Default.BusinessCenter),
        BANQUE("Banque & Assurance", Icons.Default.AccountBalance),
        IMPOTS("Administratif & CAF", Icons.AutoMirrored.Filled.ReceiptLong),
        CONSOMMATION("Consommation & Services", Icons.Default.ShoppingBag),
        JUSTICE("Justice & Santé", Icons.Default.Gavel)
    }
}

object TemplateCatalog {
    val templates = listOf(
        // LOGEMENT
        LetterTemplate(
            id = "logement_resiliation_bail_1m",
            title = "R\u00e9siliation de bail (Zone tendue / 1 mois)",
            category = LetterTemplate.Category.LOGEMENT,
            shortDescription = "Pr\u00e9avis r\u00e9duit \u00e0 1 mois pour d\u00e9part du logement en zone tendue ou mutation.",
            defaultSubject = "R\u00e9siliation du bail de location - Pr\u00e9avis d'un mois",
            defaultBody = """Par la pr\u00e9sente, je vous informe de mon intention de r\u00e9silier le contrat de location du logement situ\u00e9 au [Adresse du logement lou\u00e9].

Conform\u00e9ment aux dispositions de l'article 15 de la loi n° 89-462 du 6 juillet 1989, je b\u00e9n\u00e9ficie d'un pr\u00e9avis r\u00e9duit \u00e0 un mois au motif suivant : [Motif : Logement situ\u00e9 en zone tendue / Mutation professionnelle / Obtention d'un premier emploi].

En cons\u00e9quence, mon d\u00e9part effectif du logement aura lieu le [Date de d\u00e9part], date \u00e0 laquelle je vous remettrai les clefs et proc\u00e9derai \u00e0 l' \u00e9tat des lieux de sortie.

Je reste \u00e0 votre disposition pour convenir d'un rendez-vous pour la remise des cl\u00e9s.""",
            isPopular = true
        ),
        LetterTemplate(
            id = "logement_resiliation_bail_3m",
            title = "R\u00e9siliation de bail classique (3 mois)",
            category = LetterTemplate.Category.LOGEMENT,
            shortDescription = "Pr\u00e9avis standard de 3 mois pour r\u00e9silier votre bail de location non meubl\u00e9e.",
            defaultSubject = "Notification de r\u00e9siliation de bail de location",
            defaultBody = """Par ce courrier, je vous notifie mon d\u00e9sir de r\u00e9silier le bail de location concernant l'appartement situ\u00e9 \u00e0 [Adresse du logement].

Conform\u00e9ment \u00e0 la loi en vigueur, le d\u00e9lai de pr\u00e9avis de trois mois commencera \u00e0 courir \u00e0 compter de la r\u00e9ception de cette lettre recommand\u00e9e.

Le d\u00e9part du logement et la remise des cl\u00e9s s'effectueront donc le [Date exacte de fin de bail]. Je suis disponible afin de fixer la date de l' \u00e9tat des lieux de sortie.""",
            isPopular = false
        ),
        LetterTemplate(
            id = "logement_depot_garantie",
            title = "Demande de restitution du d\u00e9p\u00f4t de garantie",
            category = LetterTemplate.Category.LOGEMENT,
            shortDescription = "Relance suite au non-remboursement de la caution apr\u00e8s la remise des cl\u00e9s.",
            defaultSubject = "Demande de restitution du d\u00e9p\u00f4t de garantie - Logement du [Adresse]",
            defaultBody = """Suite \u00e0 la remise des cl\u00e9s du logement situ\u00e9 au [Adresse du logement] effectu\u00e9e le [Date de remise des cl\u00e9s] et \u00e0 l' \u00e9tat des lieux de sortie conforme, je constate que vous ne m'avez pas restitu\u00e9 le d\u00e9p\u00f4t de garantie d'un montant de [Montant en €] euros.

Conform\u00e9ment \u00e0 l'article 22 de la loi du 6 juillet 1989, le d\u00e9lai l\u00e9gal de restitution (1 mois si l' \u00e9tat des lieux est conforme) est aujourd'hui d\u00e9pass\u00e9.

Je vous demande par la pr\u00e9sente de bien vouloir me faire parvenir cette somme dans les plus brefs d\u00e9lais. A d\u00e9faut, une majoration l\u00e9gale de 10% du loyer mensuel par mois de retard sera appliqu\u00e9e.""",
            isPopular = true
        ),
        LetterTemplate(
            id = "logement_demande_travaux",
            title = "Demande de travaux au propri\u00e9taire",
            category = LetterTemplate.Category.LOGEMENT,
            shortDescription = "Demande d'intervention d'urgence pour r\u00e9parations majeurs (fuite, chauffage, etc.).",
            defaultSubject = "Demande de travaux d'entretien / r\u00e9paration urgente",
            defaultBody = """Je tiens \u00e0 vous informer par ce courrier d'un dysfonctionnement majeur dans le logement que je loue situ\u00e9 au [Adresse du logement].

En effet, j'ai constat\u00e9 le probl\u00e8me suivant : [Description d\u00e9taill\u00e9e de la panne ou de la d\u00e9g\u00e2t : fuite d'eau, panne de chauffage, etc.].

En vertu de l'article 6 de la loi n° 89-462 du 6 juillet 1989, le bailleur est tenu de remettre un logement en bon \u00e9tat d meubler et d'effectuer les r\u00e9parations qui ne sont pas \u00e0 la charge du locataire. Je vous remercie de faire intervenir un professionnel dans les meilleurs d\u00e9lais.""",
            isPopular = false
        ),

        // TRAVAIL
        LetterTemplate(
            id = "travail_demission",
            title = "Lettre de d\u00e9mission d'un CDI",
            category = LetterTemplate.Category.TRAVAIL,
            shortDescription = "Notification officielle de d\u00e9mission d'un poste en CDI avec ex\u00e9cution du pr\u00e9avis.",
            defaultSubject = "Notification de d\u00e9mission de mon poste de [Intitul\u00e9 du poste]",
            defaultBody = """Par la pr\u00e9sente, je vous informe de ma d\u00e9cision de d\u00e9missionner de mon emploi de [Intitul\u00e9 du poste], que j'occupe au sein de votre entreprise depuis le [Date de d\u00e9but de contrat].

Conform\u00e9ment aux termes de mon contrat de travail et de la convention collective applicable, j'effectuerai mon pr\u00e9avis d'une dur\u00e9e de [Dur\u00e9e du pr\u00e9avis : ex. 1 mois / 3 mois].

Mon d\u00e9part effectif de l'entreprise interviendra donc le [Date de fin de contrat]. Lors de mon dernier jour, je vous demanderai de bien vouloir me remettre mon certificat de travail, mon re\u00e7u pour solde de tout compte ainsi que mon attestation France Travail (P\u00f4le Emploi).""",
            isPopular = true
        ),
        LetterTemplate(
            id = "travail_rupture_conventionnelle",
            title = "Demande de rupture conventionnelle",
            category = LetterTemplate.Category.TRAVAIL,
            shortDescription = "Proposition d'entretien afin de convenir d'une rupture d'un commun accord.",
            defaultSubject = "Demande d'entretien en vue d'une rupture conventionnelle",
            defaultBody = """Salari\u00e9(e) au sein de votre entreprise en qualit\u00e9 de [Intitul\u00e9 du poste] depuis le [Date d'embauche], je souhaiterais solliciter un entretien afin d' \u00e9tudier la possibilit\u00e9 d'une rupture conventionnelle de mon contrat de travail, conform\u00e9ment aux articles L. 1237-11 et suivants du Code du travail.

Cette d\u00e9marche vise \u00e0 convenir d'un commun accord des modalit\u00e9s de la fin de notre collaboration.

Je me tiens \u00e0 votre disposition pour convenir d'une date d'entretien selon vos disponibilit\u00e9s.""",
            isPopular = true
        ),
        LetterTemplate(
            id = "travail_demande_conge",
            title = "Demande de cong\u00e9s pay\u00e9s / exceptionnels",
            category = LetterTemplate.Category.TRAVAIL,
            shortDescription = "Demande d'autorisation d'absence ou de prise de cong\u00e9s \u00e0 votre employeur.",
            defaultSubject = "Demande de cong\u00e9s du [Date de d\u00e9but] au [Date de fin]",
            defaultBody = """Par ce courrier, je sollicite votre accord pour poser une p\u00e9riode de cong\u00e9s du [Date de d\u00e9but inclus] au [Date de fin inclus], soit un total de [Nombre] jours ouvr\u00e9s.

Je veillerai \u00e0 organiser mes dossiers et transmettre les consignes de suivi avant mon d\u00e9part afin de garantir la continuit\u00e9 du service.

Je vous remercie par avance pour la confirmation de cette demande.""",
            isPopular = false
        ),
        LetterTemplate(
            id = "travail_demande_teletravail",
            title = "Demande d'am\u00e9nagement en t\u00e9l\u00e9travail",
            category = LetterTemplate.Category.TRAVAIL,
            shortDescription = "Demande de jours de t\u00e9l\u00e9travail r\u00e9guliers ou ponctuels.",
            defaultSubject = "Demande de passage en t\u00e9l\u00e9travail",
            defaultBody = """En poste au sein du service [Nom du service] en tant que [Poste], je sollicite la possibilit\u00e9 de passer en t\u00e9l\u00e9travail \u00e0 raison de [Nombre de jours] jours par semaine, conform\u00e9ment aux dispositions de l'accord d'entreprise.

Disposant d'un espace de travail d\u00e9di\u00e9 et d'une connexion haut d\u00e9bit, cet am\u00e9nagement me permettra d'optimiser mon organisation tout en conservant une efficacit\u00e9 optimale dans la r\u00e9alisation de mes missions.""",
            isPopular = false
        ),

        // BANQUE & ASSURANCE
        LetterTemplate(
            id = "banque_resiliation_assurance_hamon",
            title = "R\u00e9siliation d'assurance (Loi Hamon)",
            category = LetterTemplate.Category.BANQUE,
            shortDescription = "R\u00e9siliation \u00e0 tout moment apr\u00e8s 1 an de contrat (Auto, Moto, Habitation).",
            defaultSubject = "R\u00e9siliation du contrat d'assurance n° [Num\u00e9ro de contrat] (Loi Hamon)",
            defaultBody = """Je vous notifie par la pr\u00e9sente ma volont\u00e9 de r\u00e9silier mon contrat d'assurance [Auto / Moto / Habitation] n° [Num\u00e9ro de contrat], soucrit le [Date de souscription].

Conform\u00e9ment aux dispositions de l'article L. 113-15-2 du Code des assurances (Loi Hamon), le contrat ayant plus d'un an d'anciennet\u00e9, je peux le r\u00e9silier \u00e0 tout moment sans frais ni p\u00e9nalit\u00e9s.

Je vous remercie de me transmettre un avenant de r\u00e9siliation ainsi que le remboursement des cotisations trop-per\u00e7ues le cas \u00e9ch\u00e9ant.""",
            isPopular = true
        ),
        LetterTemplate(
            id = "banque_fermeture_compte",
            title = "Fermeture de compte bancaire",
            category = LetterTemplate.Category.BANQUE,
            shortDescription = "Cl\u00f4ture d'un compte courant ou d'un livret d mepargne avec virement du solde.",
            defaultSubject = "Demande de cl\u00f4ture du compte n° [Num\u00e9ro de compte IBAN]",
            defaultBody = """Par la pr\u00e9sente, je vous demande de bien vouloir proc\u00e9der \u00e0 la cl\u00f4ture de mon compte courant n° [Num\u00e9ro de compte], ouvert au sein de votre \u00e9tablissement.

Je vous prie de bien vouloir virer le solde cr\u00e9diteur restant sur mon nouveau compte bancaire dont le RIB est joint \u00e0 ce courrier.

Je vous informe avoir d\u00e9j\u00e0 d\u00e9truit/restitu\u00e9 les moyens de paiement (carte bancaire, ch\u00e9quier) associ\u00e9s \u00e0 ce compte.""",
            isPopular = true
        ),
        LetterTemplate(
            id = "banque_contestation_frais",
            title = "Contestation de frais bancaires abusifs",
            category = LetterTemplate.Category.BANQUE,
            shortDescription = "Demande de remboursement d'agios ou commissions d'intervention non justifi\u00e9es.",
            defaultSubject = "Contestation et demande de remboursement de frais bancaires",
            defaultBody = """En examinant le relev\u00e9 de compte n° [Num\u00e9ro de compte] du mois de [Mois/Ann\u00e9e], j'ai constat\u00e9 le pr\u00e9l\u00e8vement de frais d'un montant total de [Montant €] d\u00e9nomm\u00e9s [Intitul\u00e9 des frais : ex. commission d'intervention / agios].

Estimant ces pr\u00e9l\u00e8vements sp\u00e9cialement disproportionn\u00e9s au regard de ma situation, je vous sollicite un geste commercial afin de proc\u00e9der au remboursement de ces sommes sur mon compte bancaire.""",
            isPopular = false
        ),

        // IMPOTS & CAF
        LetterTemplate(
            id = "impots_remise_gracieuse",
            title = "Demande de remise gracieuse d'imp\u00f4t",
            category = LetterTemplate.Category.IMPOTS,
            shortDescription = "Demande d'annulation ou de r\u00e9duction d'imp\u00f4t suite \u00e0 des difficult\u00e9s financi\u00e8res.",
            defaultSubject = "Demande de remise gracieuse - Avis d'imposition n° [Num\u00e9ro fiscal]",
            defaultBody = """Faisant suite \u00e0 la r\u00e9ception de mon avis d'imposition [Ann\u00e9e] fixant un montant de [Montant €] euros, je me permets de vous contacter afin de vous faire part de graves difficult\u00e9s financières impr\u00e9vues ([Raison : baisse de revenus, s\u00e9paration, maladie, perte d meploi]).

En vertu de l'article L. 247 du Livre des proc\u00e9dures fiscales, je sollicite bienveillamment une remise gracieuse totale ou partielle de cette somme.

Vous trouverez ci-joint les justificatifs attestant de ma situation financi\u00e8re actuelle.""",
            isPopular = true
        ),
        LetterTemplate(
            id = "impots_delai_paiement",
            title = "Demande d'\u00e9ch\u00e9ancier de paiement",
            category = LetterTemplate.Category.IMPOTS,
            shortDescription = "Demande d'echelonnement des versements pour imp\u00f4t sur le revenu ou taxe fonci\u00e8re.",
            defaultSubject = "Demande de d\u00e9lai de paiement / \u00c9ch\u00e9ancier - Num\u00e9ro fiscal [Num\u00e9ro]",
            defaultBody = """Suite \u00e0 mon avis d'imposition n° [Num\u00e9ro fiscal] d'un montant de [Montant €] euros payable pour le [Date limite], je sollicite un am\u00e9nagement de mon paiement.

Actuellement confront\u00e9(e) \u00e0 des contraintes budg\u00e9taires temporaires, il me sera impossible d'honorer cette dette en une seule fois. Je vous propose de mensualiser le r\u00e8glement en [Nombre de mois] versements de [Montant mensuel €] euros.""",
            isPopular = false
        ),
        LetterTemplate(
            id = "impots_contestation_amende",
            title = "Contestation d'amende forfaitaire",
            category = LetterTemplate.Category.IMPOTS,
            shortDescription = "R\u00e9clamation formelle pour contestation de PV ou amende routi\u00e8re.",
            defaultSubject = "Contestation de l'avis de contravention n° [Num\u00e9ro de contravention]",
            defaultBody = """Par la pr\u00e9sente, je formule une protestation officielle concernant l'avis de contravention n° [Num\u00e9ro contravention] dat\u00e9 du [Date du PV].

En effet, l'infraction constat\u00e9e ne m'est pas imputable pour le motif suivant : [Motif : v\u00e9hicule c\u00e9d\u00e9 avant la date, usurpation d'immatriculation, erreur de signalisation].

Je vous prie de bien vouloir classer cette contravention sans suite et proc\u00e9der \u00e0 son annulation.""",
            isPopular = true
        ),

        // CONSOMMATION
        LetterTemplate(
            id = "consommation_retractation_14j",
            title = "Droit de r\u00e9tractation (Achat sous 14 jours)",
            category = LetterTemplate.Category.CONSOMMATION,
            shortDescription = "Annulation et remboursement d'une commande effectu\u00e9e en ligne ou par d\u00e9marchage.",
            defaultSubject = "Exercice du droit de r\u00e9tractation - Commande n° [Num\u00e9ro de commande]",
            defaultBody = """Conform\u00e9ment aux dispositions de l'article L. 221-18 du Code de la consommation, je vous notifie par la pr\u00e9sente l'exercice de mon droit de r\u00e9tractation concernant la commande n° [Num\u00e9ro commande] pass\u00e9e le [Date de commande] sur votre site.

Je vous demande de bien vouloir proc\u00e9der au remboursement int\u00e9gral de la somme de [Montant €] euros r\u00e9gl\u00e9e lors de cet achat, dans un d\u00e9lai maximum de 14 jours \u00e0 compter de la r\u00e9ception de ce courrier.""",
            isPopular = true
        ),
        LetterTemplate(
            id = "consommation_resiliation_box_mobile",
            title = "R\u00e9siliation abonnement Telecom / Box / Salle",
            category = LetterTemplate.Category.CONSOMMATION,
            shortDescription = "R\u00e9siliation d'abonnement sans engagement ou arriv\u00e9 \u00e0 terme.",
            defaultSubject = "Demande de r\u00e9siliation du contrat n° [Num\u00e9ro d'abonnement]",
            defaultBody = """Je vous informe par ce courrier de ma d\u00e9cision de r\u00e9silier mon contrat d'abonnement n° [Num\u00e9ro de contrat / Ligne d'abonnement].

Conform\u00e9ment \u00e0 vos conditions g\u00e9n\u00e9rales de vente, la r\u00e9siliation devra prendre effet \u00e0 l'issue du d\u00e9lai de pr\u00e9avis l\u00e9gal de 10 jours \u00e0 compter de la r\u00e9ception de cette lettre.

Je vous remercie de m'envoyer une confirmation \u00e9crite indiquant la date d'effet de la r\u00e9siliation et d'interrompre les pr\u00e9l\u00e8vements automatiques.""",
            isPopular = false
        ),
        LetterTemplate(
            id = "consommation_reclamation_transport",
            title = "R\u00e9clamation retard / annulation Train ou Vol",
            category = LetterTemplate.Category.CONSOMMATION,
            shortDescription = "Demande d'indemnisation forfaitaire pour vol ou train retard\u00e9 ou annul\u00e9.",
            defaultSubject = "Demande d'indemnisation pour retard/annulation - Billet n° [Num\u00e9ro]",
            defaultBody = """Passager sur le [Vol n° / Train n°] du [Date du voyage] au d\u00e9part de [D\u00e9part] et \u00e0 destination de [Arriv\u00e9e], j'ai subi un retard important de [Dur\u00e9e du retard] heures.

Conform\u00e9ment au R\u00e8glement europ\u00e9en (CE) n° 261/2004, je sollicite le versement de l'indemnit\u00e9 forfaitaire pr\u00e9vue par la loi d'un montant de [Montant indamnit\u00e9 : ex 250€ / 400€ / 600€].

Vous trouverez ci-joint la copie des cartes d membarquement et justificatifs de voyage.""",
            isPopular = false
        ),

        // JUSTICE & SANTE
        LetterTemplate(
            id = "justice_mise_en_demeure",
            title = "Mise en demeure de payer / sous huitaine",
            category = LetterTemplate.Category.JUSTICE,
            shortDescription = "Sommation juridique formelle avant poursuites judiciaires ou injonction de payer.",
            defaultSubject = "MISE EN DEMEURE - R\u00e8glement de la facture n° [Num\u00e9ro]",
            defaultBody = """Sauf erreur ou omission de ma part, je constate que la facture n° [Num\u00e9ro de facture] d'un montant de [Montant €] euros \u00e9chue le [Date d'\u00e9ch\u00e9ance] reste impay\u00e9e \u00e0 ce jour.

Par la pr\u00e9sente, JE VOUS METS EN DEMEURE de proc\u00e9der au r\u00e8glement int\u00e9gral de cette somme sous un d\u00e9lai de 8 (huit) jours \u00e0 compter de la r\u00e9ception de ce courrier.

À d\u00e9faut de r\u00e8glement dans le d\u00e9lai imparti, je me verrai contraint(e) de saisir la juridiction comp\u00e9tente afin d'obtenir le recouvrement forc\u00e9 de cette cr\u00e9ance augment\u00e9e des int\u00e9r\u00eats de retard et des frais de justice.""",
            isPopular = true
        ),
        LetterTemplate(
            id = "justice_dossier_medical",
            title = "Demande d'acc\u00e8s au dossier m\u00e9dical (Loi Kouchner)",
            category = LetterTemplate.Category.JUSTICE,
            shortDescription = "Demande d'obtention de copie des documents m\u00e9dicaux aupr\u00e8s d'un h\u00f4pital ou m\u00e9decin.",
            defaultSubject = "Demande d'acc\u00e8s au dossier m\u00e9dical - [Nom du patient]",
            defaultBody = """En application des dispositions de l'article L. 1111-7 du Code de la sant\u00e9 publique (Loi Kouchner du 4 mars 2002), je vous demande de bien vouloir me transmettre l'int\u00e9gralit\u00e9 de mon dossier m\u00e9dical concernant la p\u00e9riode du [Date] au [Date].

Je souhaite recevoir une copie num\u00e9rique ou papier de l'ensemble des comptes rendus d mexamen, ordonnances, comptes rendus op\u00e9ratoires et bulletins d'hospitalisation.

Je m'engage \u00e0 r\u00e9gler les frais de reproduction et d menvoi le cas \u00e9ch\u00e9ant.""",
            isPopular = false
        )
    )
}
