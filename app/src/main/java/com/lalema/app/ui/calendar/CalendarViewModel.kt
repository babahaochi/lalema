package com.lalema.app.ui.calendar

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
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CalendarUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val recordedDates: Set<String> = emptySet(),
    val selectedDate: String? = null,
    val showMakeupDialog: Boolean = false,
    val showDetailDialog: Boolean = false,
    val showRecordForm: Boolean = false,
    val selectedRecords: List<PoopRecord> = emptyList()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: PoopRepository
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadMonth(YearMonth.now())
    }

    fun loadMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentYearMonth = yearMonth)
            refreshMonthData()
        }
    }

    fun previousMonth() {
        loadMonth(_uiState.value.currentYearMonth.minusMonths(1))
    }

    fun nextMonth() {
        loadMonth(_uiState.value.currentYearMonth.plusMonths(1))
    }

    fun onDateClick(date: String) {
        val today = LocalDate.now()
        val clickedDate = LocalDate.parse(date, dateFormatter)
        val isFuture = clickedDate.isAfter(today)

        if (isFuture) return

        viewModelScope.launch {
            val records = repository.getByDate(date)
            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                selectedRecords = records,
                showDetailDialog = true
            )
        }
    }

    fun makeupRecord(
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
                date = _uiState.value.selectedDate!!,
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
            refreshMonthData()
            _uiState.value = _uiState.value.copy(
                showRecordForm = false,
                selectedDate = null
            )
        }
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteRecord(id)
            refreshMonthData()
            val date = _uiState.value.selectedDate
            if (date != null) {
                val records = repository.getByDate(date)
                _uiState.value = _uiState.value.copy(selectedRecords = records)
            }
        }
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(
            showMakeupDialog = false,
            showDetailDialog = false,
            showRecordForm = false,
            selectedDate = null,
            selectedRecords = emptyList()
        )
    }

    fun showRecordFormForDate(date: String) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            showDetailDialog = false,
            showRecordForm = true
        )
    }

    private suspend fun refreshMonthData() {
        val yearMonth = _uiState.value.currentYearMonth
        val startDate = yearMonth.atDay(1).format(dateFormatter)
        val endDate = yearMonth.atEndOfMonth().format(dateFormatter)
        val records = repository.getByDateRange(startDate, endDate)
        val dates = records.map { it.date }.toSet()
        _uiState.value = _uiState.value.copy(recordedDates = dates)
    }

}
