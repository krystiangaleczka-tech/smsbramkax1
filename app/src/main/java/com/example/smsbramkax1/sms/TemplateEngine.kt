package com.example.smsbramkax1.sms

import com.example.smsbramkax1.utils.LogManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.encodeToJsonElement
import java.util.regex.Pattern

class TemplateEngine {
    
    companion object {
        private const val VARIABLE_PATTERN = "\\{\\{(\\w+)\\}\\}"
        private val regex = Pattern.compile(VARIABLE_PATTERN)
    }
    
    fun renderTemplate(template: String, variables: Map<String, String>): String {
        return try {
            var result = template
            val matcher = regex.matcher(template)
            
            while (matcher.find()) {
                val variableName = matcher.group(1) ?: continue
                val replacement = variables[variableName] ?: "{{${variableName}}}"
                result = result.replace(matcher.group(), replacement)
            }
            
            result
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateEngine", "Error rendering template: ${e.message}")
            template
        }
    }
    
    fun extractVariables(template: String): List<String> {
        return try {
            val variables = mutableSetOf<String>()
            val matcher = regex.matcher(template)
            
            while (matcher.find()) {
                matcher.group(1)?.let { variables.add(it) }
            }
            
            variables.toList()
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateEngine", "Error extracting variables from template: ${e.message}")
            emptyList()
        }
    }
    
    fun validateTemplate(template: String): TemplateValidationResult {
        return try {
            val variables = extractVariables(template)
            val hasVariables = variables.isNotEmpty()
            
            if (template.isBlank()) {
                TemplateValidationResult(false, "Template cannot be empty")
            } else if (template.length > 1000) {
                TemplateValidationResult(false, "Template too long (max 1000 characters)")
            } else {
                TemplateValidationResult(true, "Template is valid", variables)
            }
        } catch (e: Exception) {
            TemplateValidationResult(false, "Template validation error: ${e.message}")
        }
    }
    
    fun variablesToJson(variables: List<String>): String {
        return try {
            val jsonArray = buildJsonArray {
                variables.forEach { variable ->
                    add(JsonPrimitive(variable))
                }
            }
            jsonArray.toString()
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateEngine", "Error converting variables to JSON: ${e.message}")
            "[]"
        }
    }
    
    fun variablesFromJson(json: String?): List<String> {
        return try {
            if (json.isNullOrBlank()) return emptyList()
            
            val jsonArray = Json.decodeFromString<JsonArray>(json)
            jsonArray.map { it.toString().replace("\"", "") }
        } catch (e: Exception) {
            LogManager.log("ERROR", "TemplateEngine", "Error parsing variables from JSON: ${e.message}")
            emptyList()
        }
    }
    
    fun getCommonVariables(): List<TemplateVariable> {
        return listOf(
            TemplateVariable("name", "Imię klienta"),
            TemplateVariable("time", "Godzina wizyty"),
            TemplateVariable("date", "Data wizyty"),
            TemplateVariable("service", "Usługa"),
            TemplateVariable("price", "Cena"),
            TemplateVariable("address", "Adres"),
            TemplateVariable("phone", "Telefon kontaktowy"),
            TemplateVariable("company", "Nazwa firmy"),
            TemplateVariable("confirmation_code", "Kod potwierdzający"),
            TemplateVariable("cancellation_link", "Link do anulowania")
        )
    }
}

data class TemplateValidationResult(
    val isValid: Boolean,
    val message: String,
    val variables: List<String> = emptyList()
)

data class TemplateVariable(
    val name: String,
    val description: String
)