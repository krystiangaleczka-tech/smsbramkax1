package com.example.smsbramkax1.sms

import android.content.Context
import com.example.smsbramkax1.data.SmsMessage
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.utils.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ScheduledSmsManager private constructor(private val context: Context) {
    
    private val database: SmsDatabase = SmsDatabase.getDatabase(context)
    private val smsMessageDao = database.smsMessageDao()
    private val smsManager: SmsManager = SmsManager(context)
    
    companion object {
        @Volatile
        private var INSTANCE: ScheduledSmsManager? = null
        
        fun getInstance(context: Context): ScheduledSmsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScheduledSmsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun scheduleSms(
        name: String,
        phoneNumber: String,
        messageBody: String,
        scheduledFor: Long
    ): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                // Always preserve the selected time from pickers
                // SMS will be processed immediately if within 24 hours, but keep the selected time
                val actualScheduledFor = scheduledFor

                val scheduledMessage = SmsMessage(
                    phoneNumber = phoneNumber,
                    messageBody = messageBody,
                    status = "SCHEDULED",
                    isScheduled = true,
                    scheduledFor = actualScheduledFor,
                    category = name // Use name as category for identification
                )
                
                val id = smsMessageDao.insertMessage(scheduledMessage)
                LogManager.log("INFO", "ScheduledSmsManager", "Scheduled SMS created with ID: $id for $phoneNumber at $scheduledFor")
                Result.success(id)
            } catch (e: Exception) {
                LogManager.log("ERROR", "ScheduledSmsManager", "Failed to schedule SMS: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateScheduledSms(
        id: Long,
        name: String,
        phoneNumber: String,
        messageBody: String,
        scheduledFor: Long
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val existing = smsMessageDao.getMessageById(id)
                    ?: return@withContext Result.failure(IllegalArgumentException("Scheduled SMS not found"))
                
                if (existing.status != "SCHEDULED" || !existing.isScheduled) {
                    return@withContext Result.failure(IllegalArgumentException("Can only edit scheduled SMS"))
                }

                // Always preserve the selected time from pickers
                val actualScheduledFor = scheduledFor

                val updated = existing.copy(
                    phoneNumber = phoneNumber,
                    messageBody = messageBody,
                    scheduledFor = actualScheduledFor,
                    category = name
                )
                
                smsMessageDao.updateMessage(updated)
                LogManager.log("INFO", "ScheduledSmsManager", "Scheduled SMS updated: ID $id")
                Result.success(Unit)
            } catch (e: Exception) {
                LogManager.log("ERROR", "ScheduledSmsManager", "Failed to update scheduled SMS: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun cancelScheduledSms(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val existing = smsMessageDao.getMessageById(id)
                    ?: return@withContext Result.failure(IllegalArgumentException("Scheduled SMS not found"))
                
                if (!existing.isScheduled || existing.status !in listOf("SCHEDULED", "QUEUED")) {
                    return@withContext Result.failure(IllegalArgumentException("Cannot cancel SMS in status: ${existing.status}"))
                }
                
                smsMessageDao.updateMessageStatus(id, "DELETED")
                LogManager.log("INFO", "ScheduledSmsManager", "Scheduled SMS cancelled: ID $id")
                Result.success(Unit)
            } catch (e: Exception) {
                LogManager.log("ERROR", "ScheduledSmsManager", "Failed to cancel scheduled SMS: ${e.message}")
                Result.failure(e)
            }
        }
    }
    
    suspend fun processDueScheduledSms(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                val twentyFourHoursFromNow = currentTime + (24 * 60 * 60 * 1000)
                LogManager.log("INFO", "ScheduledSmsManager", "Processing SMS at time: $currentTime")

                // Get all scheduled SMS (both due and within 24h for immediate sending)
                val allScheduledSms = smsMessageDao.getFutureScheduledMessages()
                LogManager.log("INFO", "ScheduledSmsManager", "Found ${allScheduledSms.size} future scheduled SMS")

                var processedCount = 0
                for (scheduledSms in allScheduledSms) {
                    if (scheduledSms.status != "SCHEDULED") continue
                    
                    val shouldProcessImmediately = scheduledSms.scheduledFor != null && scheduledSms.scheduledFor!! <= twentyFourHoursFromNow
                    val isDue = scheduledSms.scheduledFor != null && scheduledSms.scheduledFor!! <= currentTime
                    
                    LogManager.log("INFO", "ScheduledSmsManager", "SMS ID: ${scheduledSms.id}, scheduled for: ${scheduledSms.scheduledFor}, isDue: $isDue, shouldProcessImmediately: $shouldProcessImmediately")
                    
                    if (isDue || shouldProcessImmediately) {
                        smsMessageDao.updateMessageStatus(
                            scheduledSms.id, 
                            "QUEUED"
                        )
                        
                        val sendResult = smsManager.tryToSendSms(
                            scheduledSms.phoneNumber,
                            scheduledSms.messageBody
                        )
                        
                        if (sendResult) {
                            smsMessageDao.updateMessageStatus(
                                scheduledSms.id,
                                "SENT",
                                System.currentTimeMillis()
                            )
                            LogManager.log("INFO", "ScheduledSmsManager", "SMS sent successfully: ID ${scheduledSms.id}")
                        } else {
                            smsMessageDao.updateMessageStatus(
                                scheduledSms.id,
                                "FAILED"
                            )
                            LogManager.log("ERROR", "ScheduledSmsManager", "SMS failed: ID ${scheduledSms.id}")
                        }
                        processedCount++
                        LogManager.log("INFO", "ScheduledSmsManager", "Processed SMS ID ${scheduledSms.id}, sendResult: $sendResult")
                    }
                }
                
                if (processedCount > 0) {
                    LogManager.log("INFO", "ScheduledSmsManager", "Processed $processedCount SMS")
                }
                
                processedCount
            } catch (e: Exception) {
                LogManager.log("ERROR", "ScheduledSmsManager", "Error processing scheduled SMS: ${e.message}")
                0
            }
        }
    }
    
    fun getAllScheduledSms(): Flow<List<SmsMessage>> {
        return smsMessageDao.getScheduledMessages()
    }
    
    fun getPendingScheduledSms(): Flow<List<SmsMessage>> {
        return smsMessageDao.getScheduledMessages().map { messages ->
            messages.filter { it.status == "SCHEDULED" }
        }
    }
    
    suspend fun getScheduledSmsById(id: Long): SmsMessage? {
        return smsMessageDao.getMessageById(id)
    }
    
    suspend fun getScheduledCount(): Int {
        return smsMessageDao.getScheduledCount()
    }
    
    suspend fun getQueuedCount(): Int {
        return smsMessageDao.getQueuedCount()
    }
    
    suspend fun cleanupOldDeletedSms() {
        try {
            val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
            smsMessageDao.cleanupOldDeletedMessages(thirtyDaysAgo)
            LogManager.log("INFO", "ScheduledSmsManager", "Cleaned up old deleted scheduled SMS")
        } catch (e: Exception) {
            LogManager.log("ERROR", "ScheduledSmsManager", "Failed to cleanup old scheduled SMS: ${e.message}")
        }
    }
}