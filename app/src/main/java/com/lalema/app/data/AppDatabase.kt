package com.lalema.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PoopRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun poopRecordDao(): PoopRecordDao
}
