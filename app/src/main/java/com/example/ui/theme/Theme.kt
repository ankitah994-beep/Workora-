package com.workora.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val WorkoraLightColors = lightColorScheme(
    primary = WorkoraBlue,
    onPrimary = WorkoraSurface,
    primaryContainer = WorkoraBlueContainer,
    onPrimaryContainer = OnWorkoraBlueContainer,
    secondary = WorkoraBlueDark,
    onSecondary = WorkoraSurface,
    secondaryContainer = WorkoraSky,
    onSecondaryContainer = OnWorkoraSky,
    background = WorkoraBackground,
    onBackground = WorkoraOnSurface,
    surface = WorkoraSurface,
    onSurface = WorkoraOnSurface,
    surfaceVariant = WorkoraSurfaceVariant,
    onSurfaceVariant = WorkoraOnSurfaceVariant,
    outline = WorkoraOutline,
    error = WorkoraError,
    onError = WorkoraOnError
)

private val WorkoraShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

/**
 * Blue and white theme for Workora.
 *
 * The theme is light-only on purpose (a consistent blue/white look). The darkTheme parameter is
 * kept, and ignored, so that calls such as WorkoraTheme(darkTheme = ...) still compile.
 */
@Composable
fun WorkoraTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WorkoraLightColors,
        typography = WorkoraTypography,
        shapes = WorkoraShapes,
        content = content
    )
}
