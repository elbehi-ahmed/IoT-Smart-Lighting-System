package com.ElbehiAhmed.studytimer.viewmodel

// A sealed class = a fixed, known set of possible states (nothing else can exist)
sealed class TimerState {
    object Idle : TimerState()      // no timer running
    object Running : TimerState()   // timer actively counting
    object Paused : TimerState()    // timer paused, can resume
}