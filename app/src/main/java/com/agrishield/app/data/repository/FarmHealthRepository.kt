package com.agrishield.app.data.repository

import com.agrishield.app.data.ml.CropRiskEngine
import com.agrishield.app.data.ml.FarmHealthCalculator
import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.data.model.FarmHealth
import com.agrishield.app.data.model.SoilData
import com.agrishield.app.data.model.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FarmHealthRepository(
    private val calculator: FarmHealthCalculator,
    private val riskEngine: CropRiskEngine
) {
    private val _farmHealth = MutableStateFlow(FarmHealth())
    val farmHealth: StateFlow<FarmHealth> = _farmHealth.asStateFlow()

    private val _currentRisk = MutableStateFlow(CropRisk())
    val currentRisk: StateFlow<CropRisk> = _currentRisk.asStateFlow()

    fun updateMetrics(
        recentDiagnosis: Diagnosis?,
        weather: WeatherData?,
        soilData: SoilData?,
        careTasks: List<CareTask>,
        primaryCrop: String = "Tomato"
    ) {
        val calculatedRisk = if (weather != null) {
            riskEngine.calculateRisk(weather, primaryCrop, recentDiagnosis?.disease)
        } else {
            CropRisk()
        }
        _currentRisk.value = calculatedRisk

        val calculatedHealth = calculator.calculateScore(
            recentDiagnosis = recentDiagnosis,
            currentRisk = calculatedRisk,
            soilData = soilData,
            careTasks = careTasks
        )
        _farmHealth.value = calculatedHealth
    }
}
