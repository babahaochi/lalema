package com.lalema.app.ui.navigation

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.lalema.app.ui.theme.LocalIsDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalema.app.ui.calendar.CalendarScreen
import com.lalema.app.ui.home.HomeScreen
import com.lalema.app.ui.settings.SettingsScreen

@Composable
fun MainScreen(onThemeSettingsChanged: (com.lalema.app.ui.theme.ThemeSettings) -> Unit) {
    val navController = androidx.navigation.compose.rememberNavController()
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var previousIndex by remember { mutableIntStateOf(0) }

    val screens = listOf(Screen.Home, Screen.Calendar, Screen.Settings)

    DisposableEffect(navBackStackEntry) {
        val currentEntry = navBackStackEntry
        if (currentEntry != null) {
            currentRoute = currentEntry.destination.route ?: Screen.Home.route
        }
        onDispose {}
    }

    val currentScreen = screens.find { it.route == currentRoute }
    val currentIndex = screens.indexOf(currentScreen).takeIf { it >= 0 } ?: 0
    val direction = if (currentIndex >= previousIndex) 1 else -1
    previousIndex = currentIndex

    val isDark = LocalIsDarkTheme.current
    val navBarShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    val highlightAlpha = if (isDark) 0.06f else 0.40f

    Box(
        modifier = Modifier.background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (direction > 0) fullWidth else -fullWidth },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(250))
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (direction > 0) -fullWidth else fullWidth },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200))
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> if (direction > 0) -fullWidth else fullWidth },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(250))
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> if (direction > 0) fullWidth else -fullWidth },
                            animationSpec = tween(350, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(200))
                    }
                ) {
                    composable(Screen.Home.route) { HomeScreen(navController) }
                    composable(Screen.Calendar.route) { CalendarScreen(navController) }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            navController = navController,
                            onThemeSettingsChanged = onThemeSettingsChanged
                        )
                    }
                }
            }

            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = navBarShape,
                        spotColor = if (isDark) Color.Black.copy(alpha = 0.2f) else Color(0xFF6080C0).copy(alpha = 0.06f)
                    )
                    .clip(navBarShape)
                    .blur(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 0.8.dp else 0.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0xFF0D0F1A).copy(alpha = 0.85f),
                                    Color(0xFF0D0F1A).copy(alpha = 0.92f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.72f),
                                    Color.White.copy(alpha = 0.82f)
                                )
                            }
                        )
                    )
                    .border(width = 1.dp, color = if (isDark) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.40f), shape = navBarShape)
                    .drawBehind {
                        val h = size.height
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = highlightAlpha),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = h * 0.3f
                            )
                        )
                    }
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                screens.forEachIndexed { _, screen ->
                    val selected = currentRoute == screen.route
                    val indicatorWidth by animateDpAsState(
                        targetValue = if (selected) 24.dp else 0.dp,
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        label = "indicatorWidth"
                    )
                    val indicatorAlpha by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        animationSpec = tween(250),
                        label = "indicatorAlpha"
                    )

                    NavigationBarItem(
                        icon = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(indicatorWidth)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = indicatorAlpha)
                                        )
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 12.sp
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
