package com.binodnagarkoti.intervalwalktracker.timer

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class TimerState {
    IDLE, FAST, SLOW, PAUSED, COMPLETED
}

class IntervalTimerManager {
    private val _timeLeft = MutableStateFlow(180) // default 3 minutes in seconds
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
    private var fastIntervalSeconds = 180
    private var slowIntervalSeconds = 180
    
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var stateBeforePause: TimerState = TimerState.IDLE

    fun startTimer(sets: Int, fastSeconds: Int = 180, slowSeconds: Int = 180) {
        totalSets = sets
        fastIntervalSeconds = fastSeconds
        slowIntervalSeconds = slowSeconds
        
        _currentSet.value = 1
        _timerState.value = TimerState.FAST
        _timeLeft.value = fastIntervalSeconds
        _totalFastSeconds.value = 0
        _totalSlowSeconds.value = 0
        startCountdown()
    }

    private fun startCountdown() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                // Check if we should continue looping
                if (_timerState.value == TimerState.IDLE || 
                    _timerState.value == TimerState.PAUSED || 
                    _timerState.value == TimerState.COMPLETED) {
                    break
                }

                delay(1000)
                
                // Re-check state after delay in case it was paused
                if (!isActive || _timerState.value == TimerState.PAUSED) break

                if (_timeLeft.value > 0) {
                    _timeLeft.value -= 1
                    
                    // Track total time
                    if (_timerState.value == TimerState.FAST) {
                        _totalFastSeconds.value += 1
                    } else if (_timerState.value == TimerState.SLOW) {
                        _totalSlowSeconds.value += 1
                    }
                }

                // If time reached zero, handle transition but DO NOT restart countdown recursively
                // The loop will handle the next second with the new state and time
                if (_timeLeft.value <= 0) {
                    handleTransition()
                    if (_timerState.value == TimerState.COMPLETED) {
                        break
                    }
                }
            }
        }
    }

    private fun handleTransition() {
        when (_timerState.value) {
            TimerState.FAST -> {
                // Transition FAST -> SLOW
                _timerState.value = TimerState.SLOW
                _timeLeft.value = slowIntervalSeconds
            }
            TimerState.SLOW -> {
                // Transition SLOW -> FAST (next set) or COMPLETED
                if (_currentSet.value < totalSets) {
                    _currentSet.value += 1
                    _timerState.value = TimerState.FAST
                    _timeLeft.value = fastIntervalSeconds
                } else {
                    _timerState.value = TimerState.COMPLETED
                }
            }
            else -> {}
        }
    }

    fun pauseTimer() {
        if (_timerState.value == TimerState.FAST || _timerState.value == TimerState.SLOW) {
            stateBeforePause = _timerState.value
            _timerState.value = TimerState.PAUSED
            // The job will naturally exit the loop on the next check
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
        _timeLeft.value = fastIntervalSeconds // reset to fast interval
    }
    
    fun reset() {
        _currentSet.value = 1
        _totalFastSeconds.value = 0
        _totalSlowSeconds.value = 0
        _timeLeft.value = fastIntervalSeconds
        _timerState.value = TimerState.IDLE
    }
}
