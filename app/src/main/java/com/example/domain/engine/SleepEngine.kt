package com.example.domain.engine

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class SleepAnalysisResult(
    val actualDurationMinutes: Int,
    val goalDurationMinutes: Int,
    val bedtimeMillis: Long?,
    val wakeMillis: Long?,
    val durationScore: Int, // 0 to 100
    val timingScore: Int,   // 0 to 100
    val overallSleepScore: Int, // 0 to 100
    val formattedBedtime: String,
    val formattedWakeTime: String,
    val isWithinBedtimeWindow: Boolean,
    val isWithinWakeWindow: Boolean
)

object SleepEngine {

    /**
     * Evaluates sleep duration and circadian timing alignment.
     *
     * @param durationMinutes Total active sleep minutes (excluding awakenings/breaks)
     * @param goalMinutes Target sleep minutes (e.g. 480 for 8h)
     * @param sessionStartMillis Actual bedtime timestamp
     * @param sessionEndMillis Actual wake-up timestamp
     * @param bedtimeWindowStartMin Minutes from start of day for bedtime window start (e.g., 22 * 60 = 1320 for 10:00 PM)
     * @param bedtimeWindowEndMin Minutes for bedtime window end (e.g., 23 * 60 + 30 = 1410 for 11:30 PM)
     * @param wakeWindowStartMin Minutes for wake window start (e.g., 5 * 60 + 30 = 330 for 5:30 AM)
     * @param wakeWindowEndMin Minutes for wake window end (e.g., 7 * 60 + 30 = 450 for 7:30 AM)
     */
    fun evaluateSleep(
        durationMinutes: Int,
        goalMinutes: Int,
        sessionStartMillis: Long?,
        sessionEndMillis: Long?,
        bedtimeWindowStartMin: Int = 22 * 60,
        bedtimeWindowEndMin: Int = 23 * 60 + 30,
        wakeWindowStartMin: Int = 5 * 60 + 30,
        wakeWindowEndMin: Int = 7 * 60 + 30
    ): SleepAnalysisResult {
        if (durationMinutes <= 0 || sessionStartMillis == null || sessionEndMillis == null) {
            return SleepAnalysisResult(
                actualDurationMinutes = durationMinutes,
                goalDurationMinutes = goalMinutes,
                bedtimeMillis = sessionStartMillis,
                wakeMillis = sessionEndMillis,
                durationScore = 0,
                timingScore = 0,
                overallSleepScore = 0,
                formattedBedtime = "--:--",
                formattedWakeTime = "--:--",
                isWithinBedtimeWindow = false,
                isWithinWakeWindow = false
            )
        }

        // 1. Duration Score calculation
        val durationRatio = durationMinutes.toDouble() / goalMinutes.coerceAtLeast(60).toDouble()
        val durationScore = when {
            durationRatio in 0.95..1.10 -> 100
            durationRatio in 0.85..0.95 -> (80 + ((durationRatio - 0.85) / 0.10) * 20).toInt()
            durationRatio in 1.10..1.25 -> (100 - ((durationRatio - 1.10) / 0.15) * 20).toInt()
            durationRatio in 0.70..0.85 -> (60 + ((durationRatio - 0.70) / 0.15) * 20).toInt()
            durationRatio < 0.70 -> (durationRatio * 85).toInt().coerceIn(10, 60)
            else -> 75
        }.coerceIn(0, 100)

        // 2. Timing Score
        val zone = ZoneId.systemDefault()
        val startZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(sessionStartMillis), zone)
        val endZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(sessionEndMillis), zone)

        val startMinOfDay = startZdt.hour * 60 + startZdt.minute
        val endMinOfDay = endZdt.hour * 60 + endZdt.minute

        // Bedtime check
        val isBedtimeOk = isMinuteInWindow(startMinOfDay, bedtimeWindowStartMin, bedtimeWindowEndMin)
        val bedtimeDeviation = getMinuteDeviation(startMinOfDay, bedtimeWindowStartMin, bedtimeWindowEndMin)
        val bedtimeScore = max(0, 100 - (bedtimeDeviation / 2))

        // Wake check
        val isWakeOk = isMinuteInWindow(endMinOfDay, wakeWindowStartMin, wakeWindowEndMin)
        val wakeDeviation = getMinuteDeviation(endMinOfDay, wakeWindowStartMin, wakeWindowEndMin)
        val wakeScore = max(0, 100 - (wakeDeviation / 2))

        val timingScore = ((bedtimeScore * 0.5) + (wakeScore * 0.5)).toInt().coerceIn(0, 100)

        // 3. Overall Sleep Score: 60% duration + 40% timing
        val overallScore = ((durationScore * 0.60) + (timingScore * 0.40)).toInt().coerceIn(0, 100)

        val formattedBedtime = String.format("%02d:%02d", startZdt.hour, startZdt.minute)
        val formattedWakeTime = String.format("%02d:%02d", endZdt.hour, endZdt.minute)

        return SleepAnalysisResult(
            actualDurationMinutes = durationMinutes,
            goalDurationMinutes = goalMinutes,
            bedtimeMillis = sessionStartMillis,
            wakeMillis = sessionEndMillis,
            durationScore = durationScore,
            timingScore = timingScore,
            overallSleepScore = overallScore,
            formattedBedtime = formattedBedtime,
            formattedWakeTime = formattedWakeTime,
            isWithinBedtimeWindow = isBedtimeOk,
            isWithinWakeWindow = isWakeOk
        )
    }

    private fun isMinuteInWindow(minute: Int, windowStart: Int, windowEnd: Int): Boolean {
        return if (windowStart <= windowEnd) {
            minute in windowStart..windowEnd
        } else {
            // crosses midnight (e.g. 22:00 to 02:00)
            minute >= windowStart || minute <= windowEnd
        }
    }

    private fun getMinuteDeviation(minute: Int, windowStart: Int, windowEnd: Int): Int {
        if (isMinuteInWindow(minute, windowStart, windowEnd)) return 0
        val distToStart = if (minute > windowStart) (minute - windowStart) else (windowStart - minute)
        val distToEnd = if (minute > windowEnd) (minute - windowEnd) else (windowEnd - minute)
        return min(distToStart, distToEnd).coerceAtMost(180)
    }
}
