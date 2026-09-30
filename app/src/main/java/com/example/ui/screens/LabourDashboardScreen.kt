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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
private val SuccessGreen = Color(0xFF10B981)

@Composable
fun LabourDashboardScreen(
    workerName: String = "Amit Kumar",
    workerCategory: String = "Painter",
    onNavigateToJobDetail: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = { // Default action agar pass na ho
    },
    onNavigateToMyJobs: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var isOnline by remember { mutableStateOf(true) }

    val availableJobs = listOf(
        JobRequest("Rajesh Verma", "Need Painter for 2 BHK", "Silwani (2 km)", "Tomorrow", "₹850/day"),
        JobRequest("Vikas Sharma", "Wall putty and primer work", "Raisen (15 km)", "05 Oct 2026", "₹900/day"),
        JobRequest("Anil Dubey", "Exterior house painting", "Silwani (1 km)", "10 Oct 2026", "₹800/day")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 0,
                    onClick = {
                        selectedBottomTab = 0
                        Toast.makeText(context, "Home Tab Selected", Toast.LENGTH_SHORT).show()
                    },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue, unselectedIconColor = TextGray, indicatorColor = WorkoraBlue.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Work, contentDescription = "My Jobs") },
                    label = { Text("My Jobs", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 1,
                    onClick = {
                        selectedBottomTab = 1
                        Toast.makeText(context, "Opening My Jobs", Toast.LENGTH_SHORT).show()
                        onNavigateToMyJobs()
                    },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue, unselectedIconColor = TextGray, indicatorColor = WorkoraBlue.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 2,
                    onClick = {
                        selectedBottomTab = 2
                        Toast.makeText(context, "Opening Chat", Toast.LENGTH_SHORT).show()
                        onNavigateToChat()
                    },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue, unselectedIconColor = TextGray, indicatorColor = WorkoraBlue.copy(alpha = 0.1f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 3,
                    onClick = {
                        selectedBottomTab = 3
                        Toast.makeText(context, "Opening Profile", Toast.LENGTH_SHORT).show()
                        onNavigateToProfile()
                    },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue, unselectedIconColor = TextGray, indicatorColor = WorkoraBlue.copy(alpha = 0.1f))
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header Section with working notification click
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WorkoraBlue)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = workerName, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text(text = workerCategory, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Notifications Clicked", Toast.LENGTH_SHORT).show()
                        onNavigateToNotifications()
                    },
                    modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                }
            }

            // Status Toggle (Online/Offline)
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

            // Quick Stats
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

            // Jobs List with working Accept buttons
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isOnline) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text(text = "Go Online to see new jobs.", color = TextGray, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(availableJobs) { job ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                Toast.makeText(context, "Opening Job Details", Toast.LENGTH_SHORT).show()
                                onNavigateToJobDetail()
                            },
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
                                    Text(text = job.location, fontSize = 12.sp, color = TextGray)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = "• ${job.date}", fontSize = 12.sp, color = TextGray)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Job Accepted Successfully! ✓", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
                                    ) {
                                        Text("Accept", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

// Added Missing Composable and Data Class[span_3](start_span)[span_3](end_span)
@Composable
fun StatCard(modifier: Modifier = Modifier, icon: ImageVector, title: String, value: String, iconTint: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = title, fontSize = 12.sp, color = TextGray)
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
        }
    }
}

data class JobRequest(
    val clientName: String,
    val title: String,
    val location: String,
    val date: String,
    val wage: String
)
