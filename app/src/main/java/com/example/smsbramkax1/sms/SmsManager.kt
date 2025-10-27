package com.example.smsbramkax1.sms

import android.content.Context
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import com.example.smsbramkax1.utils.LogManager

class SmsManager(private val context: Context) {
    
    @Suppress("DEPRECATION")
    private val smsManager = SmsManager.getDefault()
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    
    fun sendSms(phoneNumber: String, message: String): Boolean {
        return try {
            if (!isSmsCapable()) {
                LogManager.log("ERROR", "SmsManager", "Device is not SMS capable")
                return false
            }
            
            if (!isValidPhoneNumber(phoneNumber)) {
                LogManager.log("ERROR", "SmsManager", "Invalid phone number: $phoneNumber")
                return false
            }
            
            if (message.isEmpty()) {
                LogManager.log("ERROR", "SmsManager", "Empty message")
                return false
            }
            
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
            
            LogManager.log("INFO", "SmsManager", "SMS sent successfully to $phoneNumber")
            true
            
        } catch (e: Exception) {
            LogManager.log("ERROR", "SmsManager", "Failed to send SMS to $phoneNumber: ${e.message}", e.stackTraceToString())
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
        return cleanedNumber.length >= 9 && cleanedNumber.startsWith("+") || cleanedNumber.startsWith("0")
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