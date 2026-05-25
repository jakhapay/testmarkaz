package uz.testmarkaz.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.testmarkaz.domain.model.AnswerDetail
import uz.testmarkaz.domain.model.TestResult
import uz.testmarkaz.ui.theme.Error
import uz.testmarkaz.ui.theme.Secondary
import uz.testmarkaz.ui.theme.Warning

@Composable
fun ResultsScreen(
    sessionId: String,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val result by viewModel.result.collectAsState()

    result?.let { r ->
        ResultsContent(result = r, onRetry = onRetry, onHome = onHome)
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ResultsContent(
    result: TestResult,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    val scoreColor = when {
        result.percentage >= 80 -> Secondary
        result.percentage >= 60 -> Warning
        else -> Error
    }
    val emoji = when {
        result.percentage >= 90 -> "🏆"
        result.percentage >= 80 -> "🎉"
        result.percentage >= 60 -> "👍"
        result.percentage >= 40 -> "😅"
        else -> "📚"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── Score card ─────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = scoreColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = emoji, fontSize = 64.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${result.percentage}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = scoreColor
                    )
                    Text(
                        text = "${result.score} / ${result.total} to'g'ri javob",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (result.passed) "✅ Muvaffaqiyatli o'tdingiz!" else "❌ Qayta urinib ko'ring",
                        style = MaterialTheme.typography.bodyLarge,
                        color = scoreColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Stats row ──────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniStatCard("⏱️", "${result.durationSeconds / 60}:${"%02d".format(result.durationSeconds % 60)}", "Vaqt", Modifier.weight(1f))
                MiniStatCard("✅", "${result.score}", "To'g'ri", Modifier.weight(1f))
                MiniStatCard("❌", "${result.total - result.score}", "Noto'g'ri", Modifier.weight(1f))
            }
        }

        // ── Action buttons ─────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onHome,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Home, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Bosh sahifa")
                }
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Yana urinish")
                }
            }
        }

        // ── Answer review ──────────────────────────────────────────────
        item {
            Text(
                text = "Javoblarni ko'rib chiqish",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(result.answers) { detail ->
            AnswerReviewCard(detail = detail)
        }
    }
}

@Composable
private fun MiniStatCard(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun AnswerReviewCard(detail: AnswerDetail) {
    val color = if (detail.isCorrect) Secondary else Error
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.3f))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (detail.isCorrect) "✅" else "❌")
                Text(
                    text = detail.question.questionText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
            if (!detail.isCorrect) {
                Text(
                    text = "Sizning javobingiz: ${detail.selectedOption} — ${detail.question.options[detail.selectedOption] ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Error
                )
            }
            Text(
                text = "To'g'ri javob: ${detail.question.correct} — ${detail.question.options[detail.question.correct] ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = Secondary,
                fontWeight = FontWeight.Medium
            )
            if (detail.question.explanation.isNotBlank()) {
                Text(
                    text = "💡 ${detail.question.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
