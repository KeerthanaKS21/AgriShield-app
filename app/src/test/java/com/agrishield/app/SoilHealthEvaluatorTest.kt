package com.agrishield.app

import com.agrishield.app.data.ml.SoilHealthEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SoilHealthEvaluatorTest {

    private lateinit var evaluator: SoilHealthEvaluator

    @Before
    fun setUp() {
        evaluator = SoilHealthEvaluator()
    }

    @Test
    fun testOptimalSoilParameters() {
        val result = evaluator.evaluateSoil(
            n = 140.0,
            p = 35.0,
            k = 180.0,
            ph = 6.5,
            moisture = 50.0,
            crop = "Tomato"
        )

        assertTrue("Expected score >= 90, got ${result.healthIndex}", result.healthIndex >= 90)
        assertEquals(true, result.isUserProvided)
        assertTrue(result.recommendationsEn.isNotEmpty())
    }

    @Test
    fun testAcidicAndNitrogenDeficientSoil() {
        val result = evaluator.evaluateSoil(
            n = 50.0,  // Severe nitrogen deficiency
            p = 35.0,
            k = 180.0,
            ph = 4.8,  // Highly acidic
            moisture = 45.0,
            crop = "Tomato"
        )

        val hasNitrogenRec = result.recommendationsEn.any { it.contains("Nitrogen is Low", ignoreCase = true) }
        val hasLimeRec = result.recommendationsEn.any { it.contains("Acidic", ignoreCase = true) }

        assertTrue(hasNitrogenRec)
        assertTrue(hasLimeRec)
    }
}
