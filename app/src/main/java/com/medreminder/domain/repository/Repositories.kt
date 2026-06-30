package com.medreminder.domain.repository

import com.medreminder.domain.model.Medication
import com.medreminder.domain.model.ReminderLog
import com.medreminder.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeActiveMedications(): Flow<List<Medication>>
    fun observeAllMedications(): Flow<List<Medication>>
    suspend fun getMedication(id: Long): Medication?
    suspend fun saveMedication(medication: Medication): Long
    suspend fun deleteMedication(id: Long)
}

interface ReminderLogRepository {
    fun observeLogsForDate(date: String): Flow<List<ReminderLog>>
    fun observeRecentLogs(limit: Int = 100): Flow<List<ReminderLog>>
    suspend fun logDose(log: ReminderLog): Long
    suspend fun findLog(medicationId: Long, date: String, time: String): ReminderLog?
}

interface PreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences)
}
