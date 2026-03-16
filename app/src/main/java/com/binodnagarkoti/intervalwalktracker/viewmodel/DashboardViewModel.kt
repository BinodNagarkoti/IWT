package com.binodnagarkoti.intervalwalktracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binodnagarkoti.intervalwalktracker.data.database.WalkSession
import com.binodnagarkoti.intervalwalktracker.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class TimeFilter {
    DAILY, MONTHLY, YEARLY
}

data class DailyStats(
    val totalSteps: Int = 0,
    val completedSets: Int = 0,
    val totalDuration: Int = 0,
    val fastMinutes: Int = 0,
    val slowMinutes: Int = 0,
    val remainingSets: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TimeFilter.DAILY)
    val selectedFilter: StateFlow<TimeFilter> = _selectedFilter.asStateFlow()

    private val _targetSets = MutableStateFlow(5)
    val targetSets: StateFlow<Int> = _targetSets.asStateFlow()

    val allSessions = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredSessions: StateFlow<List<WalkSession>> = combine(allSessions, _selectedFilter) { sessions, filter ->
        val startTime = getStartTimeForFilter(filter)
        sessions.filter { it.date >= startTime }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredStats: StateFlow<DailyStats> = combine(filteredSessions, _targetSets, _selectedFilter) { sessions, target, filter ->
        val completed = sessions.sumOf { it.completedSets }
        val remaining = if (filter == TimeFilter.DAILY) (target - completed).coerceAtLeast(0) else 0
        
        DailyStats(
            totalSteps = sessions.sumOf { it.steps },
            completedSets = completed,
            totalDuration = sessions.sumOf { it.durationMinutes },
            fastMinutes = sessions.sumOf { it.fastMinutes },
            slowMinutes = sessions.sumOf { it.slowMinutes },
            remainingSets = remaining
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyStats()
    )

    fun setFilter(filter: TimeFilter) {
        _selectedFilter.value = filter
    }

    fun setTargetSets(sets: Int) {
        _targetSets.value = sets
    }

    fun deleteSession(session: WalkSession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    private fun getStartTimeForFilter(filter: TimeFilter): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        when (filter) {
            TimeFilter.DAILY -> {}
            TimeFilter.MONTHLY -> calendar.set(Calendar.DAY_OF_MONTH, 1)
            TimeFilter.YEARLY -> calendar.set(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
