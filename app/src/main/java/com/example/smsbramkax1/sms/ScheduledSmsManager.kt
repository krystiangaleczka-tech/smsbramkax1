package com.example.smsbramkax1.sms

import android.content.Context
import com.example.smsbramkax1.data.ScheduledSms
import com.example.smsbramkax1.data.ScheduledSmsStatus
import com.example.smsbramkax1.storage.ScheduledSmsDao
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.utils.LogManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ScheduledSmsManager private constructor(private val context: Context) {
    
    private val database: SmsDatabase = SmsDatabase.getDatabase(context)
    private val scheduledSmsDao: ScheduledSmsDao = database.scheduledSmsDao()
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
        return try {
            if (scheduledFor <= System.currentTimeMillis()) {
                return Result.failure(IllegalArgumentException("Scheduled time must be in the future"))
            }
            
            val scheduledSms = ScheduledSms(
                name = name,
                phoneNumber = phoneNumber,
                messageBody = messageBody,
                scheduledFor = scheduledFor,
                status = ScheduledSmsStatus.SCHEDULED
            )
            
            val id = scheduledSmsDao.insertScheduledSms(scheduledSms)
            LogManager.log("INFO", "ScheduledSmsManager", "Scheduled SMS created with ID: $id for $phoneNumber at $scheduledFor")
            Result.success(id)
        } catch (e: Exception) {
            LogManager.log("ERROR", "ScheduledSmsManager", "Failed to schedule SMS: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun updateScheduledSms(
        id: Long,
        name: String? = null,
        phoneNumber: String? = null,
        messageBody: String? = null,
        scheduledFor: Long? = null
    ): Result<Unit> {
        return try {
            val existing = scheduledSmsDao.getScheduledSmsById(id)
                ?: return Result.failure(IllegalArgumentException("Scheduled SMS not found"))
            
            if (existing.status != ScheduledSmsStatus.SCHEDULED) {
                return Result.failure(IllegalArgumentException("Can only edit scheduled SMS"))
            }
            
            val updated = existing.copy(
                name = name ?: existing.name,
                phoneNumber = phoneNumber ?: existing.phoneNumber,
                messageBody = messageBody ?: existing.messageBody,
                scheduledFor = scheduledFor ?: existing.scheduledFor,
                updatedAt = System.currentTimeMillis()
            )
            
            scheduledSmsDao.updateScheduledSms(updated)
            LogManager.log("INFO", "ScheduledSmsManager", "Scheduled SMS updated: ID $id")
            Result.success(Unit)
        } catch (e: Exception) {
            LogManager.log("ERROR", "ScheduledSmsManager", "Failed to update scheduled SMS: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun cancelScheduledSms(id: Long): Result<Unit> {
        return try {
            val existing = scheduledSmsDao.getScheduledSmsById(id)
                ?: return Result.failure(IllegalArgumentException("Scheduled SMS not found"))
            
            if (existing.status !in listOf(ScheduledSmsStatus.SCHEDULED, ScheduledSmsStatus.QUEUED)) {
                return Result.failure(IllegalArgumentException("Cannot cancel SMS in status: ${existing.status}"))
            }
            
            scheduledSmsDao.updateScheduledSmsStatus(id, ScheduledSmsStatus.DELETED)
            LogManager.log("INFO", "ScheduledSmsManager", "Scheduled SMS cancelled: ID $id")
            Result.success(Unit)
        } catch (e: Exception) {
            LogManager.log("ERROR", "ScheduledSmsManager", "Failed to cancel scheduled SMS: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun processDueScheduledSms(): Int {
        return try {
            val currentTime = System.currentTimeMillis()
            val dueSms = scheduledSmsDao.getDueScheduledSms(currentTime)
            
            var processedCount = 0
            for (scheduledSms in dueSms) {
                when (scheduledSms.status) {
                    ScheduledSmsStatus.SCHEDULED -> {
                        scheduledSmsDao.updateScheduledSmsStatus(
                            scheduledSms.id, 
                            ScheduledSmsStatus.QUEUED
                        )
                        
                        val sendResult = smsManager.sendSms(
                            scheduledSms.phoneNumber,
                            scheduledSms.messageBody
                        )
                        
                        if (sendResult) {
                            scheduledSmsDao.updateScheduledSmsStatus(
                                scheduledSms.id,
                                ScheduledSmsStatus.SENT
                            )
                            LogManager.log("INFO", "ScheduledSmsManager", "Scheduled SMS sent successfully: ID ${scheduledSms.id}")
                        } else {
                            scheduledSmsDao.updateScheduledSmsStatus(
                                scheduledSms.id,
                                ScheduledSmsStatus.FAILED
                            )
                            LogManager.log("ERROR", "ScheduledSmsManager", "Scheduled SMS failed: ID ${scheduledSms.id}")
                        }
                        processedCount++
                    }
                    else -> {
                        // Already processed
                    }
                }
            }
            
            if (processedCount > 0) {
                LogManager.log("INFO", "ScheduledSmsManager", "Processed $processedCount due scheduled SMS")
            }
            
            processedCount
        } catch (e: Exception) {
            LogManager.log("ERROR", "ScheduledSmsManager", "Error processing due scheduled SMS: ${e.message}")
            0
        }
    }
    
    fun getAllScheduledSms(): Flow<List<ScheduledSms>> {
        return scheduledSmsDao.getAllScheduledSms()
    }
    
    fun getPendingScheduledSms(): Flow<List<ScheduledSms>> {
        return scheduledSmsDao.getPendingScheduledSms()
    }
    
    suspend fun getScheduledSmsById(id: Long): ScheduledSms? {
        return scheduledSmsDao.getScheduledSmsById(id)
    }
    
    suspend fun getScheduledCount(): Int {
        return scheduledSmsDao.getScheduledCount()
    }
    
    suspend fun getQueuedCount(): Int {
        return scheduledSmsDao.getQueuedCount()
    }
    
    suspend fun cleanupOldDeletedSms() {
        try {
            val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
            scheduledSmsDao.cleanupOldDeletedSms(thirtyDaysAgo)
            LogManager.log("INFO", "ScheduledSmsManager", "Cleaned up old deleted scheduled SMS")
        } catch (e: Exception) {
            LogManager.log("ERROR", "ScheduledSmsManager", "Failed to cleanup old scheduled SMS: ${e.message}")
        }
    }
}