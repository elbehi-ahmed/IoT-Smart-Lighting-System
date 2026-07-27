package com.ElbehiAhmed.studytimer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ElbehiAhmed.studytimer.entity.StudySession
import com.ElbehiAhmed.studytimer.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Represents what sort mode the user picked
enum class SortMode { NEWEST, OLDEST }

class HistoryViewModel(private val repository: SessionRepository) : ViewModel() {

    private val _sortMode = MutableStateFlow(SortMode.NEWEST)

    // flatMapLatest: whenever _sortMode changes, SWITCH to a new underlying Flow
    // (cancels the old query's collection, starts a new one) — this is how sorting live-updates
    val sessions: StateFlow<List<StudySession>> = _sortMode.flatMapLatest { mode ->
        when (mode) {
            SortMode.NEWEST -> repository.getAllSessionsNewestFirst()
            SortMode.OLDEST -> repository.getAllSessionsOldestFirst()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // keep flow alive 5s after last observer leaves
        initialValue = emptyList()
    )

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun deleteSession(session: StudySession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun deleteAllSessions() {
        viewModelScope.launch {
            repository.deleteAllSessions()
        }
    }
}