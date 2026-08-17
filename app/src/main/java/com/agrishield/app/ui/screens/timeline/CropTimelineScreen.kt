package com.agrishield.app.ui.screens.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrishield.app.data.model.CareTask
import com.agrishield.app.data.model.CropTimeline
import com.agrishield.app.data.model.GrowthStage
import com.agrishield.app.data.model.TaskCategory
import com.agrishield.app.ui.components.AddCropBottomSheet
import com.agrishield.app.ui.components.AgriPrimaryButton
import com.agrishield.app.ui.components.SectionHeader
import com.agrishield.app.ui.theme.AgriGoldAmber
import com.agrishield.app.ui.theme.AgriGreenDark
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.ui.theme.BorderLight
import com.agrishield.app.ui.theme.TextPrimary
import com.agrishield.app.ui.theme.TextSecondary
import com.agrishield.app.ui.viewmodel.TimelineViewModel
import com.agrishield.app.utils.AppLanguageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropTimelineScreen(
    viewModel: TimelineViewModel
) {
    val crops by viewModel.crops.collectAsState()
    val selectedCropId by viewModel.selectedCropId.collectAsState()
    val timeline by viewModel.timeline.collectAsState()
    val isTamil = AppLanguageManager.isTamil()

    var showAddCropSheet by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isTamil) "பயிர் பராமரிப்பு காலவரிசை" else "Crop Care Timeline",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AgriGreenMint),
                        modifier = Modifier.clickable { showAddCropSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTamil) "+ பயிர் சேர்" else "+ Add Crop",
                                color = AgriGreenPrimary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = AgriGreenPrimary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
                text = { Text(if (isTamil) "பணி சேர்க்க" else "Add Care Task", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = Color(0xFFF7FAF8)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Horizontal Crop Selector Carousel
            item {
                Text(
                    text = if (isTamil) "உங்கள் பண்ணை பயிர்கள் (${crops.size})" else "Your Farm Crops (${crops.size})",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(crops) { crop ->
                        val isSelected = crop.id == selectedCropId
                        val displayName = if (isTamil) crop.cropNameTa else crop.cropName
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCrop(crop.id) },
                            label = {
                                Text(
                                    text = "$displayName (${crop.fieldPlotName})",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = if (isSelected) AgriGreenPrimary else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AgriGreenMint,
                                selectedLabelColor = AgriGreenPrimary
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = false,
                            onClick = { showAddCropSheet = true },
                            label = { Text(if (isTamil) "+ புதிய பயிர்" else "+ New Crop", color = AgriGreenPrimary, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFE8F5E9)
                            )
                        )
                    }
                }
            }

            // 2. Active Crop Details & Growth Stage Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriGreenPrimary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTamil) timeline.cropNameTa else timeline.cropName,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "${timeline.variety} • ${timeline.fieldPlotName} • ${timeline.areaAcres} ${if (isTamil) "ஏக்கர்" else "Acres"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            }

                            // GPS Location Tag Badge
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = timeline.locationName,
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        if (timeline.latitude != null && timeline.longitude != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = AgriGreenMint, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "GPS: ${String.format("%.4f", timeline.latitude)}° N, ${String.format("%.4f", timeline.longitude)}° E",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = AgriGreenMint)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${if (isTamil) "வளர்ச்சிப் பருவம்: " else "Growth Stage: "}${getGrowthStageLabel(timeline.currentStage, isTamil)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stage Tracker Progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            GrowthStage.values().forEach { stage ->
                                val isCurrent = stage == timeline.currentStage
                                val isPast = stage.ordinal < timeline.currentStage.ordinal

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { viewModel.updateGrowthStage(stage) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .background(
                                                color = when {
                                                    isCurrent -> Color(0xFFFFD54F)
                                                    isPast -> AgriGreenMint
                                                    else -> Color.White.copy(alpha = 0.3f)
                                                },
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPast) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = getGrowthStageShort(stage, isTamil),
                                        fontSize = 10.sp,
                                        color = if (isCurrent) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.7f),
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Scheduled Tasks Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(title = if (isTamil) "திட்டமிடப்பட்ட விவசாய பராமரிப்பு பணிகள்" else "Scheduled Agronomic Tasks")
                }
            }

            if (timeline.tasks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = if (isTamil) "பணிகள் எதுவும் இல்லை. புதிய பணி சேர்க்கவும்." else "No care tasks scheduled. Tap Add Care Task below.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }
                }
            } else {
                items(timeline.tasks) { task ->
                    TaskItemCard(
                        task = task,
                        isTamil = isTamil,
                        onToggle = { viewModel.toggleTask(task.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add Crop Sheet
    if (showAddCropSheet) {
        AddCropBottomSheet(
            onDismiss = { showAddCropSheet = false },
            onAddCrop = { name, nameTa, varName, plot, area, sowingDate, stage, locName, lat, lon ->
                viewModel.addNewCrop(name, nameTa, varName, plot, area, sowingDate, stage, locName, lat, lon)
            },
            onFetchGpsLocation = { viewModel.getCurrentGpsLocation() }
        )
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskTitleTa by remember { mutableStateOf("") }
        var dueDays by remember { mutableStateOf("7") }
        var category by remember { mutableStateOf(TaskCategory.FERTILIZATION) }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text(if (isTamil) "புதிய பராமரிப்பு பணி சேர்த்தல்" else "Add Care Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Title (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = taskTitleTa,
                        onValueChange = { taskTitleTa = it },
                        label = { Text("பணி தலைப்பு (தமிழ்)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dueDays,
                        onValueChange = { dueDays = it },
                        label = { Text(if (isTamil) "நாட்கள் இடைவெளி" else "Due in (Days)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (taskTitle.isNotBlank() || taskTitleTa.isNotBlank()) {
                            val days = dueDays.toIntOrNull() ?: 7
                            viewModel.addNewTask(
                                title = taskTitle.ifBlank { taskTitleTa },
                                titleTa = taskTitleTa.ifBlank { taskTitle },
                                category = category,
                                dueDaysFromNow = days
                            )
                        }
                        showAddTaskDialog = false
                    }
                ) {
                    Text(if (isTamil) "சேர்" else "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}

private fun getGrowthStageLabel(stage: GrowthStage, isTamil: Boolean): String {
    return when (stage) {
        GrowthStage.SOWING -> if (isTamil) "விதைப்பு" else "Sowing"
        GrowthStage.SEEDLING -> if (isTamil) "நாற்றுப் பருவம்" else "Seedling"
        GrowthStage.VEGETATIVE -> if (isTamil) "தழை வளர்ச்சிப் பருவம்" else "Vegetative Stage"
        GrowthStage.FLOWERING -> if (isTamil) "பூக்கும் பருவம்" else "Flowering Stage"
        GrowthStage.FRUITING -> if (isTamil) "காய்க்கும் பருவம்" else "Fruiting Stage"
        GrowthStage.HARVEST -> if (isTamil) "அறுவடைப் பருவம்" else "Harvest Stage"
    }
}

private fun getGrowthStageShort(stage: GrowthStage, isTamil: Boolean): String {
    return when (stage) {
        GrowthStage.SOWING -> if (isTamil) "விதை" else "Sow"
        GrowthStage.SEEDLING -> if (isTamil) "நாற்று" else "Seed"
        GrowthStage.VEGETATIVE -> if (isTamil) "வளர்ச்சி" else "Veg"
        GrowthStage.FLOWERING -> if (isTamil) "பூ" else "Flow"
        GrowthStage.FRUITING -> if (isTamil) "காய்" else "Fruit"
        GrowthStage.HARVEST -> if (isTamil) "அறுவடை" else "Harv"
    }
}

@Composable
private fun TaskItemCard(
    task: CareTask,
    isTamil: Boolean,
    onToggle: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(task.dueDateEpoch))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (task.isCompleted) Color(0xFFF1F8F3) else Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Toggle Task",
                tint = if (task.isCompleted) AgriGreenPrimary else TextSecondary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                val primaryText = if (isTamil && task.titleTa.isNotBlank()) task.titleTa else task.title
                val secondaryText = if (isTamil && task.titleTa.isNotBlank()) task.title else task.titleTa

                Text(
                    text = primaryText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) TextSecondary else TextPrimary,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                    )
                )
                if (secondaryText.isNotBlank() && secondaryText != primaryText) {
                    Text(
                        text = secondaryText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (task.isCompleted) TextSecondary.copy(alpha = 0.7f) else AgriGreenDark,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${if (isTamil) "காலக்கெடு: " else "Due: "}$dateStr • ${task.category.name}",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }
        }
    }
}
