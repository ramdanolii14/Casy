package com.casy.music.data.local.entity

import com.casy.music.domain.model.HistoryItem
import com.casy.music.domain.model.LibraryItem
import com.casy.music.domain.model.Song

fun SongEntity.toDomain() = Song(videoId, title, channelName, thumbnailUrl, durationSeconds, isDownloaded, localFilePath)
fun Song.toEntity() = SongEntity(videoId, title, channelName, thumbnailUrl, durationSeconds, isDownloaded, localFilePath)

fun HistoryEntity.toDomain() = HistoryItem(id, videoId, title, channelName, thumbnailUrl, playedAt)
fun HistoryItem.toEntity() = HistoryEntity(id, videoId, title, channelName, thumbnailUrl, playedAt)
fun HistoryItem.toSong() = Song(videoId, title, channelName, thumbnailUrl)

fun LibraryEntity.toDomain() = LibraryItem(id, videoId, title, channelName, thumbnailUrl, savedAt)
fun LibraryItem.toEntity() = LibraryEntity(id, videoId, title, channelName, thumbnailUrl, savedAt)
fun LibraryItem.toSong() = Song(videoId, title, channelName, thumbnailUrl)
