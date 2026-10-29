package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate

enum class ActivityType {
    TIME_BASED,
    HABIT_BUILD,
    HABIT_QUIT
}

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey
    val id: String, // "meditation", "study", "workout", "sleep", or UUID for custom
    val name: String,
    val description: String = "",
    val dailyGoalMinutes: Int = 60, // e.g. 120 for 2h (for time-based)
    val activityType: ActivityType = ActivityType.TIME_BASED,
    val scheduledDays: String = "1,2,3,4,5,6,7", // 1=Mon, 2=Tue, ..., 7=Sun
    val isCore: Boolean = false,
    val contributesToMainSystem: Boolean = true,
    val colorHex: String,
    val iconName: String,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
) {
    fun isScheduledOn(date: LocalDate): Boolean {
        val dayVal = date.dayOfWeek.value
        val scheduledList = scheduledDays.split(",").mapNotNull { it.trim().toIntOrNull() }
        return scheduledList.isEmpty() || scheduledList.contains(dayVal)
    }

    fun getScheduledDayOfWeeks(): Set<DayOfWeek> {
        return scheduledDays.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .mapNotNull {
                try {
                    DayOfWeek.of(it)
                } catch (e: Exception) {
                    null
                }
            }
            .toSet()
    }

    fun scheduledDaysSet(): Set<Int> {
        return scheduledDays.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }

    fun getFormattedScheduledDays(): String {
        val days = getScheduledDayOfWeeks()
        if (days.size == 7) return "Everyday"
        if (days.size == 5 && !days.contains(DayOfWeek.SATURDAY) && !days.contains(DayOfWeek.SUNDAY)) return "Weekdays"
        if (days.size == 2 && days.contains(DayOfWeek.SATURDAY) && days.contains(DayOfWeek.SUNDAY)) return "Weekends"
        val dayAbbrs = mapOf(
            DayOfWeek.MONDAY to "Mon",
            DayOfWeek.TUESDAY to "Tue",
            DayOfWeek.WEDNESDAY to "Wed",
            DayOfWeek.THURSDAY to "Thu",
            DayOfWeek.FRIDAY to "Fri",
            DayOfWeek.SATURDAY to "Sat",
            DayOfWeek.SUNDAY to "Sun"
        )
        return days.sortedBy { it.value }.joinToString(", ") { dayAbbrs[it] ?: "" }
    }
}

enum class SessionStatus {
    ACTIVE,
    ON_BREAK,
    COMPLETED
}

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val activityId: String,
    val startTime: Long, // Epoch millis
    val endTime: Long? = null, // Null if active
    val status: SessionStatus = SessionStatus.ACTIVE,
    val dateString: String, // "YYYY-MM-DD" in local time
    val isManualCorrection: Boolean = false,
    val note: String = ""
)

@Entity(tableName = "breaks")
data class BreakEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val startTime: Long, // Epoch millis
    val endTime: Long? = null // Null if ongoing
)

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["activityId", "dateString"]
)
data class HabitCompletionEntity(
    val activityId: String,
    val dateString: String, // "YYYY-MM-DD"
    val completedAt: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true, // for Habit Quit: true = clean/avoided, false = relapsed
    val note: String = ""
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Kotresh",
    val isDarkMode: Boolean = true,
    val bedtimeWindowStartMinutes: Int = 22 * 60, // 10:00 PM (1320 min)
    val bedtimeWindowEndMinutes: Int = 23 * 60 + 30, // 11:30 PM (1410 min)
    val wakeWindowStartMinutes: Int = 5 * 60 + 30, // 5:30 AM (330 min)
    val wakeWindowEndMinutes: Int = 7 * 60 + 30, // 7:30 AM (450 min)
    val notificationsEnabled: Boolean = true,
    val lastKnownLevelMeditation: Int = 1,
    val lastKnownLevelStudy: Int = 1,
    val lastKnownLevelWorkout: Int = 1,
    val lastKnownLevelSleep: Int = 1
)
