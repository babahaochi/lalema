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

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

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
            ACTION_UPDATE -> {
                val hour = intent.getIntExtra("hour", 8)
                val minute = intent.getIntExtra("minute", 0)
                showLiveNotification(hour, minute)
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channelId = CHANNEL_ID
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "实况通知",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "排便提醒实况通知"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showLiveNotification(hour: Int, minute: Int) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val timeStr = String.format("%02d:%02d", hour, minute)

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("排便提醒 $timeStr")
            .setContentText("等待提醒时间到达...")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_STATUS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val progressStyleClass = Class.forName("android.app.Notification\$ProgressStyle")
                val progressStyleConstructor = progressStyleClass.getDeclaredConstructor()
                progressStyleConstructor.isAccessible = true
                val progressStyle = progressStyleConstructor.newInstance()

                val now = Calendar.getInstance()
                val totalMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                val targetMinutes = hour * 60 + minute
                val progress = if (targetMinutes > totalMinutes) {
                    (totalMinutes * 100) / targetMinutes
                } else {
                    100
                }

                try {
                    val setProgressMethod = progressStyleClass.getDeclaredMethod(
                        "setProgress", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
                    )
                    setProgressMethod.isAccessible = true
                    setProgressMethod.invoke(progressStyle, 100, progress)
                } catch (_: Exception) {}

                try {
                    val setProgressEndMethod = progressStyleClass.getDeclaredMethod(
                        "setProgressEnd", Boolean::class.javaPrimitiveType
                    )
                    setProgressEndMethod.isAccessible = true
                    setProgressEndMethod.invoke(progressStyle, progress >= 100)
                } catch (_: Exception) {}

                try {
                    val setStyleMethod = Notification.Builder::class.java.getDeclaredMethod(
                        "setStyle", Class.forName("android.app.Notification\$Style")
                    )
                    setStyleMethod.isAccessible = true
                    setStyleMethod.invoke(builder, progressStyle)
                } catch (_: Exception) {}
            } catch (_: Exception) {}
        }

        startForeground(NOTIFICATION_ID, builder.build())
    }

    companion object {
        const val ACTION_START = "com.lalema.app.LIVE_START"
        const val ACTION_STOP = "com.lalema.app.LIVE_STOP"
        const val ACTION_UPDATE = "com.lalema.app.LIVE_UPDATE"
        const val NOTIFICATION_ID = 2001
        const val CHANNEL_ID = "lalema_live_channel"

        fun start(context: Context, hour: Int, minute: Int) {
            val intent = Intent(context, LiveActivityService::class.java).apply {
                action = ACTION_START
                putExtra("hour", hour)
                putExtra("minute", minute)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, LiveActivityService::class.java).apply {
                action = ACTION_STOP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun update(context: Context, hour: Int, minute: Int) {
            val intent = Intent(context, LiveActivityService::class.java).apply {
                action = ACTION_UPDATE
                putExtra("hour", hour)
                putExtra("minute", minute)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
