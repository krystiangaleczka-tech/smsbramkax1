package com.example.smsbramkax1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_logs")
data class SystemLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = 0L,
    val level: String = "", // DEBUG, INFO, WARN, ERROR
    val tag: String = "",
    val message: String = "",
    val details: String? = null
)