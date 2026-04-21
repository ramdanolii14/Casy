package com.casy.music.domain.repository

import com.casy.music.domain.model.HistoryItem
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getRecentlyPlayed(limit: Int = 10): Flow<List<HistoryItem>>
    fun getRecommended(limit: Int = 20): Flow<List<HistoryItem>>
    suspend fun addToHistory(item: HistoryItem)
    suspend fun clearHistory()
}
