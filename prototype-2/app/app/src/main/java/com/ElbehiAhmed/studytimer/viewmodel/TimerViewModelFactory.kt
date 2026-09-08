package com.ElbehiAhmed.studytimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ElbehiAhmed.studytimer.repository.SessionRepository

class TimerViewModelFactory(
    private val repository: SessionRepository
) : ViewModelProvider.Factory {

    // This function is called internally by the Android framework when it needs
    // to create the ViewModel — we tell it exactly how.
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}