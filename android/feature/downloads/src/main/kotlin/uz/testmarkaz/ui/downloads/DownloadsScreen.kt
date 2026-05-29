package uz.testmarkaz.ui.downloads

import androidx.compose.foundation.clickable
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

    var selectedPack by remember { mutableStateOf<ContentPackInfo?>(null) }

    selectedPack?.let { pack ->
        PackDetailSheet(
            pack = pack,
            isInstalled = pack.packKey in installedKeys,
            isDownloading = pack.packKey in state.downloading,
            error = state.errors[pack.packKey],
            onDownload = { viewModel.download(pack) },
            onDismiss = { selectedPack = null }
        )
    }

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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (installed.isNotEmpty()) {
                item {
                    SectionHeader("Yuklangan paketlar (${installed.size})")
                }
                items(installed, key = { it.packKey }) { pack ->
                    PackCard(
                        pack = pack,
                        isInstalled = true,
                        isDownloading = false,
                        error = null,
                        onClick = { selectedPack = pack }
                    )
                }
            }

            if (available.isNotEmpty()) {
                item {
                    SectionHeader(
                        "Mavjud paketlar (${available.size})",
                        modifier = Modifier.padding(top = if (installed.isNotEmpty()) 8.dp else 0.dp)
                    )
                }
                items(available, key = { it.packKey }) { pack ->
                    PackCard(
                        pack = pack,
                        isInstalled = false,
                        isDownloading = pack.packKey in state.downloading,
                        error = state.errors[pack.packKey],
                        onClick = { selectedPack = pack }
                    )
                }
            }

            if (state.isLoading) {
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
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
private fun PackCard(
    pack: ContentPackInfo,
    isInstalled: Boolean,
    isDownloading: Boolean,
    error: String?,
    onClick: () -> Unit
) {
    val subject = Subject.fromCode(pack.subjectCode)
    val gradeLabel = pack.grade?.let { "${it}-sinf" }
        ?: "${pack.gradeMin}-${pack.gradeMax}-sinf"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled)
                Secondary.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Subject emoji badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(subject?.emoji ?: "📦", style = MaterialTheme.typography.titleLarge)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pack.nameUz,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2
                )
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(gradeLabel)
                    InfoChip("${pack.questionCount} savol")
                }
                if (error != null) {
                    Text(
                        error,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            when {
                isInstalled -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Secondary)
                isDownloading -> CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                error != null -> Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                else -> Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackDetailSheet(
    pack: ContentPackInfo,
    isInstalled: Boolean,
    isDownloading: Boolean,
    error: String?,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    val subject = Subject.fromCode(pack.subjectCode)
    val gradeLabel = pack.grade?.let { "${it}-sinf" }
        ?: "${pack.gradeMin}-${pack.gradeMax}-sinf"

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(subject?.emoji ?: "📦", style = MaterialTheme.typography.headlineSmall)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        pack.nameUz,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        subject?.displayName ?: pack.subjectCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            HorizontalDivider()

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("Sinf", gradeLabel)
                StatItem("Savollar", "${pack.questionCount} ta")
                StatItem("Versiya", "v${pack.version}")
            }

            // Description
            if (pack.description.isNotBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Tavsif",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        pack.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Meta info
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Ma'lumot",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                MetaRow("Manba", pack.sourceBook.ifBlank { "—" })
                MetaRow("Til", if (pack.lang == "uz-Latn") "O'zbek (Lotin)" else pack.lang)
                MetaRow("Fayl", pack.driveFileName.ifBlank { "${pack.packKey}.db" })
            }

            // Error
            if (error != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Action button
            if (isInstalled) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Yuklangan")
                }
            } else {
                Button(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isDownloading
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Yuklanmoqda...")
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Yuklab olish")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
