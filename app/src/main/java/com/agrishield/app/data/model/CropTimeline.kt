package com.agrishield.app.data.model

data class CropTimeline(
    val id: String = "",
    val cropName: String = "Tomato",
    val variety: String = "Hybrid US-440",
    val plantingDateEpoch: Long = System.currentTimeMillis() - (25L * 24 * 60 * 60 * 1000), // 25 days ago
    val currentStage: GrowthStage = GrowthStage.VEGETATIVE,
    val expectedHarvestDateEpoch: Long = System.currentTimeMillis() + (65L * 24 * 60 * 60 * 1000),
    val tasks: List<CareTask> = emptyList()
)

enum class GrowthStage {
    SOWING,
    SEEDLING,
    VEGETATIVE,
    FLOWERING,
    FRUITING,
    HARVEST
}

data class CareTask(
    val id: String = "",
    val title: String = "",
    val titleTa: String = "",
    val category: TaskCategory = TaskCategory.FERTILIZATION,
    val dueDateEpoch: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String = ""
)

enum class TaskCategory {
    IRRIGATION,
    FERTILIZATION,
    PEST_INSPECTION,
    DISEASE_SPRAY,
    WEEDING,
    HARVESTING
}
