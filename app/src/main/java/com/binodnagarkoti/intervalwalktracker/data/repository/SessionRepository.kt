package com.binodnagarkoti.intervalwalktracker.data.repository

import com.binodnagarkoti.intervalwalktracker.data.database.StepLog
import com.binodnagarkoti.intervalwalktracker.data.database.WalkSession
import com.binodnagarkoti.intervalwalktracker.data.database.WalkSessionDao
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val walkSessionDao: WalkSessionDao
) {

    val allSessions: Flow<List<WalkSession>> = walkSessionDao.getAllSessions()

    fun getTodaySteps(): Flow<Int?> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return walkSessionDao.getTodaySteps(calendar.timeInMillis)
    }

    fun getSessionsFromDate(startDate: Long): Flow<List<WalkSession>> {
        return walkSessionDao.getSessionsFromDate(startDate)
    }

    suspend fun insertSession(session: WalkSession) {
        walkSessionDao.insertSession(session)
    }

    suspend fun insertStepLog(log: StepLog) {
        walkSessionDao.insertStepLog(log)
    }
}
