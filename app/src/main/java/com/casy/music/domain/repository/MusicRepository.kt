package com.casy.music.domain.repository

import com.casy.music.domain.model.Song

interface MusicRepository {
    suspend fun searchMusic(query: String): List<Song>
    suspend fun getTrendingMusic(): List<Song>
    suspend fun getAudioStreamUrl(videoId: String, quality: String): String?
}
