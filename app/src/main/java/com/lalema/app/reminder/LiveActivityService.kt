package com.lalema.app.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import java.util.Calendar

class LiveActivityService : Service() {

    private var targetHour: Int = 8
    private var targetMinute: Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            showLiveNotification(targetHour, targetMinute)
            handler.postDelayed(this, 60_000L)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_UPDATE -> {
                targetHour = intent.getIntExtra("hour", targetHour)
                targetMinute = intent.getIntExtra("minute", targetMinute)
                showLiveNotification(targetHour, targetMinute)
                handler.removeCallbacks(updateRunnable)
                handler.postDelayed(updateRunnable, 60_000L)
            }
            ACTION_STOP -> {
                handler.removeCallbacks(updateRunnable)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationChannels.LIVE,
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

        val now = Calendar.getInstance()
        val totalMinutesInDay = 24 * 60
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val targetMinutes = hour * 60 + minute
        val progress = if (targetMinutes > 0) {
            ((currentMinutes.toFloat() / targetMinutes.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 0
        val ended = currentMinutes >= targetMinutes
        val timeStr = String.format("%02d:%02d", hour, minute)
        val statusText = if (ended) "提醒时间已过" else "等待 $timeStr..."

        val builder = Notification.Builder(this, NotificationChannels.LIVE)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("排便提醒 $timeStr")
            .setContentText(statusText)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_STATUS)
            .setShowWhen(false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val progressStyleClass = Class.forName("android.app.Notification\$ProgressStyle")
                val progressStyle = progressStyleClass.getDeclaredConstructor().apply { isAccessible = true }.newInstance()

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
                    setProgressEndMethod.invoke(progressStyle, ended)
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

        startForeground(NotificationIds.LIVE, builder.build())
    }

    companion object {
        const val ACTION_START = "com.lalema.app.LIVE_START"
        const val ACTION_STOP = "com.lalema.app.LIVE_STOP"
        const val ACTION_UPDATE = "com.lalema.app.LIVE_UPDATE"

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
