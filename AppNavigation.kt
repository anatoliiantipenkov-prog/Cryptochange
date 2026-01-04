package com.cryptosignal.assistant.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cryptosignal.assistant.R
import com.cryptosignal.assistant.presentation.screens.settings.SettingsScreen
import com.cryptosignal.assistant.presentation.screens.signals.SignalsScreen
import com.cryptosignal.assistant.presentation.screens.statistics.StatisticsScreen
import com.cryptosignal.assistant.presentation.theme.LongGreen
import com.cryptosignal.assistant.presentation.theme.ShortRed

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                BottomNavItem.values().forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(stringResource(item.labelRes)) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Signals.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Signals.route) {
                SignalsScreen(navController = navController)
            }
            composable(BottomNavItem.Statistics.route) {
                StatisticsScreen(navController = navController)
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(navController = navController)
            }
        }
    }
}