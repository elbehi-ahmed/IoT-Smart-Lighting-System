package com.ElbehiAhmed.studytimer.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity tells Room: "this class = one database table"
@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) // Room auto-assigns unique IDs
    val id: Int = 0,

    val date: String,        // e.g. "2026-07-23"
    val startTime: String,   // e.g. "14:00:00"
    val endTime: String,     // e.g. "14:45:30"
    val durationSeconds: Long,
    val durationFormatted: String // e.g. "00:45:30"
)