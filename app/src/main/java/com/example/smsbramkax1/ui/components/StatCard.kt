package com.example.smsbramkax1.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsbramkax1.ui.theme.*

@Composable
fun StatCard(
    icon: String,
    number: String,
    label: String,
    sublabel: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(icon, fontSize = 20.sp)
            Column {
                Text(number, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Foreground)
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
                sublabel?.let {
                    Text(it, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Primary)
                }
            }
        }
    }
}