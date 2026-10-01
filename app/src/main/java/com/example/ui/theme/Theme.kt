package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VintageHiFiColorScheme = darkColorScheme(
    primary = VintageBrass,
    onPrimary = Color(0xFF1A1406),
    primaryContainer = PrimaryContainerBronze,
    onPrimaryContainer = OnPrimaryContainerBronze,
    secondary = AmberTubeGlow,
    onSecondary = Color(0xFF1F1202),
    secondaryContainer = AmberGlowDim,
    onSecondaryContainer = Color(0xFFFFDDB8),
    tertiary = VintageBrassLight,
    onTertiary = Color(0xFF221A04),
    background = VinylBlack,
    onBackground = CreamIvory,
    surface = ChassisDark,
    onSurface = CreamIvory,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = MutedIvory,
    outline = DarkMuted,
    outlineVariant = Color(0xFF38342E)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VintageHiFiColorScheme,
        typography = AppTypography,
        content = content
    )
}

