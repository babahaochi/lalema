package com.lalema.app.ui.settings

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalema.app.BuildConfig
import com.lalema.app.ui.theme.LocalThemeSettings
import com.lalema.app.ui.theme.ThemeMode
import com.lalema.app.ui.theme.ThemeSettings
import com.lalema.app.ui.theme.colorPresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onThemeSettingsChanged: (ThemeSettings) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentSettings = LocalThemeSettings.current
    val isSystemDark = isSystemInDarkTheme()

    var calendarReminderEnabled by remember { mutableStateOf(false) }
    var calendarReminderHour by remember { mutableStateOf(8) }
    var calendarReminderMinute by remember { mutableStateOf(0) }
    var liveActivityEnabled by remember { mutableStateOf(false) }

    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateDialogInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
        calendarReminderEnabled = prefs.getBoolean("calendar_reminder_enabled", false)
        calendarReminderHour = prefs.getInt("calendar_reminder_hour", 8)
        calendarReminderMinute = prefs.getInt("calendar_reminder_minute", 0)
        liveActivityEnabled = prefs.getBoolean("live_activity_enabled", false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "设置",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "外观", icon = Icons.Default.Palette) {
            Text(
                text = "主题模式",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ThemeMode.entries.forEach { mode ->
                    val label = when (mode) {
                        ThemeMode.SYSTEM -> "跟随系统"
                        ThemeMode.LIGHT -> "浅色"
                        ThemeMode.DARK -> "深色"
                    }
                    FilterChip(
                        selected = currentSettings.themeMode == mode,
                        onClick = {
                            onThemeSettingsChanged(currentSettings.copy(themeMode = mode))
                        },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "配色方案",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                colorPresets.forEachIndexed { index, preset ->
                    ColorSchemeChip(
                        name = preset.name,
                        color = preset.primaryLight,
                        selected = currentSettings.colorSchemeIndex == index,
                        darkColor = preset.primaryDark,
                        isDark = currentSettings.themeMode == ThemeMode.DARK ||
                                (currentSettings.themeMode == ThemeMode.SYSTEM && isSystemDark),
                        onClick = {
                            onThemeSettingsChanged(currentSettings.copy(colorSchemeIndex = index))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSection(title = "提醒", icon = Icons.Default.Notifications) {
            SettingSwitchItem(
                title = "日历提醒",
                subtitle = "在系统日历中创建每日提醒",
                icon = Icons.Default.CalendarMonth,
                checked = calendarReminderEnabled,
                onCheckedChange = { enabled ->
                    calendarReminderEnabled = enabled
                    context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("calendar_reminder_enabled", enabled).apply()

                    if (enabled) {
                        val reminderManager = com.lalema.app.reminder.ReminderManager(
                            context,
                            context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                        )
                        reminderManager.createCalendarEvent(
                            "每日排便提醒", "记得打卡记录哦~",
                            calendarReminderHour, calendarReminderMinute
                        )
                        Toast.makeText(context, "日历提醒已设置", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            SettingSwitchItem(
                title = "实况通知",
                subtitle = "在通知栏显示连续打卡进度",
                icon = Icons.Default.Widgets,
                checked = liveActivityEnabled,
                onCheckedChange = { enabled ->
                    liveActivityEnabled = enabled
                    context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
                        .edit().putBoolean("live_activity_enabled", enabled).apply()

                    if (enabled) {
                        showStreakLiveNotification(context)
                        Toast.makeText(context, "实况通知已开启", Toast.LENGTH_SHORT).show()
                    } else {
                        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nm.cancel(2)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SettingsSection(title = "关于", icon = Icons.Default.CheckCircle) {
            SettingItem(
                title = "版本",
                subtitle = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isCheckingUpdate) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("正在检查更新...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                SettingClickableItem(
                    title = "检查更新",
                    subtitle = "当前版本 v${BuildConfig.VERSION_NAME}",
                    icon = Icons.Default.Update,
                    onClick = {
                        isCheckingUpdate = true
                        scope.launch {
                            try {
                                val info = checkForUpdate()
                                isCheckingUpdate = false
                                if (info != null) {
                                    updateDialogInfo = info
                                } else {
                                    Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show()
                                }
                            } catch (_: Exception) {
                                isCheckingUpdate = false
                                Toast.makeText(context, "检查更新失败，请稍后重试", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (updateDialogInfo != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { updateDialogInfo = null },
            icon = { Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("发现新版本") },
            text = {
                Column {
                    Text("新版本: ${updateDialogInfo!!.tagName}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("当前版本: v${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (updateDialogInfo!!.body.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(updateDialogInfo!!.body, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(updateDialogInfo!!.htmlUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "无法打开浏览器", Toast.LENGTH_SHORT).show()
                        }
                        updateDialogInfo = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("前往下载")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { updateDialogInfo = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ColorSchemeChip(
    name: String,
    color: Color,
    darkColor: Color,
    isDark: Boolean,
    selected: Boolean,
    onClick: () -> Unit
) {
    val displayColor = if (isDark) darkColor else color
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) displayColor.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surface
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) displayColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(displayColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) displayColor else MaterialTheme.colorScheme.onSurface
        )
    }
}

private data class UpdateInfo(
    val tagName: String,
    val htmlUrl: String,
    val body: String
)

private suspend fun checkForUpdate(): UpdateInfo? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/babahaochi/lalema/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "LaLeMa-Android")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.instanceFollowRedirects = true

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.bufferedReader()?.readText() ?: throw IOException("Empty response")
            val json = JSONObject(response)
            val tagName = json.getString("tag_name")
            val htmlUrl = json.getString("html_url")
            val body = json.optString("body", "")

            val latestVersion = tagName.removePrefix("v")
            val currentVersion = BuildConfig.VERSION_NAME

            if (isNewerVersion(latestVersion, currentVersion)) {
                UpdateInfo(tagName, htmlUrl, body.take(200))
            } else {
                null
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }
}

private fun isNewerVersion(latest: String, current: String): Boolean {
    val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
    val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
        val l = latestParts.getOrElse(i) { 0 }
        val c = currentParts.getOrElse(i) { 0 }
        if (l > c) return true
        if (l < c) return false
    }
    return false
}

private fun showStreakLiveNotification(context: Context) {
    val channelId = "lalema_streak_channel"
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "打卡进度",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "显示连续打卡进度"
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }

    val intent = try {
        val clazz = Class.forName("com.lalema.app.MainActivity")
        PendingIntent.getActivity(
            context, 0,
            Intent(context, clazz),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    } catch (_: Exception) { null }

    val notification = if (Build.VERSION.SDK_INT >= 36) {
        buildLiveActivityNotification(context, channelId, intent)
    } else {
        buildStandardNotification(context, channelId, intent)
    }

    nm.notify(2, notification)
}

@Suppress("NewApi")
private fun buildLiveActivityNotification(
    context: Context,
    channelId: String,
    pendingIntent: PendingIntent?
): Notification {
    return try {
        val style = Notification.ProgressStyle()
        style.javaClass.getMethod("setProgress", Int::class.javaPrimitiveType).invoke(style, 7)
        try {
            style.javaClass.getMethod("setMaxProgress", Int::class.javaPrimitiveType).invoke(style, 30)
        } catch (_: Exception) {}
        try {
            val icon = Icon.createWithResource(context, android.R.drawable.ic_dialog_info)
            style.javaClass.getMethod("setProgressTrackerIcon", Icon::class.java).invoke(style, icon)
        } catch (_: Exception) {}

        Notification.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🔥 连续打卡 7 天")
            .setContentText("继续保持！目标 30 天")
            .setStyle(style)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    } catch (_: Exception) {
        buildStandardNotification(context, channelId, pendingIntent)
    }
}

private fun buildStandardNotification(
    context: Context,
    channelId: String,
    pendingIntent: PendingIntent?
): Notification {
    return Notification.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("🔥 连续打卡 7 天")
        .setContentText("继续保持！")
        .setOngoing(true)
        .setContentIntent(pendingIntent)
        .build()
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingItem(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun SettingClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
