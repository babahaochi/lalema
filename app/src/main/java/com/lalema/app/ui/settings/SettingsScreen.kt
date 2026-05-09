@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.lalema.app.ui.settings

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.provider.MediaStore
import android.provider.Settings as AndroidSettings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.lalema.app.data.DataExportManager
import com.lalema.app.data.PoopRecord
import com.lalema.app.ui.theme.LocalIsDarkTheme
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
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.lalema.app.BuildConfig
import com.lalema.app.ui.theme.LiquidGlassCard
import com.lalema.app.ui.theme.LocalThemeSettings
import com.lalema.app.ui.theme.ThemeMode
import com.lalema.app.ui.theme.ThemePreferences
import com.lalema.app.ui.theme.colorPresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UpdateInfo(
    val tagName: String,
    val body: String,
    val htmlUrl: String,
    val publishedAt: String,
    val versionCode: Long,
    val isForceUpdate: Boolean
)

enum class UpdateCheckResult {
    HAS_UPDATE, UP_TO_DATE, NETWORK_ERROR
}

@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
    onThemeSettingsChanged: (com.lalema.app.ui.theme.ThemeSettings) -> Unit = {}
) {
    val context = LocalContext.current
    val currentSettings = LocalThemeSettings.current
    val scope = rememberCoroutineScope()
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateDialogInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    val alarmEnabled by viewModel.alarmEnabled.collectAsState()
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val calendarEnabled by viewModel.calendarEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()
    val anyReminderEnabled = alarmEnabled || notificationEnabled || calendarEnabled

    var showTimePicker by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showPosterDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }

    updateDialogInfo?.let { info ->
        val isDark = LocalIsDarkTheme.current
        AlertDialog(
            onDismissRequest = { updateDialogInfo = null },
            containerColor = if (isDark) Color(0xFF1A1C30) else Color(0xFFF0F0FA),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "发现新版本 ${info.tagName}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    if (info.body.isNotBlank()) {
                        Text(
                            text = info.body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Text(
                        text = "发布时间：${info.publishedAt}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.htmlUrl))
                        context.startActivity(intent)
                        updateDialogInfo = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                ) {
                    Text("前往下载", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { updateDialogInfo = null }) {
                    Text("稍后提醒", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showExportDialog) {
        val allRecords by viewModel.allRecords.collectAsState()
        ExportDataDialog(
            records = allRecords,
            isExporting = isExporting,
            onExport = { format, filteredRecords ->
                        isExporting = true
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                val (content, fileName, mimeType) = when (format) {
                                    "csv" -> Triple(
                                        DataExportManager.exportToCsv(filteredRecords),
                                        "lalema_export_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.csv",
                                        "text/csv"
                                    )
                                    else -> Triple(
                                        DataExportManager.exportToJson(filteredRecords),
                                        "lalema_export_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.json",
                                        "application/json"
                                    )
                                }
                                if (DataExportManager.saveToFile(context, content, fileName, mimeType)) fileName else null
                            }
                            isExporting = false
                            showExportDialog = false
                            if (result != null) {
                                Toast.makeText(context, "已导出到 Documents/LaLeMa/$result", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
            onDismiss = { showExportDialog = false }
        )
    }

    if (showPosterDialog) {
        val allRecords by viewModel.allRecords.collectAsState()
        PosterDialog(
            records = allRecords,
            onDismiss = { showPosterDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.height(48.dp)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(title = "提醒设置") {
                SettingItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    title = "闹钟提醒",
                    subtitle = if (alarmEnabled) "${reminderHour}:${String.format("%02d", reminderMinute)} 响铃" else "已关闭",
                    trailing = {
                        SwitchButton(
                            checked = alarmEnabled,
                            onCheckedChange = { viewModel.toggleAlarm() }
                        )
                    }
                )

                SettingItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    title = "实况通知提醒",
                    subtitle = if (notificationEnabled) {
                        if (viewModel.isAtLeastAndroid16) "灵动岛 + 通知栏" else "通知栏提醒"
                    } else "已关闭",
                    trailing = {
                        SwitchButton(
                            checked = notificationEnabled,
                            onCheckedChange = { viewModel.toggleNotification() }
                        )
                    }
                )

                SettingItem(
                    title = "日历日程提醒",
                    subtitle = if (calendarEnabled) "已在系统日历创建每日日程" else "已关闭",
                    trailing = {
                        SwitchButton(
                            checked = calendarEnabled,
                            onCheckedChange = { viewModel.toggleCalendar() }
                        )
                    }
                )

                if (anyReminderEnabled) {
                    SettingItem(
                        title = "提醒时间",
                        subtitle = "${reminderHour}:${String.format("%02d", reminderMinute)}",
                        onClick = { showTimePicker = !showTimePicker }
                    )

                    AnimatedVisibility(
                        visible = showTimePicker,
                        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(200)),
                        exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(150))
                    ) {
                        GlassInlineTimePicker(
                            hour = reminderHour,
                            minute = reminderMinute,
                            onHourChange = { viewModel.setReminderTime(it, reminderMinute) },
                            onMinuteChange = { viewModel.setReminderTime(reminderHour, it) }
                        )
                    }

                    SettingItem(
                        title = "忽略电池优化",
                        subtitle = "确保提醒在后台正常工作",
                        onClick = {
                            requestIgnoreBatteryOptimization(context)
                        }
                    )
                }
            }

            SettingsSection(title = "外观设置") {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "深色模式",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            ThemeModeOption(
                                mode = mode,
                                selected = currentSettings.themeMode == mode,
                                onClick = {
                                    val newSettings = currentSettings.copy(themeMode = mode)
                                    ThemePreferences.save(context, newSettings)
                                    onThemeSettingsChanged(newSettings)
                                }
                            )
                        }
                    }
                }

                LiquidGlassDividerThin()

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "主题颜色",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        colorPresets.forEachIndexed { index, preset ->
                            ColorPresetOption(
                                preset = preset,
                                selected = currentSettings.colorSchemeIndex == index,
                                onClick = {
                                    val newSettings = currentSettings.copy(colorSchemeIndex = index)
                                    ThemePreferences.save(context, newSettings)
                                    onThemeSettingsChanged(newSettings)
                                }
                            )
                        }
                    }
                }
            }

            SettingsSection(title = "数据") {
                SettingItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    title = "导出数据",
                    subtitle = "导出排便记录为 CSV 或 JSON 格式",
                    onClick = { showExportDialog = true }
                )
                SettingItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    title = "生成海报",
                    subtitle = "生成排便统计海报用于分享",
                    onClick = { showPosterDialog = true }
                )
            }

            SettingsSection(title = "其他") {
                SettingItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    title = "检查更新",
                    subtitle = if (isCheckingUpdate) "正在检查..." else "当前版本：${BuildConfig.VERSION_NAME}",
                    onClick = {
                        if (!isCheckingUpdate) {
                            isCheckingUpdate = true
                            scope.launch {
                                val result = try {
                                    withContext(Dispatchers.IO) { fetchLatestRelease() }
                                } catch (_: Exception) {
                                    Pair(UpdateCheckResult.NETWORK_ERROR, null)
                                }
                                isCheckingUpdate = false
                                when (result.first) {
                                    UpdateCheckResult.HAS_UPDATE -> updateDialogInfo = result.second
                                    UpdateCheckResult.UP_TO_DATE -> Toast.makeText(context, "已是最新版本", Toast.LENGTH_SHORT).show()
                                    UpdateCheckResult.NETWORK_ERROR -> Toast.makeText(context, "检查更新失败，请检查网络连接", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassDividerThin() {
    val isDark = LocalIsDarkTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.30f),
                        Color.Transparent
                    )
                )
            )
            .height(1.dp)
    )
}

@Composable
private fun SwitchButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = LocalIsDarkTheme.current

    val bgColor by animateColorAsState(
        targetValue = if (checked) {
            if (isDark) primaryColor.copy(alpha = 0.35f) else primaryColor.copy(alpha = 0.25f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.40f)
        },
        animationSpec = tween(300),
        label = "switchBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) primaryColor.copy(alpha = 0.4f) else
            if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.50f),
        animationSpec = tween(300),
        label = "switchBorder"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (checked) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "switchThumb"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "switchThumbOffset"
    )

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .offset(x = thumbOffset)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

