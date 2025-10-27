package com.example.smsbramkax1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smsbramkax1.data.ScheduledSmsStatus
import com.example.smsbramkax1.sms.ScheduledSmsManager
import com.example.smsbramkax1.ui.components.QuickActions
import com.example.smsbramkax1.ui.components.Action
import com.example.smsbramkax1.utils.DateUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledSmsScreen(
    scheduledSmsManager: ScheduledSmsManager,
    onNavigateBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedSms by remember { mutableStateOf<com.example.smsbramkax1.data.ScheduledSms?>(null) }
    
    val scheduledSms by scheduledSmsManager.getAllScheduledSms().collectAsStateWithLifecycle(initialValue = emptyList())
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zaplanowane SMS") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj SMS")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj zaplanowany SMS")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                QuickActions(
                    actions = listOf(
                        Action(
                            icon = Icons.Default.Add,
                            text = "Dodaj SMS",
                            onClick = { showAddDialog = true }
                        ),
                        Action(
                            icon = Icons.Default.Refresh,
                            text = "Odśwież",
                            onClick = { /* Refresh handled by Flow */ }
                        )
                    )
                )
            }
            
            if (scheduledSms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Brak zaplanowanych SMS",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Dodaj pierwszy zaplanowany SMS",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(scheduledSms) { sms ->
                    ScheduledSmsCard(
                        sms = sms,
                        onEdit = { 
                            selectedSms = sms
                            showEditDialog = true 
                        },
                        onCancel = { 
                            coroutineScope.launch {
                                scheduledSmsManager.cancelScheduledSms(sms.id)
                            }
                        }
                    )
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddScheduledSmsDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, phone, message, scheduledFor ->
                coroutineScope.launch {
                    scheduledSmsManager.scheduleSms(name, phone, message, scheduledFor)
                }
                showAddDialog = false
            }
        )
    }
    
    if (showEditDialog && selectedSms != null) {
        EditScheduledSmsDialog(
            sms = selectedSms!!,
            onDismiss = { 
                showEditDialog = false
                selectedSms = null
            },
            onUpdate = { name, phone, message, scheduledFor ->
                coroutineScope.launch {
                    scheduledSmsManager.updateScheduledSms(
                        selectedSms!!.id,
                        name,
                        phone,
                        message,
                        scheduledFor
                    )
                }
                showEditDialog = false
                selectedSms = null
            }
        )
    }
}

@Composable
private fun ScheduledSmsCard(
    sms: com.example.smsbramkax1.data.ScheduledSms,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    val statusColor = when (sms.status) {
        ScheduledSmsStatus.SCHEDULED -> MaterialTheme.colorScheme.primary
        ScheduledSmsStatus.QUEUED -> MaterialTheme.colorScheme.secondary
        ScheduledSmsStatus.SENT -> MaterialTheme.colorScheme.tertiary
        ScheduledSmsStatus.DELIVERED -> MaterialTheme.colorScheme.primary
        ScheduledSmsStatus.DELETED -> MaterialTheme.colorScheme.error
        ScheduledSmsStatus.FAILED -> MaterialTheme.colorScheme.error
    }
    
    val statusText = when (sms.status) {
        ScheduledSmsStatus.SCHEDULED -> "Zaplanowany"
        ScheduledSmsStatus.QUEUED -> "W kolejce"
        ScheduledSmsStatus.SENT -> "Wysłany"
        ScheduledSmsStatus.DELIVERED -> "Dostarczony"
        ScheduledSmsStatus.DELETED -> "Usunięty"
        ScheduledSmsStatus.FAILED -> "Błąd"
    }
    
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
                    text = sms.name,
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
            
            Text(
                text = sms.phoneNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = sms.messageBody,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Planowane: ${DateUtils.formatDateTime(sms.scheduledFor)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (sms.status == ScheduledSmsStatus.SCHEDULED) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edytuj",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onCancel) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Anuluj",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScheduledSmsDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, message: String, scheduledFor: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj zaplanowany SMS") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Numer telefonu") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Treść wiadomości") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    label = { Text("Data (RRRR-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("2024-12-31") }
                )
                
                OutlinedTextField(
                    value = selectedTime,
                    onValueChange = { selectedTime = it },
                    label = { Text("Godzina (GG:MM)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("14:30") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val dateTime = "$selectedDate $selectedTime"
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val scheduledFor = try {
                        formatter.parse(dateTime)?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    
                    onAdd(name, phone, message, scheduledFor)
                },
                enabled = name.isNotBlank() && phone.isNotBlank() && message.isNotBlank() && 
                         selectedDate.isNotBlank() && selectedTime.isNotBlank()
            ) {
                Text("Dodaj")
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
private fun EditScheduledSmsDialog(
    sms: com.example.smsbramkax1.data.ScheduledSms,
    onDismiss: () -> Unit,
    onUpdate: (name: String, phone: String, message: String, scheduledFor: Long) -> Unit
) {
    var name by remember { mutableStateOf(sms.name) }
    var phone by remember { mutableStateOf(sms.phoneNumber) }
    var message by remember { mutableStateOf(sms.messageBody) }
    
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val initialDateTime = formatter.format(Date(sms.scheduledFor))
    var dateTime by remember { mutableStateOf(initialDateTime) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj zaplanowany SMS") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Numer telefonu") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Treść wiadomości") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                OutlinedTextField(
                    value = dateTime,
                    onValueChange = { dateTime = it },
                    label = { Text("Data i godzina (RRRR-MM-DD GG:MM)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val scheduledFor = try {
                        formatter.parse(dateTime)?.time ?: sms.scheduledFor
                    } catch (e: Exception) {
                        sms.scheduledFor
                    }
                    
                    onUpdate(name, phone, message, scheduledFor)
                },
                enabled = name.isNotBlank() && phone.isNotBlank() && message.isNotBlank() && 
                         dateTime.isNotBlank()
            ) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}