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
    private val _searchDate = MutableStateFlow<String?>(null) // null = no active search

    // flatMapLatest: whenever _sortMode OR _searchDate changes, SWITCH to a new underlying Flow
    val sessions: StateFlow<List<StudySession>> = _sortMode.flatMapLatest { mode ->
        _searchDate.flatMapLatest { searchDate ->
            if (searchDate.isNullOrBlank()) {
                when (mode) {
                    SortMode.NEWEST -> repository.getAllSessionsNewestFirst()
                    SortMode.OLDEST -> repository.getAllSessionsOldestFirst()
                }
            } else {
                repository.searchByDate(searchDate)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun setSearchDate(date: String?) {
        _searchDate.value = date
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