package com.example.smsbramkax1.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.example.smsbramkax1.ui.components.Sidebar
import com.example.smsbramkax1.ui.screens.DashboardScreen
import com.example.smsbramkax1.ui.theme.CardBg
import com.example.smsbramkax1.ui.theme.Foreground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    var selectedPage by remember { mutableStateOf("Dashboard") }
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
                    "Dashboard" -> DashboardScreen()
                    "Historia SMS" -> PlaceholderScreen("Historia SMS")
                    "Wyślij SMS" -> PlaceholderScreen("Wyślij SMS")
                    "Ustawienia" -> PlaceholderScreen("Ustawienia")
                    else -> DashboardScreen()
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
                    "Dashboard" -> DashboardScreen()
                    "Historia SMS" -> PlaceholderScreen("Historia SMS")
                    "Wyślij SMS" -> PlaceholderScreen("Wyślij SMS")
                    "Ustawienia" -> PlaceholderScreen("Ustawienia")
                    else -> DashboardScreen()
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