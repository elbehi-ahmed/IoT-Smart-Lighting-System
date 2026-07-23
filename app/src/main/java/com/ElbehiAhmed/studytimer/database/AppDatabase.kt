package com.ElbehiAhmed.studytimer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ElbehiAhmed.studytimer.dao.StudySessionDao
import com.ElbehiAhmed.studytimer.entity.StudySession

@Database(entities = [StudySession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
