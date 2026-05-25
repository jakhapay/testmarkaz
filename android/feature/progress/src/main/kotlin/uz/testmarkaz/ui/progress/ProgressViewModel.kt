package uz.testmarkaz.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import uz.testmarkaz.data.db.dao.ProgressDao
import uz.testmarkaz.data.db.entity.TopicMasteryEntity
import uz.testmarkaz.data.db.entity.UserStatsEntity
import javax.inject.Inject

data class ProgressUiState(
    val stats: UserStatsEntity? = null,
    val weakTopics: List<TopicMasteryEntity> = emptyList(),
    val allMastery: List<TopicMasteryEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val progressDao: ProgressDao
) : ViewModel() {

    val uiState = combine(
        progressDao.observeStats(),
        progressDao.observeWeakTopics(limit = 5),
        progressDao.observeAllMastery()
    ) { stats, weak, all ->
        ProgressUiState(
            stats = stats,
            weakTopics = weak,
            allMastery = all,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProgressUiState()
    )
}
