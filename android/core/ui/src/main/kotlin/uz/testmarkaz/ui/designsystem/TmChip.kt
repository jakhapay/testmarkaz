package uz.testmarkaz.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ── Topic Chip ─────────────────────────────────────────────────────────────

/**
 * Non-interactive chip for displaying a topic label inside a question card.
 */
@Composable
fun TmTopicChip(
    label: String,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = {},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    )
}

// ── Subject Chip ───────────────────────────────────────────────────────────

/**
 * Coloured pill chip for displaying a subject name.
 * Pass the subject's brand colour as [color].
 */
@Composable
fun TmSubjectChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    if (onClick != null) {
        FilterChip(
            selected = false,
            onClick = onClick,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            },
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = color.copy(alpha = 0.12f),
                labelColor = color
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = false,
                borderColor = color.copy(alpha = 0.3f)
            )
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.12f))
                .padding(horizontal = TmSpacing.sm, vertical = TmSpacing.xxs),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

// ── Status Badge ───────────────────────────────────────────────────────────

/**
 * Small pill badge for pass/fail, streaks, XP, etc.
 *
 * @param text   Badge label e.g. "✓ To'g'ri", "🔥 7 kun"
 * @param color  Background/text tint colour
 */
@Composable
fun TmStatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = TmSpacing.md, vertical = TmSpacing.xxs),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

// ── Grade Chip ─────────────────────────────────────────────────────────────

/**
 * Chip showing a grade/class number (e.g. "9-sinf").
 */
@Composable
fun TmGradeChip(
    grade: Int,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = "$grade-sinf",
                style = MaterialTheme.typography.labelSmall
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp)
    )
}
