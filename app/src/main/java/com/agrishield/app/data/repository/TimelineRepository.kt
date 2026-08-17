package com.agrishield.app.data.repository

import com.agrishield.app.data.firebase.FirestoreManager
import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.CropTimeline
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
    private val _timeline = MutableStateFlow(
        CropTimeline(
            id = "default_crop",
            cropName = "Tomato",
            variety = "PKM-1 Hybrid",
            currentStage = GrowthStage.VEGETATIVE,
            tasks = getDefaultTasks()
        )
    )
    val timeline: StateFlow<CropTimeline> = _timeline.asStateFlow()

    suspend fun toggleTaskCompletion(userId: String, taskId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentTasks = _timeline.value.tasks.map { task ->
            if (task.id == taskId) {
                val newStatus = !task.isCompleted
                task.copy(
                    isCompleted = newStatus,
                    completedAt = if (newStatus) System.currentTimeMillis() else null
                )
            } else task
        }
        _timeline.value = _timeline.value.copy(tasks = currentTasks)
        val updatedTask = currentTasks.firstOrNull { it.id == taskId }
        if (updatedTask != null) {
            firestoreManager.saveCareTask(userId, updatedTask)
        }
        Result.success(Unit)
    }

    suspend fun addTask(userId: String, task: CareTask): Result<Unit> = withContext(Dispatchers.IO) {
        val updatedList = _timeline.value.tasks + task
        _timeline.value = _timeline.value.copy(tasks = updatedList)
        firestoreManager.saveCareTask(userId, task)
    }

    suspend fun updateCropStage(newStage: GrowthStage) {
        _timeline.value = _timeline.value.copy(currentStage = newStage)
    }

    private fun getDefaultTasks(): List<CareTask> {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60 * 60 * 1000
        return listOf(
            CareTask(
                id = "task_1",
                title = "Apply Basal Fertilizer (DAP + FYM)",
                titleTa = "அடி உரம் இடுதல் (டி.ஏ.பி + தொழுவுரம்)",
                category = TaskCategory.FERTILIZATION,
                dueDateEpoch = now - (20 * dayMs),
                isCompleted = true,
                completedAt = now - (20 * dayMs)
            ),
            CareTask(
                id = "task_2",
                title = "First Weeding & Earthing Up",
                titleTa = "முதல் களை எடுத்தல் மற்றும் மண் அணைத்தல்",
                category = TaskCategory.WEEDING,
                dueDateEpoch = now - (5 * dayMs),
                isCompleted = true,
                completedAt = now - (5 * dayMs)
            ),
            CareTask(
                id = "task_3",
                title = "Preventive Spray: Neem Oil 3ml/L",
                titleTa = "முன்னெச்சரிக்கை தெளிப்பு: வேப்பெண்ணெய் 3 மி.லி/லி",
                category = TaskCategory.PEST_INSPECTION,
                dueDateEpoch = now + (2 * dayMs),
                isCompleted = false
            ),
            CareTask(
                id = "task_4",
                title = "Flowering Top-Dressing: MOP (15 kg/acre)",
                titleTa = "பூக்கும் பருவம் மேலுரம்: பொட்டாஷ் (15 கிலோ/ஏக்கர்)",
                category = TaskCategory.FERTILIZATION,
                dueDateEpoch = now + (10 * dayMs),
                isCompleted = false
            ),
            CareTask(
                id = "task_5",
                title = "Drip Line Flush & Soil Moisture Inspection",
                titleTa = "சொட்டுநீர் குழாய் சுத்தம் செய்தல் & ஈரப்பதம் சரிபார்த்தல்",
                category = TaskCategory.IRRIGATION,
                dueDateEpoch = now + (15 * dayMs),
                isCompleted = false
            )
        )
    }
}
