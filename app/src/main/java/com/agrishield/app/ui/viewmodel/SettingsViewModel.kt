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
    val currentLanguage: StateFlow<String> = com.agrishield.app.utils.AppLanguageManager.currentLanguage

    fun setLanguage(lang: String) {
        com.agrishield.app.utils.AppLanguageManager.setLanguage(lang)
    }

    fun signOut() {
        authRepository.signOut()
    }
}
