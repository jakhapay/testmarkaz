package uz.testmarkaz

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import uz.testmarkaz.data.mock.MockDataSeeder
import javax.inject.Inject

@HiltAndroidApp
class TestMarkazApp : Application() {

    @Inject lateinit var mockDataSeeder: MockDataSeeder

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            mockDataSeeder.seedIfEmpty()
        }
    }
}
