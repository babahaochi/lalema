package com.lalema.app.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lalema.app.data.AppDatabase
import com.lalema.app.data.PoopRecordDao
import com.lalema.app.domain.PoopRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {

        private val requiredColumns = listOf(
            "time_hour" to "INTEGER NOT NULL DEFAULT 0",
            "time_minute" to "INTEGER NOT NULL DEFAULT 0",
            "amount" to "TEXT NOT NULL DEFAULT 'NORMAL'",
            "consistency" to "TEXT NOT NULL DEFAULT 'NORMAL'",
            "color" to "TEXT NOT NULL DEFAULT 'BROWN'",
            "smell" to "TEXT NOT NULL DEFAULT 'NORMAL'",
            "pain_level" to "INTEGER NOT NULL DEFAULT 0",
            "blood" to "INTEGER NOT NULL DEFAULT 0",
            "mucus" to "INTEGER NOT NULL DEFAULT 0",
            "notes" to "TEXT NOT NULL DEFAULT ''"
        )

        override fun migrate(database: SupportSQLiteDatabase) {
            val existingColumns = mutableSetOf<String>()
            database.query("PRAGMA table_info(poop_records)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIndex >= 0) {
                        existingColumns.add(cursor.getString(nameIndex))
                    }
                }
            }
            requiredColumns.forEach { (column, definition) ->
                if (column !in existingColumns) {
                    database.execSQL("ALTER TABLE poop_records ADD COLUMN $column $definition")
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lalema-database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun providePoopRecordDao(database: AppDatabase): PoopRecordDao {
        return database.poopRecordDao()
    }

    @Provides
    @Singleton
    fun providePoopRepository(dao: PoopRecordDao): PoopRepository {
        return PoopRepository(dao)
    }
}
