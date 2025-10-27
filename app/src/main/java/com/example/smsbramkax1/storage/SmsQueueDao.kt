// File: storage/SmsQueueDao.kt

package com.example.smsbramkax1.storage

import androidx.room.*
import com.example.smsbramkax1.data.SmsQueue
import com.example.smsbramkax1.data.SmsStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsQueueDao {
    @Insert
    suspend fun insertSms(sms: SmsQueue): Long

    @Update
    suspend fun updateSms(sms: SmsQueue)

    @Query("SELECT * FROM sms_queue WHERE id = :id")
    suspend fun getSmsById(id: Long): SmsQueue?

    @Query("SELECT * FROM sms_queue WHERE status = :status ORDER BY priority DESC, createdAt ASC")
    suspend fun getSmsByStatus(status: SmsStatus): List<SmsQueue>

    @Query("UPDATE sms_queue SET status = :status, sentAt = :sentAt WHERE id = :id")
    suspend fun updateSmsStatus(id: Long, status: SmsStatus, sentAt: Long)
    
    @Query("UPDATE sms_queue SET status = :status WHERE id = :id")
    suspend fun updateSmsStatus(id: Long, status: SmsStatus)

    @Query("UPDATE sms_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)

    @Query("DELETE FROM sms_queue WHERE status = :status AND createdAt < :beforeTime")
    suspend fun deleteOldSms(status: SmsStatus, beforeTime: Long): Int

    @Query("SELECT COUNT(*) FROM sms_queue WHERE status = :status")
    fun getSmsCountByStatus(status: SmsStatus): Flow<Int>

    @Query("SELECT * FROM sms_queue ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSms(limit: Int = 100): Flow<List<SmsQueue>>

    // Metody statystyczne dla HealthChecker
    @Query("SELECT COUNT(*) FROM sms_queue WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    @Query("SELECT COUNT(*) FROM sms_queue WHERE status = 'FAILED' AND createdAt >= :since")
    suspend fun countFailedSince(since: Long): Int

    @Query("SELECT MAX(sentAt) FROM sms_queue WHERE sentAt IS NOT NULL")
    suspend fun lastSentAt(): Long?

    @Query("SELECT * FROM sms_queue ORDER BY createdAt DESC")
    suspend fun getAllSms(): List<SmsQueue>
    
    @Query("SELECT * FROM sms_queue WHERE batchId = :batchId ORDER BY createdAt ASC")
    suspend fun getSmsByBatchId(batchId: String): List<SmsQueue>
    
    @Query("SELECT COUNT(*) FROM sms_queue WHERE batchId = :batchId AND status = :status")
    suspend fun countByBatchIdAndStatus(batchId: String, status: SmsStatus): Int
}