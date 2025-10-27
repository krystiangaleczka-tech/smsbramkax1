package com.example.smsbramkax1.storage

import androidx.room.*
import com.example.smsbramkax1.data.SystemLog
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM system_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<SystemLog>>

    @Query("SELECT * FROM system_logs WHERE level = :level ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByLevel(level: String, limit: Int = 100): Flow<List<SystemLog>>

    @Query("SELECT * FROM system_logs WHERE timestamp >= :fromTime ORDER BY timestamp DESC")
    suspend fun getLogsSince(fromTime: Long): List<SystemLog>

    @Insert
    suspend fun insertLog(log: SystemLog)

    @Query("DELETE FROM system_logs WHERE timestamp < :beforeTime")
    suspend fun deleteOldLogs(beforeTime: Long): Int

    @Query("SELECT COUNT(*) FROM system_logs WHERE level = 'ERROR'")
    fun getErrorCount(): Flow<Int>
}