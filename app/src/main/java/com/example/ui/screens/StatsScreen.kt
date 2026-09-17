package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AscendUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class StatsTimeframe(val label: String, val days: Int) {
    SEVEN_DAYS("7D", 7),
    THIRTY_DAYS("30D", 30),
    NINETY_DAYS("90D", 90),
    ONE_YEAR("1Y", 365),
    ALL_TIME("ALL", 10000)
}

@Composable
fun StatsScreen(
    uiState: AscendUiState,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf(StatsTimeframe.THIRTY_DAYS) }

    val today = remember { LocalDate.now() }
    val filteredSummaries = remember(uiState.dailySummaries, selectedTimeframe) {
        val cutoff = today.minusDays(selectedTimeframe.days.toLong())
        uiState.dailySummaries.values
            .filter { !it.localDate.isBefore(cutoff) && !it.localDate.isAfter(today) }
            .sortedBy { it.localDate }
    }

    // Calculations for timeframe
    val totalTimeframeActiveMin = filteredSummaries.sumOf { it.totalActiveMinutes }
    val totalTimeframeXp = filteredSummaries.sumOf { it.totalXp }
    val avgPct = if (filteredSummaries.isNotEmpty()) {
        filteredSummaries.map { it.overallPercentage }.average().toFloat()
    } else 0f

    val bestDay = filteredSummaries.maxByOrNull { it.totalActiveMinutes }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("stats_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "ANALYTICS & METRICS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Time Invested in Self",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 24.sp
                    ),
                    color = Color(0xFFF5F5F4)
                )
            }
        }

        // 2. Timeframe Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141414))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatsTimeframe.values().forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp)),
                        color = if (isSelected) GoldPrimary else Color.Transparent,
                        onClick = { selectedTimeframe = tf }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tf.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                color = if (isSelected) Color(0xFF0A0A0A) else Color(0xFFA1A1AA)
                            )
                        }
                    }
                }
            }
        }

        // 3. KPI Grid (Average Score, Total Time, Total XP, Streaks)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "AVG DAILY SCORE",
                    value = "${(avgPct * 100).toInt()}%",
                    subtitle = "Completion target",
                    color = EmeraldMeditation,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "TOTAL TIME",
                    value = "${totalTimeframeActiveMin / 60}h ${totalTimeframeActiveMin % 60}m",
                    subtitle = "${filteredSummaries.size} days measured",
                    color = CyanStudy,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "TOTAL XP EARNED",
                    value = "$totalTimeframeXp XP",
                    subtitle = "Mastery Points",
                    color = GoldPrimary,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "STREAK MASTERY",
                    value = "${uiState.streakStats.currentStreak} Days",
                    subtitle = "Longest: ${uiState.streakStats.longestStreak} Days",
                    color = FlameWorkout,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Daily Performance Chart
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
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PERFORMANCE TREND",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontSize = 10.sp
                            ),
                            color = Color(0xFFA1A1AA)
                        )

                        Text(
                            text = "Daily Completion %",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Color(0xFF71717A)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Trend Canvas Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val w = size.width
                        val h = size.height

                        // Draw baseline & 100% threshold guide line
                        val y100 = h * 0.15f
                        val y50 = h * 0.55f
                        val y0 = h * 0.95f

                        drawLine(
                            color = Color.White.copy(alpha = 0.06f),
                            start = Offset(0f, y100),
                            end = Offset(w, y100),
                            strokeWidth = 1.dp.toPx()
                        )

                        drawLine(
                            color = Color.White.copy(alpha = 0.04f),
                            start = Offset(0f, y50),
                            end = Offset(w, y50),
                            strokeWidth = 1.dp.toPx()
                        )

                        val displayCount = filteredSummaries.takeLast(30)
                        if (displayCount.isNotEmpty()) {
                            val barSpacing = w / displayCount.size
                            val barWidth = (barSpacing * 0.65f).coerceIn(4f, 24f)

                            displayCount.forEachIndexed { idx, summary ->
                                val x = idx * barSpacing + (barSpacing - barWidth) / 2
                                val pct = summary.overallPercentage.coerceIn(0f, 1.25f)
                                val barHeight = (pct * (y0 - y100)).coerceAtLeast(4f)
                                val barTop = y0 - barHeight

                                val barColor = when {
                                    pct >= 1.0f -> Tier4Color
                                    pct >= 0.75f -> Tier3Color
                                    pct >= 0.50f -> Tier2Color
                                    pct > 0f -> Tier1Color
                                    else -> Color(0xFF1E1E24)
                                }

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x, barTop),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Activity Share Breakdown Card
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
                    Text(
                        text = "PILLAR TIME ALLOCATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFFA1A1AA)
                    )

                    // Multi-color segmented proportion bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    ) {
                        uiState.activities.forEach { act ->
                            val actTotalMin = filteredSummaries.sumOf { it.activityMinutes[act.id] ?: 0 }
                            val weight = if (totalTimeframeActiveMin > 0) {
                                (actTotalMin.toFloat() / totalTimeframeActiveMin.toFloat()).coerceAtLeast(0.01f)
                            } else 1f / uiState.activities.size

                            Box(
                                modifier = Modifier
                                    .weight(weight)
                                    .fillMaxHeight()
                                    .background(parseColor(act.colorHex))
                            )
                        }
                    }

                    // Activity Table
                    uiState.activities.forEach { act ->
                        val actTotalMin = filteredSummaries.sumOf { it.activityMinutes[act.id] ?: 0 }
                        val pct = if (totalTimeframeActiveMin > 0) {
                            (actTotalMin.toFloat() / totalTimeframeActiveMin.toFloat()) * 100
                        } else 0f
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
                                        .clip(CircleShape)
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
                                    text = "${actTotalMin / 60}h ${actTotalMin % 60}m",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF5F5F4)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "(${pct.toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF71717A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                ),
                color = color
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    fontSize = 20.sp
                ),
                color = Color(0xFFF5F5F4)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color(0xFF71717A)
            )
        }
    }
}
