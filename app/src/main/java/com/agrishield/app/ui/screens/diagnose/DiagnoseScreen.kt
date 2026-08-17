package com.agrishield.app.ui.screens.diagnose

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrishield.app.data.model.ConfidenceLevel
import com.agrishield.app.ui.components.AgriOutlinedButton
import com.agrishield.app.ui.components.AgriPrimaryButton
import com.agrishield.app.ui.navigation.Screen
import com.agrishield.app.ui.theme.AgriGoldAmber
import com.agrishield.app.ui.theme.AgriGreenDark
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.ui.theme.AgriOrangeWarn
import com.agrishield.app.ui.theme.AgriRedAlert
import com.agrishield.app.ui.theme.BorderLight
import com.agrishield.app.ui.theme.TextPrimary
import com.agrishield.app.ui.theme.TextSecondary
import com.agrishield.app.ui.viewmodel.DiagnoseViewModel
import com.agrishield.app.utils.CameraHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnoseScreen(
    viewModel: DiagnoseViewModel,
    onNavigateToAgriBot: (String) -> Unit
) {
    val context = LocalContext.current
    val bitmap by viewModel.selectedBitmap.collectAsState()
    val imageUri by viewModel.selectedImageUri.collectAsState()
    val diagnosis by viewModel.currentDiagnosis.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val correctedBitmap = CameraHelper.loadAndCorrectBitmap(context, tempCameraUri!!)
            if (correctedBitmap != null) {
                val cachedPath = CameraHelper.saveBitmapToCache(context, correctedBitmap)
                viewModel.setImage(correctedBitmap, tempCameraUri)
            }
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val loadedBitmap = CameraHelper.loadAndCorrectBitmap(context, uri)
            if (loadedBitmap != null) {
                viewModel.setImage(loadedBitmap, uri)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crop Disease Diagnosis",
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
            // Instruction Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF5EE))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = AgriGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Take a close-up photo of the affected plant leaf under clear daylight for highest ML accuracy.",
                        style = MaterialTheme.typography.bodySmall.copy(color = AgriGreenDark)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Leaf Image Preview Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Selected Leaf Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(containerColor = AgriGreenMint),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = AgriGreenPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Image Selected",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Capture with Camera or Select from Gallery",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Camera & Gallery)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AgriOutlinedButton(
                    text = "Camera",
                    icon = Icons.Default.PhotoCamera,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val uri = CameraHelper.createTempImageUri(context)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    }
                )

                AgriOutlinedButton(
                    text = "Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    modifier = Modifier.weight(1f),
                    onClick = { galleryLauncher.launch("image/*") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Run Analysis Button
            if (bitmap != null) {
                if (isAnalyzing) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AgriGreenPrimary)
                    }
                } else {
                    AgriPrimaryButton(
                        text = "Analyze Leaf with TensorFlow Lite",
                        icon = Icons.Default.Psychology,
                        onClick = { viewModel.analyzeImage() }
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = AgriRedAlert,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Results Card
            if (diagnosis != null) {
                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Title & Confidence Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = diagnosis!!.crop,
                                    style = MaterialTheme.typography.labelMedium.copy(color = AgriGreenPrimary, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = diagnosis!!.disease,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                            }

                            // Confidence Badge
                            val confBg = when (diagnosis!!.confidenceLevel) {
                                ConfidenceLevel.HIGH -> AgriGreenMint
                                ConfidenceLevel.MEDIUM -> Color(0xFFFFF3E0)
                                ConfidenceLevel.LOW -> Color(0xFFFFEBEE)
                            }
                            val confColor = when (diagnosis!!.confidenceLevel) {
                                ConfidenceLevel.HIGH -> AgriGreenPrimary
                                ConfidenceLevel.MEDIUM -> AgriGoldAmber
                                ConfidenceLevel.LOW -> AgriRedAlert
                            }

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = confBg)
                            ) {
                                Text(
                                    text = "${String.format("%.1f", diagnosis!!.confidence)}%",
                                    color = confColor,
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Low Confidence Warning Handling
                        if (diagnosis!!.confidenceLevel == ConfidenceLevel.LOW) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = AgriOrangeWarn)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Unable to confidently identify the disease (<50% confidence). Please capture a clearer, closer image of the leaf.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF5D4037))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Explanation
                        Text(
                            text = "Pathology Explanation:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = diagnosis!!.explanation,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Treatment Guidance (English & Tamil)
                        Text(
                            text = "Recommended Agronomic Treatment:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = diagnosis!!.treatmentEn,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "பரிந்துரைக்கப்பட்ட சிகிச்சை:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = AgriGreenDark)
                        )
                        Text(
                            text = diagnosis!!.treatmentTa,
                            style = MaterialTheme.typography.bodyMedium.copy(color = AgriGreenDark)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Consult AgriBot Button
                        AgriOutlinedButton(
                            text = "Consult AgriBot for Step-by-Step Plan",
                            icon = Icons.Default.Psychology,
                            onClick = {
                                onNavigateToAgriBot("My crop has ${diagnosis!!.disease}. Please provide an exact step-by-step treatment schedule.")
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Save to Cloud Records Button
                        if (saveSuccess) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AgriGreenPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Saved to Farm Records",
                                    color = AgriGreenPrimary,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        } else if (isSaving) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AgriGreenPrimary, modifier = Modifier.size(24.dp))
                            }
                        } else {
                            AgriPrimaryButton(
                                text = "Save to Farm History",
                                icon = Icons.Default.CloudUpload,
                                onClick = { viewModel.saveDiagnosis() }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
