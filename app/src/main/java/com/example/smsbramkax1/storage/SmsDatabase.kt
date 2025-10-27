package com.example.smsbramkax1.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smsbramkax1.data.SmsQueue
import com.example.smsbramkax1.data.SystemLog

@Database(
    entities = [SmsQueue::class, SystemLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmsDatabase : RoomDatabase() {
    abstract fun smsQueueDao(): SmsQueueDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: SmsDatabase? = null

        fun getDatabase(context: Context): SmsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmsDatabase::class.java,
                    "sms_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}