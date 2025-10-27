package com.example.smsbramkax1.ui.utils

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.text.SimpleDateFormat
import java.util.*

object RefreshManager {
    private val _refreshEvents = MutableSharedFlow<String>()
    val refreshEvents = _refreshEvents.asSharedFlow()
    
    fun triggerRefresh(event: String = "default") {
        _refreshEvents.tryEmit(event)
    }
}

@Composable
fun RefreshListener(event: String = "default", onRefresh: () -> Unit) {
    LaunchedEffect(Unit) {
        RefreshManager.refreshEvents.collect { refreshEvent ->
            if (refreshEvent == event) {
                onRefresh()
            }
        }
    }
}

fun formatDateTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "przed chwilą"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min temu"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} godz temu"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} dni temu"
        else -> formatDateTime(timestamp)
    }
}