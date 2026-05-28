package com.lalema.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lalema.app.ai.CloudAiService
import com.lalema.app.ai.DietSuggestion
import com.lalema.app.ai.HealthAnalysisResult
import com.lalema.app.ai.LocalAiEngine
import com.lalema.app.ai.SmartReminderSuggestion
import com.lalema.app.ai.TrendPrediction
import com.lalema.app.domain.PoopRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiUiState(
    val healthResult: HealthAnalysisResult? = null,
    val trend: TrendPrediction? = null,
    val dietSuggestions: List<DietSuggestion>? = null,
    val reminderSuggestion: SmartReminderSuggestion? = null,
    val isAnalyzing: Boolean = false,
    val statusMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class AiViewModel @Inject constructor(
    private val localAi: LocalAiEngine,
    private val cloudAi: CloudAiService,
    private val repository: PoopRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState

    fun loadData() {
        viewModelScope.launch {
            val allRecords = repository.getAll()
            val localResult = localAi.analyzeHealth(allRecords)
            val localTrend = localAi.predictTrend(allRecords)
            _uiState.value = _uiState.value.copy(
                healthResult = localResult,
                trend = localTrend
            )
        }
    }

    fun analyzeHealth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, error = null, statusMessage = null)
            try {
                val allRecords = repository.getAll()
                val localResult = localAi.analyzeHealth(allRecords)
                _uiState.value = _uiState.value.copy(
                    healthResult = localResult,
                    isAnalyzing = false,
                    statusMessage = "健康分析完成"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = e.message
                )
            }
        }
    }

    fun analyzeDiet() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, error = null, statusMessage = null)
            try {
                val allRecords = repository.getAll()
                val suggestions = localAi.suggestDiet(allRecords)
                _uiState.value = _uiState.value.copy(
                    dietSuggestions = suggestions,
                    isAnalyzing = false,
                    statusMessage = "饮食建议已生成"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = e.message
                )
            }
        }
    }

    fun analyzeTrend() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, error = null, statusMessage = null)
            try {
                val allRecords = repository.getAll()
                val trend = localAi.predictTrend(allRecords)
                _uiState.value = _uiState.value.copy(
                    trend = trend,
                    isAnalyzing = false,
                    statusMessage = "趋势预测已更新"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = e.message
                )
            }
        }
    }

    fun analyzeReminder() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, error = null, statusMessage = null)
            try {
                val allRecords = repository.getAll()
                val suggestion = localAi.suggestReminderTime(allRecords)
                _uiState.value = _uiState.value.copy(
                    reminderSuggestion = suggestion,
                    isAnalyzing = false,
                    statusMessage = if (suggestion != null) "智能提醒建议已生成" else "数据不足，需要至少5条记录"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = e.message
                )
            }
        }
    }
}
