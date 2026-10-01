package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("Painter", "Plumber", "Electrician", "Carpenter", "Labour", "Cleaner")

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; Toast.makeText(context, "Home Tab Clicked", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AddCircleOutline, contentDescription = "Post Job") },
                    label = { Text("Post Job") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; Toast.makeText(context, "Post Job Clicked", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2; Toast.makeText(context, "Chats Clicked", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PersonOutline, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3; Toast.makeText(context, "Profile Clicked", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
                .statusBarsPadding(),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Location", fontSize = 12.sp, color = TextGray)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { Toast.makeText(context, "Change Location", Toast.LENGTH_SHORT).show() }) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bhopal, MP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextDark)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .clip(CircleShape)
                            .clickable { Toast.makeText(context, "Notifications Clicked", Toast.LENGTH_SHORT).show() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextDark)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search for 'Electrician'") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = WorkoraBlue,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Action Banner (Now Clickable!)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { Toast.makeText(context, "Banner Clicked!", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = WorkoraBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Need urgent help?", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Post a job and get responses within minutes.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { Toast.makeText(context, "Post a Job Now!", Toast.LENGTH_SHORT).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = WorkoraBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Post Job", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Categories Header
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Top Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text("See All", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue, modifier = Modifier.clickable { Toast.makeText(context, "See All Clicked", Toast.LENGTH_SHORT).show() })
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Categories Grid (Now Functional)
            item {
                // Fixed height for grid to allow nested scrolling conceptually
                Box(modifier = Modifier.height(200.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        userScrollEnabled = false // Disable scroll here so main LazyColumn handles it
                    ) {
                        items(categories) { category ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clickable { Toast.makeText(context, "$category Selected", Toast.LENGTH_SHORT).show() },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(category, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recommended Workers List
            item {
                Text("Recommended Workers", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sample Worker Card
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { Toast.makeText(context, "Worker Profile Opened", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(50.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Ramesh Kumar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Expert Plumber • 4.8 ⭐", fontSize = 13.sp, color = TextGray)
                        }
                        Button(
                            onClick = { Toast.makeText(context, "Hiring Ramesh...", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Hire", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
