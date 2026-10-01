package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabourDashboardScreen() {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableIntStateOf(0) }
    var selectedTopTab by remember { mutableIntStateOf(0) }

    val jobs = listOf(
        "House Painting" to "₹500 - ₹800/day",
        "Carpentry Work" to "₹600 - ₹800/day",
        "Electrical Work" to "₹400 - ₹600/day"
    )

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
            item {
                Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF0061FF)).padding(horizontal = 16.dp, vertical = 12.dp).statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Find Work", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = "", onValueChange = {},
                        placeholder = { Text("Search jobs, category, location...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = false, onClick = {}, label = { Text("Nearby") }, leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp)) })
                        FilterChip(selected = false, onClick = {}, label = { Text("Category") }, trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) })
                        FilterChip(selected = false, onClick = {}, label = { Text("Sort") }, trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) })
                    }
                }
            }
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTopTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF0061FF),
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTopTab]),
                            color = Color(0xFF0061FF)
                        )
                    }
                ) {
                    listOf("All Jobs", "Open", "Applications", "My Jobs").forEachIndexed { index, title ->
                        Tab(selected = selectedTopTab == index, onClick = { selectedTopTab = index }, text = { Text(title, fontWeight = FontWeight.Bold, color = if (selectedTopTab == index) Color(0xFF0061FF) else Color.Gray) })
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            items(jobs) { job ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { Toast.makeText(context, "Job Details Opened", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(job.first, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)), shape = RoundedCornerShape(4.dp)) {
                                Text("Open", color = Color(0xFF137333), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Text(" Indore, MP • 2.5 km", fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("📅 25 Apr 2026 • 🕘 09:00 AM", fontSize = 12.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                Text(" 2 workers", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(job.second, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0061FF))
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
