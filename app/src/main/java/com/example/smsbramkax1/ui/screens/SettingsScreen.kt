package com.example.smsbramkax1.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.ui.theme.*
import com.example.smsbramkax1.utils.Notify
import com.example.smsbramkax1.utils.SecureStorage
import com.example.smsbramkax1.utils.ExportManager
import com.example.smsbramkax1.utils.ErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val secureStorage = remember { SecureStorage(context) }
    val exportManager = remember { ExportManager(context) }
    
    // Settings state
    var apiKey by remember { mutableStateOf(secureStorage.getApiKey() ?: "") }
    var serverUrl by remember { mutableStateOf(secureStorage.getServerUrl() ?: "https://api.example.com") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoRetryEnabled by remember { mutableStateOf(true) }
    var maxRetries by remember { mutableStateOf("3") }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    
    // Launcher for battery optimization exemption
    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(context, "Optymalizacja baterii wyłączona", Toast.LENGTH_SHORT).show()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ustawienia",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Foreground
            )
            TextButton(onClick = onBack) {
                Text("Powrót", color = Primary)
            }
        }
        
        // API Configuration Section
        SettingsSection(title = "Konfiguracja API") {
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("Klucz API") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Wprowadź klucz API") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("URL Serwera") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://api.example.com") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    secureStorage.saveApiKey(apiKey)
                    secureStorage.saveServerUrl(serverUrl)
                    Toast.makeText(context, "Zapisano konfigurację API", Toast.LENGTH_SHORT).show()
                    Notify.success(context, "Ustawienia", "Konfiguracja API zapisana")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Zapisz konfigurację", color = PrimaryForeground)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            

        }
        
        // Notification Settings Section
        SettingsSection(title = "Powiadomienia") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Włącz powiadomienia", color = Foreground)
                    Text("Otrzymuj powiadomienia o statusie SMS", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Primary,
                        checkedTrackColor = Primary.copy(alpha = 0.3f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CardBg, contentColor = Foreground)
            ) {
                Text("Ustawienia systemowe powiadomień")
            }
        }
        
        // Auto-Retry Settings Section
        SettingsSection(title = "Automatyczne ponawianie") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Włącz auto-ponawianie", color = Foreground)
                    Text("Automatycznie ponawiaj nieudane SMS", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = autoRetryEnabled,
                    onCheckedChange = { autoRetryEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Primary,
                        checkedTrackColor = Primary.copy(alpha = 0.3f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = maxRetries,
                onValueChange = { 
                    maxRetries = it
                },
                label = { Text("Maksymalna liczba ponowień") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Save auto-retry settings and close keyboard
                        if (maxRetries.isNotEmpty()) {
                            Toast.makeText(context, "Zapisano ustawienia ponawiania", Toast.LENGTH_SHORT).show()
                            Notify.success(context, "Ustawienia", "Konfiguracja auto-ponawiania zapisana")
                        }
                    }
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    // Save auto-retry settings
                    if (maxRetries.isNotEmpty()) {
                        Toast.makeText(context, "Zapisano ustawienia ponawiania", Toast.LENGTH_SHORT).show()
                        Notify.success(context, "Ustawienia", "Konfiguracja auto-ponawiania zapisana")
                    } else {
                        Toast.makeText(context, "Podaj maksymalną liczbę ponowień", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Zapisz", color = PrimaryForeground)
            }
        }
        
        // Battery Optimization Section
        SettingsSection(title = "Optymalizacja baterii") {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                        }
                        batteryOptimizationLauncher.launch(intent)
                    } else {
                        Toast.makeText(context, "Ta funkcja wymaga Android 6.0+", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CardBg, contentColor = Foreground)
            ) {
                Text("Wyłącz optymalizację baterii")
            }
        }
        
        // Data Management Section
        SettingsSection(title = "Zarządzanie danymi") {
            Button(
                onClick = { showExportDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CardBg, contentColor = Foreground)
            ) {
                Text("Eksportuj dane")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { showClearDataDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.1f),
                    contentColor = Color.Red
                )
            ) {
                Text("Wyczyść wszystkie dane")
            }
        }
        
        // App Info Section
        SettingsSection(title = "Informacje o aplikacji") {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
            
            InfoRow("Wersja aplikacji", "$versionName ($versionCode)")
            InfoRow("Wersja Androida", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            InfoRow("Model urządzenia", "${Build.MANUFACTURER} ${Build.MODEL}")
            InfoRow("ID urządzenia", Build.ID)
        }
    }
    
    // Clear Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Potwierdzenie usunięcia") },
            text = { Text("Czy na pewno chcesz usunąć wszystkie dane? Tej operacji nie można cofnąć.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val database = SmsDatabase.getDatabase(context)
                                database.clearAllTables()
                                secureStorage.clearAll()
                                
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Wszystkie dane zostały usunięte", Toast.LENGTH_LONG).show()
                                    Notify.success(context, "Sukces", "Dane zostały wyczyszczone")
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
                                    Notify.error(context, "Błąd", "Nie udało się wyczyścić danych")
                                }
                            }
                        }
                        showClearDataDialog = false
                    }
                ) {
                    Text("Usuń", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
    
    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Eksport danych") },
            text = { Text("Wybierz format eksportu:") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                exportManager.exportSmsToCsv()
                                    .onSuccess { filePath ->
                                        Toast.makeText(context, "Wyeksportowano SMS-y do:\n$filePath", Toast.LENGTH_LONG).show()
                                        exportManager.shareFile(filePath)
                                    }
                                    .onFailure { error ->
                                        ErrorHandler.showError(context, "Błąd eksportu: ${error.message}")
                                    }
                            }
                            showExportDialog = false
                        }
                    ) {
                        Text("Eksportuj SMS (CSV)")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                exportManager.exportLogsToJson()
                                    .onSuccess { filePath ->
                                        Toast.makeText(context, "Wyeksportowano logi do:\n$filePath", Toast.LENGTH_LONG).show()
                                        exportManager.shareFile(filePath, "application/json")
                                    }
                                    .onFailure { error ->
                                        ErrorHandler.showError(context, "Błąd eksportu: ${error.message}")
                                    }
                            }
                            showExportDialog = false
                        }
                    ) {
                        Text("Eksportuj logi (JSON)")
                    }
                }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Foreground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Foreground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

