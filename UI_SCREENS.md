# SMS Gateway - UI Screens Documentation

## 📱 UI Architecture Overview

**Design System:** Material 3  
**Framework:** Jetpack Compose  
**Navigation:** Compose Navigation  
**State Management:** Compose State + Flow  

### Responsive Design Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    TABLET (≥840dp)                          │
│ ┌─────────────┐ ┌─────────────────────────────────────────┐ │
│ │   Sidebar   │ │            Main Content                │ │
│ │  Navigation │ │                                     │ │
│ │             │ │         Dashboard/History/etc.        │ │
│ │ - Dashboard │ │                                     │ │
│ │ - History   │ │                                     │ │
│ │ - Bulk SMS  │ │                                     │ │
│ │ - Templates │ │                                     │ │
│ │ - Settings  │ │                                     │ │
│ └─────────────┘ └─────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    MOBILE (<840dp)                          │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │                   Top Bar                               │ │
│ │  ☰  SMS Gateway                    ⚙️                 │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │                                                         │ │
│ │              Main Content                                │ │
│ │          Dashboard/History/etc.                          │ │
│ │                                                         │ │
│ │                                                         │ │
│ └─────────────────────────────────────────────────────────┘ │
│                                                             │
│ ┌─────────────────────────────────────────────────────────┐ │
│ │                   Bottom Bar                            │ │
│ │  📊  📜  📤  ⏰  📝  ⚙️                                 │ │
│ └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏠 DashboardScreen

**Purpose:** Main dashboard with statistics and quick actions  
**Components:** 2x2 statistics grid, message table, quick actions

### Layout Structure

```kotlin
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Grid (2x2)
        item {
            StatisticsGrid(stats = uiState.stats)
        }
        
        // Recent Messages Table
        item {
            RecentMessagesTable(
                messages = uiState.recentMessages,
                onMessageClick = { /* Handle click */ }
            )
        }
        
        // Quick Actions
        item {
            QuickActionsCard(
                onSendSms = { /* Navigate */ },
                onBulkSms = { /* Navigate */ },
                onScheduleSms = { /* Navigate */ }
            )
        }
    }
}
```

### Statistics Grid

**StatCard Components:**
1. **Queue Count:** Messages waiting to be sent
2. **Daily Sent:** Messages sent today with success rate
3. **Error Count:** Failed messages in last 24 hours
4. **System Status:** Overall health indicator

```kotlin
@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### Recent Messages Table

**Features:**
- Real-time data loading with Flow
- Status badges with color coding
- Pull-to-refresh functionality
- Empty state handling

```kotlin
@Composable
fun MessageTable(
    messages: List<SmsMessage>,
    onMessageClick: (SmsMessage) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages) { message ->
            MessageRow(
                message = message,
                onClick = { onMessageClick(message) }
            )
        }
    }
}

