package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraOrangeDark
import com.example.ui.theme.WorkoraOrangeLight

@Composable
fun WorkoraHelmetLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showHalo: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val scaleX = w / 100f
            val scaleY = h / 100f

            // Halo glow behind helmet
            if (showHalo) {
                drawCircle(
                    color = Color(0x33FF8C00),
                    radius = 48f * scaleX * haloScale,
                    center = Offset(53f * scaleX, 42f * scaleY)
                )
                drawCircle(
                    color = Color(0x18FF8C00),
                    radius = 62f * scaleX * haloScale,
                    center = Offset(53f * scaleX, 42f * scaleY)
                )
            }

            // 1. Helmet Dome (Main body)
            val domePath = Path().apply {
                moveTo(18f * scaleX, 56f * scaleY)
                cubicTo(
                    18f * scaleX, 36.67f * scaleY,
                    33.67f * scaleX, 21f * scaleY,
                    53f * scaleX, 21f * scaleY
                )
                cubicTo(
                    72.33f * scaleX, 21f * scaleY,
                    88f * scaleX, 36.67f * scaleY,
                    88f * scaleX, 56f * scaleY
                )
                close()
            }
            drawPath(path = domePath, color = WorkoraOrange)

            // 2. Helmet Top Ridge
            val ridgePath = Path().apply {
                moveTo(47f * scaleX, 16f * scaleY)
                lineTo(59f * scaleX, 16f * scaleY)
                lineTo(59f * scaleX, 32f * scaleY)
                cubicTo(
                    59f * scaleX, 32f * scaleY,
                    55.5f * scaleX, 33.2f * scaleY,
                    53f * scaleX, 33.2f * scaleY
                )
                cubicTo(
                    50.5f * scaleX, 33.2f * scaleY,
                    47f * scaleX, 32f * scaleY,
                    47f * scaleX, 32f * scaleY
                )
                close()
            }
            drawPath(path = ridgePath, color = WorkoraOrangeLight)

            // 3. Front Brim / Visor Base
            val visorPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 12f * scaleX,
                        top = 52f * scaleY,
                        right = 94f * scaleX,
                        bottom = 60f * scaleY,
                        cornerRadius = CornerRadius(4f * scaleX, 4f * scaleY)
                    )
                )
            }
            drawPath(path = visorPath, color = WorkoraOrangeDark)

            // 4. Reflector Badge
            val badgePath = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 46f * scaleX,
                        top = 38f * scaleY,
                        right = 60f * scaleX,
                        bottom = 45f * scaleY,
                        cornerRadius = CornerRadius(3.5f * scaleX, 3.5f * scaleY)
                    )
                )
            }
            drawPath(path = badgePath, color = Color.White.copy(alpha = 0.95f))
        }
    }
}
