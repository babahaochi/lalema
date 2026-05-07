package com.lalema.app.reminder

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            restoreAlarm(context)
        }
    }

    private fun restoreAlarm(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_ALARM_ENABLED, false)

        if (isEnabled) {
            val hour = prefs.getInt(KEY_ALARM_HOUR, 8)
            val minute = prefs.getInt(KEY_ALARM_MINUTE, 0)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val reminderManager = ReminderManager(context, alarmManager)
            reminderManager.setDailyReminder(hour, minute)
        }
    }

    companion object {
        private const val PREFS_NAME = "lalema_reminder_prefs"
        private const val KEY_ALARM_ENABLED = "alarm_enabled"
        private const val KEY_ALARM_HOUR = "alarm_hour"
        private const val KEY_ALARM_MINUTE = "alarm_minute"

        fun saveAlarmState(context: Context, enabled: Boolean, hour: Int, minute: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean(KEY_ALARM_ENABLED, enabled)
                putInt(KEY_ALARM_HOUR, hour)
                putInt(KEY_ALARM_MINUTE, minute)
                apply()
            }
        }

        fun loadAlarmState(context: Context): Triple<Boolean, Int, Int> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean(KEY_ALARM_ENABLED, false)
            val hour = prefs.getInt(KEY_ALARM_HOUR, 8)
            val minute = prefs.getInt(KEY_ALARM_MINUTE, 0)
            return Triple(enabled, hour, minute)
        }
    }
}
