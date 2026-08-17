package com.agrishield.app.data.ml

import com.agrishield.app.data.model.SoilData
import kotlin.math.abs

class SoilHealthEvaluator {

    /**
     * Evaluates NPK, pH, and Moisture values against agronomic targets for the specific crop.
     */
    fun evaluateSoil(
        n: Double,
        p: Double,
        k: Double,
        ph: Double,
        moisture: Double,
        crop: String = "Tomato"
    ): SoilData {
        // Standard optimal agronomic ranges for vegetable/staple crops
        val (optN, optP, optK, optPhMin, optPhMax) = when (crop.lowercase()) {
            "rice" -> Tuple5(120.0, 30.0, 150.0, 5.5, 6.5)
            "potato" -> Tuple5(150.0, 40.0, 200.0, 5.0, 6.5)
            "tomato" -> Tuple5(140.0, 35.0, 180.0, 6.0, 7.0)
            "corn", "maize" -> Tuple5(160.0, 35.0, 160.0, 5.8, 7.0)
            "pepper", "chilli" -> Tuple5(130.0, 30.0, 160.0, 6.0, 6.8)
            else -> Tuple5(140.0, 35.0, 170.0, 6.0, 7.0)
        }

        // Sub-index calculations (0 to 100)
        val nScore = (100 - (abs(n - optN) / optN * 100)).toInt().coerceIn(20, 100)
        val pScore = (100 - (abs(p - optP) / optP * 100)).toInt().coerceIn(20, 100)
        val kScore = (100 - (abs(k - optK) / optK * 100)).toInt().coerceIn(20, 100)
        
        val phScore = when {
            ph in optPhMin..optPhMax -> 100
            ph < optPhMin -> (100 - ((optPhMin - ph) * 30)).toInt().coerceIn(20, 100)
            else -> (100 - ((ph - optPhMax) * 30)).toInt().coerceIn(20, 100)
        }

        val moistureScore = when {
            moisture in 40.0..65.0 -> 100
            moisture < 40.0 -> (100 - ((40.0 - moisture) * 2)).toInt().coerceIn(20, 100)
            else -> (100 - ((moisture - 65.0) * 2)).toInt().coerceIn(20, 100)
        }

        val overallIndex = ((nScore * 0.25) + (pScore * 0.20) + (kScore * 0.20) + (phScore * 0.20) + (moistureScore * 0.15)).toInt()

        // Recommendations
        val recsEn = mutableListOf<String>()
        val recsTa = mutableListOf<String>()

        if (n < optN - 30) {
            recsEn.add("Nitrogen is Low: Apply Urea (35 kg/acre) or well-decomposed Farmyard Manure (FYM) to boost vegetative vigor.")
            recsTa.add("நைட்ரஜன் குறைவு: யூரியா (35 கிலோ/ஏக்கர்) அல்லது மக்கிய தொழுவுரம் இடவும்.")
        } else if (n > optN + 40) {
            recsEn.add("Nitrogen is High: Reduce urea to prevent excessive succulent growth and fungal susceptibility.")
            recsTa.add("நைட்ரஜன் அதிகம்: அதிக யூரியா இடுவதைத் தவிர்த்து பூஞ்சை நோய் அபாயத்தைக் குறைக்கவும்.")
        }

        if (p < optP - 10) {
            recsEn.add("Phosphorus is Low: Apply Single Super Phosphate (SSP 50 kg/acre) or DAP to stimulate strong root development.")
            recsTa.add("பாஸ்பரஸ் குறைவு: சூப்பர் பாஸ்பேட் (50 கிலோ/ஏக்கர்) அல்லது டி.ஏ.பி இட்டு வேர் வளர்ச்சியைத் தூண்டவும்.")
        }

        if (k < optK - 30) {
            recsEn.add("Potassium is Low: Apply Muriate of Potash (MOP 25 kg/acre) to improve disease resistance and fruit quality.")
            recsTa.add("பொட்டாசியம் குறைவு: பொட்டாஷ் (25 கிலோ/ஏக்கர்) இட்டு நோய் எதிர்ப்புத் திறன் மற்றும் காய் தரத்தை அதிகரிக்கவும்.")
        }

        if (ph < 5.8) {
            recsEn.add("Soil is Acidic (pH ${String.format("%.1f", ph)}): Apply Agricultural Lime (100 kg/acre) to neutralize soil acidity.")
            recsTa.add("மண் அமிலத்தன்மை கொண்டது (pH ${String.format("%.1f", ph)}): சுண்ணாம்பு (100 கிலோ/ஏக்கர்) இடவும்.")
        } else if (ph > 7.8) {
            recsEn.add("Soil is Alkaline (pH ${String.format("%.1f", ph)}): Apply Gypsum (150 kg/acre) and organic compost.")
            recsTa.add("மண் காரத்தன்மை கொண்டது (pH ${String.format("%.1f", ph)}): ஜிப்சம் (150 கிலோ/ஏக்கர்) மற்றும் மண்புழு உரம் இடவும்.")
        }

        if (recsEn.isEmpty()) {
            recsEn.add("Soil nutrients and pH are well-balanced for $crop. Maintain regular organic mulching.")
            recsTa.add("மண்ணின் சத்துக்கள் மற்றும் pH $crop பயிருக்கு உகந்த அளவில் உள்ளன. வழக்கமான கரிம மூடாக்கு தொடரவும்.")
        }

        return SoilData(
            id = java.util.UUID.randomUUID().toString(),
            nitrogenMgKg = n,
            phosphorusMgKg = p,
            potassiumMgKg = k,
            ph = ph,
            moisturePercent = moisture,
            isUserProvided = true,
            targetCrop = crop,
            healthIndex = overallIndex,
            recommendationsEn = recsEn,
            recommendationsTa = recsTa,
            testedDate = System.currentTimeMillis()
        )
    }

    private data class Tuple5(val n: Double, val p: Double, val k: Double, val phMin: Double, val phMax: Double)
}
