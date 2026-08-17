package com.agrishield.app

import com.agrishield.app.data.ml.FarmHealthCalculator
import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.ConfidenceLevel
import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.data.model.HealthRating
import com.agrishield.app.data.model.RiskLevel
import com.agrishield.app.data.model.SoilData
import com.agrishield.app.data.model.TaskCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FarmHealthCalculatorTest {

    private lateinit var calculator: FarmHealthCalculator

    @Before
    fun setUp() {
        calculator = FarmHealthCalculator()
    }

    @Test
    fun testHealthyCropHighHealthScore() {
        val healthyDiagnosis = Diagnosis(
            crop = "Tomato",
            disease = "Healthy",
            confidence = 96.0f,
            confidenceLevel = ConfidenceLevel.HIGH
        )
        val lowRisk = CropRisk(level = RiskLevel.LOW, scorePercentage = 10)
        val goodSoil = SoilData(healthIndex = 90)
        val tasks = listOf(
            CareTask(id = "1", title = "Task 1", isCompleted = true, category = TaskCategory.FERTILIZATION),
            CareTask(id = "2", title = "Task 2", isCompleted = true, category = TaskCategory.IRRIGATION)
        )

        val result = calculator.calculateScore(healthyDiagnosis, lowRisk, goodSoil, tasks)

        assertTrue("Expected score >= 85, got ${result.score}", result.score >= 85)
        assertEquals(HealthRating.EXCELLENT, result.rating)
    }

    @Test
    fun testSevereDiseaseLowHealthScore() {
        val severeDiagnosis = Diagnosis(
            crop = "Potato",
            disease = "Late Blight",
            confidence = 94.0f,
            confidenceLevel = ConfidenceLevel.HIGH
        )
        val highRisk = CropRisk(level = RiskLevel.HIGH, scorePercentage = 85)
        val soil = SoilData(healthIndex = 50)
        val tasks = emptyList<CareTask>()

        val result = calculator.calculateScore(severeDiagnosis, highRisk, soil, tasks)

        assertTrue("Expected score < 50, got ${result.score}", result.score < 50)
        assertEquals(HealthRating.CRITICAL, result.rating)
    }

    @Test
    fun testScoreAlwaysWithin0To100() {
        val diag = Diagnosis(crop = "Corn", disease = "Common Rust", confidence = 99f, confidenceLevel = ConfidenceLevel.HIGH)
        val risk = CropRisk(level = RiskLevel.HIGH, scorePercentage = 95)
        val soil = SoilData(healthIndex = 0)
        val tasks = emptyList<CareTask>()

        val result = calculator.calculateScore(diag, risk, soil, tasks)
        assertTrue(result.score in 0..100)
    }
}
