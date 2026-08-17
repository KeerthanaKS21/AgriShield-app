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
            "rice" -> 0.90
            "potato" -> 0.85
            "tomato" -> 0.80
            "apple" -> 0.75
            "pepper", "chilli" -> 0.70
            "corn", "maize" -> 0.60
            else -> 0.75
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

        val (primaryDisease, adviceEn, adviceTa) = generateAdvice(cropType, riskLevel, humidity, temp, rainMm)

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

    private fun generateAdvice(
        crop: String,
        risk: RiskLevel,
        humidity: Double,
        temp: Double,
        rainMm: Double
    ): Triple<String, String, String> {
        val cropLower = crop.lowercase()
        return when (risk) {
            RiskLevel.HIGH -> {
                val disease = if (cropLower.contains("rice")) "Rice Blast / Brown Spot"
                else if (cropLower.contains("potato") || cropLower.contains("tomato")) "Late Blight / Early Blight"
                else "Fungal Foliar Spot"

                Triple(
                    disease,
                    "High disease risk alert for $crop! High humidity (${humidity.toInt()}%) and warm temperature (${temp.toInt()}°C) create severe fungal outbreak conditions. Apply protective fungicide spray immediately and ensure field drainage.",
                    "எச்சரிக்கை: $crop பயிருக்கு அதிக நோய் அபாயம்! அதிக ஈரப்பதம் மற்றும் வெப்பம் காரணமாக பூஞ்சை நோய் பரவும் சூழல் உள்ளது. உடனடியாக பாதுகாப்பு பூஞ்சைக்கொல்லி தெளிக்கவும் மற்றும் வடிகால் வசதி செய்யவும்."
                )
            }
            RiskLevel.MEDIUM -> {
                val disease = if (cropLower.contains("rice")) "Mild Sheath Blight"
                else if (cropLower.contains("tomato")) "Early Blight / Leaf Spot"
                else "Powdery Mildew"

                Triple(
                    disease,
                    "Moderate disease risk detected for $crop. Monitor lower leaves closely for early spotting. Avoid excessive urea application and use neem oil (3ml/L) as a preventive measure.",
                    "மிதமான நோய் அபாயம் உள்ளது. கீழ் இலைகளில் புள்ளிகள் உள்ளதா என கண்காணிக்கவும். அதிக யூரியா இடுவதைத் தவிர்த்து, முன்னெச்சரிக்கையாக வேப்பெண்ணெய் (3 மி.லி/லிட்டர்) தெளிக்கவும்."
                )
            }
            RiskLevel.LOW -> {
                Triple(
                    "No Threat (Foliage Safe)",
                    "Favorable low-risk weather conditions. Current climate is not conducive for rapid disease outbreaks. Continue standard crop care and nutrition management.",
                    "குறைந்த அபாய நிலை. தற்போதைய வானிலை பயிருக்கு உகந்தது. நோய் தாக்கும் சூழல் இல்லை. வழக்கமான பயிர் பராமரிப்பைத் தொடரவும்."
                )
            }
        }
    }
}
