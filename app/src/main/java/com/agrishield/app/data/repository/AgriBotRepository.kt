package com.agrishield.app.data.repository

import com.agrishield.app.BuildConfig
import com.agrishield.app.data.model.ChatMessage
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.data.model.MessageSender
import com.agrishield.app.data.model.WeatherData
import com.agrishield.app.data.network.GeminiContent
import com.agrishield.app.data.network.GeminiGenerationConfig
import com.agrishield.app.data.network.GeminiPart
import com.agrishield.app.data.network.GeminiRequest
import com.agrishield.app.data.network.GeminiSystemInstruction
import com.agrishield.app.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AgriBotRepository {

    private val geminiApi = RetrofitClient.geminiApi

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AGRIBOT,
                text = "வணக்கம்! நான் உங்கள் அக்ரிபாட் (AgriBot). பயிர் நோய்கள், உரம், பாசனம் மற்றும் பூச்சி மேலாண்மை பற்றி என்னிடம் கேட்கலாம்.\n\nHello! I am your AgriShield AI farming advisor. Ask me anything about crop health, treatments, weather precautions, or soil care in Tamil or English.",
                language = "mixed"
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    suspend fun sendMessage(
        prompt: String,
        currentCrop: String = "Tomato",
        recentDiagnosis: Diagnosis? = null,
        currentWeather: WeatherData? = null,
        customApiKey: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = prompt,
            language = if (isTamil(prompt)) "ta" else "en"
        )
        _messages.value = _messages.value + userMsg
        _isLoading.value = true
        _errorMessage.value = null

        val apiKey = customApiKey?.ifBlank { null }
            ?: BuildConfig.GEMINI_API_KEY.ifBlank { null }
            ?: "DEMO_KEY"

        // Build structured agronomic context for Gemini
        val contextInfo = buildString {
            append("Farmer Context:\n")
            append("- Primary Crop: $currentCrop\n")
            if (recentDiagnosis != null) {
                append("- Recent Diagnosis: ${recentDiagnosis.disease} on ${recentDiagnosis.crop} (${String.format("%.1f", recentDiagnosis.confidence)}% confidence)\n")
                append("- Severity: ${recentDiagnosis.severity}\n")
            }
            if (currentWeather != null) {
                append("- Live Weather: ${currentWeather.cityName} (${String.format("%.1f", currentWeather.temperatureCelsius)}°C, Humidity: ${currentWeather.humidityPercentage}%, Rain 3h: ${currentWeather.rainMmLast3h}mm, Wind: ${String.format("%.1f", currentWeather.windSpeedKmh)}km/h)\n")
            }
        }

        val systemInstruction = GeminiSystemInstruction(
            parts = listOf(
                GeminiPart(
                    text = """
                        You are AgriBot, an expert agricultural AI assistant and agronomist built for Indian and international farmers.
                        You support both Tamil (தமிழ்) and English fluently.
                        If the user asks in Tamil, reply in clear, friendly, and practical Tamil (and vice versa for English).
                        Provide actionable, scientific, and field-tested farming advice including:
                        1. Organic and chemical treatment dosages (e.g. g/L, kg/acre).
                        2. Cultural preventive practices and irrigation advice considering the farmer's live weather context.
                        3. Simple step-by-step guidance formatted with bullet points.
                    """.trimIndent()
                )
            )
        )

        val fullPrompt = "$contextInfo\nFarmer Question: $prompt"

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = fullPrompt))
                )
            ),
            systemInstruction = systemInstruction,
            generationConfig = GeminiGenerationConfig(temperature = 0.3, maxOutputTokens = 1024)
        )

        try {
            val response = geminiApi.generateContent(apiKey = apiKey, request = request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!replyText.isNullOrBlank()) {
                val botMsg = ChatMessage(
                    sender = MessageSender.AGRIBOT,
                    text = replyText.trim(),
                    contextInfo = contextInfo
                )
                _messages.value = _messages.value + botMsg
                _isLoading.value = false
                Result.success(replyText)
            } else {
                val err = response.error?.message ?: "AgriBot was unable to generate a response. Please check your API key."
                _errorMessage.value = err
                _isLoading.value = false
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            val errorMsg = "AgriBot service is currently unavailable. Please verify your Gemini API key in Profile/Settings."
            _errorMessage.value = errorMsg
            Result.failure(Exception(errorMsg, e))
        }
    }

    private fun isTamil(text: String): Boolean {
        return text.any { it in '\u0B80'..'\u0BFF' }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }
}
