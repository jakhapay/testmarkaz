package uz.testmarkaz.navigation

object Routes {
    const val HOME         = "home"
    const val TEST_CONFIG  = "test_config"
    const val TEST_SESSION = "test_session/{sessionId}"
    const val RESULTS      = "results/{sessionId}"
    const val DOWNLOADS    = "downloads"
    const val PROGRESS     = "progress"
    const val PROFILE      = "profile"

    fun testSession(sessionId: String) = "test_session/$sessionId"
    fun results(sessionId: String)     = "results/$sessionId"
}
