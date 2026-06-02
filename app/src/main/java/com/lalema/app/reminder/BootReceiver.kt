package com.lalema.app.reminder

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            restoreAfterBoot(context)
        }
    }

    private fun restoreAfterBoot(context: Context) {
        val alarmState = loadAlarmState(context)
        if (alarmState.alarmEnabled) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val reminderManager = ReminderManager(context, alarmManager)
            reminderManager.setDailyReminder(alarmState.hour, alarmState.minute)
        }
        if (alarmState.notificationEnabled) {
            LiveActivityService.start(context, alarmState.hour, alarmState.minute)
        }
    }

    companion object {
        const val PREFS_NAME = "lalema_reminder_prefs"
        const val KEY_ALARM_ENABLED = "alarm_enabled"
        const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        const val KEY_CALENDAR_ENABLED = "calendar_enabled"
        const val KEY_CALENDAR_EVENT_ID = "calendar_event_id"
        const val KEY_REMINDER_HOUR = "reminder_hour"
        const val KEY_REMINDER_MINUTE = "reminder_minute"

        fun saveAlarmState(
            context: Context,
            alarmEnabled: Boolean,
            notificationEnabled: Boolean,
            calendarEnabled: Boolean,
            calendarEventId: Long,
            hour: Int,
            minute: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean(KEY_ALARM_ENABLED, alarmEnabled)
                putBoolean(KEY_NOTIFICATION_ENABLED, notificationEnabled)
                putBoolean(KEY_CALENDAR_ENABLED, calendarEnabled)
                putLong(KEY_CALENDAR_EVENT_ID, calendarEventId)
                putInt(KEY_REMINDER_HOUR, hour)
                putInt(KEY_REMINDER_MINUTE, minute)
                apply()
            }
        }

        fun loadAlarmState(context: Context): AlarmState {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return AlarmState(
                alarmEnabled = prefs.getBoolean(KEY_ALARM_ENABLED, false),
                notificationEnabled = prefs.getBoolean(KEY_NOTIFICATION_ENABLED, false),
                calendarEnabled = prefs.getBoolean(KEY_CALENDAR_ENABLED, false),
                calendarEventId = prefs.getLong(KEY_CALENDAR_EVENT_ID, -1L),
                hour = prefs.getInt(KEY_REMINDER_HOUR, 8),
                minute = prefs.getInt(KEY_REMINDER_MINUTE, 0)
            )
        }
    }

    data class AlarmState(
        val alarmEnabled: Boolean,
        val notificationEnabled: Boolean,
        val calendarEnabled: Boolean,
        val calendarEventId: Long,
        val hour: Int,
        val minute: Int
    )
}
