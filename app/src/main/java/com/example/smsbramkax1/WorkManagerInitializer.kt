package com.example.smsbramkax1

import android.content.Context
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.smsbramkax1.utils.LogManager

class WorkManagerInitializer : Initializer<WorkManager> {
    
    override fun create(context: Context): WorkManager {
        LogManager.log("INFO", "WorkManagerInitializer", "Initializing WorkManager")
        
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(if (android.util.Log.isLoggable("WorkManager", android.util.Log.DEBUG)) {
                android.util.Log.DEBUG
            } else {
                android.util.Log.INFO
            })
            .build()
        
        WorkManager.initialize(context, config)
        return WorkManager.getInstance(context)
    }
    
    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}