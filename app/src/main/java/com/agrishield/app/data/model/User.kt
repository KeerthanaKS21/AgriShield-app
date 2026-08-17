package com.agrishield.app.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "Farmer",
    val location: String = "Coimbatore, Tamil Nadu",
    val primaryCrop: String = "Tomato",
    val farmSizeAcres: Double = 2.5,
    val preferredLanguage: String = "ta", // "ta" or "en"
    val createdAt: Long = System.currentTimeMillis()
)
