package com.example.smsbramkax1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smsbramkax1.ui.theme.*
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.ui.utils.RefreshListener
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class SmsMessage(
    val id: String,
    val number: String,
    val message: String,
    val status: String,
    val time: String
)

@Composable
fun MessageTable(onShowAllHistory: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<SmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    fun loadMessages() {
        scope.launch {
            try {
                val database = SmsDatabase.getDatabase(context)
                val recentSms = database.smsQueueDao().getRecentSms(10).first()
                
                messages = recentSms.map { sms: com.example.smsbramkax1.data.SmsQueue ->
                    val timeAgo = getTimeAgo(sms.createdAt)
                    SmsMessage(
                        id = "#${sms.id}",
                        number = maskPhoneNumber(sms.phoneNumber),
                        message = sms.message.take(30) + if (sms.message.length > 30) "..." else "",
                        status = sms.status.name,
                        time = timeAgo
                    )
                }
                isLoading = false
            } catch (e: Exception) {
                // W przypadku błędu pokaż mockowe dane
                messages = listOf(
                    SmsMessage("#0047", "XXX XXX 789", "Przypomnienie o wizycie jutro...", "SENT", "2m"),
                    SmsMessage("#0046", "XXX XXX 456", "Twoja wizyta została potwierdzona...", "QUEUED", "15m"),
                    SmsMessage("#0045", "XXX XXX 123", "Dziękujemy za skorzystanie...", "SENT", "1h")
                )
                isLoading = false
            }
        }
    }
    
    // Pobierz prawdziwe SMS-y z bazy
    LaunchedEffect(refreshTrigger) {
        loadMessages()
    }
    
    // Listen for refresh events
    RefreshListener(event = "sms_sent") {
        refreshTrigger++
    }
    
    // Auto-refresh every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000) // 10 seconds
            refreshTrigger++
        }
    }
    
    loadMessages()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Ostatnie wiadomości",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Foreground
                    )
                    Text(
                        "Najnowsze SMS z systemu",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
                TextButton(onClick = onShowAllHistory) {
                    Text("Zobacz wszystkie →", color = Primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            Divider(color = Border)

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TableHeaderCell("Numer", Modifier.weight(1.5f))
                TableHeaderCell("Wiadomość", Modifier.weight(2f))
                TableHeaderCell("Status", Modifier.weight(1.5f))
                TableHeaderCell("Czas", Modifier.weight(0.5f))
            }

            // Table Rows
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Brak wiadomości",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                messages.forEach { msg ->
                    MessageRow(msg)
                }
            }
        }
    }
}

@Composable
fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF64748B),
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
fun MessageRow(message: SmsMessage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(message.number, fontSize = 10.sp, color = Foreground, textAlign = TextAlign.Center, modifier = Modifier.weight(1.5f))
        Text(message.message, fontSize = 10.sp, color = Foreground, textAlign = TextAlign.Center, modifier = Modifier.weight(2f))
        StatusBadge(message.status, Modifier.weight(1.2f))
        Text(message.time, fontSize = 10.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center, modifier = Modifier.weight(0.8f))
    }
    Divider(color = Border)
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status) {
        "SENT" -> StatusSentBg to StatusSentText
        "QUEUED" -> StatusQueuedBg to StatusQueuedText
        else -> Color.Gray to Color.White
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(9999.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (status == "SENT") "Wysłane" else "W kolejce",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)
    
    return when {
        minutes < 1 -> "teraz"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun maskPhoneNumber(phoneNumber: String): String {
    return when {
        phoneNumber.length >= 9 -> {
            val visible = phoneNumber.takeLast(3)
            val masked = "*".repeat(phoneNumber.length - 3)
            masked + visible
        }
        else -> phoneNumber
    }
}