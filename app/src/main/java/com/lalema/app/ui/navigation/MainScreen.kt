package com.lalema.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
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
import com.lalema.app.ui.theme.GlassMotion
import com.lalema.app.ui.theme.LocalGlassBackdrop
import com.lalema.app.ui.theme.LocalIsDarkTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

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
                    fadeIn(animationSpec = tween(GlassMotion.DURATION_NAV_ENTER)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(GlassMotion.DURATION_NAV_ENTER))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(GlassMotion.DURATION_NAV_EXIT)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(GlassMotion.DURATION_NAV_EXIT))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(GlassMotion.DURATION_NAV_ENTER)) +
                        scaleIn(initialScale = 0.96f, animationSpec = tween(GlassMotion.DURATION_NAV_ENTER))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(GlassMotion.DURATION_NAV_EXIT)) +
                        scaleOut(targetScale = 0.96f, animationSpec = tween(GlassMotion.DURATION_NAV_EXIT))
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
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val surfaceColor by animateColorAsState(
        targetValue = if (isDark) {
            Color.White.copy(alpha = 0.10f)
        } else {
            Color.White.copy(alpha = 0.42f)
        },
        animationSpec = tween(GlassMotion.DURATION_THEME),
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
            // ── 可拖动玻璃药丸轨道 ──
            // 药丸是独立玻璃块，通过 pillProgress(0..count-1) 控制 x 位移；
            // 水平拖拽跟手、松手吸附最近槽位；点击槽位亦以动画移动药丸。
            val slotCount = screens.size
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                val pillProgress = remember {
                    androidx.compose.animation.core.Animatable(
                        screens.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0).toFloat()
                    )
                }
                var isDragging by remember { mutableStateOf(false) }

                // 路由变化（点击 / 返回键 / 深链）→ 药丸吸附过去
                LaunchedEffect(currentRoute) {
                    if (!isDragging) {
                        pillProgress.animateTo(
                            targetValue = screens.indexOfFirst { it.route == currentRoute }
                                .coerceAtLeast(0).toFloat(),
                            animationSpec = GlassMotion.control()
                        )
                    }
                }

                // 药丸：玻璃折射块（始终位于图标之上）
                val slotWidthDp = maxWidth / slotCount
                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = (slotWidthDp * pillProgress.value).roundToPx(), y = 0) }
                        .width(slotWidthDp - 8.dp)
                        .fillMaxHeight()
                        .padding(vertical = 1.dp)
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { RoundedCornerShape(20.dp) },
                            effects = {
                                blur(24f.dp.toPx())
                                lens(14f.dp.toPx(), 18f.dp.toPx())
                            },
                            highlight = { Highlight.Default },
                            innerShadow = {
                                InnerShadow(
                                    radius = 14.dp,
                                    color = Color.White.copy(alpha = if (isDark) 0.14f else 0.42f)
                                )
                            },
                            onDrawSurface = {
                                drawRect(
                                    primaryColor.copy(
                                        alpha = if (isDark) 0.28f else 0.20f
                                    )
                                )
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = if (isDark) 0.14f else 0.30f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = size.height * 0.25f
                                    )
                                )
                            }
                        )
                ) {}

                // 图标行（透明点击层，接收点击选中；玻璃药丸在其上方但不遮挡视觉）
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(slotCount, slotWidthDp) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    scope.launch { pillProgress.stop() }
                                },
                                onDragEnd = {
                                    isDragging = false
                                    val target = pillProgress.value.roundToInt()
                                        .coerceIn(0, slotCount - 1)
                                    scope.launch {
                                        pillProgress.animateTo(
                                            targetValue = target.toFloat(),
                                            animationSpec = GlassMotion.control()
                                        )
                                    }
                                    onNavClick(screens[target].route)
                                },
                                onDragCancel = {
                                    isDragging = false
                                    scope.launch {
                                        pillProgress.animateTo(
                                            targetValue = pillProgress.value.roundToInt()
                                                .coerceIn(0, slotCount - 1).toFloat(),
                                            animationSpec = GlassMotion.control()
                                        )
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    val slotWidthPx = with(density) { slotWidthDp.toPx() }
                                    val newProgress = pillProgress.value + dragAmount / slotWidthPx
                                    scope.launch {
                                        pillProgress.snapTo(newProgress.coerceIn(0f, slotCount - 1f))
                                    }
                                }
                            )
                        }
                ) {
                    screens.forEachIndexed { index, screen ->
                        val selected = currentRoute == screen.route
                        GlassNavItem(
                            screen = screen,
                            selected = selected,
                            primaryColor = primaryColor,
                            isDark = isDark,
                            onClick = {
                                scope.launch {
                                    pillProgress.animateTo(
                                        targetValue = index.toFloat(),
                                        animationSpec = androidx.compose.animation.core.tween(
                                            GlassMotion.DURATION_CONTROL
                                        )
                                    )
                                }
                                onNavClick(screen.route)
                            },
                            modifier = Modifier.weight(1f)
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        animationSpec = tween(GlassMotion.DURATION_CONTROL),
        label = "navIconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        animationSpec = tween(GlassMotion.DURATION_CONTROL),
        label = "navTextColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = GlassMotion.control(),
        label = "iconScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.title,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .scale(iconScale)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = screen.title,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            lineHeight = 12.sp
        )
    }
}
