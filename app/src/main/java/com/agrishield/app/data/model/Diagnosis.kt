package com.agrishield.app.data.model

data class Diagnosis(
    val id: String = "",
    val crop: String = "",
    val disease: String = "",
    val confidence: Float = 0.0f,
    val confidenceLevel: ConfidenceLevel = ConfidenceLevel.LOW,
    val severity: String = "Moderate",
    val explanation: String = "",
    val treatmentEn: String = "",
    val treatmentTa: String = "",
    val imageUrl: String = "",
    val localImagePath: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class ConfidenceLevel {
    HIGH,   // >= 80%
    MEDIUM, // 50% - 79%
    LOW     // < 50%
}
