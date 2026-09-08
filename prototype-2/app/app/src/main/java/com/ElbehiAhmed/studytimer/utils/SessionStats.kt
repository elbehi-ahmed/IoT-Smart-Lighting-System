package com.ElbehiAhmed.studytimer.utils

data class SessionStats(
    val totalSeconds: Long = 0,
    val sessionCount: Int = 0,
    val averageSeconds: Long = 0,
    val longestSeconds: Long = 0,
    val shortestSeconds: Long = 0,
    val todaySeconds: Long = 0,
    val weekSeconds: Long = 0,
    val monthSeconds: Long = 0,
    val averageTemperature: Double? = null,
    val averageHumidity: Double? = null,
    val averageBrightness: Double? = null
)