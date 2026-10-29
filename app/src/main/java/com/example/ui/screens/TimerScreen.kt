package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.domain.engine.HabitStreakInfo
import com.example.ui.components.PrestigeEmblem
import com.example.ui.theme.*
import com.example.ui.viewmodel.AscendUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    activityId: String,
    uiState: AscendUiState,
    onBack: () -> Unit,
    onStartActivity: (String) -> Unit,
    onBreakIn: () -> Unit,
    onBreakOut: () -> Unit,
    onPunchOut: () -> Unit,
    onToggleHabit: (String, String) -> Unit = { _, _ -> },
    onRecordRelapse: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteActivity: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activity = uiState.activities.find { it.id == activityId } ?: return
    val color = parseColor(activity.colorHex)

    val today = remember { LocalDate.now() }
    val todayStr = remember { today.format(DateTimeFormatter.ISO_LOCAL_DATE) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showRelapseDialog by remember { mutableStateOf(false) }
    var relapseNote by remember { mutableStateOf("") }

    val isCurrentActivityActive = uiState.activeSession?.activityId == activityId
    val currentStatus = if (isCurrentActivityActive) uiState.activeSession?.status else null

    val isRunning = currentStatus == SessionStatus.ACTIVE
    val isOnBreak = currentStatus == SessionStatus.ON_BREAK

    val motto = remember(activityId) {
        when (activityId.lowercase()) {
            "meditation" -> "Silence. Breath. Awareness."
            "study" -> "Focus. Learn. Grow."
            "workout" -> "Discipline. Strength. Power."
            "sleep" -> "Rest. Restore. Recover."
            else -> if (activity.activityType == ActivityType.HABIT_QUIT) "Sovereignty. Freedom. Fortitude." else "Dedication. Mastery. Growth."
        }
    }

    val elapsedSeconds = if (isCurrentActivityActive) uiState.currentSessionElapsedSeconds else 0L
    val breakSeconds = if (isOnBreak) uiState.currentBreakElapsedSeconds else 0L

    val formattedSessionTimer = formatSecondsToHms(elapsedSeconds)
    val formattedBreakTimer = formatSecondsToHms(breakSeconds)

    val todayMinutes = uiState.todaySummary?.activityMinutes?.get(activityId) ?: 0
    val goalMinutes = activity.dailyGoalMinutes
    val progressPct = uiState.todaySummary?.activityPercentages?.get(activityId) ?: 0f

    val breaksBySessionId = remember(uiState.allBreaks) {
        uiState.allBreaks.groupBy { it.sessionId }
    }

    val activitySessions = remember(uiState.allSessions, activityId) {
        uiState.allSessions.filter { it.activityId == activityId }.sortedByDescending { it.startTime }
    }

    val todayCompletedSessions = remember(activitySessions, todayStr) {
        activitySessions.filter { it.dateString == todayStr && it.status == SessionStatus.COMPLETED }
    }

    val pastDaysSessions = remember(activitySessions, todayStr) {
        activitySessions.filter { it.dateString != todayStr }
    }

    val groupedPastSessions = remember(pastDaysSessions) {
        pastDaysSessions.groupBy { it.dateString }
    }

    val habitCompletions = remember(uiState.allHabitCompletions, activityId) {
        uiState.allHabitCompletions.filter { it.activityId == activityId }.sortedByDescending { it.dateString }
    }

    val habitInfo = uiState.habitStats[activityId] ?: HabitStreakInfo(0, 0, 0, false)
    val isHabitDoneToday = habitCompletions.any { it.dateString == todayStr && it.isSuccess }

    val progState = uiState.progressionStates[activityId]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = activity.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontSize = 14.sp
                            ),
                            color = Color(0xFFF5F5F4)
                        )
                        Text(
                            text = activity.getFormattedScheduledDays(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFFA1A1AA)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFF5F5F4)
                        )
                    }
                },
                actions = {
                    if (!activity.isCore) {
                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier.testTag("delete_activity_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Activity",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0A0A)
                )
            )
        },
        containerColor = Color(0xFF0A0A0A),
        modifier = modifier.testTag("timer_screen_$activityId")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Motto Header
            item {
                Text(
                    text = motto,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp,
                        fontSize = 13.sp
                    ),
                    color = color
                )
            }

            // 2. Main Interface: Habit vs Time-Based
            if (activity.activityType == ActivityType.HABIT_BUILD) {
                item {
                    HabitBuildDetailCard(
                        activity = activity,
                        habitInfo = habitInfo,
                        isDoneToday = isHabitDoneToday,
                        progressionState = progState,
                        color = color,
                        onToggle = { onToggleHabit(activityId, todayStr) }
                    )
                }
            } else if (activity.activityType == ActivityType.HABIT_QUIT) {
                item {
                    HabitQuitDetailCard(
                        activity = activity,
                        habitInfo = habitInfo,
                        isCleanToday = isHabitDoneToday,
                        progressionState = progState,
                        color = color,
                        onMarkClean = { onToggleHabit(activityId, todayStr) },
                        onRelapseClick = { showRelapseDialog = true }
                    )
                }
            } else {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 1.dp,
                                color = if (isOnBreak) GoldPrimary.copy(alpha = 0.4f) else color.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF141414)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                color = if (isOnBreak) GoldPrimary.copy(alpha = 0.15f) else if (isRunning) color.copy(alpha = 0.15f) else Color(0xFF1E1E22),
                                shape = RoundedCornerShape(100.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isOnBreak) GoldPrimary.copy(alpha = 0.5f) else if (isRunning) color.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f)
                                )
                            ) {
                                Text(
                                    text = when {
                                        isOnBreak -> "ON BREAK"
                                        isRunning -> "ACTIVE SESSION"
                                        else -> "SESSION READY"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isOnBreak) GoldPrimary else if (isRunning) color else Color(0xFFA1A1AA),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            if (isOnBreak) {
                                Text(
                                    text = formattedBreakTimer,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 44.sp,
                                        letterSpacing = 2.sp
                                    ),
                                    color = GoldPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Total Active: $formattedSessionTimer",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = Color(0xFF71717A)
                                )
                            } else {
                                Text(
                                    text = formattedSessionTimer,
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 44.sp,
                                        letterSpacing = 2.sp
                                    ),
                                    color = if (isRunning) Color(0xFFF5F5F4) else Color(0xFF71717A)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TODAY'S ACCUMULATION",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            fontSize = 10.sp
                                        ),
                                        color = Color(0xFFA1A1AA)
                                    )
                                    Text(
                                        text = "${todayMinutes / 60}h ${todayMinutes % 60}m / ${goalMinutes / 60}h ${goalMinutes % 60}m",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFFF5F5F4)
                                    )
                                }

                                Text(
                                    text = "${(progressPct * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (progressPct >= 1f) EmeraldMeditation else color
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressPct.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = color,
                                trackColor = Color.White.copy(alpha = 0.06f)
                            )
                        }
                    }
                }

                // 3. Primary Actions (Punch Controls)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        when {
                            isRunning -> {
                                OutlinedButton(
                                    onClick = onBreakIn,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .testTag("break_in_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = GoldLight
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Break In",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "BREAK IN",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Button(
                                    onClick = onPunchOut,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .testTag("punch_out_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = color,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Punch Out",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PUNCH OUT",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            isOnBreak -> {
                                Button(
                                    onClick = onBreakOut,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .testTag("break_out_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = color,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Break Out",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "BREAK OUT",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                OutlinedButton(
                                    onClick = onPunchOut,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .testTag("end_session_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "End Session",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "END SESSION",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { onStartActivity(activityId) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .testTag("start_session_primary_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = color,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start Activity",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "START ${activity.name.uppercase()}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Special Sleep System Card (If activity is Sleep)
            if (activityId == "sleep" && uiState.todaySleepAnalysis != null) {
                item {
                    val analysis = uiState.todaySleepAnalysis
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, MoonlitSleep.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CIRCADIAN RESTORATION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp
                                    ),
                                    color = MoonlitSleep
                                )

                                Surface(
                                    color = MoonlitSleep.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${analysis.overallSleepScore}/100 SCORE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MoonlitSleep,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Bedtime: ${analysis.formattedBedtime}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (analysis.isWithinBedtimeWindow) Tier4Color else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Wake: ${analysis.formattedWakeTime}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (analysis.isWithinWakeWindow) Tier4Color else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Duration Score: ${analysis.durationScore}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Timing Score: ${analysis.timingScore}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Complete Historical Log of Sessions (Bug Fix for Activity History)
            if (activity.activityType == ActivityType.TIME_BASED) {
                // Section Title: Today's Full Session Log
                item {
                    val activeCount = if (isCurrentActivityActive) 1 else 0
                    val totalTodaySessionsCount = todayCompletedSessions.size + activeCount

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY'S SESSIONS LOG ($totalTodaySessionsCount)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color(0xFFA1A1AA)
                        )

                        Text(
                            text = "${todayMinutes / 60}h ${todayMinutes % 60}m Total",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = color
                        )
                    }
                }

                // Item: Complete List of All Sessions for Today (Active + Completed)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (!isCurrentActivityActive && todayCompletedSessions.isEmpty()) {
                                Text(
                                    text = "No sessions logged yet today. Press START above to begin.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = Color(0xFF71717A)
                                )
                            } else {
                                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                                val zone = ZoneId.systemDefault()

                                // 1. Current Active Session (if any)
                                if (isCurrentActivityActive && uiState.activeSession != null) {
                                    val activeSession = uiState.activeSession
                                    val startStr = Instant.ofEpochMilli(activeSession.startTime)
                                        .atZone(zone).toLocalTime().format(timeFormatter)
                                    val elapsedMins = (uiState.currentSessionElapsedSeconds / 60).toInt()

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(color.copy(alpha = 0.08f))
                                            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isOnBreak) GoldPrimary else EmeraldMeditation)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "$startStr – Present",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = Color(0xFFF5F5F4)
                                                )
                                            }

                                            Surface(
                                                color = if (isOnBreak) GoldPrimary.copy(alpha = 0.2f) else color.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = if (isOnBreak) "ON BREAK" else "ACTIVE NOW",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    color = if (isOnBreak) GoldPrimary else color,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Running duration: ${elapsedMins / 60}h ${elapsedMins % 60}m",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = Color(0xFFA1A1AA)
                                        )
                                    }
                                }

                                // 2. Past Completed Sessions for Today
                                todayCompletedSessions.forEachIndexed { index, session ->
                                    val startStr = Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalTime().format(timeFormatter)
                                    val endStr = session.endTime?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalTime().format(timeFormatter) } ?: "—"

                                    val sBreaks = breaksBySessionId[session.id] ?: emptyList()
                                    var breakMs = 0L
                                    sBreaks.forEach { b ->
                                        val bEnd = b.endTime ?: session.endTime ?: session.startTime
                                        breakMs += (bEnd - b.startTime).coerceAtLeast(0L)
                                    }
                                    val totalMs = ((session.endTime ?: session.startTime) - session.startTime).coerceAtLeast(0L)
                                    val netMs = (totalMs - breakMs).coerceAtLeast(0L)
                                    val netMinutes = (netMs / (1000 * 60)).toInt()
                                    val breakMinutes = (breakMs / (1000 * 60)).toInt()

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF1A1A1E))
                                            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Session #${todayCompletedSessions.size - index}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFA1A1AA)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "$startStr – $endStr",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                    color = Color(0xFFF5F5F4)
                                                )
                                            }

                                            Text(
                                                text = "${netMinutes / 60}h ${netMinutes % 60}m",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = color
                                                )
                                            )
                                        }

                                        if (breakMinutes > 0 || session.isManualCorrection || session.note.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (breakMinutes > 0) {
                                                    Text(
                                                        text = "• ${breakMinutes}m break",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                        color = GoldPrimary
                                                    )
                                                }
                                                if (session.isManualCorrection) {
                                                    Text(
                                                        text = "• Manual entry",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                        color = Color(0xFF71717A)
                                                    )
                                                }
                                                if (session.note.isNotBlank()) {
                                                    Text(
                                                        text = "• ${session.note}",
                                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                        color = Color(0xFFA1A1AA)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 6: Previous Days' History Log
                if (pastDaysSessions.isNotEmpty()) {
                    item {
                        Text(
                            text = "PAST DAYS LOG",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = Color(0xFFA1A1AA)
                        )
                    }

                    items(groupedPastSessions.keys.take(7).toList()) { dateKey ->
                        val dateSessions = groupedPastSessions[dateKey] ?: emptyList()

                        var totalDayMins = 0
                        dateSessions.forEach { s ->
                            val sBreaks = breaksBySessionId[s.id] ?: emptyList()
                            val breakMs = sBreaks.sumOf { (it.endTime ?: s.endTime ?: s.startTime) - it.startTime }
                            val netMs = ((s.endTime ?: s.startTime) - s.startTime - breakMs).coerceAtLeast(0L)
                            totalDayMins += (netMs / (1000 * 60)).toInt()
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateKey,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFF5F5F4)
                                    )
                                    Text(
                                        text = "${totalDayMins / 60}h ${totalDayMins % 60}m (${dateSessions.size} sessions)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Habit Completions History Log
                item {
                    Text(
                        text = "COMPLETION HISTORY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = Color(0xFFA1A1AA)
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (habitCompletions.isEmpty()) {
                                Text(
                                    text = "No history recorded yet. Tap the button above to log your first day.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = Color(0xFF71717A)
                                )
                            } else {
                                habitCompletions.take(14).forEach { completion ->
                                    val isSuccess = completion.isSuccess
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF1A1A1E))
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                                contentDescription = null,
                                                tint = if (isSuccess) EmeraldMeditation else Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = completion.dateString,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = Color(0xFFF5F5F4)
                                            )
                                        }

                                        Text(
                                            text = if (isSuccess) {
                                                if (activity.activityType == ActivityType.HABIT_QUIT) "Clean" else "Completed"
                                            } else "Relapse",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSuccess) EmeraldMeditation else Color(0xFFEF4444)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Delete Action for Custom Activity (Requirement 5)
            if (!activity.isCore) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("delete_activity_button_bottom"),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DELETE ACTIVITY", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Activity Deletion
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete '${activity.name}'?") },
            text = {
                Text("Are you sure you want to permanently delete this activity and all its logged history? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteActivity(activityId)
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Activity", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Relapse Confirmation Dialog for Habit Quit
    if (showRelapseDialog) {
        AlertDialog(
            onDismissRequest = { showRelapseDialog = false },
            title = { Text("Record Setback / Relapse") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Honesty is the root of sovereign discipline. Logging a slip resets your current clean streak, but your longest record remains preserved as proof of what you are capable of achieving.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = relapseNote,
                        onValueChange = { relapseNote = it },
                        label = { Text("What triggered this? (Optional reflection)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRecordRelapse(activityId, todayStr, relapseNote.trim())
                        showRelapseDialog = false
                        relapseNote = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Log Relapse & Reset Streak", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRelapseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HabitBuildDetailCard(
    activity: ActivityEntity,
    habitInfo: HabitStreakInfo,
    isDoneToday: Boolean,
    progressionState: com.example.domain.engine.ProgressionState?,
    color: Color,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, EmeraldMeditation.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrestigeEmblem(
                activityId = activity.id,
                level = progressionState?.currentLevel ?: 1,
                size = 80.dp,
                isAnimated = true,
                isHabitBuild = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "LVL ${progressionState?.currentLevel ?: 1}: ${progressionState?.currentTitle ?: "Sprout"}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = EmeraldMeditation
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${habitInfo.currentStreak} DAY STREAK",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 32.sp
                ),
                color = Color(0xFFF5F5F4)
            )

            Text(
                text = "${habitInfo.totalCompletions} total completions • Scheduled on ${activity.getFormattedScheduledDays()}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFF71717A)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("toggle_habit_detail"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDoneToday) EmeraldMeditation else Color(0xFF1E1E24),
                    contentColor = if (isDoneToday) Color(0xFF0A0A0A) else Color(0xFFF5F5F4)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isDoneToday) EmeraldMeditation else Color.White.copy(alpha = 0.1f)
                )
            ) {
                Icon(
                    imageVector = if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isDoneToday) "COMPLETED TODAY ✓" else "MARK AS DONE TODAY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

@Composable
fun HabitQuitDetailCard(
    activity: ActivityEntity,
    habitInfo: HabitStreakInfo,
    isCleanToday: Boolean,
    progressionState: com.example.domain.engine.ProgressionState?,
    color: Color,
    onMarkClean: () -> Unit,
    onRelapseClick: () -> Unit
) {
    val quitRuby = Color(0xFFEF4444)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, quitRuby.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161214)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrestigeEmblem(
                activityId = activity.id,
                level = progressionState?.currentLevel ?: 1,
                size = 80.dp,
                isAnimated = true,
                isHabitQuit = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "LVL ${progressionState?.currentLevel ?: 1}: ${progressionState?.currentTitle ?: "Sovereign Choice"}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = quitRuby
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${habitInfo.currentStreak} DAYS CLEAN",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 32.sp
                ),
                color = Color(0xFFF5F5F4)
            )

            Text(
                text = "Longest Streak: ${habitInfo.longestStreak} days • Scheduled: ${activity.getFormattedScheduledDays()}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFF71717A)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onMarkClean,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("toggle_quit_detail"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCleanToday) EmeraldMeditation else Color(0xFF22161A),
                    contentColor = if (isCleanToday) Color(0xFF0A0A0A) else quitRuby
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCleanToday) EmeraldMeditation else quitRuby.copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    imageVector = if (isCleanToday) Icons.Default.Shield else Icons.Default.GppBad,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isCleanToday) "CLEAN TODAY ✓" else "MARK CLEAN TODAY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onRelapseClick,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF71717A))
            ) {
                Text("Log Relapse / Reset Streak", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

fun formatSecondsToHms(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hrs, mins, secs)
}
