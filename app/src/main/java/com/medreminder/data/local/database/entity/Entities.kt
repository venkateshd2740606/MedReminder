package com.medreminder.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dose: String,
    val scheduleTimes: String,
    val isVaccine: Boolean,
    val notes: String,
    val active: Boolean
)

@Entity(tableName = "reminder_logs")
data class ReminderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicationId: Long,
    val medicationName: String,
    val scheduledTime: String,
    val date: String,
    val status: String,
    val timestamp: Long
)
