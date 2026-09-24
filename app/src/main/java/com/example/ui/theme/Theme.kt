package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val WorkoraNavy = Color(0xFF0F172A)
val WorkoraNavyDark = Color(0xFF020617)
val WorkoraNavySoft = Color(0xFF1E293B)
val WorkoraOrange = Color(0xFFF97316)
val WorkoraBgLight = Color(0xFFF8FAFC)
val WorkoraCardBg = Color(0xFFFFFFFF)
val WorkoraBorder = Color(0xFFE2E8F0)
val WorkoraTextDark = Color(0xFF0F172A)
val WorkoraTextMuted = Color(0xFF64748B)
val WorkoraSuccess = Color(0xFF16A34A)
val WorkoraWarning = Color(0xFFEAB308)

private val LightColorScheme = lightColorScheme(
    primary = WorkoraOrange,
    secondary = WorkoraNavy,
    background = WorkoraBgLight,
    surface = WorkoraCardBg,
    onSurface = WorkoraTextDark, // Isse typing text hamesha dark aur visible rahega
    onBackground = WorkoraTextDark
)

@Composable
fun WorkoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
