package com.agrishield.app.data.model

data class CropRisk(
    val level: RiskLevel = RiskLevel.LOW,
    val scorePercentage: Int = 15,
    val primaryRiskDisease: String = "None Detected",
    val riskFactors: List<String> = emptyList(),
    val adviceEn: String = "",
    val adviceTa: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}
