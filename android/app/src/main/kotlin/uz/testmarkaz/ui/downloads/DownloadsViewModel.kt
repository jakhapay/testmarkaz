package uz.testmarkaz.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import uz.testmarkaz.data.db.dao.ProgressDao
import uz.testmarkaz.data.db.entity.InstalledPackEntity
import javax.inject.Inject

data class DownloadsUiState(
    val installedPacks: List<InstalledPackEntity> = emptyList()
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    progressDao: ProgressDao
) : ViewModel() {

    val uiState = progressDao.observeInstalledPacks()
        .map { packs -> DownloadsUiState(installedPacks = packs) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DownloadsUiState()
        )
}
