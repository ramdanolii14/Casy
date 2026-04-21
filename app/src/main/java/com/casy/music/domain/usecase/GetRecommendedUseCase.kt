package com.casy.music.domain.usecase

import com.casy.music.domain.model.HistoryItem
import com.casy.music.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecommendedUseCase @Inject constructor(private val repo: HistoryRepository) {
    operator fun invoke(): Flow<List<HistoryItem>> = repo.getRecommended()
}
