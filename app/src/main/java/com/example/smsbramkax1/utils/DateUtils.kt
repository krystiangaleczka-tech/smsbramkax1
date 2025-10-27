package com.example.smsbramkax1.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for date and time formatting
 */
object DateUtils {
    
    private val defaultFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val shortFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    /**
     * Format timestamp to readable date and time string
     */
    fun formatDateTime(timestamp: Long): String {
        return try {
            defaultFormatter.format(Date(timestamp))
        } catch (e: Exception) {
            "Nieprawidłowa data"
        }
    }
    
    /**
     * Format timestamp to relative time (e.g., "2 godziny temu", "wczoraj")
     */
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Przed chwilą"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min temu"
            diff < 24 * 60 * 60 * 1000 -> {
                val hours = diff / (60 * 60 * 1000)
                if (hours == 1L) "Godzinę temu" else "$hours godz. temu"
            }
            diff < 48 * 60 * 60 * 1000 -> "Wczoraj"
            diff < 7 * 24 * 60 * 60 * 1000 -> {
                val days = diff / (24 * 60 * 60 * 1000)
                "$days dni temu"
            }
            else -> shortFormatter.format(Date(timestamp))
        }
    }
    
    /**
     * Format time only (HH:mm)
     */
    fun formatTime(timestamp: Long): String {
        return try {
            timeFormatter.format(Date(timestamp))
        } catch (e: Exception) {
            "Nieprawidłowa godzina"
        }
    }
    
    /**
     * Format date only (dd.MM.yyyy)
     */
    fun formatDate(timestamp: Long): String {
        return try {
            shortFormatter.format(Date(timestamp))
        } catch (e: Exception) {
            "Nieprawidłowa data"
        }
    }
    
    /**
     * Check if date is today
     */
    fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.timeInMillis = timestamp
        
        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * Check if date is yesterday
     */
    fun isYesterday(timestamp: Long): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        
        val date = Calendar.getInstance()
        date.timeInMillis = timestamp
        
        return yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }
}