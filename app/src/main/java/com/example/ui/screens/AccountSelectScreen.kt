package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Private variables taaki dusri files ke colors se clash na ho
private val PrimaryBlue = Color(0xFF0062FF)
private val LightBg = Color(0xFFF5F8FF)

@Composable
fun AccountSelectScreen(
    onBackClick: () -> Unit = {},
    onWorkerClick: () -> Unit = {},
    onClientClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Back Button
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = PrimaryBlue,
            modifier = Modifier
                .size(28.dp)
                .clickable { onBackClick() }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Headings
        Text(
            text = "Choose Your Role",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "What do you want to do?",
            fontSize = 16.sp,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Worker Card ("I want to work")
        RoleCard(
            title = "I want to work",
            subtitle = "Find local jobs and earn",
            icon = Icons.Default.Build,
            onClick = onWorkerClick
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Client Card ("I want to hire")
        RoleCard(
            title = "I want to hire",
            subtitle = "Post work and find workers",
            icon = Icons.Default.Person,
            onClick = onClientClick
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom Text
        Text(
            text = "Join thousands of people\nin your local area",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RoleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightBg),
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.White, shape = RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = PrimaryBlue)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text 
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
            
            // Arrow Icon
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = PrimaryBlue
            )
        }
    }
}
