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

    @Query("UPDATE sms_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: Long)

    @Query("DELETE FROM sms_queue WHERE status = :status AND createdAt < :beforeTime")
    suspend fun deleteOldSms(status: SmsStatus, beforeTime: Long): Int

    @Query("SELECT COUNT(*) FROM sms_queue WHERE status = :status")
    fun getSmsCountByStatus(status: SmsStatus): Flow<Int>

    @Query("SELECT * FROM sms_queue ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSms(limit: Int = 100): Flow<List<SmsQueue>>
}