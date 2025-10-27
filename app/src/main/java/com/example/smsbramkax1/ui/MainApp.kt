package com.example.smsbramkax1.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.ui.components.Sidebar
import com.example.smsbramkax1.ui.screens.DashboardScreen
import com.example.smsbramkax1.ui.screens.DiagnosticsScreen
import com.example.smsbramkax1.ui.screens.HistoryScreen
import com.example.smsbramkax1.ui.screens.SettingsScreen
import com.example.smsbramkax1.ui.theme.CardBg
import com.example.smsbramkax1.ui.theme.Foreground
import com.example.smsbramkax1.utils.HealthChecker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    var selectedPage by remember { mutableStateOf("Dashboard") }
    var showHistory by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLargeScreen = configuration.screenWidthDp >= 840

    if (isLargeScreen) {
        // Desktop/Tablet - Sidebar zawsze widoczny
        Row(modifier = Modifier.fillMaxSize()) {
            Sidebar(selectedPage = selectedPage, onPageSelected = { selectedPage = it })
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    title = selectedPage,
                    showMenuIcon = false,
                    onMenuClick = {}
                )
                when (selectedPage) {
                    "Dashboard" -> DashboardScreen(
                        onNavigateToHistory = { selectedPage = "Historia SMS" },
                        onNavigateToSettings = { selectedPage = "Ustawienia" }
                    )
                    "Historia SMS" -> HistoryScreen(onBack = { selectedPage = "Dashboard" })
                    "Wyślij SMS" -> PlaceholderScreen("Wyślij SMS")
                    "Diagnostyka" -> {
                        val context = LocalContext.current
                        val database = SmsDatabase.getDatabase(context)
                        val healthChecker = HealthChecker(context, database.smsQueueDao())
                        DiagnosticsScreen(
                            healthChecker = healthChecker,
                            logDao = database.logDao()
                        )
                    }
                    "Ustawienia" -> SettingsScreen(onBack = { selectedPage = "Dashboard" })
                    else -> DashboardScreen(onNavigateToHistory = { selectedPage = "Historia SMS" })
                }
            }
        }
    } else {
        // Mobile - Sidebar w drawer
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Sidebar(selectedPage = selectedPage, onPageSelected = {
                        selectedPage = it
                        scope.launch { drawerState.close() }
                    })
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    title = selectedPage,
                    showMenuIcon = true,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
                when (selectedPage) {
                    "Dashboard" -> DashboardScreen(
                        onNavigateToHistory = { selectedPage = "Historia SMS" },
                        onNavigateToSettings = { selectedPage = "Ustawienia" }
                    )
                    "Historia SMS" -> HistoryScreen(onBack = { selectedPage = "Dashboard" })
                    "Wyślij SMS" -> PlaceholderScreen("Wyślij SMS")
                    "Diagnostyka" -> {
                        val context = LocalContext.current
                        val database = SmsDatabase.getDatabase(context)
                        val healthChecker = HealthChecker(context, database.smsQueueDao())
                        DiagnosticsScreen(
                            healthChecker = healthChecker,
                            logDao = database.logDao()
                        )
                    }
                    "Ustawienia" -> SettingsScreen(onBack = { selectedPage = "Dashboard" })
                    else -> DashboardScreen(onNavigateToHistory = { selectedPage = "Historia SMS" })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String, showMenuIcon: Boolean, onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (showMenuIcon) {
                IconButton(onClick = onMenuClick) {
                    Text("☰", fontSize = androidx.compose.ui.unit.TextUnit.Unspecified)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CardBg,
            titleContentColor = Foreground
        )
    )
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}