package com.medreminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medreminder.domain.model.AppTheme
import com.medreminder.domain.model.LogStatus
import com.medreminder.domain.model.Medication
import com.medreminder.domain.model.ScheduledDose
import com.medreminder.domain.model.UserPreferences
import com.medreminder.domain.model.VaccineInfo
import com.medreminder.domain.repository.MedicationRepository
import com.medreminder.domain.repository.PreferencesRepository
import com.medreminder.domain.repository.ReminderLogRepository
import com.medreminder.engine.MedReminderEngine
import com.medreminder.worker.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedReminderViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val reminderLogRepository: ReminderLogRepository,
    private val engine: MedReminderEngine,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val today = engine.todayDate()

    val todaySchedule: StateFlow<List<ScheduledDose>> = combine(
        medicationRepository.observeActiveMedications(),
        reminderLogRepository.observeLogsForDate(today)
    ) { meds, logs -> engine.buildTodaySchedule(meds, logs, today) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedications = medicationRepository.observeAllMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs = reminderLogRepository.observeRecentLogs(200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaccines: List<VaccineInfo> = engine.childVaccines()

    private val _editingMedication = MutableStateFlow<Medication?>(null)
    val editingMedication: StateFlow<Medication?> = _editingMedication.asStateFlow()

    fun loadMedication(id: Long) {
        viewModelScope.launch {
            _editingMedication.value = medicationRepository.getMedication(id)
        }
    }

    fun startNewMedication() {
        _editingMedication.value = Medication(name = "", dose = "", scheduleTimes = emptyList())
    }

    fun saveMedication(name: String, dose: String, scheduleInput: String, notes: String, isVaccine: Boolean) {
        viewModelScope.launch {
            val times = engine.parseScheduleTimes(scheduleInput)
            val current = _editingMedication.value
            val med = (current ?: Medication(name = name, dose = dose, scheduleTimes = times)).copy(
                name = name, dose = dose, scheduleTimes = times, notes = notes, isVaccine = isVaccine, active = true
            )
            medicationRepository.saveMedication(med)
            ReminderWorker.schedule(context)
            _editingMedication.value = null
        }
    }

    fun markTaken(dose: ScheduledDose) {
        viewModelScope.launch {
            val log = engine.createLog(dose, LogStatus.TAKEN)
            reminderLogRepository.logDose(log)
        }
    }

    fun markSkipped(dose: ScheduledDose) {
        viewModelScope.launch {
            val log = engine.createLog(dose, LogStatus.SKIPPED)
            reminderLogRepository.logDose(log)
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch { medicationRepository.deleteMedication(id) }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val prefs: StateFlow<UserPreferences> = preferencesRepository.getUserPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setTheme(theme: AppTheme) = update { it.copy(appTheme = theme) }
    fun setReminders(enabled: Boolean) {
        update { it.copy(remindersEnabled = enabled) }
        if (enabled) ReminderWorker.schedule(context) else ReminderWorker.cancel(context)
    }
    fun setAds(enabled: Boolean) = update { it.copy(adsEnabled = enabled) }
    fun setAnalytics(enabled: Boolean) = update { it.copy(analyticsEnabled = enabled) }

    private fun update(transform: (UserPreferences) -> UserPreferences) {
        viewModelScope.launch { preferencesRepository.updatePreferences(transform) }
    }
}

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun acceptAll(onComplete: () -> Unit) = save(ads = true, analytics = true, onComplete)
    fun acceptEssentialOnly(onComplete: () -> Unit) = save(ads = false, analytics = false, onComplete)
    fun denyAll(onComplete: () -> Unit) = save(ads = false, analytics = false, onComplete)

    private fun save(ads: Boolean, analytics: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            preferencesRepository.updatePreferences {
                it.copy(consentGiven = true, adsEnabled = ads, analyticsEnabled = analytics, personalizedAds = ads)
            }
            _loading.value = false
            onComplete()
        }
    }
}
