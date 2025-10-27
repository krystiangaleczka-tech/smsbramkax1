package com.example.smsbramkax1.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smsbramkax1.data.SmsStatus
import com.example.smsbramkax1.network.NetworkManager
import com.example.smsbramkax1.sms.SmsManager
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.utils.LogManager
import com.example.smsbramkax1.utils.Notify
import com.example.smsbramkax1.utils.SecureStorage
import kotlinx.coroutines.flow.first

class SendQueuedSmsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            LogManager.log("INFO", "SendQueuedSmsWorker", "Starting SMS sending process")
            
            val secureStorage = SecureStorage(applicationContext)
            val baseUrl = secureStorage.getServerUrl() ?: return Result.failure()
            val apiKey = secureStorage.getApiKey() ?: return Result.failure()
            
            val networkManager = NetworkManager(baseUrl, apiKey)
            val database = SmsDatabase.getDatabase(applicationContext)
            val smsManager = SmsManager(applicationContext)
            
            // Sprawdź stan zdrowia systemu przed rozpoczęciem
            val failedLastHour = database.smsQueueDao().countFailedSince(System.currentTimeMillis() - 60 * 60 * 1000)
            if (failedLastHour >= 5) {
                Notify.error(
                    applicationContext,
                    "Wiele błędów wysyłki",
                    "Ostatnia godzina: $failedLastHour błędów. Sprawdź połączenie i ustawienia."
                )
            }
            
            val pendingSms = database.smsQueueDao().getSmsByStatus(SmsStatus.PENDING)
            val scheduledSms = database.smsQueueDao().getSmsByStatus(SmsStatus.SCHEDULED)
                .filter { it.scheduledAt != null && it.scheduledAt!! <= System.currentTimeMillis() }
            
            val smsToSend = (pendingSms + scheduledSms).sortedByDescending { it.priority }
            
            if (smsToSend.isNotEmpty()) {
                var sentCount = 0
                var failedCount = 0
                
                smsToSend.forEach { sms ->
                    try {
                        val success = smsManager.sendSms(sms.phoneNumber, sms.message)
                        val currentTime = System.currentTimeMillis()
                        
                        if (success) {
                            database.smsQueueDao().updateSmsStatus(sms.id, SmsStatus.SENT, currentTime)
                            LogManager.log("INFO", "SendQueuedSmsWorker", "SMS sent successfully to ${sms.phoneNumber}")
                            sentCount++
                        } else {
                            database.smsQueueDao().incrementRetryCount(sms.id)
                            if (sms.retryCount >= 3) {
                                database.smsQueueDao().updateSmsStatus(sms.id, SmsStatus.FAILED, currentTime)
                            }
                            LogManager.log("WARN", "SendQueuedSmsWorker", "Failed to send SMS to ${sms.phoneNumber}")
                            failedCount++
                        }
                        
                        val statusUpdate = com.example.smsbramkax1.dto.SmsStatusUpdateDTO(
                            smsId = sms.id,
                            status = if (success) "SENT" else "FAILED",
                            sentAt = currentTime,
                            errorMessage = if (!success) "SMS sending failed" else null
                        )
                        
                        networkManager.sendSmsStatus(statusUpdate)
                        
                    } catch (e: Exception) {
                        database.smsQueueDao().incrementRetryCount(sms.id)
                        if (sms.retryCount >= 3) {
                            database.smsQueueDao().updateSmsStatus(sms.id, SmsStatus.FAILED, System.currentTimeMillis())
                        }
                        LogManager.log("ERROR", "SendQueuedSmsWorker", "Error sending SMS to ${sms.phoneNumber}: ${e.message}")
                        failedCount++
                    }
                }
                
                LogManager.log("INFO", "SendQueuedSmsWorker", "SMS sending completed: $sentCount sent, $failedCount failed")
                
                // Powiadomienie o dużej liczbie błędów
                if (failedCount >= 10) {
                    Notify.error(
                        applicationContext,
                        "Krytyczna liczba błędów",
                        "Nie udało się wysłać $failedCount SMS-ów. Sprawdź logi."
                    )
                } else if (failedCount >= 5) {
                    Notify.error(
                        applicationContext,
                        "Wiele błędów wysyłki",
                        "Nie wysłano $failedCount SMS-ów. Sprawdź połączenie."
                    )
                }
            } else {
                LogManager.log("DEBUG", "SendQueuedSmsWorker", "No SMS to send")
            }
            
            networkManager.close()
            Result.success()
            
        } catch (e: Exception) {
            LogManager.log("ERROR", "SendQueuedSmsWorker", "Failed to send queued SMS: ${e.message}", e.stackTraceToString())
            Result.failure()
        }
    }
}