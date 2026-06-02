package com.lalema.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.navigation.NavController
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
import com.lalema.app.ui.theme.LocalIsDarkTheme

@Composable
fun MainScreen(onThemeSettingsChanged: (com.lalema.app.ui.theme.ThemeSettings) -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute by remember {
        derivedStateOf {
            navBackStackEntry?.destination?.route ?: Screen.Home.route
        }
    }

    val screens = remember { listOf(Screen.Home, Screen.Calendar, Screen.Friends, Screen.Ai, Screen.Settings) }

    val isDark = LocalIsDarkTheme.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val systemBars = WindowInsets.systemBars
    val statusBarTop = systemBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = systemBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarTop, bottom = 96.dp + navBarBottom)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(160, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(220, easing = FastOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(160, easing = FastOutSlowInEasing)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(160, easing = FastOutSlowInEasing))
                }
            ) {
                composable(Screen.Home.route) {
                    TabTransition(currentRoute) { HomeScreen(navController) }
                }
                composable(Screen.Calendar.route) {
                    TabTransition(currentRoute) { CalendarScreen(navController) }
                }
                composable(Screen.Friends.route) {
                    TabTransition(currentRoute) { FriendsScreen(navController) }
                }
                composable(Screen.Ai.route) {
                    TabTransition(currentRoute) { AiScreen(navController) }
                }
                composable(Screen.Settings.route) {
                    TabTransition(currentRoute) {
                        SettingsScreen(
                            navController = navController,
                            onThemeSettingsChanged = onThemeSettingsChanged
                        )
                    }
                }
                composable("ai_config") {
                    StackTransition { AiConfigScreen(navController) }
                }
                composable("ai_chat") {
                    StackTransition { AiChatScreen(navController) }
                }
                composable("auth") {
                    StackTransition { AuthScreen(navController) }
                }
            }
        }

        BottomNavBar(
            currentRoute = currentRoute,
            screens = screens,
            isDark = isDark,
            primaryColor = primaryColor,
            onNavClick = { route -> navigateTab(navController, route) }
        )
    }
}

@Composable
private fun TabTransition(currentRoute: String, content: @Composable () -> Unit) {
    androidx.compose.runtime.key(currentRoute) {
        content()
    }
}

@Composable
private fun StackTransition(content: @Composable () -> Unit) {
    content()
}

private fun navigateTab(navController: NavController, route: String) {
    if (navController.currentDestination?.route == route) return
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun BottomNavBar(
    currentRoute: String,
    screens: List<Screen>,
    isDark: Boolean,
    primaryColor: Color,
    onNavClick: (String) -> Unit
) {
    val pillShape = RoundedCornerShape(50)
    val systemBars = WindowInsets.systemBars
    val navBarBottom = systemBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp + navBarBottom)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    shape = pillShape,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.30f),
                    clip = false
                )
                .shadow(
                    elevation = 8.dp,
                    shape = pillShape,
                    ambientColor = primaryColor.copy(alpha = if (isDark) 0.18f else 0.10f),
                    spotColor = primaryColor.copy(alpha = if (isDark) 0.20f else 0.12f),
                    clip = false
                )
                .clip(pillShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.White.copy(alpha = 0.14f),
                                Color.White.copy(alpha = 0.07f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.80f),
                                Color.White.copy(alpha = 0.60f),
                                Color.White.copy(alpha = 0.45f)
                            )
                        }
                    )
                )
                .border(
                    width = 0.5.dp,
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.90f),
                                Color.White.copy(alpha = 0.30f)
                            )
                        }
                    ),
                    shape = pillShape
                )
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.18f else 0.55f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = h * 0.25f
                        )
                    )
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isDark) 0.10f else 0.28f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = w * 0.12f
                        )
                    )
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = if (isDark) 0.08f else 0.04f),
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
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
                        onClick = { onNavClick(screen.route) }
                    )
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
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "navIconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) primaryColor else Color.Transparent,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "navTextColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 350f),
        label = "iconScale"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "indicatorAlpha"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (selected) 0.15f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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
                Icon(
                    imageVector = screen.icon,
                    contentDescription = screen.title,
                    tint = iconColor,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(iconScale)
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
