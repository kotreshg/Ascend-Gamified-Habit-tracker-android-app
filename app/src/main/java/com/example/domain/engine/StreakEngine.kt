package com.example.domain.engine

import com.example.data.model.ActivityEntity
import com.example.data.model.HabitCompletionEntity
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

data class HabitStreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletions: Int,
    val isCleanToday: Boolean,
    val lastRelapseDate: String? = null
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

        val activeDates = dailySummaries.values
            .filter { it.totalActiveMinutes > 0 }
            .map { it.localDate }
            .distinct()
            .sorted()

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

        if (activeDates.isNotEmpty()) {
            val lastActive = activeDates.last()
            val daysFromToday = ChronoUnit.DAYS.between(lastActive, todayDate)
            if (daysFromToday <= 1L) {
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

    /**
     * Calculates streak for a specific activity, respecting its scheduled days of the week.
     * An activity is NOT considered missed on days when it wasn't scheduled!
     */
    fun calculateActivityScheduledStreak(
        activity: ActivityEntity,
        completedDates: Set<LocalDate>,
        today: LocalDate = LocalDate.now()
    ): Int {
        var streak = 0
        var checkDate = today

        // If today is scheduled and not completed yet, check if yesterday was completed
        if (activity.isScheduledOn(checkDate) && !completedDates.contains(checkDate)) {
            checkDate = checkDate.minusDays(1)
        }

        while (true) {
            if (activity.isScheduledOn(checkDate)) {
                if (completedDates.contains(checkDate)) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    // Missed a scheduled day -> streak stops here
                    break
                }
            } else {
                // Not scheduled on this day, skip back without breaking streak!
                checkDate = checkDate.minusDays(1)
                // Safety cutoff (1 year max lookback)
                if (ChronoUnit.DAYS.between(checkDate, today) > 365) break
            }
        }
        return streak
    }

    /**
     * Calculates stats for a Habit Build activity.
     */
    fun calculateHabitBuildStats(
        activity: ActivityEntity,
        completions: List<HabitCompletionEntity>,
        today: LocalDate = LocalDate.now()
    ): HabitStreakInfo {
        val completedDateSet = completions
            .filter { it.isSuccess }
            .mapNotNull {
                try {
                    LocalDate.parse(it.dateString, DATE_FORMATTER)
                } catch (e: Exception) {
                    null
                }
            }
            .toSet()

        val currentStreak = calculateActivityScheduledStreak(activity, completedDateSet, today)

        // Calculate longest streak historically
        val sortedDates = completedDateSet.sorted()
        var longest = 0
        for (date in sortedDates) {
            val strk = calculateActivityScheduledStreak(activity, completedDateSet, date)
            if (strk > longest) longest = strk
        }

        val todayCompleted = completedDateSet.contains(today)

        return HabitStreakInfo(
            currentStreak = currentStreak,
            longestStreak = longest.coerceAtLeast(currentStreak),
            totalCompletions = completedDateSet.size,
            isCleanToday = todayCompleted
        )
    }

    /**
     * Calculates stats for a Habit Quit activity.
     * Abstinence / clean streak:
     * - Days marked as clean (isSuccess = true) or continuous days since the last recorded relapse (isSuccess = false).
     */
    fun calculateHabitQuitStats(
        activity: ActivityEntity,
        completions: List<HabitCompletionEntity>,
        today: LocalDate = LocalDate.now()
    ): HabitStreakInfo {
        val successDateSet = completions
            .filter { it.isSuccess }
            .mapNotNull {
                try {
                    LocalDate.parse(it.dateString, DATE_FORMATTER)
                } catch (e: Exception) {
                    null
                }
            }
            .toSet()

        val relapseDates = completions
            .filter { !it.isSuccess }
            .mapNotNull {
                try {
                    LocalDate.parse(it.dateString, DATE_FORMATTER)
                } catch (e: Exception) {
                    null
                }
            }
            .sortedDescending()

        val lastRelapse = relapseDates.firstOrNull()

        // Current clean streak:
        val currentStreak = if (lastRelapse != null) {
            val daysSince = ChronoUnit.DAYS.between(lastRelapse, today).toInt()
            if (daysSince >= 0) daysSince else 0
        } else {
            // Count continuous clean days back
            calculateActivityScheduledStreak(activity, successDateSet, today)
        }

        // Longest clean streak
        val longestStreak = completions.maxOfOrNull { it.completedAt }?.let {
            currentStreak.coerceAtLeast(successDateSet.size)
        } ?: currentStreak

        val todayStatus = successDateSet.contains(today)

        return HabitStreakInfo(
            currentStreak = currentStreak,
            longestStreak = longestStreak.coerceAtLeast(currentStreak),
            totalCompletions = successDateSet.size,
            isCleanToday = todayStatus,
            lastRelapseDate = lastRelapse?.format(DATE_FORMATTER)
        )
    }
}
