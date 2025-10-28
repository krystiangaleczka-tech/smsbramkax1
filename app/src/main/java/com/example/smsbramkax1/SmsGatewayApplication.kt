package com.example.smsbramkax1

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager

class SmsGatewayApplication : Application(), Configuration.Provider {
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG) // Always use DEBUG level for now
            .build()
    
    override fun onCreate() {
        super.onCreate()
        Log.d("SmsGatewayApp", "Application onCreate started")
        
        // Inicjalizacja WorkManager z naszą konfiguracją
        try {
            WorkManager.initialize(this, workManagerConfiguration)
            Log.d("SmsGatewayApp", "WorkManager initialized successfully")
        } catch (e: Exception) {
            Log.e("SmsGatewayApp", "Failed to initialize WorkManager", e)
        }
        
        Log.d("SmsGatewayApp", "Application onCreate completed")
    }
}