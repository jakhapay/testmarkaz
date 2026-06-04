package uz.testmarkaz.ui.pdfimport

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uz.testmarkaz.domain.model.TestConfig
import uz.testmarkaz.domain.usecase.GenerateQuestionsFromPdfUseCase
import uz.testmarkaz.domain.usecase.GenerateQuestionsFromPdfUseCase.Outcome
import uz.testmarkaz.domain.usecase.GenerateQuestionsFromPdfUseCase.Stage
import uz.testmarkaz.domain.usecase.GenerateTestUseCase
import uz.testmarkaz.ui.testconfig.SessionHolder
import javax.inject.Inject

data class PdfImportUiState(
    val fileName: String = "",
    val isWorking: Boolean = false,
    val stage: Stage? = null,
    val result: GenerateQuestionsFromPdfUseCase.Result? = null,
    val error: String? = null
)

sealed interface PdfImportEffect {
    data class NavigateToSession(val sessionId: String) : PdfImportEffect
}

@HiltViewModel
class PdfImportViewModel @Inject constructor(
    private val generateFromPdf: GenerateQuestionsFromPdfUseCase,
    private val generateTest: GenerateTestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(PdfImportUiState())
    val state: StateFlow<PdfImportUiState> = _state.asStateFlow()

    private val _effects = MutableStateFlow<PdfImportEffect?>(null)
    val effects: StateFlow<PdfImportEffect?> = _effects.asStateFlow()

    fun onPdfPicked(uri: Uri, fileName: String) {
        if (_state.value.isWorking) return
        _state.value = PdfImportUiState(fileName = fileName, isWorking = true)
        viewModelScope.launch {
            val outcome = generateFromPdf.invoke(
                uri = uri,
                title = fileName.removeSuffix(".pdf"),
                onStage = { stage -> _state.update { it.copy(stage = stage) } }
            )
            _state.update {
                when (outcome) {
                    is Outcome.Success -> it.copy(isWorking = false, result = outcome.result)
                    is Outcome.Failure -> it.copy(isWorking = false, error = outcome.reason)
                }
            }
        }
    }

    /** Build a test session from the just-created pack and navigate to it. */
    fun startTestFromPack() {
        val packKey = _state.value.result?.packKey ?: return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true) }
            val session = generateTest.invoke(TestConfig.pdfPack(packKey))
            _state.update { it.copy(isWorking = false) }
            if (session != null) {
                SessionHolder.put(session)
                _effects.update { PdfImportEffect.NavigateToSession(session.sessionId) }
            } else {
                _state.update { it.copy(error = "Test boshlab bo'lmadi: savollar yetarli emas.") }
            }
        }
    }

    fun clearEffect() = _effects.update { null }

    fun reset() {
        _state.value = PdfImportUiState()
    }
}
