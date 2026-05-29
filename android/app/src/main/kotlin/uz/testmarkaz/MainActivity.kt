package uz.testmarkaz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import uz.testmarkaz.navigation.AppNavigation
import uz.testmarkaz.ui.theme.TestMarkazTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestMarkazTheme {
                AppNavigation()
            }
        }
    }
}
