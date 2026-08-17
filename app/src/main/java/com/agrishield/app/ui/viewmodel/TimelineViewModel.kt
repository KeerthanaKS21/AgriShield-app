package com.agrishield.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.CropTimeline
import com.agrishield.app.data.model.GrowthStage
import com.agrishield.app.data.model.TaskCategory
import com.agrishield.app.data.repository.AuthRepository
import com.agrishield.app.data.repository.TimelineRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimelineViewModel(
    private val timelineRepository: TimelineRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val timeline: StateFlow<CropTimeline> = timelineRepository.timeline

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
