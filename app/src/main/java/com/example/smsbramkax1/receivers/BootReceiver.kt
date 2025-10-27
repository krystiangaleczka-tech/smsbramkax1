package com.example.smsbramkax1.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.smsbramkax1.services.SmsForegroundService
import com.example.smsbramkax1.utils.LogManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            
            LogManager.log("INFO", "BootReceiver", "Boot/restart detected, scheduling workers")
            
            scheduleWorkers(context)
            startForegroundService(context)
        }
    }
    
    private fun scheduleWorkers(context: Context) {
        val workManager = WorkManager.getInstance(context)
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()
        
        val fetchWorkRequest = PeriodicWorkRequestBuilder<com.example.smsbramkax1.workers.FetchPendingSmsWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()
        
        val sendWorkRequest = PeriodicWorkRequestBuilder<com.example.smsbramkax1.workers.SendQueuedSmsWorker>(
            5, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "fetch_pending_sms",
            ExistingPeriodicWorkPolicy.UPDATE,
            fetchWorkRequest
        )
        
        workManager.enqueueUniquePeriodicWork(
            "send_queued_sms",
            ExistingPeriodicWorkPolicy.UPDATE,
            sendWorkRequest
        )
        
        LogManager.log("INFO", "BootReceiver", "Workers scheduled successfully")
    }
    
    private fun startForegroundService(context: Context) {
        val serviceIntent = Intent(context, SmsForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        LogManager.log("INFO", "BootReceiver", "Foreground service started")
    }
}