package com.lalema.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poop_records",
    indices = [Index(value = ["date"], unique = true)]
)
data class PoopRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    companion object {
        fun create(date: String): PoopRecord {
            return PoopRecord(date = date, createdAt = System.currentTimeMillis())
        }
    }
}
