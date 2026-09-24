package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Note: Yahan koi 'val WorkoraOrange = Color(...)' nahi likhna hai 
// kyunki wo pehle se aapki Color.kt file mein maujood hain.

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
