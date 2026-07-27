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
}