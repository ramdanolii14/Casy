package com.casy.music.ui.screen.nowplaying

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.casy.music.data.remote.extractor.AudioExtractor
import com.casy.music.datastore.UserPreferences
import com.casy.music.datastore.UserPreferencesDataStore
import com.casy.music.domain.model.HistoryItem
import com.casy.music.domain.model.LibraryItem
import com.casy.music.domain.model.Song
import com.casy.music.domain.usecase.GetAudioStreamUrlUseCase
import com.casy.music.domain.usecase.ObserveIsInLibraryUseCase
import com.casy.music.domain.usecase.RemoveFromLibraryUseCase
import com.casy.music.domain.usecase.SaveToLibraryUseCase
import com.casy.music.domain.repository.HistoryRepository
import com.casy.music.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RepeatMode { OFF, ONE, ALL }

data class NowPlayingUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isLoading: Boolean = false,
    val isInLibrary: Boolean = false,
    val errorMessage: String? = null,
    // Queue
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val isShuffleOn: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val showQueue: Boolean = false
)

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAudioStreamUrl: GetAudioStreamUrlUseCase,
    private val audioExtractor: AudioExtractor,
    private val saveToLibrary: SaveToLibraryUseCase,
    private val removeFromLibrary: RemoveFromLibraryUseCase,
    private val historyRepository: HistoryRepository,
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val observeIsInLibraryUseCase: ObserveIsInLibraryUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "NowPlayingViewModel"
    }

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    val themeState: StateFlow<UserPreferences> = userPreferencesDataStore.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null

    private var libraryObserverJob: Job? = null
    private var positionPollingJob: Job? = null

    // BUG FIX: Job khusus untuk proses fetch URL audio.
    // Setiap kali playSong() dipanggil, job lama dibatalkan terlebih dahulu
    // sehingga tidak ada race condition antara beberapa request URL paralel.
    private var playJob: Job? = null

    // Simpan lagu yang sedang coba diputar untuk keperluan retry setelah 403
    private var pendingSong: Song? = null

    // Shuffle order: index asli queue yang sudah diacak
    private var shuffledIndices: List<Int> = emptyList()

    init {
        initMediaController()
    }

    // ── MediaController ───────────────────────────────────────────────────────

    private fun initMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(playerListener)
                Log.d(TAG, "MediaController terhubung")
            } catch (e: Exception) {
                Log.e(TAG, "Gagal mendapat MediaController: ${e.message}")
                _uiState.update { it.copy(errorMessage = "Gagal terhubung ke service musik") }
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) startPositionPolling() else stopPositionPolling()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _uiState.update {
                        it.copy(
                            duration = mediaController?.duration ?: 0L,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                Player.STATE_BUFFERING -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                Player.STATE_ENDED -> {
                    stopPositionPolling()
                    _uiState.update { it.copy(isPlaying = false, isLoading = false) }
                    // Auto-advance saat lagu selesai
                    handleTrackEnded()
                }
                Player.STATE_IDLE -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "ExoPlayer error: ${error.errorCodeName} — ${error.message}")

            val is403 = error.message?.contains("403") == true ||
                    error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS

            if (is403) {
                Log.w(TAG, "403 terdeteksi — mencoba ulang dengan client lain...")
                pendingSong?.let { song ->
                    retryWithFreshUrl(song)
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Akses audio ditolak (403). Coba lagi."
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error pemutaran: ${error.errorCodeName}"
                    )
                }
            }
        }
    }

    // ── Track ended handler ───────────────────────────────────────────────────

    private fun handleTrackEnded() {
        val state = _uiState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                // Putar ulang lagu yang sama
                state.currentSong?.let { playSongInternal(it, addToQueue = false) }
            }
            RepeatMode.ALL -> {
                skipNext()
            }
            RepeatMode.OFF -> {
                val hasNext = hasNextTrack()
                if (hasNext) skipNext()
                // kalau tidak ada next, berhenti (sudah ditangani STATE_ENDED)
            }
        }
    }

    // ── Position polling ──────────────────────────────────────────────────────

    private fun startPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = viewModelScope.launch {
            while (true) {
                val pos = mediaController?.currentPosition ?: 0L
                _uiState.update { it.copy(currentPosition = pos) }
                delay(500L)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
    }

    // ── Queue management ──────────────────────────────────────────────────────

    /**
     * Mulai putar lagu dan set sebagai antrian baru.
     * Biasanya dipanggil dari HomeScreen / SearchScreen.
     */
    fun playSong(song: Song) {
        val currentQueue = _uiState.value.queue
        // Cek apakah lagu sudah ada di queue
        val existingIndex = currentQueue.indexOfFirst { it.videoId == song.videoId }
        if (existingIndex >= 0) {
            // Langsung loncat ke lagu tersebut
            playQueueIndex(existingIndex)
            return
        }
        // Tidak ada di queue → reset queue dengan lagu ini saja
        _uiState.update {
            it.copy(
                queue = listOf(song),
                currentQueueIndex = 0,
                isShuffleOn = false
            )
        }
        shuffledIndices = listOf(0)
        playSongInternal(song, addToQueue = false)
    }

    /**
     * Tambahkan lagu ke akhir antrian tanpa mengganggu pemutaran saat ini.
     */
    fun addToQueue(song: Song) {
        val current = _uiState.value
        if (current.queue.any { it.videoId == song.videoId }) return // sudah ada
        val newQueue = current.queue + song
        _uiState.update { it.copy(queue = newQueue) }
        rebuildShuffleIndices(newQueue.size, current.currentQueueIndex)
        Log.d(TAG, "Ditambahkan ke queue: ${song.title}")
    }

    /**
     * Putar lagu berikutnya dari antrian, mempertimbangkan shuffle.
     * BUG FIX: Hapus keyword 'override' — ViewModel tidak memiliki method ini.
     */
    fun skipNext() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return

        val nextIndex = getNextIndex(state)
        if (nextIndex < 0) return

        playQueueIndex(nextIndex)
    }

    /**
     * Putar lagu sebelumnya dari antrian.
     * BUG FIX: Hapus keyword 'override' — ViewModel tidak memiliki method ini.
     */
    fun skipPrevious() {
        val state = _uiState.value
        if (state.queue.isEmpty()) return

        // Jika sudah > 3 detik, restart lagu saat ini
        if ((mediaController?.currentPosition ?: 0L) > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = getPrevIndex(state)
        if (prevIndex < 0) return
        playQueueIndex(prevIndex)
    }

    private fun getNextIndex(state: NowPlayingUiState): Int {
        val queue = state.queue
        if (queue.isEmpty()) return -1

        return if (state.isShuffleOn) {
            val currentShufflePos = shuffledIndices.indexOf(state.currentQueueIndex)
            val nextShufflePos = (currentShufflePos + 1) % shuffledIndices.size
            shuffledIndices[nextShufflePos]
        } else {
            val next = state.currentQueueIndex + 1
            when {
                next < queue.size -> next
                state.repeatMode == RepeatMode.ALL -> 0
                else -> -1
            }
        }
    }

    private fun getPrevIndex(state: NowPlayingUiState): Int {
        val queue = state.queue
        if (queue.isEmpty()) return -1

        return if (state.isShuffleOn) {
            val currentShufflePos = shuffledIndices.indexOf(state.currentQueueIndex)
            val prevShufflePos = if (currentShufflePos > 0) currentShufflePos - 1
            else shuffledIndices.size - 1
            shuffledIndices[prevShufflePos]
        } else {
            val prev = state.currentQueueIndex - 1
            when {
                prev >= 0 -> prev
                state.repeatMode == RepeatMode.ALL -> queue.size - 1
                else -> -1
            }
        }
    }

    private fun hasNextTrack(): Boolean {
        val state = _uiState.value
        return getNextIndex(state) >= 0
    }

    fun playQueueIndex(index: Int) {
        val queue = _uiState.value.queue
        if (index < 0 || index >= queue.size) return
        _uiState.update { it.copy(currentQueueIndex = index) }
        playSongInternal(queue[index], addToQueue = false)
    }

    fun removeFromQueue(index: Int) {
        val state = _uiState.value
        if (index < 0 || index >= state.queue.size) return
        val newQueue = state.queue.toMutableList().also { it.removeAt(index) }
        val newCurrentIndex = when {
            index < state.currentQueueIndex -> state.currentQueueIndex - 1
            index == state.currentQueueIndex -> minOf(state.currentQueueIndex, newQueue.size - 1)
            else -> state.currentQueueIndex
        }
        _uiState.update { it.copy(queue = newQueue, currentQueueIndex = newCurrentIndex) }
        rebuildShuffleIndices(newQueue.size, newCurrentIndex)
    }

    fun moveQueueItem(from: Int, to: Int) {
        val queue = _uiState.value.queue.toMutableList()
        if (from < 0 || to < 0 || from >= queue.size || to >= queue.size) return
        val item = queue.removeAt(from)
        queue.add(to, item)
        val newCurrentIndex = when (_uiState.value.currentQueueIndex) {
            from -> to
            in (minOf(from, to) + 1)..maxOf(from, to) -> {
                if (from < to) _uiState.value.currentQueueIndex - 1
                else _uiState.value.currentQueueIndex + 1
            }
            else -> _uiState.value.currentQueueIndex
        }
        _uiState.update { it.copy(queue = queue, currentQueueIndex = newCurrentIndex) }
        rebuildShuffleIndices(queue.size, newCurrentIndex)
    }

    // ── Shuffle & Repeat ──────────────────────────────────────────────────────

    fun toggleShuffle() {
        val state = _uiState.value
        val newShuffle = !state.isShuffleOn
        _uiState.update { it.copy(isShuffleOn = newShuffle) }
        if (newShuffle) {
            rebuildShuffleIndices(state.queue.size, state.currentQueueIndex)
        }
    }

    fun cycleRepeatMode() {
        val current = _uiState.value.repeatMode
        val next = when (current) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _uiState.update { it.copy(repeatMode = next) }
    }

    fun toggleShowQueue() {
        _uiState.update { it.copy(showQueue = !it.showQueue) }
    }

    private fun rebuildShuffleIndices(queueSize: Int, currentIndex: Int) {
        if (queueSize == 0) {
            shuffledIndices = emptyList()
            return
        }
        val indices = (0 until queueSize).toMutableList()
        indices.remove(currentIndex)
        indices.shuffle()
        // currentIndex selalu di posisi pertama shuffle agar tidak diulang
        shuffledIndices = listOf(currentIndex) + indices
    }

    // ── Internal play ─────────────────────────────────────────────────────────

    /**
     * Inti pemutaran lagu. Membatalkan job sebelumnya sebelum memulai yang baru,
     * sehingga ganti lagu cepat tidak menyebabkan race condition / delay berlapis.
     */
    private fun playSongInternal(song: Song, addToQueue: Boolean) {
        // BUG FIX: batalkan fetch URL yang sedang berjalan sebelum memulai yang baru
        playJob?.cancel()

        pendingSong = song
        playJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    currentSong = song,
                    errorMessage = null,
                    currentPosition = 0L,
                    duration = 0L
                )
            }

            try {
                // Offline playback: jika lagu sudah diunduh, putar file lokal langsung
                // tanpa perlu koneksi internet atau fetch URL dari server.
                val audioUrl: String = if (!song.localFilePath.isNullOrBlank()) {
                    Log.d(TAG, "Memutar dari file lokal: ${song.localFilePath}")
                    song.localFilePath!!
                } else {
                    val quality = userPreferencesDataStore.userPreferences.first().audioQuality
                    val streamInfo = audioExtractor.extractAudioUrl(song.videoId, quality)

                    if (streamInfo == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal mendapatkan URL audio. Cek koneksi internet."
                            )
                        }
                        return@launch
                    }
                    streamInfo.url
                }

                playUrl(song, audioUrl)

                historyRepository.addToHistory(
                    HistoryItem(
                        videoId = song.videoId,
                        title = song.title,
                        channelName = song.channelName,
                        thumbnailUrl = song.thumbnailUrl
                    )
                )

                observeLibraryStatus(song.videoId)

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e(TAG, "Gagal memutar lagu: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal memuat audio: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun retryWithFreshUrl(song: Song) {
        playJob?.cancel()
        playJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = "Menyegarkan URL audio..."
                )
            }
            try {
                val quality = userPreferencesDataStore.userPreferences.first().audioQuality
                val streamInfo = audioExtractor.extractAudioUrl(song.videoId, quality)

                if (streamInfo == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "URL audio tidak tersedia. Coba lagi nanti."
                        )
                    }
                    return@launch
                }

                playUrl(song, streamInfo.url)

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.e(TAG, "Retry gagal: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Retry gagal: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun playUrl(song: Song, audioUrl: String) {
        val mediaItem = MediaItem.Builder()
            .setUri(audioUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.channelName)
                    .setArtworkUri(android.net.Uri.parse(song.thumbnailUrl))
                    .build()
            )
            .build()

        mediaController?.run {
            stop()
            clearMediaItems()
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        Log.d(TAG, "Memutar: ${song.title} | url=${audioUrl.take(80)}...")
    }

    private fun observeLibraryStatus(videoId: String) {
        libraryObserverJob?.cancel()
        libraryObserverJob = viewModelScope.launch {
            observeIsInLibraryUseCase(videoId).collect { isInLibrary ->
                _uiState.update { it.copy(isInLibrary = isInLibrary) }
            }
        }
    }

    // ── Controls ──────────────────────────────────────────────────────────────

    fun togglePlayPause() {
        mediaController?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
        _uiState.update { it.copy(currentPosition = position) }
    }

    fun toggleLibrary(song: Song) {
        viewModelScope.launch {
            if (_uiState.value.isInLibrary) {
                removeFromLibrary(song.videoId)
                _uiState.update { it.copy(isInLibrary = false) }
            } else {
                saveToLibrary(
                    LibraryItem(
                        videoId = song.videoId,
                        title = song.title,
                        channelName = song.channelName,
                        thumbnailUrl = song.thumbnailUrl
                    )
                )
                _uiState.update { it.copy(isInLibrary = true) }
            }
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    override fun onCleared() {
        stopPositionPolling()
        playJob?.cancel()
        libraryObserverJob?.cancel()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}