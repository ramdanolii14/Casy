package com.casy.music.domain.model

/**
 * Domain model untuk satu lagu.
 *
 * @param localFilePath Path file MP3 lokal jika sudah diunduh, null jika streaming.
 *                      NowPlayingViewModel memutar file lokal ini langsung
 *                      tanpa fetch URL dari network.
 * @param isDownloaded  True jika lagu sudah tersimpan di perangkat.
 * @param durationSeconds Durasi lagu dalam detik (0 jika belum diketahui).
 */
data class Song(
    val videoId: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val durationSeconds: Int = 0,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null
)