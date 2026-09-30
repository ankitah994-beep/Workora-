package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(text = "W", fontSize = 60.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0061FF))
        Text(text = "Workora", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0061FF))
        Text(text = "Find. Hire. Work.", fontSize = 14.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(text = "Welcome to Workora", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your local platform to find work\nor hire skilled workers.",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onGetStarted,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061FF))
        ) {
            Text("Get Started →", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", color = Color.Gray, fontSize = 14.sp)
            Text(
                "Login",
                color = Color(0xFF0061FF),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onLoginClick() }.padding(4.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
