package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.R

@Composable
fun RoleSelectionScreen(
    onWorkerSelected: () -> Unit,
    onCustomerSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Welcome to Workora!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Aap kya karna chahte hain?",
            fontSize = 16.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(50.dp))

        // Worker Option Card
        RoleCard(
            title = "I want to Work",
            subtitle = "Jobs find karein aur paise kamayein",
            imageRes = R.drawable.img_role_worker, // Check if image exists in res/drawable
            onClick = onWorkerSelected
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Customer Option Card
        RoleCard(
            title = "I want to Hire",
            subtitle = "Apne kaam ke liye skilled workers dhoondhein",
            imageRes = R.drawable.img_role_client, // Check if image exists in res/drawable
            onClick = onCustomerSelected
        )
    }
}

@Composable
fun RoleCard(title: String, subtitle: String, imageRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0061FF))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
