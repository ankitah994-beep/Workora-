package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = WorkoraOrange,
    secondary = WorkoraNavy,
    background = WorkoraBgLight,
    surface = Color.White, // Yahan WorkoraCardBg ki jagah Color.White kar diya hai
    onSurface = WorkoraTextDark,
    onBackground = WorkoraTextDark
)

@Composable
fun WorkoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
