package com.casy.music.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.casy.music.datastore.UserPreferences
import com.casy.music.datastore.UserPreferencesDataStore
import com.casy.music.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: UserPreferencesDataStore,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = dataStore.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch { dataStore.setDarkMode(enabled) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch { dataStore.setDynamicColor(enabled) }
    }

    fun updateAudioQuality(quality: String) {
        viewModelScope.launch { dataStore.setAudioQuality(quality) }
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clearHistory() }
    }
}