package com.ElbehiAhmed.studytimer.dao

import androidx.room.*
import com.ElbehiAhmed.studytimer.entity.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    // Insert a new session. onConflict strategy: replace if same ID exists.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: StudySession): Long

    // Delete one session
    @Delete
    fun deleteSession(session: StudySession): Int

    // Delete all sessions
    @Query("DELETE FROM study_sessions")
    fun deleteAllSessions(): Int

    // Get all sessions, newest first
    @Query("SELECT * FROM study_sessions ORDER BY id DESC")
    fun getAllSessionsNewestFirst(): Flow<List<StudySession>>

    // Get all sessions, oldest first
    @Query("SELECT * FROM study_sessions ORDER BY id ASC")
    fun getAllSessionsOldestFirst(): Flow<List<StudySession>>

    // Search by date
    @Query("SELECT * FROM study_sessions WHERE date = :searchDate ORDER BY id DESC")
    fun searchByDate(searchDate: String): Flow<List<StudySession>>
}