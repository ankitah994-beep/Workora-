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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.JobPost
import com.example.model.Worker
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
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

private const val CUSTOMER_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

data class LiveWorkerCardItem(
    val key: String,
    val name: String,
    val trade: String,
    val dailyWage: Int,
    val phone: String,
    val location: String,
    val rating: Double,
    val isVerified: Boolean,
    val isAvailableToday: Boolean
)

data class CustomerPostedJobItem(
    val key: String,
    val id: Long,
    val title: String,
    val category: String,
    val description: String,
    val dailyRate: Int,
    val location: String,
    val urgency: String,
    val status: String
)

@Composable
fun CustomerDashboardScreen(
    currentUser: Any? = null,
    searchQuery: String = "",
    onSearchQueryChanged: (String) -> Unit = {},
    workers: List<Worker> = emptyList(),
    jobs: List<JobPost> = emptyList(),
    selectedCategory: String? = null,
    onCategorySelected: (String) -> Unit = {},
    activeTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onPostJob: (
        title: String,
        category: String,
        description: String,
        dailyRate: Int,
        location: String,
        workersNeeded: Int,
        urgency: String,
        dateTime: String
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onHireWorker: (Worker) -> Unit = {},
    onCompleteJob: (Long) -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenFilters: () -> Unit = {},
    toastMessage: String? = null
) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val brandPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    var appLang by remember {
        mutableStateOf(settingsPrefs.getString("app_language", "Hinglish") ?: "Hinglish")
    }

    fun tr(hi: String, hinglish: String, en: String): String {
        return when (appLang) {
            "Hindi" -> hi
            "English" -> en
            else -> hinglish
        }
    }

    val appName = remember { brandPrefs.getString("app_name", "WORKORA") ?: "WORKORA" }
    val bannerText = remember { brandPrefs.getString("banner_text", "") ?: "" }

    var currentRealLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
    }
    var showLocationModal by remember { mutableStateOf(false) }
    var locationSearchInput by remember { mutableStateOf("") }
    var isSearchingOnline by remember { mutableStateOf(false) }
    var isDetectingGps by remember { mutableStateOf(false) }
    val onlinePlaceResults = remember { mutableStateListOf<String>() }

    // Bottom Navigation State (0 = Home[span_6](start_span)[span_6](end_span), 1 = Search[span_7](start_span)[span_7](end_span), 2 = Post[span_8](start_span)[span_8](end_span), 3 = Profile[span_9](start_span)[span_9](end_span))
    var bottomNavIndex by remember { mutableIntStateOf(0) }

    var isLoadingData by remember { mutableStateOf(true) }
    val liveWorkersList = remember { mutableStateListOf<LiveWorkerCardItem>() }
    val livePostedJobs = remember { mutableStateListOf<CustomerPostedJobItem>() }

    var showHireOrPostSheet by remember { mutableStateOf(false) }
    var targetHireWorker by remember { mutableStateOf<LiveWorkerCardItem?>(null) }
    var formWorkTitle by remember { mutableStateOf("") }
    var formWorkCategory by remember { mutableStateOf("Mason") }
    var formWorkDateTime by remember { mutableStateOf("Today, 9:00 AM") }
    var formOfferedWage by remember { mutableStateOf("800") }
    var formWorkLocation by remember { mutableStateOf(currentRealLocation) }
    var formJobDetails by remember { mutableStateOf("") }

    val categoriesList = remember {
        val raw = brandPrefs.getString(
            "service_categories",
            "Mason, Mistri, Electrician, Plumber, Painter, Carpenter, Welder, Farm Labour"
        ) ?: "Mason, Mistri, Electrician, Plumber, Painter, Carpenter, Welder, Farm Labour"
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    fun openHireOrPostForm(worker: LiveWorkerCardItem?) {
        targetHireWorker = worker
        if (worker != null) {
            formWorkTitle = "Hire ${worker.name} - ${worker.trade}"
            formWorkCategory = worker.trade
            formOfferedWage = worker.dailyWage.toString()
            formWorkLocation = currentRealLocation
            formJobDetails = "Direct work request for ${worker.name} (${worker.trade}) at $currentRealLocation."
        } else {
            formWorkTitle = ""
            formWorkCategory = selectedCategory ?: "Mason / Mistri"
            formOfferedWage = "700"
            formWorkLocation = currentRealLocation
            formJobDetails = ""
        }
        showHireOrPostSheet = true
    }

    fun loadLiveWorkersAndJobsFromCloud() {
        isLoadingData = true
        CoroutineScope(Dispatchers.IO).launch {
            val loadedWorkers = mutableListOf<LiveWorkerCardItem>()
            val loadedJobs = mutableListOf<CustomerPostedJobItem>()

            try {
                val wConn = (URL("$CUSTOMER_DB_URL/workers.json").openConnection() as HttpURLConnection)
                if (wConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(wConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            loadedWorkers.add(
                                LiveWorkerCardItem(
                                    key = k,
                                    name = obj.optString("name", "Skilled Worker"),
                                    trade = obj.optString("trade", "Mason / Mistri"),
                                    dailyWage = obj.optInt("dailyWage", 600),
                                    phone = obj.optString("phone", "+91 6265798340"),
                                    location = obj.optString("location", currentRealLocation),
                                    rating = obj.optDouble("rating", 4.8),
                                    isVerified = obj.optBoolean("isVerified", true),
                                    isAvailableToday = obj.optBoolean("isAvailableToday", true)
                                )
                            )
                        }
                    }
                }
                wConn.disconnect()

                val jConn = (URL("$CUSTOMER_DB_URL/jobs.json").openConnection() as HttpURLConnection)
                if (jConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(jConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            loadedJobs.add(
                                CustomerPostedJobItem(
                                    key = k,
                                    id = obj.optLong("id", System.currentTimeMillis()),
                                    title = obj.optString("title", "Work Request"),
                                    category = obj.optString("category", "General"),
                                    description = obj.optString("description", ""),
                                    dailyRate = obj.optInt("dailyRate", 600),
                                    location = obj.optString("location", currentRealLocation),
                                    urgency = obj.optString("urgency", "Today, 9:00 AM"),
                                    status = obj.optString("status", "PENDING")
                                )
                            )
                        }
                    }
                }
                jConn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (loadedWorkers.isEmpty()) {
                loadedWorkers.addAll(
                    listOf(
                        LiveWorkerCardItem(
                            key = "w_sunil",
                            name = "Sunil Kumar",
                            trade = "Mason (राजमिस्त्री)",
                            dailyWage = 850,
                            phone = "+91 9876543210",
                            location = currentRealLocation,
                            rating = 4.9,
                            isVerified = true,
                            isAvailableToday = true
                        ),
                        LiveWorkerCardItem(
                            key = "w_ramesh",
                            name = "Ramesh Vishwakarma",
                            trade = "Electrician (इलेक्ट्रीशियन)",
                            dailyWage = 700,
                            phone = "+91 9123456780",
                            location = "Silwani, Raisen (MP)",
                            rating = 4.8,
                            isVerified = true,
                            isAvailableToday = true
                        ),
                        LiveWorkerCardItem(
                            key = "w_mukesh",
                            name = "Mukesh Ahirwar",
                            trade = "Painter & Putty Specialist",
                            dailyWage = 650,
                            phone = "+91 9988776655",
                            location = "Raisen, Madhya Pradesh",
                            rating = 4.7,
                            isVerified = true,
                            isAvailableToday = true
                        ),
                        LiveWorkerCardItem(
                            key = "w_suresh",
                            name = "Suresh Kushwaha",
                            trade = "Plumber & Pipe Fitting",
                            dailyWage = 750,
                            phone = "+91 9755443322",
                            location = "Begamganj, Raisen (MP)",
                            rating = 4.8,
                            isVerified = true,
                            isAvailableToday = true
                        )
                    )
                )
            }

            withContext(Dispatchers.Main) {
                liveWorkersList.clear()
                liveWorkersList.addAll(loadedWorkers)
                livePostedJobs.clear()
                livePostedJobs.addAll(loadedJobs.sortedByDescending { it.id })
                isLoadingData = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun detectRealCustomerGpsLocation() {
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
                    var resolvedName = ""

                    try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val sub = addr.subLocality ?: addr.featureName ?: ""
                            val city = addr.locality ?: addr.subAdminArea ?: ""
                            val state = addr.adminArea ?: ""
                            resolvedName = listOf(sub, city, state)
                                .filter { it.isNotBlank() }
                                .distinct()
                                .joinToString(", ")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (resolvedName.isBlank()) {
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
                                val place = addr.optString("village", addr.optString("town", addr.optString("city", "")))
                                val dist = addr.optString("state_district", addr.optString("county", ""))
                                val state = addr.optString("state", "")
                                resolvedName = listOf(place, dist, state)
                                    .filter { it.isNotBlank() }
                                    .distinct()
                                    .joinToString(", ")
                            }
                        }
                        conn.disconnect()
                    }

                    withContext(Dispatchers.Main) {
                        isDetectingGps = false
                        if (resolvedName.isNotBlank()) {
                            currentRealLocation = resolvedName
                            profilePrefs.edit().putString("user_location", resolvedName).apply()
                            showLocationModal = false
                            Toast.makeText(context, "Live GPS: $resolvedName ✓", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isDetectingGps = false
                        Toast.makeText(context, tr("फोन का GPS चालू करें!", "Phone ka GPS On karein!", "Turn on GPS!"), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDetectingGps = false
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
            detectRealCustomerGpsLocation()
        }
    }

    fun searchOnlineRealPlaces(query: String) {
        if (query.trim().length < 2) return
        isSearchingOnline = true
        CoroutineScope(Dispatchers.IO).launch {
            val found = mutableListOf<String>()
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
                        val cleanShort = display.split(",").take(3).joinToString(", ").trim()
                        if (cleanShort.isNotBlank() && !found.contains(cleanShort)) {
                            found.add(cleanShort)
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                onlinePlaceResults.clear()
                onlinePlaceResults.addAll(found)
                isSearchingOnline = false
            }
        }
    }

    LaunchedEffect(Unit) {
        appLang = settingsPrefs.getString("app_language", "Hinglish") ?: "Hinglish"
        currentRealLocation = profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)"
        loadLiveWorkersAndJobsFromCloud()
    }

    val filteredWorkers = liveWorkersList.filter { worker ->
        val matchesSearch = searchQuery.isBlank() ||
                worker.name.contains(searchQuery, ignoreCase = true) ||
                worker.trade.contains(searchQuery, ignoreCase = true) ||
                worker.location.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory.isNullOrBlank() ||
                selectedCategory.equals("All", ignoreCase = true) ||
                worker.trade.contains(selectedCategory, ignoreCase = true)

        matchesSearch && matchesCategory
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // ==================== 1. PROFESSIONAL TOP HEADER BAR[span_10](start_span)[span_10](end_span) ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WorkoraHelmetLogo(size = 40.dp, showHalo = false)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = appName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = tr("ढूंढें और hire करें कुशल कारीगर", "Find & Hire Skilled Labour", "Find & Hire Skilled Labour"),
                            fontSize = 10.sp,
                            color = WorkoraTextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = WorkoraNavy, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = {
                            loadLiveWorkersAndJobsFromCloud()
                            Toast.makeText(context, "Refreshed ✓", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = WorkoraNavy, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // ==================== 2. LOCATION BAR (EXACT AS SCREENSHOT)[span_11](start_span)[span_11](end_span) ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Card(
                    onClick = { showLocationModal = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = WorkoraBgLight),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentRealLocation,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WorkoraNavy,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = tr("लोकेशन बदलने के लिए टैप करें", "Tap to change location", "Tap to change location"),
                                    fontSize = 10.sp,
                                    color = WorkoraTextMuted
                                )
                            }
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = WorkoraTextMuted)
                    }
                }
            }

            // ==================== 3. BANNER CARD (FIND SKILLED WORKERS NEAR YOU)[span_12](start_span)[span_12](end_span) ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE0F2FE) // Light soft blue banner background
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = "Find Skilled Workers Near You",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraNavy,
                                lineHeight = 22.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Get your work done easily and safely.",
                                fontSize = 11.sp,
                                color = WorkoraTextMuted,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { openHireOrPostForm(null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Post New Requirement",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(85.dp)
                                .clip(CircleShape)
                                .background(WorkoraNavy.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            WorkoraHelmetLogo(size = 60.dp, showHalo = false)
                        }
                    }
                }
            }

            // ==================== 4. POPULAR CATEGORIES GRID[span_13](start_span)[span_13](end_span) ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular Categories",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraTextDark
                    )
                    Text(
                        text = "View All >",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraOrange,
                        modifier = Modifier.clickable { onCategorySelected("All") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val presetCats = listOf(
                    Triple("Mason", "🧱", Color(0xFFFEF3C7)),
                    Triple("Electrician", "⚡", Color(0xFFFEF08A)),
                    Triple("Plumber", "🔧", Color(0xFFDCFCE7)),
                    Triple("Painter", "🎨", Color(0xFFFFEDD5)),
                    Triple("Carpenter", "🪚", Color(0xFFF3E8FF)),
                    Triple("More", "⚙️", Color(0xFFF1F5F9))
                )

                // 2 Rows of 3 Categories
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    presetCats.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { (catName, emoji, bgColor) ->
                                val isSelected = selectedCategory?.equals(catName, ignoreCase = true) == true
                                Card(
                                    onClick = {
                                        if (catName == "More") {
                                            onCategorySelected("All")
                                        } else {
                                            onCategorySelected(catName)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(82.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.2.dp, if (isSelected) WorkoraOrange else WorkoraBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(bgColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = emoji, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = catName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WorkoraTextDark,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== 5. VERIFIED & TRUSTED WORKERS BANNER[span_14](start_span)[span_14](end_span) ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Verified & Trusted Workers",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WorkoraNavy
                                )
                                Text(
                                    text = "All workers are verified for your safety.",
                                    fontSize = 10.sp,
                                    color = WorkoraTextMuted
                                )
                            }
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = WorkoraTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ==================== 6. RECENT JOBS / AVAILABLE WORKERS LIST[span_15](start_span)[span_15](end_span) ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Workers Found (${filteredWorkers.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraTextDark
                    )
                    Text(
                        text = "Sort by v",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraTextMuted
                    )
                }

                if (isLoadingData) {
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
                            Text("Loading workers...", fontWeight = FontWeight.Bold, color = WorkoraNavy)
                        }
                    }
                } else if (filteredWorkers.isEmpty()) {
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
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No workers found in this category.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                        }
                    }
                } else {
                    filteredWorkers.forEach { worker ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, WorkoraBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(WorkoraNavy.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = worker.name.take(1).uppercase(),
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = WorkoraNavy
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = worker.name,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = WorkoraTextDark
                                                )
                                                if (worker.isVerified) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                                }
                                            }
                                            Text(
                                                text = worker.trade,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkoraOrange
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (worker.isAvailableToday) "Available" else "Busy",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF16A34A)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("${worker.rating} (12 reviews)", fontSize = 11.sp, color = WorkoraTextMuted)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraTextMuted, modifier = Modifier.size(13.dp))
                                        Text("2 km away", fontSize = 11.sp, color = WorkoraTextMuted)
                                    }

                                    Text(
                                        text = "₹${worker.dailyWage}/day",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WorkoraNavy
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            try {
                                                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                                                context.startActivity(dial)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Call: ${worker.phone}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, WorkoraNavy),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                    }

                                    Button(
                                        onClick = { openHireOrPostForm(worker) },
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Hire Now", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // ==================== 7. EXACT BOTTOM NAVIGATION BAR (HOME, SEARCH, POST, PROFILE)[span_16](start_span)[span_16](end_span) ====================
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home Tab[span_17](start_span)[span_17](end_span)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { bottomNavIndex = 0 }
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (bottomNavIndex == 0) WorkoraNavy else WorkoraTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Home", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (bottomNavIndex == 0) WorkoraNavy else WorkoraTextMuted)
                }

                // 2. Search Tab[span_18](start_span)[span_18](end_span)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        bottomNavIndex = 1
                        onOpenFilters()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (bottomNavIndex == 1) WorkoraNavy else WorkoraTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Search", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (bottomNavIndex == 1) WorkoraNavy else WorkoraTextMuted)
                }

                // 3. Post Tab[span_19](start_span)[span_19](end_span)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        bottomNavIndex = 2
                        openHireOrPostForm(null)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(WorkoraOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Post", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Text("Post", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (bottomNavIndex == 2) WorkoraNavy else WorkoraTextMuted)
                }

                // 4. Profile Tab[span_20](start_span)[span_20](end_span)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        bottomNavIndex = 3
                        onOpenProfile()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = if (bottomNavIndex == 3) WorkoraNavy else WorkoraTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Profile", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (bottomNavIndex == 3) WorkoraNavy else WorkoraTextMuted)
                }
            }
        }
    }

    // ==================== SINGLE HIRE / WORK REQUEST SHEET ====================
    if (showHireOrPostSheet) {
        val w = targetHireWorker
        AlertDialog(
            onDismissRequest = { showHireOrPostSheet = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (w != null) "Hire ${w.name}" else "Post Work Requirement",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraTextDark
                        )
                        Text(
                            text = if (w != null) "Send direct work request" else "Broadcast job in your location",
                            fontSize = 11.sp,
                            color = WorkoraTextMuted
                        )
                    }
                    IconButton(onClick = { showHireOrPostSheet = false }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null)
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
                    OutlinedTextField(
                        value = formWorkTitle,
                        onValueChange = { formWorkTitle = it },
                        label = { Text("Work Title (e.g. Wall Plastering)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = formWorkCategory,
                        onValueChange = { formWorkCategory = it },
                        label = { Text("Skill Required", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formOfferedWage,
                            onValueChange = { formOfferedWage = it },
                            label = { Text("Wage (₹/day)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = formWorkLocation,
                            onValueChange = { formWorkLocation = it },
                            label = { Text("Location", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = formJobDetails,
                        onValueChange = { formJobDetails = it },
                        label = { Text("Job Requirements", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val finalTitle = formWorkTitle.trim().ifBlank { "Need ${formWorkCategory.trim()}" }
                            val finalWage = formOfferedWage.toIntOrNull() ?: 700
                            val finalLoc = formWorkLocation.trim().ifBlank { currentRealLocation }

                            onPostJob(
                                finalTitle,
                                formWorkCategory.trim(),
                                formJobDetails.trim().ifBlank { "Work request at $finalLoc" },
                                finalWage,
                                finalLoc,
                                1,
                                "Today, 9:00 AM",
                                "Today, 9:00 AM"
                            )

                            showHireOrPostSheet = false
                            loadLiveWorkersAndJobsFromCloud()
                            Toast.makeText(context, "Work Posted Successfully! ✓", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                    ) {
                        Text("Send Work Request ✓", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            },
            confirmButton = {}
        )
    }

    // ==================== REAL LOCATION SELECTOR MODAL ====================
    if (showLocationModal) {
        val realQuickLocations = listOf(
            "Silwani, Raisen (MP)",
            "Raisen, Madhya Pradesh",
            "Begamganj, Raisen (MP)",
            "Gairatganj, Raisen (MP)",
            "Udaipura, Raisen (MP)",
            "Bareli, Raisen (MP)",
            "Bhopal, Madhya Pradesh",
            "Sagar, Madhya Pradesh",
            "Vidisha, Madhya Pradesh",
            "Indore, Madhya Pradesh",
            "Jabalpur, Madhya Pradesh"
        )

        AlertDialog(
            onDismissRequest = { showLocationModal = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Location 📍", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                    IconButton(onClick = { showLocationModal = false }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { detectRealCustomerGpsLocation() },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use Live GPS Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedTextField(
                        value = locationSearchInput,
                        onValueChange = {
                            locationSearchInput = it
                            if (it.length >= 3) searchOnlineRealPlaces(it)
                        },
                        label = { Text("Search Village or City...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (locationSearchInput.trim().isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val chosen = locationSearchInput.trim()
                                currentRealLocation = chosen
                                profilePrefs.edit().putString("user_location", chosen).apply()
                                showLocationModal = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set '${locationSearchInput.trim()}'", fontWeight = FontWeight.Bold, color = WorkoraOrange)
                        }
                    }

                    realQuickLocations.forEach { loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currentRealLocation == loc) WorkoraNavy else WorkoraBgLight)
                                .clickable {
                                    currentRealLocation = loc
                                    profilePrefs.edit().putString("user_location", loc).apply()
                                    showLocationModal = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (currentRealLocation == loc) WorkoraOrange else WorkoraNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(loc, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (currentRealLocation == loc) Color.White else WorkoraTextDark)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