@Composable
fun MessageRow(
    message: SmsMessage,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message.phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = message.messageBody.take(50) + if (message.messageBody.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(status = message.status)
        }
    }
}
```

---

## 📜 HistoryScreen

**Purpose:** Complete SMS history with filtering and search  
**Features:** Real-time data, status badges, detailed cards, filtering

### Screen Structure

```kotlin
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search and Filter Bar
        SearchAndFilterBar(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            selectedStatus = selectedStatus,
            onStatusSelected = viewModel::updateStatusFilter
        )
        
        // Messages List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.filteredMessages) { message ->
                HistoryMessageCard(
                    message = message,
                    onClick = { /* Navigate to details */ },
                    onRetry = { if (message.status == "FAILED") viewModel.retryMessage(message.id) }
                )
            }
        }
    }
}
```

### Message Cards

**HistoryMessageCard Features:**
- Detailed message information
- Status-specific actions (retry for failed messages)
- Timestamp formatting
- Error message display

```kotlin
@Composable
fun HistoryMessageCard(
    message: HistorySmsMessage,
    onClick: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with phone number and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                StatusBadge(status = message.status)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message content
            Text(
                text = message.messageBody,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Footer with timestamps and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(message.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Retry button for failed messages
                if (message.status == "FAILED" && onRetry != null) {
                    OutlinedButton(
                        onClick = onRetry,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Retry", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            // Error message display
            message.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error: $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
```

---

## 📤 BulkSmsScreen

**Purpose:** Bulk SMS campaign management  
**Features:** Progress tracking, batch processing, cancellation, validation

### Screen Layout

```kotlin
@Composable
fun BulkSmsScreen(
    navController: NavController,
    viewModel: BulkSmsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Campaign Progress
        if (uiState.activeCampaign != null) {
            item {
                ActiveCampaignCard(
                    campaign = uiState.activeCampaign,
                    onCancel = viewModel::cancelCampaign,
                    onPause = viewModel::pauseCampaign,
                    onResume = viewModel::resumeCampaign
                )
            }
        }
        
        // Add New Bulk SMS
        item {
            AddBulkSmsCard(
                onAddBulkSms = viewModel::addBulkSms
            )
        }
        
        // Campaign History
        items(uiState.campaignHistory) { campaign ->
            CampaignHistoryCard(
                campaign = campaign,
                onClick = { /* Navigate to details */ }
            )
        }
    }
}
```

### Progress Tracking

**BulkSmsProgressDTO Structure:**
```kotlin
data class BulkSmsProgressDTO(
    val batchId: String,
    val totalMessages: Int,
    val sentMessages: Int,
    val failedMessages: Int,
    val pendingMessages: Int,
    val startTime: Long,
    val estimatedEndTime: Long?,
    val status: CampaignStatus // RUNNING, PAUSED, COMPLETED, CANCELLED, FAILED
)
```

**Progress Card Implementation:**
```kotlin
@Composable
fun ActiveCampaignCard(
    campaign: BulkSmsProgressDTO,
    onCancel: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Campaign Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Campaign",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = campaign.status.name)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            val progress = campaign.sentMessages.toFloat() / campaign.totalMessages.toFloat()
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when (campaign.status) {
                    CampaignStatus.RUNNING -> MaterialTheme.colorScheme.primary
                    CampaignStatus.PAUSED -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Total", campaign.totalMessages.toString())
                StatItem("Sent", campaign.sentMessages.toString())
                StatItem("Failed", campaign.failedMessages.toString())
                StatItem("Pending", campaign.pendingMessages.toString())
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (campaign.status) {
                    CampaignStatus.RUNNING -> {
                        OutlinedButton(
                            onClick = onPause,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pause")
                        }
                    }
                    CampaignStatus.PAUSED -> {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Resume")
                        }
                    }
                    else -> {}
                }
                
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
```

---

## ⏰ ScheduledSmsScreen

**Purpose:** Scheduled SMS management with full CRUD operations  
**Features:** DateTime pickers, template integration, real-time updates

### Screen Components

```kotlin
@Composable
fun ScheduledSmsScreen(
    navController: NavController,
    viewModel: ScheduledSmsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Upcoming Scheduled Messages
            item {
                SectionHeader("Upcoming Messages")
            }
            
            items(uiState.upcomingMessages) { message ->
                ScheduledMessageCard(
                    message = message,
                    onEdit = { viewModel.editMessage(message) },
                    onCancel = { viewModel.cancelMessage(message.id) },
                    onSendNow = { viewModel.sendNow(message.id) }
                )
            }
            
            // Past Scheduled Messages
            if (uiState.pastMessages.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader("Past Messages")
                }
                
                items(uiState.pastMessages) { message ->
                    ScheduledMessageCard(
                        message = message,
                        onEdit = { /* Disabled for past messages */ },
                        onCancel = { /* Disabled for past messages */ },
                        onSendNow = { /* Disabled for past messages */ }
                    )
                }
            }
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.showAddDialog() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Scheduled SMS"
            )
        }
    }
    
    // Add/Edit Dialog
    if (showAddDialog) {
        AddScheduledSmsDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onSave = viewModel::saveScheduledMessage
        )
    }
}
```

### Scheduled Message Card

**Features:**
- Status-specific icons (📍 scheduled, ✅ sent, ❌ failed)
- Countdown display for upcoming messages
- Template preview
- Action buttons based on status

```kotlin
@Composable
fun ScheduledMessageCard(
    message: ScheduledSmsMessage,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onSendNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with status and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusIcon(status = message.status)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (message.status) {
                            "SCHEDULED" -> "Scheduled"
                            "SENT" -> "Sent"
                            "FAILED" -> "Failed"
                            else -> message.status
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Action buttons
                Row {
                    when (message.status) {
                        "SCHEDULED" -> {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = onSendNow) {
                                Icon(Icons.Default.Send, contentDescription = "Send Now")
                            }
                            IconButton(onClick = onCancel) {
                                Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Scheduled time
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatDateTime(message.scheduledFor),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (message.status == "SCHEDULED") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "(${getTimeRemaining(message.scheduledFor)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Recipient
            Text(
                text = message.phoneNumber,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Message preview
            Text(
                text = message.messageBody.take(100) + if (message.messageBody.length > 100) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

## 📝 TemplatesScreen

**Purpose:** SMS template management with variable support  
**Features:** CRUD operations, variable extraction, categories, live preview

### Screen Layout

```kotlin
@Composable
fun TemplatesScreen(
    navController: NavController,
    viewModel: TemplatesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Search and Filter
        SearchAndFilterBar(
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            selectedCategory = selectedCategory,
            categories = uiState.categories,
            onCategorySelected = viewModel::updateCategoryFilter
        )
        
        // Templates Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.filteredTemplates) { template ->
                TemplateCard(
                    template = template,
                    onEdit = { viewModel.editTemplate(template) },
                    onDelete = { viewModel.deleteTemplate(template.id) },
                    onUse = { /* Navigate to send SMS with template */ }
                )
            }
        }
    }
}
```

### Template Card

**Features:**
- Template preview with variable highlighting
- Category display
- Variable count indicator
- Quick actions

```kotlin
@Composable
fun TemplateCard(
    template: SmsTemplate,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    template.category?.let { category ->
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Variable count
                template.variables?.let { variables ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = variables.length.toString(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Template preview with variable highlighting
            TemplatePreview(
                content = template.content,
                variables = template.variables
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }
                
                Button(
                    onClick = onUse,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Use")
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
```

### Template Preview with Variable Highlighting

```kotlin
@Composable
fun TemplatePreview(
    content: String,
    variables: Array<String>?
) {
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        
        // Find all variables and highlight them
        val variablePattern = Regex("\\{\\{[^}]+\\}\\}")
        val matches = variablePattern.findAll(content)
        
        matches.forEach { match ->
            // Add text before the variable
            append(content.substring(lastIndex, match.range.first))
            
            // Add highlighted variable
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    background = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                append(match.value)
            }
            
            lastIndex = match.range.last + 1
        }
        
        // Add remaining text
        append(content.substring(lastIndex))
    }
    
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
    )
}
```

---

## ⚙️ SettingsScreen

**Purpose:** Application configuration and settings  
**Features:** API configuration, notifications, auto-retry, export

### Settings Categories

```kotlin
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // API Configuration
        item {
            SettingsSection(
                title = "API Configuration",
                icon = Icons.Default.Api
            ) {
                ApiSettingsCard(
                    settings = uiState.apiSettings,
                    onSettingsChange = viewModel::updateApiSettings
                )
            }
        }
        
        // Notifications
        item {
            SettingsSection(
                title = "Notifications",
                icon = Icons.Default.Notifications
            ) {
                NotificationSettingsCard(
                    settings = uiState.notificationSettings,
                    onSettingsChange = viewModel::updateNotificationSettings
                )
            }
        }
        
        // SMS Settings
        item {
            SettingsSection(
                title = "SMS Settings",
                icon = Icons.Default.Sms
            ) {
                SmsSettingsCard(
                    settings = uiState.smsSettings,
                    onSettingsChange = viewModel::updateSmsSettings
                )
            }
        }
        
        // Data Management
        item {
            SettingsSection(
                title = "Data Management",
                icon = Icons.Default.Storage
            ) {
                DataManagementCard(
                    onExportData = viewModel::exportData,
                    onImportData = viewModel::importData,
                    onClearData = viewModel::clearData
                )
            }
        }
    }
}
```

---

## 🔧 DiagnosticsScreen

**Purpose:** System health monitoring and diagnostics  
**Features:** Health status, component checks, test actions

### Health Status Display

```kotlin
@Composable
fun DiagnosticsScreen(
    navController: NavController,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Health Status
        item {
            HealthStatusCard(
                healthStatus = uiState.healthStatus
            )
        }
        
        // Component Status
        item {
            ComponentStatusCard(
                components = uiState.componentStatus
            )
        }
        
        // System Information
        item {
            SystemInfoCard(
                systemInfo = uiState.systemInfo
            )
        }
        
        // Test Actions
        item {
            TestActionsCard(
                onTestConnection = viewModel::testConnection,
                onTestSms = viewModel::testSms,
                onClearLogs = viewModel::clearLogs
            )
        }
    }
}
```

---

## 🎨 UI Components Library

### Common Components

#### StatusBadge
```kotlin
@Composable
fun StatusBadge(status: String) {
    val (color, icon) = when (status) {
        "PENDING" -> MaterialTheme.colorScheme.secondary to Icons.Default.Schedule
        "QUEUED" -> MaterialTheme.colorScheme.primary to Icons.Default.Queue
        "SCHEDULED" -> MaterialTheme.colorScheme.tertiary to Icons.Default.Event
        "SENDING" -> MaterialTheme.colorScheme.primary to Icons.Default.Send
        "SENT" -> Color(0xFF4CAF50) to Icons.Default.CheckCircle
        "DELIVERED" -> Color(0xFF2196F3) to Icons.Default.DoneAll
        "FAILED" -> MaterialTheme.colorScheme.error to Icons.Default.Error
        else -> MaterialTheme.colorScheme.outline to Icons.Default.Help
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
```

#### QuickActionsCard
```kotlin
@Composable
fun QuickActionsCard(
    onSendSms: () -> Unit,
    onBulkSms: () -> Unit,
    onScheduleSms: () -> Unit,
    onTemplates: () -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAction(
                    icon = Icons.Default.Send,
                    title = "Send SMS",
                    subtitle = "Send individual message",
                    onClick = onSendSms
                )
                
                QuickAction(
                    icon = Icons.Default.Group,
                    title = "Bulk SMS",
                    subtitle = "Send to multiple contacts",
                    onClick = onBulkSms
                )
                
                QuickAction(
                    icon = Icons.Default.Schedule,
                    title = "Schedule SMS",
                    subtitle = "Schedule for later",
                    onClick = onScheduleSms
                )
                
                QuickAction(
                    icon = Icons.Default.Template,
                    title = "Templates",
                    subtitle = "Manage templates",
                    onClick = onTemplates
                )
            }
        }
    }
}
```

---

## 📱 Navigation Architecture

### Navigation Graph Structure

```kotlin
@Composable
fun SmsGatewayNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }
        
        composable("history") {
            HistoryScreen(navController = navController)
        }
        
        composable("bulk_sms") {
            BulkSmsScreen(navController = navController)
        }
        
        composable("scheduled_sms") {
            ScheduledSmsScreen(navController = navController)
        }
        
        composable("templates") {
            TemplatesScreen(navController = navController)
        }
        
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        
        composable("diagnostics") {
            DiagnosticsScreen(navController = navController)
        }
    }
}
```

### Responsive Navigation

**Tablet (≥840dp):** Sidebar navigation  
**Mobile (<840dp):** Bottom navigation + drawer

---

## 🎯 UI Best Practices

### Material 3 Implementation
- **Color Scheme:** Dynamic colors with Material You
- **Typography:** Material 3 type scale
- **Components:** Material 3 components with proper theming
- **Motion:** Meaningful transitions and animations

### Accessibility
- **Semantic Components:** Proper content descriptions
- **Contrast:** WCAG AA compliance
- **Touch Targets:** Minimum 48dp touch targets
- **Navigation:** Proper focus handling

### Performance
- **Lazy Loading:** LazyColumn/LazyVerticalGrid for large lists
- **State Optimization:** Proper state hoisting and memoization
- **Recomposition:** Minimize unnecessary recompositions
- **Image Loading:** Efficient image loading with caching

---

**Status:** UI demonstrates modern Android development with Material 3, responsive design, and comprehensive user experience patterns.