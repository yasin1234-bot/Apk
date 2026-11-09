package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealLightContainer,
    onPrimaryContainer = TealOnContainer,
    secondary = TealSecondary,
    onSecondary = Color.White,
    secondaryContainer = TealSecondaryContainer,
    onSecondaryContainer = TealOnContainer,
    background = LightPageBackground,
    onBackground = TextPrimarySlate,
    surface = CardSurfaceWhite,
    onSurface = TextPrimarySlate,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = TextSecondarySlate,
    outline = Color(0xFFCBD5E1),
    error = StatusDisconnectedRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealDark,
    onPrimaryContainer = TealLightContainer,
    secondary = TealSecondary,
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

@Composable
fun PyBinaryShieldTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
