package com.agrishield.app.ui.screens.weather

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrishield.app.data.model.CropTimeline
import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.GrowthStage
import com.agrishield.app.data.model.IrrigationAction
import com.agrishield.app.data.model.RiskLevel
import com.agrishield.app.ui.components.RiskBadge
import com.agrishield.app.ui.components.SectionHeader
import com.agrishield.app.ui.components.WeatherCard
import com.agrishield.app.ui.theme.AgriGoldAmber
import com.agrishield.app.ui.theme.AgriGreenDark
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.ui.theme.AgriRedAlert
import com.agrishield.app.ui.theme.BorderLight
import com.agrishield.app.ui.theme.TextPrimary
import com.agrishield.app.ui.theme.TextSecondary
import com.agrishield.app.ui.viewmodel.WeatherRiskViewModel
import com.agrishield.app.utils.AppLanguageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherRiskScreen(
    viewModel: WeatherRiskViewModel
) {
    val weather by viewModel.currentWeather.collectAsState()
    val forecast by viewModel.forecast.collectAsState()
    val risk by viewModel.cropRisk.collectAsState()
    val irrigation by viewModel.irrigationAdvice.collectAsState()
    val farmCrops by viewModel.farmCrops.collectAsState()
    val selectedCrop by viewModel.selectedCrop.collectAsState()
    val selectedCropId by viewModel.selectedCropId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val currentLang by AppLanguageManager.currentLanguage.collectAsState()
    val isTa = currentLang.startsWith("ta")

    val daysSinceSowing = ((System.currentTimeMillis() - selectedCrop.sowingDateEpoch) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(1)

    val stageLabel = when (selectedCrop.currentStage) {
        GrowthStage.SOWING -> if (isTa) "விதைப்பு பருவம்" else "Sowing Stage"
        GrowthStage.SEEDLING -> if (isTa) "நாற்றுப் பருவம்" else "Seedling Stage"
        GrowthStage.VEGETATIVE -> if (isTa) "தழை வளர்ச்சிப் பருவம்" else "Vegetative Stage"
        GrowthStage.FLOWERING -> if (isTa) "பூக்கும் பருவம்" else "Flowering Stage"
        GrowthStage.FRUITING -> if (isTa) "காய்க்கும் பருவம்" else "Fruiting Stage"
        GrowthStage.HARVEST -> if (isTa) "அறுவடைப் பருவம்" else "Harvest Stage"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTa) "வானிலை & பயிர் ஆலோசனை" else "Weather & Crop Advisory",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${selectedCrop.locationName} • ${if (isTa) selectedCrop.cropNameTa else selectedCrop.cropName} (${selectedCrop.fieldPlotName})",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadWeatherAndRisk() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = AgriGreenPrimary)
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
            // 1. Live Weather Card for Selected Crop's Location
            WeatherCard(weather = weather, isTamil = isTa)

            if (isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error!!,
                        color = AgriRedAlert,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Farmer's Registered Farm Crops (Shows ONLY the crops the farmer has in the farm)
            SectionHeader(
                title = if (isTa) "என் பண்ணை பயிர்கள் (${farmCrops.size})" else "My Farm Crops (${farmCrops.size})",
                actionText = if (isTa) "காலவரிசை" else "Timeline"
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(farmCrops) { crop ->
                    val isSelected = crop.id == selectedCropId
                    val cropDays = ((System.currentTimeMillis() - crop.sowingDateEpoch) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(1)

                    Card(
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { viewModel.selectCrop(crop.id) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) AgriGreenMint else Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isSelected) AgriGreenPrimary else BorderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isTa) crop.cropNameTa else crop.cropName,
                                    style = MaterialTheme.typography.titleMedium.copy(
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
                                text = "${crop.fieldPlotName} • ${crop.areaAcres} ${if (isTa) "ஏக்கர்" else "Acres"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color.White else Color(0xFFF1F8F3))
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = crop.currentStage.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AgriGreenDark)
                                )
                                Text(
                                    text = "${if (isTa) "நாள் " else "Day "}$cropDays",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Stage & Timeline-Aware Disease Risk Analysis Card
            if (risk != null) {
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
                                        containerColor = if (risk!!.level == RiskLevel.HIGH) Color(0xFFFFEBEE) else AgriGreenMint
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Icon(
                                            Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = if (risk!!.level == RiskLevel.HIGH) AgriRedAlert else AgriGreenPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${if (isTa) selectedCrop.cropNameTa else selectedCrop.cropName} (${selectedCrop.fieldPlotName})",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                    )
                                    Text(
                                        text = "$stageLabel • ${if (isTa) "நாள் " else "Day "}$daysSinceSowing",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = AgriGreenDark, fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }
                            RiskBadge(riskLevel = risk!!.level, isTamil = isTa)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Target Stage Disease Threat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFBF4E8))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = AgriGoldAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (isTa) "பருவ நோய் அச்சுறுத்தல்: " else "Stage Disease Threat: "}${risk!!.primaryRiskDisease}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF6D4C41))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isTa) "வானிலை & காலவரிசை காரணிகள்:" else "Timeline & Weather Risk Factors:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        risk!!.riskFactors.forEach { factor ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(AgriGreenPrimary, RoundedCornerShape(3.dp)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = factor, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isTa) "பருவ வாரியான பாதுகாப்பு தெளிப்பு & வழிகாட்டல்:" else "Stage-Specific Preventive Action:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = AgriGreenDark)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTa) risk!!.adviceTa else risk!!.adviceEn,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, lineHeight = 18.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Stage & Timeline-Aware Smart Irrigation Advisory Card
            if (irrigation != null) {
                val (actionBg, actionColor, actionText) = when (irrigation!!.action) {
                    IrrigationAction.HOLD_DO_NOT_IRRIGATE -> Triple(Color(0xFFFFEBEE), AgriRedAlert, if (isTa) "பாசனம் வேண்டாம் (மழை/அறுவடை)" else "HOLD - DO NOT IRRIGATE")
                    IrrigationAction.REDUCE -> Triple(Color(0xFFFFF3E0), AgriGoldAmber, if (isTa) "பாசனத்தை குறைக்கவும் (-30%)" else "REDUCE WATERING (-30%)")
                    IrrigationAction.INCREASE -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), if (isTa) "கூடுதல் பாசனம் (காய் பருவம்)" else "INCREASE WATERING (+25%)")
                    IrrigationAction.NORMAL -> Triple(AgriGreenMint, AgriGreenPrimary, if (isTa) "வழக்கமான பாசன அட்டவணை" else "OPTIMAL WATERING SCHEDULE")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(actionBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = actionColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = actionText,
                                color = actionColor,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isTa) irrigation!!.reasonTa else irrigation!!.reasonEn,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, lineHeight = 19.sp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F8F3))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Opacity, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${if (isTa) "சிறந்த பாசன நேரம்: " else "Optimal Window: "}${irrigation!!.nextIrrigationWindow}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                            }

                            if (irrigation!!.waterVolumeLitersPerSqm > 0) {
                                Text(
                                    text = "${irrigation!!.waterVolumeLitersPerSqm} L/m²",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AgriGreenPrimary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 5. 5-Day Agricultural Forecast
            if (forecast.isNotEmpty()) {
                SectionHeader(title = if (isTa) "5-நாள் விவசாய வானிலை முன்னறிவிப்பு" else "5-Day Agricultural Forecast")
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(forecast.take(12)) { item ->
                        ForecastCard(item = item)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ForecastCard(item: ForecastItem) {
    val dateStr = SimpleDateFormat("EEE, ha", Locale.getDefault()).format(Date(item.dateTimeEpoch))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = Modifier.width(105.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = dateStr, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp))
            Spacer(modifier = Modifier.height(6.dp))
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = AgriGoldAmber, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "${item.tempCelsius.toInt()}°C", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = "${item.rainProbabilityPercent}%", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF1976D2)))
            }
        }
    }
}
