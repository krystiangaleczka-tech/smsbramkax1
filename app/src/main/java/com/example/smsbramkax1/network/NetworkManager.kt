// File: network/NetworkManager.kt

package com.example.smsbramkax1.network

import com.example.smsbramkax1.dto.SmsStatusUpdateDTO
import com.example.smsbramkax1.dto.SendSmsRequestDTO
import com.example.smsbramkax1.dto.SendSmsResponseDTO
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

    fun close() {
        client.close()
    }
}