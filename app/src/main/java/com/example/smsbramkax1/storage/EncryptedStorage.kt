package com.example.smsbramkax1.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class EncryptedStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiToken(token: String) {
        prefs.edit().putString("api_token", token).apply()
    }

    fun getApiToken(): String {
        return prefs.getString("api_token", "") ?: ""
    }

    fun generateNewToken(): String {
        val token = "sk_live_" + java.util.UUID.randomUUID().toString().replace("-", "")
        saveApiToken(token)
        return token
    }

    fun clearToken() {
        prefs.edit().remove("api_token").apply()
    }
}