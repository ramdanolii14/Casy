package com.casy.music.data.repository

import com.casy.music.BuildConfig
import com.casy.music.data.local.dao.SongDao
import com.casy.music.data.local.entity.toEntity
import com.casy.music.data.remote.api.YouTubeApiService
import com.casy.music.data.remote.extractor.AudioExtractor
import com.casy.music.domain.model.Song
import com.casy.music.domain.repository.MusicRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val apiService: YouTubeApiService,
    private val audioExtractor: AudioExtractor,
    private val songDao: SongDao
) : MusicRepository {

    override suspend fun searchMusic(query: String): List<Song> {
        val response = apiService.searchVideos(
            query = query,
            apiKey = BuildConfig.YOUTUBE_API_KEY
        )
        return response.items.map { item ->
            Song(
                videoId = item.id.videoId,
                title = item.snippet.title,
                channelName = item.snippet.channelTitle,
                thumbnailUrl = item.snippet.thumbnails.getBest()
            )
        }.also { songs ->
            songDao.insertSongs(songs.map { it.toEntity() })
        }
    }

    override suspend fun getTrendingMusic(): List<Song> {
        val response = apiService.getTrendingMusic(
            apiKey = BuildConfig.YOUTUBE_API_KEY
        )
        return response.items.map { item ->
            Song(
                videoId = item.id,
                title = item.snippet.title,
                channelName = item.snippet.channelTitle,
                thumbnailUrl = item.snippet.thumbnails.getBest()
            )
        }.also { songs ->
            songDao.insertSongs(songs.map { it.toEntity() })
        }
    }

    override suspend fun getAudioStreamUrl(videoId: String, quality: String): String? {
        return audioExtractor.extractAudioUrl(videoId, quality)?.url  // ← ambil .url dari AudioStreamInfo
    }
}