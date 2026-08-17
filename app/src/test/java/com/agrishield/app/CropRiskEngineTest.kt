package com.agrishield.app

import com.agrishield.app.data.ml.CropRiskEngine
import com.agrishield.app.data.model.RiskLevel
import com.agrishield.app.data.model.WeatherData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CropRiskEngineTest {

    private lateinit var riskEngine: CropRiskEngine

    @Before
    fun setUp() {
        riskEngine = CropRiskEngine()
    }

    @Test
    fun testHighFungalRiskConditions() {
        val warmHumidRainy = WeatherData(
            temperatureCelsius = 24.0, // Peak fungal incubation temperature
            humidityPercentage = 92,   // High relative humidity
            rainMmLast3h = 12.0,       // Leaf surface wetness
            windSpeedKmh = 25.0
        )

        val risk = riskEngine.calculateRisk(warmHumidRainy, cropType = "Tomato")

        assertEquals(RiskLevel.HIGH, risk.level)
        assertTrue("Score should be >= 60%, got ${risk.scorePercentage}%", risk.scorePercentage >= 60)
        assertTrue(risk.adviceEn.isNotBlank())
        assertTrue(risk.adviceTa.isNotBlank())
    }

    @Test
    fun testLowRiskDryConditions() {
        val dryCold = WeatherData(
            temperatureCelsius = 38.0, // Unfavorable for foliar fungal blast
            humidityPercentage = 35,   // Dry air
            rainMmLast3h = 0.0,
            windSpeedKmh = 8.0
        )

        val risk = riskEngine.calculateRisk(dryCold, cropType = "Corn")

        assertEquals(RiskLevel.LOW, risk.level)
        assertTrue(risk.scorePercentage < 40)
    }
}
