package com.lalema.app

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.lalema.app.service.NotificationService
import com.lalema.app.ui.navigation.MainScreen
import com.lalema.app.ui.theme.GlassBackground
import com.lalema.app.ui.theme.LaLeMaTheme
import com.lalema.app.ui.theme.LocalThemeSettings
import com.lalema.app.ui.theme.ThemeMode
import com.lalema.app.ui.theme.ThemePreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            var themeSettings by remember { mutableStateOf(ThemePreferences.load(context)) }
            var themeKey by remember { mutableIntStateOf(0) }

            val isDark = when (themeSettings.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            LaunchedEffect(isDark) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    window.statusBarColor = Color.TRANSPARENT
                    window.navigationBarColor = Color.TRANSPARENT
                }
            }

            LaunchedEffect(Unit) {
                NotificationService.startPolling(applicationContext, scope)
            }

            CompositionLocalProvider(LocalThemeSettings provides themeSettings) {
                key(themeKey) {
                    LaLeMaTheme(themeSettings = themeSettings) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            GlassBackground()
                            MainScreen(
                                onThemeSettingsChanged = { newSettings ->
                                    themeSettings = newSettings
                                    themeKey++
                                    ThemePreferences.save(context, newSettings)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationService.stopPolling()
    }
}
