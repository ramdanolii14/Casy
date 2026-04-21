package com.casy.music.ui.screen.main

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.casy.music.ui.components.MiniPlayer
import com.casy.music.ui.navigation.BottomNavItem
import com.casy.music.ui.navigation.NavGraph
import com.casy.music.ui.navigation.Routes
import com.casy.music.ui.screen.nowplaying.NowPlayingViewModel

@Composable
fun MainScreen(
    nowPlayingViewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by nowPlayingViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Library,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = currentDestination?.route != Routes.NOW_PLAYING

    Scaffold(
        bottomBar = {
            Column {
                // Mini Player — muncul di atas bottom nav saat ada lagu aktif
                AnimatedVisibility(
                    visible = uiState.currentSong != null && showBottomBar,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    uiState.currentSong?.let { song ->
                        MiniPlayer(
                            song = song,
                            isPlaying = uiState.isPlaying,
                            onPlayPauseClick = { nowPlayingViewModel.togglePlayPause() },
                            onPlayerClick = { navController.navigate(Routes.NOW_PLAYING) }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy
                                ?.any { it.route == item.route } == true

                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon
                                        else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label   = { Text(item.label) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavGraph(
            navController = navController,
            modifier      = Modifier.padding(paddingValues),
            nowPlayingViewModel = nowPlayingViewModel
        )
    }
}