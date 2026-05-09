package com.lalema.app.ui.settings

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lalema.app.data.PoopRecord
import com.lalema.app.domain.PoopRepository
import com.lalema.app.reminder.BootReceiver
import com.lalema.app.reminder.ReminderManager
import com.lalema.app.reminder.LiveActivityService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val poopRepository: PoopRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
    private val reminderManager = ReminderManager(
        application,
        application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    )

    private val _reminderHour = MutableStateFlow(prefs.getInt("hour", 8))
    val reminderHour: StateFlow<Int> = _reminderHour

    private val _reminderMinute = MutableStateFlow(prefs.getInt("minute", 0))
    val reminderMinute: StateFlow<Int> = _reminderMinute

    private val _alarmEnabled = MutableStateFlow(prefs.getBoolean("alarm_enabled", false))
    val alarmEnabled: StateFlow<Boolean> = _alarmEnabled

    private val _notificationEnabled = MutableStateFlow(prefs.getBoolean("notification_enabled", false))
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled

    private val _calendarEnabled = MutableStateFlow(prefs.getBoolean("calendar_enabled", false))
    val calendarEnabled: StateFlow<Boolean> = _calendarEnabled

    private val _allRecords = MutableStateFlow<List<PoopRecord>>(emptyList())
    val allRecords: StateFlow<List<PoopRecord>> = _allRecords

    init {
        viewModelScope.launch {
            poopRepository.getAllFlow().collect { _allRecords.value = it }
        }
    }

    fun toggleAlarm() {
        val newValue = !_alarmEnabled.value
        _alarmEnabled.value = newValue
        prefs.edit().putBoolean("alarm_enabled", newValue).apply()
        applyReminders()
    }

    fun toggleNotification() {
        val newValue = !_notificationEnabled.value
        _notificationEnabled.value = newValue
        prefs.edit().putBoolean("notification_enabled", newValue).apply()
        applyLiveActivity()
        applyReminders()
    }

    fun toggleCalendar() {
        val newValue = !_calendarEnabled.value
        _calendarEnabled.value = newValue
        prefs.edit().putBoolean("calendar_enabled", newValue).apply()
        if (newValue) {
            createCalendarEvent()
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _reminderHour.value = hour
        _reminderMinute.value = minute
        prefs.edit().putInt("hour", hour).putInt("minute", minute).apply()
        applyReminders()
        applyLiveActivity()
    }

    private fun applyReminders() {
        val hour = _reminderHour.value
        val minute = _reminderMinute.value
        val anyEnabled = _alarmEnabled.value || _notificationEnabled.value

        BootReceiver.saveAlarmState(getApplication(), anyEnabled, hour, minute)

        if (anyEnabled) {
            reminderManager.setDailyReminder(hour, minute)
        } else {
            reminderManager.cancelDailyReminder()
        }
    }

    private fun applyLiveActivity() {
        val context = getApplication<Application>()
        if (_notificationEnabled.value) {
            LiveActivityService.start(context, _reminderHour.value, _reminderMinute.value)
        } else {
            LiveActivityService.stop(context)
        }
    }

    private fun createCalendarEvent() {
        val hour = _reminderHour.value
        val minute = _reminderMinute.value
        reminderManager.createCalendarEvent(
            title = "排便提醒",
            description = "记得记录今天的排便情况哦",
            hour = hour,
            minute = minute
        )
    }

    val isAtLeastAndroid16: Boolean
        get() = Build.VERSION.SDK_INT >= 36
}
