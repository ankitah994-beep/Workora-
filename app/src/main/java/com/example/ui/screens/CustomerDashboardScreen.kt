package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CustNavy = Color(0xFF083D91)
private val CustOrange = Color(0xFFFF8C00)

@Composable
fun CustomerDashboardScreen(
    onNavigateToSearch: () -> Unit = {},
    onOpenChat: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(bottom = 60.dp) // Bottom Navigation Padding
    ) {
        // ✅ NEW: CUSTOMER MODE BADGE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE0E7FF))
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👨‍💼 CUSTOMER MODE (ग्राहक)",
                color = CustNavy,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        // Top Location Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { 
                    Toast.makeText(context, "Location Settings", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = CustOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Bhopal, Madhya Pradesh", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CustNavy)
                    Text("Tap to change work area", fontSize = 11.sp, color = Color.Gray)
                }
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
            }
            
            IconButton(onClick = { 
                Toast.makeText(context, "No new notifications", Toast.LENGTH_SHORT).show() 
            }) {
                Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = CustNavy)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E7FF)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Find Daily Work\nNear You", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = CustNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Connect directly with nearby customers & get daily wages.", fontSize = 12.sp, color = CustNavy.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToSearch,
                            colors = ButtonDefaults.buttonColors(containerColor = CustNavy),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Explore Jobs", fontSize = 12.sp)
                        }
                    }
                    Box(
                        modifier = Modifier.size(60.dp).background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = CustOrange, modifier = Modifier.size(30.dp))
                    }
                }
            }

            // Categories Section
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Find Jobs by Category", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CustNavy)
                Text("View All >", fontSize = 12.sp, color = CustNavy, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToSearch() })
            }
            
            val categories = listOf("Mason", "Electrician", "Plumber", "Painter", "Carpenter", "More")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categories.chunked(3).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { catName ->
                            Card(
                                modifier = Modifier.weight(1f).height(80.dp).clickable { onNavigateToSearch() },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).background(Color(0xFFF8FAFC), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, tint = CustOrange, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(catName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CustNavy)
                                }
                            }
                        }
                    }
                }
            }

            // Profile Active Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22A06B), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Verified & Trusted Worker Profile", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CustNavy)
                        Text("Your profile is active and visible to customers.", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}
