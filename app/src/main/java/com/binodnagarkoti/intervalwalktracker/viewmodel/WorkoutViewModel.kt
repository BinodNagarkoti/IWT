package com.binodnagarkoti.intervalwalktracker.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.binodnagarkoti.intervalwalktracker.data.repository.SessionRepository
import com.binodnagarkoti.intervalwalktracker.sensors.StepSensorManager
import com.binodnagarkoti.intervalwalktracker.service.WorkoutService
import com.binodnagarkoti.intervalwalktracker.timer.IntervalTimerManager
import com.binodnagarkoti.intervalwalktracker.timer.TimerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(
    private val context: Context,
    private val repository: SessionRepository,
    private val timerManager: IntervalTimerManager,
    private val stepSensorManager: StepSensorManager
) : ViewModel() {

    val timeLeft = timerManager.timeLeft
    val timerState = timerManager.timerState
    val currentSet = timerManager.currentSet
    val steps = stepSensorManager.steps
    val isSensorAvailable = stepSensorManager.isSensorAvailable

    private val _totalSets = MutableStateFlow(5)
    val totalSets: StateFlow<Int> = _totalSets.asStateFlow()

    private val _alertMessage = MutableStateFlow<String?>(null)
    val alertMessage: StateFlow<String?> = _alertMessage.asStateFlow()

    // Captured data for Summary Screen
    private val _summarySteps = MutableStateFlow(0)
    val summarySteps: StateFlow<Int> = _summarySteps.asStateFlow()

    private val _summaryCompletedSets = MutableStateFlow(0)
    val summaryCompletedSets: StateFlow<Int> = _summaryCompletedSets.asStateFlow()

    private val _summaryDurationMinutes = MutableStateFlow(0)
    val summaryDurationMinutes: StateFlow<Int> = _summaryDurationMinutes.asStateFlow()

    private val _summaryFastMinutes = MutableStateFlow(0)
    val summaryFastMinutes: StateFlow<Int> = _summaryFastMinutes.asStateFlow()

    private val _summarySlowMinutes = MutableStateFlow(0)
    val summarySlowMinutes: StateFlow<Int> = _summarySlowMinutes.asStateFlow()

    init {
        viewModelScope.launch {
            timerState.collect { state ->
                when (state) {
                    TimerState.FAST -> _alertMessage.value = "Switch to Fast Walk"
                    TimerState.SLOW -> _alertMessage.value = "Switch to Slow Walk"
                    TimerState.COMPLETED -> {
                        _alertMessage.value = "Workout Completed!"
                        captureSummary()
                    }
                    TimerState.IDLE -> _alertMessage.value = null
                    else -> {}
                }
            }
        }
    }

    private fun captureSummary() {
        _summarySteps.value = steps.value
        _summaryCompletedSets.value = if (timerState.value == TimerState.COMPLETED) totalSets.value else (currentSet.value - 1).coerceAtLeast(0)
        _summaryDurationMinutes.value = (timerManager.totalFastSeconds.value + timerManager.totalSlowSeconds.value) / 60
        _summaryFastMinutes.value = timerManager.totalFastSeconds.value / 60
        _summarySlowMinutes.value = timerManager.totalSlowSeconds.value / 60
    }

    fun startWorkout(sets: Int) {
        _totalSets.value = sets
        
        // If not already running, reset and start
        if (timerState.value == TimerState.IDLE || timerState.value == TimerState.COMPLETED) {
            timerManager.reset()
            stepSensorManager.reset()

            val intent = Intent(context, WorkoutService::class.java).apply {
                action = "START"
                putExtra("SETS", sets)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    fun pauseResume() {
        when (timerState.value) {
            TimerState.FAST, TimerState.SLOW -> timerManager.pauseTimer()
            TimerState.PAUSED -> timerManager.resumeTimer()
            else -> {}
        }
    }

    fun stopWorkout() {
        captureSummary()
        val intent = Intent(context, WorkoutService::class.java).apply {
            action = "STOP"
        }
        context.startService(intent)
    }
}

class WorkoutViewModelFactory(
    private val context: Context,
    private val repository: SessionRepository,
    private val timerManager: IntervalTimerManager,
    private val stepSensorManager: StepSensorManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(context, repository, timerManager, stepSensorManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
