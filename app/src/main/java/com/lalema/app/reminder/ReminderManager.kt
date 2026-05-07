package com.lalema.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Reminders
import androidx.core.content.ContextCompat
import java.util.*
import javax.inject.Inject

class ReminderManager @Inject constructor(
    private val context: Context,
    private val alarmManager: AlarmManager
) {

    fun setDailyReminder(hour: Int, minute: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelDailyReminder() {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DAILY,
            intent,
            PendingIntent.FLAG_NO_CREATE or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun createCalendarEvent(title: String, description: String, hour: Int, minute: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readPermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR)
            val writePermission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CALENDAR)
            if (readPermission != PackageManager.PERMISSION_GRANTED || writePermission != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return try {
            val calendarId = getDefaultCalendarId()
            if (calendarId == -1L) return false

            val eventValues = android.content.ContentValues().apply {
                put(Events.TITLE, title)
                put(Events.DESCRIPTION, description)
                put(Events.CALENDAR_ID, calendarId)
                put(Events.EVENT_TIMEZONE, TimeZone.getDefault().id)

                val start = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }.timeInMillis

                val end = Calendar.getInstance().apply {
                    timeInMillis = start
                    add(Calendar.HOUR_OF_DAY, 1)
                }.timeInMillis

                put(Events.DTSTART, start)
                put(Events.DTEND, end)
                put(Events.ALL_DAY, 0)
                put(Events.RRULE, "FREQ=DAILY")
            }

            context.contentResolver.insert(Events.CONTENT_URI, eventValues)?.let { eventUri ->
                val eventId = eventUri.lastPathSegment?.toLong() ?: return false

                val reminderValues = android.content.ContentValues().apply {
                    put(Reminders.EVENT_ID, eventId)
                    put(Reminders.METHOD, Reminders.METHOD_ALERT)
                    put(Reminders.MINUTES, 0)
                }

                context.contentResolver.insert(Reminders.CONTENT_URI, reminderValues)
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getDefaultCalendarId(): Long {
        return try {
            val projection = arrayOf(CalendarContract.Calendars._ID)
            val selection = "${CalendarContract.Calendars.IS_PRIMARY} = 1"

            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                selection,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getLong(0)
                } else {
                    -1L
                }
            } ?: -1L
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    companion object {
        const val ACTION_REMINDER = "com.lalema.app.REMINDER_ACTION"
        const val REQUEST_CODE_DAILY = 1001
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ReminderManager.ACTION_REMINDER) {
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        val channelId = "lalema_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "排便提醒",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "每日排便提醒"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, Class.forName("com.lalema.app.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = android.app.Notification.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("该上厕所啦！")
            .setContentText("记得记录今天的排便情况哦")
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}