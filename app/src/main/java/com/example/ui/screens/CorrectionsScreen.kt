package com.example.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityEntity
import com.example.data.model.SessionEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AscendUiState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CorrectionsScreen(
    uiState: AscendUiState,
    onBack: () -> Unit,
    onAddSession: (String, String, Long, Long, Int, String) -> Unit,
    onDeleteSession: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ACTIVITY HISTORY & CORRECTIONS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            fontSize = 13.sp
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
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Session",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        },
        containerColor = Color(0xFF0A0A0A),
        modifier = modifier.testTag("corrections_screen")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Philosophy reminder
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "HONEST RECORD OF ACTUAL WORK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontSize = 10.sp
                            ),
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Corrections exist to capture real sessions you forgot to punch in, not to optimize numbers. Keep the system pure.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MANUAL CORRECTION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 10.sp
                        ),
                        color = Color(0xFFA1A1AA)
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color(0xFF0A0A0A)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Record", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Grouped daily summaries / session history
            val sortedDates = uiState.dailySummaries.keys.sortedDescending()
            items(sortedDates) { dateStr ->
                val summary = uiState.dailySummaries[dateStr]
                if (summary != null && summary.totalActiveMinutes > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF5F5F4)
                                )

                                Text(
                                    text = "${summary.totalActiveMinutes / 60}h ${summary.totalActiveMinutes % 60}m Total",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GoldPrimary
                                )
                            }

                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                            uiState.activities.forEach { act ->
                                val min = summary.activityMinutes[act.id] ?: 0
                                if (min > 0) {
                                    val color = parseColor(act.colorHex)
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
                                                    .background(color)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = act.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                color = Color(0xFFF5F5F4)
                                            )
                                        }

                                        Text(
                                            text = "${min / 60}h ${min % 60}m",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color(0xFFF5F5F4)
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

    if (showAddDialog) {
        AddManualSessionDialog(
            activities = uiState.activities,
            onDismiss = { showAddDialog = false },
            onConfirm = { actId, dateStr, startMs, endMs, breakMin, note ->
                onAddSession(actId, dateStr, startMs, endMs, breakMin, note)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddManualSessionDialog(
    activities: List<ActivityEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Long, Long, Int, String) -> Unit
) {
    var selectedActivityId by remember { mutableStateOf(activities.firstOrNull()?.id ?: "study") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var startHour by remember { mutableStateOf("09") }
    var startMinute by remember { mutableStateOf("00") }
    var endHour by remember { mutableStateOf("11") }
    var endMinute by remember { mutableStateOf("30") }
    var breakMinutesInput by remember { mutableStateOf("0") }
    var noteInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Past Activity") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Activity:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activities.take(4).forEach { act ->
                        val isSelected = act.id == selectedActivityId
                        val color = parseColor(act.colorHex)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedActivityId = act.id },
                            label = { Text(act.name.take(4).uppercase(), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startHour,
                        onValueChange = { startHour = it.take(2) },
                        label = { Text("Start Hr") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = startMinute,
                        onValueChange = { startMinute = it.take(2) },
                        label = { Text("Start Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = endHour,
                        onValueChange = { endHour = it.take(2) },
                        label = { Text("End Hr") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endMinute,
                        onValueChange = { endMinute = it.take(2) },
                        label = { Text("End Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = breakMinutesInput,
                    onValueChange = { breakMinutesInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Break Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Optional Note") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val sH = startHour.toIntOrNull() ?: 9
                val sM = startMinute.toIntOrNull() ?: 0
                val eH = endHour.toIntOrNull() ?: 11
                val eM = endMinute.toIntOrNull() ?: 30
                val breakMin = breakMinutesInput.toIntOrNull() ?: 0

                val zone = ZoneId.systemDefault()
                val startTime = selectedDate.atTime(sH.coerceIn(0, 23), sM.coerceIn(0, 59)).atZone(zone).toInstant().toEpochMilli()
                val endTime = selectedDate.atTime(eH.coerceIn(0, 23), eM.coerceIn(0, 59)).atZone(zone).toInstant().toEpochMilli()
                val dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                if (endTime > startTime) {
                    onConfirm(selectedActivityId, dateStr, startTime, endTime, breakMin, noteInput)
                }
            }) {
                Text("Save Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
