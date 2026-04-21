package com.casy.music.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casy.music.core.constants.AppConstants
import com.casy.music.domain.model.Song
import com.casy.music.domain.usecase.SearchMusicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isIdle: Boolean = true
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchMusic: SearchMusicUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")
    private var searchJob: Job? = null

    init {
        queryFlow
            .debounce(AppConstants.SEARCH_DEBOUNCE_MS)
            .filter { it.isNotBlank() }
            .distinctUntilChanged()
            .onEach { performSearch(it) }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, isIdle = query.isBlank()) }
        queryFlow.value = query
        if (query.isBlank()) {
            // BUG FIX: Batalkan job pencarian yang sedang berjalan saat query dikosongkan,
            // agar hasil lama tidak muncul kembali setelah clear.
            searchJob?.cancel()
            _uiState.update { it.copy(results = emptyList(), isLoading = false, errorMessage = null) }
        }
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isIdle = false) }
            try {
                val results = searchMusic(query)
                _uiState.update { it.copy(results = results, isLoading = false) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e // biarkan coroutine cancel berjalan normal
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun clearQuery() {
        // BUG FIX: queryFlow juga harus di-reset, sebelumnya hanya _uiState yang di-reset
        // sehingga debounce bisa terpicu lagi dengan query lama setelah clear.
        searchJob?.cancel()
        queryFlow.value = ""
        _uiState.update { SearchUiState() }
    }
}