package com.casy.music.service

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.work.*
import com.casy.music.core.constants.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getCasyMusicFolder(): File {
        val folder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),
                AppConstants.CASY_MUSIC_FOLDER_NAME)
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                AppConstants.CASY_MUSIC_FOLDER_NAME)
        }
        if (!folder.exists()) folder.mkdirs()
        return folder
    }

    fun enqueueDownload(videoId: String, title: String, audioUrl: String): Operation {
        val outputPath = File(getCasyMusicFolder(), "${sanitize(title)}.mp3").absolutePath
        val data = workDataOf(
            DownloadWorker.KEY_VIDEO_ID to videoId,
            DownloadWorker.KEY_TITLE to title,
            DownloadWorker.KEY_AUDIO_URL to audioUrl,
            DownloadWorker.KEY_OUTPUT_PATH to outputPath
        )
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(data)
            .addTag("casy_download")
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        return WorkManager.getInstance(context).enqueue(request)
    }

    fun getDownloadedFiles(): List<File> =
        getCasyMusicFolder().listFiles { f -> f.extension == "mp3" }?.toList() ?: emptyList()

    private fun sanitize(name: String) = name.replace(Regex("[\\\\/:*?\"<>|]"), "_").take(100)
}
