package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val WorkoraNavy = Color(0xFF0F172A)
val WorkoraOrange = Color(0xFFF97316)
val WorkoraBgLight = Color(0xFFF8FAFC)
val WorkoraCardBg = Color(0xFFFFFFFF)
val WorkoraBorder = Color(0xFFE2E8F0)
val WorkoraTextDark = Color(0xFF0F172A)
val WorkoraTextMuted = Color(0xFF64748B)

private val LightColorScheme = lightColorScheme(
    primary = WorkoraOrange,
    secondary = WorkoraNavy,
    background = WorkoraBgLight,
    surface = WorkoraCardBg,
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
