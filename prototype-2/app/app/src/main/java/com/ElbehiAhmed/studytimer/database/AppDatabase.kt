package com.ElbehiAhmed.studytimer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ElbehiAhmed.studytimer.dao.StudySessionDao
import com.ElbehiAhmed.studytimer.entity.StudySession

// @Database tells Room: "this class represents the whole database"
@Database(entities = [StudySession::class], version = 2, exportSchema = false)
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
                ).addMigrations(MIGRATION_1_2).allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE study_sessions ADD COLUMN temperature REAL")
                database.execSQL("ALTER TABLE study_sessions ADD COLUMN humidity REAL")
                database.execSQL("ALTER TABLE study_sessions ADD COLUMN brightness REAL")
            }
        }
    }
}