package com.binodnagarkoti.intervalwalktracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binodnagarkoti.intervalwalktracker.data.database.WalkSession
import com.binodnagarkoti.intervalwalktracker.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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

data class AggregatedSession(
    val id: Int, 
    val date: Long,
    val label: String,
    val steps: Int,
    val totalSets: Int,
    val completedSets: Int,
    val durationMinutes: Int,
    val sessionCount: Int = 1,
    val isAggregated: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TimeFilter.DAILY)
    val selectedFilter: StateFlow<TimeFilter> = _selectedFilter.asStateFlow()

    private val _targetSets = MutableStateFlow(5)
    val targetSets: StateFlow<Int> = _targetSets.asStateFlow()

    // Daily tab filters
    private val _selectedYear = MutableStateFlow<Int?>(null) // null means All
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    private val _selectedMonth = MutableStateFlow<Int?>(null) // null means All (0-11)
    val selectedMonth: StateFlow<Int?> = _selectedMonth.asStateFlow()

    // Interval settings
    private val _fastIntervalValue = MutableStateFlow(3)
    val fastIntervalValue: StateFlow<Int> = _fastIntervalValue.asStateFlow()

    private val _fastIntervalUnit = MutableStateFlow("minutes")
    val fastIntervalUnit: StateFlow<String> = _fastIntervalUnit.asStateFlow()

    private val _slowIntervalValue = MutableStateFlow(3)
    val slowIntervalValue: StateFlow<Int> = _slowIntervalValue.asStateFlow()

    private val _slowIntervalUnit = MutableStateFlow("minutes")
    val slowIntervalUnit: StateFlow<String> = _slowIntervalUnit.asStateFlow()

    val allSessions = repository.allSessions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val availableYears: StateFlow<List<Int>> = allSessions.map { sessions ->
        sessions.map { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.get(Calendar.YEAR)
        }.distinct().sortedDescending()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aggregatedSessions: StateFlow<List<AggregatedSession>> = combine(
        allSessions, 
        _selectedFilter,
        _selectedYear,
        _selectedMonth
    ) { sessions, filter, year, month ->
        when (filter) {
            TimeFilter.DAILY -> {
                sessions.filter { session ->
                    val cal = Calendar.getInstance().apply { timeInMillis = session.date }
                    val yearMatch = year == null || cal.get(Calendar.YEAR) == year
                    val monthMatch = month == null || cal.get(Calendar.MONTH) == month
                    yearMatch && monthMatch
                }.map { it.toAggregated() }
            }
            TimeFilter.MONTHLY -> {
                sessions.groupBy { 
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
                .map { (date, monthlySessions) ->
                    aggregateSessions(date, monthlySessions, "MMMM yyyy")
                }
                .sortedByDescending { it.date }
            }
            TimeFilter.YEARLY -> {
                sessions.groupBy { 
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    cal.set(Calendar.MONTH, 0)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }
                .map { (date, yearlySessions) ->
                    aggregateSessions(date, yearlySessions, "yyyy")
                }
                .sortedByDescending { it.date }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private fun aggregateSessions(date: Long, sessions: List<WalkSession>, dateFormat: String): AggregatedSession {
        val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
        return AggregatedSession(
            id = sessions.first().id,
            date = date,
            label = sdf.format(Date(date)),
            steps = sessions.sumOf { it.steps },
            totalSets = sessions.sumOf { it.totalSets },
            completedSets = sessions.sumOf { it.completedSets },
            durationMinutes = sessions.sumOf { it.durationMinutes },
            sessionCount = sessions.size,
            isAggregated = true
        )
    }

    private fun WalkSession.toAggregated(): AggregatedSession {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return AggregatedSession(
            id = id,
            date = date,
            label = sdf.format(Date(date)),
            steps = steps,
            totalSets = totalSets,
            completedSets = completedSets,
            durationMinutes = durationMinutes,
            isAggregated = false
        )
    }

    val filteredStats: StateFlow<DailyStats> = combine(allSessions, _targetSets, _selectedFilter) { sessions, target, filter ->
        val startTime = getStartTimeForFilter(filter)
        val filtered = sessions.filter { it.date >= startTime }
        
        val completed = filtered.sumOf { it.completedSets }
        val remaining = if (filter == TimeFilter.DAILY) (target - completed).coerceAtLeast(0) else 0
        
        DailyStats(
            totalSteps = filtered.sumOf { it.steps },
            completedSets = completed,
            totalDuration = filtered.sumOf { it.durationMinutes },
            fastMinutes = filtered.sumOf { it.fastMinutes },
            slowMinutes = filtered.sumOf { it.slowMinutes },
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

    fun setYearFilter(year: Int?) {
        _selectedYear.value = year
    }

    fun setMonthFilter(month: Int?) {
        _selectedMonth.value = month
    }

    fun setTargetSets(sets: Int) {
        _targetSets.value = sets
    }

    fun setFastInterval(value: Int, unit: String) {
        _fastIntervalValue.value = value
        _fastIntervalUnit.value = unit
    }

    fun setSlowInterval(value: Int, unit: String) {
        _slowIntervalValue.value = value
        _slowIntervalUnit.value = unit
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
