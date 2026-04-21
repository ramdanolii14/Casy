package com.casy.music.domain.model

data class Song(
    val videoId: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val durationSeconds: Int = 0,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null
)
