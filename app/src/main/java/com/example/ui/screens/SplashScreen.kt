package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.R // अपना पैकेज नेम चेक कर लें

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }

    // लोडिंग एनीमेशन (2 सेकंड के लिए)
    LaunchedEffect(Unit) {
        while (progress < 1f) {
            progress += 0.05f
            delay(100)
        }
        onSplashFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0061FF)) // मास्टरप्लान का नीला रंग
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Logo & Text (सफ़ेद रंग में)
        Text(text = "W", fontSize = 72.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(text = "Workora", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(text = "Find. Hire. Work.", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 2 Workers Illustration
        Image(
            painter = painterResource(id = R.drawable.workora_splash_logo), // सही स्पेलिंग वाला नाम
            contentDescription = "Splash Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Loading Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Loading...", fontSize = 14.sp, color = Color.White)
        Spacer(modifier = Modifier.height(20.dp))
    }
}
