package com.example.smsbramkax1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val externalId: String,
    val phoneNumber: String,
    val messageBody: String,
    val status: String, // QUEUED, SENT, DELIVERED, FAILED
    val queuedAt: Long,
    val scheduledFor: Long,
    val sentAt: Long? = null,
    val deliveredAt: Long? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0
)