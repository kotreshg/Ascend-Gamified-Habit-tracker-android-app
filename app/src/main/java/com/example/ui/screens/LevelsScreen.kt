package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityType
import com.example.domain.engine.LevelInfo
import com.example.domain.engine.ProgressionEngine
import com.example.ui.components.LevelUpDialog
import com.example.ui.components.PrestigeEmblem
import com.example.ui.theme.*
import com.example.ui.viewmodel.AscendUiState

@Composable
fun LevelsScreen(
    uiState: AscendUiState,
    modifier: Modifier = Modifier
) {
    var selectedActivityId by remember { mutableStateOf("meditation") }
    var selectedLevelInfo by remember { mutableStateOf<LevelInfo?>(null) }

    val activity = uiState.activities.find { it.id == selectedActivityId } ?: uiState.activities.firstOrNull() ?: return
    val isHabitQuit = activity.activityType == ActivityType.HABIT_QUIT
    val isHabitBuild = activity.activityType == ActivityType.HABIT_BUILD

    val progState = uiState.progressionStates[activity.id] ?: ProgressionEngine.calculateProgression(activity.id, 0)
    val color = parseColor(activity.colorHex)

    val allLevels = remember(activity.id, isHabitQuit, isHabitBuild) {
        ProgressionEngine.getAllLevelsForActivity(activity.id, isHabitQuit = isHabitQuit, isHabitBuild = isHabitBuild)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("levels_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "MASTERY PROGRESSION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "13-Level Ascendance",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 24.sp
                    ),
                    color = Color(0xFFF5F5F4)
                )
            }
        }

        // 2. Activity Selector Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.activities) { act ->
                    val isSelected = act.id == selectedActivityId
                    val actColor = parseColor(act.colorHex)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedActivityId = act.id },
                        label = {
                            Text(
                                act.name.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = actColor,
                            selectedLabelColor = Color(0xFF0A0A0A),
                            containerColor = Color(0xFF141414),
                            labelColor = Color(0xFFA1A1AA)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.White.copy(alpha = 0.08f),
                            selectedBorderColor = actColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // 3. Current Rank Focus Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrestigeEmblem(
                        activityId = activity.id,
                        level = progState.currentLevel,
                        size = 110.dp,
                        isAnimated = true,
                        isHabitQuit = isHabitQuit,
                        isHabitBuild = isHabitBuild
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "LEVEL ${progState.currentLevel}".uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 11.sp
                        ),
                        color = Color(0xFFA1A1AA)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = progState.currentTitle,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = Color(0xFFF5F5F4)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val desc = ProgressionEngine.getLevelTitle(
                        activity.id,
                        progState.currentLevel,
                        isHabitQuit = isHabitQuit,
                        isHabitBuild = isHabitBuild
                    ).second
                    Text(
                        text = "\"$desc\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFFD4D4D8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress to Next Rank
                    if (!progState.isMaxLevel) {
                        val metricUnit = if (isHabitQuit || isHabitBuild) "DAYS" else "XP"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEXT: LVL ${progState.currentLevel + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = Color(0xFFA1A1AA)
                            )
                            Text(
                                text = "${progState.currentXp} / ${progState.nextLevelXp} $metricUnit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = color
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { progState.progressToNextLevel },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = color,
                            trackColor = Color.White.copy(alpha = 0.06f)
                        )
                    } else {
                        Surface(
                            color = GoldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(100.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "SUPREME TRANSCENDENCE UNLOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    fontSize = 10.sp
                                ),
                                color = GoldPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Protected Decay Floor Display (Requirement 1)
                    val metricUnit = if (isHabitQuit || isHabitBuild) "DAYS" else "XP"
                    Surface(
                        color = Color.White.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Current Level Decay Floor: ${progState.protectedFloorXp} $metricUnit (Floor preserved)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }
                }
            }
        }

        // 4. Section Title: Complete 13-Level Ascendance Roadmap
        item {
            Text(
                text = "ROADMAP & EMBLEMS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 10.sp
                ),
                color = Color(0xFFA1A1AA)
            )
        }

        // 5. 13-Level Roadmap Items
        items(allLevels) { lvl ->
            val isUnlocked = lvl.level <= progState.currentLevel
            val isCurrent = lvl.level == progState.currentLevel

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = if (isCurrent) color else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { selectedLevelInfo = lvl },
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFF18181A) else Color(0xFF141414)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrestigeEmblem(
                        activityId = activity.id,
                        level = lvl.level,
                        size = 52.dp,
                        isUnlocked = isUnlocked,
                        isAnimated = isCurrent,
                        isHabitQuit = isHabitQuit,
                        isHabitBuild = isHabitBuild
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LEVEL ${lvl.level}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = if (isUnlocked) color else Color(0xFF71717A)
                            )
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = color.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "CURRENT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = lvl.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = if (isUnlocked) Color(0xFFF5F5F4) else Color(0xFF71717A)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        val unit = if (isHabitQuit || isHabitBuild) "days" else "XP"
                        Text(
                            text = "${lvl.xpThreshold} $unit required",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF71717A)
                        )
                    }

                    if (isUnlocked) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Unlocked",
                                tint = color,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF3F3F46),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    selectedLevelInfo?.let { lvl ->
        AlertDialog(
            onDismissRequest = { selectedLevelInfo = null },
            title = { Text("Level ${lvl.level}: ${lvl.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "\"${lvl.description}\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val unit = if (isHabitQuit || isHabitBuild) "days continuous streak" else "accumulated XP"
                    Text(
                        text = "Threshold: ${lvl.xpThreshold} $unit required to achieve this rank.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedLevelInfo = null }) { Text("Close") }
            }
        )
    }
}
