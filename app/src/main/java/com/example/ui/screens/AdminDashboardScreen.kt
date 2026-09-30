package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFF8FAFC)
private val SuccessGreen = Color(0xFF10B981)
private val DangerRed = Color(0xFFEF4444)

@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit = {},
    onNavigateToJobs: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToAppControl: () -> Unit = {},
    onNavigateToStateControl: () -> Unit = {}
) {
    val context = LocalContext.current
    // State for Bottom Navigation
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    selected = selectedBottomTab == 0,
                    onClick = { selectedBottomTab = 0; Toast.makeText(context, "Dashboard Refreshed", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = "Users") },
                    label = { Text("Users", fontSize = 10.sp) },
                    selected = selectedBottomTab == 1,
                    onClick = { selectedBottomTab = 1; Toast.makeText(context, "Opening Users List...", Toast.LENGTH_SHORT).show(); onNavigateToUsers() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Work, contentDescription = "Jobs") },
                    label = { Text("Jobs", fontSize = 10.sp) },
                    selected = selectedBottomTab == 2,
                    onClick = { selectedBottomTab = 2; Toast.makeText(context, "Opening Jobs List...", Toast.LENGTH_SHORT).show(); onNavigateToJobs() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Report, contentDescription = "Reports") },
                    label = { Text("Reports", fontSize = 10.sp) },
                    selected = selectedBottomTab == 3,
                    onClick = { selectedBottomTab = 3; Toast.makeText(context, "Opening Reports...", Toast.LENGTH_SHORT).show(); onNavigateToReports() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                    label = { Text("More", fontSize = 10.sp) },
                    selected = selectedBottomTab == 4,
                    onClick = { selectedBottomTab = 4; Toast.makeText(context, "Opening Menu...", Toast.LENGTH_SHORT).show() },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
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
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WorkoraBlue)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp).clickable { Toast.makeText(context, "Sidebar Menu Clicked", Toast.LENGTH_SHORT).show() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Workora", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Admin Dashboard", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }
                Box(
                    modifier = Modifier.size(36.dp).background(Color.White, CircleShape).clickable { Toast.makeText(context, "Admin Profile Clicked", Toast.LENGTH_SHORT).show() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(22.dp))
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Statistics Grid
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminUniqueStatCard(
                        modifier = Modifier.weight(1f), icon = Icons.Default.Person, title = "Total Users",
                        value = "24,580", change = "12%", isUp = true,
                        onClick = { Toast.makeText(context, "Loading Users Data...", Toast.LENGTH_SHORT).show(); onNavigateToUsers() }
                    )
                    AdminUniqueStatCard(
                        modifier = Modifier.weight(1f), icon = Icons.Default.Work, title = "Total Jobs",
                        value = "8,742", change = "18%", isUp = true,
                        onClick = { Toast.makeText(context, "Loading Jobs Data...", Toast.LENGTH_SHORT).show(); onNavigateToJobs() }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminUniqueStatCard(
                        modifier = Modifier.weight(1f), icon = Icons.Default.Assessment, title = "Total Applications",
                        value = "45,230", change = "20%", isUp = true,
                        onClick = { Toast.makeText(context, "Loading Applications...", Toast.LENGTH_SHORT).show() }
                    )
                    AdminUniqueStatCard(
                        modifier = Modifier.weight(1f), icon = Icons.Default.Flag, title = "Reported Issues",
                        value = "124", change = "5%", isUp = false,
                        onClick = { Toast.makeText(context, "Loading Reports...", Toast.LENGTH_SHORT).show(); onNavigateToReports() }
                    )
                }

                // Platform Growth Graph
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { Toast.makeText(context, "Graph Expanded", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Platform Growth (Last 7 Days)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                            val points = listOf(40f, 35f, 50f, 45f, 60f, 80f, 100f)
                            val max = 120f
                            val widthPerPoint = size.width / (points.size - 1)
                            val path = Path()
                            points.forEachIndexed { index, value ->
                                val x = index * widthPerPoint
                                val y = size.height - (value / max * size.height)
                                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path = path, color = WorkoraBlue, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                            points.forEachIndexed { index, value ->
                                val x = index * widthPerPoint
                                val y = size.height - (value / max * size.height)
                                drawCircle(color = WorkoraBlue, radius = 5.dp.toPx(), center = Offset(x, y))
                                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(x, y))
                            }
                        }
                    }
                }

                // Quick Actions
                Text("Quick Actions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    AdminQuickActionItem(icon = Icons.Default.Settings, title = "App Control", onClick = { Toast.makeText(context, "Opening App Settings", Toast.LENGTH_SHORT).show(); onNavigateToAppControl() })
                    AdminQuickActionItem(icon = Icons.Default.Report, title = "View Reports", onClick = { Toast.makeText(context, "Opening Reports", Toast.LENGTH_SHORT).show(); onNavigateToReports() })
                    AdminQuickActionItem(icon = Icons.Default.Group, title = "Manage Users", onClick = { Toast.makeText(context, "Opening User Management", Toast.LENGTH_SHORT).show(); onNavigateToUsers() })
                    AdminQuickActionItem(icon = Icons.Default.Map, title = "State Control", onClick = { Toast.makeText(context, "Opening Region Settings", Toast.LENGTH_SHORT).show(); onNavigateToStateControl() })
                }
            }
        }
    }
}

@Composable
private fun AdminUniqueStatCard(modifier: Modifier, icon: ImageVector, title: String, value: String, change: String, isUp: Boolean, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(title, fontSize = 12.sp, color = TextGray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                Icon(if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, contentDescription = null, tint = if (isUp) SuccessGreen else DangerRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(change, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isUp) SuccessGreen else DangerRed)
            }
        }
    }
}

@Composable
private fun AdminQuickActionItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.width(80.dp)) {
        Box(
            modifier = Modifier.size(50.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextDark, textAlign = TextAlign.Center)
    }
}
