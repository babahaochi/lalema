package com.lalema.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalema.app.data.PoopRecord
import com.lalema.app.domain.PoopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val isTodayRecorded: Boolean = false,
    val streak: Int = 0,
    val monthCount: Int = 0,
    val monthRate: Float = 0f,
    val showRecordForm: Boolean = false,
    val todayRecords: List<PoopRecord> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PoopRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayStatus()
    }

    fun loadTodayStatus() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayStr = today.format(dateFormatter)
            val todayRecords = repository.getByDate(todayStr)
            val isRecorded = todayRecords.isNotEmpty()
            val streak = repository.getStreak()
            val monthCount = repository.getRecordCountByMonth(today.year, today.monthValue)
            val dayOfMonth = today.dayOfMonth
            val monthRate = if (dayOfMonth > 0) monthCount.toFloat() / dayOfMonth else 0f

            _uiState.value = HomeUiState(
                isTodayRecorded = isRecorded,
                streak = streak,
                monthCount = monthCount,
                monthRate = monthRate,
                todayRecords = todayRecords
            )
        }
    }

    fun recordToday(
        timeHour: Int,
        timeMinute: Int,
        amount: String,
        consistency: String,
        color: String,
        smell: String,
        painLevel: Int,
        blood: Boolean,
        mucus: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            val todayStr = LocalDate.now().format(dateFormatter)
            repository.record(
                date = todayStr,
                timeHour = timeHour,
                timeMinute = timeMinute,
                amount = amount,
                consistency = consistency,
                color = color,
                smell = smell,
                painLevel = painLevel,
                blood = blood,
                mucus = mucus,
                notes = notes
            )
            loadTodayStatus()
        }
    }

    fun showRecordForm() {
        _uiState.value = _uiState.value.copy(showRecordForm = true)
    }

    fun hideRecordForm() {
        _uiState.value = _uiState.value.copy(showRecordForm = false)
    }

    fun recordDate(
        date: String,
        timeHour: Int,
        timeMinute: Int,
        amount: String,
        consistency: String,
        color: String,
        smell: String,
        painLevel: Int,
        blood: Boolean,
        mucus: Boolean,
        notes: String
    ) {
        viewModelScope.launch {
            repository.record(
                date = date,
                timeHour = timeHour,
                timeMinute = timeMinute,
                amount = amount,
                consistency = consistency,
                color = color,
                smell = smell,
                painLevel = painLevel,
                blood = blood,
                mucus = mucus,
                notes = notes
            )
            loadTodayStatus()
        }
    }
}
