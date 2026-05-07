package com.lalema.app.data.di

import android.content.Context
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "lalema-database"
        ).build()
    }

    @Provides
    fun providePoopRecordDao(database: AppDatabase): PoopRecordDao {
        return database.poopRecordDao()
    }
}
