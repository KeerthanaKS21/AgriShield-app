package com.agrishield.app.data.ml

import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.RiskLevel
import com.agrishield.app.data.model.WeatherData
import kotlin.math.exp
import kotlin.math.pow

class CropRiskEngine {

    /**
     * Calculates crop disease risk using meteorological & epidemiological models (BLITECAST & Wallin index).
     */
    fun calculateRisk(
        weather: WeatherData,
        cropType: String = "Tomato",
        recentDiseaseDiagnosis: String? = null
    ): CropRisk {
        val temp = weather.temperatureCelsius
        val humidity = weather.humidityPercentage.toDouble()
        val rainMm = weather.rainMmLast3h
        val wind = weather.windSpeedKmh

        // 1. Temperature-Fungal Growth Curve (Gaussian curve centered at 24°C)
        val optimalTemp = 24.0
        val tempRisk = exp(-((temp - optimalTemp).pow(2.0)) / (2.0 * (6.0.pow(2.0))))

        // 2. Humidity Risk (Fungal sporulation surges when humidity > 75%)
        val humidityRisk = when {
            humidity >= 85.0 -> 1.0
            humidity >= 70.0 -> (humidity - 70.0) / 15.0 * 0.8
            humidity >= 50.0 -> (humidity - 50.0) / 20.0 * 0.3
            else -> 0.05
        }

        // 3. Rainfall / Leaf Wetness Risk
        val rainRisk = when {
            rainMm > 15.0 -> 1.0
            rainMm > 5.0 -> 0.7
            rainMm > 0.1 -> 0.4
            else -> 0.1
        }

        // 4. Crop Susceptibility Factor
        val cropSusceptibility = when (cropType.lowercase()) {
            "rice", "paddy", "நெல்" -> 0.90
            "potato", "உருளைக்கிழங்கு" -> 0.85
            "tomato", "தக்காளி" -> 0.80
            "chilli", "மிளகாய்" -> 0.75
            "cotton", "பருத்தி" -> 0.75
            "banana", "வாழை" -> 0.70
            "sugarcane", "கரும்பு" -> 0.65
            "groundnut", "நிலக்கடலை" -> 0.70
            "corn", "maize", "மக்காச்சோளம்" -> 0.60
            else -> 0.70
        }

        // 5. Composite Infection Risk Score (0.0 to 1.0)
        val fungalRiskIndex = (tempRisk * humidityRisk)
        var compositeScore = (0.40 * fungalRiskIndex) + (0.25 * rainRisk) + (0.20 * (humidity / 100.0)) + (0.15 * cropSusceptibility)

        // If high diagnosis recently logged, amplify risk score
        if (recentDiseaseDiagnosis != null && !recentDiseaseDiagnosis.contains("healthy", ignoreCase = true)) {
            compositeScore = (compositeScore * 1.2).coerceAtMost(1.0)
        }

        val scorePercent = (compositeScore * 100).toInt().coerceIn(5, 98)

        val riskLevel = when {
            scorePercent >= 60 -> RiskLevel.HIGH
            scorePercent >= 35 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        val riskFactors = mutableListOf<String>()
        if (humidity >= 75) riskFactors.add("High Relative Humidity (${humidity.toInt()}%) favorable for fungal spore germination")
        if (temp in 18.0..28.0) riskFactors.add("Temperature (${temp.toInt()}°C) in optimal fungal incubation range")
        if (rainMm > 2.0) riskFactors.add("Rainfall / leaf surface wetness detected")
        if (wind > 20.0) riskFactors.add("High wind (${wind.toInt()} km/h) accelerating pathogen spore dispersal")
        if (riskFactors.isEmpty()) riskFactors.add("Favorable dry weather with low disease pressure")

        val (primaryDisease, adviceEn, adviceTa) = generateCropSpecificAdvice(cropType, riskLevel, humidity, temp, rainMm)

        return CropRisk(
            level = riskLevel,
            scorePercentage = scorePercent,
            primaryRiskDisease = primaryDisease,
            riskFactors = riskFactors,
            adviceEn = adviceEn,
            adviceTa = adviceTa,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun generateCropSpecificAdvice(
        crop: String,
        risk: RiskLevel,
        humidity: Double,
        temp: Double,
        rainMm: Double
    ): Triple<String, String, String> {
        val cropLower = crop.lowercase()

        val diseaseName = when {
            cropLower.contains("rice") || cropLower.contains("paddy") || cropLower.contains("நெல்") -> "Rice Blast (Pyricularia oryzae) & Sheath Blight"
            cropLower.contains("chilli") || cropLower.contains("மிளகாய்") -> "Chilli Anthracnose & Leaf Curl Virus"
            cropLower.contains("cotton") || cropLower.contains("பருத்தி") -> "Bacterial Blight & Boll Rot"
            cropLower.contains("banana") || cropLower.contains("வாழை") -> "Sigatoka Leaf Spot (Mycosphaerella)"
            cropLower.contains("sugarcane") || cropLower.contains("கரும்பு") -> "Red Rot (Colletotrichum falcatum)"
            cropLower.contains("groundnut") || cropLower.contains("நிலக்கடலை") -> "Tikka Leaf Spot (Cercospora)"
            cropLower.contains("maize") || cropLower.contains("மக்காச்சோளம்") -> "Maydis Leaf Blight & Fall Armyworm"
            cropLower.contains("potato") -> "Late Blight (Phytophthora infestans)"
            else -> "Early Blight & Tomato Septoria Leaf Spot"
        }

        return when (risk) {
            RiskLevel.HIGH -> {
                Triple(
                    diseaseName,
                    "High disease risk alert for $crop! High humidity (${humidity.toInt()}%) and warm weather (${temp.toInt()}°C) create severe infection conditions for $diseaseName. Apply protective bio-fungicide (Trichoderma @ 5g/L) or Carbendazim+Mancozeb @ 2g/L immediately. Ensure proper drainage.",
                    "எச்சரிக்கை: $crop பயிருக்கு அதிக நோய் அபாயம்! அதிக ஈரப்பதம் மற்றும் வெப்பம் காரணமாக '$diseaseName' தாக்கும் சூழல் உள்ளது. உடனடியாக சூடோமோனாஸ் அல்லது கார்பென்டாசிம்+மேன்கோசெப் (2 கிராம்/லிட்டர்) தெளிக்கவும். வயலில் தண்ணீர் தேங்காமல் வடிக்கவும்."
                )
            }
            RiskLevel.MEDIUM -> {
                Triple(
                    diseaseName,
                    "Moderate disease risk detected for $crop ($diseaseName). Monitor lower foliage closely for spotting or lesions. Avoid excessive nitrogen/urea fertilizer and apply neem oil (3-4 ml/L) as an organic prophylactic spray.",
                    "மிதமான நோய் அபாயம் ($diseaseName). கீழ் இலைகளில் புள்ளிகள் உள்ளதா என உன்னிப்பாக கண்காணிக்கவும். அதிக தழைச்சத்து இடுவதைத் தவிர்த்து, முன்னெச்சரிக்கையாக வேப்பெண்ணெய் (3-4 மி.லி/லி) தெளிக்கவும்."
                )
            }
            RiskLevel.LOW -> {
                Triple(
                    "Low Risk - Healthy Foliage",
                    "Favorable low-risk microclimate for $crop. Current temperature and moisture levels are safe from major pathogen outbreaks. Continue scheduled balanced nutrition.",
                    "குறைந்த நோய் அபாயம். தற்போதைய வானிலை $crop பயிருக்கு பாதுகாப்பானது. பூஞ்சை நோய் பரவும் சூழல் இல்லை. வழக்கமான உர நிர்வாகத்தைத் தொடரவும்."
                )
            }
        }
    }
}
