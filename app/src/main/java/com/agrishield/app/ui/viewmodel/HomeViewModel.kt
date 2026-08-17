package com.agrishield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.model.AlertItem
import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.data.model.FarmHealth
import com.agrishield.app.data.model.User
import com.agrishield.app.data.model.WeatherData
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.data.repository.DiagnosisRepository
import com.agrishield.app.data.repository.FarmHealthRepository
import com.agrishield.app.data.repository.SoilRepository
import com.agrishield.app.data.repository.TimelineRepository
import com.agrishield.app.data.repository.WeatherRepository
import com.agrishield.app.utils.LocationHelper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val diagnosisRepository: DiagnosisRepository,
    private val weatherRepository: WeatherRepository,
    private val farmHealthRepository: FarmHealthRepository,
    private val soilRepository: SoilRepository,
    private val timelineRepository: TimelineRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser
    val currentWeather: StateFlow<WeatherData?> = weatherRepository.currentWeather
    val latestDiagnosis: StateFlow<Diagnosis?> = diagnosisRepository.latestDiagnosis
    val farmHealth: StateFlow<FarmHealth> = farmHealthRepository.farmHealth
    val currentRisk: StateFlow<CropRisk> = farmHealthRepository.currentRisk

    init {
        refreshDashboard()
    }

    fun refreshDashboard(customWeatherKey: String? = null) {
        viewModelScope.launch {
            val user = currentUser.value
            val userId = user?.uid ?: "local_user"

            // 1. Fetch live GPS location & weather
            val location = locationHelper.getCurrentLocation()
            val lat = location?.latitude ?: 11.0168 // Default: Coimbatore, Tamil Nadu
            val lon = location?.longitude ?: 76.9558
            weatherRepository.fetchWeather(lat, lon, customWeatherKey)

            // 2. Load latest diagnosis from Firestore
            diagnosisRepository.loadRecentDiagnoses(userId)

            // 3. Load soil test
            soilRepository.loadLatestSoil(userId)

            // 4. Update Farm Health Score & Risk engine
            farmHealthRepository.updateMetrics(
                recentDiagnosis = latestDiagnosis.value,
                weather = currentWeather.value,
                soilData = soilRepository.latestSoil.value,
                careTasks = timelineRepository.timeline.value.tasks,
                primaryCrop = user?.primaryCrop ?: "Tomato"
            )
        }
    }
}
