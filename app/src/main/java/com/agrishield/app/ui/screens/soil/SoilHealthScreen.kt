package com.agrishield.app.ui.screens.soil

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrishield.app.ui.components.AgriPrimaryButton
import com.agrishield.app.ui.components.SectionHeader
import com.agrishield.app.ui.theme.AgriGoldAmber
import com.agrishield.app.ui.theme.AgriGreenDark
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.ui.theme.BorderLight
import com.agrishield.app.ui.theme.TextPrimary
import com.agrishield.app.ui.theme.TextSecondary
import com.agrishield.app.ui.viewmodel.SoilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoilHealthScreen(
    viewModel: SoilViewModel
) {
    val soilData by viewModel.latestSoil.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    var nText by remember { mutableStateOf(soilData?.nitrogenMgKg?.toInt()?.toString() ?: "140") }
    var pText by remember { mutableStateOf(soilData?.phosphorusMgKg?.toInt()?.toString() ?: "35") }
    var kText by remember { mutableStateOf(soilData?.potassiumMgKg?.toInt()?.toString() ?: "180") }
    var phText by remember { mutableStateOf(soilData?.ph?.toString() ?: "6.5") }
    var moistureText by remember { mutableStateOf(soilData?.moisturePercent?.toInt()?.toString() ?: "45") }
    var cropText by remember { mutableStateOf("Tomato") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Soil Health Assessment",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
            // Notice Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFF57F17),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Note: Enter laboratory soil test values or estimated values below to calculate nutrient balance and fertilizer dosages.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5D4037))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nutrient Entry Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Enter Soil Parameters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = nText,
                            onValueChange = { nText = it },
                            label = { Text("N (mg/kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                        )
                        OutlinedTextField(
                            value = pText,
                            onValueChange = { pText = it },
                            label = { Text("P (mg/kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                        )
                        OutlinedTextField(
                            value = kText,
                            onValueChange = { kText = it },
                            label = { Text("K (mg/kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = phText,
                            onValueChange = { phText = it },
                            label = { Text("pH (0 - 14)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                        )
                        OutlinedTextField(
                            value = moistureText,
                            onValueChange = { moistureText = it },
                            label = { Text("Moisture (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = cropText,
                        onValueChange = { cropText = it },
                        label = { Text("Target Crop (e.g. Tomato, Rice, Potato, Corn)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSaving) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AgriGreenPrimary)
                        }
                    } else {
                        AgriPrimaryButton(
                            text = "Calculate Soil Balance & Dosages",
                            icon = Icons.Default.Eco,
                            onClick = {
                                val n = nText.toDoubleOrNull() ?: 140.0
                                val p = pText.toDoubleOrNull() ?: 35.0
                                val k = kText.toDoubleOrNull() ?: 180.0
                                val ph = phText.toDoubleOrNull() ?: 6.5
                                val moisture = moistureText.toDoubleOrNull() ?: 45.0
                                viewModel.evaluateAndSave(n, p, k, ph, moisture, cropText)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Evaluation Results & Recommendations
            if (soilData != null) {
                SectionHeader(title = "Soil Agronomic Recommendations")
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
                            Text(
                                text = "Soil Health Index: ${soilData!!.healthIndex}/100",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreenPrimary
                                )
                            )
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = AgriGreenMint)
                            ) {
                                Text(
                                    text = "Target: ${soilData!!.targetCrop}",
                                    color = AgriGreenPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Actionable Nutrient Recommendations (English):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        soilData!!.recommendationsEn.forEach { rec ->
                            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                Box(modifier = Modifier.padding(top = 6.dp).size(6.dp).background(AgriGreenPrimary, RoundedCornerShape(3.dp)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = rec, style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "பரிந்துரைகள் (தமிழ்):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = AgriGreenDark)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        soilData!!.recommendationsTa.forEach { rec ->
                            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                Box(modifier = Modifier.padding(top = 6.dp).size(6.dp).background(AgriGreenDark, RoundedCornerShape(3.dp)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = rec, style = MaterialTheme.typography.bodySmall.copy(color = AgriGreenDark))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
