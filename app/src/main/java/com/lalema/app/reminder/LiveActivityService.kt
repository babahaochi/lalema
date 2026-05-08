package com.lalema.app.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import java.util.Calendar

class LiveActivityService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val hour = intent.getIntExtra("hour", 8)
                val minute = intent.getIntExtra("minute", 0)
                showLiveNotification(hour, minute)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun showLiveNotification(hour: Int, minute: Int) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "lalema_live_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "实况通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "排便提醒实况通知"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val timeStr = String.format("%02d:%02d", hour, minute)

        val notification = if (Build.VERSION.SDK_INT >= 36) {
            buildProgressStyleNotification(channelId, pendingIntent, timeStr)
        } else {
            Notification.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("排便提醒 $timeStr")
                .setContentText("等待提醒时间到达...")
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()
        }

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun buildProgressStyleNotification(
        channelId: String,
        pendingIntent: PendingIntent,
        timeStr: String
    ): Notification {
        val builder = Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("排便提醒 $timeStr")
            .setContentText("等待提醒时间到达...")
            .setOngoing(true)
            .setContentIntent(pendingIntent)

        try {
            val progressStyleClass = Class.forName("android.app.Notification\$ProgressStyle")
            val progressStyleConstructor = progressStyleClass.getDeclaredConstructor()
            progressStyleConstructor.isAccessible = true
            val progressStyle = progressStyleConstructor.newInstance()

            val now = Calendar.getInstance()
            val totalMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
            val targetMinutes = timeStr.substring(0, 2).toInt() * 60 + timeStr.substring(3, 5).toInt()
            val progress = if (targetMinutes > totalMinutes) {
                (totalMinutes * 100) / targetMinutes
            } else {
                100
            }

            val setProgressMethod = progressStyleClass.getDeclaredMethod(
                "setProgress", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
            )
            setProgressMethod.isAccessible = true
            setProgressMethod.invoke(progressStyle, 100, progress)

            val setProgressEndMethod = progressStyleClass.getDeclaredMethod(
                "setProgressEnd", Boolean::class.javaPrimitiveType
            )
            setProgressEndMethod.isAccessible = true
            setProgressEndMethod.invoke(progressStyle, progress >= 100)

            val setStyleMethod = Notification.Builder::class.java.getDeclaredMethod(
                "setStyle", Class.forName("android.app.Notification\$Style")
            )
            setStyleMethod.isAccessible = true
            setStyleMethod.invoke(builder, progressStyle)
        } catch (_: Exception) {
        }

        return builder.build()
    }

    companion object {
        const val ACTION_START = "com.lalema.app.LIVE_START"
        const val ACTION_STOP = "com.lalema.app.LIVE_STOP"
        const val NOTIFICATION_ID = 2001
    }
}
