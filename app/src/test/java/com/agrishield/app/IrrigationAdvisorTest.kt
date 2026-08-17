package com.agrishield.app

import com.agrishield.app.data.ml.IrrigationAdvisor
import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.IrrigationAction
import com.agrishield.app.data.model.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IrrigationAdvisorTest {

    private lateinit var advisor: IrrigationAdvisor

    @Before
    fun setUp() {
        advisor = IrrigationAdvisor()
    }

    @Test
    fun testRainForecastInhibitsIrrigation() {
        val weather = WeatherData(temperatureCelsius = 26.0, humidityPercentage = 75, rainMmLast3h = 0.0)
        val rainForecast = listOf(
            ForecastItem(dateTimeEpoch = System.currentTimeMillis() + 3600000, rainProbabilityPercent = 85, tempCelsius = 25.0)
        )

        val advice = advisor.getAdvice(weather, rainForecast, soilMoisture = 50.0, crop = "Tomato")

        assertEquals(IrrigationAction.HOLD_DO_NOT_IRRIGATE, advice.action)
        assertEquals(0.0, advice.waterVolumeLitersPerSqm, 0.01)
        assertTrue(advice.rainExpectedNext12h)
        assertTrue(advice.reasonEn.contains("Rain forecast", ignoreCase = true))
    }

    @Test
    fun testHeatwaveIncreasesIrrigation() {
        val hotWeather = WeatherData(temperatureCelsius = 39.0, humidityPercentage = 30, rainMmLast3h = 0.0)
        val dryForecast = listOf(
            ForecastItem(dateTimeEpoch = System.currentTimeMillis() + 3600000, rainProbabilityPercent = 5, tempCelsius = 38.0)
        )

        val advice = advisor.getAdvice(hotWeather, dryForecast, soilMoisture = 40.0, crop = "Tomato")

        assertEquals(IrrigationAction.INCREASE, advice.action)
        assertTrue(advice.waterVolumeLitersPerSqm > 5.0)
    }
}
