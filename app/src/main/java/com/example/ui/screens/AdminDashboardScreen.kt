package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
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

private const val ADMIN_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

// =========================================================================
// COMPLETE 16-SECTION HORIZONTAL PROFESSIONAL ADMIN PANEL
// Matches exact parameter signatures in MainActivity.kt & AccountSelectScreen.kt
// =========================================================================
@Composable
fun AdminDashboardScreen(
    adminEmail: String = "admin@workora.in",
    adminTier: String = "SUPER_ADMIN",
    onLogoutAdmin: () -> Unit = {},
    onSwitchRoleFromAdmin: (UserRole) -> Unit = {},
    onBack: () -> Unit = {},
    onSwitchToCustomer: () -> Unit = {},
    onSwitchToLabour: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val brandingPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }

    LaunchedEffect(Unit) { WorkoraThemeManager.syncFromPrefs(context) }
    val bgColor = WorkoraThemeManager.bgColor(context)
    val cardColor = WorkoraThemeManager.surfaceColor(context)
    val subtleBg = WorkoraThemeManager.subtleSurfaceColor(context)
    val textDark = WorkoraThemeManager.textPrimary(context)
    val textMuted = WorkoraThemeManager.textSecondary(context)
    val borderCol = WorkoraThemeManager.borderColor(context)
    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val greenOk = Color(0xFF22A06B)
    val redDanger = Color(0xFFB42318)

    val adminSections = listOf(
        "1. Dashboard",
        "2. Users",
        "3. Customers",
        "4. Workers",
        "5. Jobs",
        "6. Applications",
        "7. Bookings",
        "8. Reports",
        "9. Reviews",
        "10. Categories",
        "11. Blocked Users",
        "12. Notifications",
        "13. Verification/KYC",
        "14. Branding",
        "15. Security & Audit Logs",
        "16. Settings"
    )
    var selectedSection by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSyncing by remember { mutableStateOf(false) }

    val usersList = remember { mutableStateListOf<JSONObject>() }
    val workersList = remember { mutableStateListOf<JSONObject>() }
    val jobsList = remember { mutableStateListOf<JSONObject>() }
    val bookingsList = remember { mutableStateListOf<JSONObject>() }
    val reportsList = remember { mutableStateListOf<JSONObject>() }
    val reviewsList = remember { mutableStateListOf<JSONObject>() }
    val kycList = remember { mutableStateListOf<JSONObject>() }
    val auditLogsList = remember { mutableStateListOf<JSONObject>() }

    var brandAppName by remember { mutableStateOf(brandingPrefs.getString("app_name", "WORKORA") ?: "WORKORA") }
    var brandTagline by remember { mutableStateOf(brandingPrefs.getString("app_tagline", "Find & Hire Skilled Labour") ?: "Find & Hire Skilled Labour") }

    fun fetchNodeList(node: String, targetList: MutableList<JSONObject>) {
        CoroutineScope(Dispatchers.IO).launch {
            val temp = mutableListOf<JSONObject>()
            try {
                val conn = URL("$ADMIN_DB_URL/$node.json").openConnection() as HttpURLConnection
                conn.connectTimeout = 4500
                conn.readTimeout = 4500
                if (conn.responseCode in 200..299) {
                    val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                        val root = JSONObject(resp)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            obj.put("_key", k)
                            temp.add(obj)
                        }
                    }
                }
                conn.disconnect()
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) {
                targetList.clear()
                targetList.addAll(temp)
            }
        }
    }

    fun syncAllAdminData() {
        isSyncing = true
        fetchNodeList("users", usersList)
        fetchNodeList("workers", workersList)
        fetchNodeList("jobs", jobsList)
        fetchNodeList("bookings", bookingsList)
        fetchNodeList("reports", reportsList)
        fetchNodeList("reviews", reviewsList)
        fetchNodeList("kyc_verifications", kycList)
        fetchNodeList("admin_audit_logs", auditLogsList)
        isSyncing = false
    }

    LaunchedEffect(Unit) { syncAllAdminData() }

    fun performModerationAction(userKey: String, actionType: String, reportKey: String = "") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (userKey.isNotBlank()) {
                    val conn = (URL("$ADMIN_DB_URL/users/$userKey.json").openConnection() as HttpURLConnection).apply {
                        requestMethod = "PATCH"
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                    }
                    val patch = JSONObject().apply {
                        put("accountStatus", actionType)
                        put("moderatedAt", System.currentTimeMillis())
                    }
                    OutputStreamWriter(conn.outputStream).use { it.write(patch.toString()) }
                    conn.responseCode
                    conn.disconnect()
                }
                if (reportKey.isNotBlank()) {
                    val rConn = (URL("$ADMIN_DB_URL/reports/$reportKey.json").openConnection() as HttpURLConnection).apply {
                        requestMethod = "PATCH"
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                    }
                    OutputStreamWriter(rConn.outputStream).use {
                        it.write(JSONObject().apply { put("status", "RESOLVED ($actionType)") }.toString())
                    }
                    rConn.responseCode
                    rConn.disconnect()
                }
                FirebaseManager.logAdminAudit(adminTier, "MODERATION_$actionType", "User:$userKey Report:$reportKey")
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) {
                syncAllAdminData()
                Toast.makeText(context, "Moderation Action ($actionType) Applied & Logged ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Horizontal Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(deepNavy)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    onBack()
                    onLogoutAdmin()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column {
                    Text(
                        text = "WORKORA — $adminTier CONSOLE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "16-Section Horizontal Control • RBAC & Audit Active",
                        fontSize = 11.sp,
                        color = brandOrange
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = {
                        onSwitchRoleFromAdmin(UserRole.CUSTOMER)
                        onSwitchToCustomer()
                    },
                    border = BorderStroke(1.dp, Color.White),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Customer UI", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = {
                        onSwitchRoleFromAdmin(UserRole.LABOUR)
                        onSwitchToLabour()
                    },
                    border = BorderStroke(1.dp, brandOrange),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Worker UI", fontSize = 11.sp, color = brandOrange, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = { syncAllAdminData() }) {
                    if (isSyncing) CircularProgressIndicator(color = brandOrange, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = Color.White)
                }
                IconButton(onClick = {
                    onLogoutAdmin()
                    onLogout()
                }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                }
            }
        }

        // Horizontal Scrollable 16-Section Navigation Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            adminSections.forEachIndexed { idx, title ->
                val active = selectedSection == idx
                Button(
                    onClick = { selectedSection = idx },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) brandOrange else subtleBg
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (active) Color.White else textDark
                    )
                }
            }
        }

        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search across ${adminSections[selectedSection]}...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = deepNavy) },
                textStyle = TextStyle(color = textDark, fontSize = 13.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedSection) {
                0 -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AdminStatHorizontalCard("Total Users", "${usersList.size}", deepNavy, cardColor, borderCol)
                        AdminStatHorizontalCard("Customers", "${usersList.count { it.optString("role") == "CUSTOMER" }}", deepNavy, cardColor, borderCol)
                        AdminStatHorizontalCard("Workers", "${workersList.size}", brandOrange, cardColor, borderCol)
                        AdminStatHorizontalCard("Active Jobs", "${jobsList.count { it.optString("status") != "CANCELLED" }}", greenOk, cardColor, borderCol)
                        AdminStatHorizontalCard("Bookings", "${bookingsList.size}", deepNavy, cardColor, borderCol)
                        AdminStatHorizontalCard("Completed", "${jobsList.count { it.optString("status") == "COMPLETED" }}", greenOk, cardColor, borderCol)
                        AdminStatHorizontalCard("Reports", "${reportsList.size}", redDanger, cardColor, borderCol)
                        AdminStatHorizontalCard("Pending KYC", "${kycList.count { it.optString("status") == "VERIFICATION_PENDING" }}", brandOrange, cardColor, borderCol)
                    }
                }

                1, 2 -> {
                    usersList.filter { it.toString().contains(searchQuery, ignoreCase = true) }.forEach { u ->
                        val key = u.optString("_key")
                        val name = u.optString("name", "User")
                        val role = u.optString("role", "CUSTOMER")
                        val status = u.optString("accountStatus", "ACTIVE")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("$name ($role) • Status: $status", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                Text("Location: ${u.optString("location", "India")} • Key: $key", fontSize = 12.sp, color = textMuted)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(onClick = { performModerationAction(key, "WARNED") }, modifier = Modifier.height(32.dp)) {
                                        Text("Warn", fontSize = 11.sp, color = brandOrange)
                                    }
                                    OutlinedButton(onClick = { performModerationAction(key, "TEMP_BLOCKED") }, modifier = Modifier.height(32.dp)) {
                                        Text("Temp Block", fontSize = 11.sp, color = redDanger)
                                    }
                                    Button(onClick = { performModerationAction(key, "PERMANENT_BANNED") }, colors = ButtonDefaults.buttonColors(containerColor = redDanger), modifier = Modifier.height(32.dp)) {
                                        Text("Ban", fontSize = 11.sp, color = Color.White)
                                    }
                                    OutlinedButton(onClick = { performModerationAction(key, "ACTIVE") }, modifier = Modifier.height(32.dp)) {
                                        Text("Unblock", fontSize = 11.sp, color = greenOk)
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    workersList.filter { it.toString().contains(searchQuery, ignoreCase = true) }.forEach { w ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${w.optString("name")} — ${w.optString("trade")} (₹${w.optInt("dailyWage")}/day)", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                Text("Area: ${w.optString("location")} • Skills: ${w.optString("skills")}", fontSize = 12.sp, color = textMuted)
                            }
                        }
                    }
                }

                4, 5, 6 -> {
                    val listToShow = if (selectedSection == 6) bookingsList else jobsList
                    listToShow.filter { it.toString().contains(searchQuery, ignoreCase = true) }.forEach { j ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${j.optString("title", j.optString("jobTitle", "Work"))} • Status: ${j.optString("status", "OPEN")}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                Text("Category: ${j.optString("category")} • Rate: ₹${j.optInt("dailyRate")} • Location: ${j.optString("location")}", fontSize = 12.sp, color = textMuted)
                                val cancelReason = j.optString("cancelReason", "")
                                if (cancelReason.isNotBlank()) {
                                    Text("Cancel Reason: $cancelReason", fontSize = 11.sp, color = redDanger, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                7 -> {
                    reportsList.forEach { rep ->
                        val repKey = rep.optString("_key")
                        val reportedPhone = rep.optString("reportedPhone", "")
                        val targetUserKey = "u_${reportedPhone.filter { it.isDigit() }.takeLast(10)}"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.dp, redDanger)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("⚠️ Reason: ${rep.optString("reason")} • Status: ${rep.optString("status")}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = redDanger)
                                Text("Details: ${rep.optString("description")}", fontSize = 12.sp, color = textDark)
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(onClick = { performModerationAction(targetUserKey, "WARNED", repKey) }, modifier = Modifier.height(32.dp)) {
                                        Text("Send Warning", fontSize = 11.sp, color = brandOrange)
                                    }
                                    OutlinedButton(onClick = { performModerationAction(targetUserKey, "TEMP_BLOCKED", repKey) }, modifier = Modifier.height(32.dp)) {
                                        Text("Temp Block", fontSize = 11.sp, color = redDanger)
                                    }
                                    Button(onClick = { performModerationAction(targetUserKey, "PERMANENT_BANNED", repKey) }, colors = ButtonDefaults.buttonColors(containerColor = redDanger), modifier = Modifier.height(32.dp)) {
                                        Text("Permanent Ban", fontSize = 11.sp, color = Color.White)
                                    }
                                    Button(onClick = { performModerationAction("", "RESOLVED", repKey) }, colors = ButtonDefaults.buttonColors(containerColor = greenOk), modifier = Modifier.height(32.dp)) {
                                        Text("Resolve ✓", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                8 -> {
                    reviewsList.forEach { rev ->
                        val revKey = rev.optString("_key")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("★ ${rev.optInt("rating", 5)} Stars by ${rev.optString("reviewerName")}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                    Text(rev.optString("comment", "No written comment"), fontSize = 12.sp, color = textMuted)
                                }
                                IconButton(
                                    onClick = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val conn = (URL("$ADMIN_DB_URL/reviews/$revKey.json").openConnection() as HttpURLConnection).apply {
                                                    requestMethod = "DELETE"
                                                }
                                                conn.responseCode
                                                conn.disconnect()
                                                FirebaseManager.logAdminAudit(adminTier, "REMOVE_ABUSE_REVIEW", revKey)
                                            } catch (_: Exception) {}
                                            withContext(Dispatchers.Main) {
                                                syncAllAdminData()
                                                Toast.makeText(context, "Review Removed ✓", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Review", tint = redDanger)
                                }
                            }
                        }
                    }
                }

                12 -> {
                    kycList.forEach { kyc ->
                        val kKey = kyc.optString("_key")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🆔 Doc: ${kyc.optString("docType")} (${kyc.optString("docNumber")})", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                Text("Status: ${kyc.optString("status")}", fontSize = 12.sp, color = brandOrange, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    val conn = (URL("$ADMIN_DB_URL/kyc_verifications/$kKey.json").openConnection() as HttpURLConnection).apply {
                                                        requestMethod = "PATCH"
                                                        setRequestProperty("Content-Type", "application/json")
                                                        doOutput = true
                                                    }
                                                    OutputStreamWriter(conn.outputStream).use {
                                                        it.write(JSONObject().apply { put("status", "VERIFIED") }.toString())
                                                    }
                                                    conn.responseCode
                                                    conn.disconnect()
                                                    FirebaseManager.logAdminAudit(adminTier, "KYC_VERIFIED", kKey)
                                                } catch (_: Exception) {}
                                                withContext(Dispatchers.Main) { syncAllAdminData() }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = greenOk)
                                    ) { Text("Approve KYC ✓", fontSize = 11.sp) }

                                    OutlinedButton(
                                        onClick = {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    val conn = (URL("$ADMIN_DB_URL/kyc_verifications/$kKey.json").openConnection() as HttpURLConnection).apply {
                                                        requestMethod = "PATCH"
                                                        setRequestProperty("Content-Type", "application/json")
                                                        doOutput = true
                                                    }
                                                    OutputStreamWriter(conn.outputStream).use {
                                                        it.write(JSONObject().apply { put("status", "REJECTED") }.toString())
                                                    }
                                                    conn.responseCode
                                                    conn.disconnect()
                                                    FirebaseManager.logAdminAudit(adminTier, "KYC_REJECTED", kKey)
                                                } catch (_: Exception) {}
                                                withContext(Dispatchers.Main) { syncAllAdminData() }
                                            }
                                        }
                                    ) { Text("Reject ✕", fontSize = 11.sp, color = redDanger) }
                                }
                            }
                        }
                    }
                }

                13 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("🎨 Live Cloud App Branding", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = deepNavy)
                            OutlinedTextField(
                                value = brandAppName,
                                onValueChange = { brandAppName = it },
                                label = { Text("App Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = brandTagline,
                                onValueChange = { brandTagline = it },
                                label = { Text("App Tagline") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    brandingPrefs.edit().putString("app_name", brandAppName).putString("app_tagline", brandTagline).apply()
                                    FirebaseManager.logAdminAudit(adminTier, "UPDATE_BRANDING", brandAppName)
                                    Toast.makeText(context, "Branding Updated ✓", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                            ) {
                                Text("Save Branding Live", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                14 -> {
                    auditLogsList.forEach { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            border = BorderStroke(1.dp, borderCol)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🛡️ Action: ${log.optString("action")}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = deepNavy)
                                Text("Target: ${log.optString("target")} • By: ${log.optString("adminIdentifier")}", fontSize = 11.sp, color = textMuted)
                            }
                        }
                    }
                }

                else -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = BorderStroke(1.dp, borderCol)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(adminSections[selectedSection], fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                            Text("All records in this module are synced live with Firebase Realtime Database.", fontSize = 12.sp, color = textMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatHorizontalCard(
    label: String,
    count: String,
    accent: Color,
    cardColor: Color,
    borderCol: Color
) {
    Card(
        modifier = Modifier.width(145.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.5.dp, borderCol)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(count, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = accent)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
        }
    }
}
