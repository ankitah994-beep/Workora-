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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ADMIN_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

data class AdminUserItem(
    val key: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val location: String,
    val skill: String,
    val role: String,
    val accountStatus: String
)

data class AdminWorkerItem(
    val key: String,
    val name: String,
    val trade: String,
    val dailyWage: Int,
    val phone: String,
    val location: String,
    val isVerified: Boolean,
    val isAvailableToday: Boolean
)

data class AdminJobItem(
    val key: String,
    val id: Long,
    val title: String,
    val category: String,
    val dailyRate: Int,
    val location: String,
    val customerName: String,
    val status: String
)

@Composable
fun AdminDashboardScreen(
    adminEmail: String = "ankitah994@gmail.com",
    adminTier: String = "SUPER_ADMIN",
    onLogoutAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }

    var isLoading by remember { mutableStateOf(true) }
    var activeTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val usersList = remember { mutableStateListOf<AdminUserItem>() }
    val workersList = remember { mutableStateListOf<AdminWorkerItem>() }
    val jobsList = remember { mutableStateListOf<AdminJobItem>() }
    val auditLogs = remember { mutableStateListOf<AdminAuditLog>() }

    // Broadcast & Notification inputs
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMessage by remember { mutableStateOf("") }

    // Quick Job Post inputs
    var newJobTitle by remember { mutableStateOf("") }
    var newJobCategory by remember { mutableStateOf("Mistri / Construction") }
    var newJobWage by remember { mutableStateOf("600") }
    var newJobLocation by remember { mutableStateOf("Silwani, Raisen") }

    // New Admin Management inputs
    var newAdminEmailInput by remember { mutableStateOf("") }
    var newAdminRoleInput by remember { mutableStateOf("SUPER_ADMIN") }

    // Emergency System Controls
    var maintenanceMode by remember { mutableStateOf(false) }
    var allowRegistrations by remember { mutableStateOf(true) }
    var allowJobPosting by remember { mutableStateOf(true) }

    var showExitDialog by remember { mutableStateOf(false) }

    fun loadAllLiveFirebaseData() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Fetch Users
                val loadedUsers = mutableListOf<AdminUserItem>()
                val uConn = (URL("$ADMIN_DB_URL/users.json").openConnection() as HttpURLConnection)
                if (uConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(uConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            loadedUsers.add(
                                AdminUserItem(
                                    key = k,
                                    fullName = obj.optString("fullName", "User"),
                                    email = obj.optString("email", k),
                                    phone = obj.optString("phone", "N/A"),
                                    location = obj.optString("location", "Silwani"),
                                    skill = obj.optString("skill", "General"),
                                    role = obj.optString("role", "CUSTOMER"),
                                    accountStatus = obj.optString("accountStatus", "ACTIVE")
                                )
                            )
                        }
                    }
                }
                uConn.disconnect()

                // 2. Fetch Workers
                val loadedWorkers = mutableListOf<AdminWorkerItem>()
                val wConn = (URL("$ADMIN_DB_URL/workers.json").openConnection() as HttpURLConnection)
                if (wConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(wConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            loadedWorkers.add(
                                AdminWorkerItem(
                                    key = k,
                                    name = obj.optString("name", "Worker"),
                                    trade = obj.optString("trade", "General Worker"),
                                    dailyWage = obj.optInt("dailyWage", 500),
                                    phone = obj.optString("phone", "N/A"),
                                    location = obj.optString("location", "Silwani"),
                                    isVerified = obj.optBoolean("isVerified", true),
                                    isAvailableToday = obj.optBoolean("isAvailableToday", true)
                                )
                            )
                        }
                    }
                }
                wConn.disconnect()

                // 3. Fetch Jobs
                val loadedJobs = mutableListOf<AdminJobItem>()
                val jConn = (URL("$ADMIN_DB_URL/jobs.json").openConnection() as HttpURLConnection)
                if (jConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(jConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            loadedJobs.add(
                                AdminJobItem(
                                    key = k,
                                    id = obj.optLong("id", System.currentTimeMillis()),
                                    title = obj.optString("title", "Work Post"),
                                    category = obj.optString("category", "General"),
                                    dailyRate = obj.optInt("dailyRate", 500),
                                    location = obj.optString("location", "Silwani"),
                                    customerName = obj.optString("customerName", "Customer"),
                                    status = obj.optString("status", "PENDING")
                                )
                            )
                        }
                    }
                }
                jConn.disconnect()

                // 4. Fetch System Settings
                val sConn = (URL("$ADMIN_DB_URL/system_settings.json").openConnection() as HttpURLConnection)
                if (sConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(sConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val obj = JSONObject(text)
                        maintenanceMode = obj.optBoolean("maintenanceMode", false)
                        allowRegistrations = obj.optBoolean("allowRegistrations", true)
                        allowJobPosting = obj.optBoolean("allowJobPosting", true)
                    }
                }
                sConn.disconnect()

                // 5. Fetch Audit Logs
                val loadedLogs = mutableListOf<AdminAuditLog>()
                val lConn = (URL("$ADMIN_DB_URL/audit_logs.json").openConnection() as HttpURLConnection)
                if (lConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(lConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            loadedLogs.add(
                                AdminAuditLog(
                                    logId = obj.optString("logId", k),
                                    adminEmail = obj.optString("adminEmail", adminEmail),
                                    adminTier = obj.optString("adminTier", adminTier),
                                    actionType = obj.optString("actionType", "ACTION"),
                                    targetEntity = obj.optString("targetEntity", "System"),
                                    details = obj.optString("details", ""),
                                    timestamp = obj.optLong("timestamp", 0L)
                                )
                            )
                        }
                    }
                }
                lConn.disconnect()

                withContext(Dispatchers.Main) {
                    usersList.clear()
                    usersList.addAll(loadedUsers)
                    workersList.clear()
                    workersList.addAll(loadedWorkers)
                    jobsList.clear()
                    jobsList.addAll(loadedJobs.sortedByDescending { it.id })
                    auditLogs.clear()
                    auditLogs.addAll(loadedLogs.sortedByDescending { it.timestamp })
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    fun updateFirebaseField(path: String, fieldName: String, value: Any, logAction: String, logDetails: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val getConn = (URL("$ADMIN_DB_URL/$path.json").openConnection() as HttpURLConnection)
                val existingText = if (getConn.responseCode in 200..299) {
                    BufferedReader(InputStreamReader(getConn.inputStream)).use { it.readText() }
                } else "{}"
                getConn.disconnect()

                val json = if (existingText.isNotBlank() && existingText != "null") JSONObject(existingText) else JSONObject()
                json.put(fieldName, value)

                val putConn = (URL("$ADMIN_DB_URL/$path.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                OutputStreamWriter(putConn.outputStream).use { it.write(json.toString()) }
                putConn.responseCode
                putConn.disconnect()

                FirebaseManager.recordAdminAuditLog(adminEmail, adminTier, logAction, path, logDetails)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "$logDetails ✓", Toast.LENGTH_SHORT).show()
                    loadAllLiveFirebaseData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Action Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteFirebaseNode(path: String, logAction: String, logDetails: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val delConn = (URL("$ADMIN_DB_URL/$path.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                }
                delConn.responseCode
                delConn.disconnect()

                FirebaseManager.recordAdminAuditLog(adminEmail, adminTier, logAction, path, logDetails)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "$logDetails ✓", Toast.LENGTH_SHORT).show()
                    loadAllLiveFirebaseData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Delete Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        authPrefs.edit()
            .putString("saved_user_role", "ADMIN")
            .putString("saved_admin_tier", adminTier)
            .apply()
        loadAllLiveFirebaseData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(WorkoraNavy)
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onLogoutAdmin() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(WorkoraOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WORKORA ADMIN PANEL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "$adminTier • $adminEmail",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    Toast.makeText(context, "Refreshing Firebase Data...", Toast.LENGTH_SHORT).show()
                    loadAllLiveFirebaseData()
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = WorkoraOrange)
                }
                IconButton(onClick = { showExitDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Exit", tint = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Live Interactive Stats Cards (Tap any card to open that section!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1. Live Cloud Control Center",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                        Text(
                            text = if (isLoading) "Syncing..." else "● Firebase Connected",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdminStatTile("Total Users", usersList.size.toString(), WorkoraNavy, Modifier.weight(1f)) { activeTab = 0 }
                        AdminStatTile("Workers", workersList.size.toString(), WorkoraOrange, Modifier.weight(1f)) { activeTab = 1 }
                        AdminStatTile("Total Jobs", jobsList.size.toString(), Color(0xFF0284C7), Modifier.weight(1f)) { activeTab = 2 }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AdminStatTile("Verified Workers", workersList.count { it.isVerified }.toString(), Color(0xFF16A34A), Modifier.weight(1f)) { activeTab = 1 }
                        AdminStatTile("Blocked Users", usersList.count { it.accountStatus == "BLOCKED" }.toString(), Color(0xFFDC2626), Modifier.weight(1f)) { activeTab = 0 }
                        AdminStatTile("Audit Logs", auditLogs.size.toString(), WorkoraNavy, Modifier.weight(1f)) { activeTab = 5 }
                    }
                }
            }

            // 2. Interactive Action Tabs (Tap to use real tools!)
            Text(
                text = "Select Control Tool (बटन दबाकर इस्तेमाल करें):",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WorkoraNavy
            )

            val tabs = listOf(
                "👥 Manage Users (${usersList.size})",
                "🛠️ Verify Workers (${workersList.size})",
                "📋 Manage Jobs (${jobsList.size})",
                "📢 Send Notification",
                "🛡️ Add Admin",
                "🚨 Emergency & Logs"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val selected = activeTab == index
                    Button(
                        onClick = { activeTab = index },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) WorkoraOrange else Color.White
                        ),
                        border = BorderStroke(1.dp, if (selected) WorkoraOrange else WorkoraBorder),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selected) Color.White else WorkoraNavy
                        )
                    }
                }
            }

            // Search Filter for Users / Workers / Jobs
            if (activeTab in 0..2) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Name, Phone or Location...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkoraOrange) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // ==================== TAB 0: REAL USER MANAGEMENT ====================
            if (activeTab == 0) {
                val filteredUsers = usersList.filter {
                    searchQuery.isBlank() ||
                            it.fullName.contains(searchQuery, true) ||
                            it.phone.contains(searchQuery, true) ||
                            it.email.contains(searchQuery, true)
                }

                Text(
                    text = "Registered App Users (${filteredUsers.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
                )

                filteredUsers.forEach { user ->
                    val isBlocked = user.accountStatus == "BLOCKED"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (isBlocked) Color(0xFFDC2626) else WorkoraBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = user.fullName, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                                    Text(text = "📧 ${user.email} • 📞 ${user.phone}", fontSize = 12.sp, color = WorkoraTextMuted)
                                    Text(text = "📍 ${user.location} • Role: ${user.role}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isBlocked) Color(0xFFDC2626).copy(alpha = 0.15f) else Color(0xFF16A34A).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = user.accountStatus,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isBlocked) Color(0xFFDC2626) else Color(0xFF16A34A)
                                    )
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val nextStatus = if (isBlocked) "ACTIVE" else "BLOCKED"
                                        updateFirebaseField(
                                            path = "users/${user.key}",
                                            fieldName = "accountStatus",
                                            value = nextStatus,
                                            logAction = "USER_STATUS_CHANGE",
                                            logDetails = "${user.fullName} marked $nextStatus"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isBlocked) Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                ) {
                                    Text(if (isBlocked) "Unblock User" else "Block User", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val nextRole = if (user.role == "CUSTOMER") "LABOUR" else "CUSTOMER"
                                        updateFirebaseField(
                                            path = "users/${user.key}",
                                            fieldName = "role",
                                            value = nextRole,
                                            logAction = "USER_ROLE_CHANGE",
                                            logDetails = "${user.fullName} role changed to $nextRole"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, WorkoraNavy)
                                ) {
                                    Text("Make ${if (user.role == "CUSTOMER") "Worker" else "Customer"}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                }

                                IconButton(
                                    onClick = {
                                        deleteFirebaseNode(
                                            path = "users/${user.key}",
                                            logAction = "DELETE_USER",
                                            logDetails = "Deleted user ${user.fullName}"
                                        )
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }

            // ==================== TAB 1: REAL WORKER VERIFICATION ====================
            if (activeTab == 1) {
                val filteredWorkers = workersList.filter {
                    searchQuery.isBlank() ||
                            it.name.contains(searchQuery, true) ||
                            it.trade.contains(searchQuery, true) ||
                            it.location.contains(searchQuery, true)
                }

                Text(
                    text = "Workers Verification & Control (${filteredWorkers.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
                )

                filteredWorkers.forEach { worker ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (worker.isVerified) Color(0xFF16A34A) else WorkoraOrange)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = worker.name, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                                    Text(text = "🛠️ ${worker.trade} • ₹${worker.dailyWage}/day", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WorkoraOrange)
                                    Text(text = "📍 ${worker.location} • 📞 ${worker.phone}", fontSize = 12.sp, color = WorkoraTextMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (worker.isVerified) "✔ VERIFIED" else "⏳ UNVERIFIED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (worker.isVerified) Color(0xFF16A34A) else WorkoraOrange
                                    )
                                    Text(
                                        text = if (worker.isAvailableToday) "● Available" else "○ Busy",
                                        fontSize = 11.sp,
                                        color = if (worker.isAvailableToday) Color(0xFF16A34A) else WorkoraTextMuted
                                    )
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val nextVerified = !worker.isVerified
                                        updateFirebaseField(
                                            path = "workers/${worker.key}",
                                            fieldName = "isVerified",
                                            value = nextVerified,
                                            logAction = "WORKER_VERIFY",
                                            logDetails = "${worker.name} Verified = $nextVerified"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (worker.isVerified) WorkoraNavy else Color(0xFF16A34A)
                                    )
                                ) {
                                    Text(if (worker.isVerified) "Revoke Verify" else "Approve & Verify ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val nextAvail = !worker.isAvailableToday
                                        updateFirebaseField(
                                            path = "workers/${worker.key}",
                                            fieldName = "isAvailableToday",
                                            value = nextAvail,
                                            logAction = "WORKER_AVAILABILITY",
                                            logDetails = "${worker.name} Available = $nextAvail"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (worker.isAvailableToday) "Mark Busy" else "Mark Available", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                }

                                IconButton(
                                    onClick = {
                                        deleteFirebaseNode(
                                            path = "workers/${worker.key}",
                                            logAction = "DELETE_WORKER",
                                            logDetails = "Removed worker ${worker.name}"
                                        )
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }

            // ==================== TAB 2: REAL JOB MANAGEMENT & POSTING ====================
            if (activeTab == 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraOrange)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("➕ Post Official Job from Admin Panel", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)

                        OutlinedTextField(
                            value = newJobTitle,
                            onValueChange = { newJobTitle = it },
                            label = { Text("Job Title (जैसे: मकान निर्माण के लिए 2 मिस्त्री चाहिए)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newJobCategory,
                                onValueChange = { newJobCategory = it },
                                label = { Text("Category") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newJobWage,
                                onValueChange = { newJobWage = it },
                                label = { Text("Daily Wage ₹") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        OutlinedTextField(
                            value = newJobLocation,
                            onValueChange = { newJobLocation = it },
                            label = { Text("Location (जैसे: Silwani, Raisen)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (newJobTitle.isBlank()) {
                                    Toast.makeText(context, "Kripya Job Title likhein!", Toast.LENGTH_SHORT).show()
                                } else {
                                    FirebaseManager.postJobToFirebase(
                                        title = newJobTitle.trim(),
                                        category = newJobCategory.trim(),
                                        description = "Posted directly by Workora Admin ($adminEmail)",
                                        dailyRate = newJobWage.toIntOrNull() ?: 600,
                                        location = newJobLocation.trim(),
                                        workersNeeded = 2,
                                        urgency = "Immediate",
                                        customerName = "Workora Official Admin",
                                        customerPhone = "+91 6265798340"
                                    ) {
                                        newJobTitle = ""
                                        Toast.makeText(context, "Live Job Posted on Firebase! ✓", Toast.LENGTH_SHORT).show()
                                        loadAllLiveFirebaseData()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publish Live Job Now ✓", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Text(
                    text = "All Posted Jobs (${jobsList.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
                )

                if (jobsList.isEmpty()) {
                    Text("Abhi koi Job post nahi hai. Upar form se pehli Job post karein!", fontSize = 13.sp, color = WorkoraTextMuted)
                }

                jobsList.forEach { job ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, WorkoraBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = job.title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                                    Text(text = "${job.category} • ₹${job.dailyRate}/day • 📍 ${job.location}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraOrange)
                                    Text(text = "Posted by: ${job.customerName}", fontSize = 12.sp, color = WorkoraTextMuted)
                                }
                                Text(
                                    text = job.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (job.status == "COMPLETED") Color(0xFF16A34A) else WorkoraNavy
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val nextStatus = if (job.status == "COMPLETED") "PENDING" else "COMPLETED"
                                        updateFirebaseField(
                                            path = "jobs/${job.key}",
                                            fieldName = "status",
                                            value = nextStatus,
                                            logAction = "JOB_STATUS_CHANGE",
                                            logDetails = "Job '${job.title}' marked $nextStatus"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraNavy)
                                ) {
                                    Text(if (job.status == "COMPLETED") "Re-Open Job" else "Mark Completed ✓", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        deleteFirebaseNode(
                                            path = "jobs/${job.key}",
                                            logAction = "DELETE_JOB",
                                            logDetails = "Deleted job '${job.title}'"
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFDC2626))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }

            // ==================== TAB 3: LIVE BROADCAST NOTIFICATION ====================
            if (activeTab == 3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraOrange)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("📢 Send Live Broadcast Notification to All Users", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                        Text("यहाँ से भेजा गया मैसेज सभी ग्राहकों और मजदूरों के Live Chat और Notification में तुरंत दिखाई देगा।", fontSize = 12.sp, color = WorkoraTextMuted)

                        OutlinedTextField(
                            value = broadcastTitle,
                            onValueChange = { broadcastTitle = it },
                            label = { Text("Announcement Title (जैसे: Workora सूचना)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = broadcastMessage,
                            onValueChange = { broadcastMessage = it },
                            label = { Text("Type Message (जैसे: सिलवानी में नए काम उपलब्ध हैं!)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (broadcastMessage.isBlank()) {
                                    Toast.makeText(context, "Kripya message likhein!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val fullMsg = if (broadcastTitle.isNotBlank()) "📢 [${broadcastTitle.trim()}]: ${broadcastMessage.trim()}" else "📢 ${broadcastMessage.trim()}"
                                    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                    FirebaseManager.sendChatMessageToCloud(
                                        senderName = "WORKORA ADMIN ✓",
                                        messageText = fullMsg,
                                        timeText = timeStr
                                    ) {
                                        FirebaseManager.recordAdminAuditLog(
                                            adminEmail = adminEmail,
                                            adminTier = adminTier,
                                            actionType = "BROADCAST_SENT",
                                            targetEntity = "AllUsers",
                                            details = fullMsg
                                        )
                                        broadcastTitle = ""
                                        broadcastMessage = ""
                                        Toast.makeText(context, "Live Broadcast Sent to All Users! ✓", Toast.LENGTH_LONG).show()
                                        loadAllLiveFirebaseData()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Live Broadcast Now ✓", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }

            // ==================== TAB 4: ADD / MANAGE ADMINS ====================
            if (activeTab == 4) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🛡️ Add or Promote Another Admin on Firebase", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                        Text("किसी भी ईमेल आईडी को यहाँ लिखकर सीधे SUPER_ADMIN या SUPPORT_ADMIN बनाएं।", fontSize = 12.sp, color = WorkoraTextMuted)

                        OutlinedTextField(
                            value = newAdminEmailInput,
                            onValueChange = { newAdminEmailInput = it },
                            label = { Text("Enter User Email (जैसे: friend@gmail.com)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("SUPER_ADMIN", "SUPPORT_ADMIN", "MODERATOR").forEach { tierOption ->
                                val selected = newAdminRoleInput == tierOption
                                OutlinedButton(
                                    onClick = { newAdminRoleInput = tierOption },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) WorkoraNavy else Color.White
                                    )
                                ) {
                                    Text(tierOption.replace("_", " "), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else WorkoraNavy)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val clean = newAdminEmailInput.trim().lowercase()
                                if (!clean.contains("@")) {
                                    Toast.makeText(context, "Sahi Email ID dalein!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val safeKey = clean.replace(".", "_").replace("@", "_at_").replace(" ", "_")
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val conn = (URL("$ADMIN_DB_URL/admins/$safeKey.json").openConnection() as HttpURLConnection).apply {
                                                requestMethod = "PUT"
                                                setRequestProperty("Content-Type", "application/json")
                                                doOutput = true
                                            }
                                            val json = JSONObject().apply {
                                                put("isActive", true)
                                                put("role", newAdminRoleInput)
                                            }
                                            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                            conn.responseCode
                                            conn.disconnect()
                                            FirebaseManager.recordAdminAuditLog(adminEmail, adminTier, "ADD_ADMIN", clean, "Promoted $clean to $newAdminRoleInput")
                                            withContext(Dispatchers.Main) {
                                                newAdminEmailInput = ""
                                                Toast.makeText(context, "$clean is now $newAdminRoleInput! ✓", Toast.LENGTH_LONG).show()
                                                loadAllLiveFirebaseData()
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Failed to add admin", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Admin Access Now ✓", fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }

            // ==================== TAB 5: EMERGENCY CONTROLS & AUDIT LOGS ====================
            if (activeTab == 5) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFDC2626))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🚨 Emergency System Switches (Live on Firebase)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkoraTextDark)
                                Text("ऐप को मेंटेनेंस मोड में डालें", fontSize = 11.sp, color = WorkoraTextMuted)
                            }
                            Switch(
                                checked = maintenanceMode,
                                onCheckedChange = {
                                    maintenanceMode = it
                                    updateFirebaseField("system_settings", "maintenanceMode", it, "SYSTEM_SETTING", "Maintenance Mode = $it")
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFDC2626))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allow New User Registrations", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkoraTextDark)
                                Text("नए यूज़र्स का साइन-अप चालू/बंद करें", fontSize = 11.sp, color = WorkoraTextMuted)
                            }
                            Switch(
                                checked = allowRegistrations,
                                onCheckedChange = {
                                    allowRegistrations = it
                                    updateFirebaseField("system_settings", "allowRegistrations", it, "SYSTEM_SETTING", "Allow Registrations = $it")
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF16A34A))
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allow New Job Posting", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkoraTextDark)
                                Text("नई जॉब पोस्टिंग चालू/बंद करें", fontSize = 11.sp, color = WorkoraTextMuted)
                            }
                            Switch(
                                checked = allowJobPosting,
                                onCheckedChange = {
                                    allowJobPosting = it
                                    updateFirebaseField("system_settings", "allowJobPosting", it, "SYSTEM_SETTING", "Allow Job Posting = $it")
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF16A34A))
                            )
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
                        Text("16. Live Admin Security Audit Logs (${auditLogs.size})", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                        if (auditLogs.isEmpty()) {
                            Text("Abhi koi action log nahi hai. Jaise hi aap koi button dabayenge, uska record yahan aa jayega.", fontSize = 12.sp, color = WorkoraTextMuted)
                        } else {
                            auditLogs.take(15).forEach { log ->
                                val timeText = remember(log.timestamp) {
                                    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(WorkoraBgLight, shape = RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${log.actionType} • ${log.adminTier}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = WorkoraNavy)
                                        Text(timeText, fontSize = 11.sp, color = WorkoraTextMuted)
                                    }
                                    Text("${log.adminEmail}: ${log.details}", fontSize = 12.sp, color = WorkoraTextDark)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Admin Panel", fontWeight = FontWeight.Bold) },
            text = { Text("Kya aap Admin Panel se bahar aakar Customer/Worker mode dekhna chahte hain?") },
            confirmButton = {
                Button(
                    onClick = {
                        authPrefs.edit().remove("saved_user_role").apply()
                        showExitDialog = false
                        onLogoutAdmin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Exit Admin Panel", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExitDialog = false }) {
                    Text("Stay Here")
                }
            }
        )
    }
}

@Composable
private fun AdminStatTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WorkoraBgLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = accent)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraTextMuted)
        }
    }
}
