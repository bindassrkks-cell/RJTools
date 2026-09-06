package com.rjtool.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkCardBg = Color(0xFF242424)
val DarkDialogBg = Color(0xFF2C2C2C)
val AccentTeal = Color(0xFF00BFA5)
val ButtonGreen = Color(0xFF00897B)
val ButtonDisabled = Color(0xFF384743)
val TextPrimary = Color(0xFFEEEEEE)
val TextSecondary = Color(0xFF9E9E9E)
val ErrorRed = Color(0xFFEF5350)

private val DarkColorScheme = darkColorScheme(
    primary = ButtonGreen,
    secondary = AccentTeal,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun RJTOOLTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}
