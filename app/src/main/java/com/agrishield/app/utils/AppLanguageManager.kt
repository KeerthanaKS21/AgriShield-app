package com.agrishield.app.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppLanguageManager {

    private const val PREF_KEY_LANG = "app_selected_language"
    private var prefs: SharedPreferences? = null

    private val _currentLanguage = MutableStateFlow("ta") // Default to Tamil (or persisted)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = prefs?.getString(PREF_KEY_LANG, prefs?.getString(Constants.KEY_LANGUAGE, "ta")) ?: "ta"
        _currentLanguage.value = savedLang
    }

    fun setLanguage(langCode: String) {
        val code = if (langCode.startsWith("en", ignoreCase = true)) "en" else "ta"
        _currentLanguage.value = code
        prefs?.edit()?.putString(PREF_KEY_LANG, code)?.putString(Constants.KEY_LANGUAGE, code)?.apply()
    }

    fun toggleLanguage(): String {
        val next = if (_currentLanguage.value == "ta") "en" else "ta"
        setLanguage(next)
        return next
    }

    fun isTamil(): Boolean = _currentLanguage.value == "ta"
}
