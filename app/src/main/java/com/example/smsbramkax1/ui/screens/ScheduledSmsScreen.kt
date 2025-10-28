package com.example.smsbramkax1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.smsbramkax1.data.SmsMessage
import com.example.smsbramkax1.data.Contact
import com.example.smsbramkax1.sms.ScheduledSmsManager
import com.example.smsbramkax1.ui.components.QuickActions
import com.example.smsbramkax1.ui.components.Action
import com.example.smsbramkax1.ui.components.CountryCodePhoneFieldWithContact
import com.example.smsbramkax1.ui.components.ContactPickerDialog
import com.example.smsbramkax1.ui.components.defaultCountries
import com.example.smsbramkax1.ui.components.Country
import com.example.smsbramkax1.utils.DateUtils
import com.example.smsbramkax1.utils.ContactManager
import com.example.smsbramkax1.utils.PermissionsManager
import com.example.smsbramkax1.workers.ProcessScheduledSmsWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduledSmsScreen(
    scheduledSmsManager: ScheduledSmsManager,
    contactManager: ContactManager,
    permissionsManager: PermissionsManager,
    onNavigateBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedSms by remember { mutableStateOf<SmsMessage?>(null) }

    val scheduledSms by scheduledSmsManager.getAllScheduledSms().collectAsStateWithLifecycle(initialValue = emptyList())
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Contact picker state
    var showContactPicker by remember { mutableStateOf(false) }
    var contactPickerMode by remember { mutableStateOf("add") }
    var selectedContactForEdit by remember { mutableStateOf<SmsMessage?>(null) }
    
    // Contacts state
    val contacts by contactManager.getAllContacts().collectAsStateWithLifecycle(initialValue = emptyList())
    var isLoadingContacts by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zaplanowane SMS") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
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
                            text = "Przetwórz teraz",
                            onClick = {
                                val workRequest = OneTimeWorkRequestBuilder<ProcessScheduledSmsWorker>().build()
                                WorkManager.getInstance(context).enqueue(workRequest)
                            }
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
                    val result = scheduledSmsManager.scheduleSms(name, phone, message, scheduledFor)
                    if (result.isSuccess) {
                        // If SMS is scheduled for immediate sending, process it now
                        if (scheduledFor <= System.currentTimeMillis() + 60000) { // within 1 minute
                            val workRequest = OneTimeWorkRequestBuilder<ProcessScheduledSmsWorker>().build()
                            WorkManager.getInstance(context).enqueue(workRequest)
                        }
                    }
                }
                showAddDialog = false
            },
            contactManager = contactManager,
            permissionsManager = permissionsManager
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
            },
            contactManager = contactManager,
            permissionsManager = permissionsManager
        )
    }
}

