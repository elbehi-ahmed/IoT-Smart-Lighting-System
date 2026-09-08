package com.ElbehiAhmed.studytimer.repository

import com.ElbehiAhmed.studytimer.dao.StudySessionDao
import com.ElbehiAhmed.studytimer.entity.StudySession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Repository takes the DAO as a constructor parameter — it doesn't create it itself
class SessionRepository(private val dao: StudySessionDao) {

    suspend fun insertSession(session: StudySession) = withContext(Dispatchers.IO) {
        dao.insertSession(session)
    }

    suspend fun deleteSession(session: StudySession) = withContext(Dispatchers.IO) {
        dao.deleteSession(session)
    }

    suspend fun deleteAllSessions() = withContext(Dispatchers.IO) {
        dao.deleteAllSessions()
    }

    fun getAllSessionsNewestFirst(): Flow<List<StudySession>> {
        return dao.getAllSessionsNewestFirst()
    }

    fun getAllSessionsOldestFirst(): Flow<List<StudySession>> {
        return dao.getAllSessionsOldestFirst()
    }

    fun searchByDate(date: String): Flow<List<StudySession>> {
        return dao.searchByDate(date)
    }
}