package com.casy.music.domain.usecase

import com.casy.music.domain.model.Song
import com.casy.music.domain.repository.MusicRepository
import javax.inject.Inject

class GetTrendingUseCase @Inject constructor(private val repo: MusicRepository) {
    suspend operator fun invoke(): List<Song> = repo.getTrendingMusic()
}
