package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole

private val ThemeIndigo = Color(0xFF1E1B4B)
private val ThemeOrange = Color(0xFFF59E0B)

@Composable
fun AccountSelectScreen(
    onSelectRole: (UserRole) -> Unit,
    toastMessage: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    val activeEmail = remember {
        (authPrefs.getString("last_logged_in_email", "") ?: "").trim().lowercase()
    }
    val activePhone = remember {
        (profilePrefs.getString("user_phone", "") ?: "").filter { it.isDigit() }.takeLast(10)
    }

    val isLocalSuperAdmin = remember(activeEmail, activePhone) {
        val isLogged = authPrefs.getBoolean("is_logged_in", false)
        isLogged && (
            activeEmail == "ankitah994@gmail.com" ||
            activeEmail.contains("ankitah994") ||
            activePhone == "6265798340" ||
            authPrefs.getString("saved_user_role", "") == "ADMIN"
        )
    }

    var isAdminPanelVisible by remember { mutableStateOf(false) }
    var isBackendAdminVerified by remember { mutableStateOf(isLocalSuperAdmin) }
    var adminRoleTier by remember { mutableStateOf("SUPER_ADMIN") }

    LaunchedEffect(Unit) {
        val isLogged = authPrefs.getBoolean("is_logged_in", false)
        if (isLogged && activeEmail.isNotBlank()) {
            if (isLocalSuperAdmin) {
                isBackendAdminVerified = true
            }
            FirebaseManager.checkIfEmailIsAdminOnCloud(activeEmail) { isAdmin, tier ->
                if (isAdmin || isLocalSuperAdmin) {
                    isBackendAdminVerified = true
                    adminRoleTier = tier
                }
            }
        }
    }

    if (isAdminPanelVisible && isBackendAdminVerified) {
        AdminDashboardScreen(
            adminEmail = if (activeEmail.isNotBlank()) activeEmail else "ankitah994@gmail.com",
            adminTier = adminRoleTier,
            onLogoutAdmin = {
                isAdminPanelVisible = false
            }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top WORKORA Title + Subtitle (Exact as Top-Middle of Photo)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = ThemeIndigo, fontWeight = FontWeight.ExtraBold)) {
                        append("WORK")
                    }
                    withStyle(SpanStyle(color = ThemeOrange, fontWeight = FontWeight.ExtraBold)) {
                        append("ORA")
                    }
                },
                fontSize = 32.sp,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Find skilled workers\nnear you or get hired\nfor the best jobs.",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center,
                lineHeight = 25.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center 3 Skilled Workers Illustration (Mason/Engineer, Plumber/Mechanic, Painter)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFFF3F4F6),
                    radius = size.minDimension * 0.46f,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy((-12).dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Worker 1: Mason / Engineer (Orange Vest & Yellow Helmet)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👷‍♂️", fontSize = 38.sp)
                    }
                    Box(
                        modifier = Modifier
                            .width(78.dp)
                            .height(75.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(Color(0xFFF97316)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Engineering, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }

                // Worker 2 (Center Tall): Technician / Plumber (Dark Navy Overalls)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛠️", fontSize = 42.sp)
                    }
                    Box(
                        modifier = Modifier
                            .width(92.dp)
                            .height(95.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(ThemeIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = ThemeOrange, modifier = Modifier.size(32.dp))
                    }
                }

                // Worker 3: Painter (Yellow Apron & Roller)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF9C3)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👨‍🎨", fontSize = 38.sp)
                    }
                    Box(
                        modifier = Modifier
                            .width(78.dp)
                            .height(75.dp)
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(Color(0xFFEAB308)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FormatPaint, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom Role Buttons ("I want to Hire" & "I want to Work" Exact as Photo)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Deep Indigo Button: I want to Hire
            Button(
                onClick = { onSelectRole(UserRole.CUSTOMER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ThemeIndigo),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "I want to Hire",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Find skilled workers for your work",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // 2. White Outlined Card Button: I want to Work
            OutlinedButton(
                onClick = { onSelectRole(UserRole.LABOUR) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color(0xFFE5E7EB)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "I want to Work",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ThemeIndigo
                    )
                    Text(
                        text = "Find jobs and earn",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // 3. Super Admin Panel Button (Visible only to verified Admin)
            if (isBackendAdminVerified) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            authPrefs.edit().putString("saved_user_role", "ADMIN").apply()
                            isAdminPanelVisible = true
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.5.dp, ThemeOrange)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ThemeIndigo),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = ThemeOrange, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("WORKORA Admin Dashboard", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = ThemeIndigo)
                                Text("Manage Users, Workers, Bookings & Settings", fontSize = 11.sp, color = Color(0xFF6B7280))
                            }
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = ThemeIndigo)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
                Text(
                    text = "Login",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.clickable { onSelectRole(UserRole.CUSTOMER) }
                )
            }
        }
    }
}
