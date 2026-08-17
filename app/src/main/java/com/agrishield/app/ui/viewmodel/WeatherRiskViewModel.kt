package com.agrishield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.ml.CropRiskEngine
import com.agrishield.app.data.ml.IrrigationAdvisor
import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.CropTimeline
import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.IrrigationAdvice
import com.agrishield.app.data.model.WeatherData
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.data.repository.DiagnosisRepository
import com.agrishield.app.data.repository.SoilRepository
import com.agrishield.app.data.repository.TimelineRepository
import com.agrishield.app.data.repository.WeatherRepository
import com.agrishield.app.utils.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherRiskViewModel(
    private val weatherRepository: WeatherRepository,
    private val diagnosisRepository: DiagnosisRepository,
    private val soilRepository: SoilRepository,
    private val authRepository: AuthRepository,
    private val locationHelper: LocationHelper,
    private val timelineRepository: TimelineRepository,
    private val riskEngine: CropRiskEngine = CropRiskEngine(),
    private val irrigationAdvisor: IrrigationAdvisor = IrrigationAdvisor()
) : ViewModel() {

    val currentWeather: StateFlow<WeatherData?> = weatherRepository.currentWeather
    val forecast: StateFlow<List<ForecastItem>> = weatherRepository.forecast
    val isLoading: StateFlow<Boolean> = weatherRepository.isLoading
    val error: StateFlow<String?> = weatherRepository.error

    // Strictly the crops the farmer actually has in their farm
    val farmCrops: StateFlow<List<CropTimeline>> = timelineRepository.crops
    val selectedCrop: StateFlow<CropTimeline> = timelineRepository.timeline
    val selectedCropId: StateFlow<String> = timelineRepository.selectedCropId

    private val _cropRisk = MutableStateFlow<CropRisk?>(null)
    val cropRisk: StateFlow<CropRisk?> = _cropRisk.asStateFlow()

    private val _irrigationAdvice = MutableStateFlow<IrrigationAdvice?>(null)
    val irrigationAdvice: StateFlow<IrrigationAdvice?> = _irrigationAdvice.asStateFlow()

    init {
        viewModelScope.launch {
            timelineRepository.timeline.collect { activeCrop ->
                loadWeatherAndEvaluateForCrop(activeCrop)
            }
        }
    }

    fun selectCrop(cropId: String) {
        timelineRepository.selectCrop(cropId)
        val crop = farmCrops.value.find { it.id == cropId } ?: selectedCrop.value
        loadWeatherAndEvaluateForCrop(crop)
    }

    fun loadWeatherAndRisk(customApiKey: String? = null) {
        loadWeatherAndEvaluateForCrop(selectedCrop.value, customApiKey)
    }

    private fun loadWeatherAndEvaluateForCrop(crop: CropTimeline, customApiKey: String? = null) {
        viewModelScope.launch {
            val lat = crop.latitude ?: locationHelper.getCurrentLocation()?.latitude ?: 11.0168
            val lon = crop.longitude ?: locationHelper.getCurrentLocation()?.longitude ?: 76.9558

            val result = weatherRepository.fetchWeather(lat, lon, customApiKey)
            val weather = result.getOrNull() ?: currentWeather.value

            if (weather != null) {
                val diagnosis = diagnosisRepository.latestDiagnosis.value
                val soilMoisture = soilRepository.latestSoil.value?.moisturePercent ?: 48.0
                val daysSinceSowing = ((System.currentTimeMillis() - crop.sowingDateEpoch) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(1)

                // 1. Stage & Timeline-Aware Disease Risk Analysis
                _cropRisk.value = riskEngine.calculateRisk(
                    weather = weather,
                    cropType = crop.cropName,
                    growthStage = crop.currentStage,
                    daysSinceSowing = daysSinceSowing,
                    recentDiseaseDiagnosis = diagnosis?.disease
                )

                // 2. Stage & Timeline-Aware Smart Irrigation Advice
                _irrigationAdvice.value = irrigationAdvisor.getAdvice(
                    weather = weather,
                    forecast = forecast.value,
                    soilMoisture = soilMoisture,
                    crop = crop.cropName,
                    growthStage = crop.currentStage,
                    daysSinceSowing = daysSinceSowing,
                    scheduledTasks = crop.tasks
                )
            }
        }
    }
}
