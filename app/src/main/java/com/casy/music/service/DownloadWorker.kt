package com.casy.music.service

import android.content.Context
import android.os.Environment
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.casy.music.data.local.dao.SongDao
import com.casy.music.data.local.entity.SongEntity
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val songDao: SongDao
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_VIDEO_ID    = "videoId"
        const val KEY_TITLE       = "title"
        const val KEY_AUDIO_URL   = "audioUrl"
        const val KEY_OUTPUT_PATH = "outputPath"
    }

    override suspend fun doWork(): Result {
        val videoId    = inputData.getString(KEY_VIDEO_ID)    ?: return Result.failure()
        val title      = inputData.getString(KEY_TITLE)       ?: "Unknown"
        val audioUrl   = inputData.getString(KEY_AUDIO_URL)
        val outputPath = inputData.getString(KEY_OUTPUT_PATH)

        return try {
            val musicDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                "Casy Music"
            )
            if (!musicDir.exists()) musicDir.mkdirs()

            val file = if (outputPath != null) File(outputPath)
            else File(musicDir, "$videoId.mp3")

            val request = YoutubeDLRequest(
                audioUrl ?: "https://www.youtube.com/watch?v=$videoId"
            )
            request.addOption("-f", "bestaudio")
            request.addOption("--extract-audio")
            request.addOption("--audio-format", "mp3")
            request.addOption("-o", file.absolutePath)

            YoutubeDL.getInstance().execute(request)

            // Simpan ke Room, lalu update kolom local_file_path via markAsDownloaded
            songDao.insertSong(
                SongEntity(
                    videoId      = videoId,
                    title        = title,
                    channelName  = inputData.getString("artist") ?: "",
                    thumbnailUrl = inputData.getString("thumbnail") ?: "",
                    isDownloaded = true,
                    localFilePath = file.absolutePath
                )
            )

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}