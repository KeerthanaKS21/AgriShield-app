package com.agrishield.app.data.ml

import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.ConfidenceLevel
import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.data.model.FarmHealth
import com.agrishield.app.data.model.HealthRating
import com.agrishield.app.data.model.RiskLevel
import com.agrishield.app.data.model.SoilData

class FarmHealthCalculator {

    // Configurable component weights (must sum to 1.0)
    var weightDisease: Double = 0.35
    var weightWeatherRisk: Double = 0.25
    var weightSoil: Double = 0.20
    var weightCareRoutine: Double = 0.20

    /**
     * Calculates genuine composite Farm Health Score (0-100) based on all live data components.
     */
    fun calculateScore(
        recentDiagnosis: Diagnosis?,
        currentRisk: CropRisk?,
        soilData: SoilData?,
        careTasks: List<CareTask>
    ): FarmHealth {
        // 1. Disease Health Component (0 to 100)
        val diseaseScore = when {
            recentDiagnosis == null -> 85 // Neutral starting baseline
            recentDiagnosis.disease.contains("healthy", ignoreCase = true) -> 98
            recentDiagnosis.confidenceLevel == ConfidenceLevel.HIGH -> 35 // Severe active infection
            recentDiagnosis.confidenceLevel == ConfidenceLevel.MEDIUM -> 60 // Moderate infection
            else -> 75 // Low confidence detection
        }

        // 2. Weather Risk Component (0 to 100)
        val weatherScore = when (currentRisk?.level) {
            RiskLevel.HIGH -> (100 - (currentRisk.scorePercentage)).coerceIn(15, 45)
            RiskLevel.MEDIUM -> (100 - (currentRisk.scorePercentage)).coerceIn(50, 75)
            RiskLevel.LOW -> (100 - (currentRisk.scorePercentage)).coerceIn(80, 98)
            null -> 80
        }

        // 3. Soil Component (0 to 100)
        val soilScore = soilData?.healthIndex ?: 80

        // 4. Care Routine Component (0 to 100)
        val careScore = if (careTasks.isEmpty()) {
            80
        } else {
            val completed = careTasks.count { it.isCompleted }
            val ratio = completed.toDouble() / careTasks.size.toDouble()
            (50 + (ratio * 50)).toInt().coerceIn(50, 100)
        }

        // 5. Weighted Combination Formula
        val compositeScore = (
            (diseaseScore * weightDisease) +
            (weatherScore * weightWeatherRisk) +
            (soilScore * weightSoil) +
            (careScore * weightCareRoutine)
        ).toInt().coerceIn(0, 100)

        val rating = when {
            compositeScore >= 85 -> HealthRating.EXCELLENT
            compositeScore >= 70 -> HealthRating.GOOD
            compositeScore >= 50 -> HealthRating.MODERATE
            else -> HealthRating.CRITICAL
        }

        val (summaryEn, summaryTa) = generateSummary(rating, compositeScore, diseaseScore, weatherScore)

        return FarmHealth(
            score = compositeScore,
            rating = rating,
            diseaseComponentScore = diseaseScore,
            weatherRiskComponentScore = weatherScore,
            soilComponentScore = soilScore,
            careRoutineScore = careScore,
            summaryEn = summaryEn,
            summaryTa = summaryTa,
            calculatedAt = System.currentTimeMillis()
        )
    }

    private fun generateSummary(
        rating: HealthRating,
        composite: Int,
        diseaseScore: Int,
        weatherScore: Int
    ): Pair<String, String> {
        return when (rating) {
            HealthRating.EXCELLENT -> Pair(
                "Farm health is in top condition ($composite/100). Robust crop vigor, optimal soil balance, and minimal weather risk.",
                "பண்ணை மிகச் சிறந்த ஆரோக்கிய நிலையில் உள்ளது ($composite/100). சிறந்த பயிர் வளர்ச்சி மற்றும் குறைந்த நோய் அபாயம்."
            )
            HealthRating.GOOD -> Pair(
                "Farm health is good ($composite/100). Stable growing conditions with standard maintenance recommended.",
                "பண்ணை நல்ல நிலையில் உள்ளது ($composite/100). வழக்கமான பராமரிப்பு போதுமானது."
            )
            HealthRating.MODERATE -> Pair(
                "Farm health is moderate ($composite/100). Weather risk ($weatherScore/100) or disease pressure requires attention.",
                "பண்ணை நடுத்தர நிலையில் உள்ளது ($composite/100). வானிலை அல்லது பயிர் நோய் கண்காணிப்பு தேவைப்படுகிறது."
            )
            HealthRating.CRITICAL -> Pair(
                "Farm health is critical ($composite/100). Immediate intervention required for active crop infection ($diseaseScore/100).",
                "எச்சரிக்கை: பண்ணை ஆபத்தான நிலையில் உள்ளது ($composite/100). உடனடியாக பயிர் சிகிச்சை மற்றும் பாதுகாப்பு நடவடிக்கைகள் தேவை."
            )
        }
    }
}
