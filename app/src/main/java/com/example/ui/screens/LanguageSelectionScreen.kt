package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WorkoraHelmetLogo
import kotlinx.coroutines.delay

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: () -> Unit
) {
    val context = LocalContext.current
    val brandPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }

    var appName by remember { mutableStateOf(brandPrefs.getString("app_name", "WORKORA") ?: "WORKORA") }
    var appTagline by remember { mutableStateOf(brandPrefs.getString("app_tagline", "FIND. HIRE. WORK.") ?: "FIND. HIRE. WORK.") }

    // Auto-advance from Welcome Screen after 2.2 seconds (No Login button on Welcome Screen)
    LaunchedEffect(Unit) {
        appName = brandPrefs.getString("app_name", "WORKORA") ?: "WORKORA"
        appTagline = brandPrefs.getString("app_tagline", "FIND. HIRE. WORK.") ?: "FIND. HIRE. WORK."
        delay(2200)
        onLanguageSelected()
    }

    val greenGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF15803D), // Deep Rich Green
            Color(0xFF16A34A), // Vibrant Green
            Color(0xFF14532D)  // Dark Forest Green
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = greenGradient)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onLanguageSelected()
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Container on Green Background
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                WorkoraHelmetLogo(
                    size = 92.dp,
                    showHalo = false
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = "WELCOME TO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = appName,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.18f), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = appTagline,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.8.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "सही काम और कुशल कारीगर अब आपके पास",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
