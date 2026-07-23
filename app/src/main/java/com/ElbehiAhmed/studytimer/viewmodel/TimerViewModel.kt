package com.ElbehiAhmed.studytimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    // Private mutable state — only this class can change it
    private val _timerState = MutableStateFlow<TimerState>(TimerState.Idle)
    // Public read-only version — the Activity can observe but not modify
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null // handle to the running coroutine loop
    private var startTimeMillis: Long = 0L

    fun startTimer() {
        // Guard clause: refuse to start if already running (your requirement)
        if (_timerState.value == TimerState.Running) return

        _timerState.value = TimerState.Running
        startTimeMillis = System.currentTimeMillis() - (_elapsedSeconds.value * 1000)

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // wait 1 second, non-blocking
                _elapsedSeconds.value = (System.currentTimeMillis() - startTimeMillis) / 1000
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value != TimerState.Running) return
        _timerState.value = TimerState.Paused
        timerJob?.cancel() // stop the counting loop, but keep elapsedSeconds value
    }

    fun resumeTimer() {
        if (_timerState.value != TimerState.Paused) return
        startTimer() // reuses same logic, recalculates startTimeMillis correctly
    }

    fun stopTimer(): Long {
        timerJob?.cancel()
        val finalSeconds = _elapsedSeconds.value
        _timerState.value = TimerState.Idle
        _elapsedSeconds.value = 0L
        return finalSeconds // caller (Activity) will use this to save the session
    }
}