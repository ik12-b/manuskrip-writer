package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ManuScribeColorScheme = darkColorScheme(
    primary = GoldAmber,
    onPrimary = ObsidianBg,
    primaryContainer = GoldAmberDark,
    onPrimaryContainer = GoldAmberLight,
    secondary = IndigoVioletLight,
    onSecondary = ObsidianBg,
    secondaryContainer = IndigoViolet,
    onSecondaryContainer = TextPrimary,
    tertiary = GoldAmberLight,
    onTertiary = ObsidianBg,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SlateParchmentBg,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = SurfaceBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // ManuScribe uses the dedicated dark obsidian parchment aesthetic
    MaterialTheme(
        colorScheme = ManuScribeColorScheme,
        typography = Typography,
        content = content
    )
}
