package com.casy.music.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.casy.music.domain.model.Song
import com.casy.music.ui.screen.home.HomeScreen
import com.casy.music.ui.screen.library.LibraryScreen
import com.casy.music.ui.screen.nowplaying.NowPlayingScreen
import com.casy.music.ui.screen.nowplaying.NowPlayingViewModel
import com.casy.music.ui.screen.search.SearchScreen
import com.casy.music.ui.screen.settings.SettingsScreen

object Routes {
    const val HOME        = "home"
    const val SEARCH      = "search"
    const val LIBRARY     = "library"
    const val SETTINGS    = "settings"
    const val NOW_PLAYING = "now_playing"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    nowPlayingViewModel: NowPlayingViewModel,
    modifier: Modifier = Modifier
) {
    val onSongClick: (Song) -> Unit = { song ->
        nowPlayingViewModel.playSong(song)
        navController.navigate(Routes.NOW_PLAYING)
    }

    NavHost(
        navController    = navController,
        startDestination = Routes.HOME,
        modifier         = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(onSongClick = onSongClick)
        }
        composable(Routes.SEARCH) {
            SearchScreen(onSongClick = onSongClick)
        }
        composable(Routes.LIBRARY) {
            LibraryScreen(onSongClick = onSongClick)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen()
        }
        composable(Routes.NOW_PLAYING) {
            NowPlayingScreen(
                onBackClick = { navController.popBackStack() },
                viewModel   = nowPlayingViewModel
            )
        }
    }
}
