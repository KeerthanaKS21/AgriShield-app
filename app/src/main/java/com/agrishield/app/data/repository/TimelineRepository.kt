package com.agrishield.app.data.repository

import com.agrishield.app.data.firebase.FirestoreManager
import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.CropTimeline
import com.agrishield.app.data.model.CropTimelineTemplates
import com.agrishield.app.data.model.GrowthStage
import com.agrishield.app.data.model.TaskCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class TimelineRepository(
    private val firestoreManager: FirestoreManager
) {
    private val defaultInitialCrop = CropTimeline(
        id = "crop_tomato_1",
        cropName = "Tomato",
        cropNameTa = "தக்காளி",
        variety = "PKM-1 Hybrid",
        fieldPlotName = "North Field (Plot A)",
        areaAcres = 2.0,
        sowingDateEpoch = System.currentTimeMillis() - (25L * 24 * 60 * 60 * 1000),
        currentStage = GrowthStage.VEGETATIVE,
        locationName = "Coimbatore South",
        latitude = 11.0168,
        longitude = 76.9558,
        tasks = CropTimelineTemplates.generateDefaultTasksForCrop("Tomato", System.currentTimeMillis() - (25L * 24 * 60 * 60 * 1000))
    )

    private val defaultPaddyCrop = CropTimeline(
        id = "crop_paddy_2",
        cropName = "Rice / Paddy",
        cropNameTa = "நெல் (சம்பா)",
        variety = "CO-51 High Yield",
        fieldPlotName = "Wetland Plot 2",
        areaAcres = 3.5,
        sowingDateEpoch = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000),
        currentStage = GrowthStage.SEEDLING,
        locationName = "Thanjavur Delta",
        latitude = 10.7870,
        longitude = 79.1378,
        tasks = CropTimelineTemplates.generateDefaultTasksForCrop("Rice", System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000))
    )

    private val _crops = MutableStateFlow<List<CropTimeline>>(listOf(defaultInitialCrop, defaultPaddyCrop))
    val crops: StateFlow<List<CropTimeline>> = _crops.asStateFlow()

    private val _selectedCropId = MutableStateFlow(defaultInitialCrop.id)
    val selectedCropId: StateFlow<String> = _selectedCropId.asStateFlow()

    private val _timeline = MutableStateFlow(defaultInitialCrop)
    val timeline: StateFlow<CropTimeline> = _timeline.asStateFlow()

    fun selectCrop(cropId: String) {
        _selectedCropId.value = cropId
        val found = _crops.value.firstOrNull { it.id == cropId } ?: _crops.value.firstOrNull()
        if (found != null) {
            _timeline.value = found
        }
    }

    suspend fun addCrop(userId: String, newCrop: CropTimeline): Result<CropTimeline> = withContext(Dispatchers.IO) {
        val tasks = if (newCrop.tasks.isEmpty()) {
            CropTimelineTemplates.generateDefaultTasksForCrop(newCrop.cropName, newCrop.sowingDateEpoch)
        } else {
            newCrop.tasks
        }
        val completeCrop = newCrop.copy(tasks = tasks)
        val updatedList = _crops.value + completeCrop
        _crops.value = updatedList
        selectCrop(completeCrop.id)
        Result.success(completeCrop)
    }

    suspend fun deleteCrop(userId: String, cropId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val updatedList = _crops.value.filterNot { it.id == cropId }
        if (updatedList.isNotEmpty()) {
            _crops.value = updatedList
            selectCrop(updatedList.first().id)
        }
        Result.success(Unit)
    }

    suspend fun toggleTaskCompletion(userId: String, taskId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentCrop = _timeline.value
        val updatedTasks = currentCrop.tasks.map { task ->
            if (task.id == taskId) {
                val newStatus = !task.isCompleted
                task.copy(
                    isCompleted = newStatus,
                    completedAt = if (newStatus) System.currentTimeMillis() else null
                )
            } else task
        }
        val updatedCrop = currentCrop.copy(tasks = updatedTasks)
        _timeline.value = updatedCrop

        _crops.value = _crops.value.map { if (it.id == updatedCrop.id) updatedCrop else it }

        val updatedTask = updatedTasks.firstOrNull { it.id == taskId }
        if (updatedTask != null) {
            firestoreManager.saveCareTask(userId, updatedTask)
        }
        Result.success(Unit)
    }

    suspend fun addTask(userId: String, task: CareTask): Result<Unit> = withContext(Dispatchers.IO) {
        val currentCrop = _timeline.value
        val updatedTasks = currentCrop.tasks + task
        val updatedCrop = currentCrop.copy(tasks = updatedTasks)
        _timeline.value = updatedCrop
        _crops.value = _crops.value.map { if (it.id == updatedCrop.id) updatedCrop else it }

        firestoreManager.saveCareTask(userId, task)
    }

    suspend fun updateCropStage(newStage: GrowthStage) {
        val currentCrop = _timeline.value
        val updatedCrop = currentCrop.copy(currentStage = newStage)
        _timeline.value = updatedCrop
        _crops.value = _crops.value.map { if (it.id == updatedCrop.id) updatedCrop else it }
    }
}
