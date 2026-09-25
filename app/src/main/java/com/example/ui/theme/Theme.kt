package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Exact Original Workora Theme Colors converted from styles.css (:root)
val WorkoraNavy = Color(0xFF083D91)          // --navy: #083D91
val WorkoraNavyLight = Color(0xFF0B4AA4)     // .wave-blue: #0b4aa4
val WorkoraOrange = Color(0xFFFF8C00)        // --orange: #FF8C00
val WorkoraOrangeLight = Color(0xFFFFF0DE)   // .category:nth-child(2n): #fff0de
val WorkoraWhite = Color(0xFFFFFFFF)         // --white: #fff
val WorkoraBgLight = Color(0xFFF8FAFC)       // --bg: #F8FAFC
val WorkoraShellBg = Color(0xFFE9EEF5)       // body background: #e9eef5
val WorkoraTextDark = Color(0xFF102A43)      // --text: #102A43
val WorkoraTextMuted = Color(0xFF667085)     // --muted: #667085
val WorkoraBorder = Color(0xFFE5E7EB)        // --border: #E5E7EB
val WorkoraSuccessGreen = Color(0xFF22A06B)  // --green: #22A06B
val WorkoraErrorRed = Color(0xFFB42318)      // reject/danger text: #b42318
val WorkoraPendingBg = Color(0xFFFFF0C9)     // .badge.pending: #fff0c9
val WorkoraPendingText = Color(0xFF9B6A00)   // .badge.pending text: #9b6a00
val WorkoraChipBg = Color(0xFFEDF3FF)        // .chips span: #edf3ff

private val WorkoraLightColorScheme = lightColorScheme(
    primary = WorkoraNavy,
    onPrimary = WorkoraWhite,
    secondary = WorkoraOrange,
    onSecondary = WorkoraWhite,
    tertiary = WorkoraSuccessGreen,
    background = WorkoraBgLight,
    onBackground = WorkoraTextDark,
    surface = WorkoraWhite,
    onSurface = WorkoraTextDark,
    surfaceVariant = WorkoraChipBg,
    onSurfaceVariant = WorkoraTextMuted,
    outline = WorkoraBorder,
    error = WorkoraErrorRed,
    onError = WorkoraWhite
)

@Composable
fun WorkoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WorkoraLightColorScheme,
        content = content
    )
}
