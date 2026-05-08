package com.lalema.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lalema.app.ui.navigation.MainScreen
import com.lalema.app.ui.theme.GlassBackground
import androidx.compose.runtime.Composable
import com.lalema.app.ui.theme.LaLeMaTheme
import com.lalema.app.ui.theme.LocalThemeSettings
import com.lalema.app.ui.theme.ThemePreferences
import com.lalema.app.ui.theme.ThemeSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var themeSettings by remember { mutableStateOf(ThemePreferences.load(context)) }
            var themeKey by remember { mutableIntStateOf(0) }

            CompositionLocalProvider(LocalThemeSettings provides themeSettings) {
                KeyedTheme(
                    themeKey = themeKey,
                    themeSettings = themeSettings,
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

@Composable
private fun KeyedTheme(
    themeKey: Int,
    themeSettings: ThemeSettings,
    onThemeSettingsChanged: (ThemeSettings) -> Unit
) {
    LaLeMaTheme(themeSettings = themeSettings) {
        Box(modifier = Modifier.fillMaxSize()) {
            GlassBackground()
            MainScreen(onThemeSettingsChanged = onThemeSettingsChanged)
        }
    }
}
