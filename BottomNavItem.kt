package com.cryptosignal.assistant.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.cryptosignal.assistant.R

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int
) {
    Signals(
        route = "signals",
        icon = Icons.Filled.BarChart,
        labelRes = R.string.nav_signals
    ),
    Statistics(
        route = "statistics",
        icon = Icons.Filled.Analytics,
        labelRes = R.string.nav_statistics
    ),
    Settings(
        route = "settings",
        icon = Icons.Filled.Settings,
        labelRes = R.string.nav_settings
    )
}