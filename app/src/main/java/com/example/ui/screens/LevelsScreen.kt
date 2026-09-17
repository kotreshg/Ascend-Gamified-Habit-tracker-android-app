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
    val progState = uiState.progressionStates[activity.id] ?: ProgressionEngine.calculateProgression(activity.id, 0)
    val color = parseColor(activity.colorHex)

    val allLevels = remember(activity.id) {
        ProgressionEngine.getAllLevelsForActivity(activity.id)
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
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF141414),
                            labelColor = Color(0xFFA1A1AA)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.White.copy(alpha = 0.06f),
                            selectedBorderColor = actColor
                        )
                    )
                }
            }
        }

        // 3. Current Level Showcase Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = color.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Level Badge / Emblem Canvas
                    PrestigeEmblem(
                        activityId = activity.id,
                        level = progState.currentLevel,
                        size = 96.dp,
                        isUnlocked = true,
                        isAnimated = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "LEVEL ${progState.currentLevel}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 11.sp
                        ),
                        color = color
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = progState.currentTitle.uppercase(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFF5F5F4)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val (lore, _) = ProgressionEngine.getLevelTitle(activity.id, progState.currentLevel).let { it.second to it.first }
                    Text(
                        text = lore,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = Color(0xFFA1A1AA),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // XP Progress Curve Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${progState.currentXp} XP",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF5F5F4)
                        )
                        Text(
                            text = if (progState.isMaxLevel) "MAX LEVEL REACHED" else "Target: ${progState.nextLevelXp} XP",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (progState.isMaxLevel) GoldLight else Color(0xFF71717A)
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // Soft Decay Protected Floor Information
                    Surface(
                        color = Color(0xFF1A1A1A),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Protected Floor",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Protected Floor: ${progState.protectedFloorXp} XP (Soft Decay Floor)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }
                }
            }
        }

        // 4. Section Title: All 13 Levels
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ROADMAP OF ASCENT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )

                Text(
                    text = "13 TIERS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF71717A)
                )
            }
        }

        // 5. 13 Level Roadmap Items
        items(allLevels) { levelInfo ->
            val isUnlocked = progState.currentLevel >= levelInfo.level
            val isCurrent = progState.currentLevel == levelInfo.level

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = if (isCurrent) color.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { selectedLevelInfo = levelInfo },
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFF181818) else Color(0xFF141414)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Level Emblem
                        PrestigeEmblem(
                            activityId = activity.id,
                            level = levelInfo.level,
                            size = 48.dp,
                            isUnlocked = isUnlocked
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LEVEL ${levelInfo.level}",
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
                                        color = color.copy(alpha = 0.15f),
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
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = levelInfo.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = if (isUnlocked) Color(0xFFF5F5F4) else Color(0xFF71717A)
                                )
                            )

                            Text(
                                text = "${levelInfo.xpThreshold} XP required",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF71717A)
                            )
                        }
                    }

                    // Status icon / badge
                    when {
                        isUnlocked -> {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldMeditation.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = EmeraldMeditation,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color(0xFF52525B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Level Details Dialog on Click
    selectedLevelInfo?.let { info ->
        val isUnlocked = progState.currentLevel >= info.level
        LevelUpDialog(
            activityName = activity.name,
            activityId = activity.id,
            level = info.level,
            title = info.title,
            description = "${info.description}\n\nRequired XP: ${info.xpThreshold} XP (${if (isUnlocked) "UNLOCKED" else "LOCKED"})",
            onDismiss = { selectedLevelInfo = null }
        )
    }
}
