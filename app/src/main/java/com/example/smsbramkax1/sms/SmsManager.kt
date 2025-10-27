// File: sms/SmsManager.kt

package com.example.smsbramkax1.sms

import android.content.Context
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import com.example.smsbramkax1.data.SmsQueue
import com.example.smsbramkax1.data.SmsStatus
import com.example.smsbramkax1.storage.SmsQueueDao
import com.example.smsbramkax1.utils.LogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsManagerHelper(
    private val context: Context,
    private val smsQueueDao: SmsQueueDao,
    private val logManager: LogManager
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    private val smsManager = SmsManager.getDefault()

    fun sendSms(phoneNumber: String, message: String, priority: Int = 5) {
        scope.launch {
            val smsQueue = SmsQueue(
                phoneNumber = phoneNumber,
                message = message,
                status = SmsStatus.PENDING,
                priority = priority,
                createdAt = System.currentTimeMillis()
            )
            val id = smsQueueDao.insertSms(smsQueue)
            logManager.logInfo("SMS", "SMS dodany do kolejki: $phoneNumber", id)
            processSms(id)
        }
    }

    private suspend fun processSms(smsId: Long) {
        val sms = smsQueueDao.getSmsById(smsId) ?: return

        try {
            smsManager?.sendTextMessage(
                sms.phoneNumber,
                null,
                sms.message,
                null,
                null
            )

            smsQueueDao.updateSmsStatus(smsId, SmsStatus.SENT, System.currentTimeMillis())
            logManager.logInfo("SMS", "SMS wysłany pomyślnie: ${sms.phoneNumber}", smsId)

        } catch (e: Exception) {
            smsQueueDao.updateSmsStatus(smsId, SmsStatus.FAILED, System.currentTimeMillis())
            logManager.logError("SMS", "Błąd wysyłania SMS: ${e.message}", smsId, e.stackTraceToString())
        }
    }

    suspend fun retryFailedSms() {
        val failedSms = smsQueueDao.getSmsByStatus(SmsStatus.FAILED)
        failedSms.forEach { sms ->
            if (sms.retryCount < 3) {
                smsQueueDao.incrementRetryCount(sms.id)
                processSms(sms.id)
            }
        }
    }
}