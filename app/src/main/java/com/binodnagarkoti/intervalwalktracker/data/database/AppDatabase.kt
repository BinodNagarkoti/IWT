package com.binodnagarkoti.intervalwalktracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WalkSession::class, StepLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walkSessionDao(): WalkSessionDao
}
