package com.binodnagarkoti.intervalwalktracker.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class UnitSystem(val label: String, val description: String) {
    METRIC("Metric", "Kilometers"),
    IMPERIAL("Imperial", "Miles")
}

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isAudioFeedbackEnabled = MutableStateFlow(true)
    val isAudioFeedbackEnabled: StateFlow<Boolean> = _isAudioFeedbackEnabled.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(true)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _unitSystem = MutableStateFlow(UnitSystem.METRIC)
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
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
}
