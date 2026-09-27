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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
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

data class AdminWorkerRow(
    val key: String,
    val name: String,
    val trade: String,
    val phone: String,
    val location: String,
    val dailyWage: Int,
    val isVerified: Boolean,
    val isBlocked: Boolean
)

data class AdminJobRow(
    val key: String,
    val title: String,
    val category: String,
    val dailyRate: Int,
    val location: String,
    val customerName: String
)

data class AdminAreaControlRow(
    val key: String,
    val stateName: String,
    val areaKeywords: String,
    val isServiceEnabled: Boolean
)

private fun decodeAdminBase64(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun encodeAdminUri(context: Context, uri: Uri): String {
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
    adminEmail: String = "ankitah994@gmail.com",
    adminTier: String = "SUPER_ADMIN",
    onLogoutAdmin: () -> Unit = {},
    onSwitchRoleFromAdmin: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val brandingPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val bgLight = Color(0xFFF8FAFC)
    val textDark = Color(0xFF102A43)
    val textMuted = Color(0xFF667085)
    val borderLight = Color(0xFFE5E7EB)
    val greenColor = Color(0xFF22A06B)
    val redColor = Color(0xFFB42318)

    // 0 = ✏️ Edit App (Default open so Admin immediately sees App Edit options!)
    // 1 = 👥 Workers & Users
    // 2 = 📋 Live Jobs
    // 3 = 📍 Area Service ON/OFF
    // 4 = 📢 Broadcast & Security
    var activeAdminTab by remember { mutableIntStateOf(0) }

    // ==================== TAB 0: EDIT APP / BRANDING STATES ====================
    var editAppName by remember {
        mutableStateOf(brandingPrefs.getString("app_name", "WORKORA") ?: "WORKORA")
    }
    var editAppTagline by remember {
        mutableStateOf(brandingPrefs.getString("app_tagline", "Find & Hire Skilled Labour") ?: "Find & Hire Skilled Labour")
    }
    var editBannerHeading by remember {
        mutableStateOf(brandingPrefs.getString("banner_text", "Find Skilled\nWorkers Near You") ?: "Find Skilled\nWorkers Near You")
    }
    var editBannerSubtext by remember {
        mutableStateOf(brandingPrefs.getString("banner_subtext", "Get your work done easily\nand safely.") ?: "Get your work done easily\nand safely.")
    }
    var editPostButtonLabel by remember {
        mutableStateOf(brandingPrefs.getString("post_btn_label", "Post Job") ?: "Post Job")
    }
    var editDefaultCategories by remember {
        mutableStateOf(
            brandingPrefs.getString(
                "app_categories",
                "Mason, Electrician, Plumber, Painter, Carpenter, Labour, Cleaner, Farm Worker, Tile Worker"
            ) ?: "Mason, Electrician, Plumber, Painter, Carpenter, Labour, Cleaner, Farm Worker, Tile Worker"
        )
    }
    var editHelplinePhone by remember {
        mutableStateOf(brandingPrefs.getString("helpline_phone", "+91 6265798340") ?: "+91 6265798340")
    }
    var editDefaultLocation by remember {
        mutableStateOf(brandingPrefs.getString("default_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
    }
    var editLogoBase64 by remember {
        mutableStateOf(brandingPrefs.getString("logo_base64", "") ?: "")
    }
    var isSavingAppEdits by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeAdminUri(context, uri)
            if (encoded.isNotBlank()) {
                editLogoBase64 = encoded
                brandingPrefs.edit().putString("logo_base64", encoded).apply()
                Toast.makeText(context, "New App Logo Selected ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ==================== TAB 1, 2, 3, 4 STATES ====================
    val cloudWorkers = remember { mutableStateListOf<AdminWorkerRow>() }
    val cloudJobs = remember { mutableStateListOf<AdminJobRow>() }
    val areaControls = remember { mutableStateListOf<AdminAreaControlRow>() }
    var isLoadingCloud by remember { mutableStateOf(false) }

    var newStateName by remember { mutableStateOf("Madhya Pradesh") }
    var newAreaKeywords by remember { mutableStateOf("Silwani, Raisen, Bhopal") }

    var broadcastTitle by remember { mutableStateOf("Workora Special Update") }
    var broadcastMessage by remember { mutableStateOf("New daily wage jobs are now open in Silwani & Raisen!") }
    var maintenanceMode by remember {
        mutableStateOf(settingsPrefs.getBoolean("maintenance_mode", false))
    }

    fun loadAllAdminDataFromFirebase() {
        isLoadingCloud = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Load App Branding
                val bConn = URL("$ADMIN_FIREBASE_URL/app_branding.json").openConnection() as HttpURLConnection
                if (bConn.responseCode in 200..299) {
                    val bResp = BufferedReader(InputStreamReader(bConn.inputStream)).use { it.readText() }
                    if (bResp.isNotBlank() && bResp != "null" && bResp.startsWith("{")) {
                        val obj = JSONObject(bResp)
                        withContext(Dispatchers.Main) {
                            editAppName = obj.optString("app_name", editAppName)
                            editAppTagline = obj.optString("app_tagline", editAppTagline)
                            editBannerHeading = obj.optString("banner_text", editBannerHeading)
                            editBannerSubtext = obj.optString("banner_subtext", editBannerSubtext)
                            editHelplinePhone = obj.optString("helpline_phone", editHelplinePhone)
                            editDefaultCategories = obj.optString("app_categories", editDefaultCategories)
                        }
                    }
                }
                bConn.disconnect()

                // 2. Load Workers
                val wConn = URL("$ADMIN_FIREBASE_URL/workers.json").openConnection() as HttpURLConnection
                if (wConn.responseCode in 200..299) {
                    val wResp = BufferedReader(InputStreamReader(wConn.inputStream)).use { it.readText() }
                    if (wResp.isNotBlank() && wResp != "null" && wResp.startsWith("{")) {
                        val root = JSONObject(wResp)
                        val keys = root.keys()
                        val list = mutableListOf<AdminWorkerRow>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val o = root.optJSONObject(k) ?: continue
                            list.add(
                                AdminWorkerRow(
                                    key = k,
                                    name = o.optString("name", "Worker"),
                                    trade = o.optString("trade", "Mason"),
                                    phone = o.optString("phone", ""),
                                    location = o.optString("location", "Silwani"),
                                    dailyWage = o.optInt("dailyWage", 600),
                                    isVerified = o.optBoolean("isVerified", true),
                                    isBlocked = o.optBoolean("isBlocked", false)
                                )
                            )
                        }
                        withContext(Dispatchers.Main) {
                            cloudWorkers.clear()
                            cloudWorkers.addAll(list)
                        }
                    }
                }
                wConn.disconnect()

                // 3. Load Jobs
                val jConn = URL("$ADMIN_FIREBASE_URL/jobs.json").openConnection() as HttpURLConnection
                if (jConn.responseCode in 200..299) {
                    val jResp = BufferedReader(InputStreamReader(jConn.inputStream)).use { it.readText() }
                    if (jResp.isNotBlank() && jResp != "null" && jResp.startsWith("{")) {
                        val root = JSONObject(jResp)
                        val keys = root.keys()
                        val list = mutableListOf<AdminJobRow>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val o = root.optJSONObject(k) ?: continue
                            list.add(
                                AdminJobRow(
                                    key = k,
                                    title = o.optString("title", "Work"),
                                    category = o.optString("category", "Mason"),
                                    dailyRate = o.optInt("dailyRate", 500),
                                    location = o.optString("location", "Silwani"),
                                    customerName = o.optString("customerName", "Customer")
                                )
                            )
                        }
                        withContext(Dispatchers.Main) {
                            cloudJobs.clear()
                            cloudJobs.addAll(list)
                        }
                    }
                }
                jConn.disconnect()

                // 4. Load Area Controls
                val aConn = URL("$ADMIN_FIREBASE_URL/area_controls.json").openConnection() as HttpURLConnection
                if (aConn.responseCode in 200..299) {
                    val aResp = BufferedReader(InputStreamReader(aConn.inputStream)).use { it.readText() }
                    if (aResp.isNotBlank() && aResp != "null" && aResp.startsWith("{")) {
                        val root = JSONObject(aResp)
                        val keys = root.keys()
                        val list = mutableListOf<AdminAreaControlRow>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val o = root.optJSONObject(k) ?: continue
                            list.add(
                                AdminAreaControlRow(
                                    key = k,
                                    stateName = o.optString("stateName", "MP"),
                                    areaKeywords = o.optString("areaKeywords", "Silwani"),
                                    isServiceEnabled = o.optBoolean("isServiceEnabled", true)
                                )
                            )
                        }
                        withContext(Dispatchers.Main) {
                            areaControls.clear()
                            areaControls.addAll(list)
                        }
                    }
                }
                aConn.disconnect()
            } catch (_: Exception) {
            }
            withContext(Dispatchers.Main) {
                isLoadingCloud = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAllAdminDataFromFirebase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // ==================== TOP NAVY ADMIN BAR ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(deepNavy)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(brandOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "WORKORA SUPER ADMIN PANEL",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "$adminEmail • $adminTier",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { activeAdminTab = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit App", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { onSwitchRoleFromAdmin("CUSTOMER") },
                    border = BorderStroke(1.dp, Color.White),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Customer View", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { onSwitchRoleFromAdmin("LABOUR") },
                    border = BorderStroke(1.dp, brandOrange),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Worker View", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                }

                IconButton(onClick = onLogoutAdmin) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                }
            }
        }

        // ==================== HORIZONTAL ADMIN NAVIGATION TABS ====================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                0 to "✏️ 1. Edit App & Branding",
                1 to "👥 2. Manage Workers (${cloudWorkers.size})",
                2 to "📋 3. Manage Jobs (${cloudJobs.size})",
                3 to "📍 4. Area Service ON/OFF",
                4 to "📢 5. Broadcast & Security"
            )
            tabs.forEach { (index, title) ->
                val selected = activeAdminTab == index
                Button(
                    onClick = { activeAdminTab = index },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) deepNavy else Color(0xFFF1F5F9)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (selected) Color.White else textDark
                    )
                }
            }
        }

        // ==================== MAIN SCROLLABLE CONTENT AREA ====================
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (activeAdminTab) {
                0 -> {
                    // ==================== ✏️ TAB 0: EDIT APP & BRANDING ====================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, brandOrange),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "✏️ Edit Workora App (Live App Customizer)",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = deepNavy
                                    )
                                    Text(
                                        text = "Yahan se App ka Naam, Logo, Home Banner, Categories aur Helpline badlein",
                                        fontSize = 12.sp,
                                        color = textMuted
                                    )
                                }
                                IconButton(onClick = { loadAllAdminDataFromFirebase() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = deepNavy)
                                }
                            }

                            // App Logo Upload Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgLight)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val logoBmp = remember(editLogoBase64) { decodeAdminBase64(editLogoBase64) }
                                    Box(
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(CircleShape)
                                            .background(deepNavy)
                                            .clickable { logoPickerLauncher.launch("image/*") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (logoBmp != null) {
                                            Image(
                                                bitmap = logoBmp,
                                                contentDescription = "App Logo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text("W", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("App Header Logo", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                        Text("Tap button to upload new logo from gallery", fontSize = 11.sp, color = textMuted)
                                    }
                                }

                                Button(
                                    onClick = { logoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Change Logo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = editAppName,
                                    onValueChange = { editAppName = it },
                                    label = { Text("1. App Name (ऐप का नाम)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = editAppTagline,
                                    onValueChange = { editAppTagline = it },
                                    label = { Text("2. App Tagline (टैगलाइन)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = editBannerHeading,
                                    onValueChange = { editBannerHeading = it },
                                    label = { Text("3. Home Banner Heading") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = editBannerSubtext,
                                    onValueChange = { editBannerSubtext = it },
                                    label = { Text("4. Home Banner Subtitle") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = editPostButtonLabel,
                                    onValueChange = { editPostButtonLabel = it },
                                    label = { Text("5. Banner Button Text") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = editHelplinePhone,
                                    onValueChange = { editHelplinePhone = it },
                                    label = { Text("6. Support Helpline Number") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = editDefaultCategories,
                                onValueChange = { editDefaultCategories = it },
                                label = { Text("7. Popular Categories (Comma separated)") },
                                singleLine = false,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = editDefaultLocation,
                                onValueChange = { editDefaultLocation = it },
                                label = { Text("8. Default App Location") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    isSavingAppEdits = true
                                    brandingPrefs.edit().apply {
                                        putString("app_name", editAppName.trim())
                                        putString("app_tagline", editAppTagline.trim())
                                        putString("banner_text", editBannerHeading.trim())
                                        putString("banner_subtext", editBannerSubtext.trim())
                                        putString("post_btn_label", editPostButtonLabel.trim())
                                        putString("helpline_phone", editHelplinePhone.trim())
                                        putString("app_categories", editDefaultCategories.trim())
                                        putString("default_location", editDefaultLocation.trim())
                                        putString("logo_base64", editLogoBase64)
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
                                                put("app_name", editAppName.trim())
                                                put("app_tagline", editAppTagline.trim())
                                                put("banner_text", editBannerHeading.trim())
                                                put("banner_subtext", editBannerSubtext.trim())
                                                put("post_btn_label", editPostButtonLabel.trim())
                                                put("helpline_phone", editHelplinePhone.trim())
                                                put("app_categories", editDefaultCategories.trim())
                                                put("default_location", editDefaultLocation.trim())
                                                put("updatedBy", adminEmail)
                                                put("updatedAt", System.currentTimeMillis())
                                            }
                                            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                            conn.responseCode
                                            conn.disconnect()
                                        } catch (_: Exception) {
                                        }

                                        withContext(Dispatchers.Main) {
                                            isSavingAppEdits = false
                                            Toast.makeText(
                                                context,
                                                "App Changes Saved & Published Live! ✓",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                enabled = !isSavingAppEdits,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = greenColor)
                            ) {
                                if (isSavingAppEdits) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Save & Publish App Changes Live ✓",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // ==================== 👥 TAB 1: MANAGE WORKERS & USERS ====================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, borderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "👥 Manage Registered Workers (${cloudWorkers.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = deepNavy
                            )

                            if (cloudWorkers.isEmpty()) {
                                Text("No workers found in Firebase /workers yet.", fontSize = 13.sp, color = textMuted)
                            }

                            cloudWorkers.forEachIndexed { idx, worker ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = bgLight),
                                    border = BorderStroke(1.dp, borderLight)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${worker.name} (${worker.trade} • ₹${worker.dailyWage}/day)",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = textDark
                                            )
                                            Text(
                                                text = "📍 ${worker.location} • 📱 ${worker.phone}",
                                                fontSize = 12.sp,
                                                color = textMuted
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    val updated = worker.copy(isVerified = !worker.isVerified)
                                                    cloudWorkers[idx] = updated
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        try {
                                                            val conn = (URL("$ADMIN_FIREBASE_URL/workers/${worker.key}/isVerified.json").openConnection() as HttpURLConnection).apply {
                                                                requestMethod = "PUT"
                                                                doOutput = true
                                                            }
                                                            OutputStreamWriter(conn.outputStream).use { it.write(updated.isVerified.toString()) }
                                                            conn.responseCode
                                                            conn.disconnect()
                                                        } catch (_: Exception) {
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (worker.isVerified) greenColor else Color.Gray
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text(
                                                    text = if (worker.isVerified) "Verified ✓" else "Verify",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    cloudWorkers.removeAt(idx)
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        try {
                                                            val conn = (URL("$ADMIN_FIREBASE_URL/workers/${worker.key}.json").openConnection() as HttpURLConnection).apply {
                                                                requestMethod = "DELETE"
                                                            }
                                                            conn.responseCode
                                                            conn.disconnect()
                                                        } catch (_: Exception) {
                                                        }
                                                    }
                                                    Toast.makeText(context, "Worker Removed", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = redColor),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // ==================== 📋 TAB 2: MANAGE LIVE JOBS ====================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, borderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "📋 Manage Customer Posted Jobs (${cloudJobs.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = deepNavy
                            )

                            if (cloudJobs.isEmpty()) {
                                Text("No live jobs in Firebase /jobs.", fontSize = 13.sp, color = textMuted)
                            }

                            cloudJobs.forEachIndexed { idx, job ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = bgLight),
                                    border = BorderStroke(1.dp, borderLight)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${job.title} (${job.category} • ₹${job.dailyRate}/day)",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = textDark
                                            )
                                            Text(
                                                text = "📍 ${job.location} • Posted by ${job.customerName}",
                                                fontSize = 12.sp,
                                                color = textMuted
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                cloudJobs.removeAt(idx)
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        val conn = (URL("$ADMIN_FIREBASE_URL/jobs/${job.key}.json").openConnection() as HttpURLConnection).apply {
                                                            requestMethod = "DELETE"
                                                        }
                                                        conn.responseCode
                                                        conn.disconnect()
                                                    } catch (_: Exception) {
                                                    }
                                                }
                                                Toast.makeText(context, "Job Deleted ✓", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = redColor),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Delete Job", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // ==================== 📍 TAB 3: AREA / STATE SERVICE ON/OFF ====================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, borderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "📍 State & Area Service Availability Control",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = deepNavy
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = newStateName,
                                    onValueChange = { newStateName = it },
                                    label = { Text("State / District Name") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = newAreaKeywords,
                                    onValueChange = { newAreaKeywords = it },
                                    label = { Text("Cities / Villages (Comma separated)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Button(
                                    onClick = {
                                        if (newStateName.isNotBlank()) {
                                            val key = "area_${System.currentTimeMillis()}"
                                            val item = AdminAreaControlRow(
                                                key = key,
                                                stateName = newStateName.trim(),
                                                areaKeywords = newAreaKeywords.trim(),
                                                isServiceEnabled = true
                                            )
                                            areaControls.add(0, item)
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    val conn = (URL("$ADMIN_FIREBASE_URL/area_controls/$key.json").openConnection() as HttpURLConnection).apply {
                                                        requestMethod = "PUT"
                                                        setRequestProperty("Content-Type", "application/json")
                                                        doOutput = true
                                                    }
                                                    val json = JSONObject().apply {
                                                        put("stateName", item.stateName)
                                                        put("areaKeywords", item.areaKeywords)
                                                        put("isServiceEnabled", true)
                                                    }
                                                    OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                                    conn.responseCode
                                                    conn.disconnect()
                                                } catch (_: Exception) {
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                                    modifier = Modifier.height(54.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                                    Text("Add Area", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            areaControls.forEachIndexed { idx, area ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = bgLight),
                                    border = BorderStroke(1.dp, if (area.isServiceEnabled) greenColor else redColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(area.stateName, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                            Text("Areas: ${area.areaKeywords}", fontSize = 12.sp, color = textMuted)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (area.isServiceEnabled) "SERVICE ON" else "CLOSED",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (area.isServiceEnabled) greenColor else redColor
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Switch(
                                                checked = area.isServiceEnabled,
                                                onCheckedChange = { enabled ->
                                                    val updated = area.copy(isServiceEnabled = enabled)
                                                    areaControls[idx] = updated
                                                    CoroutineScope(Dispatchers.IO).launch {
                                                        try {
                                                            val conn = (URL("$ADMIN_FIREBASE_URL/area_controls/${area.key}/isServiceEnabled.json").openConnection() as HttpURLConnection).apply {
                                                                requestMethod = "PUT"
                                                                doOutput = true
                                                            }
                                                            OutputStreamWriter(conn.outputStream).use { it.write(enabled.toString()) }
                                                            conn.responseCode
                                                            conn.disconnect()
                                                        } catch (_: Exception) {
                                                        }
                                                    }
                                                },
                                                colors = SwitchDefaults.colors(checkedTrackColor = greenColor)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // ==================== 📢 TAB 4: BROADCAST & MAINTENANCE ====================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, borderLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "📢 Send Live Broadcast Notification to All Users",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = deepNavy
                            )

                            OutlinedTextField(
                                value = broadcastTitle,
                                onValueChange = { broadcastTitle = it },
                                label = { Text("Notification Title") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = broadcastMessage,
                                onValueChange = { broadcastMessage = it },
                                label = { Text("Notification Message") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val id = "notif_${System.currentTimeMillis()}"
                                            val conn = (URL("$ADMIN_FIREBASE_URL/broadcasts/$id.json").openConnection() as HttpURLConnection).apply {
                                                requestMethod = "PUT"
                                                setRequestProperty("Content-Type", "application/json")
                                                doOutput = true
                                            }
                                            val json = JSONObject().apply {
                                                put("title", broadcastTitle.trim())
                                                put("message", broadcastMessage.trim())
                                                put("timestamp", System.currentTimeMillis())
                                            }
                                            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                            conn.responseCode
                                            conn.disconnect()
                                        } catch (_: Exception) {
                                        }
                                    }
                                    Toast.makeText(context, "Broadcast Sent Live to All Users! ✓", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Live Broadcast ✓", color = Color.White, fontWeight = FontWeight.ExtraBold)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("App Maintenance Mode", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                                    Text("Temporarily pause new job postings for maintenance", fontSize = 11.sp, color = textMuted)
                                }
                                Switch(
                                    checked = maintenanceMode,
                                    onCheckedChange = {
                                        maintenanceMode = it
                                        settingsPrefs.edit().putBoolean("maintenance_mode", it).apply()
                                    },
                                    colors = SwitchDefaults.colors(checkedTrackColor = brandOrange)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
