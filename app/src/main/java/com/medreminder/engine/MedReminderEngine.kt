package com.medreminder.engine

import com.medreminder.domain.model.LogStatus
import com.medreminder.domain.model.Medication
import com.medreminder.domain.model.ReminderLog
import com.medreminder.domain.model.ScheduledDose
import com.medreminder.domain.model.VaccineInfo
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedReminderEngine @Inject constructor() {

    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun todayDate(): String = LocalDate.now().format(dateFmt)

    fun buildTodaySchedule(
        medications: List<Medication>,
        logs: List<ReminderLog>,
        date: String = todayDate()
    ): List<ScheduledDose> {
        val logMap = logs.associateBy { "${it.medicationId}_${it.scheduledTime}" }
        return medications
            .filter { it.active && !it.isVaccine }
            .flatMap { med ->
                med.scheduleTimes.map { time ->
                    ScheduledDose(
                        medication = med,
                        scheduledTime = time,
                        date = date,
                        log = logMap["${med.id}_$time"]
                    )
                }
            }
            .sortedBy { it.scheduledTime }
    }

    fun parseScheduleTimes(input: String): List<String> =
        input.split(",", ";", "\n")
            .map { it.trim() }
            .filter { it.matches(Regex("\\d{1,2}:\\d{2}")) }
            .map { normalizeTime(it) }

    fun normalizeTime(time: String): String {
        val parts = time.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return String.format("%02d:%02d", h.coerceIn(0, 23), m.coerceIn(0, 59))
    }

    fun nextReminderDelayMinutes(scheduleTimes: List<String>): Long {
        if (scheduleTimes.isEmpty()) return 60L
        val now = LocalTime.now()
        val today = scheduleTimes.mapNotNull { runCatching { LocalTime.parse(it, timeFmt) }.getOrNull() }
            .filter { it.isAfter(now) }
            .minOrNull()
        return if (today != null) {
            java.time.Duration.between(now, today).toMinutes().coerceAtLeast(1L)
        } else 24 * 60L
    }

    fun createLog(
        dose: ScheduledDose,
        status: LogStatus,
        timestamp: Long = System.currentTimeMillis()
    ) = ReminderLog(
        medicationId = dose.medication.id,
        medicationName = dose.medication.name,
        scheduledTime = dose.scheduledTime,
        date = dose.date,
        status = status,
        timestamp = timestamp
    )

    fun childVaccines(): List<VaccineInfo> = listOf(
        VaccineInfo("BCG", "At birth", "Tuberculosis protection"),
        VaccineInfo("OPV-0", "At birth", "Oral polio dose 0"),
        VaccineInfo("Hepatitis B-1", "At birth", "First hepatitis B dose"),
        VaccineInfo("OPV-1,2,3", "6, 10, 14 weeks", "Oral polio series"),
        VaccineInfo("Pentavalent 1,2,3", "6, 10, 14 weeks", "DPT + Hib + Hep B"),
        VaccineInfo("Rotavirus", "6, 10, 14 weeks", "Rotavirus diarrhea protection"),
        VaccineInfo("PCV", "6, 14 weeks + booster", "Pneumococcal conjugate"),
        VaccineInfo("IPV", "14 weeks", "Inactivated polio"),
        VaccineInfo("MR-1", "9-12 months", "Measles-Rubella first dose"),
        VaccineInfo("MR-2", "16-24 months", "Measles-Rubella second dose"),
        VaccineInfo("DPT Booster", "16-24 months", "DPT booster dose"),
        VaccineInfo("OPV Booster", "16-24 months", "Polio booster"),
        VaccineInfo("Typhoid", "9-12 months", "Typhoid conjugate vaccine"),
        VaccineInfo("HPV", "9-14 years", "Human papillomavirus (girls)")
    )
}
