package com.casy.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.casy.music.ui.screen.main.MainScreen
import com.casy.music.ui.screen.nowplaying.NowPlayingViewModel
import com.casy.music.ui.theme.CasyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // NowPlayingViewModel dibuat di Activity scope agar shared antar semua screen
    private val nowPlayingViewModel: NowPlayingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeState by nowPlayingViewModel.themeState.collectAsStateWithLifecycle()

            CasyTheme(
                darkTheme    = themeState.isDarkMode,
                dynamicColor = themeState.isDynamicColor
            ) {
                MainScreen(nowPlayingViewModel = nowPlayingViewModel)
            }
        }
    }
}