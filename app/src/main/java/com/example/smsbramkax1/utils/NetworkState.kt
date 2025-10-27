package com.example.smsbramkax1.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Utility do sprawdzania stanu połączenia sieciowego.
 * 
 * ANDROID EXPERT NOTES:
 * - Używamy nowego API NetworkCapabilities (zamiast przestarzałego NetworkInfo)
 * - Sprawdzamy rzeczywistą możliwość połączenia z internetem, nie tylko stan połączenia
 * - Funkcja jest deterministyczna i bezpieczna do wywołania z dowolnego wątku
 */
object NetworkState {
    
    /**
     * Sprawdza czy urządzenie ma aktywne połączenie z internetem
     * @param context Kontekst aplikacji
     * @return true jeśli jest dostępne połączenie internetowe, false w przeciwnym razie
     */
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
            
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Sprawdza czy połączenie jest przez WiFi
     */
    fun isWifi(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
            
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    /**
     * Sprawdza czy połączenie jest przez sieć komórkową
     */
    fun isMobile(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
            
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
}