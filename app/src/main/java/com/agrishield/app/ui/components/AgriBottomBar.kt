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
import androidx.compose.ui.res.stringResource
import com.agrishield.app.R
import com.agrishield.app.ui.navigation.Screen
import com.agrishield.app.ui.theme.AgriGreenMint
import com.agrishield.app.ui.theme.AgriGreenPrimary

@Composable
fun AgriBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, R.string.nav_home, Icons.Default.Home),
        BottomNavItem(Screen.Diagnose.route, R.string.nav_diagnose, Icons.Default.PhotoCamera),
        BottomNavItem(Screen.AgriBot.route, R.string.nav_agribot, Icons.Default.Chat),
        BottomNavItem(Screen.WeatherRisk.route, R.string.nav_weather, Icons.Default.Cloud),
        BottomNavItem(Screen.Profile.route, R.string.nav_profile, Icons.Default.Person)
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
                icon = { Icon(item.icon, contentDescription = stringResource(item.titleRes)) },
                label = { Text(stringResource(item.titleRes)) },
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
    val titleRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
