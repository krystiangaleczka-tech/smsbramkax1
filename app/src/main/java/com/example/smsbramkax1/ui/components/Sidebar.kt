package com.example.smsbramkax1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsbramkax1.ui.theme.*

@Composable
fun Sidebar(
    selectedPage: String,
    onPageSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp),
        color = CardBg,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Logo Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📱", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "SMS Gateway",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Foreground
                )
            }

            Divider(color = Border)

            // Navigation Items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                NavItem("📊", "Dashboard", selectedPage == "Dashboard") { onPageSelected("Dashboard") }
                NavItem("📜", "Historia SMS", selectedPage == "Historia SMS") { onPageSelected("Historia SMS") }
                NavItem("📤", "Wyślij SMS", selectedPage == "Wyślij SMS") { onPageSelected("Wyślij SMS") }
                NavItem("🔧", "Diagnostyka", selectedPage == "Diagnostyka") { onPageSelected("Diagnostyka") }
                NavItem("⚙️", "Ustawienia", selectedPage == "Ustawienia") { onPageSelected("Ustawienia") }
            }
        }
    }
}

@Composable
fun NavItem(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor = if (selected) Primary else androidx.compose.ui.graphics.Color.Transparent
    val textColor = if (selected) PrimaryForeground else androidx.compose.ui.graphics.Color(0xFF64748B)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}