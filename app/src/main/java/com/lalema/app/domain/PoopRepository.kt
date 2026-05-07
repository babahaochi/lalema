package com.lalema.app.domain

import com.lalema.app.data.PoopRecord
import com.lalema.app.data.PoopRecordDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoopRepository @Inject constructor(
    private val dao: PoopRecordDao
) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun record(date: String): Boolean {
        val result = dao.insert(PoopRecord.create(date))
        return result != -1L
    }

    suspend fun isRecorded(date: String): Boolean {
        return dao.existsByDate(date)
    }

    suspend fun getByDateRange(startDate: String, endDate: String): List<PoopRecord> {
        return dao.getByDateRange(startDate, endDate)
    }

    fun getAllFlow(): Flow<List<PoopRecord>> {
        return dao.getAll()
    }

    suspend fun getCountByMonth(year: Int, month: Int): Int {
        val pattern = String.format("%04d-%02d-%%", year, month)
        return dao.getCountByMonth(pattern)
    }

    suspend fun getStreak(): Int {
        var streak = 0
        var current = LocalDate.now()
        while (true) {
            val dateStr = current.format(dateFormatter)
            if (dao.existsByDate(dateStr)) {
                streak++
                current = current.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    suspend fun deleteByDate(date: String) {
        dao.deleteByDate(date)
    }
}
