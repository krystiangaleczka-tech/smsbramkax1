package com.example.smsbramkax1.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smsbramkax1.data.SmsMessage
import com.example.smsbramkax1.network.NetworkManager
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.utils.LogManager
import com.example.smsbramkax1.utils.Notify
import com.example.smsbramkax1.utils.SecureStorage
import kotlinx.coroutines.flow.first

class FetchPendingSmsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            LogManager.log("INFO", "FetchPendingSmsWorker", "Starting fetch of pending SMS")
            
            val secureStorage = SecureStorage(applicationContext)
            val baseUrl = secureStorage.getServerUrl() ?: return Result.failure()
            val apiKey = secureStorage.getApiKey() ?: return Result.failure()
            
            val networkManager = NetworkManager(baseUrl, apiKey)
            val database = SmsDatabase.getDatabase(applicationContext)
            
            val pendingSms = networkManager.fetchPendingSms()
            
            if (pendingSms.isNotEmpty()) {
                var insertedCount = 0
                pendingSms.forEach { smsDto ->
                    val existingSms = database.smsMessageDao().getSmsById(smsDto.id)
                    if (existingSms == null) {
                        val isScheduled = smsDto.scheduledAt != null && smsDto.scheduledAt > System.currentTimeMillis()
                        val smsMessage = SmsMessage(
                            id = smsDto.id,
                            phoneNumber = smsDto.phoneNumber,
                            messageBody = smsDto.message,
                            status = if (isScheduled) "SCHEDULED" else "PENDING",
                            priority = smsDto.priority,
                            scheduledFor = smsDto.scheduledAt,
                            isScheduled = isScheduled
                        )
                        database.smsMessageDao().insertSms(smsMessage)
                        insertedCount++
                    }
                }
                LogManager.log("INFO", "FetchPendingSmsWorker", "Inserted $insertedCount new SMS")
            } else {
                LogManager.log("DEBUG", "FetchPendingSmsWorker", "No pending SMS found")
            }
            
            networkManager.close()
            Result.success()
            
        } catch (e: Exception) {
            LogManager.log("ERROR", "FetchPendingSmsWorker", "Failed to fetch pending SMS: ${e.message}", e.stackTraceToString())
            
            // Powiadomienie o błędzie sieci
            Notify.error(
                applicationContext,
                "Błąd synchronizacji",
                "Nie udało się pobrać oczekujących SMS-ów: ${e.message}"
            )
            
            Result.failure()
        }
    }
}