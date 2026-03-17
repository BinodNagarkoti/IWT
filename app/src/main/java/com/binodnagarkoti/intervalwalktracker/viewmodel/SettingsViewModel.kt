package com.binodnagarkoti.intervalwalktracker.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binodnagarkoti.intervalwalktracker.data.database.WalkSession
import com.binodnagarkoti.intervalwalktracker.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

enum class UnitSystem(val label: String, val description: String) {
    METRIC("Metric", "Kilometers (km)"),
    IMPERIAL("Imperial", "Miles (mi)")
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SessionRepository
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isAudioFeedbackEnabled = MutableStateFlow(true)
    val isAudioFeedbackEnabled: StateFlow<Boolean> = _isAudioFeedbackEnabled.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _unitSystem = MutableStateFlow(UnitSystem.METRIC)
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus = _exportStatus.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun toggleAudioFeedback() {
        _isAudioFeedbackEnabled.value = !_isAudioFeedbackEnabled.value
    }

    fun toggleVibration() {
        _isVibrationEnabled.value = !_isVibrationEnabled.value
    }

    fun setUnitSystem(system: UnitSystem) {
        _unitSystem.value = system
    }

    fun exportDatabaseToCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                val sessions = repository.getAllSessionsList()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            // Header
                            writer.write("id,date,durationMinutes,steps,totalSets,completedSets,fastMinutes,slowMinutes\n")
                            // Data
                            sessions.forEach { session ->
                                writer.write("${session.id},${session.date},${session.durationMinutes},${session.steps},${session.totalSets},${session.completedSets},${session.fastMinutes},${session.slowMinutes}\n")
                            }
                        }
                    }
                }
                _exportStatus.value = "Export successful"
            } catch (e: Exception) {
                _exportStatus.value = "Export failed: ${e.message}"
            }
        }
    }

    fun importCsvToDatabase(uri: Uri) {
        viewModelScope.launch {
            try {
                val sessions = mutableListOf<WalkSession>()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            val header = reader.readLine() // Skip header
                            var line: String? = reader.readLine()
                            while (line != null) {
                                val tokens = line.split(",")
                                if (tokens.size >= 8) {
                                    sessions.add(
                                        WalkSession(
                                            id = tokens[0].toInt(),
                                            date = tokens[1].toLong(),
                                            durationMinutes = tokens[2].toInt(),
                                            steps = tokens[3].toInt(),
                                            totalSets = tokens[4].toInt(),
                                            completedSets = tokens[5].toInt(),
                                            fastMinutes = tokens[6].toInt(),
                                            slowMinutes = tokens[7].toInt()
                                        )
                                    )
                                }
                                line = reader.readLine()
                            }
                        }
                    }
                }
                if (sessions.isNotEmpty()) {
                    repository.insertSessions(sessions)
                    _exportStatus.value = "Import successful: ${sessions.size} records"
                } else {
                    _exportStatus.value = "Import failed: No valid data found"
                }
            } catch (e: Exception) {
                _exportStatus.value = "Import failed: ${e.message}"
            }
        }
    }
    
    fun clearStatus() {
        _exportStatus.value = null
    }
}
