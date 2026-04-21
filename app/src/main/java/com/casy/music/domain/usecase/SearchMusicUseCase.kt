package com.casy.music.domain.usecase

import com.casy.music.domain.model.Song
import com.casy.music.domain.repository.MusicRepository
import javax.inject.Inject

class SearchMusicUseCase @Inject constructor(private val repo: MusicRepository) {
    suspend operator fun invoke(query: String): List<Song> {
        if (query.isBlank()) return emptyList()
        return repo.searchMusic(query)
    }
}
