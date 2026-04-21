package com.casy.music.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.casy.music.domain.model.Song
import com.casy.music.ui.components.RowShimmer
import com.casy.music.ui.components.SongCard
import com.casy.music.ui.components.SongListItem
import com.casy.music.ui.screen.nowplaying.NowPlayingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSongClick: (Song) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Casy Music") },
                actions = {
                    IconButton(onClick = { homeViewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Trending Section
            SectionTitle("Sedang Tren")
            if (uiState.isLoadingTrending) {
                RowShimmer()
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.trendingList, key = { it.videoId }) { song ->
                        SongCard(
                            song = song,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Recently Played Section
            if (uiState.recentlyPlayed.isNotEmpty()) {
                SectionTitle("Terakhir Diputar")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.recentlyPlayed, key = { "recent_${it.videoId}" }) { item ->
                        val song = Song(item.videoId, item.title, item.channelName, item.thumbnailUrl)
                        SongCard(
                            song = song,
                            onClick = { onSongClick(song) }
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Recommended Section
            if (uiState.recommended.isNotEmpty()) {
                SectionTitle("Rekomendasi Untukmu")
                uiState.recommended.forEachIndexed { _, item ->
                    val song = Song(item.videoId, item.title, item.channelName, item.thumbnailUrl)
                    SongListItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onAddToQueue = { nowPlayingViewModel.addToQueue(song) }
                    )
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}