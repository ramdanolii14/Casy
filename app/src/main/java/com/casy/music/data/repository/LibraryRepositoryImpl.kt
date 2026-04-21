package com.casy.music.data.repository

import com.casy.music.data.local.dao.LibraryDao
import com.casy.music.data.local.entity.toDomain
import com.casy.music.data.local.entity.toEntity
import com.casy.music.domain.model.LibraryItem
import com.casy.music.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryRepositoryImpl @Inject constructor(
    private val libraryDao: LibraryDao
) : LibraryRepository {

    override fun getLibrary(): Flow<List<LibraryItem>> =
        libraryDao.getLibrary().map { list -> list.map { it.toDomain() } }

    override suspend fun saveToLibrary(item: LibraryItem) {
        libraryDao.insertToLibrary(item.toEntity())
    }

    override suspend fun removeFromLibrary(videoId: String) {
        libraryDao.removeFromLibrary(videoId)
    }

    override suspend fun isInLibrary(videoId: String): Boolean =
        libraryDao.isInLibrary(videoId)

    override fun observeIsInLibrary(videoId: String): Flow<Boolean> =
        libraryDao.observeIsInLibrary(videoId)
}
