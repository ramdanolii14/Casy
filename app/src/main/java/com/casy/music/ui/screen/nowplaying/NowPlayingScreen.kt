package com.casy.music.ui.screen.nowplaying

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBackClick: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val song = uiState.currentSong

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.KeyboardArrowDown, "Back")
                    }
                },
                actions = {
                    // Tombol Queue
                    IconButton(onClick = { viewModel.toggleShowQueue() }) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Queue",
                            tint = if (uiState.showQueue) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Tombol Library
                    IconButton(onClick = { song?.let { viewModel.toggleLibrary(it) } }) {
                        Icon(
                            imageVector = if (uiState.isInLibrary) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Save to Library",
                            tint = if (uiState.isInLibrary) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (song == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) { Text("Tidak ada lagu yang diputar") }
            return@Scaffold
        }

        // Tampilan utama atau queue panel
        AnimatedContent(
            targetState = uiState.showQueue,
            transitionSpec = {
                slideInHorizontally { if (targetState) it else -it } togetherWith
                        slideOutHorizontally { if (targetState) -it else it }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "queue_toggle"
        ) { showQueue ->
            if (showQueue) {
                QueuePanel(
                    uiState = uiState,
                    onItemClick = { viewModel.playQueueIndex(it) },
                    onRemoveClick = { viewModel.removeFromQueue(it) }
                )
            } else {
                PlayerPanel(
                    uiState = uiState,
                    song = song,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeekTo = { viewModel.seekTo(it) },
                    onSkipNext = { viewModel.skipNext() },
                    onSkipPrevious = { viewModel.skipPrevious() },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeatMode() }
                )
            }
        }
    }
}

// ── Player Panel ──────────────────────────────────────────────────────────────

@Composable
private fun PlayerPanel(
    uiState: NowPlayingUiState,
    song: com.casy.music.domain.model.Song,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Album Art
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(Modifier.height(32.dp))

        // Title & Channel
        Text(
            text = song.title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = song.channelName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(28.dp))

        // Seekbar
        if (uiState.duration > 0) {
            var isDragging by remember { mutableStateOf(false) }
            var dragPosition by remember { mutableFloatStateOf(0f) }

            val sliderValue = if (isDragging) dragPosition
            else uiState.currentPosition.toFloat()

            Slider(
                value = sliderValue,
                valueRange = 0f..uiState.duration.toFloat(),
                onValueChange = { value ->
                    isDragging = true
                    dragPosition = value
                },
                onValueChangeFinished = {
                    onSeekTo(dragPosition.toLong())
                    isDragging = false
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    formatDuration(if (isDragging) dragPosition.toLong() else uiState.currentPosition),
                    style = MaterialTheme.typography.labelSmall
                )
                Text(formatDuration(uiState.duration), style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Shuffle & Repeat row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle button
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (uiState.isShuffleOn) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Repeat button
            IconButton(onClick = onCycleRepeat) {
                Icon(
                    imageVector = when (uiState.repeatMode) {
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = when (uiState.repeatMode) {
                        RepeatMode.OFF -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Playback Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSkipPrevious) {
                Icon(
                    Icons.Default.SkipPrevious, "Previous",
                    modifier = Modifier.size(36.dp)
                )
            }
            FilledIconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(64.dp),
                shape = CircleShape
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = if (uiState.isPlaying) Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            IconButton(onClick = onSkipNext) {
                Icon(
                    Icons.Default.SkipNext, "Next",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        uiState.errorMessage?.let { err ->
            Spacer(Modifier.height(12.dp))
            Text(
                err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Queue Panel ───────────────────────────────────────────────────────────────

@Composable
private fun QueuePanel(
    uiState: NowPlayingUiState,
    onItemClick: (Int) -> Unit,
    onRemoveClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Antrian (${uiState.queue.size} lagu)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }

        if (uiState.queue.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Antrian kosong.\nTekan ⋮ di lagu manapun untuk menambah.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                itemsIndexed(uiState.queue, key = { _, song -> song.videoId }) { index, song ->
                    val isCurrentTrack = index == uiState.currentQueueIndex
                    QueueItem(
                        song = song,
                        isCurrentTrack = isCurrentTrack,
                        index = index,
                        onClick = { onItemClick(index) },
                        onRemove = { onRemoveClick(index) }
                    )
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun QueueItem(
    song: com.casy.music.domain.model.Song,
    isCurrentTrack: Boolean,
    index: Int,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val bgColor = if (isCurrentTrack)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nomor / indikator aktif
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isCurrentTrack) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrentTrack) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // Thumbnail
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentTrack) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = song.channelName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Tombol hapus dari queue
        if (!isCurrentTrack) {
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Hapus dari antrian",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}