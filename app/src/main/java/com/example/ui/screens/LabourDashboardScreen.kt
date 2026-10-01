package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class JobRequest(val clientName: String, val title: String, val location: String, val date: String, val wage: String)

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFF8FAFC)
private val ProfileOrange = Color(0xFFFF8C00)
private val SuccessGreen = Color(0xFF10B981)

@Composable
fun LabourDashboardScreen() {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var isOnline by remember { mutableStateOf(true) }

    val availableJobs = listOf(
        JobRequest("Rajesh Verma", "Need Painter for 2 BHK", "Silwani (2 km)", "Tomorrow", "₹850/day"),
        JobRequest("Vikas Sharma", "Wall putty and primer work", "Raisen (15 km)", "05 Oct 2026", "₹900/day")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0; Toast.makeText(context, "Home Tab Clicked", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Work, contentDescription = "My Jobs") },
                    label = { Text("My Jobs", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1; Toast.makeText(context, "My Jobs Clicked", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2; Toast.makeText(context, "Chat Clicked", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3; Toast.makeText(context, "Profile Clicked", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
            }
        }
    ) { paddingValues ->
        // Changed to LazyColumn to make the entire screen scrollable
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(paddingValues)
                .statusBarsPadding()
        ) {
            // Header Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().background(WorkoraBlue).padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(50.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Amit Kumar", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(text = "Painter", fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                    IconButton(
                        onClick = { Toast.makeText(context, "Notifications Clicked", Toast.LENGTH_SHORT).show() }, 
                        modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                    }
                }
            }

            // Status Toggle Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Availability Status", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(
                                text = if (isOnline) "Online - Receiving Jobs" else "Offline - Hidden from clients",
                                fontSize = 12.sp,
                                color = if (isOnline) SuccessGreen else TextGray
                            )
                        }
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { 
                                isOnline = it
                                Toast.makeText(context, if (it) "You are now Online" else "You are now Offline", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SuccessGreen)
                        )
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.CheckCircle, title = "Completed", value = "24", iconTint = SuccessGreen)
                    StatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Star, title = "Rating", value = "4.8", iconTint = ProfileOrange)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Available Jobs Near You", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Jobs List Items
            if (!isOnline) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Go Online to see new jobs.", color = TextGray, fontSize = 14.sp)
                    }
                }
            } else {
                items(availableJobs) { job ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { Toast.makeText(context, "Opening Job Details", Toast.LENGTH_SHORT).show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = job.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text(text = job.wage, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = job.clientName, fontSize = 12.sp, color = TextGray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${job.location} • ${job.date}", fontSize = 12.sp, color = TextGray)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { Toast.makeText(context, "Job Accepted Successfully! ✓", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
                            ) { Text("Accept", fontSize = 14.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: ImageVector, title: String, value: String, iconTint: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Text(text = title, fontSize = 12.sp, color = TextGray)
        }
    }
}
