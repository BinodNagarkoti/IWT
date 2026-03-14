package com.binodnagarkoti.intervalwalktracker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walk_sessions")
data class WalkSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val durationMinutes: Int,
    val steps: Int,
    val totalSets: Int,
    val completedSets: Int,
    val fastMinutes: Int,
    val slowMinutes: Int
)

@Entity(tableName = "step_logs")
data class StepLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val steps: Int
)
