package com.baskaya.isimanaliz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD9B86C),
    onPrimary = Color(0xFF241B08),
    secondary = Color(0xFF73C7B5),
    onSecondary = Color(0xFF08221C),
    tertiary = Color(0xFFA7D8CE),
    background = Color(0xFF07181C),
    onBackground = Color(0xFFE5F0ED),
    surface = Color(0xFF0C1F24),
    onSurface = Color(0xFFE5F0ED),
    surfaceVariant = Color(0xFF173139),
    onSurfaceVariant = Color(0xFFB9CBC7),
    error = Color(0xFFFFB4AB)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF725B12),
    onPrimary = Color.White,
    secondary = Color(0xFF196B5D),
    onSecondary = Color.White,
    background = Color(0xFFF6FBF9),
    onBackground = Color(0xFF12201D),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF12201D),
    surfaceVariant = Color(0xFFDDEBE7),
    onSurfaceVariant = Color(0xFF3E4F4B)
)

@Composable
fun IsimAnalizTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
