package uz.testmarkaz.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import uz.testmarkaz.data.catalog.CatalogRepository
import uz.testmarkaz.data.catalog.ContentPackInfo
import uz.testmarkaz.data.db.dao.ProgressDao
import uz.testmarkaz.data.db.entity.InstalledPackEntity
import uz.testmarkaz.data.download.DownloadResult
import uz.testmarkaz.data.download.PackDownloadRepository
import javax.inject.Inject

data class DownloadsUiState(
    val catalog: List<ContentPackInfo> = emptyList(),
    val installedPacks: List<InstalledPackEntity> = emptyList(),
    val downloading: Set<String> = emptySet(),
    val errors: Map<String, String> = emptyMap()
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val downloadRepository: PackDownloadRepository,
    progressDao: ProgressDao
) : ViewModel() {

    private val _catalog = MutableStateFlow<List<ContentPackInfo>>(emptyList())
    private val _downloading = MutableStateFlow<Set<String>>(emptySet())
    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())

    val uiState = combine(
        _catalog,
        progressDao.observeInstalledPacks(),
        _downloading,
        _errors
    ) { catalog, installed, downloading, errors ->
        DownloadsUiState(catalog, installed, downloading, errors)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DownloadsUiState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _catalog.value = catalogRepository.loadAll().filter { it.isPublished }
        }
    }

    fun download(pack: ContentPackInfo) {
        if (_downloading.value.contains(pack.packKey)) return
        viewModelScope.launch {
            _downloading.value = _downloading.value + pack.packKey
            _errors.value = _errors.value - pack.packKey
            when (val result = downloadRepository.download(pack)) {
                is DownloadResult.Success -> Unit
                is DownloadResult.Error -> _errors.value = _errors.value + (pack.packKey to result.message)
            }
            _downloading.value = _downloading.value - pack.packKey
        }
    }
}
