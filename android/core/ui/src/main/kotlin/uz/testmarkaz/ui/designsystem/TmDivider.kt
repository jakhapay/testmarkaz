package uz.testmarkaz.ui.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Divider ────────────────────────────────────────────────────────────────

/**
 * Standard horizontal divider with consistent spacing.
 */
@Composable
fun TmDivider(
    modifier: Modifier = Modifier,
    verticalPadding: Dp = TmSpacing.sm
) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = verticalPadding),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}

// ── Section Header ─────────────────────────────────────────────────────────

/**
 * Section header row with title and optional "See all" action.
 */
@Composable
fun TmSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = TmSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        TmTitle(text = title)
        if (actionLabel != null && onAction != null) {
            TmTextButton(text = actionLabel, onClick = onAction)
        }
    }
}
