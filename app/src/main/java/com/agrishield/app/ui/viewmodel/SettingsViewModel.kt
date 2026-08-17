package com.agrishield.app.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.agrishield.app.data.model.User
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val context: Context,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _currentLanguage = MutableStateFlow(prefs.getString(Constants.KEY_LANGUAGE, "en") ?: "en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _customGeminiKey = MutableStateFlow(prefs.getString(Constants.KEY_CUSTOM_GEMINI_KEY, "") ?: "")
    val customGeminiKey: StateFlow<String> = _customGeminiKey.asStateFlow()

    private val _customWeatherKey = MutableStateFlow(prefs.getString(Constants.KEY_CUSTOM_WEATHER_KEY, "") ?: "")
    val customWeatherKey: StateFlow<String> = _customWeatherKey.asStateFlow()

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
        prefs.edit().putString(Constants.KEY_LANGUAGE, lang).apply()
    }

    fun saveApiKeys(geminiKey: String, weatherKey: String) {
        _customGeminiKey.value = geminiKey.trim()
        _customWeatherKey.value = weatherKey.trim()
        prefs.edit()
            .putString(Constants.KEY_CUSTOM_GEMINI_KEY, geminiKey.trim())
            .putString(Constants.KEY_CUSTOM_WEATHER_KEY, weatherKey.trim())
            .apply()
    }

    fun signOut() {
        authRepository.signOut()
    }
}
