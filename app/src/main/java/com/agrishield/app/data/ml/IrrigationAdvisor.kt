package com.agrishield.app.data.ml

import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.IrrigationAction
import com.agrishield.app.data.model.IrrigationAdvice
import com.agrishield.app.data.model.WeatherData

class IrrigationAdvisor {

    /**
     * Generates dynamic crop-specific irrigation advisory based on live weather data, rain forecast, and soil moisture.
     */
    fun getAdvice(
        weather: WeatherData,
        forecast: List<ForecastItem> = emptyList(),
        soilMoisture: Double = 48.0,
        crop: String = "Tomato"
    ): IrrigationAdvice {
        val temp = weather.temperatureCelsius
        val humidity = weather.humidityPercentage
        val currentRain = weather.rainMmLast3h
        val cropLower = crop.lowercase()

        // Check if rain expected in next 12 hours (first 4 3-hour forecast slots)
        val next12hForecast = forecast.take(4)
        val maxRainProb = if (next12hForecast.isNotEmpty()) {
            next12hForecast.maxOf { it.rainProbabilityPercent }
        } else 0

        val willRainSoon = maxRainProb >= 50 || currentRain > 1.0

        val (action, volume, reasonEn, reasonTa) = when {
            willRainSoon -> {
                Tuple4(
                    IrrigationAction.HOLD_DO_NOT_IRRIGATE,
                    0.0,
                    "Rain forecast (${maxRainProb}% chance within 12h). Avoid irrigation for $crop to prevent waterlogging and root rot.",
                    "அடுத்த 12 மணி நேரத்தில் மழைக்கு வாய்ப்புள்ளது (${maxRainProb}%). $crop பயிருக்கு வேரழுகல் மற்றும் தண்ணீர் தேங்குவதைத் தவிர்க்க பாசனத்தை ஒத்திவைக்கவும்."
                )
            }
            cropLower.contains("rice") || cropLower.contains("paddy") || cropLower.contains("நெல்") -> {
                if (soilMoisture < 60.0) {
                    Tuple4(
                        IrrigationAction.INCREASE,
                        8.0,
                        "Paddy requires standing water (2-3 cm) during vegetative/tillering stage. Irrigate field to maintain continuous moisture layer.",
                        "நெல் பயிருக்கு தூர் கட்டும் பருவத்தில் 2-3 செ.மீ நீர் மட்டம் அவசியம். சீரான நீர் தேங்கும் வகையில் பாசனம் செய்யவும்."
                    )
                } else {
                    Tuple4(
                        IrrigationAction.NORMAL,
                        4.0,
                        "Optimal standing water level in paddy field. Maintain submergence without overflowing.",
                        "நெல் வயலில் போதுமான நீர் மட்டம் உள்ளது. தற்போதைய நீர் அளவை பராமரிக்கவும்."
                    )
                }
            }
            cropLower.contains("banana") || cropLower.contains("வாழை") -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    15.0,
                    "Banana crop requires 15-20 Liters/plant/day. High transpiration rate in ${temp.toInt()}°C. Irrigate via drip for 1 hour early morning.",
                    "வாழை மரத்திற்கு நாள் ஒன்றுக்கு 15-20 லிட்டர் தண்ணீர் தேவை. காலை 6:00 - 8:00 மணிக்குள் 1 மணி நேரம் சொட்டு நீர் பாசனம் செய்யவும்."
                )
            }
            cropLower.contains("sugarcane") || cropLower.contains("கரும்பு") -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    7.0,
                    "Sugarcane requires moderate furrow irrigation every 7-10 days. Ensure mulch layer between rows to reduce evaporation.",
                    "கரும்பு பயிருக்கு 7-10 நாட்களுக்கு ஒருமுறை பார் சால் பாசனம் தேவை. நீர் ஆவியாவதைத் தடுக்க சோகை மூடாக்கு இடவும்."
                )
            }
            cropLower.contains("cotton") || cropLower.contains("பருத்தி") -> {
                if (soilMoisture > 65.0) {
                    Tuple4(
                        IrrigationAction.HOLD_DO_NOT_IRRIGATE,
                        0.0,
                        "Cotton is sensitive to excessive moisture. Current soil moisture (${soilMoisture.toInt()}%) is ample. Do not over-irrigate.",
                        "பருத்தி அதிக ஈரப்பதத்தை தாங்காது. மண் ஈரப்பதம் போதுமானது (${soilMoisture.toInt()}%). அதிக பாசனம் செய்வதைத் தவிர்க்கவும்."
                    )
                } else {
                    Tuple4(
                        IrrigationAction.NORMAL,
                        4.5,
                        "Provide alternate furrow irrigation during square/boll formation stage. Avoid flooding root zone.",
                        "பூ மற்றும் காய் பிடிக்கும் பருவத்தில் மிதமான பார் சால் பாசனம் செய்யவும். வேர் பகுதியில் நீர் தேங்கக் கூடாது."
                    )
                }
            }
            cropLower.contains("chilli") || cropLower.contains("மிளகாய்") -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    3.5,
                    "Chilli thrives with alternate-day light drip irrigation (35-45 mins). Avoid moisture stress during flowering to prevent blossom drop.",
                    "மிளகாய் பயிருக்கு ஒரு நாள் விட்டு ஒரு நாள் 40 நிமிடங்கள் மிதமான சொட்டு நீர்ப்பாசனம் செய்யவும். பூ உதிர்வதைத் தடுக்க சீரான ஈரம் தேவை."
                )
            }
            temp > 35.0 && humidity < 40 -> {
                Tuple4(
                    IrrigationAction.INCREASE,
                    6.0,
                    "High ambient temperature (${temp.toInt()}°C) and low humidity accelerate evapotranspiration in $crop. Increase drip cycle by 20%.",
                    "அதிக வெப்பம் (${temp.toInt()}°C) காரணமாக $crop பயிரில் நீரிழப்பு அதிகம். காலையில் 20% கூடுதல் சொட்டு நீர் பாசனம் செய்யவும்."
                )
            }
            soilMoisture < 35.0 -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    5.0,
                    "Soil moisture is dipping (${soilMoisture.toInt()}%). Run drip irrigation for 45 minutes to recharge root zone.",
                    "மண் ஈரப்பதம் குறைகிறது (${soilMoisture.toInt()}%). வேர் பகுதிக்கு 45 நிமிடங்கள் சொட்டு நீர் பாய்ச்சவும்."
                )
            }
            else -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    4.0,
                    "Optimal weather & soil moisture balance for $crop. Maintain regular morning drip schedule.",
                    "$crop பயிருக்கு வானிலை மற்றும் மண் ஈரப்பதம் சீராக உள்ளது. வழக்கமான காலை பாசன அட்டவணையைப் பின்பற்றவும்."
                )
            }
        }

        return IrrigationAdvice(
            action = action,
            waterVolumeLitersPerSqm = volume,
            reasonEn = reasonEn,
            reasonTa = reasonTa,
            rainExpectedNext12h = willRainSoon,
            nextIrrigationWindow = if (willRainSoon) "After rain subsides" else "Tomorrow Morning (6:00 AM - 7:30 AM)",
            generatedAt = System.currentTimeMillis()
        )
    }

    private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
