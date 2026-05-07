package com.lalema.app.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Immutable
data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorSchemeIndex: Int = 0
)

object ThemePreferences {
    private const val PREF_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_COLOR_INDEX = "color_index"

    fun load(context: Context): ThemeSettings {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return ThemeSettings(
            themeMode = ThemeMode.entries.getOrElse(prefs.getInt(KEY_THEME_MODE, 0)) { ThemeMode.SYSTEM },
            colorSchemeIndex = prefs.getInt(KEY_COLOR_INDEX, 0)
        )
    }

    fun save(context: Context, settings: ThemeSettings) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_THEME_MODE, settings.themeMode.ordinal)
            .putInt(KEY_COLOR_INDEX, settings.colorSchemeIndex)
            .apply()
    }
}

val LocalThemeSettings = compositionLocalOf { ThemeSettings() }

private fun buildLightScheme(preset: ColorPreset) = lightColorScheme(
    primary = preset.primaryLight,
    onPrimary = Color.White,
    primaryContainer = preset.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = preset.primaryLight,
    secondary = preset.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = preset.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = preset.secondaryLight,
    tertiary = preset.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = preset.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = preset.tertiaryLight,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFE8E8F0),
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorLight,
    onError = Color.White,
    outline = Color(0xFFB0B0C8),
    outlineVariant = Color(0xFFD8D8E8)
)

private fun buildDarkScheme(preset: ColorPreset) = darkColorScheme(
    primary = preset.primaryDark,
    onPrimary = Color(0xFF1A1C30),
    primaryContainer = preset.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = preset.primaryDark,
    secondary = preset.secondaryDark,
    onSecondary = Color(0xFF1A1C30),
    secondaryContainer = preset.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = preset.secondaryDark,
    tertiary = preset.tertiaryDark,
    onTertiary = Color(0xFF1A1C30),
    tertiaryContainer = preset.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = preset.tertiaryDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF2A2C40),
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    onError = Color(0xFF1A1C30),
    outline = Color(0xFF5A5A70),
    outlineVariant = Color(0xFF3A3A50)
)

@Composable
fun LaLeMaTheme(
    themeSettings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {
    val presetIndex = themeSettings.colorSchemeIndex.coerceIn(0, colorPresets.size - 1)
    val preset = colorPresets[presetIndex]

    val isDark = when (themeSettings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) buildDarkScheme(preset) else buildLightScheme(preset)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
