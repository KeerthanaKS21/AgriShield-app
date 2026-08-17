package com.agrishield.app.ui.screens.weather

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
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
import com.agrishield.app.data.model.ForecastItem
import com.agrishield.app.data.model.IrrigationAction
import com.agrishield.app.data.model.RiskLevel
import com.agrishield.app.ui.components.RiskBadge
import com.agrishield.app.ui.components.SectionHeader
import com.agrishield.app.ui.components.WeatherCard
import com.agrishield.app.ui.theme.AgriGoldAmber
import com.agrishield.app.ui.theme.AgriGreenDark
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.ui.theme.AgriOrangeWarn
import com.agrishield.app.ui.theme.AgriRedAlert
import com.agrishield.app.ui.theme.BorderLight
import com.agrishield.app.ui.theme.TextPrimary
import com.agrishield.app.ui.theme.TextSecondary
import com.agrishield.app.ui.viewmodel.WeatherRiskViewModel
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
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Weather & Crop Risk",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Live Weather Card
            WeatherCard(weather = weather)

            if (isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(24.dp))
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error!!,
                        color = AgriRedAlert,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Dynamic Irrigation Advisory Card
            if (irrigation != null) {
                SectionHeader(title = "Dynamic Irrigation Advisory")
                Spacer(modifier = Modifier.height(10.dp))

                val (actionBg, actionColor, actionText) = when (irrigation!!.action) {
                    IrrigationAction.HOLD_DO_NOT_IRRIGATE -> Triple(Color(0xFFFFEBEE), AgriRedAlert, "HOLD - DO NOT IRRIGATE")
                    IrrigationAction.REDUCE -> Triple(Color(0xFFFFF3E0), AgriGoldAmber, "REDUCE WATERING (-30%)")
                    IrrigationAction.INCREASE -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "INCREASE WATERING (+25%)")
                    IrrigationAction.NORMAL -> Triple(AgriGreenMint, AgriGreenPrimary, "NORMAL IRRIGATION SCHEDULE")
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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

                        Text(text = irrigation!!.reasonEn, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = irrigation!!.reasonTa, style = MaterialTheme.typography.bodySmall.copy(color = AgriGreenDark))

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Optimal Next Window:", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                            Text(text = irrigation!!.nextIrrigationWindow, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AgriGreenPrimary))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Agronomic Crop Risk Breakdown
            if (risk != null) {
                SectionHeader(title = "Crop Disease Risk Analysis")
                Spacer(modifier = Modifier.height(10.dp))

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
                            Column {
                                Text(
                                    text = "Risk Level: ${risk!!.scorePercentage}%",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Target Threat: ${risk!!.primaryRiskDisease}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                            RiskBadge(riskLevel = risk!!.level)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Meteorological Risk Factors:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        risk!!.riskFactors.forEach { factor ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(AgriGreenPrimary, RoundedCornerShape(3.dp)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = factor, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Preventive Action:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(text = risk!!.adviceEn, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = risk!!.adviceTa, style = MaterialTheme.typography.bodySmall.copy(color = AgriGreenDark))
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // 5-Day Forecast Carousel
            if (forecast.isNotEmpty()) {
                SectionHeader(title = "5-Day Agricultural Forecast")
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(forecast.take(12)) { item ->
                        ForecastCard(item = item)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
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
        modifier = Modifier.width(110.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = dateStr, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            Spacer(modifier = Modifier.height(8.dp))
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = AgriGoldAmber, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
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
