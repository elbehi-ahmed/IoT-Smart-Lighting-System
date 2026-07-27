package com.ElbehiAhmed.studytimer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ElbehiAhmed.studytimer.dao.StudySessionDao
import com.ElbehiAhmed.studytimer.entity.StudySession

// @Database tells Room: "this class represents the whole database"
@Database(entities = [StudySession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Room auto-implements this — gives you access to your DAO
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        // @Volatile ensures changes to this variable are visible to all threads immediately
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Singleton pattern: only ONE database connection should ever exist in the app
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_timer_database" // actual file name on device storage
                ).allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}