package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MangaPalette(
    val paper: Color,
    val panelSurface: Color,
    val panelSecondary: Color,
    val ink: Color,
    val muted: Color,
    val crimson: Color,
    val green: Color,
    val yellowHighlight: Color,
    val notebookPaper: Color,
    val notebookLine: Color,
    val halftoneDot: Color,
    val shadow: Color,
    val onCrimson: Color = Color.White,
    val onGreen: Color = Color.White,
    val onInk: Color
)

private val LightMangaPalette = MangaPalette(
    paper = MangaPaper,
    panelSurface = MangaSurface,
    panelSecondary = MangaSurfaceVariant,
    ink = MangaInk,
    muted = MangaMuted,
    crimson = MangaCrimson,
    green = MangaGreen,
    yellowHighlight = MangaYellow,
    notebookPaper = MangaNotebook,
    notebookLine = MangaNotebookLine,
    halftoneDot = MangaHalftone,
    shadow = MangaShadow,
    onInk = Color.White
)

private val DarkMangaPalette = MangaPalette(
    paper = DarkMangaPaper,
    panelSurface = DarkMangaSurface,
    panelSecondary = DarkMangaSurfaceVariant,
    ink = DarkMangaInk,
    muted = DarkMangaMuted,
    crimson = DarkMangaCrimson,
    green = DarkMangaGreen,
    yellowHighlight = DarkMangaYellow,
    notebookPaper = DarkMangaNotebook,
    notebookLine = DarkMangaNotebookLine,
    halftoneDot = DarkMangaHalftone,
    shadow = DarkMangaShadow,
    onInk = Color(0xFF111111)
)

val LocalMangaPalette = staticCompositionLocalOf { LightMangaPalette }

private val LightColorScheme = lightColorScheme(
    primary = MangaCrimson,
    onPrimary = Color.White,
    secondary = MangaInk,
    onSecondary = Color.White,
    tertiary = MangaGreen,
    onTertiary = Color.White,
    background = MangaPaper,
    onBackground = MangaInk,
    surface = MangaSurface,
    onSurface = MangaInk,
    surfaceVariant = MangaSurfaceVariant,
    onSurfaceVariant = MangaMuted,
    outline = MangaInk
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkMangaCrimson,
    onPrimary = Color.White,
    secondary = DarkMangaInk,
    onSecondary = Color(0xFF111111),
    tertiary = DarkMangaGreen,
    onTertiary = Color.White,
    background = DarkMangaPaper,
    onBackground = DarkMangaInk,
    surface = DarkMangaSurface,
    onSurface = DarkMangaInk,
    surfaceVariant = DarkMangaSurfaceVariant,
    onSurfaceVariant = DarkMangaMuted,
    outline = DarkMangaInk
)

@Composable
fun RhajeshMatchTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val mangaPalette = if (darkTheme) DarkMangaPalette else LightMangaPalette

    CompositionLocalProvider(LocalMangaPalette provides mangaPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MangaTypography,
            content = content
        )
    }
}