private fun requestIgnoreBatteryOptimization(context: Context) {
    try {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
            val intent = Intent(AndroidSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "已忽略电池优化", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        try {
            val intent = Intent(AndroidSettings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        } catch (e2: Exception) {
            Toast.makeText(context, "请在电池设置中手动关闭优化", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun ThemeModeOption(
    mode: ThemeMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    val label = when (mode) {
        ThemeMode.SYSTEM -> "跟随系统"
        ThemeMode.LIGHT -> "浅色"
        ThemeMode.DARK -> "深色"
    }

    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "themeModeText"
    )

    val bgAlpha by animateFloatAsState(
        targetValue = if (selected) 0.12f else 0f,
        animationSpec = tween(250),
        label = "themeModeBg"
    )

    val isDark = LocalIsDarkTheme.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(primaryColor.copy(alpha = bgAlpha))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ColorPresetOption(
    preset: com.lalema.app.ui.theme.ColorPreset,
    selected: Boolean,
    onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "colorPresetText"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (selected) 2.5.dp else 0.dp,
        animationSpec = tween(250),
        label = "colorPresetBorder"
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "colorPresetScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(preset.primaryLight)
                .border(borderWidth, MaterialTheme.colorScheme.onBackground, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = preset.name,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val isDark = LocalIsDarkTheme.current
    val modifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (isDark) Color.White.copy(alpha = 0.04f) else Color.White.copy(alpha = 0.25f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            trailing()
        }
    }
}

fun fetchLatestRelease(): Pair<UpdateCheckResult, UpdateInfo?> {
    var connection: HttpURLConnection? = null
    try {
        val url = URL("https://api.github.com/repos/babahaochi/lalema/releases/latest")
        connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
        connection.setRequestProperty("User-Agent", "LaLeMa/${BuildConfig.VERSION_NAME}")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.instanceFollowRedirects = true

        val responseCode = connection.responseCode
        if (responseCode == 301 || responseCode == 302) {
            val redirectUrl = connection.getHeaderField("Location")
            connection.disconnect()
            connection = URL(redirectUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "LaLeMa/${BuildConfig.VERSION_NAME}")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
        }

        val finalCode = connection.responseCode
        if (finalCode != 200) {
            connection.disconnect()
            return Pair(UpdateCheckResult.NETWORK_ERROR, null)
        }

        val response = connection.inputStream.bufferedReader().readText()
        connection.disconnect()

        val obj = org.json.JSONObject(response)
        val tagName = obj.getString("tag_name")
        val body = obj.optString("body", "")
        val htmlUrl = obj.getString("html_url")
        val publishedAt = obj.optString("published_at", "")

        val currentVersionName = BuildConfig.VERSION_NAME
        if (tagName == "v$currentVersionName" || tagName == currentVersionName) {
            return Pair(UpdateCheckResult.UP_TO_DATE, null)
        }

        val assets = obj.optJSONArray("assets")
        var versionCode = 0L

        if (assets != null && assets.length() > 0) {
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name")
                if (name.endsWith(".apk")) {
                    versionCode = name.substringAfter("-").substringBefore(".apk").toLongOrNull() ?: 0L
                    break
                }
            }
        }

        return Pair(
            UpdateCheckResult.HAS_UPDATE,
            UpdateInfo(
                tagName = tagName,
                body = body,
                htmlUrl = htmlUrl,
                publishedAt = publishedAt.substringBefore("T"),
                versionCode = versionCode,
                isForceUpdate = false
            )
        )
    } catch (_: Exception) {
        connection?.disconnect()
        return Pair(UpdateCheckResult.NETWORK_ERROR, null)
    }
}

@Composable
private fun GlassInlineTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(shape)
            .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.35f))
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.45f), shape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GlassArrowButton(isDark = isDark) { onHourChange((hour + 1) % 24) }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp, 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.60f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", hour),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                GlassArrowButton(isDark = isDark, isUp = false) { onHourChange((hour - 1 + 24) % 24) }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = ":",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                GlassArrowButton(isDark = isDark) { onMinuteChange((minute + 1) % 60) }
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(56.dp, 48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.60f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", minute),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                GlassArrowButton(isDark = isDark, isUp = false) { onMinuteChange((minute - 1 + 60) % 60) }
            }
        }
    }
}

