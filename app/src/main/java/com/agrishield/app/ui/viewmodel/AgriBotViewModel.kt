package com.agrishield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.model.ChatMessage
import com.agrishield.app.data.repository.AgriBotRepository
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.data.repository.DiagnosisRepository
import com.agrishield.app.data.repository.WeatherRepository
import com.agrishield.app.data.speech.AgriSpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgriBotViewModel(
    private val agriBotRepository: AgriBotRepository,
    private val speechRecognizer: AgriSpeechRecognizer,
    private val authRepository: AuthRepository,
    private val diagnosisRepository: DiagnosisRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    val messages: StateFlow<List<ChatMessage>> = agriBotRepository.messages
    val isLoading: StateFlow<Boolean> = agriBotRepository.isLoading
    val errorMessage: StateFlow<String?> = agriBotRepository.errorMessage

    val isListening: StateFlow<Boolean> = speechRecognizer.isListening
    val speechResult: StateFlow<String> = speechRecognizer.speechResult
    val rmsDbLevel: StateFlow<Float> = speechRecognizer.rmsDbLevel

    val selectedLanguage: StateFlow<String> = com.agrishield.app.utils.AppLanguageManager.currentLanguage

    fun setLanguage(langCode: String) {
        com.agrishield.app.utils.AppLanguageManager.setLanguage(langCode)
    }

    fun startVoiceInput() {
        val speechCode = if (com.agrishield.app.utils.AppLanguageManager.isTamil()) "ta-IN" else "en-IN"
        speechRecognizer.startListening(speechCode)
    }

    fun stopVoiceInput() {
        speechRecognizer.stopListening()
    }

    fun sendMessage(text: String, customApiKey: String? = null) {
        if (text.isBlank()) return

        val user = authRepository.currentUser.value
        val crop = user?.primaryCrop ?: "Tomato"
        val diagnosis = diagnosisRepository.latestDiagnosis.value
        val weather = weatherRepository.currentWeather.value
        val targetLang = com.agrishield.app.utils.AppLanguageManager.currentLanguage.value

        viewModelScope.launch {
            agriBotRepository.sendMessage(
                prompt = text.trim(),
                targetLanguage = targetLang,
                currentCrop = crop,
                recentDiagnosis = diagnosis,
                currentWeather = weather,
                customApiKey = customApiKey
            )
        }
    }

    fun clearHistory() {
        agriBotRepository.clearChat()
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.stopListening()
    }
}
