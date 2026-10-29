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
import com.example.data.model.ActivityType
import com.example.ui.theme.*
import com.example.ui.viewmodel.AscendUiState
import java.time.DayOfWeek

@Composable
fun SettingsScreen(
    uiState: AscendUiState,
    onUpdateName: (String) -> Unit,
    onToggleDark: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onUpdateGoal: (String, Int) -> Unit,
    onUpdateActivity: (ActivityEntity) -> Unit,
    onDeleteActivity: (String) -> Unit,
    onUpdateSleepSchedule: (Int, Int, Int, Int) -> Unit,
    onCreateCustomActivity: (String, String, ActivityType, Int, String, String, String, Boolean) -> Unit,
    onNavigateToCorrections: () -> Unit,
    onSeedDemoData: () -> Unit,
    onResetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var editActivityDialogTarget by remember { mutableStateOf<ActivityEntity?>(null) }
    var deleteActivityConfirmTarget by remember { mutableStateOf<ActivityEntity?>(null) }
    var showSleepScheduleDialog by remember { mutableStateOf(false) }
    var showAddCustomActivityDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "SYSTEM & PREFERENCES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 11.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        fontSize = 24.sp
                    ),
                    color = Color(0xFFF5F5F4)
                )
            }
        }

        // 2. Profile Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                    .clickable { showNameDialog = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.15f))
                                .border(1.dp, GoldPrimary.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = GoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = uiState.userSettings.userName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFF5F5F4)
                            )
                            Text(
                                text = "Personal System Architect",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF71717A)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3. Activity Schedules & Targets Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVITIES & SCHEDULES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontSize = 10.sp
                    ),
                    color = Color(0xFFA1A1AA)
                )

                Text(
                    text = "TAP TO EDIT",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                    color = Color(0xFF71717A)
                )
            }
        }

        items(uiState.activities) { act ->
            val color = parseColor(act.colorHex)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .clickable { editActivityDialogTarget = act }
                    .testTag("manage_activity_${act.id}"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = act.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFFF5F5F4)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (act.isCore) {
                                    Surface(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "CORE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color(0xFFA1A1AA),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    val typeLabel = when (act.activityType) {
                                        ActivityType.HABIT_BUILD -> "BUILD"
                                        ActivityType.HABIT_QUIT -> "QUIT"
                                        ActivityType.TIME_BASED -> "TIME"
                                    }
                                    Surface(
                                        color = color.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = typeLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = color,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Days: ${act.getFormattedScheduledDays()}" +
                                        if (act.activityType == ActivityType.TIME_BASED) " • ${act.dailyGoalMinutes / 60}h ${act.dailyGoalMinutes % 60}m/day" else "",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF71717A)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!act.isCore) {
                            IconButton(
                                onClick = { deleteActivityConfirmTarget = act },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF71717A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. Sleep Circadian Windows Card
        item {
            val settings = uiState.userSettings
            val bedStart = "${settings.bedtimeWindowStartMinutes / 60}:${String.format("%02d", settings.bedtimeWindowStartMinutes % 60)}"
            val bedEnd = "${settings.bedtimeWindowEndMinutes / 60}:${String.format("%02d", settings.bedtimeWindowEndMinutes % 60)}"
            val wakeStart = "${settings.wakeWindowStartMinutes / 60}:${String.format("%02d", settings.wakeWindowStartMinutes % 60)}"
            val wakeEnd = "${settings.wakeWindowEndMinutes / 60}:${String.format("%02d", settings.wakeWindowEndMinutes % 60)}"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, MoonlitSleep.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .clickable { showSleepScheduleDialog = true },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            text = "Circadian Schedule Windows",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MoonlitSleep
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Schedule",
                            tint = MoonlitSleep,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Preferred Bedtime: $bedStart – $bedEnd\nPreferred Wake: $wakeStart – $wakeEnd",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = Color(0xFFA1A1AA)
                    )
                }
            }
        }

        // 5. Add Custom Activity Button (Time-Based, Habit Build, or Habit Quit)
        item {
            OutlinedButton(
                onClick = { showAddCustomActivityDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("add_custom_activity_button"),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ADD ACTIVITY OR HABIT", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
            }
        }

        // 6. Data Management & History
        item {
            Text(
                text = "DATA & CORRECTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 10.sp
                ),
                color = Color(0xFFA1A1AA)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                    .clickable { onNavigateToCorrections() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = CyanStudy
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Activity History & Corrections",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFFF5F5F4)
                            )
                            Text(
                                text = "Add missed sessions or adjust logs",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = Color(0xFF71717A)
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF71717A)
                    )
                }
            }
        }

        // 7. Seed Demo Data & Reset
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSeedDemoData,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E1E24),
                        contentColor = Color(0xFFF5F5F4)
                    )
                ) {
                    Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Seed 30D Demo", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Logs", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        // 8. Philosophy Footer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "THE PATH OF ASCEND",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 11.sp
                        ),
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"Every hour and habit you invest here is becoming part of who you are.\"",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = 13.sp
                        ),
                        color = Color(0xFFF5F5F4),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Private • Offline-First • Self-Mastery Engine",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color(0xFF71717A)
                    )
                }
            }
        }
    }

    // Dialog: Edit Name
    if (showNameDialog) {
        var nameInput by remember { mutableStateOf(uiState.userSettings.userName) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Edit Architect Name") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Your Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nameInput.isNotBlank()) {
                        onUpdateName(nameInput.trim())
                    }
                    showNameDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Edit Activity (Goal & Scheduled Days of the Week)
    editActivityDialogTarget?.let { act ->
        var hoursInput by remember { mutableStateOf((act.dailyGoalMinutes / 60).toString()) }
        var minInput by remember { mutableStateOf((act.dailyGoalMinutes % 60).toString()) }
        var selectedDays by remember(act) { mutableStateOf(act.scheduledDaysSet()) }

        AlertDialog(
            onDismissRequest = { editActivityDialogTarget = null },
            title = { Text("Edit ${act.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (act.activityType == ActivityType.TIME_BASED) {
                        Text("Daily Goal:", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA1A1AA))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = hoursInput,
                                onValueChange = { hoursInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Hours") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = minInput,
                                onValueChange = { minInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Minutes") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text("Scheduled Days of the Week:", style = MaterialTheme.typography.labelMedium, color = Color(0xFFA1A1AA))
                    DaysOfWeekSelector(
                        selectedDays = selectedDays,
                        onToggleDay = { dayVal ->
                            selectedDays = if (selectedDays.contains(dayVal)) {
                                if (selectedDays.size > 1) selectedDays - dayVal else selectedDays
                            } else {
                                selectedDays + dayVal
                            }
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = hoursInput.toIntOrNull() ?: 0
                    val m = minInput.toIntOrNull() ?: 0
                    val totalMin = (h * 60 + m).coerceIn(1, 1440)
                    val daysStr = selectedDays.sorted().joinToString(",")
                    onUpdateActivity(
                        act.copy(
                            dailyGoalMinutes = totalMin,
                            scheduledDays = daysStr
                        )
                    )
                    editActivityDialogTarget = null
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editActivityDialogTarget = null }) { Text("Cancel") }
            }
        )
    }

    // Dialog: Delete Activity Confirmation (Requirement 5)
    deleteActivityConfirmTarget?.let { act ->
        AlertDialog(
            onDismissRequest = { deleteActivityConfirmTarget = null },
            title = { Text("Delete '${act.name}'?") },
            text = {
                Text("Are you sure you want to permanently delete '${act.name}' and all its tracked history? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteActivity(act.id)
                        deleteActivityConfirmTarget = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Activity", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteActivityConfirmTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Create Custom Activity (Time-based, Habit Build, or Habit Quit)
    if (showAddCustomActivityDialog) {
        var customName by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(ActivityType.TIME_BASED) }
        var customHours by remember { mutableStateOf("1") }
        var selectedDays by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) }
        var customContributes by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddCustomActivityDialog = false },
            title = { Text("Create Practice or Habit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Type selector
                    Text("Practice Type:", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedType == ActivityType.TIME_BASED,
                            onClick = { selectedType = ActivityType.TIME_BASED },
                            label = { Text("Timer", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedType == ActivityType.HABIT_BUILD,
                            onClick = { selectedType = ActivityType.HABIT_BUILD },
                            label = { Text("Build", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedType == ActivityType.HABIT_QUIT,
                            onClick = { selectedType = ActivityType.HABIT_QUIT },
                            label = { Text("Quit", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = {
                            Text(
                                when (selectedType) {
                                    ActivityType.TIME_BASED -> "Activity Name (e.g. Coding, Reading)"
                                    ActivityType.HABIT_BUILD -> "Habit to Build (e.g. Read 10 Pages)"
                                    ActivityType.HABIT_QUIT -> "Habit to Quit (e.g. No Sugar, Stop Vaping)"
                                }
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (selectedType == ActivityType.TIME_BASED) {
                        OutlinedTextField(
                            value = customHours,
                            onValueChange = { customHours = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Daily Target Hours") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Text("Scheduled Days:", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA1A1AA))
                    DaysOfWeekSelector(
                        selectedDays = selectedDays,
                        onToggleDay = { dayVal ->
                            selectedDays = if (selectedDays.contains(dayVal)) {
                                if (selectedDays.size > 1) selectedDays - dayVal else selectedDays
                            } else {
                                selectedDays + dayVal
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contribute to Main System", style = MaterialTheme.typography.bodySmall)
                        Switch(checked = customContributes, onCheckedChange = { customContributes = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customName.isNotBlank()) {
                        val hours = customHours.toIntOrNull() ?: 1
                        val daysStr = selectedDays.sorted().joinToString(",")
                        val colorHex = when (selectedType) {
                            ActivityType.HABIT_QUIT -> "#EF4444"
                            ActivityType.HABIT_BUILD -> "#10B981"
                            ActivityType.TIME_BASED -> "#E5A93B"
                        }
                        val iconName = when (selectedType) {
                            ActivityType.HABIT_QUIT -> "shield"
                            ActivityType.HABIT_BUILD -> "check_circle"
                            ActivityType.TIME_BASED -> "star"
                        }

                        onCreateCustomActivity(
                            customName.trim(),
                            when (selectedType) {
                                ActivityType.HABIT_QUIT -> "Sovereign abstinence habit"
                                ActivityType.HABIT_BUILD -> "Daily habit building practice"
                                ActivityType.TIME_BASED -> "Self-development practice"
                            },
                            selectedType,
                            hours * 60,
                            daysStr,
                            colorHex,
                            iconName,
                            customContributes
                        )
                    }
                    showAddCustomActivityDialog = false
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomActivityDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showSleepScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showSleepScheduleDialog = false },
            title = { Text("Preferred Sleep Schedule") },
            text = {
                Text(
                    "Standard Sleep Windows:\n• Bedtime Window: 22:00 – 23:30\n• Wake Window: 05:30 – 07:30\n\nAdhering to regular circadian windows maximizes sleep score and restorative XP.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateSleepSchedule(22 * 60, 23 * 60 + 30, 5 * 60 + 30, 7 * 60 + 30)
                    showSleepScheduleDialog = false
                }) {
                    Text("Apply Standard Windows")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepScheduleDialog = false }) { Text("Close") }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset All Session Logs?") },
            text = {
                Text("This will delete all logged sessions and reset the daily history. The 4 core activities and settings will be preserved.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DaysOfWeekSelector(
    selectedDays: Set<Int>,
    onToggleDay: (Int) -> Unit
) {
    val dayLabels = listOf(
        1 to "M",
        2 to "T",
        3 to "W",
        4 to "T",
        5 to "F",
        6 to "S",
        7 to "S"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        dayLabels.forEach { (dayVal, label) ->
            val isSelected = selectedDays.contains(dayVal)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) GoldPrimary else Color(0xFF1E1E24))
                    .border(1.dp, if (isSelected) GoldPrimary else Color.White.copy(alpha = 0.08f), CircleShape)
                    .clickable { onToggleDay(dayVal) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    color = if (isSelected) Color(0xFF0A0A0A) else Color(0xFFA1A1AA)
                )
            }
        }
    }
}
