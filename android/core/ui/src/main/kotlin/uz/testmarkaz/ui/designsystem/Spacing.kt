package uz.testmarkaz.ui.designsystem

import androidx.compose.ui.unit.dp

/**
 * TestMarkaz spacing tokens.
 * Always use these instead of raw dp values to keep layout consistent.
 */
object TmSpacing {
    val xxs  = 2.dp
    val xs   = 4.dp
    val sm   = 8.dp
    val md   = 12.dp
    val lg   = 16.dp
    val xl   = 20.dp
    val xxl  = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp

    /** Standard horizontal screen padding */
    val screenHorizontal = lg

    /** Standard vertical content padding */
    val screenVertical = lg

    /** Gap between list items */
    val listGap = md

    /** Gap between cards in a row */
    val cardGap = md
}
