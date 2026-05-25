package uz.testmarkaz.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ── Base Surface Card ──────────────────────────────────────────────────────

/**
 * Base card surface. Wraps any content with standard elevation and shape.
 */
@Composable
fun TmCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = content
    )
}

// ── Stat Card ──────────────────────────────────────────────────────────────

/**
 * Small stat display card with emoji, value and label.
 * Use in the home screen stats row (streak, XP, tests count).
 */
@Composable
fun TmStatCard(
    value: String,
    label: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    TmCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .padding(TmSpacing.md)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TmSpacing.xs)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineSmall
            )
            TmLabel(
                text = value,
                color = MaterialTheme.colorScheme.primary
            )
            TmCaption(text = label)
        }
    }
}

// ── Quick Action Card ──────────────────────────────────────────────────────

/**
 * Tappable card with an icon and label. Use for quick navigation shortcuts.
 */
@Composable
fun TmQuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    TmCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(TmSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TmSpacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            TmTitle(text = title)
        }
    }
}

// ── Result Score Card ──────────────────────────────────────────────────────

/**
 * Circular score badge. Use in recent sessions list and results screen.
 *
 * @param pct Score percentage 0–100
 */
@Composable
fun TmScoreBadge(
    pct: Int,
    modifier: Modifier = Modifier,
    passThreshold: Int = 60
) {
    val color = if (pct >= passThreshold) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.error
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        TmLabel(
            text = "$pct%",
            color = color
        )
    }
}

// ── Info Card ─────────────────────────────────────────────────────────────

/**
 * Highlighted info/explanation card with a coloured left accent and optional icon.
 */
@Composable
fun TmInfoCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.secondary,
    icon: String = "📘"
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(TmSpacing.lg)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TmSpacing.xs)
            ) {
                Text(text = icon, style = MaterialTheme.typography.labelLarge)
                TmLabel(text = title, color = accentColor)
            }
            Spacer(Modifier.height(TmSpacing.xs))
            TmBody(text = body)
        }
    }
}

// ── Question Card ──────────────────────────────────────────────────────────

/**
 * Card for displaying a test question text.
 */
@Composable
fun TmQuestionCard(
    questionText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        TmBody(
            text = questionText,
            modifier = Modifier.padding(TmSpacing.xl),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
