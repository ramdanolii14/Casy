package com.casy.music.ui.screen.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casy.music.domain.model.Song
import com.casy.music.ui.components.SongListItem
import com.casy.music.ui.screen.nowplaying.NowPlayingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSongClick: (Song) -> Unit,
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by libraryViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Library") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { libraryViewModel.selectTab(0) },
                    text = { Text("Tersimpan (${uiState.savedSongs.size})") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { libraryViewModel.selectTab(1) },
                    text = { Text("Diunduh (${uiState.downloadedSongs.size})") }
                )
            }

            when (uiState.selectedTab) {
                0 -> {
                    if (uiState.savedSongs.isEmpty()) {
                        EmptyState("Belum ada lagu tersimpan.")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                            items(uiState.savedSongs, key = { it.videoId }) { item ->
                                val song = Song(item.videoId, item.title, item.channelName, item.thumbnailUrl)
                                SongListItem(
                                    song = song,
                                    onClick = { onSongClick(song) },
                                    onAddToQueue = { nowPlayingViewModel.addToQueue(song) },
                                    onRemoveClick = { libraryViewModel.removeFromLibrary(song.videoId) }
                                )
                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
                1 -> {
                    if (uiState.downloadedSongs.isEmpty()) {
                        EmptyState("Belum ada lagu yang diunduh.")
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                            items(uiState.downloadedSongs, key = { it.videoId }) { song ->
                                SongListItem(
                                    song = song,
                                    onClick = { onSongClick(song) },
                                    onAddToQueue = { nowPlayingViewModel.addToQueue(song) }
                                )
                                HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}