package com.example.smsbramkax1.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.smsbramkax1.data.SmsTemplate

@Dao
interface TemplateDao {
    
    @Query("SELECT * FROM sms_templates ORDER BY category ASC, name ASC")
    fun getAllTemplates(): Flow<List<SmsTemplate>>
    
    @Query("SELECT * FROM sms_templates WHERE category = :category ORDER BY name ASC")
    fun getTemplatesByCategory(category: String): Flow<List<SmsTemplate>>
    
    @Query("SELECT * FROM sms_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): SmsTemplate?
    
    @Query("SELECT * FROM sms_templates WHERE name = :name")
    suspend fun getTemplateByName(name: String): SmsTemplate?
    
    @Query("SELECT * FROM sms_templates WHERE name LIKE :searchPattern OR content LIKE :searchPattern ORDER BY name ASC")
    fun searchTemplates(searchPattern: String): Flow<List<SmsTemplate>>
    
    @Query("SELECT DISTINCT category FROM sms_templates WHERE category IS NOT NULL ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>
    
    @Query("SELECT COUNT(*) FROM sms_templates")
    suspend fun getTemplateCount(): Int
    
    @Insert
    suspend fun insertTemplate(template: SmsTemplate): Long
    
    @Update
    suspend fun updateTemplate(template: SmsTemplate)
    
    @Delete
    suspend fun deleteTemplate(template: SmsTemplate)
    
    @Query("DELETE FROM sms_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)
}