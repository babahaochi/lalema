package com.lalema.app.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lalema.app.data.AppDatabase
import com.lalema.app.data.PoopRecordDao
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
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE poop_records ADD COLUMN count INTEGER NOT NULL DEFAULT 1")
            database.execSQL("ALTER TABLE poop_records ADD COLUMN amount TEXT NOT NULL DEFAULT 'NORMAL'")
            database.execSQL("ALTER TABLE poop_records ADD COLUMN consistency TEXT NOT NULL DEFAULT 'NORMAL'")
            database.execSQL("ALTER TABLE poop_records ADD COLUMN color TEXT NOT NULL DEFAULT 'BROWN'")
            database.execSQL("ALTER TABLE poop_records ADD COLUMN timeOfDay TEXT NOT NULL DEFAULT 'MORNING'")
            database.execSQL("ALTER TABLE poop_records ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
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
}
