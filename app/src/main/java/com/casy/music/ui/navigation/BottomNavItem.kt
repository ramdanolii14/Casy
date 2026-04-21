package com.casy.music.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Search : BottomNavItem("search", "Cari", Icons.Filled.Search, Icons.Outlined.Search)
    object Library : BottomNavItem("library", "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
    object Settings : BottomNavItem("settings", "Setelan", Icons.Filled.Settings, Icons.Outlined.Settings)
}
