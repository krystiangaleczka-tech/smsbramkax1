// File: data/SmsQueue.kt

package com.example.smsbramkax1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sms_queue")
data class SmsQueue(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val message: String,
    val messageBody: String = message, // Alias for backward compatibility
    val status: SmsStatus = SmsStatus.PENDING,
    val priority: Int = 5,
    val createdAt: Long = System.currentTimeMillis(),
    val scheduledAt: Long? = null,
    val sentAt: Long? = null,
    val retryCount: Int = 0,
    val errorMessage: String? = null,
    val batchId: String? = null,
    val category: String? = null
)