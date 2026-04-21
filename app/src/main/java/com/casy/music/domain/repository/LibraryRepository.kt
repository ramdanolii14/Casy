package com.casy.music.domain.repository

import com.casy.music.domain.model.LibraryItem
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun getLibrary(): Flow<List<LibraryItem>>
    suspend fun saveToLibrary(item: LibraryItem)
    suspend fun removeFromLibrary(videoId: String)
    suspend fun isInLibrary(videoId: String): Boolean
    fun observeIsInLibrary(videoId: String): Flow<Boolean>
}
