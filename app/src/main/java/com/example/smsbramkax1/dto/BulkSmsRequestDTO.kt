package com.example.smsbramkax1.dto

import kotlinx.serialization.Serializable

@Serializable
data class BulkSmsRequestDTO(
    val phoneNumbers: List<String>,
    val messageBody: String,
    val batchId: String? = null,
    val sendDelayMs: Long = 500L, // Default delay between SMS
    val batchSize: Int = 10 // Process in batches of 10
)

@Serializable
data class BulkSmsResponseDTO(
    val batchId: String,
    val totalRecipients: Int,
    val status: BulkSmsStatus,
    val queuedCount: Int = 0,
    val sentCount: Int = 0,
    val failedCount: Int = 0,
    val estimatedDurationMinutes: Int = 0
)

@Serializable
data class BulkSmsProgressDTO(
    val batchId: String,
    val totalRecipients: Int,
    val queuedCount: Int,
    val sentCount: Int,
    val failedCount: Int,
    val status: BulkSmsStatus,
    val startedAt: Long,
    val completedAt: Long? = null,
    val errors: List<String> = emptyList()
)

enum class BulkSmsStatus {
    QUEUED,     // Batch is queued for processing
    PROCESSING, // Currently being processed
    COMPLETED,  // All SMS sent (some may have failed)
    FAILED,     // Batch processing failed
    CANCELLED   // Batch was cancelled
}