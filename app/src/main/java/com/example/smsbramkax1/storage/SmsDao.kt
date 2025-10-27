package com.example.smsbramkax1.storage

import androidx.room.*
import com.example.smsbramkax1.data.SmsMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_messages ORDER BY queuedAt DESC")
    fun getAllMessages(): Flow<List<SmsMessage>>

    @Query("SELECT * FROM sms_messages WHERE status = :status ORDER BY scheduledFor ASC")
    fun getMessagesByStatus(status: String): Flow<List<SmsMessage>>

    @Query("SELECT * FROM sms_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): SmsMessage?

    @Query("SELECT * FROM sms_messages WHERE externalId = :externalId")
    suspend fun getMessageByExternalId(externalId: String): SmsMessage?

    @Query("SELECT * FROM sms_messages WHERE status = 'QUEUED' AND scheduledFor <= :currentTime ORDER BY scheduledFor ASC")
    suspend fun getPendingMessages(currentTime: Long): List<SmsMessage>

    @Insert
    suspend fun insertMessage(message: SmsMessage): Long

    @Update
    suspend fun updateMessage(message: SmsMessage)

    @Delete
    suspend fun deleteMessage(message: SmsMessage)

    @Query("SELECT COUNT(*) FROM sms_messages WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM sms_messages WHERE status = :status AND queuedAt >= :startTime")
    fun getCountByStatusSince(status: String, startTime: Long): Flow<Int>

    @Query("DELETE FROM sms_messages WHERE queuedAt < :beforeTime AND status IN ('SENT','DELIVERED','FAILED')")
    suspend fun deleteOldMessages(beforeTime: Long): Int
}