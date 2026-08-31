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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.lalema.app.ui.theme.LiquidGlassButton
import com.lalema.app.ui.theme.LiquidGlassStatCard
import com.lalema.app.ui.theme.PrimaryLight
import com.lalema.app.ui.theme.TertiaryLight
import com.lalema.app.ui.theme.SuccessLight
import com.lalema.app.ui.theme.LocalGlassBackdrop
import kotlinx.coroutines.delay
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.backdrop.shadow.InnerShadow
import androidx.compose.ui.unit.DpOffset

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
    var showSuccessDialog by remember { mutableStateOf(false) }
    val isDark = LocalIsDarkTheme.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("check_in_prefs", android.content.Context.MODE_PRIVATE) }
    val lastShownRecordId = remember { mutableStateOf(prefs.getLong("last_shown_record_id", -1L)) }

    LaunchedEffect(uiState.todayRecords.size) {
        if (uiState.todayRecords.isNotEmpty()) {
            val latestRecord = uiState.todayRecords.maxByOrNull { it.id }
            val currentId = latestRecord?.id ?: -1L
            if (currentId > lastShownRecordId.value) {
                showSuccessDialog = true
                prefs.edit().putLong("last_shown_record_id", currentId).apply()
                lastShownRecordId.value = currentId
            }
        }
    }

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

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = if (isDark) Color(0xFF1A1C30) else Color(0xFFF0F0FA),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "打卡成功！",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text(
                    text = "今天已记录 ${uiState.todayRecords.size} 次排便，继续保持！",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "去生成海报",
                    onClick = {
                        showSuccessDialog = false
                        navController.navigate(Screen.Settings.route)
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("关闭", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

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
                modifier = Modifier.height(44.dp)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scale * pulseScale)
                        .drawBackdrop(
                            backdrop = LocalGlassBackdrop.current,
                            shape = { buttonShape },
                            effects = {
                                vibrancy()
                                blur(28f.dp.toPx())
                                lens(18f.dp.toPx(), 24f.dp.toPx())
                            },
                            highlight = { Highlight.Default },
                            shadow = {
                                Shadow(
                                    radius = 20.dp,
                                    offset = DpOffset(0.dp, 8.dp),
                                    color = primaryColor.copy(alpha = 0.28f)
                                )
                            },
                            innerShadow = {
                                InnerShadow(
                                    radius = 16.dp,
                                    color = Color.White.copy(alpha = if (isDark) 0.10f else 0.42f)
                                )
                            },
                            onDrawSurface = { drawRect(primaryColor.copy(alpha = if (isDark) 0.16f else 0.10f)) }
                        )
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
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "记录",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 20.sp,
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassStatCard(
                        value = "${uiState.streak}",
                        label = "连续打卡",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = TertiaryLight,
                                modifier = Modifier.size(24.dp)
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
                                modifier = Modifier.size(24.dp)
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
                                modifier = Modifier.size(24.dp)
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
