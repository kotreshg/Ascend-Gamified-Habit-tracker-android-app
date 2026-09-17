package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityEntity
import com.example.data.model.BreakEntity
import com.example.data.model.SessionEntity
import com.example.data.model.SessionStatus
import com.example.ui.components.CircularActivityRing
import com.example.ui.theme.*
import com.example.ui.viewmodel.AscendUiState
import java.time.Instant
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
    modifier: Modifier = Modifier
) {
    val activity = uiState.activities.find { it.id == activityId } ?: return
    val color = parseColor(activity.colorHex)

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
            else -> "Dedication. Mastery. Growth."
        }
    }

    val elapsedSeconds = if (isCurrentActivityActive) uiState.currentSessionElapsedSeconds else 0L
    val breakSeconds = if (isOnBreak) uiState.currentBreakElapsedSeconds else 0L

    val formattedSessionTimer = formatSecondsToHms(elapsedSeconds)
    val formattedBreakTimer = formatSecondsToHms(breakSeconds)

    val todayMinutes = uiState.todaySummary?.activityMinutes?.get(activityId) ?: 0
    val goalMinutes = activity.dailyGoalMinutes
    val progressPct = uiState.todaySummary?.activityPercentages?.get(activityId) ?: 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activity.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 14.sp
                        ),
                        color = Color(0xFFF5F5F4)
                    )
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

            // 2. Large Focused Digital Timer Display Card
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
                        // Status badge
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

                        // Large Digital Timer
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

                        // Goal & Progress Bar
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
                            // Active State -> Break In & Punch Out
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
                            // On Break -> Break Out & End Session
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
                            // Idle -> Start Session
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

            // 5. Session Timeline Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S TIMELINE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${todayMinutes / 60}h ${todayMinutes % 60}m Total",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Timeline Card / List
            item {
                TodaySessionTimeline(
                    activity = activity,
                    activeSession = if (isCurrentActivityActive) uiState.activeSession else null,
                    activeBreaks = if (isCurrentActivityActive) uiState.activeSessionBreaks else emptyList()
                )
            }
        }
    }
}

@Composable
fun TodaySessionTimeline(
    activity: ActivityEntity,
    activeSession: SessionEntity?,
    activeBreaks: List<BreakEntity>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (activeSession == null) {
                Text(
                    text = "No active session in progress. Press Start above to begin tracking.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = Color(0xFF71717A)
                )
            } else {
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
                val zone = ZoneId.systemDefault()
                val startStr = Instant.ofEpochMilli(activeSession.startTime)
                    .atZone(zone).toLocalTime().format(timeFormatter)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(EmeraldMeditation)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$startStr – Present",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFF5F5F4)
                        )
                    }

                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = parseColor(activity.colorHex)
                    )
                }

                activeBreaks.forEach { b ->
                    val bStart = Instant.ofEpochMilli(b.startTime).atZone(zone).toLocalTime().format(timeFormatter)
                    val bEnd = if (b.endTime != null) {
                        Instant.ofEpochMilli(b.endTime).atZone(zone).toLocalTime().format(timeFormatter)
                    } else "Ongoing"
                    val bDurationMin = if (b.endTime != null) ((b.endTime - b.startTime) / (1000 * 60)).toInt() else 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$bStart – $bEnd (Break)",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = GoldPrimary
                            )
                        }

                        Text(
                            text = if (bDurationMin > 0) "${bDurationMin}m" else "Active",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                            color = GoldPrimary
                        )
                    }
                }
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
