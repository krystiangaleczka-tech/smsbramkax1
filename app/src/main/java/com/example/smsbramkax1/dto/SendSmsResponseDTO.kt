// File: dto/SendSmsResponseDTO.kt

package com.example.smsbramkax1.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendSmsResponseDTO(
    val success: Boolean,
    val message: String,
    val smsId: Long? = null
)