package com.lalema.app.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.lalema.app.ui.ai.AiChatScreen
import com.lalema.app.ui.ai.AiConfigScreen
import com.lalema.app.ui.ai.AiScreen
import com.lalema.app.ui.auth.AuthScreen
import com.lalema.app.ui.calendar.CalendarScreen
import com.lalema.app.ui.friends.FriendsScreen
import com.lalema.app.ui.home.HomeScreen
import com.lalema.app.ui.settings.SettingsScreen

@Composable
fun MainScreen(onThemeSettingsChanged: (com.lalema.app.ui.theme.ThemeSettings) -> Unit) {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var previousIndex by remember { mutableIntStateOf(0) }

    val screens = listOf(Screen.Home, Screen.Calendar, Screen.Friends, Screen.Ai, Screen.Settings)

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
    val pillShape = RoundedCornerShape(50)
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 64.dp)) {
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
                    composable(Screen.Friends.route) { FriendsScreen(navController) }
                    composable(Screen.Ai.route) { AiScreen(navController) }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            navController = navController,
                            onThemeSettingsChanged = onThemeSettingsChanged
                        )
                    }
                    composable("ai_config") { AiConfigScreen(navController) }
                    composable("ai_chat") { AiChatScreen(navController) }
                    composable("auth") { AuthScreen(navController) }
                }
            }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = pillShape,
                    ambientColor = if (isDark) Color.Black.copy(alpha = 0.40f) else Color(0xFF6080C0).copy(alpha = 0.14f),
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.50f) else Color(0xFF6080C0).copy(alpha = 0.18f)
                )
                .shadow(
                    elevation = 4.dp,
                    shape = pillShape,
                    ambientColor = if (isDark) primaryColor.copy(alpha = 0.06f) else primaryColor.copy(alpha = 0.03f),
                    spotColor = if (isDark) primaryColor.copy(alpha = 0.08f) else primaryColor.copy(alpha = 0.05f)
                )
                .clip(pillShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.White.copy(alpha = 0.05f),
                                Color.White.copy(alpha = 0.03f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.88f),
                                Color.White.copy(alpha = 0.75f),
                                Color.White.copy(alpha = 0.65f)
                            )
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.90f),
                                Color.White.copy(alpha = 0.40f)
                            )
                        }
                    ),
                    shape = pillShape
                )
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    // Top highlight strip
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.12f else 0.55f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = h * 0.30f
                        )
                    )
                    // Left subtle glow
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.06f else 0.30f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = w * 0.15f
                        )
                    )
                    // Inner glow from primary color
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = if (isDark) 0.04f else 0.02f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.3f),
                            radius = w * 0.6f
                        )
                    )
                }
                .padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEach { screen ->
                    val selected = currentRoute == screen.route
                    PillNavItem(
                        screen = screen,
                        selected = selected,
                        primaryColor = primaryColor,
                        isDark = isDark,
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

@Composable
private fun PillNavItem(
    screen: Screen,
    selected: Boolean,
    primaryColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "navIconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) primaryColor else Color.Transparent,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "navTextColor"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "bgAlpha"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "iconScale"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "indicatorAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                color = if (isDark) {
                    primaryColor.copy(alpha = 0.14f * bgAlpha)
                } else {
                    primaryColor.copy(alpha = 0.10f * bgAlpha)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = iconColor,
                modifier = Modifier
                    .size(22.dp)
                    .scale(iconScale)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = screen.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Dot indicator
            Box(
                modifier = Modifier
                    .size(3.dp)
                    .alpha(indicatorAlpha)
                    .clip(CircleShape)
                    .background(primaryColor)
            )
        }
    }
}
