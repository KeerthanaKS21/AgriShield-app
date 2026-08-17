package com.agrishield.app.data.model

data class AlertItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val titleTa: String = "",
    val message: String = "",
    val messageTa: String = "",
    val type: AlertType = AlertType.DISEASE_RISK,
    val severity: AlertSeverity = AlertSeverity.WARNING,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionDeepLink: String = ""
)

enum class AlertType {
    DISEASE_RISK,
    SEVERE_WEATHER,
    CARE_REMINDER,
    LOW_CONFIDENCE_SCAN,
    SOIL_DEFICIENCY
}

enum class AlertSeverity {
    INFO,
    WARNING,
    CRITICAL
}
