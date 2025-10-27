package com.example.smsbramkax1.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "sms_templates",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["category"])
    ]
)
data class SmsTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val content: String,
    val variables: String? = null, // JSON array of variable names
    val category: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)

enum class TemplateCategory(val displayName: String) {
    REMINDERS("Przypomnienia"),
    PROMOTIONS("Promocje"),
    INFO("Informacje"),
    APPOINTMENTS("Wizyty"),
    GENERAL("Ogólne")
}