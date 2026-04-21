package com.casy.music.ui.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casy.music.data.local.dao.SongDao
import com.casy.music.data.local.entity.toDomain
import com.casy.music.domain.model.LibraryItem
import com.casy.music.domain.model.Song
import com.casy.music.domain.usecase.GetLibraryUseCase
import com.casy.music.domain.usecase.RemoveFromLibraryUseCase
import com.casy.music.service.DownloadProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val savedSongs: List<LibraryItem> = emptyList(),
    val downloadedSongs: List<Song> = emptyList(),
    val selectedTab: Int = 0 // 0 = Tersimpan, 1 = Diunduh
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getLibrary: GetLibraryUseCase,
    private val removeFromLibrary: RemoveFromLibraryUseCase,
    private val songDao: SongDao,
    private val downloadProvider: DownloadProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        // Observe library dari Room
        getLibrary()
            .onEach { list -> _uiState.update { it.copy(savedSongs = list) } }
            .launchIn(viewModelScope)

        // Observe downloaded songs dari Room + file system
        songDao.getDownloadedSongs()
            .onEach { entities ->
                _uiState.update { it.copy(downloadedSongs = entities.map { e -> e.toDomain() }) }
            }
            .launchIn(viewModelScope)
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun removeFromLibrary(videoId: String) {
        viewModelScope.launch { removeFromLibrary.invoke(videoId) }
    }
}
