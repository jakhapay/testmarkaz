package uz.testmarkaz.ui.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uz.testmarkaz.domain.model.TestResult
import uz.testmarkaz.ui.testconfig.ResultHolder
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    private val _result = MutableStateFlow<TestResult?>(ResultHolder.get(sessionId))
    val result: StateFlow<TestResult?> = _result.asStateFlow()
}
