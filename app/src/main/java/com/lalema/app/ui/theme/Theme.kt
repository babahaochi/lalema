package com.lalema.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight.copy(alpha = 0.15f),
    onPrimaryContainer = PrimaryLight,
    secondary = SecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLight.copy(alpha = 0.15f),
    onSecondaryContainer = SecondaryLight,
    tertiary = TertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryLight.copy(alpha = 0.15f),
    onTertiaryContainer = TertiaryLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = CardBackgroundLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineLight.copy(alpha = 0.5f),
    surfaceTint = PrimaryLight,
    inverseSurface = OnBackgroundLight,
    inverseOnSurface = BackgroundLight,
    inversePrimary = PrimaryDark,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorLight.copy(alpha = 0.15f),
    onErrorContainer = ErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnBackgroundLight,
    primaryContainer = PrimaryDark.copy(alpha = 0.2f),
    onPrimaryContainer = PrimaryDark,
    secondary = SecondaryDark,
    onSecondary = OnBackgroundLight,
    secondaryContainer = SecondaryDark.copy(alpha = 0.2f),
    onSecondaryContainer = SecondaryDark,
    tertiary = TertiaryDark,
    onTertiary = OnBackgroundLight,
    tertiaryContainer = TertiaryDark.copy(alpha = 0.2f),
    onTertiaryContainer = TertiaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = CardBackgroundDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineDark.copy(alpha = 0.5f),
    surfaceTint = PrimaryDark,
    inverseSurface = BackgroundLight,
    inverseOnSurface = OnBackgroundLight,
    inversePrimary = PrimaryLight,
    error = ErrorDark,
    onError = OnBackgroundLight,
    errorContainer = ErrorDark.copy(alpha = 0.2f),
    onErrorContainer = ErrorDark
)

@Composable
fun LaLeMaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
