package uz.testmarkaz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import uz.testmarkaz.data.session.PausedSessionRepository
import uz.testmarkaz.ui.downloads.DownloadsScreen
import uz.testmarkaz.ui.home.HomeScreen
import uz.testmarkaz.ui.pdfimport.PdfImportScreen
import uz.testmarkaz.ui.profile.ProfileScreen
import uz.testmarkaz.ui.progress.ProgressScreen
import uz.testmarkaz.ui.results.ResultsScreen
import uz.testmarkaz.ui.testconfig.SessionHolder
import uz.testmarkaz.ui.testconfig.TestConfigScreen
import uz.testmarkaz.ui.testsession.TestSessionScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    pausedSessionRepository: PausedSessionRepository
) {
    val scope = rememberCoroutineScope()

    NavHost(
        navController    = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onStartTest     = { navController.navigate(Routes.TEST_CONFIG) },
                onResume        = { sessionId ->
                    scope.launch {
                        val restored = pausedSessionRepository.restoreById(sessionId) ?: return@launch
                        SessionHolder.put(restored.session)
                        navController.navigate(Routes.testSession(restored.session.sessionId))
                    }
                },
                onOpenDownloads = { navController.navigate(Routes.DOWNLOADS) },
                onOpenProgress  = { navController.navigate(Routes.PROGRESS) },
                onOpenProfile   = { navController.navigate(Routes.PROFILE) },
                onOpenPdfImport = { navController.navigate(Routes.PDF_IMPORT) }
            )
        }

        composable(Routes.PDF_IMPORT) {
            PdfImportScreen(
                onBack = { navController.popBackStack() },
                onSessionReady = { sessionId ->
                    navController.navigate(Routes.testSession(sessionId)) {
                        popUpTo(Routes.PDF_IMPORT) { inclusive = true }
                    }
                }
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
                sessionId  = sessionId,
                onComplete = { sid ->
                    navController.navigate(Routes.results(sid)) {
                        popUpTo(Routes.HOME)
                    }
                },
                onBack = { navController.popBackStack() }
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
