package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole

private val RoleNavyPrimary = Color(0xFF083D91)
private val RoleNavyDarkCard = Color(0xFF0B2E59)
private val RoleOrangeAccent = Color(0xFFFF8C00)
private val RoleBgLight = Color(0xFFF8FAFC)
private val RoleWhite = Color(0xFFFFFFFF)
private val RoleMainText = Color(0xFF0B2345)
private val RoleSecondaryText = Color(0xFF687280)
private val RoleBorderColor = Color(0xFFE5EAF0)

@Composable
fun AccountSelectScreen(
    onSelectRole: (UserRole) -> Unit = {},
    toastMessage: String? = null
) {
    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RoleBgLight)
    ) {
        // Same Blue & Orange Bottom Wave Design from Reference Image
        AccountSelectBottomWave(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .align(Alignment.BottomCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // Top Workora Helmet Logo
            RoleScreenHelmetLogo(size = 84.dp)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Workora",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = RoleNavyPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Find. Hire. Work.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = RoleMainText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(38.dp))

            Text(
                text = "What do you want to do?",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = RoleMainText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // CARD 1: I want to Hire (Dark Navy Card as in Reference Image)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectRole(UserRole.CUSTOMER) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RoleNavyDarkCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(RoleOrangeAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Groups,
                            contentDescription = "I want to Hire",
                            tint = RoleWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I want to Hire",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoleWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find skilled workers for your work",
                            fontSize = 13.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = RoleWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CARD 2: I want to Work (White Card as in Reference Image)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectRole(UserRole.LABOUR) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = RoleWhite),
                border = BorderStroke(1.dp, RoleBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(RoleOrangeAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Engineering,
                            contentDescription = "I want to Work",
                            tint = RoleWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I want to Work",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoleMainText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find jobs and earn money",
                            fontSize = 13.sp,
                            color = RoleSecondaryText
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = RoleSecondaryText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleScreenHelmetLogo(size: Dp = 84.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Helmet Dome
        drawArc(
            color = RoleOrangeAccent,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.10f, h * 0.18f),
            size = Size(w * 0.80f, h * 0.82f)
        )

        // Top Center Ridge
        drawRoundRect(
            color = RoleOrangeAccent,
            topLeft = Offset(w * 0.43f, h * 0.11f),
            size = Size(w * 0.14f, h * 0.15f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        // Two White Vertical Slots on Dome
        drawRoundRect(
            color = RoleBgLight,
            topLeft = Offset(w * 0.37f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = RoleBgLight,
            topLeft = Offset(w * 0.585f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Helmet Bottom Brim
        drawRoundRect(
            color = RoleOrangeAccent,
            topLeft = Offset(w * 0.03f, h * 0.56f),
            size = Size(w * 0.94f, h * 0.11f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )
    }
}

@Composable
private fun AccountSelectBottomWave(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Orange Accent Wave Rising on the Right
        val orangeWavePath = Path().apply {
            moveTo(w * 0.34f, h * 0.66f)
            cubicTo(
                w * 0.58f, h * 0.60f,
                w * 0.78f, h * 0.16f,
                w, h * 0.24f
            )
            lineTo(w, h)
            lineTo(w * 0.34f, h)
            close()
        }
        drawPath(
            path = orangeWavePath,
            color = RoleOrangeAccent
        )

        // 2. Navy Foreground Wave Sweeping from Left to Right
        val navyWavePath = Path().apply {
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
            path = navyWavePath,
            color = RoleNavyPrimary
        )
    }
}
