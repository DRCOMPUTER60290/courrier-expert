package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.LetterEntity
import com.example.data.model.LetterTemplate
import com.example.data.model.RecipientEntity
import com.example.data.model.TemplateCatalog
import com.example.data.model.UserProfileEntity
import com.example.data.remote.AiClarificationQuestion
import com.example.data.remote.GeminiService
import com.example.data.repository.LetterRepository
import com.example.utils.PdfExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LetterViewModel(private val repository: LetterRepository) : ViewModel() {

    private val geminiService = GeminiService()

    val savedLetters: StateFlow<List<LetterEntity>> = repository.allLetters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val savedRecipients: StateFlow<List<RecipientEntity>> = repository.allRecipients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dark Mode State
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
    }

    // AI state
    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _isAiGeneratingQuestions = MutableStateFlow(false)
    val isAiGeneratingQuestions: StateFlow<Boolean> = _isAiGeneratingQuestions.asStateFlow()

    private val _clarificationQuestions = MutableStateFlow<List<AiClarificationQuestion>>(emptyList())
    val clarificationQuestions: StateFlow<List<AiClarificationQuestion>> = _clarificationQuestions.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    // Current Letter Editor State
    private val _currentLetter = MutableStateFlow(createEmptyLetter())
    val currentLetter: StateFlow<LetterEntity> = _currentLetter.asStateFlow()

    // Active Category Filter for Home
    private val _selectedCategory = MutableStateFlow<LetterTemplate.Category?>(null)
    val selectedCategory: StateFlow<LetterTemplate.Category?> = _selectedCategory.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: LetterTemplate.Category?) {
        _selectedCategory.value = category
    }

    private fun createEmptyLetter(): LetterEntity {
        val currentDate = SimpleDateFormat("d MMMM yyyy", Locale.FRANCE).format(Date())
        return LetterEntity(
            title = "Nouveau courrier",
            category = "Général",
            senderName = "",
            senderAddress = "",
            senderZipCode = "",
            senderCity = "",
            senderPhone = "",
            senderEmail = "",
            recipientName = "",
            recipientAddress = "",
            recipientZipCode = "",
            recipientCity = "",
            cityDate = "Fait à Paris, le $currentDate",
            subject = "",
            referencesText = "",
            body = "",
            politeForm = "Veuillez agréer, Madame, Monsieur, l'expression de mes salutations distinguées."
        )
    }

    fun loadLetterForEditing(letter: LetterEntity) {
        _currentLetter.value = letter
    }

    fun loadTemplateForEditing(template: LetterTemplate) {
        val profile = userProfile.value
        val currentDate = SimpleDateFormat("d MMMM yyyy", Locale.FRANCE).format(Date())
        val city = profile?.city?.ifEmpty { "Paris" } ?: "Paris"

        _currentLetter.value = LetterEntity(
            title = template.title,
            category = template.category.displayName,
            senderName = profile?.fullName ?: "",
            senderAddress = profile?.address ?: "",
            senderZipCode = profile?.zipCode ?: "",
            senderCity = profile?.city ?: "",
            senderPhone = profile?.phone ?: "",
            senderEmail = profile?.email ?: "",
            recipientName = "",
            recipientAddress = "",
            recipientZipCode = "",
            recipientCity = "",
            cityDate = "Fait à $city, le $currentDate",
            subject = template.defaultSubject,
            referencesText = "",
            body = template.defaultBody,
            politeForm = "Veuillez agréer, Madame, Monsieur, l'expression de mes salutations distinguées."
        )
    }

    fun updateCurrentLetter(updated: LetterEntity) {
        _currentLetter.value = updated
    }

    fun saveCurrentLetter(onSuccess: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val letter = _currentLetter.value
            val id = repository.saveLetter(letter)
            _currentLetter.value = letter.copy(id = id.toInt())
            onSuccess(id.toInt())
        }
    }

    fun updateLetterDirectly(letter: LetterEntity) {
        viewModelScope.launch {
            repository.updateLetter(letter)
        }
    }

    fun deleteLetter(id: Int) {
        viewModelScope.launch {
            repository.deleteLetter(id)
        }
    }

    fun saveProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }

    fun saveRecipient(recipient: RecipientEntity) {
        viewModelScope.launch {
            repository.saveRecipient(recipient)
        }
    }

    fun deleteRecipient(id: Int) {
        viewModelScope.launch {
            repository.deleteRecipient(id)
        }
    }

    // AI Clarification Questions
    fun fetchClarificationQuestions(prompt: String, category: String) {
        viewModelScope.launch {
            _isAiGeneratingQuestions.value = true
            _aiError.value = null
            val result = geminiService.generateClarificationQuestions(prompt, category)
            _isAiGeneratingQuestions.value = false
            result.onSuccess { questions ->
                _clarificationQuestions.value = questions
            }.onFailure {
                _clarificationQuestions.value = geminiService.getDefaultQuestionsForCategory(category, prompt)
            }
        }
    }

    fun loadDefaultQuestions(category: String, userPrompt: String = "") {
        _clarificationQuestions.value = geminiService.getDefaultQuestionsForCategory(category, userPrompt)
    }

    fun updateClarificationAnswer(questionId: String, answer: String) {
        _clarificationQuestions.value = _clarificationQuestions.value.map { q ->
            if (q.id == questionId) q.copy(answer = answer) else q
        }
    }

    fun addCustomClarificationQuestion(questionText: String) {
        if (questionText.isNotBlank()) {
            val newQ = AiClarificationQuestion(
                id = "custom_${System.currentTimeMillis()}",
                question = questionText,
                placeholder = "Votre précision...",
                hint = "Information spécifique ajoutée à votre demande"
            )
            _clarificationQuestions.value = _clarificationQuestions.value + newQ
        }
    }

    fun clearClarificationQuestions() {
        _clarificationQuestions.value = emptyList()
    }

    // AI Generation
    fun generateLetterWithAi(
        prompt: String,
        tone: String,
        category: String,
        clarificationDetails: List<Pair<String, String>> = emptyList(),
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isAiGenerating.value = true
            _aiError.value = null
            val result = geminiService.generateLetter(prompt, tone, category, clarificationDetails)
            _isAiGenerating.value = false

            result.onSuccess { generated ->
                val profile = userProfile.value
                val currentDate = SimpleDateFormat("d MMMM yyyy", Locale.FRANCE).format(Date())
                val city = profile?.city?.ifEmpty { "Paris" } ?: "Paris"

                _currentLetter.value = LetterEntity(
                    title = generated.subject,
                    category = category,
                    senderName = profile?.fullName ?: "",
                    senderAddress = profile?.address ?: "",
                    senderZipCode = profile?.zipCode ?: "",
                    senderCity = profile?.city ?: "",
                    senderPhone = profile?.phone ?: "",
                    senderEmail = profile?.email ?: "",
                    recipientName = "",
                    recipientAddress = "",
                    recipientZipCode = "",
                    recipientCity = "",
                    cityDate = "Fait à $city, le $currentDate",
                    subject = generated.subject,
                    referencesText = if (generated.legalReferences.isNotEmpty()) "Réf : ${generated.legalReferences}" else "",
                    body = generated.body,
                    politeForm = generated.politeForm
                )
                onSuccess()
            }.onFailure { error ->
                val rawMsg = error.message ?: "Erreur de génération par l'IA."
                _aiError.value = when {
                    rawMsg.contains("quota", ignoreCase = true) || rawMsg.contains("rate", ignoreCase = true) ->
                        "Limite de requêtes atteinte. Veuillez patienter quelques secondes avant de réessayer."
                    rawMsg.contains("network", ignoreCase = true) || rawMsg.contains("timeout", ignoreCase = true) ->
                        "Problème de connexion réseau. Veuillez vérifier votre connexion et réessayer."
                    rawMsg.length > 150 -> "Erreur lors de la génération. Veuillez simplifier votre demande et réessayer."
                    else -> rawMsg
                }
            }
        }
    }

    fun rewriteTextWithAi(instruction: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isAiGenerating.value = true
            val currentBody = _currentLetter.value.body
            val result = geminiService.rewriteLetterText(currentBody, instruction)
            _isAiGenerating.value = false
            result.onSuccess { newText ->
                _currentLetter.value = _currentLetter.value.copy(body = newText)
                onResult(newText)
            }.onFailure { err ->
                _aiError.value = err.message
            }
        }
    }

    fun exportAndSharePdf(context: Context) {
        val pdfFile = PdfExporter.generatePdf(context, _currentLetter.value)
        if (pdfFile != null) {
            PdfExporter.sharePdf(context, pdfFile)
        }
    }

    fun sendEmailPdf(context: Context) {
        val letter = _currentLetter.value
        val pdfFile = PdfExporter.generatePdf(context, letter)
        if (pdfFile != null) {
            PdfExporter.sendEmailWithPdf(context, pdfFile, subject = letter.subject)
        }
    }

    fun exportAndShareLetterPdf(context: Context, letter: LetterEntity) {
        val pdfFile = PdfExporter.generatePdf(context, letter)
        if (pdfFile != null) {
            PdfExporter.sharePdf(context, pdfFile)
        }
    }

    fun sendEmailLetterPdf(context: Context, letter: LetterEntity) {
        val pdfFile = PdfExporter.generatePdf(context, letter)
        if (pdfFile != null) {
            PdfExporter.sendEmailWithPdf(context, pdfFile, subject = letter.subject)
        }
    }

    fun duplicateLetter(letter: LetterEntity) {
        viewModelScope.launch {
            val copy = letter.copy(
                id = 0,
                title = "${letter.title} (Copie)",
                status = "Brouillon",
                dateCreated = System.currentTimeMillis()
            )
            repository.saveLetter(copy)
        }
    }

    fun clearAiError() {
        _aiError.value = null
    }
}

class LetterViewModelFactory(private val repository: LetterRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LetterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LetterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
