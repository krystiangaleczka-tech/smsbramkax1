package com.example.smsbramkax1.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Dashboard() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Dashboard", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard("SMS w kolejce", "24")
            StatCard("Wysłane dzisiaj", "12")
        }
        Spacer(modifier = Modifier.height(16.dp))
                
        // Możesz dodać dalej tabelę i inne elementy na wzór design.md
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(100.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, style = MaterialTheme.typography.headlineMedium)
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
        }
    }
}