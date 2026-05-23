package uz.testmarkaz.ui.testconfig

import uz.testmarkaz.domain.model.TestResult

/** Holds completed TestResult in memory until ResultsScreen reads it */
object ResultHolder {
    private val map = mutableMapOf<String, TestResult>()
    fun put(result: TestResult) { map[result.sessionId] = result }
    fun get(id: String): TestResult? = map[id]
    fun remove(id: String) { map.remove(id) }
}
