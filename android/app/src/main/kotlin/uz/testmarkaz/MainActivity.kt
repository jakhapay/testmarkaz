package uz.testmarkaz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import uz.testmarkaz.data.session.PausedSessionRepository
import uz.testmarkaz.navigation.AppNavigation
import uz.testmarkaz.ui.theme.TestMarkazTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var pausedSessionRepository: PausedSessionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestMarkazTheme {
                AppNavigation(pausedSessionRepository = pausedSessionRepository)
            }
        }
    }
}
