package com.agrishield.app.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.agrishield.app.data.firebase.FirestoreManager
import com.agrishield.app.data.firebase.FirebaseStorageManager
import com.agrishield.app.data.ml.CropDiseaseClassifier
import com.agrishield.app.data.model.Diagnosis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class DiagnosisRepository(
    private val classifier: CropDiseaseClassifier,
    private val firestoreManager: FirestoreManager,
    private val storageManager: FirebaseStorageManager
) {
    private val _recentDiagnoses = MutableStateFlow<List<Diagnosis>>(emptyList())
    val recentDiagnoses: StateFlow<List<Diagnosis>> = _recentDiagnoses.asStateFlow()

    private val _latestDiagnosis = MutableStateFlow<Diagnosis?>(null)
    val latestDiagnosis: StateFlow<Diagnosis?> = _latestDiagnosis.asStateFlow()

    suspend fun diagnoseLeaf(bitmap: Bitmap, imageUri: Uri?, localPath: String): Diagnosis = withContext(Dispatchers.Default) {
        val result = classifier.classifyImage(bitmap, imageUri?.toString() ?: localPath)
        val finalDiagnosis = result.copy(localImagePath = localPath)
        _latestDiagnosis.value = finalDiagnosis
        finalDiagnosis
    }

    suspend fun saveDiagnosisToCloud(userId: String, diagnosis: Diagnosis, imageUri: Uri?): Result<Diagnosis> = withContext(Dispatchers.IO) {
        var updated = diagnosis
        if (imageUri != null) {
            val uploadResult = storageManager.uploadDiagnosisImage(userId, imageUri)
            if (uploadResult.isSuccess) {
                updated = updated.copy(imageUrl = uploadResult.getOrNull() ?: "")
            }
        }
        val saveResult = firestoreManager.saveDiagnosis(userId, updated)
        if (saveResult.isSuccess) {
            _latestDiagnosis.value = updated
            loadRecentDiagnoses(userId)
            Result.success(updated)
        } else {
            Result.failure(saveResult.exceptionOrNull() ?: Exception("Failed to save diagnosis"))
        }
    }

    suspend fun loadRecentDiagnoses(userId: String) = withContext(Dispatchers.IO) {
        val result = firestoreManager.getRecentDiagnoses(userId, limit = 10)
        if (result.isSuccess) {
            val list = result.getOrNull() ?: emptyList()
            _recentDiagnoses.value = list
            if (list.isNotEmpty() && _latestDiagnosis.value == null) {
                _latestDiagnosis.value = list.first()
            }
        }
    }

    fun getModelSupportedClasses(): List<String> = classifier.getSupportedClasses()
}
