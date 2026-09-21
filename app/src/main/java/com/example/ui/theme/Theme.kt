package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = WorkoraNavyLight,
    onPrimary = Color.White,
    primaryContainer = WorkoraNavyDark,
    onPrimaryContainer = WorkoraTaglineBlue,
    secondary = WorkoraOrange,
    onSecondary = Color.White,
    secondaryContainer = WorkoraOrangeDark,
    onSecondaryContainer = WorkoraOrangeSoft,
    background = Color(0xFF0B1324),
    surface = Color(0xFF131D33),
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = WorkoraNavy,
    onPrimary = Color.White,
    primaryContainer = WorkoraNavySoft,
    onPrimaryContainer = WorkoraNavyDark,
    secondary = WorkoraOrange,
    onSecondary = Color.White,
    secondaryContainer = WorkoraOrangeSoft,
    onSecondaryContainer = WorkoraOrangeDark,
    background = WorkoraBgLight,
    surface = WorkoraSurface,
    onBackground = WorkoraTextDark,
    onSurface = WorkoraTextDark,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = WorkoraTextMuted,
    outline = WorkoraBorder,
  )

@Composable
fun WorkoraTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Prefer our brand colors for strong brand identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Keep alias for backwards compatibility
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  WorkoraTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

