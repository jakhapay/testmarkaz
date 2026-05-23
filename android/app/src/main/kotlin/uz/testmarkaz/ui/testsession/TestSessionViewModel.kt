package uz.testmarkaz.ui.testsession

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.testmarkaz.domain.model.TestQuestion
import uz.testmarkaz.domain.model.TestResult
import uz.testmarkaz.domain.model.TestSession
import uz.testmarkaz.domain.usecase.CompleteTestUseCase
import uz.testmarkaz.ui.testconfig.ResultHolder
import uz.testmarkaz.ui.testconfig.SessionHolder
import javax.inject.Inject

data class TestSessionUiState(
    val session: TestSession? = null,
    val currentIndex: Int = 0,
    val selectedOption: String? = null,
    val isAnswerRevealed: Boolean = false,
    val isCompleting: Boolean = false
)

@HiltViewModel
class TestSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val completeTestUseCase: CompleteTestUseCase
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _state = MutableStateFlow(TestSessionUiState())
    val state: StateFlow<TestSessionUiState> = _state.asStateFlow()

    init {
        val session = SessionHolder.get(sessionId)
        _state.update { it.copy(session = session) }
    }

    val currentQuestion: TestQuestion?
        get() = _state.value.session?.questions?.getOrNull(_state.value.currentIndex)

    fun selectOption(option: String) {
        if (_state.value.isAnswerRevealed) return
        _state.update { it.copy(selectedOption = option) }
    }

    fun confirmAnswer() {
        val s = _state.value
        val session = s.session ?: return
        val question = session.questions.getOrNull(s.currentIndex) ?: return
        val selected = s.selectedOption ?: return

        session.answers[question.id] = selected
        _state.update { it.copy(isAnswerRevealed = true) }
    }

    fun nextQuestion(onComplete: (String) -> Unit) {
        val s = _state.value
        val session = s.session ?: return
        val next = s.currentIndex + 1

        if (next >= session.totalQuestions) {
            finishTest(session, onComplete)
        } else {
            _state.update {
                it.copy(
                    currentIndex = next,
                    selectedOption = null,
                    isAnswerRevealed = false
                )
            }
        }
    }

    private fun finishTest(session: TestSession, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isCompleting = true) }
            val result = completeTestUseCase.invoke(session)
            ResultHolder.put(result)
            SessionHolder.remove(sessionId)
            onComplete(result.sessionId)
        }
    }
}
