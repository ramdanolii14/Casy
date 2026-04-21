package com.casy.music.data.remote.extractor

import android.content.Context
import android.util.Log
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// Top-level constant agar bisa diakses dari AppModule tanpa instansiasi class
const val YTDL_UA = "com.google.android.youtube/20.10.38 (Linux; U; Android 11) gzip"

data class AudioStreamInfo(
    val url: String,
    val cookies: String = "",
    val userAgent: String = YTDL_UA
)

@Singleton
class AudioExtractor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AudioExtractor"

        /**
         * Urutan client yang dicoba dari yang paling stabil.
         *
         * - ios              : paling jarang diblokir YouTube saat ini
         * - android_testsuite: alternatif android yang lebih baru
         * - web              : fallback umum
         * - mweb             : fallback mobile web
         * - android          : client lama, kadang diblokir tapi tetap dicoba
         */
        private val CLIENTS = listOf(
            "ios",
            "android_testsuite",
            "web",
            "mweb",
            "android"
        )
    }

    /**
     * Mengambil URL audio yang siap diputar ExoPlayer.
     *
     * Strategi bertingkat: setiap client dicoba satu per satu hingga
     * ada yang berhasil mengembalikan URL valid.
     *
     * Format sengaja dibuat sederhana ("bestaudio") agar tidak gagal
     * hanya karena format spesifik (m4a/webm) tidak tersedia di video tertentu.
     */
    suspend fun extractAudioUrl(videoId: String, quality: String = "high"): AudioStreamInfo? =
        withContext(Dispatchers.IO) {
            val ytUrl = "https://www.youtube.com/watch?v=$videoId"
            val format = audioBestFormat(quality)

            for (client in CLIENTS) {
                try {
                    Log.d(TAG, "Mencoba client=$client videoId=$videoId quality=$quality")

                    val request = YoutubeDLRequest(ytUrl).apply {
                        addOption("--no-playlist")
                        addOption("-f", format)
                        addOption("--print", "url")
                        addOption("--extractor-args", "youtube:player_client=$client")
                        addOption("--add-header", "User-Agent:$YTDL_UA")
                        addOption("--add-header", "Origin:https://www.youtube.com")
                        addOption("--add-header", "Referer:https://www.youtube.com/")
                        addOption("--no-download")
                        addOption("--no-warnings")
                        addOption("--socket-timeout", "15")
                        addOption("--no-check-certificate")
                    }

                    val response = YoutubeDL.getInstance().execute(request)
                    val stdout = response.out.trim()
                    val audioUrl = stdout.lines()
                        .map { it.trim() }
                        .firstOrNull { it.startsWith("http") }

                    if (!audioUrl.isNullOrBlank()) {
                        Log.d(TAG, "Berhasil dengan client=$client url=${audioUrl.take(80)}...")
                        return@withContext AudioStreamInfo(url = audioUrl)
                    } else {
                        Log.w(
                            TAG,
                            "client=$client: output kosong. stderr=${response.err.take(300)}"
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "client=$client gagal: ${e.javaClass.simpleName}: ${e.message}")
                }
            }

            Log.e(TAG, "Semua client gagal untuk videoId=$videoId")
            null
        }

    /**
     * Selector format audio.
     *
     * Sengaja TIDAK mengunci ke ext tertentu (m4a/webm) sebagai syarat utama,
     * karena banyak video tidak punya semua format — cukup minta "bestaudio"
     * dan biarkan yt-dlp memilih yang terbaik yang tersedia.
     *
     * Fallback akhir selalu plain "bestaudio" tanpa filter apapun.
     */
    private fun audioBestFormat(quality: String): String = when (quality.lowercase()) {
        "low"    -> "worstaudio/bestaudio[abr<=64]/bestaudio"
        "medium" -> "bestaudio[abr<=128]/bestaudio"
        else     -> "bestaudio"
    }
}