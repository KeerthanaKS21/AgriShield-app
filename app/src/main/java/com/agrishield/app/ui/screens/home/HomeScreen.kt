package com.agrishield.app.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrishield.app.data.model.ConfidenceLevel
import com.agrishield.app.data.model.CropTimeline
import com.agrishield.app.data.model.GrowthStage
import com.agrishield.app.data.model.IrrigationAction
import com.agrishield.app.data.model.RiskLevel
import com.agrishield.app.ui.components.AddCropBottomSheet
import com.agrishield.app.ui.components.HealthScoreGauge
import com.agrishield.app.ui.components.RiskBadge
import com.agrishield.app.ui.components.SectionHeader
import com.agrishield.app.ui.components.WeatherCard
import com.agrishield.app.ui.navigation.Screen
import com.agrishield.app.ui.theme.AgriGoldAmber
import com.agrishield.app.ui.theme.AgriGreenDark
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.ui.theme.AgriRedAlert
import com.agrishield.app.ui.theme.BorderLight
import com.agrishield.app.ui.theme.TextPrimary
import com.agrishield.app.ui.theme.TextSecondary
import com.agrishield.app.ui.viewmodel.HomeViewModel
import com.agrishield.app.utils.AppLanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val weather by viewModel.currentWeather.collectAsState()
    val diagnosis by viewModel.latestDiagnosis.collectAsState()
    val health by viewModel.farmHealth.collectAsState()
    val risk by viewModel.currentRisk.collectAsState()
    val irrigation by viewModel.currentIrrigation.collectAsState()
    val farmCrops by viewModel.farmCrops.collectAsState()
    val activeCrop by viewModel.activeCrop.collectAsState()
    val selectedCropId by viewModel.selectedCropId.collectAsState()
    val currentLang by AppLanguageManager.currentLanguage.collectAsState()

    val isTamil = currentLang.startsWith("ta")
    var showAddCropSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTamil) "வணக்கம், ${user?.displayName ?: "விவசாயி"}" else "Vanakkam, ${user?.displayName ?: "Farmer"}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${activeCrop.locationName} • ${if (isTamil) activeCrop.cropNameTa else activeCrop.cropName} (${activeCrop.fieldPlotName})",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AgriGreenMint),
                            modifier = Modifier.clickable {
                                AppLanguageManager.toggleLanguage()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = AgriGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTamil) "தமிழ்" else "EN",
                                    color = AgriGreenPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.refreshDashboard() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AgriGreenPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF7FAF8)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // 1. Live Weather Card for Active Crop
            WeatherCard(
                weather = weather,
                isTamil = isTamil,
                modifier = Modifier.clickable { onNavigate(Screen.WeatherRisk.route) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Farm Crops & GPS Carousel Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(title = if (isTamil) "என் பயிர்கள் & GPS (${farmCrops.size})" else "Farm Crops & GPS (${farmCrops.size})")
                TextButton(onClick = { onNavigate(Screen.Timeline.route) }) {
                    Text(
                        text = if (isTamil) "காலவரிசை →" else "Timeline →",
                        color = AgriGreenPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(farmCrops) { crop ->
                    val isSelected = crop.id == selectedCropId
                    CropSummaryCard(
                        crop = crop,
                        isSelected = isSelected,
                        isTamil = isTamil,
                        onClick = { viewModel.selectActiveCrop(crop.id) }
                    )
                }

                item {
                    // Add Crop Card
                    Card(
                        modifier = Modifier
                            .width(130.dp)
                            .height(105.dp)
                            .clickable { showAddCropSheet = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AgriGreenMint)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isTamil) "+ பயிர் சேர்" else "+ Add Crop",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = AgriGreenPrimary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Crop-Specific Disease Risk & Pathology Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (risk.level == RiskLevel.HIGH) Color(0xFFFFEBEE) else AgriGreenMint
                                ),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (risk.level == RiskLevel.HIGH) AgriRedAlert else AgriGreenPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTamil) "பயிர் நோய் ஆய்வு (${activeCrop.cropNameTa})" else "Disease Analysis (${activeCrop.cropName})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = "${if (isTamil) "முதன்மை அச்சுறுத்தல்: " else "Target: "}${risk.primaryRiskDisease}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = AgriGreenDark, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                        RiskBadge(riskLevel = risk.level, isTamil = isTamil)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isTamil) risk.adviceTa else risk.adviceEn,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F8F3))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTamil) "AI தொற்று சாத்தியக்கூறு: ${risk.scorePercentage}%" else "Infection Probability: ${risk.scorePercentage}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AgriGreenDark)
                            )
                        }

                        Text(
                            text = if (isTamil) "ஆலோசனை பெறுக →" else "Consult AgriBot →",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AgriGreenPrimary),
                            modifier = Modifier.clickable {
                                onNavigate(Screen.AgriBot.route)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Crop-Specific Smart Irrigation Advice Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTamil) "பாசன வழிகாட்டி (${activeCrop.cropNameTa})" else "Irrigation Advice (${activeCrop.cropName})",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(
                                    text = if (isTamil) "வளர்ச்சிப் பருவம்: ${activeCrop.currentStage.name}" else "Stage: ${activeCrop.currentStage.name}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                )
                            }
                        }

                        // Action Badge
                        val actionBg = when (irrigation?.action) {
                            IrrigationAction.HOLD_DO_NOT_IRRIGATE -> Color(0xFFFFEBEE)
                            IrrigationAction.INCREASE -> Color(0xFFFFF3E0)
                            else -> AgriGreenMint
                        }
                        val actionColor = when (irrigation?.action) {
                            IrrigationAction.HOLD_DO_NOT_IRRIGATE -> AgriRedAlert
                            IrrigationAction.INCREASE -> AgriGoldAmber
                            else -> AgriGreenPrimary
                        }
                        val actionLabel = when (irrigation?.action) {
                            IrrigationAction.HOLD_DO_NOT_IRRIGATE -> if (isTamil) "பாசனம் வேண்டாம்" else "HOLD WATER"
                            IrrigationAction.INCREASE -> if (isTamil) "கூடுதல் பாசனம்" else "INCREASE WATER"
                            else -> if (isTamil) "வழக்கமான பாசனம்" else "OPTIMAL WATER"
                        }

                        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = actionBg)) {
                            Text(
                                text = actionLabel,
                                color = actionColor,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isTamil) (irrigation?.reasonTa ?: "பயிர் தேவைக்கேற்ப மிதமான பாசனம் செய்யவும்.")
                               else (irrigation?.reasonEn ?: "Apply standard maintenance irrigation for optimal root health."),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F8F3))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Opacity, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (isTamil) "அடுத்த பாசனம்: " else "Next Window: "}${irrigation?.nextIrrigationWindow ?: if (isTamil) "நாளை காலை" else "Tomorrow Morning"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                        }

                        if (irrigation?.waterVolumeLitersPerSqm != null && irrigation!!.waterVolumeLitersPerSqm > 0) {
                            Text(
                                text = "${irrigation!!.waterVolumeLitersPerSqm} L/m²",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AgriGreenPrimary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Farm Health Score Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTamil) "பண்ணை ஆரோக்கிய மதிப்பெண்" else "Overall Farm Health Score",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTamil) health.summaryTa else health.summaryEn,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    HealthScoreGauge(
                        score = health.score,
                        rating = health.rating,
                        size = 80.dp,
                        strokeWidth = 8.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Quick Services Grid
            SectionHeader(title = if (isTamil) "விரைவு சேவைகள்" else "Quick Services")

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionPill(
                    title = if (isTamil) "பயிர் காலவரிசை" else "Crop Timeline",
                    subtitle = if (isTamil) "${activeCrop.tasks.count { !it.isCompleted }} பணிகள் உள்ளன" else "${activeCrop.tasks.count { !it.isCompleted }} Tasks Due",
                    icon = Icons.Default.Timeline,
                    iconBg = Color(0xFFE8F5E9),
                    iconTint = AgriGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Timeline.route) }
                )

                QuickActionPill(
                    title = if (isTamil) "இலை ஸ்கேன் AI" else "Diagnose Leaf",
                    subtitle = if (isTamil) "AI கண்டறிதல்" else "TFLite Scanner",
                    icon = Icons.Default.PhotoCamera,
                    iconBg = AgriGreenMint,
                    iconTint = AgriGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Diagnose.route) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionPill(
                    title = if (isTamil) "அக்ரிபாட் AI" else "AgriBot AI",
                    subtitle = if (isTamil) "தமிழ்/English AI" else "Tamil/English Chat",
                    icon = Icons.AutoMirrored.Filled.Chat,
                    iconBg = Color(0xFFFFF8E1),
                    iconTint = AgriGoldAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.AgriBot.route) }
                )

                QuickActionPill(
                    title = if (isTamil) "வானிலை & ரேடார்" else "Weather & Radar",
                    subtitle = if (isTamil) "நேரலை முன்னறிவிப்பு" else "Live Forecast",
                    icon = Icons.Default.Cloud,
                    iconBg = Color(0xFFE3F2FD),
                    iconTint = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.WeatherRisk.route) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Recent Diagnosis Snapshot
            SectionHeader(
                title = if (isTamil) "சமீபத்திய பயிர் நோயறிதல்" else "Recent Crop Diagnosis",
                actionText = if (isTamil) "மாதிரி தகவல்" else "Model Specs",
                onActionClick = { onNavigate(Screen.ModelInfo.route) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (diagnosis == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isTamil) "இன்னும் பயிர் நோயறிதல் செய்யப்படவில்லை. இலை ஸ்கேன் செய்யவும்." else "No leaf scans recorded yet. Tap Diagnose to scan.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.Diagnose.route) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${diagnosis!!.crop}: ${diagnosis!!.disease}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "${String.format("%.1f", diagnosis!!.confidence)}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (diagnosis!!.confidenceLevel == ConfidenceLevel.HIGH) AgriGreenPrimary else AgriGoldAmber
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isTamil && diagnosis!!.treatmentTa.isNotBlank()) diagnosis!!.treatmentTa else diagnosis!!.treatmentEn,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }

    if (showAddCropSheet) {
        AddCropBottomSheet(
            onDismiss = { showAddCropSheet = false },
            onAddCrop = { name, nameTa, varName, plot, area, sowingDate, stage, locName, lat, lon ->
                viewModel.addNewCrop(name, nameTa, varName, plot, area, sowingDate, stage, locName, lat, lon)
            },
            onFetchGpsLocation = { viewModel.getCurrentGpsLocation() }
        )
    }
}

@Composable
private fun CropSummaryCard(
    crop: CropTimeline,
    isSelected: Boolean,
    isTamil: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(105.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) AgriGreenMint else Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isSelected) AgriGreenPrimary else BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTamil) crop.cropNameTa else crop.cropName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) AgriGreenPrimary else TextPrimary
                    ),
                    maxLines = 1
                )
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = "${crop.fieldPlotName} • ${crop.areaAcres} ${if (isTamil) "ஏக்கர்" else "Acres"}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary),
                maxLines = 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = crop.locationName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextSecondary),
                        maxLines = 1
                    )
                }

                if (crop.latitude != null) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionPill(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = iconBg),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary))
        }
    }
}
