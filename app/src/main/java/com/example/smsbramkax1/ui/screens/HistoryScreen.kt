package com.example.smsbramkax1.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.ui.components.StatusBadge
import com.example.smsbramkax1.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HistorySmsMessage(
    val id: Long,
    val phoneNumber: String,
    val message: String,
    val status: String,
    val createdAt: Long,
    val sentAt: Long?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<HistorySmsMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    fun loadMessages() {
        scope.launch {
            isLoading = true
            try {
                val database = SmsDatabase.getDatabase(context)
                val sentSms = database.smsMessageDao().getSentMessages().first()
                
                messages = sentSms.map { sms ->
                    HistorySmsMessage(
                        id = sms.id,
                        phoneNumber = sms.phoneNumber,
                        message = sms.messageBody,
                        status = sms.status,
                        createdAt = sms.createdAt,
                        sentAt = sms.sentAt
                    )
                }.sortedByDescending { it.createdAt }
                
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadMessages()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Historia SMS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Foreground
                    )
                    Text(
                        "Wszystkie wiadomości z systemu",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
            
            IconButton(
                onClick = { loadMessages() },
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Odśwież")
            }
        }
        
        HorizontalDivider(color = Border, thickness = 1.dp)
        
        // Messages List
        if (isLoading && messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (messages.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Brak wiadomości",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Wyślij pierwszą wiadomość, aby zobaczyć ją tutaj",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    HistoryMessageCard(message)
                }
            }
        }
    }
}

@Composable
private fun HistoryMessageCard(message: HistorySmsMessage) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with number and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    message.phoneNumber,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Foreground
                )
                
                StatusBadge(message.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message content
            Text(
                message.message,
                fontSize = 14.sp,
                color = Color(0xFF475569),
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Footer with timestamps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Utworzono:",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        dateFormat.format(Date(message.createdAt)),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
                
                if (message.sentAt != null) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            "Wysłano:",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            dateFormat.format(Date(message.sentAt)),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}