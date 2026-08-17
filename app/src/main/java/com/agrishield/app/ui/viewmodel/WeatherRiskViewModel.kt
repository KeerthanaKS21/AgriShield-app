package com.agrishield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.ml.CropRiskEngine
import com.agrishield.app.data.ml.IrrigationAdvisor
import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.IrrigationAdvice
import com.agrishield.app.data.model.WeatherData
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.data.repository.DiagnosisRepository
import com.agrishield.app.data.repository.SoilRepository
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
    private val riskEngine: CropRiskEngine = CropRiskEngine(),
    private val irrigationAdvisor: IrrigationAdvisor = IrrigationAdvisor()
) : ViewModel() {

    val currentWeather: StateFlow<WeatherData?> = weatherRepository.currentWeather
    val forecast: StateFlow<List<ForecastItem>> = weatherRepository.forecast
    val isLoading: StateFlow<Boolean> = weatherRepository.isLoading
    val error: StateFlow<String?> = weatherRepository.error

    private val _cropRisk = MutableStateFlow<CropRisk?>(null)
    val cropRisk: StateFlow<CropRisk?> = _cropRisk.asStateFlow()

    private val _irrigationAdvice = MutableStateFlow<IrrigationAdvice?>(null)
    val irrigationAdvice: StateFlow<IrrigationAdvice?> = _irrigationAdvice.asStateFlow()

    init {
        loadWeatherAndRisk()
    }

    fun loadWeatherAndRisk(customApiKey: String? = null) {
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation()
            val lat = location?.latitude ?: 11.0168 // Default: Coimbatore, TN
            val lon = location?.longitude ?: 76.9558

            val result = weatherRepository.fetchWeather(lat, lon, customApiKey)
            if (result.isSuccess) {
                val weather = result.getOrNull()!!
                val crop = authRepository.currentUser.value?.primaryCrop ?: "Tomato"
                val diagnosis = diagnosisRepository.latestDiagnosis.value

                // Evaluate real risk
                val risk = riskEngine.calculateRisk(weather, crop, diagnosis?.disease)
                _cropRisk.value = risk

                // Evaluate dynamic irrigation
                val soilMoisture = soilRepository.latestSoil.value?.moisturePercent ?: 45.0
                val advice = irrigationAdvisor.getAdvice(weather, forecast.value, soilMoisture, crop)
                _irrigationAdvice.value = advice
            }
        }
    }
}
