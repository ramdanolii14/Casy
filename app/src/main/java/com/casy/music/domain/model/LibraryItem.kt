package com.casy.music.domain.model

data class LibraryItem(
    val id: Long = 0,
    val videoId: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val savedAt: Long = System.currentTimeMillis()
)
