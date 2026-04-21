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

    // ──────────────────────────────────────────────────────────────────────────
    // FIX: workManagerConfiguration menggunakan HiltWorkerFactory agar
    // @HiltWorker / @AssistedInject pada DownloadWorker bisa di-resolve.
    // Tanpa ini WorkManager mencoba instantiate DownloadWorker via refleksi
    // dengan constructor 2-arg yang tidak ada →
    // ExceptionInInitializerError "not a concrete class".
    // ──────────────────────────────────────────────────────────────────────────
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        // PENTING: YoutubeDL.init() melakukan I/O disk (extract binary ke cache)
        // Jalankan di background agar main thread tidak diblokir (ANR / jank).
        applicationScope.launch(Dispatchers.IO) {
            try {
                YoutubeDL.getInstance().init(this@CasyApplication)
                Log.i(TAG, "YoutubeDL berhasil diinisialisasi")

                // Update yt-dlp binary secara otomatis setiap launch.
                // Jika tidak ada update → selesai < 1 detik (cek versi saja).
                // Jika ada update → download binary baru di background.
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