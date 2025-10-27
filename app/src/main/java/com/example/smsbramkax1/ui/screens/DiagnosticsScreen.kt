package com.example.smsbramkax1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smsbramkax1.R
import com.example.smsbramkax1.data.SystemLog
import com.example.smsbramkax1.utils.HealthChecker
import com.example.smsbramkax1.utils.HealthStatus
import com.example.smsbramkax1.utils.NetworkState
import com.example.smsbramkax1.storage.LogDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Ekran diagnostyki systemu SMS Gateway.
 * 
 * ANDROID EXPERT NOTES:
 * - Używamy rememberCoroutineScope do bezpiecznego wywoływania suspend funkcji z Compose
 * - Stan jest zarządzany przez mutableStateOf, co zapewnia reaktywność UI
 * - Formatowanie daty używa SimpleDateFormat z locale dla spójności
 * - LazyColumn dla wydajnego wyświetlania długich list logów
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    healthChecker: HealthChecker,
    logDao: LogDao
) {
    val scope = rememberCoroutineScope()
    var health by remember { mutableStateOf<HealthStatus?>(null) }
    var logs by remember { mutableStateOf<List<SystemLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Funkcja do odświeżania danych
    fun refreshData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                health = healthChecker.snapshot()
                logs = logDao.getRecentLogs(50)
            } catch (e: Exception) {
                errorMessage = "Błąd ładowania danych: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Załaduj dane przy pierwszym wyświetleniu
    LaunchedEffect(Unit) {
        refreshData()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Nagłówek z przyciskiem odświeżania
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Diagnostyka",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { refreshData() },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Odśwież"
                )
            }
        }
        
        Spacer(Modifier.height(12.dp))
        
        // Komunikat błędu
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(Modifier.height(12.dp))
        }
        
        // Stan zdrowia systemu
        health?.let { status ->
            HealthStatusCard(status = status)
            Spacer(Modifier.height(16.dp))
        }
        
        // Ostatnie logi
        Text(
            text = "Ostatnie logi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        
        if (isLoading && logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (logs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Brak logów do wyświetlenia",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs) { log ->
                    LogItem(log = log)
                }
            }
        }
    }
}

/**
 * Karta wyświetlająca status zdrowia systemu
 */
@Composable
private fun HealthStatusCard(status: HealthStatus) {
    val dateFormat = remember { SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !status.isHealthy -> MaterialTheme.colorScheme.errorContainer
                status.needsAttention -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Status systemu: ${status.statusDescription}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    !status.isHealthy -> MaterialTheme.colorScheme.onErrorContainer
                    status.needsAttention -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                }
            )
            
            Spacer(Modifier.height(12.dp))
            
            // Podstawowe statystyki
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Oczekujące", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${status.pendingCount}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text("Błędy (1h)", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${status.failedLastHour}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (status.failedLastHour > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Column {
                    Text("Wysłane", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${status.totalSent}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Szczegóły połączenia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sieć: ${if (status.isNetworkOk) "OK" else "Brak"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (status.isNetworkOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                
                Text(
                    text = when {
                        status.isWifi -> "WiFi"
                        status.isMobile -> "Mobile"  
                        else -> "Brak"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                status.lastSendTimestamp?.let { timestamp ->
                    Text(
                        text = "Ostatni: ${dateFormat.format(Date(timestamp))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                } ?: Text(
                    text = "Brak wysłanych",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Dodatkowy test sieciowy
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Test sieci:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = if (status.isNetworkOk) "✅ Dostępna" else "❌ Niedostępna",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (status.isNetworkOk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Element listy logów
 */
@Composable
private fun LogItem(log: SystemLog) {
    val dateFormat = remember { SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${log.level} • ${log.category}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = when (log.level) {
                        "ERROR" -> MaterialTheme.colorScheme.error
                        "WARN" -> MaterialTheme.colorScheme.error
                        "INFO" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Text(
                    text = dateFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
        }
    }
}