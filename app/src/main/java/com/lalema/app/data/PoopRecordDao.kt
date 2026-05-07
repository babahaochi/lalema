package com.lalema.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PoopRecordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(record: PoopRecord): Long

    @Query("SELECT * FROM poop_records WHERE date = :date")
    suspend fun getByDate(date: String): PoopRecord?

    @Query("SELECT * FROM poop_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getByDateRange(startDate: String, endDate: String): List<PoopRecord>

    @Query("SELECT * FROM poop_records ORDER BY date DESC")
    fun getAll(): Flow<List<PoopRecord>>

    @Query("SELECT COUNT(*) FROM poop_records WHERE date LIKE :monthPattern")
    suspend fun getCountByMonth(monthPattern: String): Int

    @Query("DELETE FROM poop_records WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT EXISTS(SELECT 1 FROM poop_records WHERE date = :date)")
    suspend fun existsByDate(date: String): Boolean
}
