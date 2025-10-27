package com.example.smsbramkax1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smsbramkax1.data.Contact
import com.example.smsbramkax1.ui.components.QuickActions
import com.example.smsbramkax1.utils.ContactManager
import com.example.smsbramkax1.utils.PermissionsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPickerScreen(
    contactManager: ContactManager,
    permissionsManager: PermissionsManager,
    onNavigateBack: () -> Unit,
    onContactsSelected: (List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedContacts by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showSyncDialog by remember { mutableStateOf(false) }
    
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        contacts = contactManager.getContactsWithPhone()
    }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Handle search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            contacts = contactManager.searchContacts(searchQuery)
        } else {
            contacts = contactManager.getContactsWithPhone()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wybierz kontakty") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    if (selectedContacts.isNotEmpty()) {
                        TextButton(
                            onClick = { 
                                onContactsSelected(selectedContacts.toList())
                            }
                        ) {
                            Text("Wybierz (${selectedContacts.size})")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (!permissionsManager.hasContactsPermission()) {
                // Show permission request
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                                Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Wymagane uprawnienie",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Aby wyświetlić kontakty, przyznaj uprawnienie do odczytu kontaktów",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { showSyncDialog = true }
                        ) {
                            Text("Synchronizuj kontakty")
                        }
                    }
                }
            } else {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Szukaj kontaktów...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Szukaj")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showSyncDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Synchronizuj")
                    }
                    Button(
                        onClick = { 
                            selectedContacts = contacts.mapNotNull { it.phoneNumber }.toSet()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Zaznacz wszystkie")
                    }
                    Button(
                        onClick = { selectedContacts = emptySet() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wyczyść")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contacts list
                if (contacts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (searchQuery.isBlank()) "Brak kontaktów" else "Brak wyników",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (searchQuery.isBlank()) "Synchronizuj kontakty z urządzenia" else "Spróbuj inne słowa kluczowe",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(contacts) { contact ->
                            ContactRow(
                                contact = contact,
                                isSelected = contact.phoneNumber in selectedContacts,
                                onToggle = { phoneNumber, selected ->
                                    if (selected) {
                                        selectedContacts += phoneNumber
                                    } else {
                                        selectedContacts -= phoneNumber
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showSyncDialog) {
        SyncContactsDialog(
            onDismiss = { showSyncDialog = false },
            onSync = {
                coroutineScope.launch {
                    contactManager.syncContacts()
                }
                showSyncDialog = false
            }
        )
    }
}

@Composable
private fun ContactRow(
    contact: Contact,
    isSelected: Boolean,
    onToggle: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isSelected,
                onValueChange = { selected ->
                    contact.phoneNumber?.let { phone ->
                        onToggle(phone, selected)
                    }
                },
                role = Role.Checkbox
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { checked ->
                    contact.phoneNumber?.let { phone ->
                        onToggle(phone, checked)
                    }
                }
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                contact.name?.let { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                contact.phoneNumber?.let { phone ->
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SyncContactsDialog(
    onDismiss: () -> Unit,
    onSync: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Synchronizacja kontaktów") },
        text = {
            Text(
                "Czy chcesz zsynchronizować kontakty z urządzenia? " +
                "Spowoduje to pobranie wszystkich kontaktów z telefonu do aplikacji."
            )
        },
        confirmButton = {
            TextButton(onClick = onSync) {
                Text("Synchronizuj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}