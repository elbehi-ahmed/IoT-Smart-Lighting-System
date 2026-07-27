package com.ElbehiAhmed.studytimer.utils

import java.text.SimpleDateFormat
import java.util.Locale

object TimeUtils {

    // Format seconds -> "HH:MM:SS"
    fun formatDuration(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Current date as "yyyy-MM-dd"
    fun currentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(System.currentTimeMillis())
    }

    // Current time as "HH:mm:ss"
    fun currentTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(System.currentTimeMillis())
    }
    // Returns true if the given "yyyy-MM-dd" date string is today
    fun isToday(date: String): Boolean {
        return date == currentDate()
    }

    // Returns true if the date falls within the last 7 days (inclusive)
    fun isWithinLastWeek(date: String): Boolean {
        return isWithinLastNDays(date, 7)
    }

    // Returns true if the date falls within the last 30 days (inclusive)
    fun isWithinLastMonth(date: String): Boolean {
        return isWithinLastNDays(date, 30)
    }

    private fun isWithinLastNDays(date: String, days: Int): Boolean {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val target = formatter.parse(date) ?: return false
            val diffMillis = System.currentTimeMillis() - target.time
            val diffDays = diffMillis / (1000 * 60 * 60 * 24)
            diffDays in 0 until days
        } catch (e: Exception) {
            false
        }
    }
}