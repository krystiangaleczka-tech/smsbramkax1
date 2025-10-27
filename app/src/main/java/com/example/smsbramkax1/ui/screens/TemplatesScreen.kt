package com.example.smsbramkax1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smsbramkax1.data.SmsTemplate
import com.example.smsbramkax1.sms.TemplateManager
import com.example.smsbramkax1.ui.components.QuickActions
import com.example.smsbramkax1.ui.components.Action
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    templateManager: TemplateManager,
    onNavigateBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTemplate by remember { mutableStateOf<SmsTemplate?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val templates by if (searchQuery.isBlank()) {
        templateManager.getAllTemplates().collectAsStateWithLifecycle(initialValue = emptyList())
    } else {
        templateManager.searchTemplates(searchQuery).collectAsStateWithLifecycle(initialValue = emptyList())
    }
    
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szablony SMS") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj szablon")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj szablon")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Szukaj szablonów...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Szukaj")
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick actions
            QuickActions(
                actions = listOf(
                    Action(
                        icon = Icons.Default.Add,
                        text = "Dodaj szablon",
                        onClick = { showAddDialog = true }
                    ),
                    Action(
                        icon = Icons.Default.Refresh,
                        text = "Odśwież",
                        onClick = { /* Refresh handled by Flow */ }
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Templates list
            if (templates.isEmpty()) {
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
                            text = if (searchQuery.isBlank()) "Brak szablonów" else "Brak wyników",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "Dodaj pierwszy szablon" else "Spróbuj inne słowa kluczowe",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(templates) { template ->
                        TemplateCard(
                            template = template,
                            onEdit = { 
                                selectedTemplate = template
                                showEditDialog = true 
                            },
                            onDelete = { 
                                coroutineScope.launch {
                                    templateManager.deleteTemplate(template.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddTemplateDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, content, category ->
                coroutineScope.launch {
                    templateManager.createTemplate(name, content, category)
                }
                showAddDialog = false
            }
        )
    }
    
    if (showEditDialog && selectedTemplate != null) {
        EditTemplateDialog(
            template = selectedTemplate!!,
            onDismiss = { 
                showEditDialog = false
                selectedTemplate = null
            },
            onUpdate = { name, content, category ->
                coroutineScope.launch {
                    templateManager.updateTemplate(
                        selectedTemplate!!.id,
                        name,
                        content,
                        category
                    )
                }
                showEditDialog = false
                selectedTemplate = null
            }
        )
    }
}

@Composable
private fun TemplateCard(
    template: SmsTemplate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                template.category?.let { category ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Text(
                text = template.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Show variables if any
            template.variables?.let { variablesJson ->
                val templateEngine = com.example.smsbramkax1.sms.TemplateEngine()
                val variables = templateEngine.variablesFromJson(variablesJson)
                if (variables.isNotEmpty()) {
                    Text(
                        text = "Zmienne: ${variables.joinToString(", ") { "{{$it}}" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edytuj",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Usuń",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTemplateDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, content: String, category: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    
    val templateEngine = com.example.smsbramkax1.sms.TemplateEngine()
    val validation = templateEngine.validateTemplate(content)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj szablon") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa szablonu") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Treść szablonu") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    supportingText = {
                        Text(
                            text = "Użyj zmiennych w formacie {{nazwa}}, np. {{name}}, {{time}}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
                
                // Show extracted variables
                val variables = templateEngine.extractVariables(content)
                if (variables.isNotEmpty()) {
                    Text(
                        text = "Wykryte zmienne: ${variables.joinToString(", ") { "{{$it}}" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategoria (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("np. Przypomnienia, Promocje") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, content, category.ifBlank { null }) },
                enabled = name.isNotBlank() && content.isNotBlank() && validation.isValid
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
private fun EditTemplateDialog(
    template: SmsTemplate,
    onDismiss: () -> Unit,
    onUpdate: (name: String, content: String, category: String?) -> Unit
) {
    var name by remember { mutableStateOf(template.name) }
    var content by remember { mutableStateOf(template.content) }
    var category by remember { mutableStateOf(template.category ?: "") }
    
    val templateEngine = com.example.smsbramkax1.sms.TemplateEngine()
    val validation = templateEngine.validateTemplate(content)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj szablon") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa szablonu") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Treść szablonu") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                    supportingText = {
                        Text(
                            text = "Użyj zmiennych w formacie {{nazwa}}, np. {{name}}, {{time}}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
                
                // Show extracted variables
                val variables = templateEngine.extractVariables(content)
                if (variables.isNotEmpty()) {
                    Text(
                        text = "Wykryte zmienne: ${variables.joinToString(", ") { "{{$it}}" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategoria (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("np. Przypomnienia, Promocje") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onUpdate(name, content, category.ifBlank { null }) },
                enabled = name.isNotBlank() && content.isNotBlank() && validation.isValid
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