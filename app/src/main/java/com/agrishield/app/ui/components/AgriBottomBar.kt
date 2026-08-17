package com.agrishield.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.agrishield.app.R
import com.agrishield.app.ui.navigation.Screen
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary
import com.agrishield.app.utils.AppLanguageManager

@Composable
fun AgriBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val currentLang by AppLanguageManager.currentLanguage.collectAsState()
    val isTa = currentLang.startsWith("ta")

    val items = listOf(
        BottomNavItem(Screen.Home.route, if (isTa) "முகப்பு" else "Home", Icons.Default.Home),
        BottomNavItem(Screen.Diagnose.route, if (isTa) "பயிர் நோய்" else "Diagnose", Icons.Default.PhotoCamera),
        BottomNavItem(Screen.AgriBot.route, if (isTa) "அக்ரிபாட்" else "AgriBot", Icons.Default.Chat),
        BottomNavItem(Screen.WeatherRisk.route, if (isTa) "வானிலை" else "Weather", Icons.Default.Cloud),
        BottomNavItem(Screen.Profile.route, if (isTa) "சுயவிவரம்" else "Profile", Icons.Default.Person)
    )

    NavigationBar(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        contentColor = AgriGreenPrimary
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AgriGreenPrimary,
                    selectedTextColor = AgriGreenPrimary,
                    indicatorColor = AgriGreenMint
                )
            )
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
