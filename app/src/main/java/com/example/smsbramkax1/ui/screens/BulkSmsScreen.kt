package com.example.smsbramkax1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smsbramkax1.dto.BulkSmsProgressDTO
import com.example.smsbramkax1.dto.BulkSmsRequestDTO
import com.example.smsbramkax1.dto.BulkSmsStatus
import com.example.smsbramkax1.sms.BulkSmsManager
import com.example.smsbramkax1.ui.components.QuickActions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkSmsScreen(
    bulkSmsManager: BulkSmsManager,
    onNavigateBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProgress by remember { mutableStateOf<BulkSmsProgressDTO?>(null) }
    
    val bulkProgress by bulkSmsManager.bulkProgress.collectAsStateWithLifecycle(initialValue = emptyMap())
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Masowe SMS") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nowa wysyłka")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Nowa wysyłka")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            QuickActions(
                onNavigateToHistory = { /* Navigate to history if needed */ },
                onNavigateToSettings = { /* Navigate to settings if needed */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (bulkProgress.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Brak aktywnych wysyłek",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Rozpocznij nową masową wysyłkę SMS",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(bulkProgress.values.toList()) { progress ->
                        BulkSmsProgressCard(
                            progress = progress,
                            onViewDetails = { selectedProgress = progress },
                            onCancel = { 
                                if (progress.status in listOf(BulkSmsStatus.QUEUED, BulkSmsStatus.PROCESSING)) {
                                    coroutineScope.launch {
                                        bulkSmsManager.cancelBulkSms(progress.batchId)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddBulkSmsDialog(
            onDismiss = { showAddDialog = false },
            onSend = { phoneNumbers, message, delay, batchSize ->
                val request = BulkSmsRequestDTO(
                    phoneNumbers = phoneNumbers,
                    messageBody = message,
                    sendDelayMs = delay,
                    batchSize = batchSize
                )
                coroutineScope.launch {
                    bulkSmsManager.createBulkSms(request)
                }
                showAddDialog = false
            }
        )
    }
    
    selectedProgress?.let { progress ->
        BulkSmsDetailsDialog(
            progress = progress,
            onDismiss = { selectedProgress = null }
        )
    }
}

@Composable
private fun BulkSmsProgressCard(
    progress: BulkSmsProgressDTO,
    onViewDetails: () -> Unit,
    onCancel: () -> Unit
) {
    val statusColor = when (progress.status) {
        BulkSmsStatus.QUEUED -> MaterialTheme.colorScheme.primary
        BulkSmsStatus.PROCESSING -> MaterialTheme.colorScheme.secondary
        BulkSmsStatus.COMPLETED -> MaterialTheme.colorScheme.tertiary
        BulkSmsStatus.FAILED -> MaterialTheme.colorScheme.error
        BulkSmsStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    
    val statusText = when (progress.status) {
        BulkSmsStatus.QUEUED -> "W kolejce"
        BulkSmsStatus.PROCESSING -> "W trakcie"
        BulkSmsStatus.COMPLETED -> "Zakończone"
        BulkSmsStatus.FAILED -> "Błąd"
        BulkSmsStatus.CANCELLED -> "Anulowano"
    }
    
    val progressPercentage = if (progress.totalRecipients > 0) {
        (progress.sentCount + progress.failedCount).toFloat() / progress.totalRecipients.toFloat()
    } else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batch: ${progress.batchId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
            
            // Progress bar
            LinearProgressIndicator(
                progress = { progressPercentage },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Wysłane: ${progress.sentCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Błędy: ${progress.failedCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Razem: ${progress.totalRecipients}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onViewDetails) {
                    Text("Szczegóły")
                }
                
                if (progress.status in listOf(BulkSmsStatus.QUEUED, BulkSmsStatus.PROCESSING)) {
                    TextButton(onClick = onCancel) {
                        Text("Anuluj")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBulkSmsDialog(
    onDismiss: () -> Unit,
    onSend: (phoneNumbers: List<String>, message: String, delay: Long, batchSize: Int) -> Unit
) {
    var phoneNumbersText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var delay by remember { mutableStateOf("500") }
    var batchSize by remember { mutableStateOf("10") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Masowa wysyłka SMS") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = phoneNumbersText,
                    onValueChange = { phoneNumbersText = it },
                    label = { Text("Numery telefonów") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    supportingText = {
                        Text(
                            text = "Wprowadź numery, każdy w nowej linii",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Treść wiadomości") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = delay,
                        onValueChange = { delay = it },
                        label = { Text("Opóźnienie (ms)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = batchSize,
                        onValueChange = { batchSize = it },
                        label = { Text("Rozmiar batcha") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val phoneNumbers = phoneNumbersText.split("\n")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    
                    val delayMs = delay.toLongOrNull() ?: 500L
                    val size = batchSize.toIntOrNull() ?: 10
                    
                    onSend(phoneNumbers, message, delayMs, size)
                },
                enabled = phoneNumbersText.isNotBlank() && message.isNotBlank()
            ) {
                Text("Wyślij")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BulkSmsDetailsDialog(
    progress: BulkSmsProgressDTO,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Szczegóły wysyłki") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Batch ID: ${progress.batchId}")
                Text("Status: ${progress.status}")
                Text("Odbiorcy: ${progress.totalRecipients}")
                Text("Wysłane: ${progress.sentCount}")
                Text("Błędy: ${progress.failedCount}")
                Text("W kolejce: ${progress.queuedCount}")
                
                if (progress.errors.isNotEmpty()) {
                    Text(
                        text = "Błędy:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    progress.errors.take(5).forEach { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (progress.errors.size > 5) {
                        Text(
                            text = "... i ${progress.errors.size - 5} więcej",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Zamknij")
            }
        }
    )
}