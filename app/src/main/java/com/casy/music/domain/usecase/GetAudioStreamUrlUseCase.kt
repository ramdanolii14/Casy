package com.casy.music.domain.usecase

import com.casy.music.domain.repository.MusicRepository
import javax.inject.Inject

class GetAudioStreamUrlUseCase @Inject constructor(private val repo: MusicRepository) {
    suspend operator fun invoke(videoId: String, quality: String = "high"): String? =
        repo.getAudioStreamUrl(videoId, quality)
}
