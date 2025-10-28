package com.example.smsbramkax1.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.smsbramkax1.MainActivity
import com.example.smsbramkax1.R
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.utils.LogManager
import com.example.smsbramkax1.utils.Notify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsForegroundService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var notificationManager: NotificationManager
    
    companion object {
        const val CHANNEL_ID = "sms_service_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_UPDATE_STATUS = "com.example.smsbramkax1.UPDATE_STATUS"
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        LogManager.log("INFO", "SmsForegroundService", "Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_UPDATE_STATUS -> updateNotification()
            else -> startForegroundService()
        }
        return START_STICKY
    }
    
    private fun startForegroundService() {
        val notification = Notify.foregroundNotification(this, "Uruchamianie usługi...")
        startForeground(Notify.ID_FOREGROUND, notification)
        
        serviceScope.launch {
            try {
                updateNotificationPeriodically()
            } catch (e: Exception) {
                LogManager.log("ERROR", "SmsForegroundService", "Error in foreground service: ${e.message}")
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SMS Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "SMS Gateway Service Status"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SMS Gateway")
            .setContentText("Service running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    private suspend fun updateNotificationPeriodically() {
        while (true) {
            updateNotification()
            kotlinx.coroutines.delay(30000) // Update every 30 seconds
        }
    }
    
    private fun updateNotification() {
        serviceScope.launch {
            try {
                val database = SmsDatabase.getDatabase(applicationContext)
                val pendingCount = database.smsMessageDao().getCountByStatus("PENDING")
                val scheduledCount = database.smsMessageDao().getCountByStatus("SCHEDULED")
                val failedCount = database.smsMessageDao().getCountByStatus("FAILED")
                
                val contentText = "Oczekujące: $pendingCount | Zaplanowane: $scheduledCount | Błędy: $failedCount"
                
                val notification = Notify.foregroundNotification(this@SmsForegroundService, contentText)
                notificationManager.notify(Notify.ID_FOREGROUND, notification)
                
            } catch (e: Exception) {
                LogManager.log("ERROR", "SmsForegroundService", "Error updating notification: ${e.message}")
                Notify.error(this@SmsForegroundService, "Błąd usługi", "Problem z aktualizacją statusu: ${e.message}")
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        LogManager.log("INFO", "SmsForegroundService", "Service destroyed")
    }
}