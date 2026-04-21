package com.casy.music.data.local.dao

import androidx.room.*
import com.casy.music.data.local.entity.LibraryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertToLibrary(library: LibraryEntity): Long

    @Query("SELECT * FROM library ORDER BY saved_at DESC")
    fun getLibrary(): Flow<List<LibraryEntity>>

    @Query("DELETE FROM library WHERE video_id = :videoId")
    suspend fun removeFromLibrary(videoId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM library WHERE video_id = :videoId)")
    suspend fun isInLibrary(videoId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM library WHERE video_id = :videoId)")
    fun observeIsInLibrary(videoId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM library")
    fun getLibraryCount(): Flow<Int>
}
