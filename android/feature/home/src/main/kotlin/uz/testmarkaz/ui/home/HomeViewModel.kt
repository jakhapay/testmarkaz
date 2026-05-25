package uz.testmarkaz.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import uz.testmarkaz.data.db.dao.ProgressDao
import uz.testmarkaz.data.db.dao.QuestionDao
import uz.testmarkaz.data.db.dao.TestSessionDao
import uz.testmarkaz.data.db.entity.UserStatsEntity
import uz.testmarkaz.data.db.entity.TestSessionEntity
import javax.inject.Inject

data class HomeUiState(
    val totalQuestions: Int = 0,
    val stats: UserStatsEntity? = null,
    val recentSessions: List<TestSessionEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val questionDao: QuestionDao,
    private val progressDao: ProgressDao,
    private val testSessionDao: TestSessionDao
) : ViewModel() {

    val uiState = combine(
        progressDao.observeStats(),
        testSessionDao.observeRecentSessions()
    ) { stats, sessions ->
        HomeUiState(
            totalQuestions = questionDao.countAll(),
            stats = stats,
            recentSessions = sessions,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
