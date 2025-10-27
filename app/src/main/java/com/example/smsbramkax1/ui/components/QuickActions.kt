package com.example.smsbramkax1.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsbramkax1.ui.theme.*

@Composable
fun QuickActions() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Szybkie akcje",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Foreground
            )
            Text(
                "Najczęściej używane funkcje",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Grid 2x2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton("📤", "Wyślij testowy SMS", isPrimary = true, Modifier.weight(1f))
                ActionButton("📊", "Zobacz historię", isPrimary = false, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton("⬇️", "Eksportuj dane", isPrimary = false, Modifier.weight(1f))
                ActionButton("⚙️", "Ustawienia", isPrimary = false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ActionButton(icon: String, label: String, isPrimary: Boolean, modifier: Modifier = Modifier) {
    Button(
        onClick = { /* akcja */ },
        modifier = modifier.height(75.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Primary else CardBg,
            contentColor = if (isPrimary) PrimaryForeground else Foreground
        ),
        border = if (!isPrimary) BorderStroke(1.dp, Border) else null,
        shape = RoundedCornerShape(6.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}