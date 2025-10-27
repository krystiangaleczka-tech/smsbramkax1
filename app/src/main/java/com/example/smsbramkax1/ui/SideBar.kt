package com.example.smsbramkax1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SideBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(240.dp)
            .background(color = MaterialTheme.colorScheme.surface),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "SMS Gateway X1", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))
            SideBarItem("Dashboard", selectedItem == "Dashboard", onItemSelected)
            SideBarItem("Historia SMS", selectedItem == "Historia SMS", onItemSelected)
            SideBarItem("Wyślij SMS", selectedItem == "Wyślij SMS", onItemSelected)
            SideBarItem("Ustawienia", selectedItem == "Ustawienia", onItemSelected)
        }
    }
}

@Composable
fun SideBarItem(name: String, selected: Boolean, onItemSelected: (String) -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    Text(
        text = name,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(color = background)
            .clickable { onItemSelected(name) },
        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyLarge
    )
}