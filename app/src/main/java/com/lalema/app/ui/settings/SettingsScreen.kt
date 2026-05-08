package com.lalema.app.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

data class UpdateInfo(
    val tagName: String,
    val body: String,
    val htmlUrl: String,
    val publishedAt: String,
    val versionCode: Long,
    val isForceUpdate: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val reminderEnabled by viewModel.reminderEnabled.collectAsState()
    val reminderHour by viewModel.reminderHour.collectAsState()
    val reminderMinute by viewModel.reminderMinute.collectAsState()

    updateDialogInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { updateDialogInfo = null },
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
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.htmlUrl))
                    context.startActivity(intent)
                    updateDialogInfo = null
                }) {
                    Text("前往下载", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { updateDialogInfo = null }) {
                    Text("稍后提醒")
                }
            }
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
                    title = "每日提醒",
                    subtitle = if (reminderEnabled) "提醒时间：${reminderHour}:${String.format("%02d", reminderMinute)}" else "已关闭",
                    trailing = {
                        SwitchButton(
                            checked = reminderEnabled,
                            onCheckedChange = { viewModel.toggleReminder() }
                        )
                    }
                )

                if (reminderEnabled) {
                    SettingItem(
                        title = "提醒时间",
                        subtitle = "${reminderHour}:${String.format("%02d", reminderMinute)}",
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    viewModel.setReminderTime(hour, minute)
                                },
                                reminderHour,
                                reminderMinute,
                                true
                            ).show()
                        }
                    )

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
                                    null
                                }
                                isCheckingUpdate = false
                                when {
                                    result != null -> updateDialogInfo = result
                                    else -> Toast.makeText(context, "检查更新失败", Toast.LENGTH_SHORT).show()
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
    val isDark = isSystemInDarkTheme()
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
    val isDark = isSystemInDarkTheme()

    val bgColor = if (checked) {
        if (isDark) primaryColor.copy(alpha = 0.35f) else primaryColor.copy(alpha = 0.25f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.40f)
    }

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (checked) primaryColor.copy(alpha = 0.4f) else
                    if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.50f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (checked) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}

private fun requestIgnoreBatteryOptimization(context: Context) {
    try {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "已忽略电池优化", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
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
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                .clip(CircleShape)
                .background(preset.primaryLight)
                .then(
                    if (selected) Modifier
                        .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                    else Modifier
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = preset.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
    val isDark = isSystemInDarkTheme()
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

fun fetchLatestRelease(): UpdateInfo? {
    val url = URL("https://api.github.com/repos/babahaochi/lalema/releases/latest")
    val connection = url.openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
    connection.connectTimeout = 8000
    connection.readTimeout = 8000

    val response = connection.inputStream.bufferedReader().readText()
    connection.disconnect()

    val obj = org.json.JSONObject(response)
    val tagName = obj.getString("tag_name")
    val body = obj.optString("body", "")
    val htmlUrl = obj.getString("html_url")
    val publishedAt = obj.optString("published_at", "")

    val assets = obj.optJSONArray("assets")
    var versionCode = 0L
    var isForceUpdate = false

    if (assets != null && assets.length() > 0) {
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk")) {
                val downloadUrl = asset.getString("browser_download_url")
                val verCode = downloadUrl.substringAfter("app-").substringBefore("/").toLongOrNull() ?: 0L
                versionCode = verCode
                break
            }
        }
    }

    val latestVersionCode = versionCode
    if (latestVersionCode <= BuildConfig.VERSION_CODE) {
        return null
    }

    return UpdateInfo(
        tagName = tagName,
        body = body,
        htmlUrl = htmlUrl,
        publishedAt = publishedAt.substringBefore("T"),
        versionCode = versionCode,
        isForceUpdate = isForceUpdate
    )
}
