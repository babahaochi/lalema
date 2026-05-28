package com.lalema.app.ai

data class HealthAnalysisResult(
    val score: Int,
    val status: String,
    val summary: String,
    val details: List<String>,
    val suggestions: List<String>,
    val isLocal: Boolean = true
)

data class DietSuggestion(
    val category: String,
    val foods: List<String>,
    val avoidFoods: List<String>,
    val tips: String
)

data class TrendPrediction(
    val nextWeekEstimate: String,
    val confidence: Float,
    val warning: String?,
    val trendDirection: TrendDirection
)

enum class TrendDirection {
    IMPROVING, STABLE, DECLINING, UNKNOWN
}

data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SmartReminderSuggestion(
    val suggestedHour: Int,
    val suggestedMinute: Int,
    val reason: String,
    val confidence: Float
)
