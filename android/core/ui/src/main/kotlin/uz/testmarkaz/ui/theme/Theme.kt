package uz.testmarkaz.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = androidx.compose.ui.graphics.Color.White,
    primaryContainer   = PrimaryLight,
    onPrimaryContainer = PrimaryDark,
    secondary          = Secondary,
    onSecondary        = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = SecondaryLight,
    error              = Error,
    errorContainer     = ErrorLight,
    background         = Gray50,
    surface            = androidx.compose.ui.graphics.Color.White,
    onSurface          = Gray900,
    onBackground       = Gray900,
    outline            = Gray300
)

private val DarkColorScheme = darkColorScheme(
    primary            = PrimaryLight,
    onPrimary          = PrimaryDark,
    primaryContainer   = PrimaryDark,
    onPrimaryContainer = PrimaryLight,
    secondary          = SecondaryLight,
    onSecondary        = Gray900,
    secondaryContainer = Secondary,
    error              = ErrorLight,
    errorContainer     = Error,
    background         = Gray900,
    surface            = Gray700,
    onSurface          = Gray100,
    onBackground       = Gray100,
    outline            = Gray500
)

@Composable
fun TestMarkazTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
