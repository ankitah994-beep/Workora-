package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.JobApplication
import com.example.model.JobPost
import com.example.ui.components.WorkoraHelmetLogo
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

private const val LABOUR_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

data class CloudLabourJob(
    val key: String,
    val id: Long,
    val title: String,
    val category: String,
    val description: String,
    val dailyRate: Int,
    val location: String,
    val customerName: String,
    val customerPhone: String,
    val urgency: String,
    val status: String
)

@Composable
fun LabourDashboardScreen(
    currentUser: Any? = null,
    jobs: List<JobPost> = emptyList(),
    applications: List<JobApplication> = emptyList(),
    selectedCategory: String? = null,
    onCategorySelected: (String) -> Unit = {},
    activeTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    isAvailable: Boolean = true,
    onToggleAvailability: () -> Unit = {},
    onApplyJob: (JobPost) -> Unit = {},
    onAcceptJob: (JobPost) -> Unit = {},
    onRejectJob: (JobPost) -> Unit = {},
    onCompleteJob: (Long) -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    toastMessage: String? = null
) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val brandPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    val appName = remember { brandPrefs.getString("app_name", "WORKORA") ?: "WORKORA" }
    val workerName = remember { profilePrefs.getString("user_name", "Ankit Ahirwar") ?: "Ankit Ahirwar" }
    val workerPhone = remember { profilePrefs.getString("user_phone", "+91 6265798340") ?: "+91 6265798340" }
    val workerSkill = remember { profilePrefs.getString("user_skill", "Mason / Mistri") ?: "Mason / Mistri" }
    val workerWage = remember { profilePrefs.getString("user_rate", "600") ?: "600" }

    var currentRealLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
    }
    var filterOnlyMyLocation by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationSearchQuery by remember { mutableStateOf("") }
    var isSearchingOnlineLocations by remember { mutableStateOf(false) }
    var isDetectingGps by remember { mutableStateOf(false) }
    val onlineLocationSuggestions = remember { mutableStateListOf<String>() }

    var localAvailable by remember { mutableStateOf(settingsPrefs.getBoolean("available_today", isAvailable)) }
    var isLoadingJobs by remember { mutableStateOf(true) }
    val liveCloudJobs = remember { mutableStateListOf<CloudLabourJob>() }
    val acceptedJobIds = remember {
        val savedSet = profilePrefs.getStringSet("worker_accepted_jobs", emptySet()) ?: emptySet()
        mutableStateListOf<Long>().apply { addAll(savedSet.mapNotNull { it.toLongOrNull() }) }
    }

    val categoriesList = remember {
        val raw = brandPrefs.getString("service_categories", "Mason, Mistri, Electrician, Plumber, Painter, Carpenter, Welder, Farm Labour")
            ?: "Mason, Mistri, Electrician, Plumber, Painter, Carpenter, Welder, Farm Labour"
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    fun syncWorkerLocationAndAvailabilityToCloud(newLocation: String, availableNow: Boolean) {
        profilePrefs.edit().putString("user_location", newLocation).apply()
        settingsPrefs.edit().putBoolean("available_today", availableNow).apply()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val safeKey = workerPhone.filter { it.isDigit() }.takeLast(10).ifBlank { "worker_default" }
                val conn = (URL("$LABOUR_DB_URL/workers/w_$safeKey.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("name", workerName)
                    put("trade", workerSkill)
                    put("dailyWage", workerWage.toIntOrNull() ?: 600)
                    put("phone", workerPhone)
                    put("location", newLocation)
                    put("isVerified", true)
                    put("isAvailableToday", availableNow)
                    put("rating", 4.9)
                    put("updatedAt", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadLiveJobsFromCloud() {
        isLoadingJobs = true
        CoroutineScope(Dispatchers.IO).launch {
            val loaded = mutableListOf<CloudLabourJob>()
            try {
                val conn = (URL("$LABOUR_DB_URL/jobs.json").openConnection() as HttpURLConnection)
                if (conn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            loaded.add(
                                CloudLabourJob(
                                    key = k,
                                    id = obj.optLong("id", System.currentTimeMillis()),
                                    title = obj.optString("title", "Work Requirement"),
                                    category = obj.optString("category", "Mason"),
                                    description = obj.optString("description", "Need skilled worker for daily work."),
                                    dailyRate = obj.optInt("dailyRate", 600),
                                    location = obj.optString("location", currentRealLocation),
                                    customerName = obj.optString("customerName", "Customer"),
                                    customerPhone = obj.optString("customerPhone", "+91 6265798340"),
                                    urgency = obj.optString("urgency", "Today, 9:00 AM"),
                                    status = obj.optString("status", "PENDING")
                                )
                            )
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (loaded.isEmpty()) {
                loaded.add(
                    CloudLabourJob(
                        key = "sample_silwani_1",
                        id = 101L,
                        title = "Wall Plastering & Brickwork (दीवार प्लास्टर और जुड़ाई)",
                        category = "Mason",
                        description = "Need experienced mason/mistri for boundary wall plastering and brickwork.",
                        dailyRate = 800,
                        location = currentRealLocation,
                        customerName = "Ramesh Verma",
                        customerPhone = "+91 6265798340",
                        urgency = "Today, 9:00 AM",
                        status = "PENDING"
                    )
                )
            }

            withContext(Dispatchers.Main) {
                liveCloudJobs.clear()
                liveCloudJobs.addAll(loaded.sortedByDescending { it.id })
                isLoadingJobs = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun detectRealGpsLocation() {
        isDetectingGps = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val providers = locationManager.getProviders(true)
                var bestLocation: android.location.Location? = null
                for (provider in providers) {
                    val l = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                        bestLocation = l
                    }
                }

                if (bestLocation != null) {
                    val lat = bestLocation.latitude
                    val lon = bestLocation.longitude
                    var resolvedPlace = ""

                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val subLoc = addr.subLocality ?: addr.featureName ?: ""
                            val city = addr.locality ?: addr.subAdminArea ?: ""
                            val state = addr.adminArea ?: ""
                            resolvedPlace = listOf(subLoc, city, state)
                                .filter { it.isNotBlank() }
                                .distinct()
                                .joinToString(", ")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (resolvedPlace.isBlank()) {
                        val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$lat&lon=$lon&zoom=14&addressdetails=1")
                        val conn = (url.openConnection() as HttpURLConnection).apply {
                            setRequestProperty("User-Agent", "WorkoraAndroidApp/1.0")
                            connectTimeout = 6000
                            readTimeout = 6000
                        }
                        if (conn.responseCode in 200..299) {
                            val response = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                            val json = JSONObject(response)
                            val addr = json.optJSONObject("address")
                            if (addr != null) {
                                val villageOrTown = addr.optString("village", addr.optString("town", addr.optString("city", addr.optString("suburb", ""))))
                                val district = addr.optString("state_district", addr.optString("county", ""))
                                val state = addr.optString("state", "")
                                resolvedPlace = listOf(villageOrTown, district, state)
                                    .filter { it.isNotBlank() }
                                    .distinct()
                                    .joinToString(", ")
                            }
                        }
                        conn.disconnect()
                    }

                    withContext(Dispatchers.Main) {
                        isDetectingGps = false
                        if (resolvedPlace.isNotBlank()) {
                            currentRealLocation = resolvedPlace
                            syncWorkerLocationAndAvailabilityToCloud(resolvedPlace, localAvailable)
                            showLocationDialog = false
                            Toast.makeText(context, "Live GPS Location Set: $resolvedPlace ✓", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "GPS Coordinates mile! Niche list se apna Gaon/Shahar chunein.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isDetectingGps = false
                        Toast.makeText(context, "Phone ka GPS On karein ya niche Search box mein apna Gaon/Shahar likhein!", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDetectingGps = false
                    Toast.makeText(context, "Search box mein apna Gaon/Shahar likh kar select karein!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            detectRealGpsLocation()
        } else {
            Toast.makeText(context, "Location permission nahi mili, aap search karke location chun sakte hain!", Toast.LENGTH_SHORT).show()
        }
    }

    fun searchRealLocationsOnline(query: String) {
        if (query.trim().length < 2) return
        isSearchingOnlineLocations = true
        CoroutineScope(Dispatchers.IO).launch {
            val results = mutableListOf<String>()
            try {
                val encoded = URLEncoder.encode("${query.trim()}, India", "UTF-8")
                val url = URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&addressdetails=1&limit=6")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "WorkoraAndroidApp/1.0")
                    connectTimeout = 6000
                    readTimeout = 6000
                }
                if (conn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val arr = JSONArray(text)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val display = obj.optString("display_name", "")
                        val shortClean = display.split(",").take(3).joinToString(", ").trim()
                        if (shortClean.isNotBlank() && !results.contains(shortClean)) {
                            results.add(shortClean)
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                onlineLocationSuggestions.clear()
                onlineLocationSuggestions.addAll(results)
                isSearchingOnlineLocations = false
            }
        }
    }

    LaunchedEffect(Unit) {
        syncWorkerLocationAndAvailabilityToCloud(currentRealLocation, localAvailable)
        loadLiveJobsFromCloud()
    }

    val locationFilteredJobs = liveCloudJobs.filter { job ->
        val matchesCategory = selectedCategory.isNullOrBlank() ||
                selectedCategory.equals("All", ignoreCase = true) ||
                job.category.contains(selectedCategory, ignoreCase = true) ||
                job.title.contains(selectedCategory, ignoreCase = true)

        val primaryCityWord = currentRealLocation.split(",").firstOrNull()?.trim() ?: ""
        val matchesLocation = !filterOnlyMyLocation ||
                primaryCityWord.isBlank() ||
                job.location.contains(primaryCityWord, ignoreCase = true) ||
                currentRealLocation.contains(job.location.split(",").firstOrNull()?.trim() ?: "", ignoreCase = true)

        matchesCategory && matchesLocation
    }

    val pendingWorkRequests = locationFilteredJobs.filter { !acceptedJobIds.contains(it.id) && it.status != "COMPLETED" }
    val myAcceptedJobs = locationFilteredJobs.filter { acceptedJobIds.contains(it.id) || it.status == "ACCEPTED" }
    val displayedJobs = if (activeTab == 0) pendingWorkRequests else myAcceptedJobs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. TOP HEADER BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WorkoraHelmetLogo(size = 42.dp, showHalo = false)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = appName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(WorkoraNavy, shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "WORKER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Find Daily Work & Earn",
                        fontSize = 11.sp,
                        color = WorkoraTextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onSwitchRole,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, WorkoraBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Role", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                }

                IconButton(
                    onClick = onOpenProfile,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WorkoraNavy.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings & Profile", tint = WorkoraNavy, modifier = Modifier.size(20.dp))
                }
            }
        }

        // 2. GREETING & REAL INTERACTIVE LOCATION BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Hello, $workerName 👋",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WorkoraTextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                onClick = { showLocationDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F1)),
                border = BorderStroke(1.dp, WorkoraOrange)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = WorkoraOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = currentRealLocation,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraNavy,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Tap to change real location or use Live GPS 📍",
                                fontSize = 10.sp,
                                color = WorkoraTextMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Change",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraOrange
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = WorkoraOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 3. AVAILABLE FOR WORK TODAY CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (localAvailable) WorkoraNavy else Color(0xFF475569)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (localAvailable) Color(0xFF22C55E) else Color(0xFF94A3B8))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (localAvailable) "Available for Work Today" else "Currently Offline / Busy",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Switch(
                            checked = localAvailable,
                            onCheckedChange = { checked ->
                                localAvailable = checked
                                onToggleAvailability()
                                syncWorkerLocationAndAvailabilityToCloud(currentRealLocation, checked)
                                Toast.makeText(
                                    context,
                                    if (checked) "Aap $currentRealLocation mein kaam ke liye Online hain! ✓" else "Status Offline ho gaya.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = WorkoraOrange
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$workerName ($currentRealLocation)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Skill: $workerSkill • Expected: ₹$workerWage / day • 4.9 ★",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (localAvailable) "Visible to Hirers" else "Hidden",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 4. QUICK STATS ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Jobs Available", fontSize = 11.sp, color = WorkoraTextMuted)
                        Text("${pendingWorkRequests.size} Nearby", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenProfile() },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("₹", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Daily Wage", fontSize = 11.sp, color = WorkoraTextMuted)
                        Text("₹$workerWage", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("My Rating", fontSize = 11.sp, color = WorkoraTextMuted)
                        Text("4.9 ★", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                    }
                }
            }

            // 5. LOCATION FILTER BAR FOR JOBS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { filterOnlyMyLocation = true },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filterOnlyMyLocation) WorkoraOrange else Color.White
                    ),
                    border = BorderStroke(1.dp, if (filterOnlyMyLocation) WorkoraOrange else WorkoraBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (filterOnlyMyLocation) Color.White else WorkoraOrange,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Near ${currentRealLocation.split(",").firstOrNull() ?: "Me"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (filterOnlyMyLocation) Color.White else WorkoraNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { filterOnlyMyLocation = false },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!filterOnlyMyLocation) WorkoraNavy else Color.White
                    ),
                    border = BorderStroke(1.dp, if (!filterOnlyMyLocation) WorkoraNavy else WorkoraBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "🌍 All Locations (${liveCloudJobs.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (!filterOnlyMyLocation) Color.White else WorkoraNavy
                    )
                }

                IconButton(
                    onClick = {
                        loadLiveJobsFromCloud()
                        Toast.makeText(context, "Refreshing Live Jobs...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = WorkoraNavy)
                }
            }

            // 6. TABS: WORK REQUESTS vs MY JOBS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                val tabTitles = listOf(
                    "Work Requests (${pendingWorkRequests.size})",
                    "My Jobs (${myAcceptedJobs.size})"
                )
                tabTitles.forEachIndexed { idx, title ->
                    val selected = activeTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selected) WorkoraNavy else Color.Transparent)
                            .clickable { onTabSelected(idx) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selected) Color.White else WorkoraTextMuted
                        )
                    }
                }
            }

            // 7. SKILL CATEGORY CHIPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isAllSelected = selectedCategory == null || selectedCategory == "All"
                Button(
                    onClick = { onCategorySelected("All") },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAllSelected) WorkoraNavy else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isAllSelected) WorkoraNavy else WorkoraBorder),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAllSelected) Color.White else WorkoraTextDark
                    )
                }

                categoriesList.forEach { cat ->
                    val isCatSelected = selectedCategory?.equals(cat, ignoreCase = true) == true
                    Button(
                        onClick = { onCategorySelected(cat) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCatSelected) WorkoraOrange else Color.White
                        ),
                        border = BorderStroke(1.dp, if (isCatSelected) WorkoraOrange else WorkoraBorder),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCatSelected) Color.White else WorkoraTextDark
                        )
                    }
                }
            }

            // 8. LIVE JOBS LIST
            if (isLoadingJobs) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = WorkoraOrange, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Text("Loading live jobs in your location...", fontWeight = FontWeight.Bold, color = WorkoraNavy)
                    }
                }
            } else if (displayedJobs.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(40.dp))
                        Text(
                            text = if (filterOnlyMyLocation) "Is Location ($currentRealLocation) mein abhi koi kaam nahi hai" else "Koi Job Request Nahi Mili",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                        if (filterOnlyMyLocation) {
                            OutlinedButton(onClick = { filterOnlyMyLocation = false }) {
                                Text("Show All Locations Jobs 🌍", fontWeight = FontWeight.Bold, color = WorkoraOrange)
                            }
                        }
                    }
                }
            } else {
                displayedJobs.forEach { job ->
                    val isAccepted = acceptedJobIds.contains(job.id) || job.status == "ACCEPTED"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, if (isAccepted) Color(0xFF16A34A) else WorkoraBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(WorkoraNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = job.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WorkoraNavy
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isAccepted) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isAccepted) "✔ ACCEPTED JOB" else "● PENDING REQUEST",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isAccepted) Color(0xFF16A34A) else Color(0xFFD97706)
                                    )
                                }
                            }

                            Text(
                                text = job.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraTextDark
                            )

                            Text(
                                text = job.description,
                                fontSize = 13.sp,
                                color = WorkoraTextMuted,
                                lineHeight = 18.sp
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(WorkoraBgLight)
                                    .clickable {
                                        try {
                                            val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(job.location)}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Location: ${job.location}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${job.location} • 🕒 ${job.urgency}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkoraNavy
                                    )
                                }
                                Text(
                                    text = "Map 🗺️",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WorkoraOrange
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₹${job.dailyRate} / day",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WorkoraOrange
                                )
                                Text(
                                    text = "Customer: ${job.customerName}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkoraTextDark
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.customerPhone}"))
                                            context.startActivity(dial)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Phone: ${job.customerPhone}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f).height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, WorkoraNavy)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call Customer", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                }

                                if (!isAccepted) {
                                    Button(
                                        onClick = {
                                            acceptedJobIds.add(job.id)
                                            profilePrefs.edit()
                                                .putStringSet("worker_accepted_jobs", acceptedJobIds.map { it.toString() }.toSet())
                                                .apply()
                                            Toast.makeText(context, "Kaam Accept Ho Gaya! My Jobs tab dekhein ✓", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Accept Work ✓", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            onCompleteJob(job.id)
                                            acceptedJobIds.remove(job.id)
                                            Toast.makeText(context, "Job Completed! ✓", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                                    ) {
                                        Text("Mark Completed ✓", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showLocationDialog) {
        val realQuickLocations = listOf(
            "Silwani, Raisen (MP)",
            "Raisen, Madhya Pradesh",
            "Begamganj, Raisen (MP)",
            "Gairatganj, Raisen (MP)",
            "Udaipura, Raisen (MP)",
            "Bareli, Raisen (MP)",
            "Sultanpur, Raisen (MP)",
            "Obaidullaganj, Raisen (MP)",
            "Mandideep, Raisen (MP)",
            "Bhopal, Madhya Pradesh",
            "Sagar, Madhya Pradesh",
            "Vidisha, Madhya Pradesh",
            "Narmadapuram (Hoshangabad), MP",
            "Indore, Madhya Pradesh",
            "Jabalpur, Madhya Pradesh"
        )

        val filteredQuickLocations = realQuickLocations.filter {
            locationSearchQuery.isBlank() || it.contains(locationSearchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select Real Location 📍",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                    }
                    IconButton(onClick = { showLocationDialog = false }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = WorkoraTextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (hasFine || hasCoarse) {
                                detectRealGpsLocation()
                            } else {
                                gpsPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        if (isDetectingGps) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detecting GPS...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use My Current Live GPS Location", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = locationSearchQuery,
                        onValueChange = {
                            locationSearchQuery = it
                            if (it.length >= 3) {
                                searchRealLocationsOnline(it)
                            }
                        },
                        label = { Text("Gaon, Tehsil ya Shahar ka naam likhein...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkoraOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraOrange,
                            unfocusedBorderColor = WorkoraBorder
                        )
                    )

                    if (locationSearchQuery.trim().isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val chosen = locationSearchQuery.trim()
                                currentRealLocation = chosen
                                syncWorkerLocationAndAvailabilityToCloud(chosen, localAvailable)
                                showLocationDialog = false
                                Toast.makeText(context, "Location Set: $chosen ✓", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WorkoraOrange)
                        ) {
                            Text(
                                text = "✔ Set '${locationSearchQuery.trim()}' as My Location",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraOrange
                            )
                        }
                    }

                    if (isSearchingOnlineLocations) {
                        Text("Searching real locations across India...", fontSize = 11.sp, color = WorkoraOrange)
                    }
                    if (onlineLocationSuggestions.isNotEmpty()) {
                        Text(
                            text = "Live Verified Locations (India):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF16A34A)
                        )
                        onlineLocationSuggestions.forEach { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF0FDF4))
                                    .clickable {
                                        currentRealLocation = place
                                        syncWorkerLocationAndAvailabilityToCloud(place, localAvailable)
                                        showLocationDialog = false
                                        Toast.makeText(context, "Location Updated: $place ✓", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = place, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
                            }
                        }
                    }

                    Text(
                        text = "Popular Real Locations (Tap to Select):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )

                    filteredQuickLocations.forEach { loc ->
                        val isCurrent = currentRealLocation.equals(loc, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) WorkoraNavy else WorkoraBgLight)
                                .clickable {
                                    currentRealLocation = loc
                                    syncWorkerLocationAndAvailabilityToCloud(loc, localAvailable)
                                    showLocationDialog = false
                                    Toast.makeText(context, "Location Set: $loc ✓", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isCurrent) WorkoraOrange else WorkoraNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = loc,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color.White else WorkoraTextDark
                                )
                            }
                            if (isCurrent) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
