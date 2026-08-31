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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.lalema.app.ui.ai.AiChatScreen
import com.lalema.app.ui.ai.AiConfigScreen
import com.lalema.app.ui.ai.AiScreen
import com.lalema.app.ui.auth.AuthScreen
import com.lalema.app.ui.calendar.CalendarScreen
import com.lalema.app.ui.friends.FriendsScreen
import com.lalema.app.ui.home.HomeScreen
import com.lalema.app.ui.settings.SettingsScreen
import com.lalema.app.ui.theme.LocalGlassBackdrop
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

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
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
    val backdrop = LocalGlassBackdrop.current

    val surfaceColor by animateColorAsState(
        targetValue = if (isDark) {
            Color.White.copy(alpha = 0.10f)
        } else {
            Color.White.copy(alpha = 0.42f)
        },
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "navSurface"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp + navBarBottom)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { pillShape },
                    effects = {
                        blur(28f.dp.toPx())
                        lens(20f.dp.toPx(), 24f.dp.toPx())
                    },
                    highlight = { Highlight.Default },
                    shadow = {
                        Shadow(
                            radius = 26.dp,
                            offset = DpOffset(0.dp, 10.dp),
                            color = Color.Black.copy(alpha = if (isDark) 0.45f else 0.16f)
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = 24.dp,
                            color = Color.White.copy(alpha = if (isDark) 0.10f else 0.45f)
                        )
                    },
                    onDrawSurface = {
                        drawRect(surfaceColor)
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (isDark) 0.18f else 0.50f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = size.height * 0.30f
                            )
                        )
                    }
                )
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

    val backdrop = LocalGlassBackdrop.current

    val chipSurface by animateColorAsState(
        targetValue = if (selected) {
            primaryColor.copy(alpha = if (isDark) 0.22f else 0.16f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "chipSurface"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .then(
                if (selected) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop,
                        shape = { RoundedCornerShape(20.dp) },
                        effects = {
                            blur(16f.dp.toPx())
                            lens(10f.dp.toPx(), 14f.dp.toPx())
                        },
                        highlight = { Highlight.Default },
                        innerShadow = {
                            InnerShadow(
                                radius = 12.dp,
                                color = Color.White.copy(alpha = if (isDark) 0.12f else 0.40f)
                            )
                        },
                        onDrawSurface = { drawRect(chipSurface) }
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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
