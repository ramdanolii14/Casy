package com.casy.music.data.local.dao

import androidx.room.*
import com.casy.music.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY played_at DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 10): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY played_at DESC LIMIT :limit")
    suspend fun getRecentlyPlayedOneShot(limit: Int = 10): List<HistoryEntity>

    @Query("SELECT *, COUNT(video_id) as play_count FROM history GROUP BY video_id ORDER BY play_count DESC, played_at DESC LIMIT :limit")
    fun getRecommended(limit: Int = 20): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE video_id = :videoId LIMIT 1")
    suspend fun getHistoryEntry(videoId: String): HistoryEntity?

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    @Query("DELETE FROM history WHERE id NOT IN (SELECT id FROM history ORDER BY played_at DESC LIMIT :maxEntries)")
    suspend fun trimHistory(maxEntries: Int = 200)
}
