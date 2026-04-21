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

    override suspend fun doWork(): Result {
        val videoId = inputData.getString("videoId") ?: return Result.failure()
        val title = inputData.getString("title") ?: "Unknown"

        return try {
            val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "Casy Music")
            if (!musicDir.exists()) musicDir.mkdirs()

            val file = File(musicDir, "$videoId.mp3")

            val request = YoutubeDLRequest("https://www.youtube.com/watch?v=$videoId")
            request.addOption("-f", "bestaudio")
            request.addOption("--extract-audio")
            request.addOption("--audio-format", "mp3")
            request.addOption("-o", file.absolutePath)

            YoutubeDL.getInstance().execute(request)

            val entity = SongEntity(
                videoId = videoId,
                title = title,
                channelName = inputData.getString("artist") ?: "",
                thumbnailUrl = inputData.getString("thumbnail") ?: "",
                localPath = file.absolutePath,
                isDownloaded = true
            )
            songDao.insertSong(entity)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}