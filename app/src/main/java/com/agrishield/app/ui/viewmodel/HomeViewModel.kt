package com.agrishield.app.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.model.AlertItem
import com.agrishield.app.data.model.CropRisk
import com.agrishield.app.data.model.CropTimeline
import com.agrishield.app.data.model.Diagnosis
import com.agrishield.app.data.model.FarmHealth
import com.agrishield.app.data.model.GrowthStage
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
    val currentIrrigation: StateFlow<com.agrishield.app.data.model.IrrigationAdvice?> = farmHealthRepository.currentIrrigation

    val farmCrops: StateFlow<List<CropTimeline>> = timelineRepository.crops
    val activeCrop: StateFlow<CropTimeline> = timelineRepository.timeline
    val selectedCropId: StateFlow<String> = timelineRepository.selectedCropId

    init {
        refreshDashboard()
    }

    fun selectActiveCrop(cropId: String) {
        timelineRepository.selectCrop(cropId)
        val selected = timelineRepository.timeline.value
        refreshDashboardForCrop(selected)
    }

    fun refreshDashboard(customWeatherKey: String? = null) {
        viewModelScope.launch {
            val user = currentUser.value
            val userId = user?.uid ?: "local_user"
            val active = activeCrop.value

            // 1. Fetch live GPS location & weather (use crop's GPS if available, else device GPS)
            val lat = active.latitude ?: 11.0168
            val lon = active.longitude ?: 76.9558
            weatherRepository.fetchWeather(lat, lon, customWeatherKey)

            // 2. Load latest diagnosis from Firestore
            diagnosisRepository.loadRecentDiagnoses(userId)

            // 3. Load soil test
            soilRepository.loadLatestSoil(userId)

            // 4. Update Farm Health Score & Risk engine
            val days = ((System.currentTimeMillis() - active.sowingDateEpoch) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
            farmHealthRepository.updateMetrics(
                recentDiagnosis = latestDiagnosis.value,
                weather = currentWeather.value,
                forecast = weatherRepository.forecast.value,
                soilData = soilRepository.latestSoil.value,
                careTasks = active.tasks,
                primaryCrop = active.cropName,
                growthStage = active.currentStage,
                daysSinceSowing = days
            )
        }
    }

    private fun refreshDashboardForCrop(crop: CropTimeline) {
        viewModelScope.launch {
            val lat = crop.latitude ?: 11.0168
            val lon = crop.longitude ?: 76.9558
            weatherRepository.fetchWeather(lat, lon)

            val days = ((System.currentTimeMillis() - crop.sowingDateEpoch) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(1)
            farmHealthRepository.updateMetrics(
                recentDiagnosis = latestDiagnosis.value,
                weather = currentWeather.value,
                forecast = weatherRepository.forecast.value,
                soilData = soilRepository.latestSoil.value,
                careTasks = crop.tasks,
                primaryCrop = crop.cropName,
                growthStage = crop.currentStage,
                daysSinceSowing = days
            )
        }
    }

    fun addNewCrop(
        cropName: String,
        cropNameTa: String,
        variety: String,
        fieldPlotName: String,
        areaAcres: Double,
        sowingDateEpoch: Long,
        currentStage: GrowthStage,
        locationName: String,
        latitude: Double?,
        longitude: Double?
    ) {
        val user = currentUser.value
        val userId = user?.uid ?: "local_user"
        val newCrop = CropTimeline(
            id = "crop_${System.currentTimeMillis()}",
            cropName = cropName.trim(),
            cropNameTa = cropNameTa.trim().ifBlank { cropName.trim() },
            variety = variety.trim().ifBlank { "Standard Variety" },
            fieldPlotName = fieldPlotName.trim().ifBlank { "Field Plot" },
            areaAcres = if (areaAcres > 0) areaAcres else 1.0,
            sowingDateEpoch = sowingDateEpoch,
            currentStage = currentStage,
            locationName = locationName.trim().ifBlank { "Farm Location" },
            latitude = latitude,
            longitude = longitude
        )

        viewModelScope.launch {
            timelineRepository.addCrop(userId, newCrop)
            refreshDashboardForCrop(newCrop)
        }
    }

    suspend fun getCurrentGpsLocation(): Location? {
        return locationHelper.getCurrentLocation()
    }
}
