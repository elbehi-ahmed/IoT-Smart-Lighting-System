package com.ElbehiAhmed.studytimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ElbehiAhmed.studytimer.entity.StudySession
import com.ElbehiAhmed.studytimer.repository.SessionRepository
import com.ElbehiAhmed.studytimer.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Repository is now a CONSTRUCTOR PARAMETER — dependency injection in action
class TimerViewModel(private val repository: SessionRepository) : ViewModel() {

    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var startTimeMillis: Long = 0L

    // We need to remember the wall-clock start time/date for saving later
    private var sessionStartDate: String = ""
    private var sessionStartTime: String = ""

    fun startTimer() {
        if (_timerState.value == TimerState.Running) return

        // Only record the "official" start date/time the FIRST time (not on resume)
        if (_timerState.value == TimerState.Idle) {
            sessionStartDate = TimeUtils.currentDate()
            sessionStartTime = TimeUtils.currentTime()
        }

        _timerState.value = TimerState.Running
        startTimeMillis = System.currentTimeMillis() - (_elapsedSeconds.value * 1000)

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value = (System.currentTimeMillis() - startTimeMillis) / 1000
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value != TimerState.Running) return
        _timerState.value = TimerState.Paused
        timerJob?.cancel()
    }

    fun resumeTimer() {
        if (_timerState.value != TimerState.Paused) return
        startTimer()
    }

    // Now returns Unit — it saves the session itself instead of handing data back to the Activity
    fun stopTimer() {
        if (_timerState.value == TimerState.Idle) return // nothing to stop

        timerJob?.cancel()
        val finalSeconds = _elapsedSeconds.value
        val endTime = TimeUtils.currentTime()

        _timerState.value = TimerState.Idle
        _elapsedSeconds.value = 0L

        // Don't save zero-second accidental taps
        if (finalSeconds > 0) {
            val session = StudySession(
                date = sessionStartDate,
                startTime = sessionStartTime,
                endTime = endTime,
                durationSeconds = finalSeconds,
                durationFormatted = TimeUtils.formatDuration(finalSeconds)
            )

            // viewModelScope.launch because insertSession is a suspend fun (database work)
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertSession(session)
            }
        }
    }
}