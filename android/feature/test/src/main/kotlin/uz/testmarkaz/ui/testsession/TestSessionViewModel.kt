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
import uz.testmarkaz.data.session.PausedSessionRepository
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
    private val completeTestUseCase: CompleteTestUseCase,
    private val pausedRepo: PausedSessionRepository
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _state = MutableStateFlow(TestSessionUiState())
    val state: StateFlow<TestSessionUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Normal start — session is already in memory
            val inMemory = SessionHolder.get(sessionId)
            if (inMemory != null) {
                _state.update { it.copy(session = inMemory, currentIndex = inMemory.answeredCount) }
                return@launch
            }
            // 2. App restarted — restore from Room
            val restored = pausedRepo.restore()
            if (restored != null && restored.session.sessionId == sessionId) {
                SessionHolder.put(restored.session)
                _state.update { it.copy(session = restored.session, currentIndex = restored.currentIndex) }
            }
        }
    }

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
            _state.update { it.copy(currentIndex = next, selectedOption = null, isAnswerRevealed = false) }
        }
    }

    /** Persist progress then navigate back. */
    fun pauseAndExit(onBack: () -> Unit) {
        val s = _state.value
        val session = s.session ?: run { onBack(); return }
        viewModelScope.launch {
            pausedRepo.save(session, s.currentIndex)
            onBack()
        }
    }

    private fun finishTest(session: TestSession, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isCompleting = true) }
            pausedRepo.clear(session.sessionId)   // no longer a paused session
            val result = completeTestUseCase.invoke(session)
            ResultHolder.put(result)
            SessionHolder.remove(sessionId)
            onComplete(result.sessionId)
        }
    }
}
