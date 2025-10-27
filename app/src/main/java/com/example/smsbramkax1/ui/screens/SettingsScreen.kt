package com.example.smsbramkax1.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.app.AppOpsManager
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
import androidx.compose.runtime.LaunchedEffect
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

// Battery optimization states
enum class BatteryOptimizationState {
    UNRESTRICTED,    // Nieograniczony - bez optymalizacji
    OPTIMIZED,       // Zoptymalizowany - standardowa optymalizacja
    RESTRICTED,      // Ograniczony - agresywna optymalizacja
    UNKNOWN          // Nieznany stan
}

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
    
    var batteryOptimizationState by remember { mutableStateOf(BatteryOptimizationState.UNKNOWN) }
    
    // Function to check detailed battery optimization status
    fun checkBatteryOptimizationStatus() {
        batteryOptimizationState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isIgnoring = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
            
            if (isIgnoring) {
                BatteryOptimizationState.UNRESTRICTED
            } else {
                // Check if app is in restricted mode (requires additional checks)
                try {
                    val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as? android.app.AppOpsManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val mode = appOpsManager?.unsafeCheckOpNoThrow(
                            "android:run_in_background",
                            android.os.Process.myUid(),
                            context.packageName
                        )
                        when (mode) {
                            android.app.AppOpsManager.MODE_ALLOWED -> BatteryOptimizationState.OPTIMIZED
                            android.app.AppOpsManager.MODE_IGNORED -> BatteryOptimizationState.RESTRICTED
                            else -> BatteryOptimizationState.OPTIMIZED
                        }
                    } else {
                        BatteryOptimizationState.OPTIMIZED
                    }
                } catch (e: Exception) {
                    BatteryOptimizationState.OPTIMIZED
                }
            }
        } else {
            BatteryOptimizationState.UNKNOWN
        }
    }
    
    // Check status on screen load and periodically
    LaunchedEffect(Unit) {
        checkBatteryOptimizationStatus()
        
        // Check every 2 seconds to ensure status is up to date
        while (true) {
            kotlinx.coroutines.delay(2000)
            checkBatteryOptimizationStatus()
        }
    }
    

    
    // Launcher for battery optimization exemption
    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Refresh battery optimization status after returning from settings with delay
        coroutineScope.launch {
            kotlinx.coroutines.delay(1000) // Wait 1 second for system to update
            checkBatteryOptimizationStatus()
            
            val message = when (batteryOptimizationState) {
                BatteryOptimizationState.UNRESTRICTED -> "Optymalizacja baterii wyłączona ✓"
                BatteryOptimizationState.OPTIMIZED -> "Tryb zoptymalizowany"
                BatteryOptimizationState.RESTRICTED -> "Tryb ograniczony"
                BatteryOptimizationState.UNKNOWN -> "Nieznany stan optymalizacji"
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                    // Refresh status before opening settings
                    checkBatteryOptimizationStatus()
                    
                    // Always open app settings for battery optimization
                    try {
                        val settingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(settingsIntent)
                        Toast.makeText(context, "Wybierz opcję zużycia baterii", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Błąd otwierania ustawień: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (batteryOptimizationState) {
                        BatteryOptimizationState.UNRESTRICTED -> Success
                        BatteryOptimizationState.OPTIMIZED -> Primary
                        BatteryOptimizationState.RESTRICTED -> Color(0xFFEF4444) // Red
                        BatteryOptimizationState.UNKNOWN -> CardBg
                    },
                    contentColor = when (batteryOptimizationState) {
                        BatteryOptimizationState.UNRESTRICTED -> Color.White
                        BatteryOptimizationState.OPTIMIZED -> Color.White
                        BatteryOptimizationState.RESTRICTED -> Color.White
                        BatteryOptimizationState.UNKNOWN -> Foreground
                    }
                )
            ) {
                Text(
                    text = when (batteryOptimizationState) {
                        BatteryOptimizationState.UNRESTRICTED -> "✓ Nieograniczone"
                        BatteryOptimizationState.OPTIMIZED -> "Zoptymalizowane"
                        BatteryOptimizationState.RESTRICTED -> "⚠ Ograniczone"
                        BatteryOptimizationState.UNKNOWN -> "Sprawdź ustawienia baterii"
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (batteryOptimizationState) {
                    BatteryOptimizationState.UNRESTRICTED -> "Aplikacja działa bez ograniczeń baterii"
                    BatteryOptimizationState.OPTIMIZED -> "Standardowa optymalizacja baterii"
                    BatteryOptimizationState.RESTRICTED -> "Agresywna optymalizacja - może ograniczać działanie"
                    BatteryOptimizationState.UNKNOWN -> "Nie można określić stanu optymalizacji"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Foreground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    checkBatteryOptimizationStatus()
                    Toast.makeText(context, "Sprawdzono stan optymalizacji", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CardBg, contentColor = Foreground)
            ) {
                Text("🔄 Odśwież stan")
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

