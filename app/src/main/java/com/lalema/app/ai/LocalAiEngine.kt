package com.lalema.app.ai

import com.lalema.app.data.PoopConsistency
import com.lalema.app.data.PoopRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class LocalAiEngine @Inject constructor() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun analyzeHealth(records: List<PoopRecord>): HealthAnalysisResult {
        if (records.isEmpty()) {
            return HealthAnalysisResult(
                score = 0,
                status = "暂无数据",
                summary = "还没有记录任何排便数据，开始记录以获得健康分析。",
                details = emptyList(),
                suggestions = listOf("建议每天记录排便情况，持续一周后可获得基础分析。"),
                isLocal = true
            )
        }

        val recentRecords = records.filter {
            val recordDate = LocalDate.parse(it.date, dateFormatter)
            ChronoUnit.DAYS.between(recordDate, LocalDate.now()) <= 30
        }

        val score = calculateHealthScore(recentRecords)
        val status = getHealthStatus(score)
        val details = generateDetails(recentRecords)
        val suggestions = generateSuggestions(recentRecords, score)
        val summary = generateSummary(recentRecords, score, status)

        return HealthAnalysisResult(
            score = score,
            status = status,
            summary = summary,
            details = details,
            suggestions = suggestions,
            isLocal = true
        )
    }

    fun suggestDiet(records: List<PoopRecord>): List<DietSuggestion> {
        if (records.isEmpty()) return emptyList()

        val recent = records.filter {
            val recordDate = LocalDate.parse(it.date, dateFormatter)
            ChronoUnit.DAYS.between(recordDate, LocalDate.now()) <= 14
        }

        val avgConsistency = recent.map { consistencyToValue(it.consistency) }.average()
        val hasConstipation = recent.any { it.consistency == "HARD" || it.consistency == "VERY_HARD" }
        val hasDiarrhea = recent.any { it.consistency == "LIQUID" || it.consistency == "VERY_SOFT" }

        val suggestions = mutableListOf<DietSuggestion>()

        if (hasConstipation || avgConsistency < 2) {
            suggestions.add(DietSuggestion(
                category = "缓解便秘",
                foods = listOf("燕麦", "红薯", "香蕉", "酸奶", "西兰花", "奇亚籽", "火龙果"),
                avoidFoods = listOf("精制白米", "油炸食品", "过量奶酪", "未熟香蕉"),
                tips = "每日饮水量建议达到2000ml以上，膳食纤维摄入25-30g"
            ))
        }

        if (hasDiarrhea || avgConsistency > 4) {
            suggestions.add(DietSuggestion(
                category = "缓解腹泻",
                foods = listOf("白粥", "蒸苹果", "山药", "胡萝卜", "苏打饼干"),
                avoidFoods = listOf("辛辣食物", "生冷食物", "高纤维食物", "乳制品"),
                tips = "暂时减少纤维摄入，补充电解质，症状持续超过2天请就医"
            ))
        }

        if (suggestions.isEmpty()) {
            suggestions.add(DietSuggestion(
                category = "维持肠道健康",
                foods = listOf("全谷物", "深色蔬菜", "发酵食品", "坚果", "豆类"),
                avoidFoods = listOf("过量加工食品", "过量红肉", "过量糖分"),
                tips = "保持饮食多样化，规律进餐，适量运动"
            ))
        }

        return suggestions
    }

    fun predictTrend(records: List<PoopRecord>): TrendPrediction {
        if (records.size < 7) {
            return TrendPrediction(
                nextWeekEstimate = "数据不足，需要至少7天记录",
                confidence = 0f,
                warning = null,
                trendDirection = TrendDirection.UNKNOWN
            )
        }

        val sorted = records.sortedBy { LocalDate.parse(it.date, dateFormatter) }
        val recent7 = sorted.takeLast(7)
        val previous7 = sorted.dropLast(7).takeLast(7)

        val recentDates = recent7.map { it.date }.distinct().size
        val prevDates = previous7.map { it.date }.distinct().size

        val direction = when {
            recentDates > prevDates -> TrendDirection.IMPROVING
            recentDates < prevDates -> TrendDirection.DECLINING
            else -> TrendDirection.STABLE
        }

        val avgConsistency = recent7.map { consistencyToValue(it.consistency) }.average()
        val warning = when {
            recentDates == 0 -> "最近7天没有排便记录，请关注"
            avgConsistency < 1.5 -> "近期排便偏硬，注意补水和纤维摄入"
            avgConsistency > 4.5 -> "近期排便偏稀，注意饮食卫生"
            else -> null
        }

        return TrendPrediction(
            nextWeekEstimate = "预计下周排便${recentDates}次左右",
            confidence = 0.6f,
            warning = warning,
            trendDirection = direction
        )
    }

    fun suggestReminderTime(records: List<PoopRecord>): SmartReminderSuggestion? {
        if (records.size < 5) return null

        val hourCounts = mutableMapOf<Int, Int>()
        records.forEach {
            hourCounts[it.timeHour] = (hourCounts[it.timeHour] ?: 0) + 1
        }

        val bestHour = hourCounts.maxByOrNull { it.value }?.key ?: 8
        val confidence = (hourCounts[bestHour]?.toFloat() ?: 0f) / records.size

        return SmartReminderSuggestion(
            suggestedHour = bestHour,
            suggestedMinute = 0,
            reason = "根据您的历史记录，${bestHour}:00是您最常排便的时间",
            confidence = confidence
        )
    }

    private fun consistencyToValue(consistency: String): Int {
        return when (consistency) {
            "VERY_HARD" -> 1
            "HARD" -> 2
            "NORMAL" -> 3
            "SOFT" -> 4
            "VERY_SOFT" -> 5
            "LIQUID" -> 6
            else -> 3
        }
    }

    private fun calculateHealthScore(records: List<PoopRecord>): Int {
        if (records.isEmpty()) return 0

        val distinctDays = records.map { it.date }.distinct().size
        val frequencyScore = when (distinctDays) {
            in 4..21 -> 30
            in 2..3 -> 20
            in 22..30 -> 25
            else -> 15
        }

        val consistencyValues = records.map { consistencyToValue(it.consistency) }
        val avgConsistency = if (consistencyValues.isNotEmpty()) consistencyValues.average() else 3.0
        val consistencyScore = when (avgConsistency) {
            in 2.5..3.5 -> 35
            in 2.0..4.0 -> 25
            else -> 15
        }

        val regularityScore = calculateRegularity(records)

        return (frequencyScore + consistencyScore + regularityScore).coerceIn(0, 100)
    }

    private fun calculateRegularity(records: List<PoopRecord>): Int {
        if (records.size < 3) return 0
        val dates = records.map { LocalDate.parse(it.date, dateFormatter) }.sorted()
        val intervals = mutableListOf<Long>()
        for (i in 1 until dates.size) {
            intervals.add(ChronoUnit.DAYS.between(dates[i - 1], dates[i]))
        }
        if (intervals.isEmpty()) return 0
        val avgInterval = intervals.average()
        val variance = intervals.map { (it - avgInterval) * (it - avgInterval) }.average()
        val stdDev = kotlin.math.sqrt(variance)

        return when {
            stdDev < 0.5 -> 35
            stdDev < 1.5 -> 30
            stdDev < 3.0 -> 20
            else -> 10
        }
    }

    private fun getHealthStatus(score: Int): String = when {
        score >= 80 -> "优秀"
        score >= 60 -> "良好"
        score >= 40 -> "一般"
        score >= 20 -> "需关注"
        else -> "数据不足"
    }

    private fun generateDetails(records: List<PoopRecord>): List<String> {
        val details = mutableListOf<String>()
        val days = records.map { it.date }.distinct().size
        details.add("近30天记录天数: $days 天")

        val consistencyValues = records.map { consistencyToValue(it.consistency) }
        val avgConsistency = if (consistencyValues.isNotEmpty()) consistencyValues.average() else 0.0
        details.add("平均便便形态: ${String.format("%.1f", avgConsistency)} 级 (Bristol标准)")

        val avgTime = records.map { it.timeHour * 60 + it.timeMinute }.average()
        val avgHour = (avgTime / 60).toInt()
        val avgMinute = (avgTime % 60).toInt()
        details.add("平均排便时间: ${String.format("%02d:%02d", avgHour, avgMinute)}")

        return details
    }

    private fun generateSuggestions(records: List<PoopRecord>, score: Int): List<String> {
        val suggestions = mutableListOf<String>()

        if (score < 40) {
            suggestions.add("排便规律性较差，建议固定时间如厕培养习惯")
        }

        val hardCount = records.count { it.consistency == "HARD" || it.consistency == "VERY_HARD" }
        if (hardCount > records.size * 0.3) {
            suggestions.add("便便偏硬的情况较多，注意增加饮水和膳食纤维")
        }

        val wateryCount = records.count { it.consistency == "LIQUID" || it.consistency == "VERY_SOFT" }
        if (wateryCount > records.size * 0.3) {
            suggestions.add("便便偏稀的情况较多，注意饮食卫生和肠道调理")
        }

        if (suggestions.isEmpty()) {
            suggestions.add("保持当前良好的排便习惯，继续记录以获取更精准分析")
        }

        return suggestions
    }

    private fun generateSummary(records: List<PoopRecord>, score: Int, status: String): String {
        return "根据近${records.size}条记录分析，您的肠道健康评分为${score}分，整体状态$status。"
    }
}
