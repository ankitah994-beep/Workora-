package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2200)
        onLanguageSelected()
    }

    val indigoGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF252161), // Deep Royal Indigo Top
            Color(0xFF1E1B4B), // Deep Navy Center
            Color(0xFF151336)  // Dark Indigo Bottom
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = indigoGradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onLanguageSelected()
            }
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Construction Skyline & Cranes Silhouette at Bottom (Exact as Photo 1)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .align(Alignment.BottomCenter)
        ) {
            val silhouetteColor = Color.White.copy(alpha = 0.05f)
            val w = size.width
            val h = size.height

            // Skyline buildings
            drawRect(color = silhouetteColor, topLeft = Offset(w * 0.05f, h * 0.45f), size = Size(w * 0.14f, h * 0.55f))
            drawRect(color = silhouetteColor, topLeft = Offset(w * 0.22f, h * 0.30f), size = Size(w * 0.16f, h * 0.70f))
            drawRect(color = silhouetteColor, topLeft = Offset(w * 0.42f, h * 0.50f), size = Size(w * 0.15f, h * 0.50f))
            drawRect(color = silhouetteColor, topLeft = Offset(w * 0.60f, h * 0.25f), size = Size(w * 0.18f, h * 0.75f))
            drawRect(color = silhouetteColor, topLeft = Offset(w * 0.80f, h * 0.40f), size = Size(w * 0.15f, h * 0.60f))

            // Left Crane
            drawLine(color = silhouetteColor, start = Offset(w * 0.18f, h * 0.45f), end = Offset(w * 0.18f, h * 0.08f), strokeWidth = 6f)
            drawLine(color = silhouetteColor, start = Offset(w * 0.08f, h * 0.12f), end = Offset(w * 0.35f, h * 0.08f), strokeWidth = 5f)

            // Right Crane
            drawLine(color = silhouetteColor, start = Offset(w * 0.75f, h * 0.35f), end = Offset(w * 0.75f, h * 0.05f), strokeWidth = 6f)
            drawLine(color = silhouetteColor, start = Offset(w * 0.55f, h * 0.08f), end = Offset(w * 0.90f, h * 0.05f), strokeWidth = 5f)
        }

        // Center Worker Helmet Avatar + WORKORA Branding
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Worker with Yellow Helmet Vector Graphic
            Canvas(modifier = Modifier.size(92.dp)) {
                val w = size.width
                val h = size.height

                // Shoulders / Suit (Blue & Orange)
                val shoulderPath = Path().apply {
                    moveTo(w * 0.18f, h * 0.92f)
                    quadraticBezierTo(w * 0.50f, h * 0.62f, w * 0.82f, h * 0.92f)
                    close()
                }
                drawPath(path = shoulderPath, color = Color(0xFF3B82F6))

                // Collar / Vest Accent
                val vestPath = Path().apply {
                    moveTo(w * 0.35f, h * 0.92f)
                    lineTo(w * 0.50f, h * 0.70f)
                    lineTo(w * 0.65f, h * 0.92f)
                    close()
                }
                drawPath(path = vestPath, color = Color(0xFFF59E0B))

                // Face Circle
                drawCircle(
                    color = Color(0xFFFDE68A),
                    radius = w * 0.19f,
                    center = Offset(w * 0.50f, h * 0.48f)
                )

                // Yellow Construction Hardhat Dome
                val helmetPath = Path().apply {
                    moveTo(w * 0.27f, h * 0.42f)
                    quadraticBezierTo(w * 0.50f, h * 0.10f, w * 0.73f, h * 0.42f)
                    close()
                }
                drawPath(path = helmetPath, color = Color(0xFFFBBF24))

                // Helmet Visor Brim
                drawLine(
                    color = Color(0xFFF59E0B),
                    start = Offset(w * 0.24f, h * 0.42f),
                    end = Offset(w * 0.78f, h * 0.42f),
                    strokeWidth = 10f
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // WORK (White) + ORA (Orange)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.ExtraBold)) {
                        append("WORK")
                    }
                    withStyle(SpanStyle(color = Color(0xFFF59E0B), fontWeight = FontWeight.ExtraBold)) {
                        append("ORA")
                    }
                },
                fontSize = 38.sp,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Find. Hire. Work.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f),
                letterSpacing = 0.8.sp
            )
        }
    }
}
