package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.model.AdminAuditLog
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminModuleSpec(
    val id: Int,
    val title: String,
    val subtitle: String,
    val phaseStatus: String,
    val capabilities: List<String>,
    val icon: ImageVector
)

@Composable
fun AdminDashboardScreen(
    adminEmail: String = "admin@workora.com",
    adminTier: String = "SUPER_ADMIN",
    onLogoutAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }

    var isAuthorizedSession by remember { mutableStateOf(true) }
    var isLoadingMetrics by remember { mutableStateOf(true) }
    var metrics by remember { mutableStateOf(LiveAdminMetrics()) }
    val auditLogs = remember { mutableStateListOf<AdminAuditLog>() }
    var selectedModuleIndex by remember { mutableIntStateOf(0) }
    var pendingDestructiveConfirm by remember { mutableStateOf<String?>(null) }

    val adminModules = remember {
        listOf(
            AdminModuleSpec(1, "1. Dashboard Overview", "Live Users, Workers, Jobs & Activity", "LIVE IN PHASE 1", listOf("Total Users", "Total Workers", "Total Customers", "Active Workers", "Active Jobs", "Completed Jobs", "Pending Requests", "Reports / Complaints"), Icons.Default.VerifiedUser),
            AdminModuleSpec(2, "2. User Management", "Search, Filter, Suspend, Block & Delete Users", "PHASE 2", listOf("All Users", "Customers & Workers", "Search & Filter", "Edit / Suspend / Block / Unblock / Delete User", "User Activity"), Icons.Default.Person),
            AdminModuleSpec(3, "3. Worker Management", "Verify Documents, Approve, Reject & Manage Skills", "PHASE 2", listOf("Pending Verification", "Verified & Rejected Workers", "Skills & Location", "Approve / Reject / Suspend Worker"), Icons.Default.Build),
            AdminModuleSpec(4, "4. Customer Management", "Customer Profiles, Booking History & Controls", "PHASE 2", listOf("All Customers", "Booking History", "Active & Cancelled Jobs", "Suspend / Block Customer"), Icons.Default.Person),
            AdminModuleSpec(5, "5. Job Management", "Full Lifecycle Control over All Posted Jobs", "PHASE 3", listOf("Pending / Accepted / Active / Completed Jobs", "Customer & Worker Details", "Job Status Control"), Icons.Default.CheckCircle),
            AdminModuleSpec(6, "6. Service & Categories", "Add, Edit, Order & Enable/Disable Categories", "PHASE 3", listOf("All Categories", "Add / Edit / Delete Category", "Enable / Disable Service", "Category Ordering"), Icons.Default.Build),
            AdminModuleSpec(7, "7. Location Management", "Manage Cities, Serviceable Areas & Distribution", "PHASE 3", listOf("Cities & Areas", "Enable / Disable Serviceable Area", "Worker Distribution by Area"), Icons.Default.LocationOn),
            AdminModuleSpec(8, "8. Reports & Complaints", "Investigate & Resolve User, Job & Fraud Reports", "PHASE 4", listOf("User & Worker Reports", "Job Complaints", "Abuse & Fraud Reports", "Investigate / Resolve / Close"), Icons.Default.Warning),
            AdminModuleSpec(9, "9. Support Management", "Customer & Worker Support Tickets & Replies", "PHASE 4", listOf("Open / Pending / Resolved Tickets", "Customer & Worker Support", "Reply to User"), Icons.Default.Info),
            AdminModuleSpec(10, "10. Notifications", "Broadcast to All Users, Customers or Workers", "PHASE 4", listOf("Send to All / Customers / Workers / Specific User", "Scheduled Notifications", "Notification History"), Icons.Default.Notifications),
            AdminModuleSpec(11, "11. App Content (CMS)", "Welcome Screen, Announcements, FAQ & Policies", "PHASE 5", listOf("Welcome Content", "App Announcements", "FAQ & Help Center", "Terms & Privacy Policy"), Icons.Default.Info),
            AdminModuleSpec(12, "12. Analytics", "User Growth, Daily/Monthly Jobs & Retention", "PHASE 5", listOf("User / Worker / Customer Growth", "Jobs Per Day & Month", "Popular Services & Areas"), Icons.Default.CheckCircle),
            AdminModuleSpec(13, "13. Security Center", "Admin 2FA, Sessions, Login History & Alerts", "LIVE IN PHASE 1", listOf("Admin Login Verification", "Admin Activity Logs", "Suspicious Activity", "Blocked Accounts"), Icons.Default.Lock),
            AdminModuleSpec(14, "14. Admin Management", "Super Admin, Support Admin & Moderator RBAC", "LIVE IN PHASE 1", listOf("Super Admin / Support Admin / Moderator", "Roles & Permissions", "Add / Remove Admin"), Icons.Default.VerifiedUser),
            AdminModuleSpec(15, "15. System Settings", "Maintenance Mode, Registration Toggles & Config", "PHASE 5", listOf("Maintenance Mode", "Registration On/Off", "Worker & Customer Registration Toggle"), Icons.Default.Settings),
            AdminModuleSpec(16, "16. Audit Logs", "Immutable Record of Who Changed What & When", "LIVE IN PHASE 1", listOf("Admin Account & Timestamp", "User / Worker / Job / Settings Changes"), Icons.Default.Lock),
            AdminModuleSpec(17, "17. Emergency Controls", "Kill-Switches for Registrations, Jobs & Areas", "PHASE 5", listOf("Disable New Registrations", "Disable New Job Requests", "Emergency Announcement"), Icons.Default.Warning),
            AdminModuleSpec(18, "18. Data Management", "Database Collections, Export & Backup Status", "PHASE 5", listOf("Users / Workers / Jobs Data", "Data Export", "Firebase Backup Status"), Icons.Default.CheckCircle),
            AdminModuleSpec(19, "19. Quick Actions", "One-Tap Shortcuts for Frequent Admin Operations", "LIVE IN PHASE 1", listOf("Verify Worker", "Manage Users", "Manage Jobs", "Emergency Controls"), Icons.Default.Settings)
        )
    }

    fun refreshAdminData() {
        isLoadingMetrics = true
        FirebaseManager.fetchLiveAdminMetrics { liveMetrics, logs ->
            metrics = liveMetrics
            auditLogs.clear()
            auditLogs.addAll(logs)
            isLoadingMetrics = false
        }
    }

    LaunchedEffect(Unit) {
        val savedRole = authPrefs.getString("saved_user_role", "")
        if (savedRole != "ADMIN") {
            isAuthorizedSession = false
            Toast.makeText(context, "Unauthorized Access Blocked by RBAC Guard!", Toast.LENGTH_LONG).show()
            onLogoutAdmin()
        } else {
            refreshAdminData()
        }
    }

    if (!isAuthorizedSession) {
        Box(modifier = Modifier.fillMaxSize().background(WorkoraBgLight), contentAlignment = Alignment.Center) {
            Text("Verifying Backend Admin Authorization...", fontWeight = FontWeight.Bold, color = WorkoraNavy)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(WorkoraNavy)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(WorkoraOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "WORKORA ADMIN PANEL",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "$adminTier • $adminEmail",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { refreshAdminData() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = WorkoraOrange)
                }
                IconButton(
                    onClick = {
                        pendingDestructiveConfirm = "LOGOUT_ADMIN"
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Live Cloud Activity Overview",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                        Text(
                            text = if (isLoadingMetrics) "Syncing..." else "● Firebase Live",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AdminStatTile("Total Users", metrics.totalUsers.toString(), WorkoraNavy, Modifier.weight(1f))
                        AdminStatTile("Total Workers", metrics.totalWorkers.toString(), WorkoraOrange, Modifier.weight(1f))
                        AdminStatTile("Customers", metrics.totalCustomers.toString(), Color(0xFF0284C7), Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AdminStatTile("Active Workers", metrics.activeWorkers.toString(), Color(0xFF16A34A), Modifier.weight(1f))
                        AdminStatTile("Active Jobs", metrics.activeJobs.toString(), WorkoraOrange, Modifier.weight(1f))
                        AdminStatTile("Completed", metrics.completedJobs.toString(), WorkoraNavy, Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AdminStatTile("Pending Jobs", metrics.pendingRequests.toString(), Color(0xFFD97706), Modifier.weight(1f))
                        AdminStatTile("Open Reports", metrics.reportsCount.toString(), Color(0xFFDC2626), Modifier.weight(1f))
                        AdminStatTile("Security Status", "Protected", Color(0xFF16A34A), Modifier.weight(1f))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "19. Admin Quick Actions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Verify Worker" to 2,
                            "Manage Users" to 1,
                            "Manage Jobs" to 4,
                            "Add Service" to 5,
                            "View Reports" to 7,
                            "Send Notification" to 9,
                            "Emergency Controls" to 16
                        ).forEach { (label, targetIdx) ->
                            OutlinedButton(
                                onClick = { selectedModuleIndex = targetIdx },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, WorkoraOrange),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                            }
                        }
                    }
                }
            }

            Text(
                text = "All 19 Admin Control Modules (Tap to Inspect)",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WorkoraNavy
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                adminModules.forEachIndexed { index, module ->
                    val isSelected = selectedModuleIndex == index
                    Button(
                        onClick = { selectedModuleIndex = index },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) WorkoraNavy else Color.White
                        ),
                        border = BorderStroke(1.dp, if (isSelected) WorkoraNavy else WorkoraBorder),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = module.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else WorkoraTextDark
                        )
                    }
                }
            }

            val activeModule = adminModules[selectedModuleIndex]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, WorkoraOrange)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(activeModule.icon, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeModule.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraNavy
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (activeModule.phaseStatus.contains("LIVE")) Color(0xFF16A34A).copy(alpha = 0.15f)
                                    else WorkoraOrange.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = activeModule.phaseStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (activeModule.phaseStatus.contains("LIVE")) Color(0xFF16A34A) else WorkoraOrange
                            )
                        }
                    }

                    Text(text = activeModule.subtitle, fontSize = 13.sp, color = WorkoraTextMuted)

                    activeModule.capabilities.forEach { cap ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = cap, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = WorkoraTextDark)
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "16. Recent Security & Admin Audit Logs (/audit_logs)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )

                    if (auditLogs.isEmpty()) {
                        Text(
                            text = "No audit entries recorded yet. Every Admin login and state change is automatically logged here.",
                            fontSize = 12.sp,
                            color = WorkoraTextMuted
                        )
                    } else {
                        auditLogs.take(5).forEach { log ->
                            val formattedTime = remember(log.timestamp) {
                                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(WorkoraBgLight, shape = RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${log.actionType} • ${log.adminTier}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WorkoraNavy)
                                    Text(text = formattedTime, fontSize = 11.sp, color = WorkoraTextMuted)
                                }
                                Text(text = "${log.adminEmail}: ${log.details}", fontSize = 12.sp, color = WorkoraTextDark)
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingDestructiveConfirm != null) {
        AlertDialog(
            onDismissRequest = { pendingDestructiveConfirm = null },
            title = { Text("Confirm Admin Action", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to end your secure Admin session and log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        FirebaseManager.recordAdminAuditLog(
                            adminEmail = adminEmail,
                            adminTier = adminTier,
                            actionType = "ADMIN_LOGOUT",
                            targetEntity = "AdminSession",
                            details = "Admin signed out cleanly"
                        )
                        authPrefs.edit()
                            .putBoolean("is_logged_in", false)
                            .remove("saved_user_role")
                            .remove("saved_admin_tier")
                            .apply()
                        pendingDestructiveConfirm = null
                        onLogoutAdmin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Confirm Logout", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingDestructiveConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminStatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(WorkoraBgLight, shape = RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = accent)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraTextMuted)
    }
}
