package com.binodnagarkoti.intervalwalktracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WalkSession::class, StepLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walkSessionDao(): WalkSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Changed from inMemoryDatabaseBuilder to databaseBuilder for permanent storage
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "interval_walk_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
