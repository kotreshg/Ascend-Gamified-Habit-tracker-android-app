package com.example.data.repository

import com.example.data.dao.ActivityDao
import com.example.data.dao.BreakDao
import com.example.data.dao.SessionDao
import com.example.data.dao.SettingsDao
import com.example.data.db.AscendDatabase
import com.example.data.model.ActivityEntity
import com.example.data.model.BreakEntity
import com.example.data.model.SessionEntity
import com.example.data.model.SessionStatus
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class AscendRepository(
    private val activityDao: ActivityDao,
    private val sessionDao: SessionDao,
    private val breakDao: BreakDao,
    private val settingsDao: SettingsDao
) {
    val activitiesFlow: Flow<List<ActivityEntity>> = activityDao.getAllActivitiesFlow()
    val activeSessionFlow: Flow<SessionEntity?> = sessionDao.getActiveSessionFlow()
    val allSessionsFlow: Flow<List<SessionEntity>> = sessionDao.getAllSessionsFlow()
    val allBreaksFlow: Flow<List<BreakEntity>> = breakDao.getAllBreaksFlow()
    val settingsFlow: Flow<UserSettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun getActivity(id: String): ActivityEntity? = activityDao.getActivityById(id)

    suspend fun getSession(id: Long): SessionEntity? = sessionDao.getSessionById(id)

    suspend fun getBreaksForSession(sessionId: Long): List<BreakEntity> =
        breakDao.getBreaksForSession(sessionId)

    fun getBreaksForSessionFlow(sessionId: Long): Flow<List<BreakEntity>> =
        breakDao.getBreaksForSessionFlow(sessionId)

    suspend fun startSession(activityId: String): Long {
        val now = System.currentTimeMillis()
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Close any lingering active session first
        val active = sessionDao.getActiveSession()
        if (active != null) {
            if (active.activityId == activityId) {
                // If it's already active and on break, resume it
                if (active.status == SessionStatus.ON_BREAK) {
                    breakOut(active.id)
                }
                return active.id
            } else {
                // Punch out previous active session
                punchOut(active.id)
            }
        }

        val session = SessionEntity(
            activityId = activityId,
            startTime = now,
            endTime = null,
            status = SessionStatus.ACTIVE,
            dateString = todayStr,
            isManualCorrection = false
        )
        return sessionDao.insertSession(session)
    }

    suspend fun breakIn(sessionId: Long) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        if (session.status != SessionStatus.ACTIVE) return

        val now = System.currentTimeMillis()
        val breakEntity = BreakEntity(
            sessionId = sessionId,
            startTime = now,
            endTime = null
        )
        breakDao.insertBreak(breakEntity)

        sessionDao.updateSession(session.copy(status = SessionStatus.ON_BREAK))
    }

    suspend fun breakOut(sessionId: Long) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val activeBreak = breakDao.getActiveBreakForSession(sessionId)
        val now = System.currentTimeMillis()

        if (activeBreak != null) {
            breakDao.updateBreak(activeBreak.copy(endTime = now))
        }

        sessionDao.updateSession(session.copy(status = SessionStatus.ACTIVE))
    }

    suspend fun punchOut(sessionId: Long) {
        val session = sessionDao.getSessionById(sessionId) ?: return
        val now = System.currentTimeMillis()

        // Close any open break
        val activeBreak = breakDao.getActiveBreakForSession(sessionId)
        if (activeBreak != null) {
            breakDao.updateBreak(activeBreak.copy(endTime = now))
        }

        sessionDao.updateSession(
            session.copy(
                endTime = now,
                status = SessionStatus.COMPLETED
            )
        )
    }

    suspend fun addManualSession(
        activityId: String,
        dateString: String,
        startTime: Long,
        endTime: Long,
        breakMinutes: Int = 0,
        note: String = ""
    ): Long {
        val session = SessionEntity(
            activityId = activityId,
            startTime = startTime,
            endTime = endTime,
            status = SessionStatus.COMPLETED,
            dateString = dateString,
            isManualCorrection = true,
            note = note
        )
        val sessionId = sessionDao.insertSession(session)

        if (breakMinutes > 0 && endTime > startTime + (breakMinutes * 60 * 1000L)) {
            val midTime = (startTime + endTime) / 2
            val breakDurationMs = breakMinutes * 60 * 1000L
            val breakStart = midTime - (breakDurationMs / 2)
            val breakEnd = midTime + (breakDurationMs / 2)
            breakDao.insertBreak(
                BreakEntity(
                    sessionId = sessionId,
                    startTime = breakStart,
                    endTime = breakEnd
                )
            )
        }

        return sessionId
    }

    suspend fun updateSession(session: SessionEntity) {
        sessionDao.updateSession(session)
    }

    suspend fun deleteSession(sessionId: Long) {
        breakDao.deleteBreaksForSession(sessionId)
        sessionDao.deleteSessionById(sessionId)
    }

    suspend fun updateActivityGoal(activityId: String, goalMinutes: Int) {
        val activity = activityDao.getActivityById(activityId) ?: return
        activityDao.updateActivity(activity.copy(dailyGoalMinutes = goalMinutes))
    }

    suspend fun createCustomActivity(
        name: String,
        description: String,
        dailyGoalMinutes: Int,
        colorHex: String,
        iconName: String,
        contributesToMainSystem: Boolean
    ) {
        val id = "custom_${UUID.randomUUID().toString().take(8)}"
        val activity = ActivityEntity(
            id = id,
            name = name,
            description = description,
            dailyGoalMinutes = dailyGoalMinutes,
            isCore = false,
            contributesToMainSystem = contributesToMainSystem,
            colorHex = colorHex,
            iconName = iconName,
            sortOrder = 10
        )
        activityDao.insertActivity(activity)
    }

    suspend fun archiveActivity(activityId: String) {
        activityDao.archiveActivity(activityId)
    }

    suspend fun updateUserSettings(settings: UserSettingsEntity) {
        settingsDao.updateSettings(settings)
    }

    suspend fun seedDemoData() {
        // Seed 30 days of realistic, inspiring self-mastery records
        sessionDao.deleteAllSessions()
        breakDao.deleteAllBreaks()

        val today = LocalDate.now()
        val zone = java.time.ZoneId.systemDefault()

        for (daysAgo in 30 downTo 1) {
            val date = today.minusDays(daysAgo.toLong())
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

            // 1. Sleep: 22:45 to 06:45 next morning (dated on previous day's evening)
            val sleepStart = date.minusDays(1).atTime(22, 45).atZone(zone).toInstant().toEpochMilli()
            val sleepEnd = date.atTime(6, 45).atZone(zone).toInstant().toEpochMilli()
            val sleepSessionId = sessionDao.insertSession(
                SessionEntity(
                    activityId = "sleep",
                    startTime = sleepStart,
                    endTime = sleepEnd,
                    status = SessionStatus.COMPLETED,
                    dateString = dateStr,
                    isManualCorrection = false
                )
            )
            // Brief awakening
            val awakeStart = date.atTime(2, 30).atZone(zone).toInstant().toEpochMilli()
            val awakeEnd = date.atTime(2, 45).atZone(zone).toInstant().toEpochMilli()
            breakDao.insertBreak(
                BreakEntity(sessionId = sleepSessionId, startTime = awakeStart, endTime = awakeEnd)
            )

            // 2. Morning Meditation: 07:00 to 08:30 (1h 30m)
            val medStart = date.atTime(7, 0).atZone(zone).toInstant().toEpochMilli()
            val medEnd = date.atTime(8, 30).atZone(zone).toInstant().toEpochMilli()
            sessionDao.insertSession(
                SessionEntity(
                    activityId = "meditation",
                    startTime = medStart,
                    endTime = medEnd,
                    status = SessionStatus.COMPLETED,
                    dateString = dateStr
                )
            )

            // 3. Deep Study: 09:30 to 12:30 (3h with 15m break)
            val studyStart = date.atTime(9, 30).atZone(zone).toInstant().toEpochMilli()
            val studyEnd = date.atTime(12, 30).atZone(zone).toInstant().toEpochMilli()
            val studySessionId = sessionDao.insertSession(
                SessionEntity(
                    activityId = "study",
                    startTime = studyStart,
                    endTime = studyEnd,
                    status = SessionStatus.COMPLETED,
                    dateString = dateStr
                )
            )
            val studyBreakStart = date.atTime(10, 45).atZone(zone).toInstant().toEpochMilli()
            val studyBreakEnd = date.atTime(11, 0).atZone(zone).toInstant().toEpochMilli()
            breakDao.insertBreak(
                BreakEntity(sessionId = studySessionId, startTime = studyBreakStart, endTime = studyBreakEnd)
            )

            // 4. Workout in the evening (skip every 4th day for rest)
            if (daysAgo % 4 != 0) {
                val workoutStart = date.atTime(17, 30).atZone(zone).toInstant().toEpochMilli()
                val workoutEnd = date.atTime(18, 35).atZone(zone).toInstant().toEpochMilli()
                sessionDao.insertSession(
                    SessionEntity(
                        activityId = "workout",
                        startTime = workoutStart,
                        endTime = workoutEnd,
                        status = SessionStatus.COMPLETED,
                        dateString = dateStr
                    )
                )
            }
        }
    }

    suspend fun resetAllData() {
        sessionDao.deleteAllSessions()
        breakDao.deleteAllBreaks()
    }
}
