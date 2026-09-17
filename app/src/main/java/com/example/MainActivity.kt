package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.model.SessionStatus
import com.example.ui.components.LevelUpDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AscendViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Today", Icons.Default.Dashboard)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Levels : Screen("levels", "Levels", Icons.Default.MilitaryTech)
    object Stats : Screen("stats", "Analytics", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val viewModel: AscendViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val navController = rememberNavController()

            AscendTheme(darkTheme = uiState.userSettings.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route

                    val bottomNavScreens = listOf(
                        Screen.Home,
                        Screen.Calendar,
                        Screen.Levels,
                        Screen.Stats,
                        Screen.Settings
                    )

                    val showBottomNav = bottomNavScreens.any { it.route == currentRoute }

                    Scaffold(
                        bottomBar = {
                            if (showBottomNav) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    // Mini Floating Pill if a session is actively running elsewhere
                                    val activeSession = uiState.activeSession
                                    val activeActivity = uiState.activeActivity
                                    if (activeSession != null && activeActivity != null && currentRoute != "timer/{activityId}") {
                                        val color = parseColor(activeActivity.colorHex)
                                        val isBreak = activeSession.status == SessionStatus.ON_BREAK
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .border(1.dp, if (isBreak) GoldPrimary else color, RoundedCornerShape(14.dp))
                                                .clickable {
                                                    navController.navigate("timer/${activeActivity.id}")
                                                },
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isBreak) GoldPrimary else color)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = "${activeActivity.name}: ${if (isBreak) "Break" else "Active"} ${formatSecondsToHms(uiState.currentSessionElapsedSeconds)}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                Text(
                                                    text = "VIEW >",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = GoldLight
                                                )
                                            }
                                        }
                                    }

                                    NavigationBar(
                                        containerColor = Color(0xFF0F0F0F),
                                        tonalElevation = 0.dp,
                                        modifier = Modifier
                                            .testTag("bottom_navigation_bar")
                                            .border(
                                                width = 1.dp,
                                                color = Color.White.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
                                            )
                                    ) {
                                        bottomNavScreens.forEach { screen ->
                                            val isSelected = currentRoute == screen.route
                                            NavigationBarItem(
                                                icon = {
                                                    Icon(
                                                        imageVector = screen.icon,
                                                        contentDescription = screen.label
                                                    )
                                                },
                                                label = {
                                                    Text(
                                                        text = screen.label,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                },
                                                selected = isSelected,
                                                onClick = {
                                                    if (currentRoute != screen.route) {
                                                        navController.navigate(screen.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = GoldPrimary,
                                                    selectedTextColor = GoldPrimary,
                                                    indicatorColor = Color(0x1AD4AF37),
                                                    unselectedIconColor = Color(0xFF71717A),
                                                    unselectedTextColor = Color(0xFF71717A)
                                                ),
                                                modifier = Modifier.testTag("nav_item_${screen.route}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Home.route,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(Screen.Home.route) {
                                HomeScreen(
                                    uiState = uiState,
                                    onStartActivity = { actId -> viewModel.startActivity(actId) },
                                    onOpenTimer = { actId -> navController.navigate("timer/$actId") },
                                    onNavigateToLevels = { navController.navigate(Screen.Levels.route) }
                                )
                            }

                            composable(Screen.Calendar.route) {
                                CalendarScreen(uiState = uiState)
                            }

                            composable(Screen.Levels.route) {
                                LevelsScreen(uiState = uiState)
                            }

                            composable(Screen.Stats.route) {
                                StatsScreen(uiState = uiState)
                            }

                            composable(Screen.Settings.route) {
                                SettingsScreen(
                                    uiState = uiState,
                                    onUpdateName = { viewModel.updateUserProfileName(it) },
                                    onToggleDark = { viewModel.toggleDarkMode(it) },
                                    onToggleNotifications = { viewModel.toggleNotifications(it) },
                                    onUpdateGoal = { actId, min -> viewModel.updateDailyGoal(actId, min) },
                                    onUpdateSleepSchedule = { bStart, bEnd, wStart, wEnd ->
                                        viewModel.updateSleepSchedule(bStart, bEnd, wStart, wEnd)
                                    },
                                    onCreateCustomActivity = { name, desc, min, hex, icon, cont ->
                                        viewModel.createCustomActivity(name, desc, min, hex, icon, cont)
                                    },
                                    onNavigateToCorrections = { navController.navigate("corrections") },
                                    onSeedDemoData = { viewModel.seedDemoData() },
                                    onResetData = { viewModel.resetAllData() }
                                )
                            }

                            composable(
                                route = "timer/{activityId}",
                                arguments = listOf(navArgument("activityId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val actId = backStackEntry.arguments?.getString("activityId") ?: "meditation"
                                TimerScreen(
                                    activityId = actId,
                                    uiState = uiState,
                                    onBack = { navController.popBackStack() },
                                    onStartActivity = { viewModel.startActivity(it) },
                                    onBreakIn = { viewModel.breakIn() },
                                    onBreakOut = { viewModel.breakOut() },
                                    onPunchOut = { viewModel.punchOut() }
                                )
                            }

                            composable("corrections") {
                                CorrectionsScreen(
                                    uiState = uiState,
                                    onBack = { navController.popBackStack() },
                                    onAddSession = { actId, dateStr, sTime, eTime, bMin, note ->
                                        viewModel.addManualCorrectionSession(actId, dateStr, sTime, eTime, bMin, note)
                                    },
                                    onDeleteSession = { viewModel.deleteSession(it) }
                                )
                            }
                        }
                    }

                    // Level Up Celebration Modal Dialog
                    uiState.levelUpCelebration?.let { celebration ->
                        LevelUpDialog(
                            activityName = celebration.activity.name,
                            activityId = celebration.activity.id,
                            level = celebration.levelInfo.level,
                            title = celebration.levelInfo.title,
                            description = celebration.levelInfo.description,
                            onDismiss = { viewModel.dismissLevelUpCelebration() }
                        )
                    }
                }
            }
        }
    }
}
