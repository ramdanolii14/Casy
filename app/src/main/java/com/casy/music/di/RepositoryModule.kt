package com.casy.music.di

import com.casy.music.data.repository.HistoryRepositoryImpl
import com.casy.music.data.repository.LibraryRepositoryImpl
import com.casy.music.data.repository.MusicRepositoryImpl
import com.casy.music.domain.repository.HistoryRepository
import com.casy.music.domain.repository.LibraryRepository
import com.casy.music.domain.repository.MusicRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository

    @Binds @Singleton
    abstract fun bindLibraryRepository(impl: LibraryRepositoryImpl): LibraryRepository

    @Binds @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
}
