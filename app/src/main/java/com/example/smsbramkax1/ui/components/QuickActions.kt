package com.example.smsbramkax1.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.smsbramkax1.ui.theme.*
import com.example.smsbramkax1.sms.SmsManager
import com.example.smsbramkax1.utils.Notify
import com.example.smsbramkax1.utils.NetworkState
import com.example.smsbramkax1.utils.PermissionsManager
import com.example.smsbramkax1.storage.SmsDatabase
import com.example.smsbramkax1.ui.utils.RefreshManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QuickActions(onNavigateToHistory: () -> Unit = {}, onNavigateToSettings: () -> Unit = {}) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val permissionsManager = remember { PermissionsManager(context) }
    
    // Launcher for SMS permissions
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            sendTestSms(context, coroutineScope)
        } else {
            Toast.makeText(context, "Brak uprawnień do wysyłania SMS", Toast.LENGTH_LONG).show()
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Szybkie akcje",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Foreground
            )
            Text(
                "Najczęściej używane funkcje",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Grid 2x2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = "📤", 
                    label = "Wyślij testowy SMS", 
                    isPrimary = true, 
                    modifier = Modifier.weight(1f),
                    onClick = {
                        when {
                            permissionsManager.hasSmsPermissions() -> {
                                sendTestSms(context, coroutineScope)
                            }
                            else -> {
                                smsPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.SEND_SMS,
                                        Manifest.permission.RECEIVE_SMS,
                                        Manifest.permission.READ_SMS
                                    )
                                )
                            }
                        }
                    }
                )
                ActionButton("📊", "Zobacz historię", isPrimary = false, Modifier.weight(1f)) {
                    onNavigateToHistory()
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton("⬇️", "Eksportuj dane", isPrimary = false, Modifier.weight(1f)) {
                    exportSmsData(context, coroutineScope)
                }
                ActionButton("⚙️", "Ustawienia", isPrimary = false, Modifier.weight(1f)) {
                    onNavigateToSettings()
                }
            }
        }
    }
}

private fun sendTestSms(context: android.content.Context, scope: CoroutineScope) {
    scope.launch(Dispatchers.IO) {
        try {
            val database = SmsDatabase.getDatabase(context)
            val smsManager = SmsManager(context)
            val testPhoneNumber = "+48500083577" // Numer testowy
            val testMessage = "Zapraszam na jutrzejsza wizyte do serduszka Krystusia <3"
            
            // Najpierw dodaj SMS do kolejki
            val smsMessage = com.example.smsbramkax1.data.SmsMessage(
                phoneNumber = testPhoneNumber,
                messageBody = testMessage,
                status = "PENDING",
                isScheduled = false,
                createdAt = System.currentTimeMillis()
            )
            
            val smsId = database.smsMessageDao().insertMessage(smsMessage)
            
            // Sprawdź stan sieci przed wysłaniem
            if (!NetworkState.isOnline(context)) {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context, 
                        "SMS dodany do kolejki (brak internetu)", 
                        Toast.LENGTH_LONG
                    ).show()
                    Notify.success(context, "SMS w kolejce", "Dodano do kolejki - wyśle przy połączeniu")
                    // Trigger refresh - SMS jest w bazie
                    RefreshManager.triggerRefresh("sms_sent")
                }
                return@launch
            }
            
            // Wyślij SMS bezpośrednio
            val result = smsManager.sendSms(testPhoneNumber, testMessage)
            
            // Zaktualizuj status w bazie
            val currentTime = System.currentTimeMillis()
            if (result.isSuccess) {
                database.smsMessageDao().updateMessageStatus(smsId, "SENT", currentTime)
            } else {
                database.smsMessageDao().updateMessageStatus(smsId, "FAILED")
            }
            
            scope.launch(Dispatchers.Main) {
                if (result.isSuccess) {
                    Toast.makeText(
                        context, 
                        "Test SMS wysłany pomyślnie!", 
                        Toast.LENGTH_SHORT
                    ).show()
                    Notify.success(context, "Test SMS", "Wysłano testowy SMS")
                    // Trigger refresh dla wszystkich komponentów
                    RefreshManager.triggerRefresh("sms_sent")
                } else {
                    Toast.makeText(
                        context, 
                        "Błąd wysyłania SMS", 
                        Toast.LENGTH_LONG
                    ).show()
                    Notify.error(context, "Błąd SMS", "Nie udało się wysłać testowego SMS")
                    // Trigger refresh mimo błędu (SMS jest w kolejce)
                    RefreshManager.triggerRefresh("sms_sent")
                }
            }
        } catch (e: Exception) {
            scope.launch(Dispatchers.Main) {
                Toast.makeText(
                    context, 
                    "Błąd: ${e.message}", 
                    Toast.LENGTH_LONG
                ).show()
                Notify.error(context, "Błąd krytyczny", "Wyjątek: ${e.message}")
            }
        }
    }
}

