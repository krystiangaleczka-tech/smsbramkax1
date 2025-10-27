package com.example.smsbramkax1.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "contacts_cache",
    indices = [
        Index(value = ["contactId"], unique = true),
        Index(value = ["name"]),
        Index(value = ["phoneNumber"])
    ]
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: String, // Android system contact ID
    val name: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)