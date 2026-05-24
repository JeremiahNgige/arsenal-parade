package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ArsenalRed,
    secondary = ArsenalGold,
    tertiary = ArsenalRedLight,
    background = ArsenalCharcoal,
    surface = CardBackgroundDark,
    onPrimary = ArsenalWhite,
    onSecondary = ArsenalCharcoal,
    onTertiary = ArsenalWhite,
    onBackground = ArsenalWhite,
    onSurface = ArsenalWhite
)

private val LightColorScheme = lightColorScheme(
    primary = ArsenalRed,
    secondary = ArsenalGold,
    tertiary = ArsenalMidnight,
    background = ArsenalWhite,
    surface = SoftGray,
    onPrimary = ArsenalWhite,
    onSecondary = ArsenalCharcoal,
    onTertiary = ArsenalWhite,
    onBackground = ArsenalMidnight,
    onSurface = ArsenalMidnight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme as default for the slick stadium aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
