package uz.testmarkaz.ui.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ── Empty State ────────────────────────────────────────────────────────────

/**
 * Full empty-state block with emoji, title, subtitle and optional CTA button.
 *
 * Example usage:
 * ```
 * TmEmptyState(
 *     emoji = "📚",
 *     title = "No tests yet",
 *     subtitle = "Start your first test now!",
 *     actionLabel = "Start Test",
 *     onAction = { onStartTest() }
 * )
 * ```
 */
@Composable
fun TmEmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    TmCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(TmSpacing.xxxl)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TmSpacing.sm)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(TmSpacing.xs))
            TmTitle(
                text = title,
                textAlign = TextAlign.Center
            )
            TmBodySmall(
                text = subtitle,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(TmSpacing.sm))
                TmPrimaryButton(
                    text = actionLabel,
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
            }
        }
    }
}

// ── Loading State ──────────────────────────────────────────────────────────

/**
 * Centered loading spinner with an optional message.
 */
@Composable
fun TmLoadingState(
    modifier: Modifier = Modifier,
    message: String = "Yuklanmoqda..."
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmSpacing.md)
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
        TmBodySmall(text = message)
    }
}

// ── Error State ────────────────────────────────────────────────────────────

/**
 * Error state with retry button.
 */
@Composable
fun TmErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    TmInfoCard(
        title = "Xatolik yuz berdi",
        body = message,
        modifier = modifier.fillMaxWidth(),
        accentColor = MaterialTheme.colorScheme.error,
        icon = "⚠️"
    )
    if (onRetry != null) {
        Spacer(Modifier.height(TmSpacing.sm))
        TmOutlineButton(
            text = "Qayta urinish",
            onClick = onRetry
        )
    }
}
