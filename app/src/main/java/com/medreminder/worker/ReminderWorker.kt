package com.medreminder.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.medreminder.data.local.PreferencesDataStore
import com.medreminder.domain.repository.MedicationRepository
import com.medreminder.engine.MedReminderEngine
import com.medreminder.notification.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val medicationRepository: MedicationRepository,
    private val engine: MedReminderEngine,
    private val notificationHelper: NotificationHelper,
    private val preferencesDataStore: PreferencesDataStore
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = preferencesDataStore.preferencesFlow.first()
        if (!prefs.remindersEnabled) return Result.success()

        val meds = medicationRepository.observeActiveMedications().first()
        val today = engine.todayDate()
        val schedule = engine.buildTodaySchedule(meds, emptyList(), today)
        val pending = schedule.filter { it.isPending }
        if (pending.isNotEmpty()) {
            val next = pending.first()
            notificationHelper.showReminder(
                "Medicine Reminder",
                "Time for ${next.medication.name} (${next.medication.dose}) at ${next.scheduledTime}"
            )
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "med_reminder_periodic"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
