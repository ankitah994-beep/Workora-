package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

data class CategoryItem(val name: String, val icon: ImageVector)
data class TopWorker(val name: String, val category: String, val rating: String, val wage: String)

@Composable
fun CustomerHomeScreen(
    userName: String = "Ankit",
    userLocation: String = "Silwani, MP",
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToWorkerDetail: () -> Unit = {},
    onNavigateToBookings: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    val categories = listOf(
        CategoryItem("Painter", Icons.Default.Build),
        CategoryItem("Plumber", Icons.Default.Build),
        CategoryItem("Electrician", Icons.Default.Build),
        CategoryItem("Mason", Icons.Default.Build),
        CategoryItem("Carpenter", Icons.Default.Build)
    )

    val topWorkers = listOf(
        TopWorker("Amit Kumar", "Painter", "4.8", "₹800/d"),
        TopWorker("Rahul Sharma", "Plumber", "4.9", "₹900/d"),
        TopWorker("Suresh Das", "Mason", "4.5", "₹850/d")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WorkoraBlue,
                        unselectedIconColor = TextGray,
                        indicatorColor = WorkoraBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Bookings") },
                    label = { Text("Bookings", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 1,
                    onClick = { 
                        selectedBottomTab = 1
                        onNavigateToBookings()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WorkoraBlue,
                        unselectedIconColor = TextGray,
                        indicatorColor = WorkoraBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 2,
                    onClick = { 
                        selectedBottomTab = 2
                        onNavigateToChat()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WorkoraBlue,
                        unselectedIconColor = TextGray,
                        indicatorColor = WorkoraBlue.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    selected = selectedBottomTab == 3,
                    onClick = { 
                        selectedBottomTab = 3
                        onNavigateToProfile()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WorkoraBlue,
                        unselectedIconColor = TextGray,
                        indicatorColor = WorkoraBlue.copy(alpha = 0.1f)
                    )
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
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WorkoraBlue)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Hello, $userName 👋", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = userLocation, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }
                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                }
            }

            // Search Bar Mock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(52.dp)
                    .background(Color.White, RoundedCornerShape(26.dp))
                    .border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(26.dp))
                    .clickable { onNavigateToSearch() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Search for workers or jobs...", fontSize = 14.sp, color = TextGray)
                }
            }

            // Categories Section
            Text(
                text = "Categories",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.width(4.dp)) }
                items(categories) { category ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { 
                            Toast.makeText(context, "Opening ${category.name}s", Toast.LENGTH_SHORT).show() 
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color.White, CircleShape)
                                .border(1.dp, BorderGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(category.icon, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = category.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
                item { Spacer(modifier = Modifier.width(4.dp)) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Top Rated Workers Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Top Rated Workers", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text(
                    text = "See All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraBlue,
                    modifier = Modifier.clickable { onNavigateToSearch() }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.width(4.dp)) }
                items(topWorkers) { worker ->
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onNavigateToWorkerDetail() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color(0xFFEFF6FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(30.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = worker.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1)
                            Text(text = worker.category, fontSize = 12.sp, color = WorkoraBlue, fontWeight = FontWeight.Medium)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = ProfileOrange, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = worker.rating, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                }
                                Text(text = worker.wage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.width(4.dp)) }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
