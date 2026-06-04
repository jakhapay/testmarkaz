package uz.testmarkaz.ui.testconfig

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.testmarkaz.domain.model.Subject
import uz.testmarkaz.domain.model.TestMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestConfigScreen(
    onSessionReady: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: TestConfigViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val effect by viewModel.effects.collectAsState()

    LaunchedEffect(effect) {
        when (val e = effect) {
            is TestConfigEffect.NavigateToSession -> {
                viewModel.clearEffect()
                onSessionReady(e.sessionId)
            }
            null -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Test sozlamalari") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Orqaga")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ── Test mode ──────────────────────────────────────────────
            Text("Test turi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            TestModeSelector(
                selected = state.mode,
                onSelect = viewModel::selectMode
            )

            // ── Subject picker (hide for FULL_RANDOM) ──────────────────
            if (state.mode != TestMode.FULL_RANDOM && state.mode != TestMode.RANDOM_CLASS) {
                Text("Fan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                SubjectDropdown(
                    selected = state.selectedSubject,
                    onSelect = viewModel::selectSubject
                )
            }

            // ── Grade picker ───────────────────────────────────────────
            when (state.mode) {
                TestMode.SUBJECT, TestMode.RANDOM_CLASS -> {
                    Text("Sinf", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    GradeChipRow(
                        selected = state.selectedGrade,
                        onSelect = viewModel::selectGrade
                    )
                }
                TestMode.RANGE -> {
                    Text("Sinf diapazoni", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    GradeRangePicker(
                        min = state.gradeMin,
                        max = state.gradeMax,
                        onMinChange = viewModel::selectGradeMin,
                        onMaxChange = viewModel::selectGradeMax
                    )
                }
                TestMode.FULL_RANDOM -> Unit
                TestMode.PDF_PACK -> Unit
            }

            // ── Error ──────────────────────────────────────────────────
            state.errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Start button ───────────────────────────────────────────
            Button(
                onClick = viewModel::startTest,
                enabled = !state.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Savollar tayyorlanmoqda…")
                } else {
                    Text(
                        "25 ta savol boshlansin →",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TestModeSelector(selected: TestMode, onSelect: (TestMode) -> Unit) {
    val modes = listOf(
        TestMode.SUBJECT      to "Fan bo'yicha",
        TestMode.RANGE        to "Sinf diapazoni",
        TestMode.RANDOM_CLASS to "Tasodifiy sinf",
        TestMode.FULL_RANDOM  to "To'liq tasodifiy"
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        modes.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (mode, label) ->
                    val isSelected = selected == mode
                    OutlinedButton(
                        onClick = { onSelect(mode) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                // pad if odd
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectDropdown(selected: Subject, onSelect: (Subject) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = "${selected.emoji}  ${selected.displayName}",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Subject.entries.forEach { subject ->
                DropdownMenuItem(
                    text = { Text("${subject.emoji}  ${subject.displayName}") },
                    onClick = { onSelect(subject); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun GradeChipRow(selected: Int, onSelect: (Int) -> Unit) {
    val grades = 1..11
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        grades.chunked(6).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { grade ->
                    FilterChip(
                        selected = selected == grade,
                        onClick = { onSelect(grade) },
                        label = { Text("$grade-sinf") }
                    )
                }
            }
        }
    }
}

@Composable
private fun GradeRangePicker(
    min: Int, max: Int,
    onMinChange: (Int) -> Unit,
    onMaxChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Dan:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(48.dp))
            Slider(
                value = min.toFloat(),
                onValueChange = { onMinChange(it.toInt()) },
                valueRange = 1f..11f,
                steps = 9,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$min-sinf",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(64.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Gacha:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(48.dp))
            Slider(
                value = max.toFloat(),
                onValueChange = { onMaxChange(it.toInt()) },
                valueRange = 1f..11f,
                steps = 9,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$max-sinf",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(64.dp)
            )
        }
    }
}
