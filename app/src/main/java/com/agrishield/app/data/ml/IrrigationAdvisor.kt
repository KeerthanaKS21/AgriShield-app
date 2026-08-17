package com.agrishield.app.data.ml

import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.GrowthStage
import com.agrishield.app.data.model.IrrigationAction
import com.agrishield.app.data.model.IrrigationAdvice
import com.agrishield.app.data.model.WeatherData

class IrrigationAdvisor {

    /**
     * Generates dynamic crop-specific irrigation advisory based on live weather data, rain forecast, soil moisture,
     * the crop's active growth stage, and upcoming timeline care tasks.
     */
    fun getAdvice(
        weather: WeatherData,
        forecast: List<ForecastItem> = emptyList(),
        soilMoisture: Double = 48.0,
        crop: String = "Tomato",
        growthStage: GrowthStage = GrowthStage.VEGETATIVE,
        daysSinceSowing: Int = 30,
        scheduledTasks: List<CareTask> = emptyList()
    ): IrrigationAdvice {
        val temp = weather.temperatureCelsius
        val humidity = weather.humidityPercentage
        val currentRain = weather.rainMmLast3h
        val cropLower = crop.lowercase()

        // Check if rain expected in next 12 hours
        val next12hForecast = forecast.take(4)
        val maxRainProb = if (next12hForecast.isNotEmpty()) {
            next12hForecast.maxOf { it.rainProbabilityPercent }
        } else 0

        val willRainSoon = maxRainProb >= 50 || currentRain > 1.0

        // Check if any upcoming task in timeline interacts with irrigation (e.g. Fertilizing, Spraying)
        val hasFertilizerTask = scheduledTasks.any { !it.isCompleted && (it.title.contains("Fertilizer", ignoreCase = true) || it.title.contains("Urea", ignoreCase = true) || it.title.contains("Nutrient", ignoreCase = true)) }
        val hasSprayTask = scheduledTasks.any { !it.isCompleted && (it.title.contains("Spray", ignoreCase = true) || it.title.contains("Neem", ignoreCase = true) || it.title.contains("Fungicide", ignoreCase = true)) }

        val (action, volume, reasonEn, reasonTa) = when {
            willRainSoon -> {
                Tuple4(
                    IrrigationAction.HOLD_DO_NOT_IRRIGATE,
                    0.0,
                    "Rain forecast (${maxRainProb}% chance within 12h). Avoid irrigation for $crop to prevent waterlogging and root asphyxiation.",
                    "அடுத்த 12 மணி நேரத்தில் மழைக்கு வாய்ப்புள்ளது (${maxRainProb}%). $crop பயிருக்கு வேரழுகல் மற்றும் தண்ணீர் தேங்குவதைத் தவிர்க்க பாசனத்தை ஒத்திவைக்கவும்."
                )
            }
            growthStage == GrowthStage.HARVEST -> {
                Tuple4(
                    IrrigationAction.HOLD_DO_NOT_IRRIGATE,
                    0.0,
                    "Harvest Stage (Day $daysSinceSowing): Stop/minimize irrigation 7-10 days prior to harvest to enhance produce firmness, brix sugars, and shelf life.",
                    "அறுவடைப் பருவம் (நாள் $daysSinceSowing): அறுவடைக்கு முன் பாசனத்தை நிறுத்துவது காய்களின் தரம், இனிப்பு மற்றும் சேமிப்புத் திறனை அதிகரிக்கும்."
                )
            }
            growthStage == GrowthStage.SOWING || growthStage == GrowthStage.SEEDLING -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    2.5,
                    "Seedling Stage (Day $daysSinceSowing): Young tender roots require light, uniform surface moisture. Run gentle micro-sprinklers or drip for 25 mins early morning.",
                    "நாற்றுப் பருவம் (நாள் $daysSinceSowing): இளம் வேர்களுக்கு மிதமான மேலோட்டமான ஈரம் தேவை. காலையில் 25 நிமிடங்கள் மெல்லிய சொட்டு நீர்ப்பாசனம் செய்யவும்."
                )
            }
            growthStage == GrowthStage.FLOWERING -> {
                val fertNoteEn = if (hasFertilizerTask) " (Apply scheduled flowering fertilizer before this drip cycle)." else ""
                val fertNoteTa = if (hasFertilizerTask) " (உர அட்டவணைப்படி உரமிட்ட பின் பாசனம் செய்யவும்)." else ""
                Tuple4(
                    IrrigationAction.NORMAL,
                    4.5,
                    "Critical Flowering Stage (Day $daysSinceSowing): Maintain steady soil moisture without flooding to prevent blossom drop.$fertNoteEn",
                    "முக்கிய பூக்கும் பருவம் (நாள் $daysSinceSowing): பூக்கள் உதிர்வதைத் தவிர்க்க சீரான ஈரம் பராமரிக்கவும்.$fertNoteTa"
                )
            }
            growthStage == GrowthStage.FRUITING -> {
                val sprayNoteEn = if (hasSprayTask) " Irrigate root zone in early morning prior to scheduled evening foliar spray." else ""
                val sprayNoteTa = if (hasSprayTask) " மாலையில் மருந்து தெளிப்பதற்கு முன் காலையிலேயே வேர்ப்பகுதியில் பாசனம் செய்யவும்." else ""
                if (soilMoisture < 40.0 || temp > 34.0) {
                    Tuple4(
                        IrrigationAction.INCREASE,
                        6.5,
                        "Fruiting Stage (Day $daysSinceSowing): Peak water demand during fruit sizing. Hot weather (${temp.toInt()}°C) requires increased drip duration (50 mins) to prevent fruit cracking.$sprayNoteEn",
                        "காய்க்கும் பருவம் (நாள் $daysSinceSowing): காய் பெருக்கும் காலத்தில் அதிக நீர் தேவை. காய் வெடிப்பைத் தடுக்க 50 நிமிடங்கள் தாராளமாக பாசனம் செய்யவும்.$sprayNoteTa"
                    )
                } else {
                    Tuple4(
                        IrrigationAction.NORMAL,
                        5.0,
                        "Fruiting Stage (Day $daysSinceSowing): Regular deep drip irrigation (40 mins) to support heavy fruit/pod load.$sprayNoteEn",
                        "காய்க்கும் பருவம் (நாள் $daysSinceSowing): காய்கள் திரட்சியாக வளர 40 நிமிடங்கள் சீரான சொட்டு நீர் பாசனம் செய்யவும்.$sprayNoteTa"
                    )
                }
            }
            cropLower.contains("rice") || cropLower.contains("paddy") || cropLower.contains("நெல்") -> {
                if (soilMoisture < 60.0) {
                    Tuple4(
                        IrrigationAction.INCREASE,
                        8.0,
                        "Paddy Vegetative/Tillering Stage: Maintain continuous 2-3 cm standing water in the plot for maximum tillers.",
                        "நெல் தூர் கட்டும் பருவம்: அதிக தூர்கள் வெளிவர வயலில் 2-3 செ.மீ சீரான நீர் தேக்கி வைக்கவும்."
                    )
                } else {
                    Tuple4(
                        IrrigationAction.NORMAL,
                        4.0,
                        "Paddy standing water level is optimal. Maintain shallow submergence without spilling over bunds.",
                        "நெல் வயலில் போதுமான நீர் மட்டம் உள்ளது. வரப்புகளுக்கு மேல் வழியாமல் சீராக பராமரிக்கவும்."
                    )
                }
            }
            cropLower.contains("banana") || cropLower.contains("வாழை") -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    16.0,
                    "Banana crop (Day $daysSinceSowing): High canopy transpiration requires 15-20 Liters/plant/day. Irrigate via drip for 1 hour early morning.",
                    "வாழை மரம் (நாள் $daysSinceSowing): நாள் ஒன்றுக்கு 15-20 லிட்டர் தண்ணீர் தேவை. காலை 6:00 - 8:00 மணிக்குள் 1 மணி நேரம் சொட்டு நீர் பாசனம் செய்யவும்."
                )
            }
            cropLower.contains("cotton") || cropLower.contains("பருத்தி") -> {
                if (soilMoisture > 65.0) {
                    Tuple4(
                        IrrigationAction.HOLD_DO_NOT_IRRIGATE,
                        0.0,
                        "Cotton (Day $daysSinceSowing): Soil moisture (${soilMoisture.toInt()}%) is ample. Excess water causes square drop and vegetative runaway.",
                        "பருத்தி (நாள் $daysSinceSowing): மண் ஈரப்பதம் போதுமானது (${soilMoisture.toInt()}%). அதிக தண்ணீர் பாய்ச்சினால் பூ மொட்டுகள் உதிரும்."
                    )
                } else {
                    Tuple4(
                        IrrigationAction.NORMAL,
                        4.5,
                        "Cotton (Day $daysSinceSowing): Provide alternate furrow irrigation in morning. Avoid water stagnating near stems.",
                        "பருத்தி (நாள் $daysSinceSowing): காலையில் மிதமான பார் சால் பாசனம் செய்யவும். தண்டுப் பகுதியில் நீர் தேங்கக்கூடாது."
                    )
                }
            }
            soilMoisture < 35.0 -> {
                Tuple4(
                    IrrigationAction.INCREASE,
                    5.5,
                    "Vegetative Stage (Day $daysSinceSowing): Soil moisture is low (${soilMoisture.toInt()}%). Run drip cycle for 45 mins to replenish root zone.",
                    "தழை வளர்ச்சி பருவம் (நாள் $daysSinceSowing): மண் ஈரப்பதம் குறைந்துள்ளது (${soilMoisture.toInt()}%). 45 நிமிடங்கள் சொட்டு நீர் பாசனம் செய்யவும்."
                )
            }
            else -> {
                Tuple4(
                    IrrigationAction.NORMAL,
                    4.0,
                    "Vegetative Stage (Day $daysSinceSowing): Optimal soil moisture and weather conditions. Continue regular morning drip schedule.",
                    "தழை வளர்ச்சி பருவம் (நாள் $daysSinceSowing): மண் ஈரப்பதம் மற்றும் வானிலை சீராக உள்ளது. வழக்கமான காலை பாசன அட்டவணையைப் பின்பற்றவும்."
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
