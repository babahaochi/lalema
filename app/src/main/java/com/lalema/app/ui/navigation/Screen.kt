package com.lalema.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
}
