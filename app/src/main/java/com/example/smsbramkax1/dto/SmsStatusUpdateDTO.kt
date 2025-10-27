// File: dto/SmsStatusUpdateDTO.kt

package com.example.smsbramkax1.dto

import kotlinx.serialization.Serializable

@Serializable
data class SmsStatusUpdateDTO(
    val smsId: Long,
    val status: String, // SENT, FAILED
    val sentAt: Long,
    val errorMessage: String? = null
)