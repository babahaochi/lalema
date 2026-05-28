package com.lalema.app.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.graphicsLayer
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
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
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

        // Glassmorphism Bottom Navigation Bar - centered
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Deep ambient shadow layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .shadow(
                        elevation = 24.dp,
                        shape = pillShape,
                        ambientColor = Color.Black.copy(alpha = 0.50f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    )
            )
            // Colored subtle glow shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .shadow(
                        elevation = 8.dp,
                        shape = pillShape,
                        ambientColor = primaryColor.copy(alpha = if (isDark) 0.12f else 0.08f),
                        spotColor = primaryColor.copy(alpha = if (isDark) 0.15f else 0.10f)
                    )
            )
            // Main glass container
            Box(
                modifier = Modifier
                    .clip(pillShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color.White.copy(alpha = 0.12f),
                                    Color.White.copy(alpha = 0.06f),
                                    Color.White.copy(alpha = 0.03f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.72f),
                                    Color.White.copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.40f)
                                )
                            }
                        )
                    )
                    .border(
                        width = 0.5.dp,
                        brush = Brush.verticalGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color.White.copy(alpha = 0.20f),
                                    Color.White.copy(alpha = 0.06f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.80f),
                                    Color.White.copy(alpha = 0.30f)
                                )
                            }
                        ),
                        shape = pillShape
                    )
                    .drawBehind {
                        val w = size.width
                        val h = size.height
                        // Top highlight strip - glass edge reflection
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.15f else 0.50f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = h * 0.25f
                            )
                        )
                        // Left subtle glow
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.08f else 0.25f),
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = w * 0.12f
                            )
                        )
                        // Inner glow from primary color
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = if (isDark) 0.06f else 0.03f),
                                    Color.Transparent
                                ),
                                center = Offset(w * 0.5f, h * 0.3f),
                                radius = w * 0.55f
                            )
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    screens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        GlassNavItem(
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
}

@Composable
private fun GlassNavItem(
    screen: Screen,
    selected: Boolean,
    primaryColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "navIconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) primaryColor else Color.Transparent,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "navTextColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 350f),
        label = "iconScale"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "indicatorAlpha"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) 0.15f else 0f,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "glowAlpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Active indicator background glow
        Box(
            modifier = Modifier
                .matchParentSize()
                .alpha(glowAlpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        if (selected) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val iconOffset by animateDpAsState(
                    targetValue = (-1).dp,
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    label = "iconOffset"
                )
                Icon(
                    imageVector = screen.icon,
                    contentDescription = screen.title,
                    tint = iconColor,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(iconScale)
                        .offset(y = iconOffset)
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = screen.title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    lineHeight = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .alpha(indicatorAlpha)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
            }
        } else {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.title,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .scale(iconScale)
            )
        }
    }
}
