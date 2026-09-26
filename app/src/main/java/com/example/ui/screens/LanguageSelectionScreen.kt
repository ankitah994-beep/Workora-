package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WelcomeNavyPrimary = Color(0xFF083D91)
private val WelcomeNavyDeep = Color(0xFF052963)
private val WelcomeOrangeAccent = Color(0xFFFF8C00)
private val WelcomeWhite = Color(0xFFFFFFFF)

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A449E),
                        WelcomeNavyPrimary,
                        Color(0xFF07357F)
                    )
                )
            )
    ) {
        // Bottom Reference Blue & Orange Wave Design
        WelcomeBottomWaveCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .align(Alignment.BottomCenter)
        )

        // Center Content: Helmet Logo + Welcome to Workora + Tagline + Continue Button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.8f))

            // Official Workora Orange Hard-Hat Logo (Exact Match to Reference Image)
            WorkoraOfficialHelmetCanvas(size = 112.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome to Workora",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFFB74D),
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Workora",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WelcomeWhite,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Find. Hire. Work.",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFE2E8F0),
                letterSpacing = 0.6.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Continue Button
            Button(
                onClick = onLanguageSelected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WelcomeOrangeAccent,
                    contentColor = WelcomeWhite
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "Continue",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = WelcomeWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Continue",
                    tint = WelcomeWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
private fun WorkoraOfficialHelmetCanvas(size: Dp = 112.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Helmet Dome
        drawArc(
            color = WelcomeOrangeAccent,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.10f, h * 0.18f),
            size = Size(w * 0.80f, h * 0.82f)
        )

        // Top Center Crown Ridge
        drawRoundRect(
            color = WelcomeOrangeAccent,
            topLeft = Offset(w * 0.43f, h * 0.11f),
            size = Size(w * 0.14f, h * 0.15f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        // Two Navy Vertical Rib Slots on Helmet Dome (Matches Reference Image)
        drawRoundRect(
            color = WelcomeNavyPrimary,
            topLeft = Offset(w * 0.37f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = WelcomeNavyPrimary,
            topLeft = Offset(w * 0.585f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Helmet Bottom Brim
        drawRoundRect(
            color = WelcomeOrangeAccent,
            topLeft = Offset(w * 0.03f, h * 0.56f),
            size = Size(w * 0.94f, h * 0.11f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )
    }
}

@Composable
private fun WelcomeBottomWaveCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Orange Accent Wave Rising on the Right
        val orangeWavePath = Path().apply {
            moveTo(w * 0.32f, h * 0.68f)
            cubicTo(
                w * 0.58f, h * 0.62f,
                w * 0.78f, h * 0.18f,
                w, h * 0.26f
            )
            lineTo(w, h)
            lineTo(w * 0.32f, h)
            close()
        }
        drawPath(
            path = orangeWavePath,
            color = WelcomeOrangeAccent
        )

        // 2. Deep Navy Foreground Wave Sweeping from Left to Right
        val deepNavyWavePath = Path().apply {
            moveTo(0f, h * 0.36f)
            cubicTo(
                w * 0.28f, h * 0.08f,
                w * 0.55f, h * 0.82f,
                w, h * 0.40f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = deepNavyWavePath,
            color = WelcomeNavyDeep
        )
    }
}
