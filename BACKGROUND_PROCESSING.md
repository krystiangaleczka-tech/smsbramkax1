# SMS Gateway - Background Processing Documentation

## ⚙️ Background Processing Overview

**Framework:** Android WorkManager  
**Service Type:** Foreground Service (DATA_SYNC)  
**Architecture:** Worker-based task processing  

### Background Processing Flow

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UI/Manager    │───▶│   WorkManager   │───▶│   Workers       │
│                 │    │                 │    │                 │
│ - SMS Creation  │    │ - Scheduling    │    │ - Send SMS      │
│ - Scheduling    │    │ - Constraints   │    │ - Process       │
│ - Status Update │    │ - Retry Logic   │    │ - Sync Contacts │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │                        │
                                ▼                        ▼
                       ┌─────────────────┐    ┌─────────────────┐
                       │ Foreground      │    │ Android SMS API │
                       │ Service         │    │                 │
                       │                 │    │ - Send SMS      │
                       │ - Notifications │    │ - Delivery      │
                       │ - Status Track  │    │ - Status        │
                       └─────────────────┘    └─────────────────┘
```

---

## 🔧 WorkManager Configuration

### Application Setup

**SmsGatewayApplication.kt:**
```kotlin
class SmsGatewayApplication : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
            .build()
    }
}
```

### Worker Constraints

**Common Constraints:**
- **Network Type:** Connected (for API sync)
- **Battery:** Not low (for critical operations)
- **Storage:** Not low (for data operations)
- **Charging:** Required for bulk operations

---

## 👷 Workers Implementation

### 1. SendQueuedSmsWorker

**Purpose:** Process queued SMS messages  
**Trigger:** On-demand WorkManager execution  
**Constraints:** Network connected, battery not low

#### Worker Configuration

```kotlin
class SendQueuedSmsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            processQueuedMessages()
            Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "Error processing queued messages", exception)
            Result.retry()
        }
    }
}
```

#### Processing Logic

```kotlin
private suspend fun processQueuedMessages() {
    // 1. Retrieve pending messages
    val pendingMessages = smsMessageDao.getByStatus("PENDING")
    
    // 2. Check system health
    val healthStatus = healthChecker.getHealthStatus()
    if (!healthStatus.isHealthy) {
        throw Exception("System not healthy: ${healthStatus.description}")
    }
    
    // 3. Process each message
    pendingMessages.forEach { message ->
        try {
            // Update status to SENDING
            smsMessageDao.updateStatus(message.id, "SENDING")
            
            // Send SMS via Android API
            val result = smsManager.sendSms(message.phoneNumber, message.messageBody)
            
            if (result.isSuccess) {
                // Update status to SENT
                smsMessageDao.updateStatus(message.id, "SENT")
                smsMessageDao.updateSentAt(message.id, System.currentTimeMillis())
                
                // Log success
                logManager.log("INFO", "SMS", "Message sent successfully", message.id)
            } else {
                // Handle failure
                handleSendFailure(message, result.exceptionOrNull())
            }
            
        } catch (exception: Exception) {
            handleSendFailure(message, exception)
        }
    }
}
```

#### Error Handling & Retry Logic

```kotlin
private suspend fun handleSendFailure(message: SmsMessage, error: Throwable?) {
    val retryCount = message.retryCount + 1
    val maxRetries = 3
    
    if (retryCount <= maxRetries) {
        // Update retry count and keep in PENDING
        smsMessageDao.updateRetryCount(message.id, retryCount)
        
        // Schedule retry with exponential backoff
        val delayMs = (1000L * pow(2.0, retryCount.toDouble())).toLong()
        
        logManager.log(
            "WARNING", 
            "SMS", 
            "Message failed, scheduling retry $retryCount/$maxRetries in ${delayMs}ms", 
            message.id
        )
        
        // Re-enqueue with delay
        WorkManager.getInstance(applicationContext)
            .beginUniqueWork(
                "send_sms_${message.id}",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<SendQueuedSmsWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .build()
            )
            .enqueue()
            
    } else {
        // Max retries reached, mark as FAILED
        smsMessageDao.updateStatus(message.id, "FAILED")
        smsMessageDao.updateErrorMessage(message.id, error?.message ?: "Unknown error")
        
        logManager.log(
            "ERROR", 
            "SMS", 
            "Message failed after $maxRetries retries: ${error?.message}", 
            message.id
        )
        
        // Send error notification
        notify.sendErrorNotification(message.id, error?.message)
    }
}
```

---

### 2. ProcessScheduledSmsWorker

**Purpose:** Process scheduled SMS messages  
**Trigger:** Periodic WorkManager execution (60 minutes)  
**Constraints:** None (runs even without network)

#### Worker Configuration

```kotlin
class ProcessScheduledSmsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "process_scheduled_sms"
        const val REPEAT_INTERVAL = 60L // minutes
    }
    
    override suspend fun doWork(): Result {
        return try {
            processScheduledMessages()
            cleanupOldMessages()
            Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "Error processing scheduled messages", exception)
            Result.failure()
        }
    }
}
```

#### Scheduled Message Processing

```kotlin
private suspend fun processScheduledMessages() {
    val now = System.currentTimeMillis()
    
    // Get due scheduled messages
    val dueMessages = smsMessageDao.getDueScheduledMessages(now)
    
    dueMessages.forEach { message ->
        try {
            // Check if within 24-hour window
            val scheduledAt = message.scheduledAt ?: 0L
            val windowEnd = scheduledAt + (24 * 60 * 60 * 1000L) // 24 hours
            
            if (now <= windowEnd) {
                // Update status to QUEUED for immediate processing
                smsMessageDao.updateStatus(message.id, "QUEUED")
                
                logManager.log(
                    "INFO", 
                    "SCHEDULED_SMS", 
                    "Scheduled message ${message.id} moved to queue", 
                    message.id
                )
                
                // Trigger immediate processing
                val sendRequest = OneTimeWorkRequestBuilder<SendQueuedSmsWorker>()
                    .addTag("sms_send")
                    .build()
                
                WorkManager.getInstance(applicationContext).enqueue(sendRequest)
                
            } else {
                // Outside 24-hour window, mark as FAILED
                smsMessageDao.updateStatus(message.id, "FAILED")
                smsMessageDao.updateErrorMessage(
                    message.id, 
                    "Scheduled time expired (24-hour window)"
                )
                
                logManager.log(
                    "WARNING", 
                    "SCHEDULED_SMS", 
                    "Scheduled message ${message.id} expired", 
                    message.id
                )
            }
            
        } catch (exception: Exception) {
            logManager.log(
                "ERROR", 
                "SCHEDULED_SMS", 
                "Error processing scheduled message ${message.id}: ${exception.message}", 
                message.id
            )
        }
    }
}
```

#### Cleanup Operations

```kotlin
private suspend fun cleanupOldMessages() {
    val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days
    
    // Clean up old deleted messages
    val deletedCount = smsMessageDao.deleteOldDeletedMessages(cutoffTime)
    
    if (deletedCount > 0) {
        logManager.log(
            "INFO", 
            "CLEANUP", 
            "Cleaned up $deletedCount old deleted messages"
        )
    }
}
```

---

### 3. SyncContactsWorker

**Purpose:** Contact synchronization from device  
**Trigger:** Manual or periodic WorkManager execution (6 hours)  
**Constraints:** Contacts permission required

#### Worker Configuration

```kotlin
class SyncContactsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "sync_contacts"
        const val REPEAT_INTERVAL = 6L // hours
    }
    
    override suspend fun doWork(): Result {
        return try {
            if (permissionsManager.hasContactsPermission()) {
                syncContacts()
                Result.success()
            } else {
                logManager.log("WARNING", "CONTACTS", "Contacts permission not granted")
                Result.failure()
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Error syncing contacts", exception)
            Result.failure()
        }
    }
}
```

#### Contact Synchronization Process

```kotlin
private suspend fun syncContacts() {
    logManager.log("INFO", "CONTACTS", "Starting contact synchronization")
    
    try {
        // 1. Get contacts from device
        val deviceContacts = contactManager.getDeviceContacts()
        
        // 2. Process each contact
        var syncedCount = 0
        var updatedCount = 0
        
        deviceContacts.forEach { deviceContact ->
            try {
                // Clean and validate phone number
                val cleanPhone = contactManager.cleanPhoneNumber(deviceContact.phoneNumber)
                
                if (contactManager.isValidPhoneNumber(cleanPhone)) {
                    val existingContact = smsDatabase.contactDao()
                        .getContactByPhoneNumber(cleanPhone)
                    
                    if (existingContact == null) {
                        // Insert new contact
                        val newContact = Contact(
                            name = deviceContact.name,
                            phoneNumber = cleanPhone,
                            contactId = deviceContact.contactId
                        )
                        smsDatabase.contactDao().insertOrUpdate(newContact)
                        syncedCount++
                        
                    } else {
                        // Update existing contact if needed
                        if (existingContact.name != deviceContact.name || 
                            existingContact.contactId != deviceContact.contactId) {
                            val updatedContact = existingContact.copy(
                                name = deviceContact.name,
                                contactId = deviceContact.contactId,
                                updatedAt = System.currentTimeMillis()
                            )
                            smsDatabase.contactDao().update(updatedContact)
                            updatedCount++
                        }
                    }
                }
                
            } catch (exception: Exception) {
                logManager.log(
                    "WARNING", 
                    "CONTACTS", 
                    "Error processing contact ${deviceContact.name}: ${exception.message}"
                )
            }
        }
        
        // 3. Clean up old contacts
        val cutoffTime = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L) // 90 days
        val deletedCount = smsDatabase.contactDao().deleteOldContacts(cutoffTime)
        
        logManager.log(
            "INFO", 
            "CONTACTS", 
            "Sync completed: $syncedCount new, $updatedCount updated, $deletedCount deleted"
        )
        
    } catch (exception: Exception) {
        logManager.log("ERROR", "CONTACTS", "Contact sync failed: ${exception.message}")
        throw exception
    }
}
```

---

### 4. FetchPendingSmsWorker

**Purpose:** Synchronize with external API (if configured)  
**Trigger:** Periodic WorkManager execution (30 minutes)  
**Constraints:** Network connected

#### Worker Configuration

```kotlin
class FetchPendingSmsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "fetch_pending_sms"
        const val REPEAT_INTERVAL = 30L // minutes
    }
    
    override suspend fun doWork(): Result {
        return try {
            if (networkState.isOnline() && apiConfig.isConfigured()) {
                fetchPendingSms()
                Result.success()
            } else {
                logManager.log("INFO", "API", "API sync skipped: offline or not configured")
                Result.success()
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Error fetching pending SMS", exception)
            Result.retry()
        }
    }
}
```

#### API Synchronization

```kotlin
private suspend fun fetchPendingSms() {
    try {
        // Get pending SMS from API
        val pendingSms = networkManager.fetchPendingSms()
        
        if (pendingSms.isNotEmpty()) {
            var importedCount = 0
            
            pendingSms.forEach { apiSms ->
                try {
                    // Convert to local format
                    val localSms = SmsMessage(
                        phoneNumber = apiSms.phoneNumber,
                        messageBody = apiSms.messageBody,
                        status = "PENDING",
                        isScheduled = apiSms.scheduledFor != null,
                        scheduledFor = apiSms.scheduledFor,
                        scheduledAt = apiSms.scheduledAt ?: System.currentTimeMillis(),
                        category = apiSms.category
                    )
                    
                    // Insert into local database
                    smsMessageDao.insert(localSms)
                    importedCount++
                    
                    // Mark as processed in API
                    networkManager.markSmsProcessed(apiSms.id)
                    
                } catch (exception: Exception) {
                    logManager.log(
                        "ERROR", 
                        "API", 
                        "Error importing SMS ${apiSms.id}: ${exception.message}"
                    )
                }
            }
            
            logManager.log(
                "INFO", 
                "API", 
                "Imported $importedCount SMS from API"
            )
            
            // Trigger processing if any were imported
            if (importedCount > 0) {
                val sendRequest = OneTimeWorkRequestBuilder<SendQueuedSmsWorker>()
                    .addTag("sms_send")
                    .build()
                
                WorkManager.getInstance(applicationContext).enqueue(sendRequest)
            }
        }
        
    } catch (exception: Exception) {
        logManager.log("ERROR", "API", "API sync failed: ${exception.message}")
        throw exception
    }
}
```

---

## 🌐 Foreground Service

### SmsForegroundService

**Purpose:** Background SMS processing with user notification  
**Type:** DATA_SYNC  
**Lifetime:** Active when tasks are in queue

#### Service Configuration

```kotlin
class SmsForegroundService : Service() {
    
