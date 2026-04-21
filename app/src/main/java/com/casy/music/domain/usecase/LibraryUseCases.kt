package com.casy.music.domain.usecase

import com.casy.music.domain.model.LibraryItem
import com.casy.music.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveToLibraryUseCase @Inject constructor(private val repo: LibraryRepository) {
    suspend operator fun invoke(item: LibraryItem) = repo.saveToLibrary(item)
}

class RemoveFromLibraryUseCase @Inject constructor(private val repo: LibraryRepository) {
    suspend operator fun invoke(videoId: String) = repo.removeFromLibrary(videoId)
}

class GetLibraryUseCase @Inject constructor(private val repo: LibraryRepository) {
    operator fun invoke(): Flow<List<LibraryItem>> = repo.getLibrary()
}

class ObserveIsInLibraryUseCase @Inject constructor(private val repo: LibraryRepository) {
    operator fun invoke(videoId: String): Flow<Boolean> = repo.observeIsInLibrary(videoId)
}
