package com.medreminder.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.medreminder.data.local.database.dao.MedicationDao
import com.medreminder.data.local.database.dao.ReminderLogDao
import com.medreminder.data.local.database.entity.MedicationEntity
import com.medreminder.data.local.database.entity.ReminderLogEntity

@Database(
    entities = [MedicationEntity::class, ReminderLogEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MedReminderDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun reminderLogDao(): ReminderLogDao
}
