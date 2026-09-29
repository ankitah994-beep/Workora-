package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private val SplashBg = Color(0xFFF8FAFC)
private val SplashNavy = Color(0xFF083D91)
private val SplashOrange = Color(0xFFFF8C00)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(1800L)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(96.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        color = SplashOrange,
                        radius = w * 0.46f,
                        style = Stroke(width = w * 0.06f)
                    )
                    val path = Path().apply {
                        moveTo(w * 0.24f, h * 0.36f)
                        lineTo(w * 0.37f, h * 0.66f)
                        lineTo(w * 0.50f, h * 0.44f)
                        lineTo(w * 0.63f, h * 0.66f)
                        lineTo(w * 0.76f, h * 0.36f)
                    }
                    drawPath(
                        path = path,
                        color = SplashNavy,
                        style = Stroke(width = w * 0.085f, cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WORKORA",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SplashNavy,
                letterSpacing = 2.sp
            )
            Text(
                text = "Find. Hire. Work.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF667085)
            )
        }
    }
}
