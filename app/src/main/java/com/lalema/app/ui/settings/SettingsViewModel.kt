package com.lalema.app.ui.settings

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lalema.app.data.PoopRecord
import com.lalema.app.domain.PoopRepository
import com.lalema.app.reminder.BootReceiver
import com.lalema.app.reminder.LiveActivityService
import com.lalema.app.reminder.ReminderManager
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

    private val context = application
    private val reminderManager = ReminderManager(
        application,
        application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    )

    private val initialState = BootReceiver.loadAlarmState(application)

    private val _reminderHour = MutableStateFlow(initialState.hour)
    val reminderHour: StateFlow<Int> = _reminderHour

    private val _reminderMinute = MutableStateFlow(initialState.minute)
    val reminderMinute: StateFlow<Int> = _reminderMinute

    private val _alarmEnabled = MutableStateFlow(initialState.alarmEnabled)
    val alarmEnabled: StateFlow<Boolean> = _alarmEnabled

    private val _notificationEnabled = MutableStateFlow(initialState.notificationEnabled)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled

    private val _calendarEnabled = MutableStateFlow(initialState.calendarEnabled)
    val calendarEnabled: StateFlow<Boolean> = _calendarEnabled

    private val _calendarEventId = MutableStateFlow(initialState.calendarEventId)
    val calendarEventId: StateFlow<Long> = _calendarEventId

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
        persistState()
        applyAlarm()
    }

    fun toggleNotification() {
        val newValue = !_notificationEnabled.value
        _notificationEnabled.value = newValue
        persistState()
        applyLiveActivity()
    }

    fun toggleCalendar() {
        val newValue = !_calendarEnabled.value
        _calendarEnabled.value = newValue
        persistState()

        if (newValue) {
            createCalendarEvent()
        } else {
            val eventId = _calendarEventId.value
            if (eventId > 0) {
                if (reminderManager.deleteCalendarEvent(eventId)) {
                    Toast.makeText(context, "已删除日历事件", Toast.LENGTH_SHORT).show()
                }
                _calendarEventId.value = -1L
                persistState()
            }
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _reminderHour.value = hour
        _reminderMinute.value = minute
        persistState()
        applyAlarm()
        applyLiveActivity()
        if (_calendarEnabled.value && _calendarEventId.value > 0) {
            reminderManager.updateCalendarEvent(
                _calendarEventId.value,
                "排便提醒",
                "记得记录今天的排便情况哦",
                hour,
                minute
            )
        }
    }

    private fun applyAlarm() {
        if (_alarmEnabled.value) {
            reminderManager.setDailyReminder(_reminderHour.value, _reminderMinute.value)
        } else {
            reminderManager.cancelDailyReminder()
        }
    }

    private fun applyLiveActivity() {
        if (_notificationEnabled.value) {
            LiveActivityService.start(context, _reminderHour.value, _reminderMinute.value)
        } else {
            LiveActivityService.stop(context)
        }
    }

    private fun createCalendarEvent() {
        val eventId = reminderManager.createCalendarEvent(
            title = "排便提醒",
            description = "记得记录今天的排便情况哦",
            hour = _reminderHour.value,
            minute = _reminderMinute.value
        )
        if (eventId > 0) {
            _calendarEventId.value = eventId
            persistState()
        }
    }

    private fun persistState() {
        BootReceiver.saveAlarmState(
            context,
            _alarmEnabled.value,
            _notificationEnabled.value,
            _calendarEnabled.value,
            _calendarEventId.value,
            _reminderHour.value,
            _reminderMinute.value
        )
    }

    val isAtLeastAndroid16: Boolean
        get() = Build.VERSION.SDK_INT >= 36
}
