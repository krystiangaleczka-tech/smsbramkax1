package com.example.smsbramkax1.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContactResponseDTO(
    val id: Long,
    val contactId: String,
    val name: String?,
    val phoneNumber: String?,
    val email: String?,
    val lastUpdated: Long
)

@Serializable
data class SyncContactsRequestDTO(
    val forceSync: Boolean = false
)

@Serializable
data class SyncContactsResponseDTO(
    val syncedCount: Int,
    val totalCount: Int,
    val lastSyncTime: Long
)