    companion object {
        const val SERVICE_ID = 1001
        const val CHANNEL_ID = "sms_service"
        const val NOTIFICATION_TITLE = "SMS Gateway"
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(SERVICE_ID, createNotification())
        return START_STICKY
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText("Processing SMS messages...")
            .setSmallIcon(R.drawable.ic_sms)
            .setOngoing(true)
            .build()
    }
}
```

#### Service Lifecycle Management

```kotlin
class ServiceManager @Inject constructor(
    private val context: Context,
    private val workManager: WorkManager,
    private val smsMessageDao: SmsMessageDao
) {
    
    suspend fun startServiceIfNeeded() {
        val pendingCount = smsMessageDao.countByStatus("PENDING")
        val queuedCount = smsMessageDao.countByStatus("QUEUED")
        
        if (pendingCount > 0 || queuedCount > 0) {
            startService()
        } else {
            stopService()
        }
    }
    
    private fun startService() {
        val intent = Intent(context, SmsForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
    
    private fun stopService() {
        val intent = Intent(context, SmsForegroundService::class.java)
        context.stopService(intent)
    }
}
```

---

## 📡 Broadcast Receivers

### BootReceiver

**Purpose:** Handle system boot and app restart  
**Actions:** BOOT_COMPLETED, MY_PACKAGE_REPLACED

```kotlin
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Reschedule periodic workers
                schedulePeriodicWorkers(context)
                
                // Check for pending messages
                CoroutineScope(Dispatchers.IO).launch {
                    val serviceManager = ServiceManager(context)
                    serviceManager.startServiceIfNeeded()
                }
            }
        }
    }
    
    private fun schedulePeriodicWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)
        
        // Process scheduled SMS worker
        val scheduledWorker = PeriodicWorkRequestBuilder<ProcessScheduledSmsWorker>(
            60, // repeat interval (minutes)
            TimeUnit.MINUTES
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            ProcessScheduledSmsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            scheduledWorker
        )
        
        // Sync contacts worker
        val contactsWorker = PeriodicWorkRequestBuilder<SyncContactsWorker>(
            6, // repeat interval (hours)
            TimeUnit.HOURS
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            SyncContactsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            contactsWorker
        )
        
        // Fetch pending SMS worker
        val fetchWorker = PeriodicWorkRequestBuilder<FetchPendingSmsWorker>(
            30, // repeat interval (minutes)
            TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()
        
        workManager.enqueueUniquePeriodicWork(
            FetchPendingSmsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            fetchWorker
        )
    }
}
```

---

## 🔄 Worker Scheduling Strategies

### Immediate Processing

```kotlin
fun scheduleImmediateSmsSend(smsId: Long) {
    val request = OneTimeWorkRequestBuilder<SendQueuedSmsWorker>()
        .addTag("sms_send")
        .addTag("sms_$smsId")
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        )
        .build()
    
    WorkManager.getInstance(context).enqueueUniqueWork(
        "send_sms_$smsId",
        ExistingWorkPolicy.REPLACE,
        request
    )
}
```

### Delayed Processing

```kotlin
fun scheduleDelayedSmsSend(smsId: Long, delayMs: Long) {
    val request = OneTimeWorkRequestBuilder<SendQueuedSmsWorker>()
        .addTag("sms_send")
        .addTag("sms_$smsId")
        .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            WorkRequest.MIN_BACKOFF_MILLIS,
            WorkRequest.MAX_BACKOFF_MILLIS
        )
        .build()
    
    WorkManager.getInstance(context).enqueueUniqueWork(
        "send_sms_$smsId",
        ExistingWorkPolicy.REPLACE,
        request
    )
}
```

### Bulk Processing

```kotlin
fun scheduleBulkSmsSend(batchId: String) {
    val request = OneTimeWorkRequestBuilder<SendQueuedSmsWorker>()
        .addTag("bulk_send")
        .addTag("batch_$batchId")
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresCharging(true) // Bulk operations require charging
                .build()
        )
        .build()
    
    WorkManager.getInstance(context).enqueueUniqueWork(
        "bulk_send_$batchId",
        ExistingWorkPolicy.REPLACE,
        request
    )
}
```

---

## 📊 Worker Monitoring & Debugging

### WorkManager Observation

```kotlin
class WorkManagerObserver @Inject constructor(
    private val workManager: WorkManager
) {
    
    fun observeWorkInfo(): Flow<List<WorkInfo>> {
        return workManager.getWorkInfosByTagLiveData("sms_send").asFlow()
            .map { workInfos -> workInfos.filter { it.state != WorkInfo.State.CANCELLED } }
    }
    
    fun getActiveWorkersCount(): Flow<Int> {
        return workManager.getWorkInfosByTagLiveData("sms_send").asFlow()
            .map { workInfos -> workInfos.count { it.state == WorkInfo.State.RUNNING } }
    }
    
    fun cancelAllSmsWork() {
        workManager.cancelAllWorkByTag("sms_send")
    }
    
    fun cancelBatchWork(batchId: String) {
        workManager.cancelAllWorkByTag("batch_$batchId")
    }
}
```

### Worker Status Tracking

```kotlin
data class WorkerStatus(
    val workerName: String,
    val state: WorkInfo.State,
    val progress: Data,
    val runAttemptCount: Int,
    val lastEnqueueTime: Long
)

