package com.lalema.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.lalema.app.ui.theme.LiquidGlassCard
import com.lalema.app.ui.theme.PrimaryLight
import com.lalema.app.ui.theme.SuccessLight
import com.lalema.app.ui.theme.WarningLight
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavHostController,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    PoopRecordForm(
        show = state.showRecordForm,
        onDismiss = { viewModel.dismissDialog() },
        onSubmit = { timeHour, timeMinute, amount, consistency, color, smell, painLevel, blood, mucus, notes ->
            viewModel.makeupRecord(timeHour, timeMinute, amount, consistency, color, smell, painLevel, blood, mucus, notes)
        }
    )

    var recordToDelete by remember { mutableStateOf<PoopRecord?>(null) }

    if (state.showDetailDialog && state.selectedRecords.isNotEmpty()) {
        val selectedDate = LocalDate.parse(state.selectedDate, dateFormatter)
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = {
                Text(
                    "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日 记录",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.selectedRecords) { record ->
                        RecordDetailCard(
                            record = record,
                            onDelete = { recordToDelete = record }
                        )
                        if (record != state.selectedRecords.last()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissDialog() }) {
                    Text("关闭", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    recordToDelete?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("删除记录") },
            text = { Text("确定要删除这条 ${record.timeHour}:${String.format("%02d", record.timeMinute)} 的记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(record.id)
                        recordToDelete = null
                    }
                ) {
                    Text("删除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("取消")
                }
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
                modifier = Modifier.height(48.dp)
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
                    .padding(16.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

                    val yearMonth = state.currentYearMonth
                    val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value % 7
                    val daysInMonth = yearMonth.lengthOfMonth()
                    val totalRawCells = firstDayOffset + daysInMonth
                    val remainder = totalRawCells % 7
                    val totalCells = if (remainder == 0) totalRawCells else totalRawCells + (7 - remainder)
                    val today = LocalDate.now()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(360.dp),
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
                                            if (isMakeupAvailable || (isToday && !isRecorded) || isRecorded)
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
    val isDark = isSystemInDarkTheme()
    val shape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.50f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
