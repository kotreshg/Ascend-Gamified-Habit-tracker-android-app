package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityEntity
import com.example.data.model.ActivityType
import com.example.data.model.SessionStatus
import com.example.domain.engine.HabitStreakInfo
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AscendUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    uiState: AscendUiState,
    onStartActivity: (String) -> Unit,
    onOpenTimer: (String) -> Unit,
    onToggleHabit: (String) -> Unit,
    onNavigateToLevels: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val todayStr = remember { today.format(DateTimeFormatter.ISO_LOCAL_DATE) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header (Date, Welcome, Streak on right)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = today.format(DateTimeFormatter.ofPattern("EEEE, MMM d")).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFFA1A1AA)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Welcome, ${uiState.userSettings.userName}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.5).sp,
                            fontSize = 24.sp
                        ),
                        color = Color(0xFFF5F5F4)
                    )
                }

                // Streak on Right
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .clickable { onNavigateToLevels() }
                        .padding(bottom = 2.dp)
                ) {
                    Text(
                        text = "STREAK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFFA1A1AA)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${uiState.streakStats.currentStreak}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            ),
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "DAYS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            }
        }

        // 2. Daily Motivation Card
        item {
            DailyMotivationCard(quote = uiState.dailyQuote)
        }

        // 3. Overall Growth Section
        item {
            val ringData = uiState.activities.filter { it.contributesToMainSystem && it.activityType == ActivityType.TIME_BASED }.map { act ->
                val pct = uiState.todaySummary?.activityPercentages?.get(act.id) ?: 0f
                val color = parseColor(act.colorHex)
                ActivityRingData(act.id, act.name, color, pct)
            }
            val overallPct = uiState.todaySummary?.overallPercentage ?: 0f
            val xpToday = uiState.todaySummary?.totalXp ?: 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Overall Growth",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = Color(0xFFA1A1AA)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${(overallPct * 100).toInt()}",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                ),
                                color = Color(0xFFF5F5F4)
                            )
                            Text(
                                text = "%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFFF5F5F4).copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+$xpToday XP today",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = EmeraldMeditation
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val activeMin = uiState.todaySummary?.totalActiveMinutes ?: 0
                        val goalMin = uiState.todaySummary?.totalGoalMinutes ?: 0
                        val remainingMin = (goalMin - activeMin).coerceAtLeast(0)
                        Text(
                            text = if (remainingMin > 0) "${remainingMin / 60}h ${remainingMin % 60}m remaining today" else "Daily goal accomplished!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp
                            ),
                            color = Color(0xFF71717A)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    MasterRadialProgress(
                        overallPercentage = overallPct,
                        activityRings = ringData,
                        size = 84.dp
                    )
                }
            }
        }

        // 4. Section Title: Core Foundations & Custom Practices
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DISCIPLINE PILLARS & HABITS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )

                Text(
                    text = "${uiState.activities.size} ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF71717A)
                )
            }
        }

        // 5. Activity Cards
        items(uiState.activities) { activity ->
            val isScheduledToday = activity.isScheduledOn(today)
            val progState = uiState.progressionStates[activity.id]
            val color = parseColor(activity.colorHex)

            when (activity.activityType) {
                ActivityType.TIME_BASED -> {
                    val isCurrentActive = uiState.activeSession?.activityId == activity.id
                    val currentStatus = if (isCurrentActive) uiState.activeSession?.status else null
                    val actualMin = uiState.todaySummary?.activityMinutes?.get(activity.id) ?: 0
                    val goalMin = activity.dailyGoalMinutes
                    val pct = uiState.todaySummary?.activityPercentages?.get(activity.id) ?: 0f

                    TimeBasedActivityCard(
                        activity = activity,
                        actualMinutes = actualMin,
                        goalMinutes = goalMin,
                        completionPct = pct,
                        progressionState = progState,
                        color = color,
                        isScheduledToday = isScheduledToday,
                        sessionStatus = currentStatus,
                        liveElapsedSeconds = if (isCurrentActive) uiState.currentSessionElapsedSeconds else 0L,
                        onCardClick = { onOpenTimer(activity.id) },
                        onStartClick = {
                            if (isCurrentActive) {
                                onOpenTimer(activity.id)
                            } else {
                                onStartActivity(activity.id)
                                onOpenTimer(activity.id)
                            }
                        }
                    )
                }
                ActivityType.HABIT_BUILD -> {
                    val habitInfo = uiState.habitStats[activity.id] ?: HabitStreakInfo(0, 0, 0, false)
                    val isDoneToday = uiState.allHabitCompletions.any { it.activityId == activity.id && it.dateString == todayStr && it.isSuccess }

                    HabitBuildCard(
                        activity = activity,
                        habitInfo = habitInfo,
                        isDoneToday = isDoneToday,
                        progressionState = progState,
                        color = color,
                        isScheduledToday = isScheduledToday,
                        onCardClick = { onOpenTimer(activity.id) },
                        onToggle = { onToggleHabit(activity.id) }
                    )
                }
                ActivityType.HABIT_QUIT -> {
                    val habitInfo = uiState.habitStats[activity.id] ?: HabitStreakInfo(0, 0, 0, false)
                    val isCleanToday = uiState.allHabitCompletions.any { it.activityId == activity.id && it.dateString == todayStr && it.isSuccess }

                    HabitQuitCard(
                        activity = activity,
                        habitInfo = habitInfo,
                        isCleanToday = isCleanToday,
                        progressionState = progState,
                        color = color,
                        isScheduledToday = isScheduledToday,
                        onCardClick = { onOpenTimer(activity.id) },
                        onToggle = { onToggleHabit(activity.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TimeBasedActivityCard(
    activity: ActivityEntity,
    actualMinutes: Int,
    goalMinutes: Int,
    completionPct: Float,
    progressionState: com.example.domain.engine.ProgressionState?,
    color: Color,
    isScheduledToday: Boolean,
    sessionStatus: SessionStatus?,
    liveElapsedSeconds: Long,
    onCardClick: () -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actualHours = actualMinutes / 60
    val actualRemMin = actualMinutes % 60
    val goalHours = goalMinutes / 60
    val goalRemMin = goalMinutes % 60

    val goalStr = if (goalRemMin > 0) "${goalHours}h ${goalRemMin}m" else "${goalHours}h"
    val actualStr = "${actualHours}h ${actualRemMin}m"

    val isActive = sessionStatus == SessionStatus.ACTIVE
    val isOnBreak = sessionStatus == SessionStatus.ON_BREAK

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = if (isActive || isOnBreak) 1.5.dp else 1.dp,
                color = if (isActive) color.copy(alpha = 0.5f) else if (isOnBreak) GoldPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onCardClick() }
            .testTag("activity_card_${activity.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF181818) else Color(0xFF141414)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Activity Icon Container & Level Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon Box & Schedule Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getActivityIcon(activity.id),
                            contentDescription = activity.name,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Surface(
                            color = if (isScheduledToday) Color.White.copy(alpha = 0.06f) else Color(0xFF262626),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isScheduledToday) activity.getFormattedScheduledDays() else "Rest Day (${activity.getFormattedScheduledDays()})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isScheduledToday) Color(0xFFA1A1AA) else Color(0xFF71717A),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Level & Title Badge on Top Right
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LVL ${progressionState?.currentLevel ?: 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFFA1A1AA)
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = progressionState?.currentTitle ?: "Initiate",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = color
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Section: Name, Time, Progress Bar, and Start Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activity.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            ),
                            color = Color(0xFFF5F5F4)
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = color.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = color,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isOnBreak) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = GoldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "BREAK",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = GoldPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "$actualStr / $goalStr",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF71717A)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = completionPct.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Action button
                IconButton(
                    onClick = onStartClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive || isOnBreak) color else Color(0xFF1E1E22))
                        .border(1.dp, if (isActive || isOnBreak) color else Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                        .testTag("start_button_${activity.id}")
                ) {
                    Icon(
                        imageVector = if (isActive || isOnBreak) Icons.Default.Timer else Icons.Default.PlayArrow,
                        contentDescription = "Start ${activity.name}",
                        tint = if (isActive || isOnBreak) Color.White else Color(0xFFE5E5E0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HabitBuildCard(
    activity: ActivityEntity,
    habitInfo: HabitStreakInfo,
    isDoneToday: Boolean,
    progressionState: com.example.domain.engine.ProgressionState?,
    color: Color,
    isScheduledToday: Boolean,
    onCardClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = if (isDoneToday) 1.5.dp else 1.dp,
                color = if (isDoneToday) EmeraldMeditation.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onCardClick() }
            .testTag("habit_build_card_${activity.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Type Badge, Scheduled Days, and Level Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = EmeraldMeditation.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "HABIT BUILD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            ),
                            color = EmeraldMeditation,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (isScheduledToday) activity.getFormattedScheduledDays() else "Rest Day (${activity.getFormattedScheduledDays()})",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF71717A)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LVL ${progressionState?.currentLevel ?: 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFA1A1AA)
                    )
                    Text(
                        text = progressionState?.currentTitle ?: "Initiate",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = EmeraldMeditation
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body Row: Title, Streaks and Toggle Checkmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFFF5F5F4)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${habitInfo.currentStreak} day streak • ${habitInfo.totalCompletions} total completed",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF71717A)
                    )
                }

                // Checkmark button
                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDoneToday) EmeraldMeditation else Color(0xFF1E1E24),
                        contentColor = if (isDoneToday) Color(0xFF0A0A0A) else Color(0xFFF5F5F4)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDoneToday) EmeraldMeditation else Color.White.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.testTag("toggle_habit_${activity.id}")
                ) {
                    Icon(
                        imageVector = if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle completion",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDoneToday) "DONE TODAY" else "MARK DONE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun HabitQuitCard(
    activity: ActivityEntity,
    habitInfo: HabitStreakInfo,
    isCleanToday: Boolean,
    progressionState: com.example.domain.engine.ProgressionState?,
    color: Color,
    isScheduledToday: Boolean,
    onCardClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quitRuby = Color(0xFFEF4444)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = if (isCleanToday) 1.5.dp else 1.dp,
                color = if (isCleanToday) GoldPrimary.copy(alpha = 0.4f) else quitRuby.copy(alpha = 0.25f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable { onCardClick() }
            .testTag("habit_quit_card_${activity.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141214)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Type Badge, Scheduled Days, and Level Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = quitRuby.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = quitRuby,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "HABIT QUIT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = quitRuby
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (isScheduledToday) activity.getFormattedScheduledDays() else "Rest Day (${activity.getFormattedScheduledDays()})",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF71717A)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LVL ${progressionState?.currentLevel ?: 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFA1A1AA)
                    )
                    Text(
                        text = progressionState?.currentTitle ?: "Sovereign Choice",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = quitRuby
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body Row: Title, Streaks and Shield Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFFF5F5F4)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${habitInfo.currentStreak} days clean • Longest: ${habitInfo.longestStreak}d",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color(0xFF71717A)
                    )
                }

                // Clean status button
                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCleanToday) Color(0xFF1E281E) else Color(0xFF22161A),
                        contentColor = if (isCleanToday) EmeraldMeditation else quitRuby
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCleanToday) EmeraldMeditation.copy(alpha = 0.5f) else quitRuby.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("toggle_quit_${activity.id}")
                ) {
                    Icon(
                        imageVector = if (isCleanToday) Icons.Default.Shield else Icons.Default.GppBad,
                        contentDescription = "Mark Clean Today",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCleanToday) "CLEAN TODAY ✓" else "MARK CLEAN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}

fun getActivityIcon(activityId: String): ImageVector {
    return when (activityId.lowercase()) {
        "meditation" -> Icons.Default.SelfImprovement
        "study" -> Icons.Default.AutoStories
        "workout" -> Icons.Default.FitnessCenter
        "sleep" -> Icons.Default.Bedtime
        else -> Icons.Default.Stars
    }
}

fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        GoldPrimary
    }
}
