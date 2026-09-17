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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityEntity
import com.example.data.model.SessionStatus
import com.example.domain.engine.ProgressionEngine
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
    onNavigateToLevels: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header (Date in tracking-widest, Welcome Name, Streak on right)
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

        // 3. Overall Growth Section (Matching Sophisticated Dark section)
        item {
            val ringData = uiState.activities.filter { it.contributesToMainSystem }.map { act ->
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

        // 4. Section Title: Core Foundations
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CORE FOUNDATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )

                Text(
                    text = "4 PILLARS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF71717A)
                )
            }
        }

        // 5. Core Activity Cards
        items(uiState.activities) { activity ->
            val isCurrentActive = uiState.activeSession?.activityId == activity.id
            val currentStatus = if (isCurrentActive) uiState.activeSession?.status else null

            val actualMin = uiState.todaySummary?.activityMinutes?.get(activity.id) ?: 0
            val goalMin = activity.dailyGoalMinutes
            val pct = uiState.todaySummary?.activityPercentages?.get(activity.id) ?: 0f
            val progState = uiState.progressionStates[activity.id]
            val color = parseColor(activity.colorHex)

            ActivityMasterCard(
                activity = activity,
                actualMinutes = actualMin,
                goalMinutes = goalMin,
                completionPct = pct,
                progressionState = progState,
                color = color,
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
    }
}

@Composable
fun ActivityMasterCard(
    activity: ActivityEntity,
    actualMinutes: Int,
    goalMinutes: Int,
    completionPct: Float,
    progressionState: com.example.domain.engine.ProgressionState?,
    color: Color,
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
                // Icon Box
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

            Spacer(modifier = Modifier.height(16.dp))

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

                    // Sleek horizontal progress bar
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