class WorkerStatusTracker @Inject constructor(
    private val workManager: WorkManager
) {
    
    suspend fun getWorkerStatuses(): List<WorkerStatus> {
        val workInfos = workManager.getWorkInfosByTag("sms_send").await()
        
        return workInfos.map { workInfo ->
            WorkerStatus(
                workerName = workInfo.id.toString(),
                state = workInfo.state,
                progress = workInfo.progress,
                runAttemptCount = workInfo.runAttemptCount,
                lastEnqueueTime = workInfo.lastEnqueueTime
            )
        }
    }
}
```

---

## 🔧 Background Processing Best Practices

### Worker Design Principles

1. **Idempotency:** Workers should handle multiple executions safely
2. **Error Handling:** Comprehensive error handling with proper logging
3. **Resource Management:** Proper cleanup of resources
4. **Constraints:** Use appropriate constraints for each worker type
5. **Backoff Strategy:** Implement exponential backoff for retries

### Performance Optimization

1. **Batch Processing:** Process multiple items in a single worker execution
2. **Database Transactions:** Use transactions for data consistency
3. **Memory Management:** Avoid memory leaks in long-running operations
4. **Network Efficiency:** Batch API calls when possible

### Battery Optimization

1. **Smart Scheduling:** Use WorkManager constraints effectively
2. **Batch Operations:** Group similar operations together
3. **Charging Requirements:** Require charging for intensive operations
4. **Doze Mode:** Handle device doze mode gracefully

---

## 🚨 Error Handling & Recovery

### Worker Failure Scenarios

1. **Network Issues:** Automatic retry with exponential backoff
2. **Permission Loss:** Fail gracefully and notify user
3. **Database Errors:** Retry with transaction rollback
4. **Resource Constraints:** Reschedule with appropriate constraints

### Recovery Strategies

```kotlin
class WorkerErrorHandler @Inject constructor(
    private val logManager: LogManager,
    private val notify: Notify
) {
    
    fun handleWorkerFailure(workerClass: Class<out CoroutineWorker>, error: Throwable) {
        val errorMessage = "Worker ${workerClass.simpleName} failed: ${error.message}"
        
        logManager.log("ERROR", "WORKER", errorMessage)
        
        when (error) {
            is NetworkException -> {
                // Network errors will be retried automatically
                logManager.log("INFO", "WORKER", "Network error, will retry")
            }
            is PermissionException -> {
                // Notify user of permission issues
                notify.sendPermissionNotification()
            }
            is DatabaseException -> {
                // Critical error, may require user intervention
                notify.sendDatabaseErrorNotification()
            }
            else -> {
                // Unknown error, log for debugging
                logManager.log("ERROR", "WORKER", "Unknown error: ${error.stackTraceToString()}")
            }
        }
    }
}
```

---

**Status:** Background processing demonstrates robust WorkManager integration with proper error handling, retry mechanisms, and resource management for production-ready SMS processing.