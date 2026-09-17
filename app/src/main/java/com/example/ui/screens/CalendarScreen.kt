package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.DaySummary
import com.example.ui.components.HeatmapCalendar
import com.example.ui.theme.*
import com.example.ui.viewmodel.AscendUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    uiState: AscendUiState,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedActivityId by remember { mutableStateOf<String?>("all") }

    val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    val daySummary = uiState.dailySummaries[dateStr]

    val activeFilterColor = if (selectedActivityId != null && selectedActivityId != "all") {
        uiState.activities.find { it.id == selectedActivityId }?.let { parseColor(it.colorHex) }
    } else null

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "DISCIPLINE MATRIX",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Consistency Map",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 24.sp
                    ),
                    color = Color(0xFFF5F5F4)
                )
            }
        }

        // 2. Activity Filter Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedActivityId == "all",
                        onClick = { selectedActivityId = "all" },
                        label = {
                            Text(
                                "ALL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color(0xFF0A0A0A),
                            containerColor = Color(0xFF141414),
                            labelColor = Color(0xFFA1A1AA)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedActivityId == "all",
                            borderColor = Color.White.copy(alpha = 0.06f),
                            selectedBorderColor = GoldPrimary
                        )
                    )
                }

                items(uiState.activities) { act ->
                    val isSelected = selectedActivityId == act.id
                    val color = parseColor(act.colorHex)
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
                            selectedContainerColor = color,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF141414),
                            labelColor = Color(0xFFA1A1AA)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.White.copy(alpha = 0.06f),
                            selectedBorderColor = color
                        )
                    )
                }
            }
        }

        // 3. Heatmap Calendar Component
        item {
            HeatmapCalendar(
                dailySummaries = uiState.dailySummaries,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                activityFilterColor = activeFilterColor
            )
        }

        // 4. Selected Day Detail Section
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
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header with Date and Day Tier
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = Color(0xFFF5F5F4)
                            )
                            Text(
                                text = if (daySummary != null && daySummary.totalActiveMinutes > 0) {
                                    "${daySummary.totalActiveMinutes / 60}h ${daySummary.totalActiveMinutes % 60}m Active • ${daySummary.totalXp} XP Earned"
                                } else "No activity recorded on this date",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF71717A)
                            )
                        }

                        // Intensity Badge
                        val tier = daySummary?.intensityTier ?: 0
                        val tierName = when (tier) {
                            4 -> "100%+ Day"
                            3 -> "75-99% Day"
                            2 -> "50-74% Day"
                            1 -> "1-49% Day"
                            else -> "Rest Day"
                        }
                        val tierColor = when (tier) {
                            4 -> Tier4Color
                            3 -> Tier3Color
                            2 -> Tier2Color
                            1 -> Tier1Color
                            else -> Color(0xFF71717A)
                        }

                        Surface(
                            color = tierColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(100.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = tierName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 10.sp
                                ),
                                color = tierColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                    // Per-Activity Breakdown for this Day
                    uiState.activities.filter {
                        if (selectedActivityId == "all") true else it.id == selectedActivityId
                    }.forEach { act ->
                        val actMin = daySummary?.activityMinutes?.get(act.id) ?: 0
                        val goalMin = act.dailyGoalMinutes
                        val pct = daySummary?.activityPercentages?.get(act.id) ?: 0f
                        val xp = daySummary?.activityXp?.get(act.id) ?: 0
                        val color = parseColor(act.colorHex)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = act.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFFF5F5F4)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${actMin / 60}h ${actMin % 60}m",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF5F5F4)
                                )
                                Text(
                                    text = " / ${goalMin / 60}h",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF71717A)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Surface(
                                    color = if (pct >= 1f) Tier4Color.copy(alpha = 0.15f) else Color(0xFF1E1E22),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "+$xp XP",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = if (pct >= 1f) Tier4Color else Color(0xFFA1A1AA),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
