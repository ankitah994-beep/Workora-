package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFF8FAFC)
private val ProfileOrange = Color(0xFFFF8C00)
private val ProfileGreen = Color(0xFF10B981)

@Composable
fun WorkerDetailScreen(
    workerName: String = "Amit Kumar",
    category: String = "Painter",
    rating: String = "4.8",
    reviewsCount: String = "124 Reviews",
    wage: String = "₹800",
    experience: String = "5 Years",
    location: String = "Raisen, MP (5 km away)",
    about: String = "I am a professional painter with 5 years of experience in interior and exterior house painting. I ensure clean and high-quality work on time.",
    onBack: () -> Unit = {},
    onHire: () -> Unit = {},
    onCall: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WorkoraBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Worker Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color(0xFFEFF6FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(45.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = workerName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Verified, contentDescription = "Verified", tint = WorkoraBlue, modifier = Modifier.size(18.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = category, fontSize = 15.sp, color = WorkoraBlue, fontWeight = FontWeight.Medium)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(icon = Icons.Default.Star, value = rating, label = reviewsCount, iconTint = ProfileOrange)
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = BorderGray)
                        StatItem(icon = Icons.Default.Verified, value = experience, label = "Experience", iconTint = ProfileGreen)
                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = BorderGray)
                        StatItem(icon = null, value = "$wage/day", label = "Daily Wage", iconTint = TextDark)
                    }
                }
            }

            // About Section
            Column {
                Text(text = "About", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = about,
                    fontSize = 14.sp,
                    color = TextGray,
                    lineHeight = 22.sp
                )
            }
            
            Divider(color = BorderGray)

            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Location", fontSize = 12.sp, color = TextGray)
                    Text(text = location, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
            }

            Divider(color = BorderGray)

            // Work Proof / Portfolio
            Column {
                Text(text = "Work Proofs (Portfolio)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PortfolioImagePlaceholder(modifier = Modifier.weight(1f))
                    PortfolioImagePlaceholder(modifier = Modifier.weight(1f))
                    PortfolioImagePlaceholder(modifier = Modifier.weight(1f))
                }
            }
        }

        // Functional Bottom Action Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Calling $workerName...", Toast.LENGTH_SHORT).show()
                    onCall()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, WorkoraBlue)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue)
            }
            
            Button(
                onClick = {
                    Toast.makeText(context, "Hiring request sent to $workerName!", Toast.LENGTH_SHORT).show()
                    onHire()
                },
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
            ) {
                Text("Hire Now", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector?, value: String, label: String, iconTint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, color = TextGray)
    }
}

@Composable
private fun PortfolioImagePlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Image, contentDescription = null, tint = TextGray, modifier = Modifier.size(28.dp))
    }
}
