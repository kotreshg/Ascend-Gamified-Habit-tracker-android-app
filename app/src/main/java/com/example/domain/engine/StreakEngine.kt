package com.example.domain.engine

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class DaySummary(
    val dateString: String, // "YYYY-MM-DD"
    val localDate: LocalDate,
    val totalActiveMinutes: Int,
    val totalGoalMinutes: Int,
    val totalXp: Int,
    val overallPercentage: Float, // 0.0 to 1.0+
    val activityMinutes: Map<String, Int>, // activityId -> minutes
    val activityPercentages: Map<String, Float>,
    val activityXp: Map<String, Int>,
    val intensityTier: Int // 0: 0%, 1: 1-49%, 2: 50-74%, 3: 75-99%, 4: 100%+
)

data class StreakStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalDaysActive: Int,
    val bestDayDate: String?,
    val bestDayMinutes: Int,
    val totalAllTimeMinutes: Int,
    val totalAllTimeXp: Int,
    val averageDailyScorePercent: Int
)

object StreakEngine {

    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * Determines intensity tier from completion percentage:
     * Tier 4: 100%+
     * Tier 3: 75-99%
     * Tier 2: 50-74%
     * Tier 1: 1-49%
     * Tier 0: 0%
     */
    fun calculateIntensityTier(percentage: Float): Int {
        val pct = percentage * 100f
        return when {
            pct >= 100f -> 4
            pct >= 75f -> 3
            pct >= 50f -> 2
            pct > 0f -> 1
            else -> 0
        }
    }

    /**
     * Calculates streak stats across chronological daily summaries.
     */
    fun calculateStreakStats(
        dailySummaries: Map<String, DaySummary>,
        todayDate: LocalDate = LocalDate.now()
    ): StreakStats {
        if (dailySummaries.isEmpty()) {
            return StreakStats(
                currentStreak = 0,
                longestStreak = 0,
                totalDaysActive = 0,
                bestDayDate = null,
                bestDayMinutes = 0,
                totalAllTimeMinutes = 0,
                totalAllTimeXp = 0,
                averageDailyScorePercent = 0
            )
        }

        var longestStreak = 0
        var currentStreak = 0
        var totalMinutes = 0
        var totalXp = 0
        var bestMinutes = 0
        var bestDate: String? = null
        var totalScoreSum = 0f
        var scoredDaysCount = 0

        // Parse and sort dates
        val activeDates = dailySummaries.values
            .filter { it.totalActiveMinutes > 0 }
            .map { it.localDate }
            .distinct()
            .sorted()

        // Calculate all-time totals & best day
        dailySummaries.values.forEach { day ->
            totalMinutes += day.totalActiveMinutes
            totalXp += day.totalXp
            if (day.totalActiveMinutes > bestMinutes) {
                bestMinutes = day.totalActiveMinutes
                bestDate = day.dateString
            }
            if (day.totalActiveMinutes > 0) {
                totalScoreSum += day.overallPercentage
                scoredDaysCount++
            }
        }

        // Calculate streaks
        var tempStreak = 0
        var prevDate: LocalDate? = null

        for (date in activeDates) {
            if (prevDate == null) {
                tempStreak = 1
            } else {
                val daysDiff = ChronoUnit.DAYS.between(prevDate, date)
                if (daysDiff == 1L) {
                    tempStreak++
                } else {
                    tempStreak = 1
                }
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
            prevDate = date
        }

        // Current streak from today or yesterday
        if (activeDates.isNotEmpty()) {
            val lastActive = activeDates.last()
            val daysFromToday = ChronoUnit.DAYS.between(lastActive, todayDate)
            if (daysFromToday <= 1L) {
                // Streak is active! Count backwards from lastActive
                var backwardStreak = 0
                var checkDate = lastActive
                val activeDateSet = activeDates.toSet()
                while (activeDateSet.contains(checkDate)) {
                    backwardStreak++
                    checkDate = checkDate.minusDays(1)
                }
                currentStreak = backwardStreak
            } else {
                currentStreak = 0
            }
        }

        val avgScore = if (scoredDaysCount > 0) {
            ((totalScoreSum / scoredDaysCount) * 100f).toInt().coerceIn(0, 100)
        } else 0

        return StreakStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak.coerceAtLeast(currentStreak),
            totalDaysActive = activeDates.size,
            bestDayDate = bestDate,
            bestDayMinutes = bestMinutes,
            totalAllTimeMinutes = totalMinutes,
            totalAllTimeXp = totalXp,
            averageDailyScorePercent = avgScore
        )
    }
}
