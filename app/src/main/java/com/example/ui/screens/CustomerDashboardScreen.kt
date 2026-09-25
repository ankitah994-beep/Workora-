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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
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

    val appName = remember { brandPrefs.getString("app_name", "WORKORA") ?: "WORKORA" }
    val bannerText = remember { brandPrefs.getString("banner_text", "") ?: "" }

    var currentRealLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
    }
    var filterBySelectedLocation by remember { mutableStateOf(false) }
    var showLocationModal by remember { mutableStateOf(false) }
    var locationSearchInput by remember { mutableStateOf("") }
    var isSearchingOnline by remember { mutableStateOf(false) }
    var isDetectingGps by remember { mutableStateOf(false) }
    val onlinePlaceResults = remember { mutableStateListOf<String>() }

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
                            Toast.makeText(context, "Live GPS Location Set: $resolvedName ✓", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Niche list se apna Gaon/Shahar select karein!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isDetectingGps = false
                        Toast.makeText(context, "Phone ka GPS On karein ya Search box mein Gaon/Shahar likhein!", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDetectingGps = false
                    Toast.makeText(context, "Search box mein apna Gaon/Shahar likh kar chunein!", Toast.LENGTH_SHORT).show()
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
        } else {
            Toast.makeText(context, "Aap search box mein Gaon/Shahar likh kar location chun sakte hain!", Toast.LENGTH_SHORT).show()
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
        loadLiveWorkersAndJobsFromCloud()
    }

    val primaryPlaceKeyword = currentRealLocation.split(",").firstOrNull()?.trim() ?: ""

    val filteredWorkers = liveWorkersList.filter { worker ->
        val matchesSearch = searchQuery.isBlank() ||
                worker.name.contains(searchQuery, ignoreCase = true) ||
                worker.trade.contains(searchQuery, ignoreCase = true) ||
                worker.location.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory.isNullOrBlank() ||
                selectedCategory.equals("All", ignoreCase = true) ||
                worker.trade.contains(selectedCategory, ignoreCase = true)

        val matchesLocation = !filterBySelectedLocation ||
                primaryPlaceKeyword.isBlank() ||
                worker.location.contains(primaryPlaceKeyword, ignoreCase = true) ||
                currentRealLocation.contains(worker.location.split(",").firstOrNull()?.trim() ?: "", ignoreCase = true)

        matchesSearch && matchesCategory && matchesLocation
    }

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
                .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                .background(WorkoraOrange.copy(alpha = 0.16f), shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HIRER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraOrange
                            )
                        }
                    }
                    Text(
                        text = "Find & Hire Skilled Labour",
                        fontSize = 11.sp,
                        color = WorkoraTextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = onSwitchRole,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, WorkoraBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Role", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                }

                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = WorkoraNavy, modifier = Modifier.size(21.dp))
                }

                IconButton(
                    onClick = onOpenProfile,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(WorkoraNavy.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings & Profile", tint = WorkoraNavy, modifier = Modifier.size(19.dp))
                }
            }
        }

        // 2. REAL LOCATION SELECTOR BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Card(
                onClick = { showLocationModal = true },
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
                            contentDescription = "Real Location",
                            tint = WorkoraOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                                text = "Tap to select Village/City or Live GPS 📍",
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
            if (bannerText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WorkoraOrange.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "📢 $bannerText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavy
                    )
                }
            }

            // 3. SEARCH BAR + POST NEW WORK BUTTON
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search worker by name, skill (Mistri, Electrician) or village...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkoraOrange) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WorkoraOrange,
                    unfocusedBorderColor = WorkoraBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Button(
                onClick = { openHireOrPostForm(null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Post New Work Requirement in ${primaryPlaceKeyword.ifBlank { "Your Area" }}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // 4. LOCATION FILTER BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { filterBySelectedLocation = true },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filterBySelectedLocation) WorkoraOrange else Color.White
                    ),
                    border = BorderStroke(1.dp, if (filterBySelectedLocation) WorkoraOrange else WorkoraBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (filterBySelectedLocation) Color.White else WorkoraOrange,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "In ${primaryPlaceKeyword.ifBlank { "My Area" }}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (filterBySelectedLocation) Color.White else WorkoraNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { filterBySelectedLocation = false },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!filterBySelectedLocation) WorkoraNavy else Color.White
                    ),
                    border = BorderStroke(1.dp, if (!filterBySelectedLocation) WorkoraNavy else WorkoraBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "🌍 All Areas (${liveWorkersList.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (!filterBySelectedLocation) Color.White else WorkoraNavy
                    )
                }

                IconButton(
                    onClick = {
                        loadLiveWorkersAndJobsFromCloud()
                        Toast.makeText(context, "Refreshing Workers & Jobs...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = WorkoraNavy)
                }
            }

            // 5. TABS: FIND WORKERS vs MY POSTED JOBS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                val tabs = listOf(
                    "Available Workers (${filteredWorkers.size})",
                    "My Posted Jobs (${livePostedJobs.size})"
                )
                tabs.forEachIndexed { idx, title ->
                    val isSelected = activeTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) WorkoraNavy else Color.Transparent)
                            .clickable { onTabSelected(idx) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color.White else WorkoraTextMuted
                        )
                    }
                }
            }

            // 6. SKILL CATEGORY FILTER CHIPS
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

            // 7. WORKERS LIST OR MY POSTED JOBS
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
                        Text("Loading verified workers near $currentRealLocation...", fontWeight = FontWeight.Bold, color = WorkoraNavy)
                    }
                }
            } else if (activeTab == 0) {
                if (filteredWorkers.isEmpty()) {
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
                                text = "Is location ($currentRealLocation) mein abhi koi worker nahi mila",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraNavy
                            )
                            OutlinedButton(onClick = { filterBySelectedLocation = false }) {
                                Text("Show Workers from All Areas 🌍", fontWeight = FontWeight.Bold, color = WorkoraOrange)
                            }
                        }
                    }
                } else {
                    filteredWorkers.forEach { worker ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, WorkoraBorder),
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
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(WorkoraNavy.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = worker.name.take(1).uppercase(),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = WorkoraNavy
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = worker.name,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = WorkoraTextDark
                                                )
                                                if (worker.isVerified) {
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.VerifiedUser,
                                                        contentDescription = "Verified",
                                                        tint = Color(0xFF16A34A),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = worker.trade,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkoraOrange
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${worker.dailyWage}/day",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = WorkoraNavy
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("${worker.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(WorkoraBgLight)
                                        .clickable {
                                            try {
                                                val mapUri = Uri.parse("geo:0,0?q=${Uri.encode(worker.location)}")
                                                context.startActivity(Intent(Intent.ACTION_VIEW, mapUri))
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Location: ${worker.location}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = worker.location,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WorkoraNavy
                                        )
                                    }
                                    Text(
                                        text = if (worker.isAvailableToday) "● Available Today • Map 🗺️" else "○ Busy • Map 🗺️",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (worker.isAvailableToday) Color(0xFF16A34A) else WorkoraTextMuted
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
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, WorkoraNavy)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Call Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                    }

                                    Button(
                                        onClick = { openHireOrPostForm(worker) },
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                                    ) {
                                        Text("Hire ${worker.name.split(" ").first()}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (livePostedJobs.isEmpty()) {
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
                            Icon(Icons.Default.Build, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(40.dp))
                            Text("Abhi tak koi kaam post nahi kiya gaya", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                            Button(
                                onClick = { openHireOrPostForm(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                            ) {
                                Text("+ Post First Work Request", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    livePostedJobs.forEach { job ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, WorkoraBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(job.title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark, modifier = Modifier.weight(1f))
                                    Text(
                                        text = job.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (job.status == "COMPLETED") Color(0xFF16A34A) else WorkoraOrange
                                    )
                                }
                                Text("${job.category} • ₹${job.dailyRate}/day • 📍 ${job.location}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                if (job.description.isNotBlank()) {
                                    Text(job.description, fontSize = 12.sp, color = WorkoraTextMuted)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onCompleteJob(job.id)
                                            livePostedJobs.remove(job)
                                            Toast.makeText(context, "Job Marked Completed ✓", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mark Completed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
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
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraTextDark
                        )
                        Text(
                            text = if (w != null) "Send direct work details to this worker" else "Find available workers in your selected location",
                            fontSize = 11.sp,
                            color = WorkoraTextMuted
                        )
                    }
                    IconButton(onClick = { showHireOrPostSheet = false }, modifier = Modifier.size(28.dp)) {
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
                        label = { Text("Work Type / Skill Required", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = formWorkDateTime,
                        onValueChange = { formWorkDateTime = it },
                        label = { Text("Work Date & Time", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Today, 9:00 AM", "Today, 2:00 PM", "Tomorrow, 9:00 AM").forEach { slot ->
                            val isSelected = formWorkDateTime == slot
                            Button(
                                onClick = { formWorkDateTime = slot },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) WorkoraNavy else WorkoraBgLight
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = slot,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else WorkoraTextDark
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formOfferedWage,
                            onValueChange = { formOfferedWage = it },
                            label = { Text("Offered Wage (₹/day)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = formWorkLocation,
                            onValueChange = { formWorkLocation = it },
                            label = { Text("Work Location", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Quick Wage:", fontSize = 11.sp, color = WorkoraTextMuted)
                        listOf("600", "700", "850", "1000").forEach { wageChip ->
                            val isSelected = formOfferedWage == wageChip
                            Button(
                                onClick = { formOfferedWage = wageChip },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) WorkoraOrange else WorkoraBgLight
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = "₹$wageChip",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else WorkoraTextDark
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formJobDetails,
                        onValueChange = { formJobDetails = it },
                        label = { Text("Job Details & Requirements", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val finalTitle = formWorkTitle.trim().ifBlank { "Need ${formWorkCategory.trim()} in $formWorkLocation" }
                            val finalWage = formOfferedWage.toIntOrNull() ?: 700
                            val finalLoc = formWorkLocation.trim().ifBlank { currentRealLocation }
                            val finalDesc = formJobDetails.trim().ifBlank { "Work request at $finalLoc ($formWorkDateTime)" }

                            onPostJob(
                                finalTitle,
                                formWorkCategory.trim(),
                                finalDesc,
                                finalWage,
                                finalLoc,
                                1,
                                formWorkDateTime,
                                formWorkDateTime
                            )

                            showHireOrPostSheet = false
                            loadLiveWorkersAndJobsFromCloud()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send Work Request",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showLocationModal) {
        val realLocationsList = listOf(
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

        val matchingPresetLocations = realLocationsList.filter {
            locationSearchInput.isBlank() || it.contains(locationSearchInput, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showLocationModal = false },
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
                    IconButton(onClick = { showLocationModal = false }, modifier = Modifier.size(28.dp)) {
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
                                detectRealCustomerGpsLocation()
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
                            Text("Detecting Live GPS...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Use My Current Live GPS Location", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = locationSearchInput,
                        onValueChange = {
                            locationSearchInput = it
                            if (it.length >= 3) {
                                searchOnlineRealPlaces(it)
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

                    if (locationSearchInput.trim().isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val chosen = locationSearchInput.trim()
                                currentRealLocation = chosen
                                profilePrefs.edit().putString("user_location", chosen).apply()
                                showLocationModal = false
                                Toast.makeText(context, "Location Set: $chosen ✓", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WorkoraOrange)
                        ) {
                            Text(
                                text = "✔ Set '${locationSearchInput.trim()}' as My Location",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraOrange
                            )
                        }
                    }

                    if (isSearchingOnline) {
                        Text("Searching real villages & cities in India...", fontSize = 11.sp, color = WorkoraOrange)
                    }
                    if (onlinePlaceResults.isNotEmpty()) {
                        Text(
                            text = "Live Verified Locations (India):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF16A34A)
                        )
                        onlinePlaceResults.forEach { place ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF0FDF4))
                                    .clickable {
                                        currentRealLocation = place
                                        profilePrefs.edit().putString("user_location", place).apply()
                                        showLocationModal = false
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

                    matchingPresetLocations.forEach { loc ->
                        val isSelected = currentRealLocation.equals(loc, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) WorkoraNavy else WorkoraBgLight)
                                .clickable {
                                    currentRealLocation = loc
                                    profilePrefs.edit().putString("user_location", loc).apply()
                                    showLocationModal = false
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
                                    tint = if (isSelected) WorkoraOrange else WorkoraNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = loc,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else WorkoraTextDark
                                )
                            }
                            if (isSelected) {
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
