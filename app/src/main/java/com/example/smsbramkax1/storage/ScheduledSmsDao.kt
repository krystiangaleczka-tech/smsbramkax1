package com.example.smsbramkax1.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.smsbramkax1.data.ScheduledSms
import com.example.smsbramkax1.data.ScheduledSmsStatus

@Dao
interface ScheduledSmsDao {
    
    @Query("SELECT * FROM scheduled_sms ORDER BY scheduledFor ASC")
    fun getAllScheduledSms(): Flow<List<ScheduledSms>>
    
    @Query("SELECT * FROM scheduled_sms WHERE status = 'SCHEDULED' ORDER BY scheduledFor ASC")
    fun getPendingScheduledSms(): Flow<List<ScheduledSms>>
    
    @Query("SELECT * FROM scheduled_sms WHERE status = 'SCHEDULED' AND scheduledFor <= :currentTime")
    suspend fun getDueScheduledSms(currentTime: Long): List<ScheduledSms>
    
    @Query("SELECT * FROM scheduled_sms WHERE id = :id")
    suspend fun getScheduledSmsById(id: Long): ScheduledSms?
    
    @Query("SELECT * FROM scheduled_sms WHERE status != 'DELETED' ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentScheduledSms(limit: Int = 50): Flow<List<ScheduledSms>>
    
    @Query("SELECT COUNT(*) FROM scheduled_sms WHERE status = 'SCHEDULED'")
    suspend fun getScheduledCount(): Int
    
    @Query("SELECT COUNT(*) FROM scheduled_sms WHERE status = 'QUEUED'")
    suspend fun getQueuedCount(): Int
    
    @Insert
    suspend fun insertScheduledSms(scheduledSms: ScheduledSms): Long
    
    @Update
    suspend fun updateScheduledSms(scheduledSms: ScheduledSms)
    
    @Query("UPDATE scheduled_sms SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateScheduledSmsStatus(id: Long, status: ScheduledSmsStatus, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE scheduled_sms SET status = 'DELETED', updatedAt = :updatedAt WHERE id = :id")
    suspend fun deleteScheduledSms(id: Long, updatedAt: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun permanentlyDeleteScheduledSms(scheduledSms: ScheduledSms)
    
    @Query("DELETE FROM scheduled_sms WHERE status = 'DELETED' AND updatedAt < :beforeTime")
    suspend fun cleanupOldDeletedSms(beforeTime: Long)
    
    @Query("SELECT * FROM scheduled_sms WHERE phoneNumber LIKE :phoneNumberPattern ORDER BY scheduledFor DESC")
    fun getScheduledSmsByPhoneNumber(phoneNumberPattern: String): Flow<List<ScheduledSms>>
    
    @Query("SELECT * FROM scheduled_sms WHERE name LIKE :namePattern ORDER BY scheduledFor DESC")
    fun getScheduledSmsByName(namePattern: String): Flow<List<ScheduledSms>>
}