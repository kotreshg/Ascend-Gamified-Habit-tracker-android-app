package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AscendDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1E1400),
    primaryContainer = Color(0xFF382A0C),
    onPrimaryContainer = GoldLight,
    secondary = CyanStudy,
    onSecondary = Color(0xFF001F2A),
    secondaryContainer = Color(0xFF0C3042),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = EmeraldMeditation,
    onTertiary = Color(0xFF002114),
    tertiaryContainer = Color(0xFF063B25),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorderSubtle
)

private val AscendLightColorScheme = lightColorScheme(
    primary = GoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = CyanStudyDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = EmeraldMeditationDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = Color(0xFFCBD5E1)
)

@Composable
fun AscendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AscendDarkColorScheme else AscendLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AscendTypography,
        content = content
    )
}
