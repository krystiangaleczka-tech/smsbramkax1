package com.example.smsbramkax1.storage

import androidx.room.*
import com.example.smsbramkax1.data.SystemLog
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert
    suspend fun insertLog(log: SystemLog): Long
    
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int = 100): List<SystemLog>
    
    @Query("SELECT * FROM system_logs WHERE level = :level ORDER BY timestamp DESC")
    suspend fun getLogsByLevel(level: String): List<SystemLog>
    
    @Query("SELECT * FROM system_logs WHERE smsId = :smsId ORDER BY timestamp DESC")
    suspend fun getLogsBySmsId(smsId: Long): List<SystemLog>
    
    @Query("SELECT * FROM system_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    suspend fun getLogsSince(sinceTimestamp: Long): List<SystemLog>
    
    @Query("DELETE FROM system_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteLogsOlderThan(cutoffTimestamp: Long): Int
    
    @Query("DELETE FROM system_logs")
    suspend fun clearAllLogs()
}