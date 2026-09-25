package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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

private fun encodeBitmapToBase64(bitmap: Bitmap): String {
    return try {
        val scaled = Bitmap.createScaledBitmap(bitmap, 220, 220, true)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        ""
    }
}

private fun decodeBase64ToImageBitmap(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

@Composable
fun AdminDashboardScreen(
    adminEmail: String = "ankitah994@gmail.com",
    adminTier: String = "SUPER_ADMIN",
    onLogoutAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val brandPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }

    var isLoading by remember { mutableStateOf(true) }
    var activeTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Track whether branding is saved (false = Orange Save Icon, true = White Save Icon)
    var isBrandingSaved by remember { mutableStateOf(false) }

    // App Branding & Logo Editor States
    var customAppName by remember { mutableStateOf(brandPrefs.getString("app_name", "WORKORA") ?: "WORKORA") }
    var customAppTagline by remember { mutableStateOf(brandPrefs.getString("app_tagline", "FIND. HIRE. WORK.") ?: "FIND. HIRE. WORK.") }
    var customWelcomeHeading by remember { mutableStateOf(brandPrefs.getString("welcome_heading", "What do you want to do?") ?: "What do you want to do?") }
    var customSupportPhone by remember { mutableStateOf(brandPrefs.getString("support_phone", "+91 6265798340") ?: "+91 6265798340") }
    var customBannerText by remember { mutableStateOf(brandPrefs.getString("banner_text", "Silwani & Raisen ke sabhi verified mistri aur workers ab online!") ?: "Silwani & Raisen ke sabhi verified mistri aur workers ab online!") }
    var customCategoriesList by remember { mutableStateOf(brandPrefs.getString("service_categories", "Mistri, Electrician, Plumber, Painter, Carpenter, Welder, Farm Labour") ?: "Mistri, Electrician, Plumber, Painter, Carpenter, Welder, Farm Labour") }
    var customLogoBase64 by remember { mutableStateOf(brandPrefs.getString("logo_base64", "") ?: "") }
    var customLogoBitmap by remember { mutableStateOf<ImageBitmap?>(decodeBase64ToImageBitmap(customLogoBase64)) }

    val usersList = remember { mutableStateListOf<AdminUserItem>() }
    val workersList = remember { mutableStateListOf<AdminWorkerItem>() }
    val jobsList = remember { mutableStateListOf<AdminJobItem>() }
    val auditLogs = remember { mutableStateListOf<AdminAuditLog>() }

    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMessage by remember { mutableStateOf("") }

    var newJobTitle by remember { mutableStateOf("") }
    var newJobCategory by remember { mutableStateOf("Mistri / Construction") }
    var newJobWage by remember { mutableStateOf("600") }
    var newJobLocation by remember { mutableStateOf("Silwani, Raisen") }

    var newAdminEmailInput by remember { mutableStateOf("") }
    var newAdminRoleInput by remember { mutableStateOf("SUPER_ADMIN") }

    var maintenanceMode by remember { mutableStateOf(false) }
    var allowRegistrations by remember { mutableStateOf(true) }
    var allowJobPosting by remember { mutableStateOf(true) }

    var showExitDialog by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bmp = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
                if (bmp != null) {
                    val file = File(context.filesDir, "workora_custom_logo.jpg")
                    FileOutputStream(file).use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                    val encoded = encodeBitmapToBase64(bmp)
                    customLogoBase64 = encoded
                    customLogoBitmap = bmp.asImageBitmap()
                    isBrandingSaved = false
                    brandPrefs.edit().putString("logo_base64", encoded).apply()
                    Toast.makeText(context, "Logo Select Ho Gaya! Ab Save Button Dabayein ✓", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Photo load nahi ho payi!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun loadAllLiveFirebaseData() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bConn = (URL("$ADMIN_DB_URL/app_branding.json").openConnection() as HttpURLConnection)
                if (bConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(bConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val obj = JSONObject(text)
                        val cloudName = obj.optString("appName", customAppName)
                        val cloudTagline = obj.optString("appTagline", customAppTagline)
                        val cloudHeading = obj.optString("welcomeHeading", customWelcomeHeading)
                        val cloudPhone = obj.optString("supportPhone", customSupportPhone)
                        val cloudBanner = obj.optString("bannerText", customBannerText)
                        val cloudCats = obj.optString("serviceCategories", customCategoriesList)
                        val cloudLogo = obj.optString("logoBase64", customLogoBase64)

                        brandPrefs.edit()
                            .putString("app_name", cloudName)
                            .putString("app_tagline", cloudTagline)
                            .putString("welcome_heading", cloudHeading)
                            .putString("support_phone", cloudPhone)
                            .putString("banner_text", cloudBanner)
                            .putString("service_categories", cloudCats)
                            .putString("logo_base64", cloudLogo)
                            .apply()

                        withContext(Dispatchers.Main) {
                            customAppName = cloudName
                            customAppTagline = cloudTagline
                            customWelcomeHeading = cloudHeading
                            customSupportPhone = cloudPhone
                            customBannerText = cloudBanner
                            customCategoriesList = cloudCats
                            if (cloudLogo.isNotBlank()) {
                                customLogoBase64 = cloudLogo
                                customLogoBitmap = decodeBase64ToImageBitmap(cloudLogo)
                            }
                        }
                    }
                }
                bConn.disconnect()

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

    fun saveAppBrandingToCloud() {
        isBrandingSaved = true
        brandPrefs.edit()
            .putString("app_name", customAppName.trim())
            .putString("app_tagline", customAppTagline.trim())
            .putString("welcome_heading", customWelcomeHeading.trim())
            .putString("support_phone", customSupportPhone.trim())
            .putString("banner_text", customBannerText.trim())
            .putString("service_categories", customCategoriesList.trim())
            .putString("logo_base64", customLogoBase64)
            .apply()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = (URL("$ADMIN_DB_URL/app_branding.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("appName", customAppName.trim())
                    put("appTagline", customAppTagline.trim())
                    put("welcomeHeading", customWelcomeHeading.trim())
                    put("supportPhone", customSupportPhone.trim())
                    put("bannerText", customBannerText.trim())
                    put("serviceCategories", customCategoriesList.trim())
                    put("logoBase64", customLogoBase64)
                    put("updatedBy", adminEmail)
                    put("updatedAt", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()

                FirebaseManager.recordAdminAuditLog(
                    adminEmail = adminEmail,
                    adminTier = adminTier,
                    actionType = "APP_BRANDING_UPDATED",
                    targetEntity = "AppConfig",
                    details = "Updated App Logo & Branding (${customAppName.trim()})"
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Saved! Save Icon White Ho Gaya ✓", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Saved Locally! ✓", Toast.LENGTH_SHORT).show()
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
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Compact Top Header Bar with proper statusBarsPadding so text never clips
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(WorkoraNavy)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = { onLogoutAdmin() },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { activeTab = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    if (customLogoBitmap != null) {
                        Image(
                            bitmap = customLogoBitmap!!,
                            contentDescription = "App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "$customAppName ADMIN PANEL",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "$adminTier • $adminEmail",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Refreshing Firebase...", Toast.LENGTH_SHORT).show()
                        loadAllLiveFirebaseData()
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = WorkoraOrange, modifier = Modifier.size(20.dp))
                }
                IconButton(
                    onClick = { showExitDialog = true },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Exit", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Compact Live Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Cloud Control Center",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                        Text(
                            text = if (isLoading) "Syncing..." else "● Firebase Connected",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AdminStatTile("Edit App/Logo", "🎨", WorkoraOrange, Modifier.weight(1f)) { activeTab = 0 }
                        AdminStatTile("Total Users", usersList.size.toString(), WorkoraNavy, Modifier.weight(1f)) { activeTab = 1 }
                        AdminStatTile("Workers", workersList.size.toString(), Color(0xFF16A34A), Modifier.weight(1f)) { activeTab = 2 }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AdminStatTile("Total Jobs", jobsList.size.toString(), Color(0xFF0284C7), Modifier.weight(1f)) { activeTab = 3 }
                        AdminStatTile("Blocked Users", usersList.count { it.accountStatus == "BLOCKED" }.toString(), Color(0xFFDC2626), Modifier.weight(1f)) { activeTab = 1 }
                        AdminStatTile("Audit Logs", auditLogs.size.toString(), WorkoraNavy, Modifier.weight(1f)) { activeTab = 6 }
                    }
                }
            }

            // 2. Left-Aligned Numbered Vertical Rows for "Select Control Tool"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Select Control Tool (नंबर पर टैप करें):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    val numberedTools = listOf(
                        "1. Edit App & Logo",
                        "2. Manage Users (${usersList.size})",
                        "3. Verify Workers (${workersList.size})",
                        "4. Manage Jobs (${jobsList.size})",
                        "5. Send Notification",
                        "6. Add Admin",
                        "7. Emergency & Logs"
                    )

                    numberedTools.forEachIndexed { index, toolTitle ->
                        val isSelected = activeTab == index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) WorkoraNavy else WorkoraBgLight)
                                .clickable { activeTab = index }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = toolTitle,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) Color.White else WorkoraTextDark
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = if (isSelected) WorkoraOrange else WorkoraTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // ==================== TAB 0: 1. EDIT APP & LOGO ====================
            if (activeTab == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.2.dp, WorkoraOrange)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "1. Edit App Logo, Name & Branding",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        // Compact App Logo Upload Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(WorkoraBgLight)
                                    .clickable { logoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (customLogoBitmap != null) {
                                    Image(
                                        bitmap = customLogoBitmap!!,
                                        contentDescription = "Custom App Logo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = null,
                                            tint = WorkoraOrange,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text("Logo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { logoPickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth().height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraNavy),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Select Logo from Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (customLogoBitmap != null) {
                                    OutlinedButton(
                                        onClick = {
                                            customLogoBase64 = ""
                                            customLogoBitmap = null
                                            isBrandingSaved = false
                                            File(context.filesDir, "workora_custom_logo.jpg").delete()
                                            brandPrefs.edit().remove("logo_base64").apply()
                                            Toast.makeText(context, "Default Logo Restored!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.fillMaxWidth().height(34.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text("Reset Default Logo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = customAppName,
                            onValueChange = {
                                customAppName = it
                                isBrandingSaved = false
                            },
                            label = { Text("App Name (जैसे: WORKORA)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = customAppTagline,
                            onValueChange = {
                                customAppTagline = it
                                isBrandingSaved = false
                            },
                            label = { Text("App Tagline (जैसे: FIND. HIRE. WORK.)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = customWelcomeHeading,
                            onValueChange = {
                                customWelcomeHeading = it
                                isBrandingSaved = false
                            },
                            label = { Text("Home Heading (जैसे: What do you want to do?)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = customSupportPhone,
                            onValueChange = {
                                customSupportPhone = it
                                isBrandingSaved = false
                            },
                            label = { Text("Admin Helpline Number", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = customBannerText,
                            onValueChange = {
                                customBannerText = it
                                isBrandingSaved = false
                            },
                            label = { Text("Top App Announcement Banner", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = customCategoriesList,
                            onValueChange = {
                                customCategoriesList = it
                                isBrandingSaved = false
                            },
                            label = { Text("Work Categories (Comma se alag karein)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Save Button: Save Icon is Orange before saving, and turns White after saving!
                        Button(
                            onClick = { saveAppBrandingToCloud() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBrandingSaved) Color(0xFF16A34A) else WorkoraNavy
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Save Icon",
                                tint = if (isBrandingSaved) Color.White else WorkoraOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBrandingSaved) "Saved! (Icon White Ho Gaya ✓)" else "Save App Logo & Branding",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Search Filter for Users / Workers / Jobs
            if (activeTab in 1..3) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Name, Phone or Location...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // ==================== TAB 1: 2. MANAGE USERS ====================
            if (activeTab == 1) {
                val filteredUsers = usersList.filter {
                    searchQuery.isBlank() ||
                            it.fullName.contains(searchQuery, true) ||
                            it.phone.contains(searchQuery, true) ||
                            it.email.contains(searchQuery, true)
                }

                Text(
                    text = "2. Registered App Users (${filteredUsers.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
                )

                filteredUsers.forEach { user ->
                    val isBlocked = user.accountStatus == "BLOCKED"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (isBlocked) Color(0xFFDC2626) else WorkoraBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = user.fullName, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                                    Text(text = "📧 ${user.email} • 📞 ${user.phone}", fontSize = 11.sp, color = WorkoraTextMuted)
                                    Text(text = "📍 ${user.location} • Role: ${user.role}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isBlocked) Color(0xFFDC2626).copy(alpha = 0.15f) else Color(0xFF16A34A).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = user.accountStatus,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isBlocked) Color(0xFFDC2626) else Color(0xFF16A34A)
                                    )
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isBlocked) Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                ) {
                                    Text(if (isBlocked) "Unblock" else "Block User", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    border = BorderStroke(1.dp, WorkoraNavy)
                                ) {
                                    Text("Make ${if (user.role == "CUSTOMER") "Worker" else "Customer"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                }

                                IconButton(
                                    onClick = {
                                        deleteFirebaseNode(
                                            path = "users/${user.key}",
                                            logAction = "DELETE_USER",
                                            logDetails = "Deleted user ${user.fullName}"
                                        )
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ==================== TAB 2: 3. VERIFY WORKERS ====================
            if (activeTab == 2) {
                val filteredWorkers = workersList.filter {
                    searchQuery.isBlank() ||
                            it.name.contains(searchQuery, true) ||
                            it.trade.contains(searchQuery, true) ||
                            it.location.contains(searchQuery, true)
                }

                Text(
                    text = "3. Workers Verification & Control (${filteredWorkers.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
                )

                filteredWorkers.forEach { worker ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (worker.isVerified) Color(0xFF16A34A) else WorkoraOrange)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = worker.name, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                                    Text(text = "🛠️ ${worker.trade} • ₹${worker.dailyWage}/day", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraOrange)
                                    Text(text = "📍 ${worker.location} • 📞 ${worker.phone}", fontSize = 11.sp, color = WorkoraTextMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (worker.isVerified) "✔ VERIFIED" else "⏳ UNVERIFIED",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (worker.isVerified) Color(0xFF16A34A) else WorkoraOrange
                                    )
                                    Text(
                                        text = if (worker.isAvailableToday) "● Available" else "○ Busy",
                                        fontSize = 10.sp,
                                        color = if (worker.isAvailableToday) Color(0xFF16A34A) else WorkoraTextMuted
                                    )
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (worker.isVerified) WorkoraNavy else Color(0xFF16A34A)
                                    )
                                ) {
                                    Text(if (worker.isVerified) "Revoke Verify" else "Approve ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(if (worker.isAvailableToday) "Mark Busy" else "Mark Available", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                }

                                IconButton(
                                    onClick = {
                                        deleteFirebaseNode(
                                            path = "workers/${worker.key}",
                                            logAction = "DELETE_WORKER",
                                            logDetails = "Removed worker ${worker.name}"
                                        )
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // ==================== TAB 3: 4. MANAGE JOBS ====================
            if (activeTab == 3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraOrange)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("4. Post Official Job from Admin Panel", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)

                        OutlinedTextField(
                            value = newJobTitle,
                            onValueChange = { newJobTitle = it },
                            label = { Text("Job Title (जैसे: 2 मिस्त्री चाहिए)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = newJobCategory,
                                onValueChange = { newJobCategory = it },
                                label = { Text("Category", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = newJobWage,
                                onValueChange = { newJobWage = it },
                                label = { Text("Wage ₹", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = newJobLocation,
                            onValueChange = { newJobLocation = it },
                            label = { Text("Location (Silwani, Raisen)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                if (newJobTitle.isBlank()) {
                                    Toast.makeText(context, "Kripya Job Title likhein!", Toast.LENGTH_SHORT).show()
                                } else {
                                    FirebaseManager.postJobToFirebase(
                                        title = newJobTitle.trim(),
                                        category = newJobCategory.trim(),
                                        description = "Posted by $customAppName Admin ($adminEmail)",
                                        dailyRate = newJobWage.toIntOrNull() ?: 600,
                                        location = newJobLocation.trim(),
                                        workersNeeded = 2,
                                        urgency = "Immediate",
                                        customerName = "$customAppName Official Admin",
                                        customerPhone = customSupportPhone
                                    ) {
                                        newJobTitle = ""
                                        Toast.makeText(context, "Live Job Posted! ✓", Toast.LENGTH_SHORT).show()
                                        loadAllLiveFirebaseData()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Publish Live Job Now ✓", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                jobsList.forEach { job ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, WorkoraBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = job.title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                                    Text(text = "${job.category} • ₹${job.dailyRate}/day • 📍 ${job.location}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraOrange)
                                }
                                Text(
                                    text = job.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (job.status == "COMPLETED") Color(0xFF16A34A) else WorkoraNavy
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraNavy)
                                ) {
                                    Text(if (job.status == "COMPLETED") "Re-Open" else "Mark Completed ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        deleteFirebaseNode(
                                            path = "jobs/${job.key}",
                                            logAction = "DELETE_JOB",
                                            logDetails = "Deleted job '${job.title}'"
                                        )
                                    },
                                    modifier = Modifier.height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFDC2626))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }

            // ==================== TAB 4: 5. SEND NOTIFICATION ====================
            if (activeTab == 4) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraOrange)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("5. Send Live Notification to All Users", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)

                        OutlinedTextField(
                            value = broadcastTitle,
                            onValueChange = { broadcastTitle = it },
                            label = { Text("Notice Title (जैसे: Workora सूचना)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = broadcastMessage,
                            onValueChange = { broadcastMessage = it },
                            label = { Text("Type Message (जैसे: सिलवानी में नए काम उपलब्ध हैं!)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                if (broadcastMessage.isBlank()) {
                                    Toast.makeText(context, "Kripya message likhein!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val fullMsg = if (broadcastTitle.isNotBlank()) "📢 [${broadcastTitle.trim()}]: ${broadcastMessage.trim()}" else "📢 ${broadcastMessage.trim()}"
                                    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                    FirebaseManager.sendChatMessageToCloud(
                                        senderName = "$customAppName ADMIN ✓",
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
                                        Toast.makeText(context, "Live Notification Sent! ✓", Toast.LENGTH_LONG).show()
                                        loadAllLiveFirebaseData()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Broadcast Now ✓", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }

            // ==================== TAB 5: 6. ADD ADMIN ====================
            if (activeTab == 5) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("6. Add or Promote Another Admin", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)

                        OutlinedTextField(
                            value = newAdminEmailInput,
                            onValueChange = { newAdminEmailInput = it },
                            label = { Text("Enter User Email (friend@gmail.com)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("SUPER_ADMIN", "SUPPORT_ADMIN", "MODERATOR").forEach { tierOption ->
                                val selected = newAdminRoleInput == tierOption
                                OutlinedButton(
                                    onClick = { newAdminRoleInput = tierOption },
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) WorkoraNavy else Color.White
                                    )
                                ) {
                                    Text(tierOption.replace("_", " "), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else WorkoraNavy)
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
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Admin Access Now ✓", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }
                }
            }

            // ==================== TAB 6: 7. EMERGENCY & LOGS ====================
            if (activeTab == 6) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFDC2626))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("7. Emergency System Switches", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFDC2626))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Maintenance Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                                Text("ऐप को मेंटेनेंस मोड में डालें", fontSize = 10.sp, color = WorkoraTextMuted)
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
                                Text("Allow New Registrations", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                                Text("नए यूज़र्स का साइन-अप चालू/बंद करें", fontSize = 10.sp, color = WorkoraTextMuted)
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
                                Text("Allow New Job Posting", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                                Text("नई जॉब पोस्टिंग चालू/बंद करें", fontSize = 10.sp, color = WorkoraTextMuted)
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
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Live Admin Audit Logs (${auditLogs.size})", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                        auditLogs.take(15).forEach { log ->
                            val timeText = remember(log.timestamp) {
                                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(WorkoraBgLight, shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${log.actionType} • ${log.adminTier}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WorkoraNavy)
                                    Text(timeText, fontSize = 10.sp, color = WorkoraTextMuted)
                                }
                                Text("${log.adminEmail}: ${log.details}", fontSize = 11.sp, color = WorkoraTextDark)
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
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = WorkoraBgLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = accent)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WorkoraTextMuted)
        }
    }
}
