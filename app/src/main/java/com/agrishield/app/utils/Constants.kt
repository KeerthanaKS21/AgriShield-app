package com.agrishield.app.utils

object Constants {
    const val PREFS_NAME = "agrishield_prefs"
    const val KEY_LANGUAGE = "pref_language"
    const val KEY_CUSTOM_GEMINI_KEY = "pref_custom_gemini_key"
    const val KEY_CUSTOM_WEATHER_KEY = "pref_custom_weather_key"

    // Model & Inference
    const val MODEL_FILE = "model.tflite"
    const val LABELS_FILE = "labels.txt"
    const val MODEL_INPUT_SIZE = 224
    const val CONFIDENCE_HIGH_THRESHOLD = 0.80f
    const val CONFIDENCE_MEDIUM_THRESHOLD = 0.50f

    // API URLs
    const val OPENWEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"

    // Notification Channel
    const val CHANNEL_ID_ALERTS = "agrishield_alerts_channel"
    const val CHANNEL_NAME_ALERTS = "AgriShield Agricultural Alerts"
    const val NOTIFICATION_ID_RISK = 1001
    const val NOTIFICATION_ID_WEATHER = 1002
    const val NOTIFICATION_ID_CARE = 1003

    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_DIAGNOSES = "diagnoses"
    const val COLLECTION_SOIL = "soilTests"
    const val COLLECTION_TIMELINE = "careTasks"
    const val COLLECTION_ALERTS = "alerts"
}
