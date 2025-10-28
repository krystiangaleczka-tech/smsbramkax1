// File: storage/SmsDatabase.kt

package com.example.smsbramkax1.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.smsbramkax1.data.SmsMessage
import com.example.smsbramkax1.data.SystemLog
import com.example.smsbramkax1.data.SmsTemplate
import com.example.smsbramkax1.data.Contact

@Database(
    entities = [SmsMessage::class, SystemLog::class, SmsTemplate::class, Contact::class],
    version = 3,
    exportSchema = false
)
abstract class SmsDatabase : RoomDatabase() {
    
    abstract fun smsMessageDao(): SmsMessageDao
    abstract fun logDao(): LogDao
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
                .fallbackToDestructiveMigration(dropAllTables = true) // Clean migration for unified table
                .build()
                INSTANCE = instance
                instance
            }
       }
    }
}