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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Brown500,
    onPrimary = Brown50,
    primaryContainer = Brown100,
    onPrimaryContainer = Brown900,
    secondary = WarmOrange500,
    onSecondary = Brown900,
    secondaryContainer = WarmOrange200,
    onSecondaryContainer = Brown800,
    tertiary = Brown700,
    onTertiary = Brown50,
    tertiaryContainer = Brown200,
    onTertiaryContainer = Brown800,
    background = Brown50,
    onBackground = Brown900,
    surface = Brown50,
    onSurface = Brown900,
    surfaceVariant = Brown100,
    onSurfaceVariant = Brown700,
    outline = Brown400,
    outlineVariant = Brown200
)

private val DarkColorScheme = darkColorScheme(
    primary = Brown200,
    onPrimary = Brown800,
    primaryContainer = Brown600,
    onPrimaryContainer = Brown100,
    secondary = WarmOrange400,
    onSecondary = Brown800,
    secondaryContainer = WarmOrange700,
    onSecondaryContainer = WarmOrange200,
    tertiary = Brown300,
    onTertiary = Brown700,
    tertiaryContainer = Brown600,
    onTertiaryContainer = Brown100,
    background = Brown900,
    onBackground = Brown50,
    surface = Brown900,
    onSurface = Brown50,
    surfaceVariant = Brown800,
    onSurfaceVariant = Brown200,
    outline = Brown500,
    outlineVariant = Brown700
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
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
