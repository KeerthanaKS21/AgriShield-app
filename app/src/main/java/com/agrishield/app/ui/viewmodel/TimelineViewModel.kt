package com.agrishield.app.ui.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.CropTimeline
import com.agrishield.app.data.model.GrowthStage
import com.agrishield.app.data.model.TaskCategory
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.data.repository.TimelineRepository
import com.agrishield.app.utils.LocationHelper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimelineViewModel(
    private val timelineRepository: TimelineRepository,
    private val authRepository: AuthRepository,
    private val locationHelper: LocationHelper? = null
) : ViewModel() {

    val crops: StateFlow<List<CropTimeline>> = timelineRepository.crops
    val selectedCropId: StateFlow<String> = timelineRepository.selectedCropId
    val timeline: StateFlow<CropTimeline> = timelineRepository.timeline

    fun selectCrop(cropId: String) {
        timelineRepository.selectCrop(cropId)
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
        val user = authRepository.currentUser.value
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
        }
    }

    fun deleteCrop(cropId: String) {
        val user = authRepository.currentUser.value
        val userId = user?.uid ?: "local_user"
        viewModelScope.launch {
            timelineRepository.deleteCrop(userId, cropId)
        }
    }

    suspend fun getCurrentGpsLocation(): Location? {
        return locationHelper?.getCurrentLocation()
    }

    suspend fun getCurrentGpsLocationWithPlace(): Pair<Location, String>? {
        return locationHelper?.getCurrentGpsLocationWithPlace()
    }

    fun toggleTask(taskId: String) {
        val user = authRepository.currentUser.value
        val userId = user?.uid ?: "local_user"
        viewModelScope.launch {
            timelineRepository.toggleTaskCompletion(userId, taskId)
        }
    }

    fun addNewTask(title: String, titleTa: String, category: TaskCategory, dueDaysFromNow: Int) {
        val user = authRepository.currentUser.value
        val userId = user?.uid ?: "local_user"
        val newTask = CareTask(
            id = "task_${System.currentTimeMillis()}",
            title = title,
            titleTa = titleTa,
            category = category,
            dueDateEpoch = System.currentTimeMillis() + (dueDaysFromNow * 24L * 60 * 60 * 1000),
            isCompleted = false
        )
        viewModelScope.launch {
            timelineRepository.addTask(userId, newTask)
        }
    }

    fun updateGrowthStage(stage: GrowthStage) {
        viewModelScope.launch {
            timelineRepository.updateCropStage(stage)
        }
    }
}
