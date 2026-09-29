package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val WorkoraBlue = Color(0xFF0062FF)

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBlue),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "W",
            fontSize = 100.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = 2.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Workora",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Find. Hire. Work.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))

        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(4.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Loading...",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
