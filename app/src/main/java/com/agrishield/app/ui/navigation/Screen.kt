package com.agrishield.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Diagnose : Screen("diagnose")
    object AgriBot : Screen("agribot")
    object WeatherRisk : Screen("weather_risk")
    object SoilHealth : Screen("soil_health")
    object Timeline : Screen("timeline")
    object ModelInfo : Screen("model_info")
    object Profile : Screen("profile")
}
