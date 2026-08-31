@file:OptIn(ExperimentalMaterial3Api::class)

package com.lalema.app.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.lalema.app.ui.theme.LocalIsDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.lalema.app.data.PoopAmount
import com.lalema.app.data.PoopColor
import com.lalema.app.data.PoopConsistency
import com.lalema.app.data.PoopRecord
import com.lalema.app.data.PoopSmell
import com.lalema.app.ui.home.PoopRecordForm
import com.lalema.app.ui.theme.LiquidGlassButton
import com.lalema.app.ui.theme.LiquidGlassCard
import com.lalema.app.ui.theme.PrimaryLight
import com.lalema.app.ui.theme.SuccessLight
import com.lalema.app.ui.theme.WarningLight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@Composable
fun CalendarScreen(
    navController: NavHostController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = LocalIsDarkTheme.current

    PoopRecordForm(
        show = state.showRecordForm,
        onDismiss = { viewModel.dismissDialog() },
        onSubmit = { timeHour, timeMinute, amount, consistency, color, smell, painLevel, blood, mucus, notes ->
            viewModel.makeupRecord(timeHour, timeMinute, amount, consistency, color, smell, painLevel, blood, mucus, notes)
        }
    )

    var recordToDelete by remember { mutableStateOf<PoopRecord?>(null) }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            containerColor = if (isDark) Color(0xFF1A1C30) else Color(0xFFF0F0FA),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "删除记录",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "确定要删除这条 ${record.timeHour}:${String.format("%02d", record.timeMinute)} 的记录吗？",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "删除",
                    onClick = {
                        viewModel.deleteRecord(record.id)
                        recordToDelete = null
                    },
                    tint = MaterialTheme.colorScheme.error
                )
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (state.showDetailDialog && state.selectedDate != null) {
        DateDetailBottomSheet(
            state = state,
            onDismiss = { viewModel.dismissDialog() },
            onAddRecord = { date ->
                viewModel.showRecordFormForDate(date)
            },
            onDeleteRecord = { record ->
                viewModel.deleteRecord(record.id)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "日历",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousMonth() }) {
                            Icon(
                                Icons.Filled.ChevronLeft,
                                contentDescription = "上个月",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${state.currentYearMonth.year}年${state.currentYearMonth.monthValue}月",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = "下个月",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("日", "一", "二", "三", "四", "五", "六").forEach { dayOfWeek ->
                            Text(
                                text = dayOfWeek,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val today = LocalDate.now()

                    AnimatedContent(
                        targetState = state.currentYearMonth,
                        transitionSpec = {
                            val direction = if (targetState > initialState) 1 else -1
                            (slideInHorizontally(
                                initialOffsetX = { fullWidth -> direction * fullWidth / 3 },
                                animationSpec = tween(300)
                            ) + fadeIn(tween(200))) togetherWith
                            (slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -direction * fullWidth / 3 },
                                animationSpec = tween(300)
                            ) + fadeOut(tween(200)))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = "monthTransition"
                    ) { yearMonth ->
                        val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value % 7
                        val daysInMonth = yearMonth.lengthOfMonth()
                        val totalRawCells = firstDayOffset + daysInMonth
                        val remainder = totalRawCells % 7
                        val totalCells = if (remainder == 0) totalRawCells else totalRawCells + (7 - remainder)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        userScrollEnabled = false
                    ) {
                        items(
                            count = totalCells,
                            key = { index ->
                                val day = index - firstDayOffset + 1
                                if (index < firstDayOffset || day > daysInMonth) {
                                    "empty_$index"
                                } else {
                                    yearMonth.atDay(day).format(dateFormatter)
                                }
                            }
                        ) { index ->
                            val day = index - firstDayOffset + 1
                            val isEmpty = index < firstDayOffset || day > daysInMonth

                            if (isEmpty) {
                                Box(modifier = Modifier.aspectRatio(1f))
                            } else {
                                val date = yearMonth.atDay(day)
                                val dateStr = date.format(dateFormatter)
                                val isRecorded = dateStr in state.recordedDates
                                val isToday = date == today
                                val isFuture = date.isAfter(today)
                                val isPast7Days = date.isBefore(today) && !date.isBefore(today.minusDays(6))
                                val isMakeupAvailable = isPast7Days && !isRecorded

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .then(
                                            if (!isFuture)
                                                Modifier.clickable { viewModel.onDateClick(dateStr) }
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        isRecorded -> Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(
                                                            SuccessLight.copy(alpha = 0.8f),
                                                            SuccessLight
                                                        )
                                                    )
                                                )
                                                .shadow(4.dp, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        isToday -> Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, PrimaryLight, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        isFuture -> Text(
                                            text = day.toString(),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        isMakeupAvailable -> Text(
                                            text = day.toString(),
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        else -> Text(
                                            text = day.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LegendSection()
        }
    }
}

@Composable
private fun LegendSection() {
    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            Text(
                text = "图例",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = SuccessLight, text = "已记录")
                LegendItem(color = PrimaryLight, text = "今天", isOutline = true)
                LegendItem(color = WarningLight, text = "可补卡")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String, isOutline: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .then(
                    if (isOutline) Modifier.border(2.dp, color, CircleShape)
                    else Modifier.background(color)
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RecordDetailCard(
    record: PoopRecord,
    onDelete: () -> Unit
) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${record.timeHour}:${String.format("%02d", record.timeMinute)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailItem(label = "量", value = PoopAmount.valueOf(record.amount).displayName)
                DetailItem(label = "干稀", value = PoopConsistency.valueOf(record.consistency).displayName)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "颜色",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(50.dp)
                )
                val colorHex = PoopColor.valueOf(record.color).colorHex
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(android.graphics.Color.parseColor(colorHex).let { Color(it) })
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = PoopColor.valueOf(record.color).displayName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                DetailItem(label = "气味", value = PoopSmell.valueOf(record.smell).displayName)
                DetailItem(label = "疼痛", value = getPainLevelText(record.painLevel))
            }

            if (record.blood || record.mucus) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (record.blood) {
                        Text(
                            text = "有血",
                            fontSize = 14.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (record.blood && record.mucus) {
                        Text(
                            text = " | ",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (record.mucus) {
                        Text(
                            text = "有粘液",
                            fontSize = 14.sp,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (record.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "备注: ${record.notes}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(50.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getPainLevelText(level: Int): String {
    return when (level) {
        0 -> "无疼痛"
        1 -> "轻微"
        2 -> "中等"
        3 -> "严重"
        else -> "未知"
    }
}

@Composable
private fun DateDetailBottomSheet(
    state: CalendarUiState,
    onDismiss: () -> Unit,
    onAddRecord: (String) -> Unit,
    onDeleteRecord: (PoopRecord) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = LocalIsDarkTheme.current
    val selectedDate = state.selectedDate?.let { LocalDate.parse(it, dateFormatter) }
    var recordToDelete by remember { mutableStateOf<PoopRecord?>(null) }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            containerColor = if (isDark) Color(0xFF1A1C30) else Color(0xFFF0F0FA),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "删除记录",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "确定要删除这条 ${record.timeHour}:${String.format("%02d", record.timeMinute)} 的记录吗？",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                LiquidGlassButton(
                    text = "删除",
                    onClick = {
                        onDeleteRecord(record)
                        recordToDelete = null
                    },
                    tint = MaterialTheme.colorScheme.error
                )
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF1A1C30) else Color(0xFFF0F0FA),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDark) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.15f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate?.let { "${it.monthValue}月${it.dayOfMonth}日" } ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.selectedRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "暂无记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击下方按钮添加记录",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                state.selectedRecords.forEach { record ->
                    RecordDetailCard(
                        record = record,
                        onDelete = { recordToDelete = record }
                    )
                    if (record != state.selectedRecords.last()) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LiquidGlassButton(
                text = "添加记录",
                onClick = {
                    state.selectedDate?.let { onAddRecord(it) }
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
