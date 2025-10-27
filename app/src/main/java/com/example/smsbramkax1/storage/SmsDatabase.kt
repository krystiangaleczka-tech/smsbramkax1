// File: storage/SmsDatabase.kt

package com.example.smsbramkax1.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smsbramkax1.data.SmsQueue
import com.example.smsbramkax1.data.SystemLog
import com.example.smsbramkax1.data.ScheduledSms
import com.example.smsbramkax1.data.SmsTemplate
import com.example.smsbramkax1.data.Contact

@Database(
    entities = [SmsQueue::class, SystemLog::class, ScheduledSms::class, SmsTemplate::class, Contact::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SmsDatabase : RoomDatabase() {
    
    abstract fun smsQueueDao(): SmsQueueDao
    abstract fun logDao(): LogDao
    abstract fun scheduledSmsDao(): ScheduledSmsDao
    abstract fun templateDao(): TemplateDao
    abstract fun contactDao(): ContactDao
    
    companion object {
        @Volatile
        private var INSTANCE: SmsDatabase? = null
        
        fun getDatabase(context: Context): SmsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmsDatabase::class.java,
                    "sms_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true) // ← ZMIENIONE
                .build()
                INSTANCE = instance
                instance
            }
       }
    }
}