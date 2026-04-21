package com.casy.music.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "library",
    indices = [Index(value = ["video_id"], unique = true)]
)
data class LibraryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "video_id") val videoId: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "channel_name") val channelName: String,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String,
    @ColumnInfo(name = "saved_at") val savedAt: Long = System.currentTimeMillis()
)
