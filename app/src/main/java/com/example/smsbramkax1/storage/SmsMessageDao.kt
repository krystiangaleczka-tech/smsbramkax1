package com.example.smsbramkax1.storage

import androidx.room.*
import com.example.smsbramkax1.data.SmsMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsMessageDao {
    // Basic CRUD
    @Insert
    suspend fun insertMessage(message: SmsMessage): Long
    
    @Update
    suspend fun updateMessage(message: SmsMessage)
    
    @Query("SELECT * FROM sms_messages ORDER BY createdAt DESC")
    fun getAllMessages(): Flow<List<SmsMessage>>
    
    // History queries
    @Query("SELECT * FROM sms_messages WHERE status IN ('SENT', 'DELIVERED') ORDER BY sentAt DESC")
    fun getSentMessages(): Flow<List<SmsMessage>>
    
    // Scheduled queries
    @Query("SELECT * FROM sms_messages WHERE isScheduled = 1 ORDER BY scheduledFor DESC")
    fun getScheduledMessages(): Flow<List<SmsMessage>>
    
    @Query("SELECT * FROM sms_messages WHERE isScheduled = 1 AND status = 'SCHEDULED' AND scheduledFor <= :currentTime")
    suspend fun getDueScheduledMessages(currentTime: Long): List<SmsMessage>
    
    @Query("SELECT * FROM sms_messages WHERE isScheduled = 1 AND status = 'SCHEDULED'")
    suspend fun getAllScheduledMessages(): List<SmsMessage>
    
    // Additional query for future scheduled messages (for processing)
    @Query("SELECT * FROM sms_messages WHERE isScheduled = 1 AND status IN ('SCHEDULED', 'QUEUED') ORDER BY scheduledFor ASC")
    suspend fun getFutureScheduledMessages(): List<SmsMessage>
    
    // Status updates
    @Query("UPDATE sms_messages SET status = :status, sentAt = :sentAt WHERE id = :id")
    suspend fun updateMessageStatus(id: Long, status: String, sentAt: Long? = null)
    
    @Query("UPDATE sms_messages SET status = :status WHERE id = :id")
    suspend fun updateMessageStatus(id: Long, status: String)
    
    // Get by ID
    @Query("SELECT * FROM sms_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): SmsMessage?
    
    // Delete
    @Query("DELETE FROM sms_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)
    
    // Statistics
    @Query("SELECT COUNT(*) FROM sms_messages WHERE status = :status")
    suspend fun getCountByStatus(status: String): Int
    
    @Query("SELECT COUNT(*) FROM sms_messages WHERE isScheduled = 1 AND status = 'SCHEDULED'")
    suspend fun getScheduledCount(): Int
    
    @Query("SELECT COUNT(*) FROM sms_messages WHERE isScheduled = 1 AND status = 'QUEUED'")
    suspend fun getQueuedCount(): Int
    
    // Cleanup
    @Query("DELETE FROM sms_messages WHERE status IN ('SENT', 'DELIVERED', 'FAILED') AND createdAt < :beforeTime")
    suspend fun deleteOldMessages(beforeTime: Long): Int
    
    @Query("DELETE FROM sms_messages WHERE isScheduled = 1 AND status = 'DELETED' AND createdAt < :beforeTime")
    suspend fun cleanupOldDeletedMessages(beforeTime: Long): Int
    
    // Legacy compatibility methods
    @Insert
    suspend fun insertSms(sms: SmsMessage): Long
    
    @Query("SELECT * FROM sms_messages ORDER BY createdAt DESC")
    suspend fun getAllSms(): List<SmsMessage>
    
    @Query("SELECT * FROM sms_messages WHERE id = :id")
    suspend fun getSmsById(id: Long): SmsMessage?
    
    @Query("SELECT * FROM sms_messages WHERE status = :status")
    suspend fun getSmsByStatus(status: String): List<SmsMessage>
    
    @Query("SELECT * FROM sms_messages WHERE batchId = :batchId")
    suspend fun getSmsByBatchId(batchId: String): List<SmsMessage>
    
    @Query("SELECT * FROM sms_messages ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentSms(limit: Int): List<SmsMessage>
    
    @Query("UPDATE sms_messages SET status = :status, sentAt = :sentAt WHERE id = :id")
    suspend fun updateSmsStatus(id: Long, status: String, sentAt: Long? = null)
    
    @Query("UPDATE sms_messages SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)
    
    @Query("SELECT COUNT(*) FROM sms_messages WHERE status = :status")
    suspend fun countByStatus(status: String): Int
    
    @Query("SELECT COUNT(*) FROM sms_messages WHERE status = 'FAILED' AND createdAt >= :since")
    suspend fun countFailedSince(since: Long): Int
    
    @Query("SELECT sentAt FROM sms_messages WHERE status = 'SENT' ORDER BY sentAt DESC LIMIT 1")
    suspend fun lastSentAt(): Long?
}