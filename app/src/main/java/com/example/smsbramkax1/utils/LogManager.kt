// File: utils/LogManager.kt

package com.example.smsbramkax1.utils

import com.example.smsbramkax1.data.SystemLog
import com.example.smsbramkax1.storage.LogDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class LogManager(private val logDao: LogDao) {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun logInfo(category: String, message: String, smsId: Long? = null) {
        log("INFO", category, message, smsId)
    }

    fun logWarning(category: String, message: String, smsId: Long? = null) {
        log("WARNING", category, message, smsId)
    }

    fun logError(category: String, message: String, smsId: Long? = null, stackTrace: String? = null) {
        log("ERROR", category, message, smsId, stackTrace)
    }

    private fun log(level: String, category: String, message: String, smsId: Long?, stackTrace: String? = null) {
        scope.launch {
            val logEntry = SystemLog(
                timestamp = System.currentTimeMillis(),
                level = level,
                category = category,
                message = message,
                smsId = smsId,
                stackTrace = stackTrace,
                meta = if (smsId != null || stackTrace != null) {
                    "smsId: $smsId, stackTrace: $stackTrace"
                } else null
            )
            logDao.insertLog(logEntry)
        }
    }

    companion object {
        private var instance: LogManager? = null
        
        fun initialize(logDao: LogDao) {
            instance = LogManager(logDao)
        }
        
        fun log(level: String, category: String, message: String, smsId: Long? = null, stackTrace: String? = null) {
            instance?.log(level, category, message, smsId, stackTrace)
            android.util.Log.println(
                when (level) {
                    "ERROR" -> android.util.Log.ERROR
                    "WARN", "WARNING" -> android.util.Log.WARN
                    "INFO" -> android.util.Log.INFO
                    else -> android.util.Log.DEBUG
                },
                "SMSGateway",
                "[$category] $message"
            )
        }
        
        fun log(level: String, category: String, message: String, stackTrace: String? = null) {
            log(level, category, message, null, stackTrace)
        }
    }

    suspend fun getRecentLogs(limit: Int = 100) = logDao.getRecentLogs(limit).first()

    suspend fun getLogsByLevel(level: String) = logDao.getLogsByLevel(level).first()

    suspend fun clearOldLogs(daysToKeep: Int = 7) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        logDao.deleteLogsOlderThan(cutoffTime)
    }
}