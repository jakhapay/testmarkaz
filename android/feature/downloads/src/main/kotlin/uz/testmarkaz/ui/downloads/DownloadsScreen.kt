package uz.testmarkaz.ui.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.testmarkaz.data.catalog.ContentPackInfo
import uz.testmarkaz.data.db.entity.InstalledPackEntity
import uz.testmarkaz.domain.model.Subject
import uz.testmarkaz.ui.theme.Secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val installedKeys = state.installedPacks.map { it.packKey }.toSet()
    val installed = state.catalog.filter { it.packKey in installedKeys }
    val available = state.catalog.filter { it.packKey !in installedKeys }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savollar to'plami") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Paketlar haqida",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Har bir paket bitta fan va sinf uchun savollar to'plamini o'z ichiga oladi. Yuklab olgandan so'ng oflayn ishlaydi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            if (installed.isNotEmpty()) {
                item {
                    Text(
                        "Yuklangan paketlar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(installed, key = { it.packKey }) { pack ->
                    InstalledPackCard(pack = pack)
                }
            }

            if (available.isNotEmpty()) {
                item {
                    Text(
                        "Mavjud paketlar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = if (installed.isNotEmpty()) 8.dp else 0.dp)
                    )
                }
                items(available, key = { it.packKey }) { pack ->
                    AvailablePackCard(
                        pack = pack,
                        isDownloading = pack.packKey in state.downloading,
                        error = state.errors[pack.packKey],
                        onDownload = { viewModel.download(pack) }
                    )
                }
            }

            if (state.catalog.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledPackCard(pack: ContentPackInfo) {
    val subject = Subject.fromCode(pack.subjectCode)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Secondary.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${subject?.emoji ?: "📦"}  ${pack.nameUz}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${pack.questionCount} savol",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Icon(Icons.Default.CheckCircle, contentDescription = "Yuklangan", tint = Secondary)
        }
    }
}

@Composable
private fun AvailablePackCard(
    pack: ContentPackInfo,
    isDownloading: Boolean,
    error: String?,
    onDownload: () -> Unit
) {
    val subject = Subject.fromCode(pack.subjectCode)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${subject?.emoji ?: "📦"}  ${pack.nameUz}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${pack.questionCount} savol",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (error != null) {
                    Text(
                        error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (isDownloading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else if (error != null) {
                IconButton(onClick = onDownload) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = "Qayta urinish",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                IconButton(onClick = onDownload) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Yuklab olish",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
