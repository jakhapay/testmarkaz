package uz.testmarkaz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uz.testmarkaz.ui.downloads.DownloadsScreen
import uz.testmarkaz.ui.home.HomeScreen
import uz.testmarkaz.ui.profile.ProfileScreen
import uz.testmarkaz.ui.progress.ProgressScreen
import uz.testmarkaz.ui.results.ResultsScreen
import uz.testmarkaz.ui.testconfig.TestConfigScreen
import uz.testmarkaz.ui.testsession.TestSessionScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onStartTest     = { navController.navigate(Routes.TEST_CONFIG) },
                onOpenDownloads = { navController.navigate(Routes.DOWNLOADS) },
                onOpenProgress  = { navController.navigate(Routes.PROGRESS) },
                onOpenProfile   = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.TEST_CONFIG) {
            TestConfigScreen(
                onSessionReady = { sessionId ->
                    navController.navigate(Routes.testSession(sessionId)) {
                        popUpTo(Routes.TEST_CONFIG) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TEST_SESSION) { backStack ->
            val sessionId = backStack.arguments?.getString("sessionId") ?: return@composable
            TestSessionScreen(
                sessionId = sessionId,
                onComplete = { sid ->
                    navController.navigate(Routes.results(sid)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(Routes.RESULTS) { backStack ->
            val sessionId = backStack.arguments?.getString("sessionId") ?: return@composable
            ResultsScreen(
                sessionId = sessionId,
                onRetry   = { navController.navigate(Routes.TEST_CONFIG) },
                onHome    = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DOWNLOADS) {
            DownloadsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PROGRESS) {
            ProgressScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PROFILE) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
    }
}
