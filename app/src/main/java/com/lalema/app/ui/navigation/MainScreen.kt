package com.lalema.app.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.lalema.app.ui.theme.LocalIsDarkTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lalema.app.ui.calendar.CalendarScreen
import com.lalema.app.ui.home.HomeScreen
import com.lalema.app.ui.settings.SettingsScreen

@Composable
fun MainScreen(onThemeSettingsChanged: (com.lalema.app.ui.theme.ThemeSettings) -> Unit) {
    val navController = rememberNavController()
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
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.background(Color.Transparent)) {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(navBarShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0xFF0D0F1A).copy(alpha = 0.30f),
                                    Color(0xFF0D0F1A).copy(alpha = 0.45f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.25f),
                                    Color.White.copy(alpha = 0.40f)
                                )
                            }
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        color = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.25f),
                        shape = navBarShape
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    screens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        GlassNavItem(
                            screen = screen,
                            selected = selected,
                            primaryColor = primaryColor,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassNavItem(
    screen: Screen,
    selected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 24.dp else 0.dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "indicatorWidth"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(250),
        label = "indicatorAlpha"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "navIconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "navTextColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = screen.title,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(primaryColor.copy(alpha = indicatorAlpha))
        )
    }
}
