package com.agrishield.app.data.ml

import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.GrowthStage
import com.agrishield.app.data.model.RiskLevel
import com.agrishield.app.data.model.WeatherData
import kotlin.math.exp
import kotlin.math.pow

class CropRiskEngine {

    /**
     * Calculates crop disease risk using meteorological & epidemiological models (BLITECAST & Wallin index),
     * specifically aligned with the crop's active growth stage and timeline.
     */
    fun calculateRisk(
        weather: WeatherData,
        cropType: String = "Tomato",
        growthStage: GrowthStage = GrowthStage.VEGETATIVE,
        daysSinceSowing: Int = 30,
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

        // 4. Stage-Vulnerability Factor
        val stageSusceptibility = when (growthStage) {
            GrowthStage.SOWING, GrowthStage.SEEDLING -> 0.85 // High damping off vulnerability
            GrowthStage.VEGETATIVE -> 0.70
            GrowthStage.FLOWERING -> 0.90 // Peak vulnerable to blossom & foliar pathogens
            GrowthStage.FRUITING -> 0.85 // Fruit rot & blight sensitivity
            GrowthStage.HARVEST -> 0.50
        }

        // 5. Crop Baseline Susceptibility
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

        // 6. Composite Infection Risk Score (0.0 to 1.0)
        val fungalRiskIndex = (tempRisk * humidityRisk)
        var compositeScore = (0.35 * fungalRiskIndex) + (0.25 * rainRisk) + (0.20 * stageSusceptibility) + (0.20 * cropSusceptibility)

        if (recentDiseaseDiagnosis != null && !recentDiseaseDiagnosis.contains("healthy", ignoreCase = true)) {
            compositeScore = (compositeScore * 1.2).coerceAtMost(1.0)
        }

        val scorePercent = (compositeScore * 100).toInt().coerceIn(5, 98)

        val riskLevel = when {
            scorePercent >= 60 -> RiskLevel.HIGH
            scorePercent >= 35 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        val stageName = when (growthStage) {
            GrowthStage.SOWING -> "Sowing / விதைப்பு"
            GrowthStage.SEEDLING -> "Seedling / நாற்று"
            GrowthStage.VEGETATIVE -> "Vegetative / தழை வளர்ச்சி"
            GrowthStage.FLOWERING -> "Flowering / பூக்கும் பருவம்"
            GrowthStage.FRUITING -> "Fruiting / காய்க்கும் பருவம்"
            GrowthStage.HARVEST -> "Harvest / அறுவடை"
        }

        val riskFactors = mutableListOf<String>()
        riskFactors.add("Growth Stage: $stageName (Day $daysSinceSowing)")
        if (humidity >= 75) riskFactors.add("High Relative Humidity (${humidity.toInt()}%) favorable for fungal spore germination")
        if (temp in 18.0..28.0) riskFactors.add("Optimal fungal incubation temperature (${temp.toInt()}°C)")
        if (rainMm > 1.0) riskFactors.add("Leaf surface moisture detected from rainfall")
        if (wind > 20.0) riskFactors.add("Wind (${wind.toInt()} km/h) dispersing spore pathogens")

        val (primaryDisease, adviceEn, adviceTa) = generateStageAndTimelineAdvice(
            cropType,
            growthStage,
            daysSinceSowing,
            riskLevel,
            humidity,
            temp
        )

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

    private fun generateStageAndTimelineAdvice(
        crop: String,
        stage: GrowthStage,
        days: Int,
        risk: RiskLevel,
        humidity: Double,
        temp: Double
    ): Triple<String, String, String> {
        val cropLower = crop.lowercase()

        // Stage & Crop Specific Disease Identification
        val (diseaseName, stageAdviceEn, stageAdviceTa) = when (stage) {
            GrowthStage.SOWING, GrowthStage.SEEDLING -> {
                val dis = if (cropLower.contains("rice")) "Damping Off & Seedling Blast"
                          else if (cropLower.contains("chilli")) "Pythium Damping Off & Collar Rot"
                          else "Seedling Damping Off (Pythium / Rhizoctonia)"
                Triple(
                    dis,
                    "In the seedling stage (Day $days), young shoots are susceptible to $dis in moist soil. Drench nursery beds with Trichoderma viride (10g/L) or Copper Oxychloride (2.5g/L). Avoid waterlogging.",
                    "நாற்றுப் பருவத்தில் (நாள் $days), இளம் செடிகளுக்கு '$dis' தாக்கும் அபாயம் உள்ளது. டிரைக்கோடெர்மா விரிடி (10 கிராம்/லிட்டர்) கொண்டு நாற்றங்கால் நனைக்கவும். தண்ணீர் தேங்குவதைத் தவிர்க்கவும்."
                )
            }
            GrowthStage.VEGETATIVE -> {
                val dis = if (cropLower.contains("rice")) "Rice Blast & Brown Spot"
                          else if (cropLower.contains("cotton")) "Bacterial Blight & Jassid damage"
                          else if (cropLower.contains("banana")) "Sigatoka Leaf Spot"
                          else "Early Blight & Foliar Leaf Spot"
                Triple(
                    dis,
                    "During vegetative expansion (Day $days), protect lush foliage from $dis. Apply preventive spray of Neem Oil (1500ppm @ 4ml/L) or Pseudomonas (5g/L). Maintain balanced nitrogen to avoid excessive tender foliage.",
                    "தழை வளர்ச்சிப் பருவத்தில் (நாள் $days), பசுமையான இலைகளை '$dis' தாக்காமல் பாதுகாக்க வேப்பெண்ணெய் (4 மி.லி/லி) அல்லது சூடோமோனாஸ் தெளிக்கவும். அதிக யூரியா இடுவதைத் தவிர்க்கவும்."
                )
            }
            GrowthStage.FLOWERING -> {
                val dis = if (cropLower.contains("chilli")) "Flower Rot & Thrips Curling"
                          else if (cropLower.contains("cotton")) "Square Dropping & Bollworm"
                          else if (cropLower.contains("tomato")) "Blossom Blight & Septoria Spot"
                          else "Blossom Blight & Powdery Mildew"
                Triple(
                    dis,
                    "Crucial flowering stage (Day $days). High humidity (${humidity.toInt()}%) triggers $dis and blossom drop. Spray Boron 20% (1g/L) + Mancozeb (2g/L) in the evening to protect flowers without disturbing pollinators.",
                    "முக்கியமான பூக்கும் பருவம் (நாள் $days). அதிக ஈரப்பதம் காரணமாக '$dis' ஏற்பட்டு பூ உதிர வாய்ப்புள்ளது. மாலையில் போரான் (1 கிராம்/லி) + மேன்கோசெப் (2 கிராம்/லி) தெளித்து பூக்களைக் காப்பாற்றவும்."
                )
            }
            GrowthStage.FRUITING -> {
                val dis = if (cropLower.contains("rice")) "Panicle Blast & Grain Discoloration"
                          else if (cropLower.contains("chilli")) "Anthracnose Fruit Rot (Dieback)"
                          else if (cropLower.contains("tomato")) "Late Blight & Fruit Borer"
                          else if (cropLower.contains("banana")) "Cigar End Rot & Bunch Spot"
                          else "Fruit Rot & Late Blight"
                Triple(
                    dis,
                    "Fruit development stage (Day $days). Guard against $dis. Spray Potassium Nitrate 13:0:45 (5g/L) to boost fruit rind immunity and apply bio-fungicide to prevent fruit spotting.",
                    "காய்க்கும் பருவம் (நாள் $days). '$dis' நோயிலிருந்து காய்களைப் பாதுகாக்க பொட்டாசியம் நைட்ரேட் (5 கிராம்/லி) மற்றும் பாதுகாப்பு பூஞ்சைக்கொல்லி தெளிக்கவும்."
                )
            }
            GrowthStage.HARVEST -> {
                Triple(
                    "Post-Harvest Foliar Decay",
                    "Harvest stage (Day $days). Adhere strictly to Pre-Harvest Intervals (PHI). Withhold synthetic chemical sprays. Grade and harvest in dry weather.",
                    "அறுவடைப் பருவம் (நாள் $days). இரசாயன மருந்துகள் தெளிப்பதைத் தவிர்க்கவும். தெளிவான வறண்ட வானிலையில் பயிரை அறுவடை செய்து தரம் பிரிக்கவும்."
                )
            }
        }

        val riskPrefixEn = when (risk) {
            RiskLevel.HIGH -> "HIGH WEATHER RISK: "
            RiskLevel.MEDIUM -> "MODERATE RISK: "
            RiskLevel.LOW -> "LOW RISK: "
        }

        val riskPrefixTa = when (risk) {
            RiskLevel.HIGH -> "அதிக அபாயம்: "
            RiskLevel.MEDIUM -> "மிதமான அபாயம்: "
            RiskLevel.LOW -> "குறைந்த அபாயம்: "
        }

        return Triple(
            diseaseName,
            "$riskPrefixEn$stageAdviceEn",
            "$riskPrefixTa$stageAdviceTa"
        )
    }
}
