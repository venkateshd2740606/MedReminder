package com.medreminder.domain.model

enum class AppTheme(val displayName: String) {
    SYSTEM("System"), LIGHT("Light"), DARK("Dark")
}

enum class LogStatus { TAKEN, SKIPPED, SNOOZED }

data class UserPreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val hapticFeedback: Boolean = true,
    val soundEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val highContrastMode: Boolean = false,
    val fontScale: Float = 1.0f,
    val adsEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val consentGiven: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val personalizedAds: Boolean = false,
    val language: String = "system",
    val remindersEnabled: Boolean = true
)

data class Medication(
    val id: Long = 0,
    val name: String,
    val dose: String,
    val scheduleTimes: List<String>,
    val isVaccine: Boolean = false,
    val notes: String = "",
    val active: Boolean = true
)

data class ReminderLog(
    val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val scheduledTime: String,
    val date: String,
    val status: LogStatus,
    val timestamp: Long
)

data class ScheduledDose(
    val medication: Medication,
    val scheduledTime: String,
    val date: String,
    val log: ReminderLog? = null
) {
    val isTaken: Boolean get() = log?.status == LogStatus.TAKEN
    val isSkipped: Boolean get() = log?.status == LogStatus.SKIPPED
    val isPending: Boolean get() = log == null
}

data class VaccineInfo(
    val name: String,
    val recommendedAge: String,
    val notes: String
)
