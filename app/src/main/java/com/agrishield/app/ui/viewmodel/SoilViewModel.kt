package com.agrishield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.model.SoilData
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.data.repository.SoilRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SoilViewModel(
    private val soilRepository: SoilRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val latestSoil: StateFlow<SoilData?> = soilRepository.latestSoil

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun evaluateAndSave(
        n: Double,
        p: Double,
        k: Double,
        ph: Double,
        moisture: Double,
        crop: String
    ) {
        val user = authRepository.currentUser.value
        val userId = user?.uid ?: "local_user"

        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            try {
                val evaluated = soilRepository.evaluateManualInput(n, p, k, ph, moisture, crop)
                val res = soilRepository.saveSoilData(userId, evaluated)
                if (res.isSuccess) {
                    _saveSuccess.value = true
                } else {
                    _errorMessage.value = "Failed to save to cloud records: ${res.exceptionOrNull()?.localizedMessage}"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Soil evaluation failed"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
