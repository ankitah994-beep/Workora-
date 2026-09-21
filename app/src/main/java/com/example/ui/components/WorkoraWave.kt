package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraOrange

@Composable
fun WorkoraWave(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    bottomFillColor: Color = Color.White
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height
        val sx = w / 400f
        val sy = h / 120f

        // Top semi-transparent orange wave
        val orangeWavePath = Path().apply {
            moveTo(0f, 45f * sy)
            cubicTo(
                90f * sx, 105f * sy,
                180f * sx, 15f * sy,
                280f * sx, 75f * sy
            )
            cubicTo(
                340f * sx, 110f * sy,
                380f * sx, 45f * sy,
                400f * sx, 60f * sy
            )
            lineTo(400f * sx, 120f * sy)
            lineTo(0f, 120f * sy)
            close()
        }
        drawPath(path = orangeWavePath, color = WorkoraOrange.copy(alpha = 0.22f))

        // Foreground bottom wave
        val whiteWavePath = Path().apply {
            moveTo(0f, 65f * sy)
            cubicTo(
                110f * sx, 125f * sy,
                210f * sx, 35f * sy,
                295f * sx, 90f * sy
            )
            cubicTo(
                345f * sx, 120f * sy,
                380f * sx, 85f * sy,
                400f * sx, 90f * sy
            )
            lineTo(400f * sx, 120f * sy)
            lineTo(0f, 120f * sy)
            close()
        }
        drawPath(path = whiteWavePath, color = bottomFillColor)
    }
}
