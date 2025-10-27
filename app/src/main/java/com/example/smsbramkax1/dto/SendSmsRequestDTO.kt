// File: dto/SendSmsRequestDTO.kt

package com.example.smsbramkax1.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendSmsRequestDTO(
    val id: Long,
    val phoneNumber: String,
    val message: String,
    val scheduledAt: Long? = null,
    val priority: Int = 5
)