package uz.testmarkaz.ui.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * TestMarkaz shape tokens.
 */
object TmShape {
    /** Small elements: chips, badges, tags */
    val small  = RoundedCornerShape(8.dp)

    /** Medium elements: input fields, snackbars */
    val medium = RoundedCornerShape(12.dp)

    /** Large elements: cards, bottom sheets */
    val large  = RoundedCornerShape(16.dp)

    /** Extra large: modal sheets, dialogs */
    val xLarge = RoundedCornerShape(24.dp)

    /** Fully rounded: buttons, FABs, pills */
    val full   = CircleShape
}
