package com.example.smsbramkax1.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.smsbramkax1.data.SmsMessage
import com.example.smsbramkax1.data.SystemLog
import com.example.smsbramkax1.storage.SmsDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray
import org.json.JSONObject

class ExportManager(private val context: Context) {
    
    companion object {
        private const val AUTHORITY = "com.example.smsbramkax1.fileprovider"
        private const val EXPORT_DIR = "exports"
    }
    
    suspend fun exportSmsToCsv(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val database = SmsDatabase.getDatabase(context)
            val allSms = database.smsMessageDao().getAllSms()
            
            if (allSms.isEmpty()) {
                return@withContext Result.failure(Exception("Brak danych do eksportu"))
            }
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val fileName = "sms_export_${dateFormat.format(Date())}.csv"
            val file = createExportFile(fileName)
            
            FileOutputStream(file).use { output ->
                // CSV Headers
                output.write("ID,PhoneNumber,Message,Status,CreatedAt,SentAt,RetryCount,ErrorMessage,Priority\n".toByteArray())
                
                // SMS Data
                allSms.forEach { sms ->
                    val status = when (sms.status) {
                        "PENDING" -> "Oczekujący"
                        "SENT" -> "Wysłany"
                        "FAILED" -> "Błąd"
                        "SCHEDULED" -> "Zaplanowany"
                        else -> sms.status
                    }
                    
                    val createdDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(sms.createdAt))
                    val sentDate = sms.sentAt?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it)) } ?: ""
                    
                    val line = "${sms.id},${sms.phoneNumber},\"${escapeCsv(sms.message)}\",$status,$createdDate,$sentDate,${sms.retryCount},\"${escapeCsv(sms.errorMessage ?: "")}\",${sms.priority}\n"
                    output.write(line.toByteArray())
                }
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun exportLogsToJson(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val database = SmsDatabase.getDatabase(context)
            val allLogs = database.logDao().getRecentLogs(1000)
            
            if (allLogs.isEmpty()) {
                return@withContext Result.failure(Exception("Brak logów do eksportu"))
            }
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val fileName = "logs_export_${dateFormat.format(Date())}.json"
            val file = createExportFile(fileName)
            
            val logsArray = JSONArray()
            allLogs.forEach { log: com.example.smsbramkax1.data.SystemLog ->
                val logObject = JSONObject().apply {
                    put("id", log.id)
                    put("level", log.level)
                    put("message", log.message)
                    put("timestamp", log.timestamp)
                    put("category", log.category)
                    put("smsId", log.smsId ?: 0)
                    put("stackTrace", log.stackTrace ?: "")
                    put("meta", log.meta ?: "")
                }
                logsArray.put(logObject)
            }
            
            val exportObject = JSONObject().apply {
                put("exportDate", System.currentTimeMillis())
                put("exportType", "logs")
                put("totalLogs", allLogs.size)
                put("logs", logsArray)
            }
            
            FileOutputStream(file).use { output ->
                output.write(exportObject.toString(2).toByteArray())
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun importSmsFromCsv(filePath: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Plik nie istnieje"))
            }
            
            val database = SmsDatabase.getDatabase(context)
            var importedCount = 0
            
            file.readLines().drop(1).forEach { line ->
                try {
                    val columns = parseCsvLine(line)
                    if (columns.size >= 8) {
                        val phoneNumber = columns[1].trim()
                        val message = unescapeCsv(columns[2].trim())
                        val statusStr = columns[3].trim()
                        val createdAtStr = columns[4].trim()
                        val priority = if (columns.size > 8) columns[8].trim().toIntOrNull() ?: 5 else 5
                        
                        if (phoneNumber.isNotEmpty() && message.isNotEmpty()) {
                            val status = when (statusStr) {
                                "Oczekujący", "PENDING" -> "PENDING"
                                "Wysłany", "SENT" -> "SENT"
                                "Błąd", "FAILED" -> "FAILED"
                                "Zaplanowany", "SCHEDULED" -> "SCHEDULED"
                                else -> "PENDING"
                            }
                            
                            val createdAt = try {
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(createdAtStr)?.time ?: System.currentTimeMillis()
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                            
                            val smsMessage = SmsMessage(
                                phoneNumber = phoneNumber,
                                messageBody = message,
                                status = status,
                                priority = priority,
                                createdAt = createdAt,
                                isScheduled = status == "SCHEDULED"
                            )
                            
                            database.smsMessageDao().insertSms(smsMessage)
                            importedCount++
                        }
                    }
                } catch (e: Exception) {
                    // Skip invalid lines but continue processing
                }
            }
            
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun shareFile(filePath: String, mimeType: String = "text/csv") {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                ErrorHandler.showError(context, "Plik nie istnieje")
                return
            }
            
            val uri = FileProvider.getUriForFile(context, AUTHORITY, file)
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Eksport danych z SMS Gateway")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(shareIntent, "Udostępnij plik")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            ErrorHandler(context).handleDatabaseError(e)
        }
    }
    
    suspend fun exportFullBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val database = SmsDatabase.getDatabase(context)
            val allSms = database.smsMessageDao().getAllSms()
            val allLogs = database.logDao().getRecentLogs(1000)
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val fileName = "full_backup_${dateFormat.format(Date())}.json"
            val file = createExportFile(fileName)
            
            // Export SMS
            val smsArray = JSONArray()
            allSms.forEach { sms ->
                val smsObject = JSONObject().apply {
                    put("id", sms.id)
                    put("phoneNumber", sms.phoneNumber)
                    put("message", sms.message)
                    put("status", sms.status)
                    put("createdAt", sms.createdAt)
                    put("sentAt", sms.sentAt ?: 0)
                    put("retryCount", sms.retryCount)
                    put("errorMessage", sms.errorMessage ?: "")
                    put("priority", sms.priority)
                }
                smsArray.put(smsObject)
            }
            
            // Export Logs
            val logsArray = JSONArray()
            allLogs.forEach { log: com.example.smsbramkax1.data.SystemLog ->
                val logObject = JSONObject().apply {
                    put("id", log.id)
                    put("level", log.level)
                    put("message", log.message)
                    put("timestamp", log.timestamp)
                    put("category", log.category)
                    put("smsId", log.smsId ?: 0)
                    put("stackTrace", log.stackTrace ?: "")
                    put("meta", log.meta ?: "")
                }
                logsArray.put(logObject)
            }
            
            val backupObject = JSONObject().apply {
                put("exportDate", System.currentTimeMillis())
                put("exportType", "full_backup")
                put("version", "1.0")
                put("totalSms", allSms.size)
                put("totalLogs", allLogs.size)
                put("sms", smsArray)
                put("logs", logsArray)
            }
            
            FileOutputStream(file).use { output ->
                output.write(backupObject.toString(2).toByteArray())
            }
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun createExportFile(fileName: String): File {
        // Use app's external files directory for compatibility with FileProvider
        val exportDir = File(context.getExternalFilesDir(null), EXPORT_DIR).also {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        
        return File(exportDir, fileName)
    }
    
    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
    
    private fun unescapeCsv(value: String): String {
        return value.replace("\"\"", "\"")
    }
    
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '"' -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> {
                    current.append(char)
                }
            }
        }
        result.add(current.toString())
        
        return result
    }
}

// Extension function to get all logs (assuming it doesn't exist yet)
suspend fun getAllLogs(): List<SystemLog> {
    // This would need to be implemented in LogDao
    return emptyList()
}