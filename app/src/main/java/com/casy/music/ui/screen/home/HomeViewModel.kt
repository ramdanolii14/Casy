package com.casy.music.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casy.music.domain.model.HistoryItem
import com.casy.music.domain.model.Song
import com.casy.music.domain.usecase.GetRecentlyPlayedUseCase
import com.casy.music.domain.usecase.GetRecommendedUseCase
import com.casy.music.domain.usecase.GetTrendingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val trendingList: List<Song> = emptyList(),
    val recentlyPlayed: List<HistoryItem> = emptyList(),
    val recommended: List<HistoryItem> = emptyList(),
    val isLoadingTrending: Boolean = false,
    val isLoadingRecent: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTrending: GetTrendingUseCase,
    private val getRecentlyPlayed: GetRecentlyPlayedUseCase,
    private val getRecommended: GetRecommendedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTrending()
        observeLocalData()
    }

    private fun loadTrending() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrending = true, errorMessage = null) }
            try {
                // Deduplikasi trending berdasarkan videoId
                val trending = getTrending().distinctBy { it.videoId }
                _uiState.update { it.copy(trendingList = trending, isLoadingTrending = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingTrending = false, errorMessage = e.message) }
            }
        }
    }

    private fun observeLocalData() {
        // Recently Played — deduplikasi berdasarkan videoId sebelum update UI
        getRecentlyPlayed()
            .onEach { list ->
                _uiState.update { it.copy(recentlyPlayed = list.distinctBy { item -> item.videoId }) }
            }
            .launchIn(viewModelScope)

        // Recommended — deduplikasi berdasarkan videoId sebelum update UI
        getRecommended()
            .onEach { list ->
                _uiState.update { it.copy(recommended = list.distinctBy { item -> item.videoId }) }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() = loadTrending()
}