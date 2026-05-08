package com.lalema.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "主页", Icons.Default.Home)
    data object Calendar : Screen("calendar", "日历", Icons.Default.CalendarMonth)
    data object Settings : Screen("settings", "设置", Icons.Default.Settings)
}
