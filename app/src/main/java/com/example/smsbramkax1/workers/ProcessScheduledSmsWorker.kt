package com.example.smsbramkax1.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smsbramkax1.sms.ScheduledSmsManager
import com.example.smsbramkax1.utils.LogManager

class ProcessScheduledSmsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val scheduledSmsManager = ScheduledSmsManager.getInstance(applicationContext)
            val processedCount = scheduledSmsManager.processDueScheduledSms()
            
            LogManager.log("INFO", "ProcessScheduledSmsWorker", "Processed $processedCount due SMS")
            
            // Cleanup old deleted SMS
            scheduledSmsManager.cleanupOldDeletedSms()
            
            Result.success()
        } catch (e: Exception) {
            LogManager.log("ERROR", "ProcessScheduledSmsWorker", "ProcessScheduledSmsWorker failed: ${e.message}")
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "ProcessScheduledSmsWorker"
    }
}