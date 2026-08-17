package com.agrishield.app.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.model.ConfidenceLevel
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.data.repository.DiagnosisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiagnoseViewModel(
    private val diagnosisRepository: DiagnosisRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _currentDiagnosis = MutableStateFlow<Diagnosis?>(null)
    val currentDiagnosis: StateFlow<Diagnosis?> = _currentDiagnosis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun setImage(bitmap: Bitmap?, uri: Uri?) {
        _selectedBitmap.value = bitmap
        _selectedImageUri.value = uri
        _currentDiagnosis.value = null
        _saveSuccess.value = false
        _errorMessage.value = null
    }

    fun analyzeImage() {
        val bitmap = _selectedBitmap.value
        if (bitmap == null) {
            _errorMessage.value = "Please select or capture a crop leaf image first."
            return
        }

        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorMessage.value = null
            try {
                val diagnosis = diagnosisRepository.diagnoseLeaf(
                    bitmap = bitmap,
                    imageUri = _selectedImageUri.value,
                    localPath = ""
                )
                _currentDiagnosis.value = diagnosis
            } catch (e: Exception) {
                _errorMessage.value = "Diagnosis inference failed: ${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun saveDiagnosis() {
        val diagnosis = _currentDiagnosis.value ?: return
        val user = authRepository.currentUser.value
        val userId = user?.uid ?: "local_user"

        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            val result = diagnosisRepository.saveDiagnosisToCloud(
                userId = userId,
                diagnosis = diagnosis,
                imageUri = _selectedImageUri.value
            )
            _isSaving.value = false
            if (result.isSuccess) {
                _saveSuccess.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.localizedMessage ?: "Failed to save to farm records"
            }
        }
    }

    fun getSupportedClasses(): List<String> = diagnosisRepository.getModelSupportedClasses()

    fun reset() {
        _selectedBitmap.value = null
        _selectedImageUri.value = null
        _currentDiagnosis.value = null
        _saveSuccess.value = false
        _errorMessage.value = null
    }
}
