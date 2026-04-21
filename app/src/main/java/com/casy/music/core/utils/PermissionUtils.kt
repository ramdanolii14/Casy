package com.casy.music.core.utils

import android.Manifest
import android.os.Build

object PermissionUtils {
    fun getRequiredStoragePermissions(): List<String> = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            listOf(Manifest.permission.READ_MEDIA_AUDIO)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        else ->
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun getNotificationPermission(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.POST_NOTIFICATIONS else null

    fun getAllRequiredPermissions(): List<String> =
        getRequiredStoragePermissions().toMutableList()
            .also { list -> getNotificationPermission()?.let { list.add(it) } }
}
