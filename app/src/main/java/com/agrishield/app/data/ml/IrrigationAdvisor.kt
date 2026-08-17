package com.agrishield.app.data.ml

import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.IrrigationAction
import com.agrishield.app.data.model.IrrigationAdvice
import com.agrishield.app.data.model.WeatherData

class IrrigationAdvisor {

    /**
     * Generates dynamic irrigation advisory based on live weather data, rain forecast, and soil moisture.
     */
    fun getAdvice(
        weather: WeatherData,
        forecast: List<ForecastItem>,
        soilMoisture: Double = 45.0,
        crop: String = "Tomato"
    ): IrrigationAdvice {
        val temp = weather.temperatureCelsius
        val humidity = weather.humidityPercentage
        val currentRain = weather.rainMmLast3h

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
                    "Rain forecast (${maxRainProb}% chance within 12h). Avoid irrigation to prevent waterlogging and root asphyxiation.",
                    "அடுத்த 12 மணி நேரத்தில் மழை பெய்ய வாய்ப்புள்ளது (${maxRainProb}%). வேரழுகல் மற்றும் தண்ணீர் தேங்குவதைத் தவிர்க்க பாசனத்தை ஒத்திவைக்கவும்."
                )
            }
            soilMoisture > 70.0 -> {
                Tuple4(
                    IrrigationAction.HOLD_DO_NOT_IRRIGATE,
                    0.0,
                    "Soil moisture is high (${soilMoisture.toInt()}%). Soil is saturated; no additional irrigation needed today.",
                    "மண் ஈரப்பதம் அதிகமாக உள்ளது (${soilMoisture.toInt()}%). இன்று கூடுதல் பாசனம் தேவையில்லை."
                )
            }
            temp > 35.0 && humidity < 40 -> {
                Tuple4(
                    IrrigationAction.INCREASE,
                    6.5,
                    "High heat (${temp.toInt()}°C) and dry air accelerate evapotranspiration. Increase irrigation by 25% during evening or early morning.",
                    "அதிக வெப்பம் (${temp.toInt()}°C) மற்றும் உலர் காற்று காரணமாக நீரிழப்பு அதிகம். அதிகாலை அல்லது மாலையில் 25% கூடுதல் பாசனம் செய்யவும்."
                )
            }
            soilMoisture < 35.0 -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    5.0,
                    "Soil moisture is dropping (${soilMoisture.toInt()}%). Provide regular drip irrigation to maintain root zone moisture.",
                    "மண் ஈரப்பதம் குறைகிறது (${soilMoisture.toInt()}%). வழக்கமான சொட்டு நீர்ப்பாசனம் செய்யவும்."
                )
            }
            else -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    4.0,
                    "Optimal weather and soil conditions. Apply standard maintenance drip irrigation.",
                    "வானிலை மற்றும் மண் ஈரப்பதம் சீராக உள்ளது. வழக்கமான பராமரிப்பு பாசனம் செய்யவும்."
                )
            }
        }

        return IrrigationAdvice(
            action = action,
            waterVolumeLitersPerSqm = volume,
            reasonEn = reasonEn,
            reasonTa = reasonTa,
            rainExpectedNext12h = willRainSoon,
            nextIrrigationWindow = if (willRainSoon) "After rain subsides" else "Tomorrow Morning (6:00 AM - 8:00 AM)",
            generatedAt = System.currentTimeMillis()
        )
    }

    private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
