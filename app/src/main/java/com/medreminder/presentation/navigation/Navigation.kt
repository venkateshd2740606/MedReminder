package com.medreminder.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medreminder.ads.AdManager
import com.medreminder.analytics.AnalyticsManager
import com.medreminder.domain.model.UserPreferences
import com.medreminder.presentation.ui.screens.dashboard.DashboardScreen
import com.medreminder.presentation.ui.screens.history.HistoryScreen
import com.medreminder.presentation.ui.screens.medication.AddEditMedicationScreen
import com.medreminder.presentation.ui.screens.settings.SettingsScreen
import com.medreminder.presentation.ui.screens.vaccine.VaccineListScreen

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object AddMedication : Screen("add_medication")
    data object EditMedication : Screen("edit_medication/{id}") {
        fun create(id: Long) = "edit_medication/$id"
    }
    data object Vaccines : Screen("vaccines")
    data object History : Screen("history")
    data object Settings : Screen("settings")
}

@Composable
fun MedReminderNavHost(
    navController: NavHostController,
    adManager: AdManager,
    analyticsManager: AnalyticsManager,
    preferences: UserPreferences,
    startDestination: String = Screen.Dashboard.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val bottomRoutes = setOf(Screen.Dashboard.route, Screen.Vaccines.route, Screen.History.route, Screen.Settings.route)

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Dashboard.route,
                        onClick = { navController.navigateToTab(Screen.Dashboard.route) },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Today") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Vaccines.route,
                        onClick = { navController.navigateToTab(Screen.Vaccines.route) },
                        icon = { Icon(Icons.Default.Vaccines, null) },
                        label = { Text("Vaccines") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.History.route,
                        onClick = { navController.navigateToTab(Screen.History.route) },
                        icon = { Icon(Icons.Default.History, null) },
                        label = { Text("History") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = { navController.navigateToTab(Screen.Settings.route) },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Settings") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddMedication = { navController.navigate(Screen.AddMedication.route) },
                    onEditMedication = { id -> navController.navigate(Screen.EditMedication.create(id)) },
                    adManager = adManager,
                    adsEnabled = preferences.adsEnabled
                )
            }
            composable(Screen.AddMedication.route) {
                AddEditMedicationScreen(medicationId = null, onBack = { navController.popBackStack() })
            }
            composable(
                Screen.EditMedication.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                AddEditMedicationScreen(
                    medicationId = entry.arguments?.getLong("id"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Vaccines.route) { VaccineListScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
