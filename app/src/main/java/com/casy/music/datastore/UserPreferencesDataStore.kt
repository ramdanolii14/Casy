package com.casy.music.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.casy.music.core.constants.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppConstants.PREFS_NAME
)

data class UserPreferences(
    val isDarkMode: Boolean = false,
    val isDynamicColor: Boolean = true,
    val audioQuality: String = AppConstants.QUALITY_HIGH
)

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_DARK_MODE     = booleanPreferencesKey(AppConstants.KEY_DARK_MODE)
    private val KEY_DYNAMIC_COLOR = booleanPreferencesKey(AppConstants.KEY_DYNAMIC_COLOR)
    private val KEY_AUDIO_QUALITY = stringPreferencesKey(AppConstants.KEY_AUDIO_QUALITY)

    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            isDarkMode     = prefs[KEY_DARK_MODE]     ?: false,
            isDynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
            audioQuality   = prefs[KEY_AUDIO_QUALITY] ?: AppConstants.QUALITY_HIGH
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setAudioQuality(quality: String) {
        context.dataStore.edit { it[KEY_AUDIO_QUALITY] = quality }
    }
}
