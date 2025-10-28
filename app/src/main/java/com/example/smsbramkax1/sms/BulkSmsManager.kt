package com.example.smsbramkax1.sms

import android.content.Context
import com.example.smsbramkax1.data.SmsMessage
import com.example.smsbramkax1.dto.BulkSmsProgressDTO
import com.example.smsbramkax1.dto.BulkSmsRequestDTO
import com.example.smsbramkax1.dto.BulkSmsResponseDTO
import com.example.smsbramkax1.dto.BulkSmsStatus
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.storage.SmsMessageDao
import com.example.smsbramkax1.utils.LogManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.random.Random

class BulkSmsManager private constructor(private val context: Context) {
    
    private val database: SmsDatabase = SmsDatabase.getDatabase(context)
    private val smsMessageDao: SmsMessageDao = database.smsMessageDao()
    private val smsManager: SmsManager = SmsManager(context)
    
    private val _bulkProgress = MutableStateFlow<Map<String, BulkSmsProgressDTO>>(emptyMap())
    val bulkProgress: StateFlow<Map<String, BulkSmsProgressDTO>> = _bulkProgress.asStateFlow()
    
    private val processingJobs = mutableMapOf<String, Job>()
    
    companion object {
        @Volatile
        private var INSTANCE: BulkSmsManager? = null
        
        fun getInstance(context: Context): BulkSmsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BulkSmsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun createBulkSms(request: BulkSmsRequestDTO): BulkSmsResponseDTO {
        val batchId = request.batchId ?: generateBatchId()
        val phoneNumbers = request.phoneNumbers.distinct().filter { isValidPhoneNumber(it) }
        
        if (phoneNumbers.isEmpty()) {
            throw IllegalArgumentException("No valid phone numbers provided")
        }
        
        if (request.messageBody.isBlank()) {
            throw IllegalArgumentException("Message body cannot be empty")
        }
        
        val estimatedDuration = estimateDuration(phoneNumbers.size, request.sendDelayMs)
        
        // Queue all SMS in database
        val queuedCount = queueBulkSms(batchId, phoneNumbers, request.messageBody)
        
        val response = BulkSmsResponseDTO(
            batchId = batchId,
            totalRecipients = phoneNumbers.size,
            status = BulkSmsStatus.QUEUED,
            queuedCount = queuedCount,
            estimatedDurationMinutes = estimatedDuration
        )
        
        // Start processing in background
        startBulkProcessing(batchId, request.sendDelayMs, request.batchSize)
        
        LogManager.log("INFO", "BulkSmsManager", "Bulk SMS created: batchId=$batchId, recipients=${phoneNumbers.size}")
        return response
    }
    
    private suspend fun queueBulkSms(batchId: String, phoneNumbers: List<String>, messageBody: String): Int {
        var queuedCount = 0
        val currentTime = System.currentTimeMillis()
        
        phoneNumbers.forEach { phoneNumber ->
            try {
                val smsMessage = SmsMessage(
                    phoneNumber = phoneNumber,
                    messageBody = messageBody,
                    status = "QUEUED",
                    isScheduled = false,
                    createdAt = currentTime,
                    batchId = batchId
                )
                smsMessageDao.insertMessage(smsMessage)
                queuedCount++
            } catch (e: Exception) {
                LogManager.log("ERROR", "BulkSmsManager", "Failed to queue SMS for $phoneNumber: ${e.message}")
            }
        }
        
        return queuedCount
    }
    
    private fun startBulkProcessing(batchId: String, delayMs: Long, batchSize: Int) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                updateProgress(batchId) { it.copy(status = BulkSmsStatus.PROCESSING) }
                
                val queuedSms = smsMessageDao.getSmsByBatchId(batchId)
                var sentCount = 0
                var failedCount = 0
                val errors = mutableListOf<String>()
                
                // Process in batches
                queuedSms.chunked(batchSize).forEach { batch ->
                    for (sms in batch) {
                        if (sms.status == "QUEUED") {
                            try {
                                val result = smsManager.sendSms(sms.phoneNumber, sms.messageBody)
                                
                                if (result.isSuccess) {
                                    smsMessageDao.updateMessageStatus(sms.id, "SENT", System.currentTimeMillis())
                                    sentCount++
                                    LogManager.log("INFO", "BulkSmsManager", "Bulk SMS sent: batchId=$batchId, to=${sms.phoneNumber}")
                                } else {
                                    smsMessageDao.updateMessageStatus(sms.id, "FAILED")
                                    failedCount++
                                    errors.add("${sms.phoneNumber}: Send failed")
                                    LogManager.log("ERROR", "BulkSmsManager", "Bulk SMS failed: batchId=$batchId, to=${sms.phoneNumber}")
                                }
                                
                                // Rate limiting
                                if (delayMs > 0) {
                                    delay(delayMs)
                                }
                            } catch (e: Exception) {
                                smsMessageDao.updateMessageStatus(sms.id, "FAILED")
                                failedCount++
                                errors.add("${sms.phoneNumber}: ${e.message}")
                                LogManager.log("ERROR", "BulkSmsManager", "Bulk SMS error: batchId=$batchId, to=${sms.phoneNumber}, error=${e.message}")
                            }
                        }
                    }
                }
                
                val finalStatus = if (failedCount == 0) BulkSmsStatus.COMPLETED else BulkSmsStatus.COMPLETED
                updateProgress(batchId) { 
                    it.copy(
                        status = finalStatus,
                        sentCount = sentCount,
                        failedCount = failedCount,
                        completedAt = System.currentTimeMillis(),
                        errors = errors
                    )
                }
                
                LogManager.log("INFO", "BulkSmsManager", "Bulk SMS completed: batchId=$batchId, sent=$sentCount, failed=$failedCount")
                
            } catch (e: Exception) {
                LogManager.log("ERROR", "BulkSmsManager", "Bulk SMS processing failed: batchId=$batchId, error=${e.message}")
                updateProgress(batchId) { 
                    it.copy(
                        status = BulkSmsStatus.FAILED,
                        completedAt = System.currentTimeMillis(),
                        errors = listOf("Processing failed: ${e.message}")
                    )
                }
            } finally {
                processingJobs.remove(batchId)
            }
        }
        
        processingJobs[batchId] = job
    }
    
    fun cancelBulkSms(batchId: String): Boolean {
        return try {
            processingJobs[batchId]?.cancel()
            processingJobs.remove(batchId)
            
            // Update remaining queued SMS to CANCELLED status
            CoroutineScope(Dispatchers.IO).launch {
                val queuedSms = smsMessageDao.getSmsByBatchId(batchId)
                queuedSms.filter { it.status == "QUEUED" }.forEach { sms ->
                    smsMessageDao.updateMessageStatus(sms.id, "FAILED")
                }
            }
            
            updateProgress(batchId) { 
                it.copy(
                    status = BulkSmsStatus.CANCELLED,
                    completedAt = System.currentTimeMillis()
                )
            }
            
            LogManager.log("INFO", "BulkSmsManager", "Bulk SMS cancelled: batchId=$batchId")
            true
        } catch (e: Exception) {
            LogManager.log("ERROR", "BulkSmsManager", "Failed to cancel bulk SMS: batchId=$batchId, error=${e.message}")
            false
        }
    }
    
    fun getBulkProgress(batchId: String): BulkSmsProgressDTO? {
        return _bulkProgress.value[batchId]
    }
    
    fun getAllBulkProgress(): Map<String, BulkSmsProgressDTO> {
        return _bulkProgress.value
    }
    
    suspend fun getDetailedProgress(batchId: String): BulkSmsProgressDTO? {
        return try {
            val queuedSms = smsMessageDao.getSmsByBatchId(batchId)
            val sentCount = queuedSms.count { it.status == "SENT" }
            val failedCount = queuedSms.count { it.status == "FAILED" }
            val queuedCount = queuedSms.count { it.status == "QUEUED" }
            
            val currentProgress = _bulkProgress.value[batchId]
            currentProgress?.copy(
                totalRecipients = queuedSms.size,
                queuedCount = queuedCount,
                sentCount = sentCount,
                failedCount = failedCount
            )
        } catch (e: Exception) {
            LogManager.log("ERROR", "BulkSmsManager", "Failed to get detailed progress: batchId=$batchId, error=${e.message}")
            null
        }
    }
    
    private fun updateProgress(batchId: String, updateFn: (BulkSmsProgressDTO) -> BulkSmsProgressDTO) {
        val current = _bulkProgress.value[batchId] ?: BulkSmsProgressDTO(
            batchId = batchId,
            totalRecipients = 0,
            queuedCount = 0,
            sentCount = 0,
            failedCount = 0,
            status = BulkSmsStatus.QUEUED,
            startedAt = System.currentTimeMillis()
        )
        
        val updated = updateFn(current)
        val newProgress = _bulkProgress.value.toMutableMap()
        newProgress[batchId] = updated
        _bulkProgress.value = newProgress
    }
    
    private fun generateBatchId(): String {
        return "bulk_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }
    
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.matches(Regex("^\\+?[0-9]{9,15}$"))
    }
    
    private fun estimateDuration(recipientCount: Int, delayMs: Long): Int {
        val totalDelayMs = recipientCount * delayMs
        return ((totalDelayMs / 1000) / 60).toInt() + 1 // Add 1 minute buffer
    }
    
    fun cleanupOldProgress() {
        val cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours ago
        val currentProgress = _bulkProgress.value.filter { (_, progress) ->
            progress.startedAt > cutoffTime || progress.completedAt == null
        }
        _bulkProgress.value = currentProgress
    }
}