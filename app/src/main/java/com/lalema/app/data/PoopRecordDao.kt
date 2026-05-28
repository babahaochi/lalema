package com.lalema.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PoopRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PoopRecord): Long

    @Query("SELECT * FROM poop_records WHERE date = :date ORDER BY time_hour DESC, time_minute DESC")
    suspend fun getByDate(date: String): List<PoopRecord>

    @Query("SELECT * FROM poop_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC, time_hour DESC, time_minute DESC")
    suspend fun getByDateRange(startDate: String, endDate: String): List<PoopRecord>

    @Query("SELECT * FROM poop_records ORDER BY date DESC, time_hour DESC, time_minute DESC")
    fun getAll(): Flow<List<PoopRecord>>

    @Query("SELECT * FROM poop_records ORDER BY date DESC, time_hour DESC, time_minute DESC")
    suspend fun getAllList(): List<PoopRecord>

    @Query("SELECT COUNT(DISTINCT date) FROM poop_records WHERE date LIKE :monthPattern")
    suspend fun getCountByMonth(monthPattern: String): Int

    @Query("SELECT COUNT(*) FROM poop_records WHERE date LIKE :monthPattern")
    suspend fun getRecordCountByMonth(monthPattern: String): Int

    @Query("DELETE FROM poop_records WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM poop_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM poop_records WHERE date = :date)")
    suspend fun existsByDate(date: String): Boolean
}
