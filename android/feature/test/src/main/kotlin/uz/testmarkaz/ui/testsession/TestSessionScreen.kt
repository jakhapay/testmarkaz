package uz.testmarkaz.ui.testsession

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uz.testmarkaz.ui.theme.Error
import uz.testmarkaz.ui.theme.Secondary

@Composable
fun TestSessionScreen(
    sessionId: String,
    onComplete: (String) -> Unit,
    viewModel: TestSessionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val session = state.session ?: return

    val question = session.questions.getOrNull(state.currentIndex) ?: return
    val progress = (state.currentIndex + 1).toFloat() / session.totalQuestions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {

        // ── Progress bar ──────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${state.currentIndex + 1} / ${session.totalQuestions}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Savol ${state.currentIndex + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }

        AnimatedContent(
            targetState = state.currentIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "question_transition"
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Topic chip ─────────────────────────────────────────
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = question.topic,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )

                // ── Question text ──────────────────────────────────────
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = question.questionText,
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                // ── Options ───────────────────────────────────────────
                question.options.forEach { (key, text) ->
                    OptionButton(
                        optionKey = key,
                        optionText = text,
                        selected = state.selectedOption == key,
                        revealed = state.isAnswerRevealed,
                        correctKey = question.correct,
                        onClick = { viewModel.selectOption(key) }
                    )
                }

                // ── Explanation ───────────────────────────────────────
                if (state.isAnswerRevealed && question.explanation.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Secondary.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "📘 Izoh",
                                style = MaterialTheme.typography.labelLarge,
                                color = Secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ── Action button ─────────────────────────────────────
                if (!state.isAnswerRevealed) {
                    Button(
                        onClick = { viewModel.confirmAnswer() },
                        enabled = state.selectedOption != null,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Tekshirish", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    val isLast = state.currentIndex == session.totalQuestions - 1
                    Button(
                        onClick = { viewModel.nextQuestion(onComplete) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            if (isLast) "Natijalarni ko'rish →" else "Keyingi savol →",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun OptionButton(
    optionKey: String,
    optionText: String,
    selected: Boolean,
    revealed: Boolean,
    correctKey: String,
    onClick: () -> Unit
) {
    val isCorrect = optionKey == correctKey
    val containerColor = when {
        revealed && isCorrect -> Secondary.copy(alpha = 0.15f)
        revealed && selected  -> Error.copy(alpha = 0.15f)
        selected              -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else                  -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        revealed && isCorrect -> Secondary
        revealed && selected  -> Error
        selected              -> MaterialTheme.colorScheme.primary
        else                  -> MaterialTheme.colorScheme.outline
    }
    val borderWidth = if (selected || (revealed && isCorrect)) 2.dp else 1.dp

    OutlinedButton(
        onClick = onClick,
        enabled = !revealed,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = optionKey,
                style = MaterialTheme.typography.labelLarge,
                color = borderColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(4.dp)
                    .width(20.dp)
            )
            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (revealed && isCorrect) Text("✓", color = Secondary, fontWeight = FontWeight.Bold)
            if (revealed && selected && !isCorrect) Text("✗", color = Error, fontWeight = FontWeight.Bold)
        }
    }
}
