package com.agrishield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.model.User
import com.agrishield.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please enter email and password"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.signIn(email, pass)
            _isLoading.value = false
            if (result.isSuccess) {
                _authSuccess.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Authentication failed"
            }
        }
    }

    fun signUp(email: String, pass: String, name: String, location: String, crop: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _errorMessage.value = "Please fill in all required fields"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = authRepository.signUp(email, pass, name, location, crop)
            _isLoading.value = false
            if (result.isSuccess) {
                _authSuccess.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Registration failed"
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
