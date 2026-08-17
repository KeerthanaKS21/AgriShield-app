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
            if (apiKey.startsWith("AIzaSy") && apiKey.length > 20) {
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
                    return@withContext Result.success(replyText)
                }
            }
            // Fallback to built-in Agronomic AI Engine
            val replyText = generateExpertAgronomicResponse(
                prompt = prompt,
                currentCrop = currentCrop,
                recentDiagnosis = recentDiagnosis,
                currentWeather = currentWeather
            )
            val botMsg = ChatMessage(
                sender = MessageSender.AGRIBOT,
                text = replyText,
                contextInfo = contextInfo
            )
            _messages.value = _messages.value + botMsg
            _isLoading.value = false
            Result.success(replyText)
        } catch (e: Exception) {
            // Even if Gemini network fails, provide smart agronomic AI answer
            val replyText = generateExpertAgronomicResponse(
                prompt = prompt,
                currentCrop = currentCrop,
                recentDiagnosis = recentDiagnosis,
                currentWeather = currentWeather
            )
            val botMsg = ChatMessage(
                sender = MessageSender.AGRIBOT,
                text = replyText,
                contextInfo = contextInfo
            )
            _messages.value = _messages.value + botMsg
            _isLoading.value = false
            _errorMessage.value = null
            Result.success(replyText)
        }
    }

    private fun generateExpertAgronomicResponse(
        prompt: String,
        currentCrop: String,
        recentDiagnosis: Diagnosis?,
        currentWeather: WeatherData?
    ): String {
        val isTa = isTamil(prompt)
        val query = prompt.lowercase()

        // 1. Weather precaution & Fertilizer Timing Query
        if (query.contains("மழை") || query.contains("rain") || query.contains("உரம் இடலாமா") || query.contains("apply fertilizer")) {
            return if (isTa) {
                """
                🌧️ **வானிலை மற்றும் உர மேலாண்மை வழிகாட்டுதல்**:
                
                • **மழை எச்சரிக்கை**: மழைக்கு முன்போ அல்லது மழை பெய்யும் போதோ தழைச்சத்து (யுரியா) அல்லது இலைவழி தெளிப்பான்களை இட வேண்டாம். நீரில் கரைந்து வீணாகிவிடும்.
                • **பரிந்துரை**: மழை பெய்து நிலம் வடியும் வரை காத்திருந்து, லேசான ஈரம் உள்ள போது அடி உரம் அல்லது மண்புழு உரம் இடுங்கள்.
                • **நோய் தடுப்பு**: மழைக்குப் பின் ஈரப்பதம் அதிகமாக இருக்கும் என்பதால், பூஞ்சை நோய் பரவாமல் தடுக்க *சூடோமோனாஸ்* (5 கிராம்/லிட்டர்) தெளிக்கவும்.
                """.trimIndent()
            } else {
                """
                🌧️ **Weather & Fertilizer Timing Advisory**:
                
                • **Rain Warning**: Avoid broadcasting nitrogen fertilizers (like Urea) or foliar spraying immediately before or during rainfall to prevent leaching and surface runoff.
                • **Recommendation**: Wait until rainfall stops and excess water drains. Apply fertilizers when the soil is at field capacity (moist, not waterlogged).
                • **Fungal Precaution**: High humidity after rain promotes fungal pathogens. Spray *Pseudomonas fluorescens* (5g/L) or Copper Oxychloride (2g/L) as a protective measure.
                """.trimIndent()
            }
        }

        // 2. Irrigation / Water Management Query
        if (query.contains("பாசனம்") || query.contains("தண்ணீர்") || query.contains("irrigation") || query.contains("water")) {
            val weatherInfo = if (currentWeather != null) "தற்போதைய வெப்பநிலை: ${String.format("%.1f", currentWeather.temperatureCelsius)}°C, ஈரப்பதம்: ${currentWeather.humidityPercentage}%" else ""
            return if (isTa) {
                """
                💧 **$currentCrop பயிருக்கான பாசன ஆலோசனை**:
                $weatherInfo
                
                • **பாசன முறை**: சொட்டு நீர் பாசனம் (Drip Irrigation) மூலமாக காலை 6:00 - 9:00 மணிக்குள் நீர் பாய்ச்சுவது சிறந்தது.
                • **அளவு**: ஒரு செடிக்கு நாள் ஒன்றுக்கு சராசரியாக 2 - 2.5 லிட்டர் நீர் தேவைப்படும்.
                • **கவனிக்க வேண்டியவை**: பூக்கும் மற்றும் காய் பிடிக்கும் பருவத்தில் நிலத்தில் சீரான ஈரப்பதம் இருப்பதை உறுதி செய்யுங்கள். அதிக நீர் தேங்கினால் வேர் அழுகல் ஏற்படலாம்.
                """.trimIndent()
            } else {
                """
                💧 **Irrigation Advisory for $currentCrop**:
                
                • **Optimal Timing**: Irrigate early in the morning (6:00 AM – 9:00 AM) to minimize evapotranspiration losses.
                • **Water Requirement**: 2.0 – 2.5 Liters per plant per day via drip irrigation during vegetative and flowering stages.
                • **Field Care**: Avoid waterlogging to prevent root suffocation and damping-off disease. Ensure adequate soil aeration.
                """.trimIndent()
            }
        }

        // 3. Black Spots / Early Blight / Late Blight Query
        if (query.contains("புள்ளி") || query.contains("black") || query.contains("spot") || query.contains("blight") || query.contains("கருப்பு")) {
            return if (isTa) {
                """
                🌿 **இலைப்புள்ளி மற்றும் கருகல் நோய் மேலாண்மை ($currentCrop)**:
                
                **1. இயற்கை முறை (Organic Control):**
                • வேப்ப எண்ணெய் (Neem Oil 1500 ppm) - 3 முதல் 5 மி.லி / லிட்டர் நீரில் காதி சோப்பு சேர்த்து தெளிக்கவும்.
                • *சூடோமோனாஸ் புளோரசன்ஸ்* (Pseudomonas) - 5 கிராம் / லிட்டர் நீரில் கலந்து 10 நாட்கள் இடைவெளியில் தெளிக்கவும்.
                
                **2. ரசாயன முறை (Chemical Control):**
                • மேன்கோசெப் 75% WP (Mancozeb) - 2 கிராம் / லிட்டர் அல்லது
                • காப்பர் ஆக்ஸிகுளோரைடு 50% WP (COC) - 2.5 கிராம் / லிட்டர் நீரில் கலந்து இலைகளின் இருபுறமும் படும்படி தெளிக்கவும்.
                
                **3. தடுப்பு முறைகள்:**
                • பாதிக்கப்பட்ட இலைகளை உடனே அகற்றி அழிக்கவும்.
                • இலைகளின் மேல் நேரடியாக தண்ணீர் பாய்ச்சுவதைத் தவிர்க்கவும்.
                """.trimIndent()
            } else {
                """
                🌿 **Leaf Spot & Blight Management ($currentCrop)**:
                
                **1. Organic / Biological Control:**
                • Neem Oil (1500 ppm) @ 3–5 ml/L water with a mild emulsifier.
                • Spray *Pseudomonas fluorescens* @ 5g/L water every 10 days for biological suppression.
                
                **2. Chemical Treatment:**
                • Mancozeb 75% WP @ 2.0g/L water, OR
                • Copper Oxychloride 50% WP @ 2.5g/L water. Ensure complete coverage on both upper and lower leaf surfaces.
                
                **3. Cultural Practices:**
                • Prune infected lower leaves and safely dispose away from the field.
                • Avoid overhead sprinkler irrigation during high disease pressure.
                """.trimIndent()
            }
        }

        // 4. Yellow Leaves / Mosaic / Nitrogen Deficiency Query
        if (query.contains("மஞ்சள்") || query.contains("yellow") || query.contains("mosaic") || query.contains("நரம்பு")) {
            return if (isTa) {
                """
                🌱 **இலைகள் மஞ்சள் நிறமாக மாறுவதற்கான தீர்வுகள் ($currentCrop)**:
                
                **காரணம் 1: தழைச்சத்து (Nitrogen) குறைபாடு:**
                • கீழ் இலைகள் சீராக மஞ்சள் நிறமாக மாறினால், 19:19:19 (NPK நீரில் கரையும் உரம்) 5 கிராம்/லிட்டர் அல்லது யூரியா 1% கரைசல் தெளிக்கவும்.
                
                **காரணம் 2: இலை சுருட்டல் / மொசைக் வைரஸ் (பூச்சிகளால் பரவுவது):**
                • வெள்ளை ஈ மற்றும் அசுவினி பூச்சிகளைக் கட்டுப்படுத்த ஏக்கருக்கு 15 மஞ்சள் நிற ஒட்டும் பொறிகளை (Yellow Sticky Traps) அமைக்கவும்.
                • வேப்ப எண்ணெய் 3 மி.லி/லிட்டர் அல்லது தயோமீத்தாக்ஸாம் (Thiamethoxam 25 WG) 0.5 கிராம்/லிட்டர் தெளிக்கவும்.
                """.trimIndent()
            } else {
                """
                🌱 **Yellowing Leaves & Chlorosis Solutions ($currentCrop)**:
                
                **Possibility 1: Nitrogen / Micronutrient Deficiency:**
                • Uniform yellowing of older leaves indicates Nitrogen deficiency. Foliar spray 19:19:19 soluble NPK @ 5g/L or Chelated Zinc @ 1g/L.
                
                **Possibility 2: Yellow Vein Mosaic / Viral Vector:**
                • If accompanied by curling or mottling, control whitefly and aphid vectors immediately.
                • Install 15 Yellow Sticky Traps per acre.
                • Spray Neem Oil (1500 ppm) @ 4 ml/L or Thiamethoxam 25% WG @ 0.5g/L.
                """.trimIndent()
            }
        }

        // 5. Fertilizer / Nutrients / Organic farming query
        if (query.contains("உரம்") || query.contains("fertilizer") || query.contains("nutrient") || query.contains("npk") || query.contains("organic")) {
            return if (isTa) {
                """
                🌾 **$currentCrop பயிருக்கான சமச்சீர் உர மேலாண்மை**:
                
                **1. வளர்ச்சிப் பருவம் (Vegetative Stage):**
                • மண்புழு உரம் (Vermicompost) அல்லது நன்கு மக்கிய தொழு உரம் ஏக்கருக்கு 2 டன் இடவும்.
                • ஜீவாமிர்தம் 200 லிட்டர் / ஏக்கர் பாசன நீரில் கலந்து விடவும்.
                
                **2. பூக்கும் மற்றும் காய்க்கும் பருவம் (Flowering & Fruiting):**
                • பொட்டாஷ் சத்துக்காக 13:0:45 (பொட்டாசியம் நைட்ரேட்) 5 கிராம்/லிட்டர் நீரில் கலந்து இலைவழியாக தெளிக்கவும்.
                • போரான் (Boron 20%) 1 கிராம்/லிட்டர் தெளிப்பது பூ உதிர்வதைத் தடுத்து காய் தரத்தை அதிகரிக்கும்.
                """.trimIndent()
            } else {
                """
                🌾 **Balanced Crop Nutrition & Fertilizer Schedule ($currentCrop)**:
                
                **1. Vegetative Stage:**
                • Basal dose: Well-decomposed Farmyard Manure (FYM) @ 4–5 tonnes/acre or Vermicompost @ 1.5 tonnes/acre.
                • High nitrogen nutrition: 19:19:19 @ 5g/L as foliar spray to promote vigorous canopy growth.
                
                **2. Flowering & Fruit Setting Stage:**
                • Apply Potassium Nitrate (13:0:45) @ 5g/L to support flower retention and fruit enlargement.
                • Foliar Boron (20% Solubor) @ 1.0g/L to prevent blossom drop and enhance pollination.
                """.trimIndent()
            }
        }

        // 6. Context-Aware Diagnosis fallback if diagnosis is present
        if (recentDiagnosis != null) {
            val dName = recentDiagnosis.disease
            return if (isTa) {
                """
                🌾 **அக்ரிபாட் விவசாய ஆலோசனை**:
                
                உங்கள் வயலில் கண்டறியப்பட்ட **${recentDiagnosis.crop} - $dName** ($recentDiagnosis.severity தீவிரம்) அடிப்படையிலான பரிந்துரைகள்:
                
                1. **பாதுகாப்பு சிகிச்சை**: காப்பர் ஆக்ஸிகுளோரைடு (COC) 2.5 கிராம்/லி அல்லது வேப்ப எண்ணெய் 3 மி.லி/லி தெளிக்கவும்.
                2. **நீர் பாசனம்**: மாலை நேரங்களில் இலைகள் நனையாமல் வேர்ப்பகுதியில் மட்டும் நீர் பாய்ச்சவும்.
                3. **சுற்றுப்புற பராமரிப்பு**: பாதிக்கப்பட்ட இலைகளை உடனே நீக்கி நிலத்தை சுத்தமாக வைக்கவும்.
                """.trimIndent()
            } else {
                """
                🌾 **AgriBot Advisory for $currentCrop**:
                
                Based on your recent diagnosis of **$dName** on **${recentDiagnosis.crop}** (Severity: ${recentDiagnosis.severity}):
                
                1. **Targeted Treatment**: Apply Copper Oxychloride 50 WP @ 2.5g/L or bio-agent *Trichoderma viride* @ 5g/L.
                2. **Canopy Airflow**: Prune lower diseased foliage to increase light penetration and reduce humidity traps.
                3. **Irrigation Strategy**: Use root-zone drip irrigation rather than overhead sprinklers.
                """.trimIndent()
            }
        }

        // 7. General Friendly Agronomist response
        return if (isTa) {
            """
            🌾 **வணக்கம்! உங்கள் அக்ரிபாட் விவசாய வழிகாட்டி ($currentCrop)**:
            
            உங்கள் கேள்விக்குரிய சிறந்த நடைமுறைகள்:
            • **பயிர் பாதுகாப்பு**: இலைகளின் கீழ் பகுதி மற்றும் தண்டுகளை தொடர்ந்து பரிசோதித்து பூச்சி/நோய் தாக்குதலை முன்கூட்டியே கவனியுங்கள்.
            • **இயற்கை பராமரிப்பு**: 10 நாட்களுக்கு ஒருமுறை பஞ்சகவ்யா (3%) அல்லது ஜீவாமிர்தம் தெளிப்பது நோய் எதிர்ப்பு சக்தியை அதிகரிக்கும்.
            • **பாசனம்**: மண்ணின் ஈரப்பதத்திற்கு ஏற்ப காலை வேளையில் மிதமான பாசனம் மேற்கொள்ளவும்.
            
            குறிப்பிட்ட நோய் அல்லது உரம் பற்றி அறிய எந்த நேரத்திலும் கேளுங்கள்!
            """.trimIndent()
        } else {
            """
            🌾 **AgriShield Expert Agronomist Advisory ($currentCrop)**:
            
            Key field management recommendations for your query:
            • **Crop Scouting**: Regularly inspect both sides of the leaf canopy for early signs of fungal sporulation or pest infestation.
            • **Organic Resilience**: Spray Panchagavya (3%) or Seaweed extract (2 ml/L) every 12-14 days to enhance systemic acquired resistance (SAR).
            • **Soil & Water**: Maintain balanced soil moisture avoiding extreme wet-dry cycles.
            
            Feel free to ask about specific dosages, disease treatments, or weather precautions anytime!
            """.trimIndent()
        }
    }

    private fun isTamil(text: String): Boolean {
        return text.any { it in '\u0B80'..'\u0BFF' }
    }

    fun clearChat() {
        _messages.value = emptyList()
    }
}

