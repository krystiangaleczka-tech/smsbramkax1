package com.example.smsbramkax1.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "scheduled_sms",
    indices = [
        Index(value = ["status"]),
        Index(value = ["scheduledFor"]),
        Index(value = ["phoneNumber"])
    ]
)
data class ScheduledSms(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val messageBody: String,
    val scheduledFor: Long, // timestamp when SMS should be sent
    val status: ScheduledSmsStatus = ScheduledSmsStatus.SCHEDULED,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null,
    val sentAt: Long? = null,
    val deliveredAt: Long? = null,
    val errorMessage: String? = null
)

enum class ScheduledSmsStatus {
    SCHEDULED, // SMS is scheduled for future sending
    QUEUED,    // SMS is queued for immediate sending
    SENT,       // SMS has been sent successfully
    DELIVERED,  // SMS has been delivered to recipient
    DELETED,    // SMS was cancelled/deleted before sending
    FAILED      // SMS failed to send
}