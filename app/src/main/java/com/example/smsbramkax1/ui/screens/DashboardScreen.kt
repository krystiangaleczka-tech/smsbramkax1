package com.example.smsbramkax1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsbramkax1.ui.components.*
import com.example.smsbramkax1.ui.theme.*

@Composable
fun DashboardScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            Text(
                "Dashboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Foreground
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stats Grid (2x2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = "✉️",
                    number = "128",
                    label = "SMS w kolejce",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = "✅",
                    number = "542",
                    label = "Wysłane dzisiaj",
                    sublabel = "Sukces: 99%",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = "⚠️",
                    number = "3",
                    label = "Błędy",
                    sublabel = "Zobacz szczegóły",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = "💾",
                    number = "Aktywny",
                    label = "Status systemu",
                    sublabel = "Uptime: 48h",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Messages Table
        item {
            MessageTable()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Actions
        item {
            QuickActions()
        }
    }
}