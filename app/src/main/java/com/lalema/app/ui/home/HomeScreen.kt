package com.lalema.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.lalema.app.ui.theme.LocalIsDarkTheme
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TagFaces
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lalema.app.data.PoopColor
import com.lalema.app.data.PoopConsistency
import com.lalema.app.data.PoopRecord
import com.lalema.app.ui.navigation.Screen
import com.lalema.app.ui.theme.LiquidGlassCard
import com.lalema.app.ui.theme.LiquidGlassStatCard
import com.lalema.app.ui.theme.PrimaryLight
import com.lalema.app.ui.theme.TertiaryLight
import com.lalema.app.ui.theme.SuccessLight
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "buttonScale"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "buttonPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    var showContent by remember { mutableStateOf(false) }
    val isDark = LocalIsDarkTheme.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val isHomeActive = navBackStackEntry?.destination?.route == Screen.Home.route

    LaunchedEffect(isHomeActive) {
        if (isHomeActive) {
            viewModel.loadTodayStatus()
            delay(100)
            showContent = true
        } else {
            showContent = false
        }
    }

    PoopRecordForm(
        show = uiState.showRecordForm,
        onDismiss = { viewModel.hideRecordForm() },
        onSubmit = { timeHour, timeMinute, amount, consistency, color, smell, painLevel, blood, mucus, notes ->
            viewModel.recordToday(timeHour, timeMinute, amount, consistency, color, smell, painLevel, blood, mucus, notes)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "拉了吗",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.height(48.dp)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(500, easing = FastOutSlowInEasing)) + slideInVertically(
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
                    initialOffsetY = { -30 }
                )
            ) {
                Text(
                    text = "今天感觉如何？",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 150, easing = FastOutSlowInEasing)) + slideInVertically(
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 250f),
                    initialOffsetY = { 50 }
                )
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val buttonShape = CircleShape

                val glassBg = if (isDark) {
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.25f),
                            primaryColor.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.70f),
                            primaryColor.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.50f)
                        )
                    )
                }

                val borderColor = if (isDark) {
                    primaryColor.copy(alpha = 0.35f)
                } else {
                    Color.White.copy(alpha = 0.80f)
                }

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(scale * pulseScale)
                        .shadow(
                            elevation = 16.dp,
                            shape = buttonShape,
                            spotColor = primaryColor.copy(alpha = 0.2f)
                        )
                        .clip(buttonShape)
                        .background(brush = glassBg)
                        .border(width = 1.5.dp, color = borderColor, shape = buttonShape)
                        .drawBehind {
                            val h = size.height
                            val w = size.width
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (isDark) 0.08f else 0.40f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = h * 0.35f
                                )
                            )
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = if (isDark) 0.04f else 0.20f),
                                        Color.Transparent
                                    ),
                                    center = Offset(w * 0.35f, h * 0.2f),
                                    radius = w * 0.5f
                                )
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            pressed = true
                            viewModel.showRecordForm()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "记录",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LaunchedEffect(pressed) {
                if (pressed) {
                    delay(150)
                    pressed = false
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 300, easing = FastOutSlowInEasing)) + slideInVertically(
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 200f),
                    initialOffsetY = { 30 }
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidGlassStatCard(
                        value = "${uiState.streak}",
                        label = "连续打卡",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = TertiaryLight,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    LiquidGlassStatCard(
                        value = "${uiState.monthCount}",
                        label = "本月次数",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.TagFaces,
                                contentDescription = null,
                                tint = PrimaryLight,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                    LiquidGlassStatCard(
                        value = "${(uiState.monthRate * 100).toInt()}%",
                        label = "打卡率",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = SuccessLight,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 450, easing = FastOutSlowInEasing)) + slideInVertically(
                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 180f),
                    initialOffsetY = { 30 }
                )
            ) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(Screen.Calendar.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "查看日历",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showContent && uiState.todayRecords.isNotEmpty(),
                enter = expandVertically(animationSpec = tween(600, delayMillis = 800)) + fadeIn(animationSpec = tween(600, delayMillis = 800)),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    TodayRecordsSection(records = uiState.todayRecords)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TodayRecordsSection(records: List<PoopRecord>) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "今日记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            records.forEachIndexed { index, record ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                RecordItem(record = record)
            }
        }
    }
}

@Composable
private fun RecordItem(record: PoopRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${record.timeHour}:${String.format("%02d", record.timeMinute)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = PoopConsistency.valueOf(record.consistency).displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = PoopColor.valueOf(record.color).displayName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
