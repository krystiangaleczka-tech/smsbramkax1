package com.example.smsbramkax1.sms

import android.content.Context
import com.example.smsbramkax1.data.SmsTemplate
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.storage.TemplateDao
import com.example.smsbramkax1.utils.LogManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TemplateManager private constructor(private val context: Context) {
    
    private val database: SmsDatabase = SmsDatabase.getDatabase(context)
    private val templateDao: TemplateDao = database.templateDao()
    private val templateEngine: TemplateEngine = TemplateEngine()
    
    companion object {
        @Volatile
        private var INSTANCE: TemplateManager? = null
        
        fun getInstance(context: Context): TemplateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TemplateManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    suspend fun createTemplate(
        name: String,
        content: String,
        category: String? = null
    ): Result<Long> {
        return try {
            // Validate template
            val validation = templateEngine.validateTemplate(content)
            if (!validation.isValid) {
                return Result.failure(IllegalArgumentException(validation.message))
            }
            
            // Check if name already exists
            val existing = templateDao.getTemplateByName(name)
            if (existing != null) {
                return Result.failure(IllegalArgumentException("Template with name '$name' already exists"))
            }
            
            val variables = templateEngine.extractVariables(content)
            val variablesJson = templateEngine.variablesToJson(variables)
            
            val template = SmsTemplate(
                name = name,
                content = content,
                variables = variablesJson,
                category = category
            )
            
            val id = templateDao.insertTemplate(template)
            LogManager.log("INFO", "TemplateManager", "Template created with ID: $id, name: $name")
            Result.success(id)
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateManager", "Failed to create template: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun updateTemplate(
        id: Long,
        name: String? = null,
        content: String? = null,
        category: String? = null
    ): Result<Unit> {
        return try {
            val existing = templateDao.getTemplateById(id)
                ?: return Result.failure(IllegalArgumentException("Template not found"))
            
            val newContent = content ?: existing.content
            val validation = templateEngine.validateTemplate(newContent)
            if (!validation.isValid) {
                return Result.failure(IllegalArgumentException(validation.message))
            }
            
            val newName = name ?: existing.name
            if (newName != existing.name) {
                val nameExists = templateDao.getTemplateByName(newName)
                if (nameExists != null) {
                    return Result.failure(IllegalArgumentException("Template with name '$newName' already exists"))
                }
            }
            
            val variables = templateEngine.extractVariables(newContent)
            val variablesJson = templateEngine.variablesToJson(variables)
            
            val updated = existing.copy(
                name = newName,
                content = newContent,
                variables = variablesJson,
                category = category ?: existing.category,
                updatedAt = System.currentTimeMillis()
            )
            
            templateDao.updateTemplate(updated)
            LogManager.log("INFO", "TemplateManager", "Template updated: ID $id")
            Result.success(Unit)
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateManager", "Failed to update template: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun deleteTemplate(id: Long): Result<Unit> {
        return try {
            val existing = templateDao.getTemplateById(id)
                ?: return Result.failure(IllegalArgumentException("Template not found"))
            
            templateDao.deleteTemplateById(id)
            LogManager.log("INFO", "TemplateManager", "Template deleted: ID $id, name: ${existing.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateManager", "Failed to delete template: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun renderTemplate(templateId: Long, variables: Map<String, String>): Result<String> {
        return try {
            val template = templateDao.getTemplateById(templateId)
                ?: return Result.failure(IllegalArgumentException("Template not found"))
            
            val rendered = templateEngine.renderTemplate(template.content, variables)
            LogManager.log("INFO", "TemplateManager", "Template rendered: ID $templateId")
            Result.success(rendered)
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateManager", "Failed to render template: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun renderTemplateByName(templateName: String, variables: Map<String, String>): Result<String> {
        return try {
            val template = templateDao.getTemplateByName(templateName)
                ?: return Result.failure(IllegalArgumentException("Template '$templateName' not found"))
            
            val rendered = templateEngine.renderTemplate(template.content, variables)
            LogManager.log("INFO", "TemplateManager", "Template rendered by name: $templateName")
            Result.success(rendered)
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateManager", "Failed to render template by name: ${e.message}")
            Result.failure(e)
        }
    }
    
    fun getAllTemplates(): Flow<List<SmsTemplate>> {
        return templateDao.getAllTemplates()
    }
    
    fun getTemplatesByCategory(category: String): Flow<List<SmsTemplate>> {
        return templateDao.getTemplatesByCategory(category)
    }
    
    fun searchTemplates(query: String): Flow<List<SmsTemplate>> {
        val searchPattern = "%$query%"
        return templateDao.searchTemplates(searchPattern)
    }
    
    fun getCategories(): Flow<List<String>> {
        return templateDao.getCategories()
    }
    
    suspend fun getTemplateById(id: Long): SmsTemplate? {
        return templateDao.getTemplateById(id)
    }
    
    suspend fun getTemplateByName(name: String): SmsTemplate? {
        return templateDao.getTemplateByName(name)
    }
    
    suspend fun getTemplateCount(): Int {
        return templateDao.getTemplateCount()
    }
    
    fun getTemplateEngine(): TemplateEngine {
        return templateEngine
    }
    
    suspend fun initializeDefaultTemplates() {
        try {
            val count = getTemplateCount()
            if (count > 0) {
                LogManager.log("INFO", "TemplateManager", "Templates already initialized ($count templates)")
                return
            }
            
            val defaultTemplates = listOf(
                Triple(
                    "Przypomnienie o wizycie",
                    "Witaj {{name}}! Przypominamy o wizycie {{date}} o godzinie {{time}}. Usługa: {{service}}. Do zobaczenia!",
                    "REMINDERS"
                ),
                Triple(
                    "Potwierdzenie wizyty",
                    "Dziękujemy {{name}}! Twoja wizyta {{date}} o {{time}} została potwierdzona. Usługa: {{service}}. Czekamy na Ciebie!",
                    "APPOINTMENTS"
                ),
                Triple(
                    "Anulowanie wizyty",
                    "Niestety, Twoja wizyta {{date}} o {{time}} została anulowana. Skontaktuj się z nami w celu rezerwacji nowego terminu.",
                    "APPOINTMENTS"
                ),
                Triple(
                    "Promocja",
                    "Witaj {{name}}! Specjalna oferta na {{service}} tylko za {{price}}! Zarezerwuj wizytę już dziś. Tel: {{phone}}",
                    "PROMOTIONS"
                ),
                Triple(
                    "Informacja",
                    "Szanowny Kliencie, informujemy o zmianie godzin pracy. Nowe godziny: od 9:00 do 21:00. Dziękujemy za zrozumienie.",
                    "INFO"
                )
            )
            
            for ((name, content, category) in defaultTemplates) {
                createTemplate(name, content, category)
            }
            
            LogManager.log("INFO", "TemplateManager", "Default templates initialized (${defaultTemplates.size} templates)")
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateManager", "Failed to initialize default templates: ${e.message}")
        }
    }
}