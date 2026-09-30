package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFF8FAFC)
private val SuccessGreen = Color(0xFF10B981)
private val DangerRed = Color(0xFFEF4444)

// =========================================================================
// 1. ADMIN LOGIN SCREEN
// =========================================================================
@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Blue Section with Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(WorkoraBlue),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("W", fontSize = 72.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("Workora", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Find. Hire. Work.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        // Bottom White Section (Login Form)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(Color.White, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Admin Login", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Text("Access your admin panel", fontSize = 14.sp, color = TextGray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                placeholder = { Text("Email or Phone", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextGray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraBlue, unfocusedBorderColor = BorderGray)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                placeholder = { Text("Password", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, tint = TextGray)
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraBlue, unfocusedBorderColor = BorderGray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Invalid admin credentials", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
            ) {
                Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Forgot password?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue, modifier = Modifier.clickable { Toast.makeText(context, "Reset Link Sent", Toast.LENGTH_SHORT).show() })
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Secure Admin Access", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// =========================================================================
// 2. ADMIN DASHBOARD SCREEN
// =========================================================================
@Composable
fun AdminDashboardScreen(
    onNavigateToUsers: () -> Unit = {},
    onNavigateToJobs: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToAppControl: () -> Unit = {},
    onNavigateToStateControl: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Dashboard, "Dashboard") }, label = { Text("Dashboard", fontSize = 10.sp) },
                    selected = selectedBottomTab == 0, onClick = { selectedBottomTab = 0 }, colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Group, "Users") }, label = { Text("Users", fontSize = 10.sp) },
                    selected = selectedBottomTab == 1, onClick = { selectedBottomTab = 1; onNavigateToUsers() }, colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Work, "Jobs") }, label = { Text("Jobs", fontSize = 10.sp) },
                    selected = selectedBottomTab == 2, onClick = { selectedBottomTab = 2; onNavigateToJobs() }, colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Report, "Reports") }, label = { Text("Reports", fontSize = 10.sp) },
                    selected = selectedBottomTab == 3, onClick = { selectedBottomTab = 3; onNavigateToReports() }, colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MoreHoriz, "More") }, label = { Text("More", fontSize = 10.sp) },
                    selected = selectedBottomTab == 4, onClick = { selectedBottomTab = 4 }, colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue)
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
            // Top App Bar matching Masterplan
            Row(
                modifier = Modifier.fillMaxWidth().background(WorkoraBlue).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(24.dp).clickable { })
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Workora", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Admin Dashboard", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }
                Box(modifier = Modifier.size(36.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(24.dp))
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Real-time Statistics Grid (2x2)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Person, title = "Total Users", value = "24,580", change = "12%", isUp = true, onClick = onNavigateToUsers)
                    AdminStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Work, title = "Total Jobs", value = "8,742", change = "18%", isUp = true, onClick = onNavigateToJobs)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Assessment, title = "Total Applications", value = "45,230", change = "20%", isUp = true, onClick = {})
                    AdminStatCard(modifier = Modifier.weight(1f), icon = Icons.Default.Flag, title = "Reported Issues", value = "124", change = "5%", isUp = false, onClick = onNavigateToReports)
                }

                // Platform Growth Graph Placeholder
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Platform Growth (Last 7 Days)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(16.dp))
                        // Draw a mock chart exactly like the image
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
                            
                            drawPath(
                                path = path,
                                color = WorkoraBlue,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                            
                            // Draw dots
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickActionItem(icon = Icons.Default.Settings, title = "App Control", onClick = onNavigateToAppControl)
                    QuickActionItem(icon = Icons.Default.Report, title = "View Reports", onClick = onNavigateToReports)
                    QuickActionItem(icon = Icons.Default.Group, title = "Manage Users", onClick = onNavigateToUsers)
                    QuickActionItem(icon = Icons.Default.Map, title = "State Control", onClick = onNavigateToStateControl)
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String, change: String, isUp: Boolean, onClick: () -> Unit) {
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
                Icon(if (isUp) Icons.Default.ArrowUpward else Icons.Default.ArrowUpward, contentDescription = null, tint = if (isUp) SuccessGreen else DangerRed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(change, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isUp) SuccessGreen else DangerRed)
            }
        }
    }
}

@Composable
fun QuickActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.width(80.dp)) {
        Box(
            modifier = Modifier.size(50.dp).background(Color.White, RoundedCornerShape(12.dp)).border(1.dp, BorderGray, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextDark, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
