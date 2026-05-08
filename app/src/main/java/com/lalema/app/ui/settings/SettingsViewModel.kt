package com.lalema.app.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

    private val _reminderEnabled = MutableStateFlow(prefs.getBoolean("enabled", false))
    val reminderEnabled: StateFlow<Boolean> = _reminderEnabled

    private val _reminderHour = MutableStateFlow(prefs.getInt("hour", 8))
    val reminderHour: StateFlow<Int> = _reminderHour

    private val _reminderMinute = MutableStateFlow(prefs.getInt("minute", 0))
    val reminderMinute: StateFlow<Int> = _reminderMinute

    fun toggleReminder() {
        val newValue = !_reminderEnabled.value
        _reminderEnabled.value = newValue
        prefs.edit().putBoolean("enabled", newValue).apply()
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _reminderHour.value = hour
        _reminderMinute.value = minute
        prefs.edit().putInt("hour", hour).putInt("minute", minute).apply()
    }
}
