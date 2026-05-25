package uz.testmarkaz.ui.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Linear Progress ────────────────────────────────────────────────────────

/**
 * Animated linear progress bar with optional question counter label.
 *
 * @param progress   Value between 0f and 1f
 * @param label      Optional label shown above the bar (e.g. "5 / 25")
 */
@Composable
fun TmLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer,
    height: Dp = 6.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "progress_anim"
    )

    Column(modifier = modifier) {
        if (label != null) {
            TmCaption(
                text = label,
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(Modifier.height(TmSpacing.xs))
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2)),
            color = color,
            trackColor = trackColor,
            strokeCap = StrokeCap.Round
        )
    }
}

// ── Circular Progress ──────────────────────────────────────────────────────

/**
 * Circular progress indicator with a centred score label.
 * Use on the results screen.
 *
 * @param score      Correct answers
 * @param total      Total questions
 * @param size       Diameter of the circle
 */
@Composable
fun TmCircularScore(
    score: Int,
    total: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    passThreshold: Int = 60
) {
    val pct = if (total > 0) score * 100 / total else 0
    val progress = if (total > 0) score.toFloat() / total else 0f
    val color = if (pct >= passThreshold) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.error

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "score_anim"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize(),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeWidth = 10.dp,
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            TmCaption(text = "$score / $total")
        }
    }
}

// ── XP Progress Bar ────────────────────────────────────────────────────────

/**
 * Compact XP progress bar with current / max labels.
 */
@Composable
fun TmXpBar(
    currentXp: Int,
    maxXp: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (maxXp > 0) currentXp.toFloat() / maxXp else 0f

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TmCaption(text = "⭐ $currentXp XP")
            TmCaption(text = "$maxXp XP")
        }
        Spacer(Modifier.height(TmSpacing.xs))
        TmLinearProgress(
            progress = progress,
            color = MaterialTheme.colorScheme.tertiary,
            trackColor = MaterialTheme.colorScheme.tertiaryContainer,
            height = 8.dp
        )
    }
}
