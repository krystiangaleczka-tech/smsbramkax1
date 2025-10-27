package com.example.smsbramkax1.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsManager(private val context: Context) {
    
    companion object {
        const val SMS_PERMISSION_REQUEST_CODE = 1001
        const val BATTERY_OPTIMIZATION_REQUEST_CODE = 1002
        
        val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            )
        }
        
        val OPTIONAL_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    
    fun hasAllRequiredPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun hasSmsPermissions(): Boolean {
        val smsPermissions = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        return smsPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Notifications don't require permission on older Android versions
        }
    }
    
    fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    
    fun getMissingPermissions(): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    fun shouldShowPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }
    
    fun isBatteryOptimizationDisabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Battery optimization not available on older versions
        }
    }
    
    fun getBatteryOptimizationIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }
    
    fun getAppSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
}

@Composable
fun PermissionRequestDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Wymagane uprawnienia",
    message: String,
    confirmText: String = "Przyznaj"
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmText)
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
fun PermissionRationaleDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit,
    permissionName: String
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Odmowa uprawnień") },
            text = { 
                Text(
                    "Aplikacja wymaga uprawnienia $permissionName do prawidłowego działania. " +
                    "Bez tego uprawnienia niektóre funkcje mogą nie działać.\n\n" +
                    "Przejdź do ustawień aplikacji i przyznaj wymagane uprawnienia ręcznie."
                )
            },
            confirmButton = {
                TextButton(onClick = onGoToSettings) {
                    Text("Ustawienia")
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
fun BatteryOptimizationDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDisableOptimization: () -> Unit
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Optymalizacja baterii") },
            text = { 
                Text(
                    "Aplikacja działa w tle do wysyłania i odbierania SMS-ów. " +
                    "Optymalizacja baterii może ograniczyć jej działanie.\n\n" +
                    "Zalecamy wyłączenie optymalizacji baterii dla tej aplikacji, aby zapewnić niezawodne działanie."
                )
            },
            confirmButton = {
                TextButton(onClick = onDisableOptimization) {
                    Text("Wyłącz optymalizację")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Pomiń")
                }
            }
        )
    }
}

@Composable
fun PermissionsManagerComposable(
    permissionsManager: PermissionsManager,
    onPermissionsGranted: () -> Unit = {},
    onPermissionsDenied: () -> Unit = {}
) {
    val context = LocalContext.current
    
    var showSmsRationale by remember { mutableStateOf(false) }
    var showNotificationRationale by remember { mutableStateOf(false) }
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }
    
    // Launcher for SMS permissions
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onPermissionsGranted()
        } else {
            showSmsRationale = true
        }
    }
    
    // Launcher for notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onPermissionsGranted()
        } else {
            showNotificationRationale = true
        }
    }
    
    // Launcher for battery optimization
    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Check if battery optimization was disabled
        if (permissionsManager.isBatteryOptimizationDisabled()) {
            onPermissionsGranted()
        }
    }
    
    // Check permissions on composition
    LaunchedEffect(Unit) {
        if (!permissionsManager.hasAllRequiredPermissions()) {
            val missingPermissions = permissionsManager.getMissingPermissions()
            
            if (missingPermissions.contains(Manifest.permission.POST_NOTIFICATIONS)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                val smsPermissions = missingPermissions.filter { 
                    it != Manifest.permission.POST_NOTIFICATIONS 
                }.toTypedArray()
                if (smsPermissions.isNotEmpty()) {
                    smsPermissionLauncher.launch(smsPermissions)
                }
            }
        } else if (!permissionsManager.isBatteryOptimizationDisabled()) {
            showBatteryOptimizationDialog = true
        } else {
            onPermissionsGranted()
        }
    }
    
    // SMS Permission Rationale Dialog
    PermissionRationaleDialog(
        isVisible = showSmsRationale,
        onDismiss = { 
            showSmsRationale = false
            onPermissionsDenied()
        },
        onGoToSettings = {
            context.startActivity(permissionsManager.getAppSettingsIntent())
            showSmsRationale = false
        },
        permissionName = "SMS"
    )
    
    // Notification Permission Rationale Dialog
    PermissionRationaleDialog(
        isVisible = showNotificationRationale,
        onDismiss = { 
            showNotificationRationale = false
            onPermissionsDenied()
        },
        onGoToSettings = {
            context.startActivity(permissionsManager.getAppSettingsIntent())
            showNotificationRationale = false
        },
        permissionName = "Powiadomień"
    )
    
    // Battery Optimization Dialog
    BatteryOptimizationDialog(
        isVisible = showBatteryOptimizationDialog,
        onDismiss = { 
            showBatteryOptimizationDialog = false
            onPermissionsGranted() // Continue even if user skips
        },
        onDisableOptimization = {
            batteryOptimizationLauncher.launch(permissionsManager.getBatteryOptimizationIntent())
            showBatteryOptimizationDialog = false
        }
    )
}

fun requestSmsPermission(activity: Activity) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        ),
        PermissionsManager.SMS_PERMISSION_REQUEST_CODE
    )
}

fun requestNotificationPermission(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            PermissionsManager.SMS_PERMISSION_REQUEST_CODE
        )
    }
}