package com.medreminder.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.medreminder.ads.AdManager
import com.medreminder.analytics.AnalyticsManager
import com.medreminder.domain.model.UserPreferences
import com.medreminder.domain.repository.PreferencesRepository
import com.medreminder.presentation.navigation.MedReminderNavHost
import com.medreminder.presentation.navigation.Screen
import com.medreminder.presentation.ui.theme.MedReminderTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var adManager: AdManager
    @Inject lateinit var analyticsManager: AnalyticsManager
    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adManager.initialize()

        setContent {
            val prefs by preferencesRepository.getUserPreferences()
                .collectAsStateWithLifecycle(initialValue = null)

            if (prefs == null) {
                LoadingShell()
                return@setContent
            }

            MedReminderRoot(
                prefs = prefs!!,
                adManager = adManager,
                analyticsManager = analyticsManager
            )
        }
    }
}

@Composable
private fun LoadingShell() {
    MedReminderTheme { Box(Modifier.fillMaxSize()) }
}

@Composable
private fun MedReminderRoot(
    prefs: UserPreferences,
    adManager: AdManager,
    analyticsManager: AnalyticsManager
) {
    LaunchedEffect(prefs.analyticsEnabled) {
        analyticsManager.setCollectionEnabled(prefs.analyticsEnabled)
    }
    LaunchedEffect(prefs.adsEnabled, prefs.personalizedAds) {
        adManager.updateAdPolicy(prefs.adsEnabled, prefs.personalizedAds)
    }

    MedReminderTheme(
        appTheme = prefs.appTheme,
        highContrast = prefs.highContrastMode,
        fontScale = prefs.fontScale
    ) {
        val navController = rememberNavController()
        MedReminderNavHost(
            navController = navController,
            adManager = adManager,
            analyticsManager = analyticsManager,
            preferences = prefs,
            startDestination = Screen.Dashboard.route
        )
    }
}
