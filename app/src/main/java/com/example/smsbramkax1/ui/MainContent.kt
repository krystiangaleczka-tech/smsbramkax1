package com.example.smsbramkax1.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(selectedTab: String, onTabSelected: (String) -> Unit) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { /* implement Bottom Navigation jeśli potrzebujesz */ }
    ) { paddingValues ->
        Row(Modifier.fillMaxSize().padding(paddingValues)) {
            SideBar(selectedItem = selectedTab, onItemSelected = onTabSelected)
            when (selectedTab) {
                "Dashboard" -> Dashboard()
                "Historia SMS" -> Text("Strona historii SMS")
                "Wyślij SMS" -> Text("Strona wysyłania SMS")
                "Ustawienia" -> Text("Strona ustawień")
                else -> Text("Nieznana strona")
            }
        }
    }
}