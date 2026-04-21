package com.casy.music.data.remote.api

import com.casy.music.data.remote.dto.SearchResponseDto
import com.casy.music.data.remote.dto.TrendingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("search")
    suspend fun searchVideos(
        @Query("q") query: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("videoCategoryId") categoryId: String = "10",
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): SearchResponseDto

    @GET("videos")
    suspend fun getTrendingMusic(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("chart") chart: String = "mostPopular",
        @Query("videoCategoryId") categoryId: String = "10",
        @Query("regionCode") region: String = "ID",
        @Query("maxResults") maxResults: Int = 20,
        @Query("key") apiKey: String
    ): TrendingResponseDto
}
