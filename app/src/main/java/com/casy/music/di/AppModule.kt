package com.casy.music.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.casy.music.data.remote.extractor.YTDL_UA  // ← import top-level constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * ExoPlayer dikonfigurasi dengan header yang IDENTIK dengan yang dikirim
     * yt-dlp saat mengambil URL, sehingga Google tidak menolak request (403).
     *
     * Header wajib untuk URL YouTube signed:
     *  - User-Agent  : harus sama persis dengan UA saat URL di-generate
     *  - Origin      : https://www.youtube.com
     *  - Referer     : https://www.youtube.com/
     *  - X-YouTube-Client-Name / Version : agar server tahu ini klien Android
     */
    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(YTDL_UA)  // ← langsung pakai top-level constant
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(20_000)
            .setDefaultRequestProperties(
                mapOf(
                    "Origin"                   to "https://www.youtube.com",
                    "Referer"                  to "https://www.youtube.com/",
                    "X-YouTube-Client-Name"    to "3",
                    "X-YouTube-Client-Version" to "20.10.38"
                )
            )

        return ExoPlayer.Builder(context)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
    }
}