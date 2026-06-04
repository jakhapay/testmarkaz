package uz.testmarkaz.ui.testconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.testmarkaz.domain.model.Subject
import uz.testmarkaz.domain.model.TestConfig
import uz.testmarkaz.domain.model.TestMode
import uz.testmarkaz.domain.model.TestSession
import uz.testmarkaz.domain.usecase.GenerateTestUseCase
import javax.inject.Inject

data class TestConfigUiState(
    val mode: TestMode = TestMode.SUBJECT,
    val selectedSubject: Subject = Subject.MATEMATIKA,
    val selectedGrade: Int = 9,
    val gradeMin: Int = 9,
    val gradeMax: Int = 11,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)

sealed interface TestConfigEffect {
    data class NavigateToSession(val sessionId: String) : TestConfigEffect
}

@HiltViewModel
class TestConfigViewModel @Inject constructor(
    private val generateTestUseCase: GenerateTestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TestConfigUiState())
    val state: StateFlow<TestConfigUiState> = _state.asStateFlow()

    private val _effects = MutableStateFlow<TestConfigEffect?>(null)
    val effects: StateFlow<TestConfigEffect?> = _effects.asStateFlow()

    fun selectMode(mode: TestMode) = _state.update { it.copy(mode = mode, errorMessage = null) }
    fun selectSubject(subject: Subject) = _state.update { it.copy(selectedSubject = subject) }
    fun selectGrade(grade: Int) = _state.update { it.copy(selectedGrade = grade) }
    fun selectGradeMin(grade: Int) = _state.update { it.copy(gradeMin = grade.coerceAtMost(it.gradeMax)) }
    fun selectGradeMax(grade: Int) = _state.update { it.copy(gradeMax = grade.coerceAtLeast(it.gradeMin)) }
    fun clearEffect() = _effects.update { null }

    fun startTest() {
        val s = _state.value
        val config = when (s.mode) {
            TestMode.SUBJECT      -> TestConfig.singleSubject(s.selectedSubject, s.selectedGrade)
            TestMode.RANGE        -> TestConfig.subjectRange(s.selectedSubject, s.gradeMin, s.gradeMax)
            TestMode.RANDOM_CLASS -> TestConfig.randomClass(s.selectedGrade)
            TestMode.FULL_RANDOM  -> TestConfig.fullRandom()
            TestMode.PDF_PACK     -> return   // PDF tests start from PdfImportScreen, not here
        }

        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, errorMessage = null) }
            val session = generateTestUseCase.invoke(config)
            if (session != null) {
                // Store session in a shared holder so TestSessionScreen can retrieve it
                SessionHolder.put(session)
                _effects.update { TestConfigEffect.NavigateToSession(session.sessionId) }
            } else {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = "Bu bo'yicha yetarli savollar yo'q. Iltimos, qo'shimcha paketlarni yuklab oling."
                    )
                }
            }
            _state.update { it.copy(isGenerating = false) }
        }
    }
}

/** In-memory session holder — avoids Parcelable complexity for large question lists */
object SessionHolder {
    private val map = mutableMapOf<String, TestSession>()
    fun put(session: TestSession) { map[session.sessionId] = session }
    fun get(id: String): TestSession? = map[id]
    fun remove(id: String) { map.remove(id) }
}
