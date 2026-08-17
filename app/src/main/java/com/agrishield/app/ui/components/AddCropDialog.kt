package com.agrishield.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrishield.app.data.model.GrowthStage
import com.agrishield.app.ui.theme.AgriGreenDark
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.ui.theme.BorderLight
import com.agrishield.app.ui.theme.TextPrimary
import com.agrishield.app.ui.theme.TextSecondary
import com.agrishield.app.utils.AppLanguageManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCropBottomSheet(
    onDismiss: () -> Unit,
    onAddCrop: (
        cropName: String,
        cropNameTa: String,
        variety: String,
        fieldPlotName: String,
        areaAcres: Double,
        sowingDateEpoch: Long,
        currentStage: GrowthStage,
        locationName: String,
        latitude: Double?,
        longitude: Double?
    ) -> Unit,
    onFetchGpsLocation: suspend () -> Pair<android.location.Location, String>?
) {
    val isTamil = AppLanguageManager.isTamil()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val presetCrops = listOf(
        Pair("Tomato", "தக்காளி"),
        Pair("Rice / Paddy", "நெல்"),
        Pair("Chilli", "மிளகாய்"),
        Pair("Cotton", "பருத்தி"),
        Pair("Banana", "வாழை"),
        Pair("Sugarcane", "கரும்பு"),
        Pair("Maize", "மக்காச்சோளம்"),
        Pair("Groundnut", "நிலக்கடலை")
    )

    var selectedCropPair by remember { mutableStateOf(presetCrops[0]) }
    var customCropName by remember { mutableStateOf("") }
    var isCustomCrop by remember { mutableStateOf(false) }

    var variety by remember { mutableStateOf("Hybrid F1") }
    var plotName by remember { mutableStateOf(if (isTamil) "தோட்டம் 1" else "Plot 1") }
    var areaText by remember { mutableStateOf("2.0") }
    var selectedStage by remember { mutableStateOf(GrowthStage.VEGETATIVE) }
    var daysAgoText by remember { mutableStateOf("15") }

    var locationName by remember { mutableStateOf("Coimbatore") }
    var latitude by remember { mutableStateOf<Double?>(11.0168) }
    var longitude by remember { mutableStateOf<Double?>(76.9558) }
    var isFetchingGps by remember { mutableStateOf(false) }
    var gpsCaptured by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isTamil) "புதிய பயிர் சேர்த்தல்" else "Add New Crop",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Text(
                        text = if (isTamil) "உங்கள் பண்ணையில் புதிய பயிர் & GPS இருப்பிடம் பதிவு செய்க"
                               else "Register crop details, field plot, and GPS location",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Crop Selection Chips
            Text(
                text = if (isTamil) "1. பயிர் தேர்வு" else "1. Select Crop",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = AgriGreenDark)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetCrops.forEach { cropPair ->
                    val isSelected = !isCustomCrop && selectedCropPair == cropPair
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCropPair = cropPair
                            isCustomCrop = false
                        },
                        label = { Text(if (isTamil) cropPair.second else cropPair.first) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgriGreenMint,
                            selectedLabelColor = AgriGreenPrimary
                        )
                    )
                }

                FilterChip(
                    selected = isCustomCrop,
                    onClick = { isCustomCrop = true },
                    label = { Text(if (isTamil) "+ பிற பயிர்" else "+ Other Crop") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AgriGreenMint,
                        selectedLabelColor = AgriGreenPrimary
                    )
                )
            }

            if (isCustomCrop) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customCropName,
                    onValueChange = { customCropName = it },
                    label = { Text(if (isTamil) "பயிர் பெயர் உள்ளிடவும்" else "Enter Custom Crop Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Variety & Field Plot
            Text(
                text = if (isTamil) "2. ரகம் & நில விவரம்" else "2. Variety & Plot Details",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = AgriGreenDark)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = variety,
                    onValueChange = { variety = it },
                    label = { Text(if (isTamil) "பயிர் ரகம்" else "Crop Variety") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                )

                OutlinedTextField(
                    value = plotName,
                    onValueChange = { plotName = it },
                    label = { Text(if (isTamil) "நிலம் / பாத்தி" else "Plot / Field Name") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = areaText,
                    onValueChange = { areaText = it },
                    label = { Text(if (isTamil) "பரப்பளவு (ஏக்கர்)" else "Area (Acres)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                )

                OutlinedTextField(
                    value = daysAgoText,
                    onValueChange = { daysAgoText = it },
                    label = { Text(if (isTamil) "நடவு செய்து (நாட்கள்)" else "Planted (Days Ago)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Current Growth Stage
            Text(
                text = if (isTamil) "3. தற்போதைய வளர்ச்சிப் பருவம்" else "3. Current Growth Stage",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = AgriGreenDark)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GrowthStage.values().forEach { stage ->
                    val isSelected = selectedStage == stage
                    val stageLabel = when (stage) {
                        GrowthStage.SOWING -> if (isTamil) "விதைப்பு" else "Sowing"
                        GrowthStage.SEEDLING -> if (isTamil) "நாற்று" else "Seedling"
                        GrowthStage.VEGETATIVE -> if (isTamil) "வளர்ச்சி" else "Vegetative"
                        GrowthStage.FLOWERING -> if (isTamil) "பூக்கும்" else "Flowering"
                        GrowthStage.FRUITING -> if (isTamil) "காய்க்கும்" else "Fruiting"
                        GrowthStage.HARVEST -> if (isTamil) "அறுவடை" else "Harvest"
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStage = stage },
                        label = { Text(stageLabel, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgriGreenMint,
                            selectedLabelColor = AgriGreenPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. GPS & Location Tagging
            Text(
                text = if (isTamil) "4. இருப்பிடம் & நேரலை GPS" else "4. Location & GPS Tagging",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = AgriGreenDark)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text(if (isTamil) "ஊர் / பகுதியின் பெயர்" else "Village / Region Name") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = AgriGreenPrimary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AgriGreenPrimary)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // GPS Fetch Card Button
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = if (gpsCaptured) AgriGreenMint else Color(0xFFF1F8F3)),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (gpsCaptured) AgriGreenPrimary else BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            isFetchingGps = true
                            val result = onFetchGpsLocation()
                            if (result != null) {
                                val (loc, place) = result
                                latitude = loc.latitude
                                longitude = loc.longitude
                                if (place.isNotBlank()) {
                                    locationName = place
                                }
                                gpsCaptured = true
                            }
                            isFetchingGps = false
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isFetchingGps) {
                            CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = if (gpsCaptured) Icons.Default.CheckCircle else Icons.Default.MyLocation,
                                contentDescription = "GPS",
                                tint = AgriGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (gpsCaptured) (if (isTamil) "GPS இருப்பிடம் இணைக்கப்பட்டது" else "GPS Location Tagged")
                                       else (if (isTamil) "நேரலை GPS இருப்பிடத்தைப் பெறு" else "Use Current GPS Location"),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AgriGreenDark)
                            )
                            if (latitude != null && longitude != null) {
                                Text(
                                    text = "Lat: ${String.format("%.4f", latitude)}, Lon: ${String.format("%.4f", longitude)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = TextSecondary)
                                )
                            }
                        }
                    }

                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            AgriPrimaryButton(
                text = if (isTamil) "பயிரைச் சேர்த்து காலவரிசையை உருவாக்கு" else "Add Crop & Generate Care Timeline",
                icon = Icons.Default.Eco,
                onClick = {
                    val finalCropName = if (isCustomCrop && customCropName.isNotBlank()) customCropName.trim() else selectedCropPair.first
                    val finalCropNameTa = if (isCustomCrop && customCropName.isNotBlank()) customCropName.trim() else selectedCropPair.second
                    val daysAgo = daysAgoText.toLongOrNull() ?: 15L
                    val sowingDate = System.currentTimeMillis() - (daysAgo * 24L * 60 * 60 * 1000)
                    val area = areaText.toDoubleOrNull() ?: 1.0

                    onAddCrop(
                        finalCropName,
                        finalCropNameTa,
                        variety,
                        plotName,
                        area,
                        sowingDate,
                        selectedStage,
                        locationName,
                        latitude,
                        longitude
                    )
                    onDismiss()
                }
            )
        }
    }
}
