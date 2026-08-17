package com.agrishield.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrishield.app.data.model.ConfidenceLevel
import com.agrishield.app.data.model.RiskLevel
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

    val isTamil = user?.preferredLanguage == "ta"

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
                            text = "${user?.location ?: "Tamil Nadu"} • ${user?.primaryCrop ?: "Tomato"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshDashboard() }) {
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1. Live Weather Card
            WeatherCard(
                weather = weather,
                modifier = Modifier.clickable { onNavigate(Screen.WeatherRisk.route) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Farm Health Score & Risk Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTamil) "பண்ணை ஆரோக்கியம்" else "Farm Health Score",
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
                            size = 90.dp,
                            strokeWidth = 9.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Risk Indicator Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F8F3))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (risk.level == RiskLevel.HIGH) AgriRedAlert else AgriGoldAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTamil) "நோய் அபாய நிலை" else "Disease Risk:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        RiskBadge(riskLevel = risk.level, isTamil = isTamil)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Quick Actions Grid
            SectionHeader(title = if (isTamil) "விரைவு சேவைகள்" else "Quick Services")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionPill(
                    title = if (isTamil) "இலை ஸ்கேன்" else "Diagnose Leaf",
                    subtitle = if (isTamil) "AI கண்டறிதல்" else "TFLite AI",
                    icon = Icons.Default.PhotoCamera,
                    iconBg = AgriGreenMint,
                    iconTint = AgriGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Diagnose.route) }
                )

                QuickActionPill(
                    title = if (isTamil) "அக்ரிபாட்" else "AgriBot AI",
                    subtitle = if (isTamil) "ஜெமினி ஆலோசனை" else "Tamil/English",
                    icon = Icons.Default.Chat,
                    iconBg = Color(0xFFFFF8E1),
                    iconTint = AgriGoldAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.AgriBot.route) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionPill(
                    title = if (isTamil) "வானிலை & அபாயம்" else "Weather & Risk",
                    subtitle = if (isTamil) "முன்னறிவிப்பு" else "Forecast + Radar",
                    icon = Icons.Default.Cloud,
                    iconBg = Color(0xFFE3F2FD),
                    iconTint = Color(0xFF1976D2),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.WeatherRisk.route) }
                )

                QuickActionPill(
                    title = if (isTamil) "மண் வளம்" else "Soil Health",
                    subtitle = if (isTamil) "NPK & pH" else "Nutrient Test",
                    icon = Icons.Default.Eco,
                    iconBg = Color(0xFFE8F5E9),
                    iconTint = AgriGreenPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.SoilHealth.route) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Recent Diagnosis Snapshot
            SectionHeader(
                title = if (isTamil) "சமீபத்திய பயிர் நோயறிதல்" else "Recent Crop Diagnosis",
                actionText = if (isTamil) "மாதிரி தகவல்" else "Model Specs",
                onActionClick = { onNavigate(Screen.ModelInfo.route) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (diagnosis == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AgriGreenPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isTamil) "இன்னும் பயிர் நோயறிதல் செய்யப்படவில்லை. இலை ஸ்கேன் செய்யவும்." else "No leaf scans recorded yet. Tap Diagnose to scan.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
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
                    Column(modifier = Modifier.padding(16.dp)) {
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

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isTamil && diagnosis!!.treatmentTa.isNotBlank()) diagnosis!!.treatmentTa else diagnosis!!.treatmentEn,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            maxLines = 2
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = iconBg),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary))
        }
    }
}
