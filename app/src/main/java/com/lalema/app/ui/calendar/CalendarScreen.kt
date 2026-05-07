package com.lalema.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.lalema.app.ui.theme.Brown500
import com.lalema.app.ui.theme.WarmOrange500
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("日历") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "上个月")
                }
                Text(
                    text = "${state.currentYearMonth.year}年${state.currentYearMonth.monthValue}月",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "下个月")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("日", "一", "二", "三", "四", "五", "六").forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val yearMonth = state.currentYearMonth
            val firstDayOffset = yearMonth.atDay(1).dayOfWeek.value % 7
            val daysInMonth = yearMonth.lengthOfMonth()
            val totalRawCells = firstDayOffset + daysInMonth
            val remainder = totalRawCells % 7
            val totalCells = if (remainder == 0) totalRawCells else totalRawCells + (7 - remainder)
            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
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
                                    if (isMakeupAvailable || (isToday && !isRecorded))
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
                                        .background(Color(0xFF4CAF50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                isToday -> Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Brown500, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                isFuture -> Text(
                                    text = day.toString(),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                isMakeupAvailable -> Text(
                                    text = day.toString(),
                                    color = WarmOrange500,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                else -> Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showMakeupDialog && state.selectedDate != null) {
        val selectedDate = LocalDate.parse(state.selectedDate, dateFormatter)
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text("补打卡") },
            text = { Text("要补打 ${selectedDate.monthValue}月${selectedDate.dayOfMonth}日 的卡吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.makeupRecord(state.selectedDate!!) }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}
