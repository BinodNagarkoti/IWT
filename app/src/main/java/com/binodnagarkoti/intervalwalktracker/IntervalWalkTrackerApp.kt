package com.binodnagarkoti.intervalwalktracker

import android.app.Application
import com.binodnagarkoti.intervalwalktracker.data.database.AppDatabase
import com.binodnagarkoti.intervalwalktracker.data.repository.SessionRepository
import com.binodnagarkoti.intervalwalktracker.sensors.StepSensorManager
import com.binodnagarkoti.intervalwalktracker.timer.IntervalTimerManager

class IntervalWalkTrackerApp : Application() {

    lateinit var repository: SessionRepository
    lateinit var timerManager: IntervalTimerManager
    lateinit var stepSensorManager: StepSensorManager

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        repository = SessionRepository(database.walkSessionDao())
        timerManager = IntervalTimerManager()
        stepSensorManager = StepSensorManager(this)
    }
}
