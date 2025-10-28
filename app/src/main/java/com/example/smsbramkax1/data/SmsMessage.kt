package com.example.smsbramkax1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val messageBody: String,
    val status: String, // PENDING, QUEUED, SENT, DELIVERED, FAILED, SCHEDULED
    val isScheduled: Boolean = false,
    val scheduledFor: Long? = null,
    val scheduledAt: Long? = null, // Legacy compatibility
    val createdAt: Long = System.currentTimeMillis(),
    val sentAt: Long? = null,
    val deliveredAt: Long? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val priority: Int = 5,
    val batchId: String? = null,
    val category: String? = null
) {
    // Legacy compatibility property
    val message: String get() = messageBody
}