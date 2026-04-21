package com.casy.music.data.repository

import com.casy.music.data.local.dao.HistoryDao
import com.casy.music.data.local.entity.toDomain
import com.casy.music.data.local.entity.toEntity
import com.casy.music.domain.model.HistoryItem
import com.casy.music.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun getRecentlyPlayed(limit: Int): Flow<List<HistoryItem>> =
        historyDao.getRecentlyPlayed(limit).map { list -> list.map { it.toDomain() } }

    override fun getRecommended(limit: Int): Flow<List<HistoryItem>> =
        historyDao.getRecommended(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun addToHistory(item: HistoryItem) {
        historyDao.insertHistory(item.toEntity())
        historyDao.trimHistory(200)
    }

    override suspend fun clearHistory() {
        historyDao.clearHistory()
    }
}
