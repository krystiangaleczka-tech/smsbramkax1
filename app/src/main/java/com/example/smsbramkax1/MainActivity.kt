package com.example.smsbramkax1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smsbramkax1.ui.MainApp
import com.example.smsbramkax1.ui.theme.Smsbramkax1Theme
import com.example.smsbramkax1.utils.Notify

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicjalizacja kanałów powiadomień
        Notify.ensureChannels(applicationContext)
        
        setContent {
            Smsbramkax1Theme {
                MainApp()
            }
        }
    }
}