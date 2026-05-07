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
    primary = Brown600,
    onPrimary = Brown50,
    primaryContainer = Brown100,
    onPrimaryContainer = Brown900,
    secondary = WarmOrange500,
    onSecondary = Brown900,
    secondaryContainer = OrangeLight,
    onSecondaryContainer = Brown800,
    tertiary = Green600,
    onTertiary = Brown50,
    tertiaryContainer = GreenLight,
    onTertiaryContainer = Green700,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Brown100,
    onSurfaceVariant = Brown700,
    outline = OutlineLight,
    outlineVariant = Brown200,
    surfaceTint = Brown600,
    inverseSurface = Brown900,
    inverseOnSurface = Brown50,
    inversePrimary = Brown300,
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Brown200,
    onPrimary = Brown900,
    primaryContainer = Brown700,
    onPrimaryContainer = Brown100,
    secondary = WarmOrange400,
    onSecondary = Brown900,
    secondaryContainer = Brown800,
    onSecondaryContainer = WarmOrange200,
    tertiary = Green400,
    onTertiary = Brown900,
    tertiaryContainer = Green700,
    onTertiaryContainer = GreenLight,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Brown800,
    onSurfaceVariant = Brown200,
    outline = OutlineDark,
    outlineVariant = Brown700,
    surfaceTint = Brown200,
    inverseSurface = Brown100,
    inverseOnSurface = Brown900,
    inversePrimary = Brown600,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
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
