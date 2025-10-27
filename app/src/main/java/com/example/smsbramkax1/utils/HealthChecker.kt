package com.example.smsbramkax1.utils

import android.content.Context
import com.example.smsbramkax1.storage.SmsQueueDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reprezentuje stan zdrowia systemu SMS Gateway.
 * 
 * ANDROID EXPERT NOTES:
 * - Używamy data class zamiast zwykłej klasy dla immutabilności i automatycznego equals/hashCode
 * - Wszystkie pola są proste typy, co ułatwia serializację i testowanie
 * - lastSendTimestamp jako Long? pozwala na null-check przy braku wysłanych SMS-ów
 */
data class HealthStatus(
    val pendingCount: Int,
    val failedLastHour: Int,
    val failedLastDay: Int,
    val lastSendTimestamp: Long?,
    val isNetworkOk: Boolean,
    val isWifi: Boolean,
    val isMobile: Boolean,
    val totalSent: Int,
    val totalFailed: Int
) {
    /**
     * Zwraca czy system jest w dobrym stanie
     */
    val isHealthy: Boolean
        get() = isNetworkOk && failedLastHour < 5 && pendingCount < 100
        
    /**
     * Zwraca czy system wymaga uwagi
     */
    val needsAttention: Boolean
        get() = !isNetworkOk || failedLastHour >= 5 || pendingCount >= 100
        
    /**
     * Zwraca opis stanu systemu
     */
    val statusDescription: String
        get() = when {
            !isNetworkOk -> "Brak połączenia z internetem"
            failedLastHour >= 10 -> "Krytyczna liczba błędów"
            failedLastHour >= 5 -> "Wiele błędów wysyłki"
            pendingCount >= 100 -> "Duża kolejka oczekujących"
            else -> "System działa poprawnie"
        }
}

/**
 * Sprawdza zdrowie systemu SMS Gateway.
 * 
 * ANDROID EXPERT NOTES:
 * - Używamy withContext(Dispatchers.IO) aby operacje bazy danych nie blokowały głównego wątku
 * - Klasa jest testowalna - wszystkie zależności są wstrzykiwane przez konstruktor
 * - Metoda snapshot jest suspend function, co pozwala na bezpieczne wywołanie z coroutine
 */
class HealthChecker(
    private val context: Context,
    private val smsQueueDao: SmsQueueDao
) {
    /**
     * Pobiera aktualny stan zdrowia systemu
     */
    suspend fun snapshot(): HealthStatus = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 60 * 60 * 1000
        val oneDayAgo = now - 24 * 60 * 60 * 1000
        
        val pending = smsQueueDao.countByStatus("PENDING")
        val failedHour = smsQueueDao.countFailedSince(oneHourAgo)
        val failedDay = smsQueueDao.countFailedSince(oneDayAgo)
        val lastSent = smsQueueDao.lastSentAt()
        val totalSent = smsQueueDao.countByStatus("SENT")
        val totalFailed = smsQueueDao.countByStatus("FAILED")
        
        val networkOk = NetworkState.isOnline(context)
        val isWifi = NetworkState.isWifi(context)
        val isMobile = NetworkState.isMobile(context)
        
        HealthStatus(
            pendingCount = pending,
            failedLastHour = failedHour,
            failedLastDay = failedDay,
            lastSendTimestamp = lastSent,
            isNetworkOk = networkOk,
            isWifi = isWifi,
            isMobile = isMobile,
            totalSent = totalSent,
            totalFailed = totalFailed
        )
    }
    
    /**
     * Sprawdza czy należy wysłać powiadomienie o stanie systemu
     */
    suspend fun shouldNotify(): Boolean {
        val status = snapshot()
        return status.needsAttention
    }
}