@Composable
private fun ScheduledSmsCard(
    sms: SmsMessage,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    val statusColor = when (sms.status) {
        "SCHEDULED" -> MaterialTheme.colorScheme.primary
        "QUEUED" -> MaterialTheme.colorScheme.secondary
        "SENT" -> MaterialTheme.colorScheme.tertiary
        "DELIVERED" -> MaterialTheme.colorScheme.primary
        "DELETED" -> MaterialTheme.colorScheme.error
        "FAILED" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val statusText = when (sms.status) {
        "SCHEDULED" -> "Zaplanowany"
        "QUEUED" -> "W kolejce"
        "SENT" -> "Wysłany"
        "DELIVERED" -> "Dostarczony"
        "DELETED" -> "Usunięty"
        "FAILED" -> "Błąd"
        else -> sms.status
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
                    text = sms.category ?: "Bez nazwy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status icon
                        when (sms.status) {
                            "SCHEDULED" -> Icon(
                                Icons.Default.Info,
                                contentDescription = "Zaplanowany",
                                modifier = Modifier.size(12.dp),
                                tint = statusColor
                            )
                            "QUEUED" -> Icon(
                                Icons.Default.Info,
                                contentDescription = "W kolejce",
                                modifier = Modifier.size(12.dp),
                                tint = statusColor
                            )
                            "SENT" -> Icon(
                                Icons.Default.Check,
                                contentDescription = "Wysłany",
                                modifier = Modifier.size(12.dp),
                                tint = statusColor
                            )
                            "DELIVERED" -> Icon(
                                Icons.Default.Check,
                                contentDescription = "Dostarczony",
                                modifier = Modifier.size(12.dp),
                                tint = statusColor
                            )
                            "FAILED" -> Icon(
                                Icons.Default.Close,
                                contentDescription = "Błąd",
                                modifier = Modifier.size(12.dp),
                                tint = statusColor
                            )
                            else -> Icon(
                                Icons.Default.Info,
                                contentDescription = sms.status,
                                modifier = Modifier.size(12.dp),
                                tint = statusColor
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    }
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
            
            // Date information
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Planowane: ${DateUtils.formatDateTime(sms.scheduledFor ?: System.currentTimeMillis())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Show sent date if SMS was sent
                if (sms.status in listOf("SENT", "DELIVERED") && sms.sentAt != null) {
                    Text(
                        text = "Wysłano: ${DateUtils.formatDateTime(sms.sentAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (sms.status == "SCHEDULED") {
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
                } else if (sms.status == "FAILED") {
                    // Retry button for failed SMS
                    IconButton(
                        onClick = {
                            // TODO: Implement retry functionality
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Ponów",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
    onAdd: (name: String, phone: String, message: String, scheduledFor: Long) -> Unit,
    contactManager: ContactManager,
    permissionsManager: PermissionsManager
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { 
        mutableStateOf(defaultCountries.first { it.code == "+48" }) 
    }
    var message by remember { mutableStateOf("") }
    
    // Contact picker state
    var showContactPicker by remember { mutableStateOf(false) }
    val contacts by contactManager.getAllContacts().collectAsStateWithLifecycle(initialValue = emptyList())
    var isLoadingContacts by remember { mutableStateOf(false) }
    
    // Permission launcher for contacts
    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Load contacts after permission granted
            isLoadingContacts = true
            // Note: In a real implementation, you'd load contacts here
            isLoadingContacts = false
        }
    }

    // Date and Time picker states
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }
    var selectedTime by remember { mutableStateOf<Calendar?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )

    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )
    
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
                
                CountryCodePhoneFieldWithContact(
                    selectedCountry = selectedCountry,
                    onCountrySelected = { selectedCountry = it },
                    phoneNumber = phoneNumber,
                    onPhoneNumberChanged = { phoneNumber = it },
                    onContactSelected = { 
                        // Check permissions first
                        val missingPermissions = permissionsManager.getMissingContactPermissions()
                        if (missingPermissions.isEmpty()) {
                            showContactPicker = true
                        } else {
                            contactPermissionLauncher.launch(missingPermissions.toTypedArray())
                        }
                    },
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

                // Date Picker Field
                OutlinedTextField(
                    value = selectedDate?.let {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.time)
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Wybierz datę") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Wybierz datę")
                        }
                    }
                )

                // Time Picker Field
                OutlinedTextField(
                    value = selectedTime?.let {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it.time)
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Godzina") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Wybierz godzinę") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Clear, contentDescription = "Wybierz godzinę")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val scheduledFor = if (selectedDate != null && selectedTime != null) {
                        // Create calendar with selected date
                        val dateCalendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDate!!.timeInMillis
                        }

                        // Set time from selected time
                        dateCalendar.set(Calendar.HOUR_OF_DAY, selectedTime!!.get(Calendar.HOUR_OF_DAY))
                        dateCalendar.set(Calendar.MINUTE, selectedTime!!.get(Calendar.MINUTE))
                        dateCalendar.set(Calendar.SECOND, 0)
                        dateCalendar.set(Calendar.MILLISECOND, 0)

                        dateCalendar.timeInMillis
                    } else {
                        System.currentTimeMillis()
                    }

                    val fullPhoneNumber = selectedCountry.code + phoneNumber
                    onAdd(name, fullPhoneNumber, message, scheduledFor)
                },
                enabled = name.isNotBlank() && phoneNumber.isNotBlank() && message.isNotBlank() &&
                         selectedDate != null && selectedTime != null
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

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = Calendar.getInstance().apply {
                            timeInMillis = datePickerState.selectedDateMillis ?: calendar.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuluj")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Wybierz godzinę") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Create a calendar with only the selected time components
                        selectedTime = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
    
    // Contact Picker Dialog
    if (showContactPicker) {
        ContactPickerDialog(
            contacts = contacts,
            onDismiss = { showContactPicker = false },
            onContactSelected = { contact ->
                // Set name and phone number from selected contact
                name = contact.name ?: ""
                
                val contactPhone = contact.phoneNumber ?: ""
                
                // Extract country code and phone number
                var countryFound = false
                for (country in defaultCountries) {
                    if (contactPhone.startsWith(country.code)) {
                        selectedCountry = country
                        phoneNumber = contactPhone.removePrefix(country.code)
                        countryFound = true
                        break
                    }
                }
                
                // If no country code found, assume Polish number and clean it
                if (!countryFound) {
                    selectedCountry = defaultCountries.first { it.code == "+48" }
                    // Remove all non-digits and handle different formats
                    val cleanPhone = contactPhone.replace(Regex("[^0-9]"), "")
                    phoneNumber = when {
                        cleanPhone.startsWith("48") && cleanPhone.length >= 11 -> cleanPhone.substring(2) // +48 format
                        cleanPhone.length >= 9 -> cleanPhone.take(9) // Polish local format or fallback
                        else -> cleanPhone // Use whatever is left
                    }
                }
                
                showContactPicker = false
            },
            isLoading = isLoadingContacts
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditScheduledSmsDialog(
    sms: SmsMessage,
    onDismiss: () -> Unit,
    onUpdate: (name: String, phone: String, message: String, scheduledFor: Long) -> Unit,
    contactManager: ContactManager,
    permissionsManager: PermissionsManager
) {
    var name by remember { mutableStateOf(sms.category ?: "") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountry by remember { 
        mutableStateOf(defaultCountries.first { it.code == "+48" }) 
    }
    var message by remember { mutableStateOf(sms.messageBody) }
    
    // Contact picker state
    var showContactPicker by remember { mutableStateOf(false) }
    val contacts by contactManager.getAllContacts().collectAsStateWithLifecycle(initialValue = emptyList())
    var isLoadingContacts by remember { mutableStateOf(false) }
    
    // Permission launcher for contacts
    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Load contacts after permission granted
            isLoadingContacts = true
            // Note: In a real implementation, you'd load contacts here
            isLoadingContacts = false
        }
    }
    
    // Extract phone number without country code
    LaunchedEffect(sms.phoneNumber) {
        defaultCountries.forEach { country ->
            if (sms.phoneNumber.startsWith(country.code)) {
                selectedCountry = country
                phoneNumber = sms.phoneNumber.removePrefix(country.code)
                return@forEach
            }
        }
    }

    // Date and Time picker states
    val calendar = Calendar.getInstance().apply { timeInMillis = sms.scheduledFor ?: System.currentTimeMillis() }
    var selectedDate by remember { mutableStateOf(calendar) }
    var selectedTime by remember { mutableStateOf(calendar) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = sms.scheduledFor
    )

    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )
    
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
                
                CountryCodePhoneFieldWithContact(
                    selectedCountry = selectedCountry,
                    onCountrySelected = { selectedCountry = it },
                    phoneNumber = phoneNumber,
                    onPhoneNumberChanged = { phoneNumber = it },
                    onContactSelected = { 
                        // Check permissions first
                        val missingPermissions = permissionsManager.getMissingContactPermissions()
                        if (missingPermissions.isEmpty()) {
                            showContactPicker = true
                        } else {
                            contactPermissionLauncher.launch(missingPermissions.toTypedArray())
                        }
                    },
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
                
                // Date Picker Field
                OutlinedTextField(
                    value = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time),
                    onValueChange = {},
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Wybierz datę")
                        }
                    }
                )

                // Time Picker Field
                OutlinedTextField(
                    value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.time),
                    onValueChange = {},
                    label = { Text("Godzina") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Clear, contentDescription = "Wybierz godzinę")
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val scheduledFor = Calendar.getInstance().apply {
                        timeInMillis = selectedDate.timeInMillis
                        set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val fullPhoneNumber = selectedCountry.code + phoneNumber
                    onUpdate(name, fullPhoneNumber, message, scheduledFor)
                },
                enabled = name.isNotBlank() && phoneNumber.isNotBlank() && message.isNotBlank()
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

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDate = Calendar.getInstance().apply {
                            timeInMillis = datePickerState.selectedDateMillis ?: selectedDate.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuluj")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Wybierz godzinę") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Create a calendar with only the selected time components
                        selectedTime = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
    
    // Contact Picker Dialog
    if (showContactPicker) {
        ContactPickerDialog(
            contacts = contacts,
            onDismiss = { showContactPicker = false },
            onContactSelected = { contact ->
                // Set name and phone number from selected contact
                name = contact.name ?: ""
                
                val contactPhone = contact.phoneNumber ?: ""
                
                // Extract country code and phone number
                var countryFound = false
                for (country in defaultCountries) {
                    if (contactPhone.startsWith(country.code)) {
                        selectedCountry = country
                        phoneNumber = contactPhone.removePrefix(country.code)
                        countryFound = true
                        break
                    }
                }
                
                // If no country code found, assume Polish number and clean it
                if (!countryFound) {
                    selectedCountry = defaultCountries.first { it.code == "+48" }
                    // Remove all non-digits and handle different formats
                    val cleanPhone = contactPhone.replace(Regex("[^0-9]"), "")
                    phoneNumber = when {
                        cleanPhone.startsWith("48") && cleanPhone.length >= 11 -> cleanPhone.substring(2) // +48 format
                        cleanPhone.length >= 9 -> cleanPhone.take(9) // Polish local format or fallback
                        else -> cleanPhone // Use whatever is left
                    }
                }
                
                showContactPicker = false
            },
            isLoading = isLoadingContacts
        )
    }
}