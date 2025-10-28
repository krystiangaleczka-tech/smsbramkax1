package com.example.smsbramkax1.dto

import kotlinx.serialization.Serializable

@Serializable
data class ScheduledSmsRequestDTO(
    val name: String,
    val phoneNumber: String,
    val messageBody: String,
    val scheduledFor: Long // timestamp
)

@Serializable
data class ScheduledSmsResponseDTO(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val messageBody: String,
    val scheduledFor: Long,
    val status: String, // Now using String instead of ScheduledSmsStatus
    val createdAt: Long,
    val updatedAt: Long?,
    val sentAt: Long?,
    val deliveredAt: Long?,
    val errorMessage: String?
)

@Serializable
data class UpdateScheduledSmsRequestDTO(
    val name: String? = null,
    val phoneNumber: String? = null,
    val messageBody: String? = null,
    val scheduledFor: Long? = null
)