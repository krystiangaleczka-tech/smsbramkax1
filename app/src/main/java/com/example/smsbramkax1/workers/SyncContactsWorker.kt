package com.example.smsbramkax1.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smsbramkax1.utils.ContactManager
import com.example.smsbramkax1.utils.LogManager

class SyncContactsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val contactManager = ContactManager.getInstance(applicationContext)
            val syncResult = contactManager.syncContacts()
            
            if (syncResult.isSuccess) {
                val syncedCount = syncResult.getOrNull() ?: 0
                LogManager.log("INFO", "SyncContactsWorker", "Synced $syncedCount contacts")
                Result.success()
            } else {
                LogManager.log("ERROR", "SyncContactsWorker", "SyncContactsWorker failed: ${syncResult.exceptionOrNull()?.message}")
                Result.failure()
            }
        } catch (e: Exception) {
            LogManager.log("ERROR", "SyncContactsWorker", "SyncContactsWorker failed: ${e.message}")
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "SyncContactsWorker"
    }
}