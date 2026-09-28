package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private const val ADMIN_FIREBASE_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

data class AdminWorkerEntry(
    val key: String,
    val name: String,
    val trade: String,
    val dailyWage: Int,
    val location: String,
    val phone: String,
    val isVerified: Boolean = true
)

data class AdminJobEntry(
    val key: String,
    val title: String,
    val category: String,
    val dailyRate: Int,
    val location: String,
    val customerName: String,
    val status: String = "OPEN"
)

data class AdminUserEntry(
    val key: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String,
    val location: String,
    val status: String = "ACTIVE"
)

private fun decodeAdminLogoBitmap(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun encodeAdminUriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else ""
    } catch (_: Exception) {
        ""
    }
}

@Composable
fun AdminDashboardScreen(
    adminEmail: String = "",
    adminTier: String = "SUPER_ADMIN",
    onLogoutAdmin: () -> Unit = {},
    onSwitchRoleFromAdmin: (String) -> Unit = {},
    onBack: () -> Unit = {},
    onSwitchToCustomer: () -> Unit = {},
    onSwitchToLabour: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val brandingPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }

    // Original Crisp White + Navy Blue + Orange Theme
    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val bgLight = Color(0xFFF8FAFC)
    val cardWhite = Color.White
    val textDark = Color(0xFF102A43)
    val textMuted = Color(0xFF667085)
    val borderLight = Color(0xFFE5E7EB)
    val greenTrusted = Color(0xFF22A06B)
    val redDanger = Color(0xFFB42318)

    // 0 = 📊 Overview | 1 = 🎨 Branding | 2 = 👷 Workers | 3 = 📋 Jobs | 4 = 👥 Users
    var activeTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var isSyncingCloud by remember { mutableStateOf(false) }
    var showAddWorkerModal by remember { mutableStateOf(false) }

    // Live Branding State
    var appNameInput by remember {
        mutableStateOf(brandingPrefs.getString("app_name", "WORKORA") ?: "WORKORA")
    }
    var appTaglineInput by remember {
        mutableStateOf(brandingPrefs.getString("app_tagline", "Find & Hire Skilled Labour") ?: "Find & Hire Skilled Labour")
    }
    var bannerHeadingInput by remember {
        mutableStateOf(brandingPrefs.getString("banner_text", "Find Skilled\nWorkers Near You") ?: "Find Skilled\nWorkers Near You")
    }
    var bannerSubtextInput by remember {
        mutableStateOf(brandingPrefs.getString("banner_subtext", "Get your work done easily\nand safely.") ?: "Get your work done easily\nand safely.")
    }
    var logoBase64 by remember {
        mutableStateOf(brandingPrefs.getString("logo_base64", "") ?: "")
    }
    var isSavingBranding by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeAdminUriToBase64(context, uri)
            if (encoded.isNotBlank()) {
                logoBase64 = encoded
                brandingPrefs.edit().putString("logo_base64", encoded).apply()
                Toast.makeText(context, "New App Logo Selected ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val workersList = remember {
        mutableStateListOf(
            AdminWorkerEntry("w_ramesh", "Ramesh Kumar", "Mason", 600, "Silwani, Raisen (MP)", "+91 9876500001", true),
            AdminWorkerEntry("w_suresh", "Suresh Patel", "Electrician", 550, "Silwani, Raisen (MP)", "+91 9876500002", true),
            AdminWorkerEntry("w_amit", "Amit Yadav", "Plumber", 500, "Silwani, Raisen (MP)", "+91 9876500003", true)
        )
    }

    val jobsList = remember {
        mutableStateListOf(
            AdminJobEntry("job_sample_1", "House Repair & Wall Plastering", "Mason", 600, "Silwani, Raisen (MP)", "Ramesh Verma", "OPEN"),
            AdminJobEntry("job_sample_2", "Complete House Wiring & Fan Fitting", "Electrician", 550, "Silwani, Raisen (MP)", "Suresh Yadav", "OPEN")
        )
    }

    val usersList = remember {
        mutableStateListOf(
            AdminUserEntry("u_sample_1", "Ramesh Verma", "+91 9876500001", "customer@workora.in", "CUSTOMER", "Silwani, Raisen (MP)", "ACTIVE"),
            AdminUserEntry("u_sample_2", "Suresh Patel", "+91 9876500002", "worker@workora.in", "LABOUR", "Silwani, Raisen (MP)", "ACTIVE")
        )
    }

    fun syncAdminDataFromFirebase() {
        isSyncingCloud = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Sync Workers
                val wConn = URL("$ADMIN_FIREBASE_URL/workers.json").openConnection() as HttpURLConnection
                wConn.connectTimeout = 5000
                wConn.readTimeout = 5000
                if (wConn.responseCode in 200..299) {
                    val resp = BufferedReader(InputStreamReader(wConn.inputStream)).use { it.readText() }
                    if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                        val root = JSONObject(resp)
                        val keys = root.keys()
                        val fetched = mutableListOf<AdminWorkerEntry>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val name = obj.optString("name", "")
                            if (name.isNotBlank()) {
                                fetched.add(
                                    AdminWorkerEntry(
                                        key = k,
                                        name = name,
                                        trade = obj.optString("trade", "Mason"),
                                        dailyWage = obj.optInt("dailyWage", 600),
                                        location = obj.optString("location", "Silwani, Raisen (MP)"),
                                        phone = obj.optString("phone", ""),
                                        isVerified = obj.optBoolean("isVerified", true)
                                    )
                                )
                            }
                        }
                        if (fetched.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                workersList.clear()
                                workersList.addAll(fetched)
                            }
                        }
                    }
                }
                wConn.disconnect()

                // 2. Sync Jobs
                val jConn = URL("$ADMIN_FIREBASE_URL/jobs.json").openConnection() as HttpURLConnection
                jConn.connectTimeout = 5000
                jConn.readTimeout = 5000
                if (jConn.responseCode in 200..299) {
                    val resp = BufferedReader(InputStreamReader(jConn.inputStream)).use { it.readText() }
                    if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                        val root = JSONObject(resp)
                        val keys = root.keys()
                        val fetched = mutableListOf<AdminJobEntry>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val title = obj.optString("title", "")
                            if (title.isNotBlank()) {
                                fetched.add(
                                    AdminJobEntry(
                                        key = k,
                                        title = title,
                                        category = obj.optString("category", "Mason"),
                                        dailyRate = obj.optInt("dailyRate", 600),
                                        location = obj.optString("location", "Silwani, Raisen (MP)"),
                                        customerName = obj.optString("customerName", "Customer"),
                                        status = obj.optString("status", "OPEN")
                                    )
                                )
                            }
                        }
                        if (fetched.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                jobsList.clear()
                                jobsList.addAll(fetched)
                            }
                        }
                    }
                }
                jConn.disconnect()

                // 3. Sync Users
                val uConn = URL("$ADMIN_FIREBASE_URL/users.json").openConnection() as HttpURLConnection
                uConn.connectTimeout = 5000
                uConn.readTimeout = 5000
                if (uConn.responseCode in 200..299) {
                    val resp = BufferedReader(InputStreamReader(uConn.inputStream)).use { it.readText() }
                    if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                        val root = JSONObject(resp)
                        val keys = root.keys()
                        val fetched = mutableListOf<AdminUserEntry>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val name = obj.optString("name", "")
                            if (name.isNotBlank()) {
                                fetched.add(
                                    AdminUserEntry(
                                        key = k,
                                        name = name,
                                        phone = obj.optString("phone", ""),
                                        email = obj.optString("email", ""),
                                        role = obj.optString("role", "CUSTOMER"),
                                        location = obj.optString("location", "Silwani, Raisen (MP)"),
                                        status = obj.optString("accountStatus", "ACTIVE")
                                    )
                                )
                            }
                        }
                        if (fetched.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                usersList.clear()
                                usersList.addAll(fetched)
                            }
                        }
                    }
                }
                uConn.disconnect()
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                isSyncingCloud = false
            }
        }
    }

    LaunchedEffect(Unit) {
        syncAdminDataFromFirebase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // ==================== TOP NAVY HEADER BAR ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(deepNavy)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = {
                    onBack()
                    onLogoutAdmin()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "WORKORA ADMIN PANEL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Live Cloud Control • $adminTier",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = brandOrange
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { syncAdminDataFromFirebase() }) {
                    if (isSyncingCloud) {
                        CircularProgressIndicator(color = brandOrange, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = Color.White)
                    }
                }
                IconButton(onClick = {
                    onLogoutAdmin()
                    onLogout()
                }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                }
            }
        }

        // ==================== QUICK ROLE SWITCHER STRIP ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardWhite)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    onSwitchRoleFromAdmin("CUSTOMER")
                    onSwitchToCustomer()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = deepNavy),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Customer Mode", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }

            Button(
                onClick = {
                    onSwitchRoleFromAdmin("LABOUR")
                    onSwitchToLabour()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Worker Mode", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        // ==================== 5 MAIN ADMIN TABS ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                0 to "📊 Overview",
                1 to "🎨 Branding",
                2 to "👷 Workers (${workersList.size})",
                3 to "📋 Jobs (${jobsList.size})",
                4 to "👥 Users (${usersList.size})"
            ).forEach { (idx, label) ->
                val selected = activeTab == idx
                Button(
                    onClick = { activeTab = idx },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) deepNavy else cardWhite
                    ),
                    border = BorderStroke(1.dp, if (selected) deepNavy else borderLight),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (selected) Color.White else textDark
                    )
                }
            }
        }

        // ==================== MAIN CONTENT BODY ====================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (activeTab) {
                0 -> {
                    // ==================== TAB 0: 📊 OVERVIEW ====================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminOverviewStatCard(
                            title = "Total Users",
                            value = "${usersList.size}",
                            subtitle = "Verified Accounts",
                            accentColor = deepNavy,
                            modifier = Modifier.weight(1f),
                            onClick = { activeTab = 4 }
                        )
                        AdminOverviewStatCard(
                            title = "Active Workers",
                            value = "${workersList.size}",
                            subtitle = "Ready for Work",
                            accentColor = brandOrange,
                            modifier = Modifier.weight(1f),
                            onClick = { activeTab = 2 }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminOverviewStatCard(
                            title = "Posted Jobs",
                            value = "${jobsList.size}",
                            subtitle = "Live Customer Jobs",
                            accentColor = greenTrusted,
                            modifier = Modifier.weight(1f),
                            onClick = { activeTab = 3 }
                        )
                        AdminOverviewStatCard(
                            title = "Cloud Status",
                            value = "ONLINE ✓",
                            subtitle = "Firebase RTDB Live",
                            accentColor = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f),
                            onClick = { syncAdminDataFromFirebase() }
                        )
                    }

                    // Quick Branding Preview Card on Overview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardWhite),
                        border = BorderStroke(1.dp, borderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎨 Live App Branding & Logo",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = deepNavy
                                )
                                Text(
                                    text = "Edit >",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = brandOrange,
                                    modifier = Modifier.clickable { activeTab = 1 }
                                )
                            }
                            Text("• App Name: $appNameInput", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textDark)
                            Text("• Tagline: $appTaglineInput", fontSize = 12.sp, color = textMuted)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { activeTab = 1 },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Customize Branding", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { showAddWorkerModal = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.2.dp, deepNavy)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = deepNavy, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Worker", color = deepNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Recent Workers Preview on Overview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardWhite),
                        border = BorderStroke(1.dp, borderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👷 Recent Verified Workers", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                Text(
                                    text = "View All >",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = deepNavy,
                                    modifier = Modifier.clickable { activeTab = 2 }
                                )
                            }
                            workersList.take(3).forEach { w ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bgLight)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${w.name} (${w.trade})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textDark)
                                        Text("📍 ${w.location}", fontSize = 11.sp, color = textMuted)
                                    }
                                    Text("₹${w.dailyWage}/day", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = deepNavy)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // ==================== TAB 1: 🎨 BRANDING & LOGO EDITOR ====================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardWhite),
                        border = BorderStroke(1.dp, borderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "🎨 Live App Branding & Logo Control",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = deepNavy
                            )
                            Text(
                                text = "Changes saved here update live across both Customer and Worker dashboards via Firebase.",
                                fontSize = 12.sp,
                                color = textMuted
                            )

                            // Custom Logo Picker Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgLight)
                                    .padding(12.dp)
                            ) {
                                val bmp = remember(logoBase64) { decodeAdminLogoBitmap(logoBase64) }
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(deepNavy)
                                        .clickable { logoPickerLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp,
                                            contentDescription = "App Logo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("App Header Logo", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                    Text("Tap to upload custom logo from gallery", fontSize = 11.sp, color = textMuted)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { logoPickerLauncher.launch("image/*") },
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Text("Upload Logo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deepNavy)
                                        }
                                        if (logoBase64.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = {
                                                    logoBase64 = ""
                                                    brandingPrefs.edit().remove("logo_base64").apply()
                                                },
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                            ) {
                                                Text("Reset W Logo", fontSize = 11.sp, color = redDanger)
                                            }
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = appNameInput,
                                onValueChange = { appNameInput = it },
                                label = { Text("App Name (Header Title)") },
                                textStyle = TextStyle(color = textDark, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = appTaglineInput,
                                onValueChange = { appTaglineInput = it },
                                label = { Text("App Tagline (Subtitle)") },
                                textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = bannerHeadingInput,
                                onValueChange = { bannerHeadingInput = it },
                                label = { Text("Home Banner Main Heading") },
                                textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = bannerSubtextInput,
                                onValueChange = { bannerSubtextInput = it },
                                label = { Text("Home Banner Subtext") },
                                textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    isSavingBranding = true
                                    brandingPrefs.edit().apply {
                                        putString("app_name", appNameInput.trim())
                                        putString("app_tagline", appTaglineInput.trim())
                                        putString("banner_text", bannerHeadingInput.trim())
                                        putString("banner_subtext", bannerSubtextInput.trim())
                                        putString("logo_base64", logoBase64)
                                        apply()
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val conn = (URL("$ADMIN_FIREBASE_URL/app_branding.json").openConnection() as HttpURLConnection).apply {
                                                requestMethod = "PUT"
                                                setRequestProperty("Content-Type", "application/json")
                                                doOutput = true
                                            }
                                            val json = JSONObject().apply {
                                                put("app_name", appNameInput.trim())
                                                put("app_tagline", appTaglineInput.trim())
                                                put("banner_text", bannerHeadingInput.trim())
                                                put("banner_subtext", bannerSubtextInput.trim())
                                                put("updatedAt", System.currentTimeMillis())
                                            }
                                            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                            conn.responseCode
                                            conn.disconnect()
                                        } catch (_: Exception) {}
                                        withContext(Dispatchers.Main) {
                                            isSavingBranding = false
                                            Toast.makeText(context, "App Branding Saved Live to Cloud ✓", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !isSavingBranding,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                            ) {
                                if (isSavingBranding) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Branding Live to Cloud ✓", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ==================== TAB 2: 👷 MANAGE WORKERS ====================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👷 Manage Workers (${workersList.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textDark
                        )
                        Button(
                            onClick = { showAddWorkerModal = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Worker", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search worker by name, skill or location...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = deepNavy) },
                        textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cardWhite,
                            unfocusedContainerColor = cardWhite
                        )
                    )

                    workersList.filter {
                        searchQuery.isBlank() ||
                                it.name.contains(searchQuery, ignoreCase = true) ||
                                it.trade.contains(searchQuery, ignoreCase = true) ||
                                it.location.contains(searchQuery, ignoreCase = true)
                    }.forEach { worker ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardWhite),
                            border = BorderStroke(1.dp, borderLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(worker.name, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (worker.isVerified) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = "Verified", tint = greenTrusted, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Text("${worker.trade} • ₹${worker.dailyWage}/day", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                                    Text("📍 ${worker.location}", fontSize = 12.sp, color = textMuted)
                                }

                                IconButton(
                                    onClick = {
                                        workersList.remove(worker)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val conn = (URL("$ADMIN_FIREBASE_URL/workers/${worker.key}.json").openConnection() as HttpURLConnection).apply {
                                                    requestMethod = "DELETE"
                                                }
                                                conn.responseCode
                                                conn.disconnect()
                                            } catch (_: Exception) {}
                                        }
                                        Toast.makeText(context, "${worker.name} removed ✓", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = redDanger)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // ==================== TAB 3: 📋 MANAGE JOBS ====================
                    Text(
                        text = "📋 Posted Customer Jobs (${jobsList.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )

                    jobsList.forEach { job ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardWhite),
                            border = BorderStroke(1.dp, borderLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(job.title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                    Text("${job.category} • ₹${job.dailyRate}/day • Status: ${job.status}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = deepNavy)
                                    Text("📍 ${job.location} • By: ${job.customerName}", fontSize = 12.sp, color = textMuted)
                                }

                                IconButton(
                                    onClick = {
                                        jobsList.remove(job)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val conn = (URL("$ADMIN_FIREBASE_URL/jobs/${job.key}.json").openConnection() as HttpURLConnection).apply {
                                                    requestMethod = "DELETE"
                                                }
                                                conn.responseCode
                                                conn.disconnect()
                                            } catch (_: Exception) {}
                                        }
                                        Toast.makeText(context, "Job removed ✓", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Job", tint = redDanger)
                                }
                            }
                        }
                    }
                }

                else -> {
                    // ==================== TAB 4: 👥 MANAGE USERS ====================
                    Text(
                        text = "👥 Registered Users (${usersList.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )

                    usersList.forEach { u ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardWhite),
                            border = BorderStroke(1.dp, borderLight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${u.name} (${u.role})", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (u.status == "ACTIVE") Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = u.status,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (u.status == "ACTIVE") greenTrusted else redDanger
                                        )
                                    }
                                }
                                Text("📍 ${u.location}", fontSize = 12.sp, color = textMuted)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            val newStatus = if (u.status == "ACTIVE") "BLOCKED" else "ACTIVE"
                                            val idx = usersList.indexOf(u)
                                            if (idx >= 0) usersList[idx] = u.copy(status = newStatus)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    val conn = (URL("$ADMIN_FIREBASE_URL/users/${u.key}.json").openConnection() as HttpURLConnection).apply {
                                                        requestMethod = "PATCH"
                                                        setRequestProperty("Content-Type", "application/json")
                                                        doOutput = true
                                                    }
                                                    OutputStreamWriter(conn.outputStream).use {
                                                        it.write(JSONObject().apply { put("accountStatus", newStatus) }.toString())
                                                    }
                                                    conn.responseCode
                                                    conn.disconnect()
                                                } catch (_: Exception) {}
                                            }
                                            Toast.makeText(context, "${u.name} marked $newStatus ✓", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(
                                            text = if (u.status == "ACTIVE") "Block User" else "Unblock User",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (u.status == "ACTIVE") redDanger else greenTrusted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Add New Verified Worker Modal
    if (showAddWorkerModal) {
        var wName by remember { mutableStateOf("") }
        var wTrade by remember { mutableStateOf("Mason") }
        var wWage by remember { mutableStateOf("600") }
        var wLoc by remember { mutableStateOf("Silwani, Raisen (MP)") }
        var wPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddWorkerModal = false },
            containerColor = cardWhite,
            title = {
                Text("👷 Add Verified Worker", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = deepNavy)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = wName, onValueChange = { wName = it }, label = { Text("Worker Full Name") }, singleLine = true)
                    OutlinedTextField(value = wTrade, onValueChange = { wTrade = it }, label = { Text("Trade (Mason, Electrician, Plumber)") }, singleLine = true)
                    OutlinedTextField(value = wWage, onValueChange = { wWage = it.filter { c -> c.isDigit() } }, label = { Text("Daily Wage (₹/day)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    OutlinedTextField(value = wLoc, onValueChange = { wLoc = it }, label = { Text("Location / Area") }, singleLine = true)
                    OutlinedTextField(value = wPhone, onValueChange = { wPhone = it }, label = { Text("Phone Number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (wName.isNotBlank()) {
                            val key = "w_${System.currentTimeMillis()}"
                            val wageInt = wWage.toIntOrNull() ?: 600
                            val newEntry = AdminWorkerEntry(key, wName.trim(), wTrade.trim(), wageInt, wLoc.trim(), wPhone.trim(), true)
                            workersList.add(0, newEntry)
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val conn = (URL("$ADMIN_FIREBASE_URL/workers/$key.json").openConnection() as HttpURLConnection).apply {
                                        requestMethod = "PUT"
                                        setRequestProperty("Content-Type", "application/json")
                                        doOutput = true
                                    }
                                    val json = JSONObject().apply {
                                        put("name", wName.trim())
                                        put("trade", wTrade.trim())
                                        put("dailyWage", wageInt)
                                        put("location", wLoc.trim())
                                        put("phone", wPhone.trim())
                                        put("isAvailableToday", true)
                                        put("isVerified", true)
                                    }
                                    OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                    conn.responseCode
                                    conn.disconnect()
                                } catch (_: Exception) {}
                            }
                            showAddWorkerModal = false
                            Toast.makeText(context, "Worker Added Live ✓", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                ) {
                    Text("Save Worker", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddWorkerModal = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminOverviewStatCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667085)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color(0xFF667085)
            )
        }
    }
}