private fun showSmsHistory(context: android.content.Context, scope: CoroutineScope) {
    scope.launch(Dispatchers.IO) {
        try {
            val database = SmsDatabase.getDatabase(context)
            val recentSms = database.smsMessageDao().getAllMessages().first().take(20)
            
            scope.launch(Dispatchers.Main) {
                if (recentSms.isEmpty()) {
                    Toast.makeText(context, "Brak SMS-ów w historii", Toast.LENGTH_SHORT).show()
                } else {
                    val historyText = recentSms.take(5).joinToString("\n\n") { sms ->
                        val status = when (sms.status) {
                            "PENDING" -> "Oczekujący"
                            "SENT" -> "Wysłany"
                            "FAILED" -> "Błąd"
                            "SCHEDULED" -> "Zaplanowany"
                            else -> sms.status
                        }
                        val date = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(sms.createdAt))
                        "📱 ${sms.phoneNumber}\n📅 $date\n📊 Status: $status\n💬 ${sms.messageBody.take(50)}${if (sms.messageBody.length > 50) "..." else ""}"
                    }
                    
                    Toast.makeText(context, "Ostatnie SMS-y:\n\n$historyText", Toast.LENGTH_LONG).show()
                    Notify.success(context, "Historia SMS", "Wyświetlono ${recentSms.size} ostatnich SMS-ów")
                }
            }
        } catch (e: Exception) {
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
                Notify.error(context, "Błąd historii", "Nie udało się załadować historii: ${e.message}")
            }
        }
    }
}

private fun exportSmsData(context: android.content.Context, scope: CoroutineScope) {
    scope.launch(Dispatchers.IO) {
        try {
            val database = SmsDatabase.getDatabase(context)
            val allSms = database.smsMessageDao().getAllSms()
            
            if (allSms.isEmpty()) {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Brak danych do eksportu", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val fileName = "sms_export_${dateFormat.format(Date())}.csv"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileOutputStream(file).use { output ->
                // Nagłówki CSV
                output.write("ID,PhoneNumber,Message,Status,CreatedAt,SentAt,RetryCount,ErrorMessage\n".toByteArray())
                
                // Dane SMS
                allSms.forEach { sms ->
                    val status = when (sms.status) {
                        "PENDING" -> "Oczekujący"
                        "SENT" -> "Wysłany"
                        "FAILED" -> "Błąd"
                        "SCHEDULED" -> "Zaplanowany"
                        else -> sms.status
                    }
                    val createdDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(sms.createdAt))
                    val sentDate = sms.sentAt?.let { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(it)) } ?: ""
                    
                    val line = "${sms.id},${sms.phoneNumber},\"${sms.message.replace("\"", "\"\"")}\",$status,$createdDate,$sentDate,${sms.retryCount},\"${sms.errorMessage ?: ""}\"\n"
                    output.write(line.toByteArray())
                }
            }
            
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Wyeksportowano ${allSms.size} SMS-ów do:\n$file", Toast.LENGTH_LONG).show()
                Notify.success(context, "Eksport zakończony", "Wyeksportowano ${allSms.size} SMS-ów")
            }
        } catch (e: Exception) {
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Błąd eksportu: ${e.message}", Toast.LENGTH_LONG).show()
                Notify.error(context, "Błąd eksportu", "Nie udało się wyeksportować danych: ${e.message}")
            }
        }
    }
}

private fun showSettings(context: android.content.Context) {
    Toast.makeText(context, "Ustawienia - w budowie", Toast.LENGTH_SHORT).show()
    Notify.success(context, "Ustawienia", "Funkcja ustawień będzie dostępna w przyszłej wersji")
}

@Composable
fun ActionButton(
    icon: String, 
    label: String, 
    isPrimary: Boolean, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(75.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary) Primary else CardBg,
            contentColor = if (isPrimary) PrimaryForeground else Foreground
        ),
        border = if (!isPrimary) BorderStroke(1.dp, Border) else null,
        shape = RoundedCornerShape(6.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}