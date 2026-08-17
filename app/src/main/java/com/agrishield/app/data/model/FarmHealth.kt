package com.agrishield.app.data.model

data class FarmHealth(
    val score: Int = 85, // 0 to 100
    val rating: HealthRating = HealthRating.GOOD,
    val diseaseComponentScore: Int = 90,
    val weatherRiskComponentScore: Int = 80,
    val soilComponentScore: Int = 85,
    val careRoutineScore: Int = 85,
    val summaryEn: String = "",
    val summaryTa: String = "",
    val calculatedAt: Long = System.currentTimeMillis()
)

enum class HealthRating {
    EXCELLENT, // 85-100
    GOOD,      // 70-84
    MODERATE,  // 50-69
    CRITICAL   // < 50
}
