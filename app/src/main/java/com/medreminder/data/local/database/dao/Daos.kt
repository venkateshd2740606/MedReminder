package com.medreminder.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.medreminder.data.local.database.entity.MedicationEntity
import com.medreminder.data.local.database.entity.ReminderLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE active = 1 ORDER BY name ASC")
    fun observeActive(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun observeAll(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MedicationEntity): Long

    @Update
    suspend fun update(entity: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ReminderLogDao {
    @Query("SELECT * FROM reminder_logs WHERE date = :date ORDER BY timestamp DESC")
    fun observeByDate(date: String): Flow<List<ReminderLogEntity>>

    @Query("SELECT * FROM reminder_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<ReminderLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReminderLogEntity): Long

    @Query("SELECT * FROM reminder_logs WHERE medicationId = :medicationId AND date = :date AND scheduledTime = :time LIMIT 1")
    suspend fun findForDose(medicationId: Long, date: String, time: String): ReminderLogEntity?
}
