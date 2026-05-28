package com.lalema.app.domain

import com.lalema.app.data.PoopRecord
import com.lalema.app.data.PoopRecordDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
class PoopRepository(
    private val dao: PoopRecordDao
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun record(
        date: String,
        timeHour: Int = java.time.LocalTime.now().hour,
        timeMinute: Int = java.time.LocalTime.now().minute,
        amount: String = "NORMAL",
        consistency: String = "NORMAL",
        color: String = "BROWN",
        smell: String = "NORMAL",
        painLevel: Int = 0,
        blood: Boolean = false,
        mucus: Boolean = false,
        notes: String = ""
    ): Boolean {
        val result = dao.insert(
            PoopRecord.create(
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
        )
        return result != -1L
    }

    suspend fun isRecorded(date: String): Boolean {
        return dao.existsByDate(date)
    }

    suspend fun getByDate(date: String): List<PoopRecord> {
        return dao.getByDate(date)
    }

    suspend fun getByDateRange(startDate: String, endDate: String): List<PoopRecord> {
        return dao.getByDateRange(startDate, endDate)
    }

    fun getAllFlow(): Flow<List<PoopRecord>> {
        return dao.getAll()
    }

    suspend fun getAll(): List<PoopRecord> {
        return dao.getAllList()
    }

    suspend fun getCountByMonth(year: Int, month: Int): Int {
        val pattern = String.format("%04d-%02d-%%", year, month)
        return dao.getCountByMonth(pattern)
    }

    suspend fun getRecordCountByMonth(year: Int, month: Int): Int {
        val pattern = String.format("%04d-%02d-%%", year, month)
        return dao.getRecordCountByMonth(pattern)
    }

    suspend fun getStreak(): Int {
        var streak = 0
        var current = LocalDate.now()
        var daysChecked = 0
        while (daysChecked < 365) {
            val dateStr = current.format(dateFormatter)
            if (dao.existsByDate(dateStr)) {
                streak++
                current = current.minusDays(1)
                daysChecked++
            } else {
                break
            }
        }
        return streak
    }

    suspend fun deleteByDate(date: String) {
        dao.deleteByDate(date)
    }

    suspend fun deleteRecord(id: Long) {
        dao.deleteById(id)
    }
}
