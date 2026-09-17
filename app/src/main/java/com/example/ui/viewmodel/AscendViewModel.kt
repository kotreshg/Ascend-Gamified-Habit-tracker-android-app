package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AscendDatabase
import com.example.data.model.ActivityEntity
import com.example.data.model.BreakEntity
import com.example.data.model.SessionEntity
import com.example.data.model.SessionStatus
import com.example.data.model.UserSettingsEntity
import com.example.data.repository.AscendRepository
import com.example.domain.engine.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

data class LevelUpCelebration(
    val activity: ActivityEntity,
    val levelInfo: LevelInfo
)

data class AscendUiState(
    val activities: List<ActivityEntity> = emptyList(),
    val activeSession: SessionEntity? = null,
    val activeActivity: ActivityEntity? = null,
    val activeSessionBreaks: List<BreakEntity> = emptyList(),
    val currentSessionElapsedSeconds: Long = 0L,
    val currentBreakElapsedSeconds: Long = 0L,
    val userSettings: UserSettingsEntity = UserSettingsEntity(),
    val dailySummaries: Map<String, DaySummary> = emptyMap(),
    val todaySummary: DaySummary? = null,
    val progressionStates: Map<String, ProgressionState> = emptyMap(),
    val streakStats: StreakStats = StreakStats(0, 0, 0, null, 0, 0, 0, 0),
    val todaySleepAnalysis: SleepAnalysisResult? = null,
    val dailyQuote: Quote = MotivationProvider.getDailyQuote(),
    val levelUpCelebration: LevelUpCelebration? = null,
    val isInitialized: Boolean = false
)

class AscendViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AscendRepository

    private val _uiState = MutableStateFlow(AscendUiState())
    val uiState: StateFlow<AscendUiState> = _uiState.asStateFlow()

    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())

    init {
        val database = AscendDatabase.getDatabase(application, viewModelScope)
        repository = AscendRepository(
            activityDao = database.activityDao(),
            sessionDao = database.sessionDao(),
            breakDao = database.breakDao(),
            settingsDao = database.settingsDao()
        )

        // Seed initial core data if needed
        viewModelScope.launch(Dispatchers.IO) {
            AscendDatabase.populateInitialData(database)
        }

        // Ticker loop for live second updates (used only for real-time display calculation from actual timestamps)
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(1000)
            }
        }

        // Combine DB flows into rich reactive state
        viewModelScope.launch {
            combine(
                repository.activitiesFlow,
                repository.activeSessionFlow,
                repository.allSessionsFlow,
                repository.allBreaksFlow,
                repository.settingsFlow
            ) { activities, activeSession, allSessions, allBreaks, settings ->
                DbSnapshot(
                    activities = activities,
                    activeSession = activeSession,
                    allSessions = allSessions,
                    allBreaks = allBreaks,
                    settings = settings ?: UserSettingsEntity()
                )
            }.combine(_currentTimeMillis) { snapshot, nowMillis ->
                calculateState(
                    activities = snapshot.activities,
                    activeSession = snapshot.activeSession,
                    allSessions = snapshot.allSessions,
                    allBreaks = snapshot.allBreaks,
                    settings = snapshot.settings,
                    nowMillis = nowMillis
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private data class DbSnapshot(
        val activities: List<ActivityEntity>,
        val activeSession: SessionEntity?,
        val allSessions: List<SessionEntity>,
        val allBreaks: List<BreakEntity>,
        val settings: UserSettingsEntity
    )

    private fun calculateState(
        activities: List<ActivityEntity>,
        activeSession: SessionEntity?,
        allSessions: List<SessionEntity>,
        allBreaks: List<BreakEntity>,
        settings: UserSettingsEntity,
        nowMillis: Long
    ): AscendUiState {
        val activityMap = activities.associateBy { it.id }
        val breaksBySessionId = allBreaks.groupBy { it.sessionId }

        val activeActivity = activeSession?.let { activityMap[it.activityId] }
        val activeBreaks = activeSession?.let { breaksBySessionId[it.id] ?: emptyList() } ?: emptyList()

        // 1. Calculate live timer seconds from actual timestamps
        var sessionElapsedSec = 0L
        var currentBreakElapsedSec = 0L

        if (activeSession != null) {
            val sessionStart = activeSession.startTime
            var totalBreakMs = 0L

            activeBreaks.forEach { b ->
                val bStart = b.startTime
                val bEnd = b.endTime ?: nowMillis
                totalBreakMs += (bEnd - bStart).coerceAtLeast(0L)
            }

            val totalElapsedMs = (nowMillis - sessionStart).coerceAtLeast(0L)
            val netActiveMs = (totalElapsedMs - totalBreakMs).coerceAtLeast(0L)
            sessionElapsedSec = netActiveMs / 1000L

            if (activeSession.status == SessionStatus.ON_BREAK) {
                val openBreak = activeBreaks.find { it.endTime == null }
                if (openBreak != null) {
                    currentBreakElapsedSec = (nowMillis - openBreak.startTime).coerceAtLeast(0L) / 1000L
                }
            }
        }

        // 2. Build Daily Summaries
        val sessionsByDate = allSessions.groupBy { it.dateString }
        val allDates = (sessionsByDate.keys + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)).distinct()

        val dailySummaries = mutableMapOf<String, DaySummary>()

        allDates.forEach { dateStr ->
            val dateSessions = sessionsByDate[dateStr] ?: emptyList()
            val parsedDate = try {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                LocalDate.now()
            }

            val actMinutes = mutableMapOf<String, Int>()
            val actXp = mutableMapOf<String, Int>()
            val actPct = mutableMapOf<String, Float>()

            activities.forEach { act ->
                actMinutes[act.id] = 0
                actXp[act.id] = 0
                actPct[act.id] = 0f
            }

            dateSessions.forEach { sess ->
                val act = activityMap[sess.activityId]
                if (act != null) {
                    val sStart = sess.startTime
                    val sEnd = sess.endTime ?: nowMillis
                    val sBreaks = breaksBySessionId[sess.id] ?: emptyList()
                    var breakMs = 0L
                    sBreaks.forEach { b ->
                        val bStart = b.startTime
                        val bEnd = b.endTime ?: nowMillis
                        breakMs += (bEnd - bStart).coerceAtLeast(0L)
                    }
                    val activeMs = ((sEnd - sStart) - breakMs).coerceAtLeast(0L)
                    val minutes = (activeMs / (1000 * 60)).toInt()

                    val currentMin = actMinutes[act.id] ?: 0
                    actMinutes[act.id] = currentMin + minutes
                }
            }

            var totalDayActiveMin = 0
            var totalDayGoalMin = 0
            var totalDayXp = 0

            activities.filter { it.contributesToMainSystem }.forEach { act ->
                val min = actMinutes[act.id] ?: 0
                val goal = act.dailyGoalMinutes
                val xp = ProgressionEngine.calculateDailyXp(min, goal)
                val pct = if (goal > 0) (min.toFloat() / goal.toFloat()) else 0f

                actXp[act.id] = xp
                actPct[act.id] = pct

                totalDayActiveMin += min
                totalDayGoalMin += goal
                totalDayXp += xp
            }

            val overallDayPct = if (totalDayGoalMin > 0) {
                (totalDayActiveMin.toFloat() / totalDayGoalMin.toFloat())
            } else 0f

            val tier = StreakEngine.calculateIntensityTier(overallDayPct)

            dailySummaries[dateStr] = DaySummary(
                dateString = dateStr,
                localDate = parsedDate,
                totalActiveMinutes = totalDayActiveMin,
                totalGoalMinutes = totalDayGoalMin,
                totalXp = totalDayXp,
                overallPercentage = overallDayPct,
                activityMinutes = actMinutes,
                activityPercentages = actPct,
                activityXp = actXp,
                intensityTier = tier
            )
        }

        val todayDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todaySummary = dailySummaries[todayDateStr]

        // 3. Calculate Progression per Activity
        val progressionStates = mutableMapOf<String, ProgressionState>()
        activities.forEach { act ->
            var totalActXp = 0
            dailySummaries.values.forEach { day ->
                totalActXp += day.activityXp[act.id] ?: 0
            }
            progressionStates[act.id] = ProgressionEngine.calculateProgression(act.id, totalActXp)
        }

        // 4. Calculate Streaks & Summary Stats
        val streakStats = StreakEngine.calculateStreakStats(dailySummaries, LocalDate.now())

        // 5. Evaluate Sleep Analysis for Today/Recent Night
        val sleepSessions = allSessions.filter { it.activityId == "sleep" }.sortedByDescending { it.startTime }
        val recentSleep = sleepSessions.firstOrNull()
        val sleepGoalMin = activityMap["sleep"]?.dailyGoalMinutes ?: 480

        val sleepAnalysis = if (recentSleep != null) {
            val sBreaks = breaksBySessionId[recentSleep.id] ?: emptyList()
            var sBreakMs = 0L
            sBreaks.forEach { b ->
                val bStart = b.startTime
                val bEnd = b.endTime ?: nowMillis
                sBreakMs += (bEnd - bStart).coerceAtLeast(0L)
            }
            val sEnd = recentSleep.endTime ?: nowMillis
            val activeSleepMin = (((sEnd - recentSleep.startTime) - sBreakMs) / (1000 * 60)).toInt()

            SleepEngine.evaluateSleep(
                durationMinutes = activeSleepMin,
                goalMinutes = sleepGoalMin,
                sessionStartMillis = recentSleep.startTime,
                sessionEndMillis = recentSleep.endTime ?: nowMillis,
                bedtimeWindowStartMin = settings.bedtimeWindowStartMinutes,
                bedtimeWindowEndMin = settings.bedtimeWindowEndMinutes,
                wakeWindowStartMin = settings.wakeWindowStartMinutes,
                wakeWindowEndMin = settings.wakeWindowEndMinutes
            )
        } else null

        // Check for newly unlocked level
        var levelUpToCelebrate: LevelUpCelebration? = _uiState.value.levelUpCelebration
        if (levelUpToCelebrate == null) {
            activities.forEach { act ->
                val prog = progressionStates[act.id]
                if (prog != null && prog.currentLevel > 0) {
                    val lastKnown = when (act.id) {
                        "meditation" -> settings.lastKnownLevelMeditation
                        "study" -> settings.lastKnownLevelStudy
                        "workout" -> settings.lastKnownLevelWorkout
                        "sleep" -> settings.lastKnownLevelSleep
                        else -> 1
                    }
                    if (prog.currentLevel > lastKnown) {
                        val levelInfo = LevelInfo(
                            level = prog.currentLevel,
                            title = prog.currentTitle,
                            xpThreshold = prog.prevLevelXp,
                            description = ProgressionEngine.getLevelTitle(act.id, prog.currentLevel).second,
                            activityId = act.id
                        )
                        levelUpToCelebrate = LevelUpCelebration(activity = act, levelInfo = levelInfo)
                    }
                }
            }
        }

        return AscendUiState(
            activities = activities,
            activeSession = activeSession,
            activeActivity = activeActivity,
            activeSessionBreaks = activeBreaks,
            currentSessionElapsedSeconds = sessionElapsedSec,
            currentBreakElapsedSeconds = currentBreakElapsedSec,
            userSettings = settings,
            dailySummaries = dailySummaries,
            todaySummary = todaySummary,
            progressionStates = progressionStates,
            streakStats = streakStats,
            todaySleepAnalysis = sleepAnalysis,
            dailyQuote = MotivationProvider.getDailyQuote(),
            levelUpCelebration = levelUpToCelebrate,
            isInitialized = true
        )
    }

    fun startActivity(activityId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.startSession(activityId)
        }
    }

    fun breakIn() {
        val active = _uiState.value.activeSession ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.breakIn(active.id)
        }
    }

    fun breakOut() {
        val active = _uiState.value.activeSession ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.breakOut(active.id)
        }
    }

    fun punchOut() {
        val active = _uiState.value.activeSession ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repository.punchOut(active.id)
        }
    }

    fun updateDailyGoal(activityId: String, goalMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateActivityGoal(activityId, goalMinutes)
        }
    }

    fun updateSleepSchedule(
        bedtimeStart: Int,
        bedtimeEnd: Int,
        wakeStart: Int,
        wakeEnd: Int
    ) {
        val currentSettings = _uiState.value.userSettings
        val updated = currentSettings.copy(
            bedtimeWindowStartMinutes = bedtimeStart,
            bedtimeWindowEndMinutes = bedtimeEnd,
            wakeWindowStartMinutes = wakeStart,
            wakeWindowEndMinutes = wakeEnd
        )
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserSettings(updated)
        }
    }

    fun createCustomActivity(
        name: String,
        description: String,
        dailyGoalMinutes: Int,
        colorHex: String,
        iconName: String,
        contributesToMainSystem: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.createCustomActivity(
                name = name,
                description = description,
                dailyGoalMinutes = dailyGoalMinutes,
                colorHex = colorHex,
                iconName = iconName,
                contributesToMainSystem = contributesToMainSystem
            )
        }
    }

    fun addManualCorrectionSession(
        activityId: String,
        dateString: String,
        startTime: Long,
        endTime: Long,
        breakMinutes: Int,
        note: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addManualSession(
                activityId = activityId,
                dateString = dateString,
                startTime = startTime,
                endTime = endTime,
                breakMinutes = breakMinutes,
                note = note
            )
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSession(sessionId)
        }
    }

    fun updateUserProfileName(name: String) {
        val current = _uiState.value.userSettings
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserSettings(current.copy(userName = name))
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        val current = _uiState.value.userSettings
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserSettings(current.copy(isDarkMode = isDark))
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        val current = _uiState.value.userSettings
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserSettings(current.copy(notificationsEnabled = enabled))
        }
    }

    fun dismissLevelUpCelebration() {
        val celebration = _uiState.value.levelUpCelebration ?: return
        val currentSettings = _uiState.value.userSettings
        val updatedSettings = when (celebration.activity.id) {
            "meditation" -> currentSettings.copy(lastKnownLevelMeditation = celebration.levelInfo.level)
            "study" -> currentSettings.copy(lastKnownLevelStudy = celebration.levelInfo.level)
            "workout" -> currentSettings.copy(lastKnownLevelWorkout = celebration.levelInfo.level)
            "sleep" -> currentSettings.copy(lastKnownLevelSleep = celebration.levelInfo.level)
            else -> currentSettings
        }
        _uiState.update { it.copy(levelUpCelebration = null) }
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUserSettings(updatedSettings)
        }
    }

    fun seedDemoData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.seedDemoData()
        }
    }

    fun resetAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetAllData()
        }
    }
}
