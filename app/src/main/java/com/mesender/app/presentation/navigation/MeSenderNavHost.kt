package com.mesender.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mesender.app.presentation.ui.home.HomeScreen
import com.mesender.app.presentation.ui.search.SearchScreen
import com.mesender.app.presentation.ui.settings.SettingsScreen
import com.mesender.app.presentation.ui.thread.ThreadScreen

@Composable
fun MeSenderNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoutes.HOME) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onOpenInbox = { inboxId -> navController.navigate(NavRoutes.thread(inboxId)) },
                onOpenSearch = { navController.navigate(NavRoutes.SEARCH) },
                onOpenSettings = { navController.navigate(NavRoutes.SETTINGS) }
            )
        }
        composable(NavRoutes.THREAD) { backStackEntry ->
            val inboxId = backStackEntry.arguments?.getString("inboxId")?.toLongOrNull()
            if (inboxId != null) {
                ThreadScreen(inboxId = inboxId, onBack = { navController.navigateUp() })
            }
        }
        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onBack = { navController.navigateUp() },
                onOpenInbox = { inboxId -> navController.navigate(NavRoutes.thread(inboxId)) }
            )
        }
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(onBack = { navController.navigateUp() })
        }
    }
}