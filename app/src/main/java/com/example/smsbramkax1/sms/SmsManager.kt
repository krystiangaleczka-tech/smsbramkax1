package com.example.smsbramkax1.sms

import android.content.Context
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import com.example.smsbramkax1.data.SmsMessage
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.utils.LogManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsManager(private val context: Context) {
    
    @Suppress("DEPRECATION")
    private val smsManager = SmsManager.getDefault()
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    private val smsMessageDao = SmsDatabase.getDatabase(context).smsMessageDao()
    
    suspend fun sendSms(phoneNumber: String, message: String): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                if (!isSmsCapable()) {
                    LogManager.log("ERROR", "SmsManager", "Device is not SMS capable")
                    return@withContext Result.failure(Exception("Device not SMS capable"))
                }
                
                if (!isValidPhoneNumber(phoneNumber)) {
                    LogManager.log("ERROR", "SmsManager", "Invalid phone number: $phoneNumber")
                    return@withContext Result.failure(Exception("Invalid phone number"))
                }
                
                if (message.isEmpty()) {
                    LogManager.log("ERROR", "SmsManager", "Empty message")
                    return@withContext Result.failure(Exception("Empty message"))
                }
                
                // 1. Create message with PENDING status
                val smsMessage = SmsMessage(
                    phoneNumber = phoneNumber,
                    messageBody = message,
                    status = "PENDING",
                    isScheduled = false
                )
                val id = smsMessageDao.insertMessage(smsMessage)
                LogManager.log("INFO", "SmsManager", "Created SMS message with ID: $id")
                
                // 2. Try to send
                val success = tryToSendSms(phoneNumber, message)
                
                // 3. Update status
                if (success) {
                    smsMessageDao.updateMessageStatus(id, "SENT", System.currentTimeMillis())
                    LogManager.log("INFO", "SmsManager", "SMS sent successfully to $phoneNumber, ID: $id")
                    Result.success(id)
                } else {
                    smsMessageDao.updateMessageStatus(id, "FAILED")
                    LogManager.log("ERROR", "SmsManager", "SMS failed to send to $phoneNumber, ID: $id")
                    Result.failure(Exception("SMS send failed"))
                }
                
            } catch (e: Exception) {
                LogManager.log("ERROR", "SmsManager", "Failed to send SMS to $phoneNumber: ${e.message}", e.stackTraceToString())
                Result.failure(e)
            }
        }
    }
    
    fun tryToSendSms(phoneNumber: String, message: String): Boolean {
        return try {
            val parts = smsManager?.divideMessage(message)
            if (parts != null && parts.size > 1) {
                smsManager?.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
                )
            } else {
                smsManager?.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
                )
            }
            true
        } catch (e: Exception) {
            LogManager.log("ERROR", "SmsManager", "Failed to send SMS to $phoneNumber: ${e.message}")
            false
        }
    }
    
    fun isSmsCapable(): Boolean {
        return try {
            // Alternative way to check SMS capability without deprecated API
            telephonyManager != null && smsManager != null
        } catch (e: Exception) {
            LogManager.log("ERROR", "SmsManager", "Error checking SMS capability: ${e.message}")
            false
        }
    }
    
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val cleanedNumber = phoneNumber.replace(Regex("[^+0-9]"), "")
        return cleanedNumber.length >= 9 && (cleanedNumber.startsWith("+") || cleanedNumber.startsWith("0"))
    }
    
    fun getNetworkOperator(): String? {
        return try {
            telephonyManager?.networkOperatorName
        } catch (e: Exception) {
            LogManager.log("ERROR", "SmsManager", "Error getting network operator: ${e.message}")
            null
        }
    }
}