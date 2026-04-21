package com.casy.music.data.local.dao

import androidx.room.*
import com.casy.music.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Query("SELECT * FROM songs WHERE video_id = :videoId")
    suspend fun getSongById(videoId: String): SongEntity?

    @Query("SELECT * FROM songs WHERE is_downloaded = 1")
    fun getDownloadedSongs(): Flow<List<SongEntity>>

    @Query("UPDATE songs SET is_downloaded = 1, local_file_path = :filePath WHERE video_id = :videoId")
    suspend fun markAsDownloaded(videoId: String, filePath: String)

    @Query("UPDATE songs SET is_downloaded = 0, local_file_path = NULL WHERE video_id = :videoId")
    suspend fun markAsNotDownloaded(videoId: String)

    @Query("DELETE FROM songs WHERE video_id = :videoId")
    suspend fun deleteSong(videoId: String)
}
