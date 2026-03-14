package com.binodnagarkoti.intervalwalktracker.timer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class TimerState {
    IDLE, FAST, SLOW, PAUSED, COMPLETED
}

class IntervalTimerManager {
    private val _timeLeft = MutableStateFlow(180) // 3 minutes in seconds
    val timeLeft: StateFlow<Int> = _timeLeft

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState

    private val _currentSet = MutableStateFlow(1)
    val currentSet: StateFlow<Int> = _currentSet

    private val _totalFastSeconds = MutableStateFlow(0)
    val totalFastSeconds: StateFlow<Int> = _totalFastSeconds

    private val _totalSlowSeconds = MutableStateFlow(0)
    val totalSlowSeconds: StateFlow<Int> = _totalSlowSeconds

    private var totalSets = 5
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var stateBeforePause: TimerState = TimerState.IDLE

    fun startTimer(sets: Int) {
        totalSets = sets
        _currentSet.value = 1
        _timerState.value = TimerState.FAST
        _timeLeft.value = 180
        _totalFastSeconds.value = 0
        _totalSlowSeconds.value = 0
        startCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _timeLeft.value > 0) {
                delay(1000)
                _timeLeft.value -= 1
                
                // Track total time
                if (_timerState.value == TimerState.FAST) {
                    _totalFastSeconds.value += 1
                } else if (_timerState.value == TimerState.SLOW) {
                    _totalSlowSeconds.value += 1
                }

                if (_timeLeft.value == 0) {
                    handleTransition()
                }
            }
        }
    }

    private fun handleTransition() {
        when (_timerState.value) {
            TimerState.FAST -> {
                _timerState.value = TimerState.SLOW
                _timeLeft.value = 180
                startCountdown()
            }
            TimerState.SLOW -> {
                if (_currentSet.value < totalSets) {
                    _currentSet.value += 1
                    _timerState.value = TimerState.FAST
                    _timeLeft.value = 180
                    startCountdown()
                } else {
                    _timerState.value = TimerState.COMPLETED
                    timerJob?.cancel()
                }
            }
            else -> {}
        }
    }

    fun pauseTimer() {
        if (_timerState.value == TimerState.FAST || _timerState.value == TimerState.SLOW) {
            stateBeforePause = _timerState.value
            _timerState.value = TimerState.PAUSED
            timerJob?.cancel()
        }
    }

    fun resumeTimer() {
        if (_timerState.value == TimerState.PAUSED) {
            _timerState.value = stateBeforePause
            startCountdown()
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE
        _timeLeft.value = 180
        // We don't reset currentSet or totalSeconds immediately so they can be read for saving
    }
    
    fun reset() {
        _currentSet.value = 1
        _totalFastSeconds.value = 0
        _totalSlowSeconds.value = 0
    }
}
