package com.ElbehiAhmed.studytimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ElbehiAhmed.studytimer.entity.StudySession
import com.ElbehiAhmed.studytimer.repository.SessionRepository
import com.ElbehiAhmed.studytimer.utils.SessionStats
import com.ElbehiAhmed.studytimer.utils.TimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(repository: SessionRepository) : ViewModel() {

    // map: transform each emitted List<StudySession> into a computed SessionStats
    val stats: StateFlow<SessionStats> = repository.getAllSessionsNewestFirst()
        .map { sessions -> computeStats(sessions) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionStats()
        )

    private fun computeStats(sessions: List<StudySession>): SessionStats {
        if (sessions.isEmpty()) return SessionStats()

        val durations = sessions.map { it.durationSeconds }
        val total = durations.sum()
        val today = sessions.filter { TimeUtils.isToday(it.date) }.sumOf { it.durationSeconds }
        val week = sessions.filter { TimeUtils.isWithinLastWeek(it.date) }.sumOf { it.durationSeconds }
        val month = sessions.filter { TimeUtils.isWithinLastMonth(it.date) }.sumOf { it.durationSeconds }
        fun average(values: List<Double?>): Double? {
            val available = values.filterNotNull()
            return available.takeIf { it.isNotEmpty() }?.average()
        }

        return SessionStats(
            totalSeconds = total,
            sessionCount = sessions.size,
            averageSeconds = total / sessions.size,
            longestSeconds = durations.max(),
            shortestSeconds = durations.min(),
            todaySeconds = today,
            weekSeconds = week,
            monthSeconds = month,
            averageTemperature = average(sessions.map { it.temperature }),
            averageHumidity = average(sessions.map { it.humidity }),
            averageBrightness = average(sessions.map { it.brightness })
        )
    }
}