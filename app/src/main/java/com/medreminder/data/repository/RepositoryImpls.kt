package com.medreminder.data.repository

import com.medreminder.data.local.PreferencesDataStore
import com.medreminder.data.local.database.dao.MedicationDao
import com.medreminder.data.local.database.dao.ReminderLogDao
import com.medreminder.data.local.database.entity.MedicationEntity
import com.medreminder.data.local.database.entity.ReminderLogEntity
import com.medreminder.domain.model.LogStatus
import com.medreminder.domain.model.Medication
import com.medreminder.domain.model.ReminderLog
import com.medreminder.domain.model.UserPreferences
import com.medreminder.domain.repository.MedicationRepository
import com.medreminder.domain.repository.PreferencesRepository
import com.medreminder.domain.repository.ReminderLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao
) : MedicationRepository {
    override fun observeActiveMedications(): Flow<List<Medication>> =
        medicationDao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeAllMedications(): Flow<List<Medication>> =
        medicationDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getMedication(id: Long): Medication? =
        medicationDao.getById(id)?.toDomain()

    override suspend fun saveMedication(medication: Medication): Long {
        val entity = medication.toEntity()
        return if (medication.id == 0L) medicationDao.insert(entity)
        else { medicationDao.update(entity); medication.id }
    }

    override suspend fun deleteMedication(id: Long) = medicationDao.delete(id)
}

@Singleton
class ReminderLogRepositoryImpl @Inject constructor(
    private val reminderLogDao: ReminderLogDao
) : ReminderLogRepository {
    override fun observeLogsForDate(date: String): Flow<List<ReminderLog>> =
        reminderLogDao.observeByDate(date).map { list -> list.map { it.toDomain() } }

    override fun observeRecentLogs(limit: Int): Flow<List<ReminderLog>> =
        reminderLogDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun logDose(log: ReminderLog): Long =
        reminderLogDao.insert(log.toEntity())

    override suspend fun findLog(medicationId: Long, date: String, time: String): ReminderLog? =
        reminderLogDao.findForDose(medicationId, date, time)?.toDomain()
}

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: PreferencesDataStore
) : PreferencesRepository {
    override fun getUserPreferences(): Flow<UserPreferences> = dataStore.preferencesFlow
    override suspend fun updatePreferences(transform: (UserPreferences) -> UserPreferences) =
        dataStore.update(transform)
}

private fun MedicationEntity.toDomain() = Medication(
    id = id, name = name, dose = dose,
    scheduleTimes = scheduleTimes.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    isVaccine = isVaccine, notes = notes, active = active
)

private fun Medication.toEntity() = MedicationEntity(
    id = id, name = name, dose = dose,
    scheduleTimes = scheduleTimes.joinToString(","),
    isVaccine = isVaccine, notes = notes, active = active
)

private fun ReminderLogEntity.toDomain() = ReminderLog(
    id = id, medicationId = medicationId, medicationName = medicationName,
    scheduledTime = scheduledTime, date = date,
    status = runCatching { LogStatus.valueOf(status) }.getOrDefault(LogStatus.SKIPPED),
    timestamp = timestamp
)

private fun ReminderLog.toEntity() = ReminderLogEntity(
    id = id, medicationId = medicationId, medicationName = medicationName,
    scheduledTime = scheduledTime, date = date, status = status.name, timestamp = timestamp
)
