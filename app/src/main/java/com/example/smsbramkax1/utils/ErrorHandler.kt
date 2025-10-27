package com.example.smsbramkax1.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.smsbramkax1.data.SmsQueue
import com.example.smsbramkax1.storage.SmsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ErrorHandler(private val context: Context) {
    
    companion object {
        fun showError(context: Context, message: String, duration: Int = Toast.LENGTH_LONG) {
            Toast.makeText(context, message, duration).show()
        }
        
        fun showSuccess(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
            Toast.makeText(context, message, duration).show()
        }
        
        fun showInfo(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
            Toast.makeText(context, message, duration).show()
        }
    }
    
    fun handleSmsError(error: Throwable, phoneNumber: String? = null) {
        val message = when {
            error.message?.contains("No service") == true -> "Brak usługi SMS. Sprawdź połączenie z siecią komórkową."
            error.message?.contains("Permission denied") == true -> "Brak uprawnień do wysyłania SMS. Sprawdź ustawienia aplikacji."
            error.message?.contains("Airplane mode") == true -> "Tryb samolotu jest włączony. Wyłącz go, aby wysyłać SMS-y."
            phoneNumber != null -> "Błąd wysyłania SMS na numer $phoneNumber: ${error.message}"
            else -> "Błąd wysyłania SMS: ${error.message}"
        }
        
        Notify.error(context, "Błąd SMS", message)
        showError(context, message)
    }
    
    fun handleNetworkError(error: Throwable) {
        val message = when {
            error.message?.contains("UnknownHost") == true -> "Nie można połączyć się z serwerem. Sprawdź adres URL."
            error.message?.contains("Timeout") == true -> "Przekroczono czas połączenia. Sprawdź połączenie internetowe."
            error.message?.contains("Network") == true -> "Brak połączenia z internetem."
            else -> "Błąd sieci: ${error.message}"
        }
        
        Notify.error(context, "Błąd sieci", message)
        showError(context, message)
    }
    
    fun handleDatabaseError(error: Throwable) {
        val message = when {
            error.message?.contains("database") == true -> "Błąd bazy danych: ${error.message}"
            error.message?.contains("disk") == true -> "Brak miejsca na dysku."
            else -> "Błąd zapisu danych: ${error.message}"
        }
        
        Notify.error(context, "Błąd bazy danych", message)
        showError(context, message)
    }
    
    fun handlePermissionError(permission: String) {
        val message = "Brak uprawnień: $permission. Przyznaj uprawnienia w ustawieniach aplikacji."
        Notify.error(context, "Brak uprawnień", message)
        showError(context, message)
    }
}

@Composable
fun ErrorSnackbar(
    snackbarHostState: SnackbarHostState,
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    LaunchedEffect(snackbarHostState, message) {
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long
        )
        
        if (result == SnackbarResult.ActionPerformed && onAction != null) {
            onAction()
        }
    }
}

@Composable
fun RetryDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    title: String = "Błąd operacji",
    message: String,
    retryText: String = "Ponów"
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onRetry) {
                    Text(retryText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun ConfirmationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Potwierdzenie",
    message: String,
    confirmText: String = "Tak",
    dismissText: String = "Anuluj",
    isDestructive: Boolean = false
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = if (isDestructive) 
                            androidx.compose.ui.graphics.Color.Red 
                        else 
                            androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        )
    }
}

@Composable
fun RetryFailedSmsDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onRetry: (SmsQueue) -> Unit,
    onRetryAll: (List<SmsQueue>) -> Unit,
    failedSms: List<SmsQueue>
) {
    if (isVisible && failedSms.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Nieudane SMS-y") },
            text = { 
                Text(
                    "Znaleziono ${failedSms.size} nieudanych SMS-ów.\n\n" +
                    "Czy chcesz ponowić wysyłkę?\n\n" +
                    "Ostatni błąd: ${failedSms.first().errorMessage ?: "Nieznany błąd"}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (failedSms.size == 1) {
                            onRetry(failedSms.first())
                        } else {
                            onRetryAll(failedSms)
                        }
                    }
                ) {
                    Text(if (failedSms.size == 1) "Ponów" else "Ponów wszystkie")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun NetworkErrorDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onSettings: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Brak połączenia") },
            text = { 
                Text(
                    "Brak połączenia z internetem.\n\n" +
                    "Sprawdź połączenie sieciowe i spróbuj ponownie."
                )
            },
            confirmButton = {
                TextButton(onClick = onRetry) {
                    Text("Spróbuj ponownie")
                }
            },
            dismissButton = {
                TextButton(onClick = onSettings) {
                    Text("Ustawienia sieci")
                }
            }
        )
    }
}

suspend fun retryFailedSms(
    context: Context,
    smsId: Long,
    scope: CoroutineScope
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val database = SmsDatabase.getDatabase(context)
            val smsQueue = database.smsQueueDao().getSmsById(smsId)
            
            if (smsQueue != null) {
                val smsManager = com.example.smsbramkax1.sms.SmsManager(context)
                val success = smsManager.sendSms(smsQueue.phoneNumber, smsQueue.message)
                
                if (success) {
                    database.smsQueueDao().updateSmsStatus(
                        smsId, 
                        com.example.smsbramkax1.data.SmsStatus.SENT, 
                        System.currentTimeMillis()
                    )
                    withContext(Dispatchers.Main) {
                        ErrorHandler.showSuccess(context, "SMS wysłany pomyślnie")
                        Notify.success(context, "Sukces", "SMS został ponownie wysłany")
                    }
                } else {
                    database.smsQueueDao().incrementRetryCount(smsId)
                    withContext(Dispatchers.Main) {
                        ErrorHandler.showError(context, "Nie udało się wysłać SMS")
                        Notify.error(context, "Błąd", "SMS nie został wysłany")
                    }
                }
                success
            } else {
                withContext(Dispatchers.Main) {
                    ErrorHandler.showError(context, "Nie znaleziono SMS w bazie danych")
                }
                false
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                ErrorHandler(context).handleSmsError(e)
            }
            false
        }
    }
}

suspend fun retryAllFailedSms(
    context: Context,
    scope: CoroutineScope
): Int {
    return withContext(Dispatchers.IO) {
        try {
            val database = SmsDatabase.getDatabase(context)
            val failedSms = database.smsQueueDao().getSmsByStatus(com.example.smsbramkax1.data.SmsStatus.FAILED)
            var successCount = 0
            
            for (sms: com.example.smsbramkax1.data.SmsQueue in failedSms) {
                val smsManager = com.example.smsbramkax1.sms.SmsManager(context)
                val success = smsManager.sendSms(sms.phoneNumber, sms.message)
                
                if (success) {
                    database.smsQueueDao().updateSmsStatus(
                        sms.id,
                        com.example.smsbramkax1.data.SmsStatus.SENT,
                        System.currentTimeMillis()
                    )
                    successCount++
                } else {
                    database.smsQueueDao().incrementRetryCount(sms.id)
                }
            }
            
            withContext(Dispatchers.Main) {
                if (successCount > 0) {
                    ErrorHandler.showSuccess(context, "Ponowiono $successCount SMS-ów")
                    Notify.success(context, "Sukces", "Wysłano $successCount SMS-ów")
                } else {
                    ErrorHandler.showError(context, "Nie udało się ponowić żadnego SMS")
                    Notify.error(context, "Błąd", "Żaden SMS nie został wysłany")
                }
            }
            
            successCount
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                ErrorHandler(context).handleSmsError(e)
            }
            0
        }
    }
}