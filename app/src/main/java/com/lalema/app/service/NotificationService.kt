package com.lalema.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.lalema.app.MainActivity
import com.lalema.app.R
import com.lalema.app.api.ApiClient
import com.lalema.app.api.NotificationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object NotificationService {
    private const val CHANNEL_ID = "lalema_reminder"
    private const val NOTIFICATION_ID_BASE = 10000
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount
    private var pollJob: Job? = null

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "好友提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "好友打卡提醒通知"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun startPolling(context: Context, scope: CoroutineScope) {
        stopPolling()
        createChannel(context)
        pollJob = scope.launch(Dispatchers.IO) {
            var lastId = 0L
            while (true) {
                try {
                    if (ApiClient.isLoggedIn(context)) {
                        val api = ApiClient.getService(context)
                        val resp = api.getNotifications(lastId)
                        if (resp.code == 200) {
                            val newList = resp.data ?: emptyList()
                            if (newList.isNotEmpty()) {
                                lastId = newList.maxOf { it.id }
                                for (n in newList) {
                                    showSystemNotification(context, n)
                                }
                            }
                            val countResp = api.getUnreadCount()
                            if (countResp.code == 200) {
                                _unreadCount.value = countResp.data ?: 0
                            }
                        }
                    }
                } catch (_: Exception) {}
                delay(15000L)
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun refreshUnreadCount(context: Context, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                if (ApiClient.isLoggedIn(context)) {
                    val api = ApiClient.getService(context)
                    val resp = api.getUnreadCount()
                    if (resp.code == 200) {
                        _unreadCount.value = resp.data ?: 0
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun showSystemNotification(context: Context, n: NotificationData) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, n.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(n.title)
            .setContentText(n.content ?: "")
            .setStyle(NotificationCompat.BigTextStyle().bigText(n.content ?: ""))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_BASE + n.id.toInt(), builder.build())
    }
}
