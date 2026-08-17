package com.agrishield.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.agrishield.app.AgriShieldApp
import com.agrishield.app.ui.components.AgriBottomBar
import com.agrishield.app.ui.screens.agribot.AgriBotScreen
import com.agrishield.app.ui.screens.auth.AuthScreen
import com.agrishield.app.ui.screens.diagnose.DiagnoseScreen
import com.agrishield.app.ui.screens.home.HomeScreen
import com.agrishield.app.ui.screens.modelinfo.ModelInfoScreen
import com.agrishield.app.ui.screens.profile.ProfileSettingsScreen
import com.agrishield.app.ui.screens.soil.SoilHealthScreen
import com.agrishield.app.ui.screens.splash.SplashScreen
import com.agrishield.app.ui.screens.timeline.CropTimelineScreen
import com.agrishield.app.ui.screens.weather.WeatherRiskScreen
import com.agrishield.app.ui.viewmodel.AgriBotViewModel
import com.agrishield.app.ui.viewmodel.AuthViewModel
import com.agrishield.app.ui.viewmodel.DiagnoseViewModel
import com.agrishield.app.ui.viewmodel.HomeViewModel
import com.agrishield.app.ui.viewmodel.SettingsViewModel
import com.agrishield.app.ui.viewmodel.SoilViewModel
import com.agrishield.app.ui.viewmodel.TimelineViewModel
import com.agrishield.app.ui.viewmodel.WeatherRiskViewModel

@Composable
fun AgriNavHost(
    navController: NavHostController,
    app: AgriShieldApp
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Diagnose.route,
        Screen.AgriBot.route,
        Screen.WeatherRisk.route,
        Screen.Profile.route
    )

    // ViewModels instantiated from Application container
    val authViewModel = AuthViewModel(app.authRepository)
    val homeViewModel = HomeViewModel(
        app.authRepository,
        app.diagnosisRepository,
        app.weatherRepository,
        app.farmHealthRepository,
        app.soilRepository,
        app.timelineRepository,
        app.locationHelper
    )
    val diagnoseViewModel = DiagnoseViewModel(app.diagnosisRepository, app.authRepository)
    val agriBotViewModel = AgriBotViewModel(
        app.agriBotRepository,
        app.speechRecognizer,
        app.authRepository,
        app.diagnosisRepository,
        app.weatherRepository
    )
    val weatherRiskViewModel = WeatherRiskViewModel(
        app.weatherRepository,
        app.diagnosisRepository,
        app.soilRepository,
        app.authRepository,
        app.locationHelper,
        app.timelineRepository
    )
    val soilViewModel = SoilViewModel(app.soilRepository, app.authRepository)
    val timelineViewModel = TimelineViewModel(app.timelineRepository, app.authRepository, app.locationHelper)
    val settingsViewModel = SettingsViewModel(app, app.authRepository)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AgriBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onTimeout = {
                        val isLoggedIn = app.authManager.isUserLoggedIn
                        val destination = if (isLoggedIn) Screen.Home.route else Screen.Auth.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Auth.route) {
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            composable(Screen.Diagnose.route) {
                DiagnoseScreen(
                    viewModel = diagnoseViewModel,
                    onNavigateToAgriBot = { prompt ->
                        agriBotViewModel.sendMessage(prompt)
                        navController.navigate(Screen.AgriBot.route)
                    }
                )
            }

            composable(Screen.AgriBot.route) {
                AgriBotScreen(viewModel = agriBotViewModel)
            }

            composable(Screen.WeatherRisk.route) {
                WeatherRiskScreen(viewModel = weatherRiskViewModel)
            }

            composable(Screen.SoilHealth.route) {
                SoilHealthScreen(viewModel = soilViewModel)
            }

            composable(Screen.Timeline.route) {
                CropTimelineScreen(viewModel = timelineViewModel)
            }

            composable(Screen.ModelInfo.route) {
                ModelInfoScreen(
                    supportedClasses = app.cropClassifier.getSupportedClasses(),
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                ProfileSettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToModelInfo = { navController.navigate(Screen.ModelInfo.route) },
                    onLoggedOut = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
