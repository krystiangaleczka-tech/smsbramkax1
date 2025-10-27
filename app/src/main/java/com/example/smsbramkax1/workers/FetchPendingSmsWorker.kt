package com.example.smsbramkax1.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smsbramkax1.data.SmsQueue
import com.example.smsbramkax1.data.SmsStatus
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
                    val existingSms = database.smsQueueDao().getSmsById(smsDto.id)
                    if (existingSms == null) {
                        val smsQueue = SmsQueue(
                            id = smsDto.id,
                            phoneNumber = smsDto.phoneNumber,
                            message = smsDto.message,
                            status = if (smsDto.scheduledAt != null && smsDto.scheduledAt > System.currentTimeMillis()) {
                                SmsStatus.SCHEDULED
                            } else {
                                SmsStatus.PENDING
                            },
                            priority = smsDto.priority,
                            scheduledAt = smsDto.scheduledAt
                        )
                        database.smsQueueDao().insertSms(smsQueue)
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