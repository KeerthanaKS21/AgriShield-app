package com.agrishield.app.data.model

data class SoilData(
    val id: String = "",
    val nitrogenMgKg: Double = 140.0,
    val phosphorusMgKg: Double = 35.0,
    val potassiumMgKg: Double = 180.0,
    val ph: Double = 6.5,
    val moisturePercent: Double = 45.0,
    val isUserProvided: Boolean = true, // Explicitly tracks user-entered vs sensor data
    val targetCrop: String = "Tomato",
    val healthIndex: Int = 85,
    val recommendationsEn: List<String> = emptyList(),
    val recommendationsTa: List<String> = emptyList(),
    val testedDate: Long = System.currentTimeMillis()
)
