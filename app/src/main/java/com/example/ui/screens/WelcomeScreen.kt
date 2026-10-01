package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R // अपना पैकेज नेम चेक कर लें

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Logo and Branding
        Text(text = "W", fontSize = 54.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0061FF))
        Text(text = "Workora", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0061FF))
        Text(text = "Find. Hire. Work.", fontSize = 14.sp, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Welcome Text
        Text(text = "Welcome to Workora", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your local platform to find work\nor hire skilled workers.",
            fontSize = 15.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Illustration (अपनी सफेद वाली वेलकम इमेज का नाम यहाँ डालें, जैसे img_welcome)
        Image(
            painter = painterResource(id = R.drawable.workora_logo), // इसे बदलकर अपनी वेलकम इमेज का नाम रखें
            contentDescription = "Welcome Illustration",
            modifier = Modifier.fillMaxWidth().height(220.dp),
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Paging Dots
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF0061FF)))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE2E8F0)))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE2E8F0)))
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE2E8F0)))
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
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
        Spacer(modifier = Modifier.height(10.dp))
    }
}
