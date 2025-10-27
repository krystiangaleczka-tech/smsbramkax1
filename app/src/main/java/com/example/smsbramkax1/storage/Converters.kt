// File: storage/Converters.kt

package com.example.smsbramkax1.storage

import androidx.room.TypeConverter
import com.example.smsbramkax1.data.SmsStatus
import com.example.smsbramkax1.data.ScheduledSmsStatus

class Converters {

    @TypeConverter
    fun fromSmsStatus(value: SmsStatus): String {
        return value.name
    }

    @TypeConverter
    fun toSmsStatus(value: String): SmsStatus {
        return SmsStatus.valueOf(value)
    }

    @TypeConverter
    fun fromScheduledSmsStatus(value: ScheduledSmsStatus): String {
        return value.name
    }

    @TypeConverter
    fun toScheduledSmsStatus(value: String): ScheduledSmsStatus {
        return ScheduledSmsStatus.valueOf(value)
    }
}