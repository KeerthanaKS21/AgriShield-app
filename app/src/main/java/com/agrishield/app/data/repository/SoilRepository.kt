package com.agrishield.app.data.repository

import com.agrishield.app.data.firebase.FirestoreManager
import com.agrishield.app.data.ml.SoilHealthEvaluator
import com.agrishield.app.data.model.SoilData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SoilRepository(
    private val evaluator: SoilHealthEvaluator,
    private val firestoreManager: FirestoreManager
) {
    private val _latestSoil = MutableStateFlow<SoilData?>(null)
    val latestSoil: StateFlow<SoilData?> = _latestSoil.asStateFlow()

    fun evaluateManualInput(
        n: Double,
        p: Double,
        k: Double,
        ph: Double,
        moisture: Double,
        crop: String
    ): SoilData {
        val result = evaluator.evaluateSoil(n, p, k, ph, moisture, crop)
        _latestSoil.value = result
        return result
    }

    suspend fun saveSoilData(userId: String, data: SoilData): Result<Unit> = withContext(Dispatchers.IO) {
        val res = firestoreManager.saveSoilTest(userId, data)
        if (res.isSuccess) {
            _latestSoil.value = data
        }
        res
    }

    suspend fun loadLatestSoil(userId: String) = withContext(Dispatchers.IO) {
        val res = firestoreManager.getLatestSoilTest(userId)
        if (res.isSuccess && res.getOrNull() != null) {
            _latestSoil.value = res.getOrNull()
        } else if (_latestSoil.value == null) {
            // Default baseline
            _latestSoil.value = evaluator.evaluateSoil(140.0, 35.0, 180.0, 6.5, 45.0, "Tomato")
        }
    }
}
