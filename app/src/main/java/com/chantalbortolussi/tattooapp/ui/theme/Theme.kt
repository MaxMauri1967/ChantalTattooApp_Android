package com.chantalbortolussi.tattooapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    secondary = GoldLight,
    tertiary = GoldDark,
    background = CharcoalBackground,
    surface = CardBackground,
    onPrimary = CharcoalBackground,
    onSecondary = CharcoalBackground,
    onTertiary = CharcoalBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun ChantalTattooAppTheme(content: @Composable () -> Unit) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
