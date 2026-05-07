package com.lalema.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val showAlreadyRecorded: Boolean = false
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

    private fun loadTodayStatus() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val todayStr = today.format(dateFormatter)
            val isRecorded = repository.isRecorded(todayStr)
            val streak = repository.getStreak()
            val monthCount = repository.getCountByMonth(today.year, today.monthValue)
            val dayOfMonth = today.dayOfMonth
            val monthRate = if (dayOfMonth > 0) monthCount.toFloat() / dayOfMonth else 0f

            _uiState.value = HomeUiState(
                isTodayRecorded = isRecorded,
                streak = streak,
                monthCount = monthCount,
                monthRate = monthRate
            )
        }
    }

    fun recordToday() {
        viewModelScope.launch {
            val todayStr = LocalDate.now().format(dateFormatter)
            val isRecorded = repository.isRecorded(todayStr)
            if (isRecorded) {
                _uiState.value = _uiState.value.copy(showAlreadyRecorded = true)
            } else {
                repository.record(todayStr)
                loadTodayStatus()
            }
        }
    }

    fun dismissAlreadyRecorded() {
        _uiState.value = _uiState.value.copy(showAlreadyRecorded = false)
    }

    fun recordDate(date: String) {
        viewModelScope.launch {
            repository.record(date)
            loadTodayStatus()
        }
    }
}
