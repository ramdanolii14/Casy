package com.casy.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CasyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Scope aplikasi — tidak dibatalkan selama aplikasi hidup
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // ──────────────────────────────────────────────────────────────────────
        // PENTING: YoutubeDL.init() melakukan I/O disk (extract binary ke cache)
        // yang bisa memakan waktu 500ms – 1500ms.
        // Jalankan di background agar main thread tidak diblokir (ANR / jank).
        // AudioExtractor sudah aman: ia juga berjalan di Dispatchers.IO.
        // ──────────────────────────────────────────────────────────────────────
        applicationScope.launch(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().init(this@CasyApplication)
                Log.i(TAG, "YoutubeDL berhasil diinisialisasi")

                // ── Update yt-dlp binary secara otomatis ──────────────────────
                // YouTube terus mengubah format & signature tiap beberapa minggu.
                // Tanpa update rutin, yt-dlp lama akan gagal dengan error
                // "Requested format is not available" atau 403.
                //
                // UpdateChannel.STABLE = rilis stabil (direkomendasikan production)
                // UpdateChannel.NIGHTLY = rilis harian (lebih baru tapi mungkin ada bug)
                //
                // updateYoutubeDL() aman dipanggil setiap launch:
                //  - Jika tidak ada update → selesai dalam < 1 detik (cek versi saja)
                //  - Jika ada update → download binary baru di background
                // ──────────────────────────────────────────────────────────────
                try {
                    val status = YoutubeDL.getInstance()
                        .updateYoutubeDL(this@CasyApplication, YoutubeDL.UpdateChannel.STABLE)
                    Log.i(TAG, "Update yt-dlp: $status")
                } catch (e: Exception) {
                    // Gagal update tidak fatal — binary lama masih bisa dipakai
                    Log.w(TAG, "Update yt-dlp gagal (akan pakai versi lama): ${e.message}")
                }

            } catch (e: YoutubeDLException) {
                Log.e(TAG, "Gagal inisialisasi YoutubeDL: ${e.message}")
            }
        }

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_PLAYBACK,
                    "Casy Music Playback",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notifikasi kontrol pemutaran musik"
                    setShowBadge(false)
                }
            )

            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_DOWNLOAD,
                    "Casy Music Download",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifikasi unduhan lagu"
                }
            )
        }
    }

    companion object {
        private const val TAG = "CasyApplication"
        const val CHANNEL_PLAYBACK = "casy_playback_channel"
        const val CHANNEL_DOWNLOAD = "casy_download_channel"
    }
}