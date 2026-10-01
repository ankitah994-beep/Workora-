package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen() {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    val categories = listOf("All" to Icons.Default.Apps, "Construction" to Icons.Default.Construction, "Electrical" to Icons.Default.ElectricalServices, "Plumbing" to Icons.Default.Plumbing, "Painting" to Icons.Default.FormatPaint)

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home", fontSize = 10.sp) }, selected = selectedBottomTab == 0, onClick = { selectedBottomTab = 0 }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF0061FF)))
                NavigationBarItem(icon = { Icon(Icons.Default.WorkOutline, null) }, label = { Text("Jobs", fontSize = 10.sp) }, selected = selectedBottomTab == 1, onClick = { selectedBottomTab = 1 })
                NavigationBarItem(icon = { Icon(Icons.Default.AddCircle, null, tint = Color(0xFF0061FF), modifier = Modifier.size(36.dp)) }, label = { Text("Post", fontSize = 10.sp) }, selected = selectedBottomTab == 2, onClick = { selectedBottomTab = 2 })
                NavigationBarItem(icon = { Icon(Icons.Default.Assignment, null) }, label = { Text("Applications", fontSize = 10.sp) }, selected = selectedBottomTab == 3, onClick = { selectedBottomTab = 3 })
                NavigationBarItem(icon = { Icon(Icons.Default.PersonOutline, null) }, label = { Text("Profile", fontSize = 10.sp) }, selected = selectedBottomTab == 4, onClick = { selectedBottomTab = 4 })
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(paddingValues)
        ) {
            // Extended Blue Header with Search
            item {
                Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF0061FF)).padding(16.dp).statusBarsPadding()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("W Workora", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = "", onValueChange = {},
                        placeholder = { Text("Search workers, jobs, or categories...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )
                }
            }

            // Categories
            item {
                LazyRow(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(categories) { category ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { Toast.makeText(context, "${category.first} Clicked", Toast.LENGTH_SHORT).show() }) {
                            Box(modifier = Modifier.size(56.dp).background(Color.White, CircleShape).border(1.dp, Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(category.second, contentDescription = category.first, tint = Color(0xFF0061FF))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(category.first, fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            // Post Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { Toast.makeText(context, "Post New Work", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0061FF))
                ) {
                    Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).background(Color.White.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Post a New Work", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Find skilled workers near you", fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Active Jobs Section
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Active Jobs", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("View All", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0061FF))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE2E8F0))) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("House Painting", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹500 - ₹700/day • 2 workers", fontSize = 12.sp, color = Color.Gray)
                            Text("📍 Indore, MP • 2 days ago", fontSize = 12.sp, color = Color.Gray)
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)), shape = RoundedCornerShape(4.dp)) {
                            Text("In Progress", color = Color(0xFF137333), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
