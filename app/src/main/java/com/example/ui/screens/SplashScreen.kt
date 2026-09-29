package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R // अगर आपका पैकेज नाम अलग है, तो इसे बदल लें
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    // 2.5 सेकंड तक यह लोडिंग स्क्रीन दिखेगी, फिर ऐप चालू होगा
    LaunchedEffect(Unit) {
        delay(2500L)
        onSplashFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0061FF)), // डिज़ाइन वाला गाढ़ा नीला रंग
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ऊपर से थोड़ी जगह छोड़ने के लिए
        Spacer(modifier = Modifier.height(100.dp))

        // 1. आपका लोगो (W Workora)
        Image(
            painter = painterResource(id = R.drawable.workora_splash_logo),
            contentDescription = "Workora Logo",
            modifier = Modifier.width(220.dp), // लोगो का साइज़
            contentScale = ContentScale.Fit
        )
        
        // 2. लोगो के नीचे वाली टैगलाइन (डिज़ाइन के अनुसार)
        Text(
            text = "Find. Hire. Work.",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 12.dp)
        )

        // यह खाली जगह बीच में आएगी, जिससे Loading Bar सबसे नीचे चला जाएगा
        Spacer(modifier = Modifier.weight(1f)) 

        // 3. लोडिंग वाली लाइन (Progress Bar)
        LinearProgressIndicator(
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .width(180.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Loading... टेक्स्ट
        Text(
            text = "Loading...",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp
        )

        // नीचे से थोड़ी जगह (Padding)
        Spacer(modifier = Modifier.height(60.dp))
    }
}
