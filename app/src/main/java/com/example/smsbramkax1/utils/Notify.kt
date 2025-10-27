package com.example.smsbramkax1.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.smsbramkax1.R

/**
 * Zarządza powiadomieniami systemowymi dla aplikacji SMS Gateway.
 * Tworzy kanały powiadomień i udostępnia metody do wysyłania różnych typów powiadomień.
 * 
 * ANDROID EXPERT NOTES:
 * - Używamy oddzielnych kanałów dla różnych typów powiadomień, aby użytkownicy mogli zarządzać nimi niezależnie
 * - Kanał STATUS ma niski priorytet (nie przeszkadza użytkownikowi)
 * - Kanał ERRORS ma wysoki priorytet (ważne powiadomienia o błędach)
 * - Foreground notification jest ongoing, aby system nie zabił naszej usługi
 */
object Notify {

    const val CHANNEL_STATUS = "sms_status"
    const val CHANNEL_ERRORS = "sms_errors"
    const val ID_FOREGROUND = 1001
    const val ID_ERROR = 2001

    /**
     * Tworzy kanały powiadomień (dla Android 8.0+)
     * Powinno być wywołane podczas startu aplikacji
     */
    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val status = NotificationChannel(
                CHANNEL_STATUS,
                "Status SMS Gateway",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Powiadomienia o statusie działania bramki SMS"
                setShowBadge(false)
            }
            
            val errors = NotificationChannel(
                CHANNEL_ERRORS,
                "Błędy SMS Gateway",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Krytyczne błędy i problemy z działaniem bramki"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(status)
            nm.createNotificationChannel(errors)
        }
    }

    /**
     * Tworzy powiadomienie dla usługi foreground
     */
    fun foregroundNotification(context: Context, text: String) =
        NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SMS Gateway działa")
            .setContentText(text)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

    /**
     * Wysyła powiadomienie o błędzie
     */
    fun error(context: Context, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                // Brak uprawnień do powiadomień - logujemy i nie wysyłamy
                return
            }
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ERRORS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(ID_ERROR, notification)
        } catch (e: SecurityException) {
            // Obsługa przypadku gdy uprawnienia zostały odebrane
            // Można tu dodać logowanie do Timber
        }
    }
    
    /**
     * Wysyła powiadomienie o sukcesie (opcjonalnie)
     */
    fun success(context: Context, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                // Brak uprawnień do powiadomień - logujemy i nie wysyłamy
                return
            }
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_STATUS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // Obsługa przypadku gdy uprawnienia zostały odebrane
            // Można tu dodać logowanie do Timber
        }
    }
}