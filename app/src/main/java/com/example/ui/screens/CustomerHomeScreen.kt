package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BgGray = Color(0xFFF8FAFC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen() {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = { WorkoraBottomNavigation() },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Top Bar (Header)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WorkoraBlue)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .statusBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Workora", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Find. Hire. Work.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = WorkoraBlue)
                    }
                }
            }

            // 2. Search Bar
            Box(modifier = Modifier.background(WorkoraBlue).padding(horizontal = 20.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search workers, jobs, or categories...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }
            
            // Blue Curve Background trick
            Box(modifier = Modifier.fillMaxWidth().height(20.dp).background(WorkoraBlue)) {
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(Color.White))
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // 3. Categories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CategoryItem(Icons.Default.Foundation, "Construction", Color(0xFFFDE68A), Color(0xFFD97706))
                    CategoryItem(Icons.Default.ElectricalServices, "Electrical", Color(0xFFDBEAFE), WorkoraBlue)
                    CategoryItem(Icons.Default.Plumbing, "Plumbing", Color(0xFFD1FAE5), Color(0xFF059669))
                    CategoryItem(Icons.Default.FormatPaint, "Painting", Color(0xFFFCE7F3), Color(0xFFDB2777))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Banner (Post a New Work)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgGray),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(WorkoraBlue.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(30.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Post a New Work", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Find skilled workers near you", fontSize = 12.sp, color = TextGray)
                        }
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Active Jobs Section
                SectionHeader(title = "Active Jobs", actionText = "View All")
                Spacer(modifier = Modifier.height(12.dp))
                JobCard("House Painting", "Indore, MP • 2 days ago", "In Progress", Color(0xFF22A06B))

                Spacer(modifier = Modifier.height(24.dp))

                // 6. Recommended Workers Section
                SectionHeader(title = "Recommended Workers", actionText = "View All")
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { WorkerCard("Rohit Sharma", "Painter", "4.6 (24)", "₹500/day") }
                    item { WorkerCard("Suresh Patel", "Electrician", "4.8 (15)", "₹600/day") }
                    item { WorkerCard("Amit Kumar", "Carpenter", "4.7 (32)", "₹550/day") }
                }
                
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun CategoryItem(icon: ImageVector, title: String, bgColor: Color, iconColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(60.dp).clip(CircleShape).background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(30.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

@Composable
fun SectionHeader(title: String, actionText: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text(actionText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = WorkoraBlue)
    }
}

@Composable
fun JobCard(title: String, details: String, status: String, statusColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(BgGray), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.FormatPaint, contentDescription = null, tint = WorkoraBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(details, fontSize = 12.sp, color = TextGray)
            }
            Text(status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = statusColor, modifier = Modifier.background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun WorkerCard(name: String, role: String, rating: String, price: String) {
    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(BgGray), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(role, fontSize = 12.sp, color = TextGray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                Text(" $rating", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextDark)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(price, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(32.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
            ) {
                Text("Hire", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkoraBottomNavigation() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(icon = { Icon(Icons.Default.Home, contentDescription = "Home") }, label = { Text("Home") }, selected = true, onClick = { }, colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue, selectedTextColor = WorkoraBlue, unselectedIconColor = TextGray, indicatorColor = Color.White))
        NavigationBarItem(icon = { Icon(Icons.Outlined.WorkOutline, contentDescription = "Jobs") }, label = { Text("Jobs") }, selected = false, onClick = { })
        
        // Post Button (Center FAB style)
        NavigationBarItem(
            icon = { 
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(WorkoraBlue), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, contentDescription = "Post", tint = Color.White)
                }
            }, 
            label = { Text("Post") }, selected = false, onClick = { }
        )
        
        NavigationBarItem(icon = { Icon(Icons.Outlined.Assignment, contentDescription = "Applications") }, label = { Text("Applications") }, selected = false, onClick = { })
        NavigationBarItem(icon = { Icon(Icons.Outlined.PersonOutline, contentDescription = "Profile") }, label = { Text("Profile") }, selected = false, onClick = { })
    }
}
