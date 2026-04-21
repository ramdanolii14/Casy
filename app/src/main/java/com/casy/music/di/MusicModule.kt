package com.casy.music.di

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MusicModule {

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "media_cache")
        val databaseProvider = StandaloneDatabaseProvider(context)
        // Limit cache 200MB untuk streaming harian (otomatis dihapus jika penuh)
        return SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(200 * 1024 * 1024), databaseProvider)
    }
}