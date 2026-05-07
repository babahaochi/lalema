package com.lalema.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Reminders
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.*


class ReminderManager(
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
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
            if (calendarId == -1L) {
                Toast.makeText(context, "未找到可用的日历账户，请先在系统日历中添加账户", Toast.LENGTH_LONG).show()
                return false
            }

            val eventValues = ContentValues().apply {
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
                put(Events.HAS_ALARM, 1)
            }

            val eventUri = context.contentResolver.insert(Events.CONTENT_URI, eventValues)
            if (eventUri == null) {
                Toast.makeText(context, "创建日历事件失败：无法插入事件", Toast.LENGTH_SHORT).show()
                return false
            }

            val eventId = eventUri.lastPathSegment?.toLong()
            if (eventId == null) {
                Toast.makeText(context, "创建日历事件失败：无法获取事件ID", Toast.LENGTH_SHORT).show()
                return false
            }

            val reminderValues = ContentValues().apply {
                put(Reminders.EVENT_ID, eventId)
                put(Reminders.METHOD, Reminders.METHOD_ALERT)
                put(Reminders.MINUTES, 0)
            }

            val reminderUri = context.contentResolver.insert(Reminders.CONTENT_URI, reminderValues)
            if (reminderUri == null) {
                Toast.makeText(context, "创建提醒失败", Toast.LENGTH_SHORT).show()
            }

            true
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "权限不足：${e.message}", Toast.LENGTH_SHORT).show()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "创建日历事件失败：${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun getDefaultCalendarId(): Long {
        return try {
            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.IS_PRIMARY
            )

            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                CalendarContract.Calendars.IS_PRIMARY + " DESC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                    if (idIndex >= 0) {
                        cursor.getLong(idIndex)
                    } else {
                        -1L
                    }
                } else {
                    -1L
                }
            } ?: -1L
        } catch (e: SecurityException) {
            e.printStackTrace()
            -1L
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

            val alarmState = BootReceiver.loadAlarmState(context)
            if (alarmState.first) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val nextCalendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, alarmState.second)
                    set(Calendar.MINUTE, alarmState.third)
                    set(Calendar.SECOND, 0)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    ReminderManager.REQUEST_CODE_DAILY,
                    Intent(context, ReminderReceiver::class.java).apply {
                        action = ReminderManager.ACTION_REMINDER
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextCalendar.timeInMillis,
                            pendingIntent
                        )
                    }
                }
            }
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
