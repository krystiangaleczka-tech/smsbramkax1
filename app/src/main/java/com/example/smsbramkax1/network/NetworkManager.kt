// File: network/NetworkManager.kt

package com.example.smsbramkax1.network

import com.example.smsbramkax1.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class NetworkManager(
    private val baseUrl: String,
    private val apiKey: String
) {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
        }
    }

    suspend fun fetchPendingSms(): List<SendSmsRequestDTO> {
        return try {
            client.get("$baseUrl/sms/pending") {
                header("Authorization", "Bearer $apiKey")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendSmsStatus(statusUpdate: SmsStatusUpdateDTO): Boolean {
        return try {
            val response = client.post("$baseUrl/sms/status") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(statusUpdate)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun registerDevice(deviceId: String, deviceName: String): Boolean {
        return try {
            val response = client.post("$baseUrl/device/register") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(mapOf("deviceId" to deviceId, "deviceName" to deviceName))
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    // Scheduled SMS endpoints
    suspend fun scheduleSms(request: ScheduledSmsRequestDTO): ScheduledSmsResponseDTO? {
        return try {
            client.post("$baseUrl/sms/schedule") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getScheduledSms(): List<ScheduledSmsResponseDTO> {
        return try {
            client.get("$baseUrl/sms/schedule") {
                header("Authorization", "Bearer $apiKey")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateScheduledSms(id: Long, request: UpdateScheduledSmsRequestDTO): Boolean {
        return try {
            val response = client.put("$baseUrl/sms/schedule/$id") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun cancelScheduledSms(id: Long): Boolean {
        return try {
            val response = client.delete("$baseUrl/sms/schedule/$id") {
                header("Authorization", "Bearer $apiKey")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    // Templates endpoints
    suspend fun getTemplates(): List<TemplateResponseDTO> {
        return try {
            client.get("$baseUrl/templates") {
                header("Authorization", "Bearer $apiKey")
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createTemplate(request: TemplateRequestDTO): TemplateResponseDTO? {
        return try {
            client.post("$baseUrl/templates") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateTemplate(id: Long, request: TemplateRequestDTO): Boolean {
        return try {
            val response = client.put("$baseUrl/templates/$id") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTemplate(id: Long): Boolean {
        return try {
            val response = client.delete("$baseUrl/templates/$id") {
                header("Authorization", "Bearer $apiKey")
            }
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    suspend fun renderTemplate(request: RenderTemplateRequestDTO): RenderTemplateResponseDTO? {
        return try {
            client.post("$baseUrl/templates/${request.templateId}/render") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    // Bulk SMS endpoints
    suspend fun sendBulkSms(request: BulkSmsRequestDTO): BulkSmsResponseDTO? {
        return try {
            client.post("$baseUrl/sms/bulk") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getBulkSmsStatus(batchId: String): BulkSmsProgressDTO? {
        return try {
            client.get("$baseUrl/sms/bulk/$batchId") {
                header("Authorization", "Bearer $apiKey")
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getBulkSmsProgress(batchId: String): BulkSmsProgressDTO? {
        return try {
            client.get("$baseUrl/sms/bulk/$batchId/progress") {
                header("Authorization", "Bearer $apiKey")
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    // Contacts endpoints
    suspend fun searchContacts(query: String): List<ContactResponseDTO> {
        return try {
            client.get("$baseUrl/contacts/search") {
                header("Authorization", "Bearer $apiKey")
                parameter("q", query)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getContacts(limit: Int = 50, offset: Int = 0): List<ContactResponseDTO> {
        return try {
            client.get("$baseUrl/contacts") {
                header("Authorization", "Bearer $apiKey")
                parameter("limit", limit)
                parameter("offset", offset)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun syncContacts(request: SyncContactsRequestDTO = SyncContactsRequestDTO()): SyncContactsResponseDTO? {
        return try {
            client.post("$baseUrl/contacts/sync") {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    fun close() {
        client.close()
    }
}