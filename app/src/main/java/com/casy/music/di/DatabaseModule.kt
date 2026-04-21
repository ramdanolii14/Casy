package com.casy.music.di

import android.content.Context
import androidx.room.Room
import com.casy.music.core.constants.AppConstants
import com.casy.music.data.local.dao.HistoryDao
import com.casy.music.data.local.dao.LibraryDao
import com.casy.music.data.local.dao.SongDao
import com.casy.music.data.local.database.CasyDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideCasyDatabase(@ApplicationContext context: Context): CasyDatabase =
        Room.databaseBuilder(context, CasyDatabase::class.java, AppConstants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideSongDao(db: CasyDatabase): SongDao = db.songDao()

    @Provides @Singleton
    fun provideHistoryDao(db: CasyDatabase): HistoryDao = db.historyDao()

    @Provides @Singleton
    fun provideLibraryDao(db: CasyDatabase): LibraryDao = db.libraryDao()
}
