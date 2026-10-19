package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AutoClipDarkColorScheme = darkColorScheme(
    primary = CyberCyanBright,
    onPrimary = Color(0xFF032329),
    primaryContainer = CyberCyanContainer,
    onPrimaryContainer = Color(0xFFCFFAFE),
    secondary = ElectricIndigo,
    onSecondary = Color.White,
    secondaryContainer = ElectricIndigoContainer,
    onSecondaryContainer = OnElectricIndigoContainer,
    tertiary = ViralAmber,
    onTertiary = Color(0xFF281900),
    background = StudioAbyss,
    onBackground = TextPrimaryDark,
    surface = StudioSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = StudioSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = StudioCardBorder,
    error = ViralRose,
    onError = Color.White
)

private val AutoClipLightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = CyberCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFFAFE),
    onSecondaryContainer = Color(0xFF083344),
    tertiary = ViralAmber,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1),
    error = ViralRose
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AutoClipDarkColorScheme else AutoClipLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
