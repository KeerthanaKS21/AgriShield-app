package com.agrishield.app.data.repository

import com.agrishield.app.data.ml.CropRiskEngine
import com.agrishield.app.data.ml.FarmHealthCalculator
import com.agrishield.app.data.ml.IrrigationAdvisor
import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.data.model.FarmHealth
import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.IrrigationAdvice
import com.agrishield.app.data.model.SoilData
import com.agrishield.app.data.model.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FarmHealthRepository(
    private val calculator: FarmHealthCalculator,
    private val riskEngine: CropRiskEngine,
    private val irrigationAdvisor: IrrigationAdvisor
) {
    private val _farmHealth = MutableStateFlow(FarmHealth())
    val farmHealth: StateFlow<FarmHealth> = _farmHealth.asStateFlow()

    private val _currentRisk = MutableStateFlow(CropRisk())
    val currentRisk: StateFlow<CropRisk> = _currentRisk.asStateFlow()

    private val _currentIrrigation = MutableStateFlow<IrrigationAdvice?>(null)
    val currentIrrigation: StateFlow<IrrigationAdvice?> = _currentIrrigation.asStateFlow()

    fun updateMetrics(
        recentDiagnosis: Diagnosis?,
        weather: WeatherData?,
        forecast: List<ForecastItem> = emptyList(),
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

        if (weather != null) {
            _currentIrrigation.value = irrigationAdvisor.getAdvice(
                weather = weather,
                forecast = forecast,
                soilMoisture = soilData?.moisturePercent ?: 48.0,
                crop = primaryCrop
            )
        }
    }
}
