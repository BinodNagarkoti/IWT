package com.binodnagarkoti.intervalwalktracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.binodnagarkoti.intervalwalktracker.viewmodel.UnitSystem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("dark_theme", true))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isAudioFeedbackEnabled = MutableStateFlow(prefs.getBoolean("audio_feedback", true))
    val isAudioFeedbackEnabled: StateFlow<Boolean> = _isAudioFeedbackEnabled.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _unitSystem = MutableStateFlow(
        UnitSystem.valueOf(prefs.getString("unit_system", UnitSystem.METRIC.name) ?: UnitSystem.METRIC.name)
    )
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    private val _targetSets = MutableStateFlow(prefs.getInt("target_sets", 5))
    val targetSets: StateFlow<Int> = _targetSets.asStateFlow()

    fun setDarkTheme(enabled: Boolean) {
        _isDarkTheme.value = enabled
        prefs.edit().putBoolean("dark_theme", enabled).apply()
    }

    fun setAudioFeedback(enabled: Boolean) {
        _isAudioFeedbackEnabled.value = enabled
        prefs.edit().putBoolean("audio_feedback", enabled).apply()
    }

    fun setVibration(enabled: Boolean) {
        _isVibrationEnabled.value = enabled
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }

    fun setUnitSystem(system: UnitSystem) {
        _unitSystem.value = system
        prefs.edit().putString("unit_system", system.name).apply()
    }

    fun setTargetSets(sets: Int) {
        _targetSets.value = sets
        prefs.edit().putInt("target_sets", sets).apply()
    }
}
