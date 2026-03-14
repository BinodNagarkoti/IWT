package com.binodnagarkoti.intervalwalktracker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkSessionDao {
    @Query("SELECT * FROM walk_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<WalkSession>>

    @Query("SELECT * FROM walk_sessions WHERE date >= :startDate ORDER BY date DESC")
    fun getSessionsFromDate(startDate: Long): Flow<List<WalkSession>>

    @Insert
    suspend fun insertSession(session: WalkSession)

    @Query("SELECT SUM(steps) FROM walk_sessions WHERE date >= :startOfDay")
    fun getTodaySteps(startOfDay: Long): Flow<Int?>

    @Query("SELECT * FROM step_logs ORDER BY timestamp DESC")
    fun getStepLogs(): Flow<List<StepLog>>

    @Insert
    suspend fun insertStepLog(log: StepLog)
}
