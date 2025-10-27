package com.example.smsbramkax1.ui.utils

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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