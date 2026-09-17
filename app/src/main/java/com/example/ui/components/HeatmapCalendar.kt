package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.DaySummary
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HeatmapCalendar(
    dailySummaries: Map<String, DaySummary>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    activityFilterColor: Color? = null
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    val daysOfWeek = remember { listOf("M", "T", "W", "T", "F", "S", "S") }

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
                .padding(16.dp)
        ) {
            // Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentYearMonth.year}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of Week Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                daysOfWeek.forEach { dayLetter ->
                    Text(
                        text = dayLetter,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid Matrix
            val firstDayOfMonth = currentYearMonth.atDay(1)
            val daysInMonth = currentYearMonth.lengthOfMonth()
            val startDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
            val leadingEmptyDays = startDayOfWeek - 1

            val totalCells = ((leadingEmptyDays + daysInMonth + 6) / 7) * 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (row in 0 until (totalCells / 7)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - leadingEmptyDays + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellDate = currentYearMonth.atDay(dayNumber)
                                val dateStr = cellDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val summary = dailySummaries[dateStr]
                                val tier = summary?.intensityTier ?: 0
                                val isSelected = cellDate == selectedDate
                                val isToday = cellDate == LocalDate.now()

                                val cellBgColor = if (activityFilterColor != null && summary != null && summary.totalActiveMinutes > 0) {
                                    activityFilterColor.copy(alpha = (summary.overallPercentage.coerceIn(0.2f, 1f)))
                                } else {
                                    when (tier) {
                                        4 -> Tier4Color
                                        3 -> Tier3Color
                                        2 -> Tier2Color
                                        1 -> Tier1Color
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cellBgColor)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(
                                                    2.dp,
                                                    GoldPrimary,
                                                    RoundedCornerShape(8.dp)
                                                )
                                            } else if (isToday) {
                                                Modifier.border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                            } else Modifier
                                        )
                                        .clickable { onDateSelected(cellDate) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        ),
                                        color = if (tier > 0 || (activityFilterColor != null && summary?.totalActiveMinutes ?: 0 > 0)) {
                                            Color.White
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Intensity Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))

                listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    Tier1Color,
                    Tier2Color,
                    Tier3Color,
                    Tier4Color
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(14.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "100%+",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
