package com.agrishield.app.ui.screens.profile

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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrishield.app.ui.components.AgriOutlinedButton
import com.agrishield.app.ui.components.AgriPrimaryButton
import com.agrishield.app.ui.components.SectionHeader
import com.agrishield.app.ui.navigation.Screen
import com.agrishield.app.ui.theme.AgriGoldAmber
import com.agrishield.app.ui.theme.AgriGreenDark
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.ui.theme.AgriRedAlert
import com.agrishield.app.ui.theme.BorderLight
import com.agrishield.app.ui.theme.TextPrimary
import com.agrishield.app.ui.theme.TextSecondary
import com.agrishield.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToModelInfo: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val language by viewModel.currentLanguage.collectAsState()
    val isTa = language.startsWith("ta")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isTa) "விவசாயி சுயவிவரம் & அமைப்புகள்" else "Farmer Profile & Settings",
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
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = AgriGreenMint),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(32.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user?.displayName ?: if (isTa) "விவசாயி" else "Farmer",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Text(
                            text = user?.email ?: "farmer@agrishield.com",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDone, contentDescription = null, tint = AgriGreenPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTa) "ஃபயர்பேஸ் இணைக்கப்பட்டுள்ளது" else "Firebase Authenticated",
                                style = MaterialTheme.typography.labelSmall.copy(color = AgriGreenPrimary)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Language Selection Card
            SectionHeader(title = if (isTa) "பயன்பாட்டு மொழி / App Language" else "App Language / பயன்பாட்டு மொழி")
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isTa) AgriGreenMint else Color(0xFFF5F5F5)),
                        border = if (isTa) androidx.compose.foundation.BorderStroke(1.5.dp, AgriGreenPrimary) else null,
                        modifier = Modifier.weight(1f).clickable { viewModel.setLanguage("ta") }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "தமிழ் (Tamil)", fontWeight = FontWeight.Bold, color = if (isTa) AgriGreenPrimary else TextPrimary)
                        }
                    }

                    val isEn = !isTa
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isEn) AgriGreenMint else Color(0xFFF5F5F5)),
                        border = if (isEn) androidx.compose.foundation.BorderStroke(1.5.dp, AgriGreenPrimary) else null,
                        modifier = Modifier.weight(1f).clickable { viewModel.setLanguage("en") }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "English", fontWeight = FontWeight.Bold, color = if (isEn) AgriGreenPrimary else TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ML Model Transparency Button
            AgriOutlinedButton(
                text = if (isTa) "டென்சார்ஃப்ளோ லைட் மாதிரி தகவல் & மாதிரி விவரங்கள்" else "TensorFlow Lite Model Info & Transparency",
                icon = Icons.Default.Shield,
                onClick = onNavigateToModelInfo
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sign Out Button
            AgriOutlinedButton(
                text = if (isTa) "அக்ரிஷீல்டிலிருந்து வெளியேறு" else "Log Out from AgriShield",
                icon = Icons.Default.Logout,
                onClick = {
                    viewModel.signOut()
                    onLoggedOut()
                }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
