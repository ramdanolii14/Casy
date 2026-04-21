package com.casy.music.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.casy.music.data.local.dao.HistoryDao
import com.casy.music.data.local.dao.LibraryDao
import com.casy.music.data.local.dao.SongDao
import com.casy.music.data.local.entity.HistoryEntity
import com.casy.music.data.local.entity.LibraryEntity
import com.casy.music.data.local.entity.SongEntity

@Database(
    entities = [SongEntity::class, HistoryEntity::class, LibraryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class CasyDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun historyDao(): HistoryDao
    abstract fun libraryDao(): LibraryDao
}