@Composable
private fun GlassArrowButton(
    isDark: Boolean,
    isUp: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "arrowScale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.40f))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isUp) "▲" else "▼",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ExportDataDialog(
    records: List<PoopRecord>,
    isExporting: Boolean,
    onExport: (String, List<PoopRecord>) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var selectedRange by remember { mutableStateOf("all") }

    val filteredRecords = when (selectedRange) {
        "month" -> records.filter {
            it.date.startsWith(java.time.YearMonth.now().toString())
        }
        "week" -> records.filter {
            val date = java.time.LocalDate.parse(it.date)
            val weekAgo = java.time.LocalDate.now().minusDays(7)
            !date.isBefore(weekAgo)
        }
        "30days" -> records.filter {
            val date = java.time.LocalDate.parse(it.date)
            val monthAgo = java.time.LocalDate.now().minusDays(30)
            !date.isBefore(monthAgo)
        }
        else -> records
    }

    val rangeOptions = listOf(
        "all" to "全部",
        "month" to "本月",
        "week" to "最近7天",
        "30days" to "最近30天"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF1A1C30) else Color(0xFFF0F0FA),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "导出数据",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = "共 ${filteredRecords.size} 条记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rangeOptions.forEach { (key, label) ->
                        val selected = selectedRange == key
                        val bgColor by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f) else if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.35f),
                            animationSpec = tween(200),
                            label = "rangeBg"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.40f),
                            animationSpec = tween(200),
                            label = "rangeBorder"
                        )
                        val textColor by animateColorAsState(
                            targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            animationSpec = tween(200),
                            label = "rangeText"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                                .clickable { selectedRange = key }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onExport("csv", filteredRecords) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isExporting && filteredRecords.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    ) {
                        Text("CSV", color = Color.White)
                    }
                    Button(
                        onClick = { onExport("json", filteredRecords) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = !isExporting && filteredRecords.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    ) {
                        Text("JSON", color = Color.White)
                    }
                }
                if (isExporting) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun PosterDialog(
    records: List<PoopRecord>,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }

    val totalRecords = records.size
    val totalDays = records.map { it.date }.distinct().size
    val streak = calculateStreak(records)
    val thisMonthRecords = records.filter {
        it.date.startsWith(java.time.YearMonth.now().toString())
    }.size
    val mostCommonTime = records.groupBy { it.timeHour }
        .maxByOrNull { it.value.size }?.key?.let { String.format("%02d:00", it) } ?: "--"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF1A1C30) else Color(0xFFF0F0FA),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "生成海报",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "拉了吗",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "我的排便报告",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PosterStatItem("总记录", "$totalRecords", "次")
                            PosterStatItem("打卡天数", "$totalDays", "天")
                            PosterStatItem("连续打卡", "$streak", "天")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PosterStatItem("本月记录", "$thisMonthRecords", "次")
                            PosterStatItem("常用时间", mostCommonTime, "")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "LaLeMa · 拉了吗",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isGenerating = true
                    scope.launch {
                        val bitmap = generatePosterBitmap(
                            totalRecords = totalRecords,
                            totalDays = totalDays,
                            streak = streak,
                            thisMonthRecords = thisMonthRecords,
                            mostCommonTime = mostCommonTime,
                            isDark = isDark
                        )
                        isGenerating = false
                        if (bitmap != null) {
                            val fileName = "lalema_poster_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.png"
                            val saved = savePosterBitmap(context, bitmap, fileName)
                            if (saved) {
                                Toast.makeText(context, "海报已保存到 Pictures/LaLeMa/$fileName", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                enabled = !isGenerating && records.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                )
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("保存海报", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun PosterStatItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun calculateStreak(records: List<PoopRecord>): Int {
    val dates = records.map { it.date }.distinct().sorted().toSet()
    var streak = 0
    var date = java.time.LocalDate.now()
    for (i in 0..365) {
        if (date.toString() in dates) {
            streak++
            date = date.minusDays(1)
        } else {
            break
        }
    }
    return streak
}

private fun generatePosterBitmap(
    totalRecords: Int,
    totalDays: Int,
    streak: Int,
    thisMonthRecords: Int,
    mostCommonTime: String,
    isDark: Boolean
): android.graphics.Bitmap? {
    val width = 1080
    val height = 1920
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val bgColor = if (isDark) android.graphics.Color.parseColor("#0D0F1A") else android.graphics.Color.parseColor("#F0F0FA")
    canvas.drawColor(bgColor)

    val primaryColor = android.graphics.Color.parseColor("#6750A4")
    val textGray = if (isDark) android.graphics.Color.parseColor("#B0B0C0") else android.graphics.Color.parseColor("#666680")

    val titlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor
        textSize = 96f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText("拉了吗", width / 2f, 400f, titlePaint)

    val subtitlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = textGray
        textSize = 40f
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText("我的排便报告", width / 2f, 470f, subtitlePaint)

    val cardBgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = if (isDark) android.graphics.Color.parseColor("#1A1C30") else android.graphics.Color.parseColor("#FFFFFF")
        alpha = 180
    }
    val cardRect = android.graphics.RectF(80f, 560f, (width - 80).toFloat(), 1200f)
    canvas.drawRoundRect(cardRect, 60f, 60f, cardBgPaint)

    val statValuePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor
        textSize = 80f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }
    val statLabelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = textGray
        textSize = 36f
        textAlign = android.graphics.Paint.Align.CENTER
    }

    val statY = 780f
    val colWidth = (width - 160f) / 3
    canvas.drawText("$totalRecords", 80f + colWidth * 0.5f, statY, statValuePaint)
    canvas.drawText("总记录", 80f + colWidth * 0.5f, statY + 50f, statLabelPaint)
    canvas.drawText("$totalDays", 80f + colWidth * 1.5f, statY, statValuePaint)
    canvas.drawText("打卡天数", 80f + colWidth * 1.5f, statY + 50f, statLabelPaint)
    canvas.drawText("$streak", 80f + colWidth * 2.5f, statY, statValuePaint)
    canvas.drawText("连续打卡", 80f + colWidth * 2.5f, statY + 50f, statLabelPaint)

    val statY2 = 1000f
    val colWidth2 = (width - 160f) / 2f
    canvas.drawText("$thisMonthRecords", 80f + colWidth2 * 0.5f, statY2, statValuePaint)
    canvas.drawText("本月记录", 80f + colWidth2 * 0.5f, statY2 + 50f, statLabelPaint)
    canvas.drawText(mostCommonTime, 80f + colWidth2 * 1.5f, statY2, statValuePaint)
    canvas.drawText("常用时间", 80f + colWidth2 * 1.5f, statY2 + 50f, statLabelPaint)

    val footerPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = textGray
        alpha = 150
        textSize = 32f
        textAlign = android.graphics.Paint.Align.CENTER
    }
    canvas.drawText("LaLeMa · 拉了吗", width / 2f, 1400f, footerPaint)

    return bitmap
}

private fun savePosterBitmap(context: Context, bitmap: android.graphics.Bitmap, fileName: String): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/LaLeMa")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.getContentUri("external"),
                contentValues
            ) ?: return false
            context.contentResolver.openOutputStream(uri)?.use { os ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, os)
            }
            true
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "LaLeMa"
            )
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use { fos ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
            }
            true
        }
    } catch (_: Exception) {
        false
    }
}
