package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey
    val id: String, // "meditation", "study", "workout", "sleep", or UUID for custom
    val name: String,
    val description: String = "",
    val dailyGoalMinutes: Int, // e.g. 120 for 2h
    val isCore: Boolean = false,
    val contributesToMainSystem: Boolean = true,
    val colorHex: String,
    val iconName: String,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
)

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
