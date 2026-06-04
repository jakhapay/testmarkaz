package uz.testmarkaz.ui.pdfimport

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.testmarkaz.domain.usecase.GenerateQuestionsFromPdfUseCase.Stage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfImportScreen(
    onBack: () -> Unit,
    onSessionReady: (String) -> Unit,
    viewModel: PdfImportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val effect by viewModel.effects.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(effect) {
        (effect as? PdfImportEffect.NavigateToSession)?.let {
            viewModel.clearEffect()
            onSessionReady(it.sessionId)
        }
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0 && c.moveToFirst()) c.getString(idx) else null
            } ?: "hujjat.pdf"
            viewModel.onPdfPicked(uri, name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF dan test") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Orqaga")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "PDF kitob yoki konspekt yuklang — ilova undan avtomatik test tuzadi. " +
                    "Hammasi qurilmada, oflayn ishlaydi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            when {
                state.isWorking -> WorkingCard(state.stage, state.fileName)
                state.result != null -> ResultCard(
                    fileName = state.fileName,
                    count = state.result!!.questionCount,
                    onStartTest = { viewModel.startTestFromPack() },
                    onAnother = { viewModel.reset() }
                )
                state.error != null -> ErrorCard(state.error!!) { viewModel.reset() }
                else -> {
                    Button(
                        onClick = { picker.launch(arrayOf("application/pdf")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("PDF tanlash", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkingCard(stage: Stage?, fileName: String) {
    val label = when (stage) {
        Stage.EXTRACTING -> "Matn ajratilmoqda…"
        Stage.CHUNKING -> "Matn bo'linmoqda…"
        Stage.GENERATING -> "Savollar tuzilmoqda…"
        Stage.VALIDATING -> "Savollar tekshirilmoqda…"
        Stage.SAVING -> "Saqlanmoqda…"
        Stage.DONE, null -> "Tayyorlanmoqda…"
    }
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (fileName.isNotBlank()) {
                Text(fileName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ResultCard(
    fileName: String,
    count: Int,
    onStartTest: () -> Unit,
    onAnother: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Text(
                "$count ta savol tayyor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                fileName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                "Savollar qurilmadagi AI tomonidan yaratilgan va xato bo'lishi mumkin.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onStartTest,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Test boshlash", fontWeight = FontWeight.SemiBold) }
            TextButton(onClick = onAnother) { Text("Boshqa PDF yuklash") }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onRetry) { Text("Qayta urinish") }
        }
    }
}
