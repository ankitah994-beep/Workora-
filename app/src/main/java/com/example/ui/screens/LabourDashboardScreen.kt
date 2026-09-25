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
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Plumbing
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
    val status: String,
    val distanceKm: Int = 2
)

// Private Canvas Illustration of Skilled Worker with Yellow Hard Hat, Blue Overalls & Wrench
@Composable
private fun LabourHeroIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = Color(0xFFBAE6FD).copy(alpha = 0.55f),
            radius = w * 0.46f,
            center = Offset(w * 0.52f, h * 0.54f)
        )

        // Wrench in right hand
        drawLine(
            color = Color(0xFF334155),
            start = Offset(w * 0.18f, h * 0.44f),
            end = Offset(w * 0.28f, h * 0.72f),
            strokeWidth = w * 0.055f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFF475569),
            radius = w * 0.075f,
            center = Offset(w * 0.16f, h * 0.40f)
        )
        drawCircle(
            color = Color(0xFFE0F2FE),
            radius = w * 0.032f,
            center = Offset(w * 0.14f, h * 0.38f)
        )

        // Light blue work shirt
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(w * 0.26f, h * 0.54f),
            size = Size(w * 0.52f, h * 0.36f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )

        // Navy overalls
        drawRoundRect(
            color = Color(0xFF0B2545),
            topLeft = Offset(w * 0.34f, h * 0.58f),
            size = Size(w * 0.36f, h * 0.32f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )
        drawRect(
            color = Color(0xFF0B2545),
            topLeft = Offset(w * 0.36f, h * 0.52f),
            size = Size(w * 0.06f, h * 0.10f)
        )
        drawRect(
            color = Color(0xFF0B2545),
            topLeft = Offset(w * 0.62f, h * 0.52f),
            size = Size(w * 0.06f, h * 0.10f)
        )

        // Neck & Face
        drawRoundRect(
            color = Color(0xFFFDBA74),
            topLeft = Offset(w * 0.45f, h * 0.44f),
            size = Size(w * 0.14f, h * 0.12f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawCircle(
            color = Color(0xFFFED7AA),
            radius = w * 0.16f,
            center = Offset(w * 0.52f, h * 0.34f)
        )

        // Eyes & smile
        drawCircle(
            color = Color(0xFF1E293B),
            radius = w * 0.018f,
            center = Offset(w * 0.47f, h * 0.33f)
        )
        drawCircle(
            color = Color(0xFF1E293B),
            radius = w * 0.018f,
            center = Offset(w * 0.57f, h * 0.33f)
        )
        drawArc(
            color = Color(0xFF1E293B),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.47f, h * 0.35f),
            size = Size(w * 0.10f, h * 0.06f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        // Yellow Safety Helmet
        val helmetPath = Path().apply {
            moveTo(w * 0.34f, h * 0.26f)
            cubicTo(
                w * 0.34f, h * 0.10f,
                w * 0.70f, h * 0.10f,
                w * 0.70f, h * 0.26f
            )
            close()
        }
        drawPath(path = helmetPath, color = Color(0xFFFACC15))
        drawRoundRect(
            color = Color(0xFFEAB308),
            topLeft = Offset(w * 0.31f, h * 0.24f),
            size = Size(w * 0.42f, h * 0.045f),
            cornerRadius = CornerRadius(10f, 10f)
        )
    }
}

@Composable
private fun LabourJobAvatarIcon(
    accentColor: Color = Color(0xFF0B2545),
    size: Dp = 52.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            drawArc(
                color = accentColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.15f, h * 0.62f),
                size = Size(w * 0.70f, h * 0.55f)
            )
            drawCircle(
                color = Color(0xFFFDBA74),
                radius = w * 0.20f,
                center = Offset(w * 0.50f, h * 0.44f)
            )
            drawArc(
                color = Color(0xFFFACC15),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.28f, h * 0.18f),
                size = Size(w * 0.44f, h * 0.36f)
            )
            drawRoundRect(
                color = Color(0xFFEAB308),
                topLeft = Offset(w * 0.25f, h * 0.33f),
                size = Size(w * 0.50f, h * 0.06f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }
}

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
    val workerName = remember { profilePrefs.getString("user_name", "Ankit Ahirwar") ?: "Ankit Ahirwar" }
    val workerPhone = remember { profilePrefs.getString("user_phone", "+91 6265798340") ?: "+91 6265798340" }
    val workerSkill = remember { profilePrefs.getString("user_skill", "Mason / Mistri") ?: "Mason / Mistri" }
    val workerWage = remember { profilePrefs.getString("user_rate", "600") ?: "600" }

    var currentRealLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
    }
    var showLocationDialog by remember { mutableStateOf(false) }
    var locationSearchQuery by remember { mutableStateOf("") }
    var isSearchingOnlineLocations by remember { mutableStateOf(false) }
    var isDetectingGps by remember { mutableStateOf(false) }
    val onlineLocationSuggestions = remember { mutableStateListOf<String>() }

    // 0 = Home View (Left side of reference image), 1 = Search / Available Jobs View (Right side of reference image)
    var bottomNavIndex by remember { mutableIntStateOf(0) }
    var jobSearchText by remember { mutableStateOf("") }
    var sortByHighestWage by remember { mutableStateOf(true) }

    var localAvailable by remember { mutableStateOf(settingsPrefs.getBoolean("available_today", isAvailable)) }
    var isLoadingJobs by remember { mutableStateOf(true) }
    val liveCloudJobs = remember { mutableStateListOf<CloudLabourJob>() }
    val acceptedJobIds = remember {
        val savedSet = profilePrefs.getStringSet("worker_accepted_jobs", emptySet()) ?: emptySet()
        mutableStateListOf<Long>().apply { addAll(savedSet.mapNotNull { it.toLongOrNull() }) }
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
                        var idx = 0
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            idx++
                            loaded.add(
                                CloudLabourJob(
                                    key = k,
                                    id = obj.optLong("id", System.currentTimeMillis()),
                                    title = obj.optString("title", "House Repair Work"),
                                    category = obj.optString("category", "Mason"),
                                    description = obj.optString("description", "Need skilled worker for daily work."),
                                    dailyRate = obj.optInt("dailyRate", 600),
                                    location = obj.optString("location", currentRealLocation),
                                    customerName = obj.optString("customerName", "Ramesh Kumar"),
                                    customerPhone = obj.optString("customerPhone", "+91 6265798340"),
                                    urgency = obj.optString("urgency", "Today, 9:00 AM"),
                                    status = obj.optString("status", "PENDING"),
                                    distanceKm = 2 + (idx % 4)
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
                loaded.addAll(
                    listOf(
                        CloudLabourJob(
                            key = "sample_silwani_1",
                            id = 101L,
                            title = "Wall Plastering & Brickwork",
                            category = "Mason",
                            description = "Need experienced mason for boundary wall plastering and brickwork.",
                            dailyRate = 600,
                            location = currentRealLocation,
                            customerName = "Ramesh Kumar",
                            customerPhone = "+91 9876543210",
                            urgency = "Today, 9:00 AM",
                            status = "PENDING",
                            distanceKm = 2
                        ),
                        CloudLabourJob(
                            key = "sample_silwani_2",
                            id = 102L,
                            title = "House Wiring & Switchboard",
                            category = "Electrician",
                            description = "Complete room wiring and fan installation work.",
                            dailyRate = 550,
                            location = currentRealLocation,
                            customerName = "Suresh Patel",
                            customerPhone = "+91 9123456780",
                            urgency = "Today, 10:00 AM",
                            status = "PENDING",
                            distanceKm = 3
                        ),
                        CloudLabourJob(
                            key = "sample_silwani_3",
                            id = 103L,
                            title = "Water Tank Pipe Fitting",
                            category = "Plumber",
                            description = "Bathroom tap and overhead tank pipeline fitting.",
                            dailyRate = 500,
                            location = currentRealLocation,
                            customerName = "Amit Yadav",
                            customerPhone = "+91 9988776655",
                            urgency = "Tomorrow, 9:00 AM",
                            status = "PENDING",
                            distanceKm = 4
                        )
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
                                val villageOrTown = addr.optString("village", addr.optString("town", addr.optString("city", "")))
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
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isDetectingGps = false
                        Toast.makeText(context, "Phone ka GPS On karein!", Toast.LENGTH_LONG).show()
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
            detectRealGpsLocation()
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
        appLang = settingsPrefs.getString("app_language", "Hinglish") ?: "Hinglish"
        currentRealLocation = profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)"
        syncWorkerLocationAndAvailabilityToCloud(currentRealLocation, localAvailable)
        loadLiveJobsFromCloud()
    }

    val filteredJobs = liveCloudJobs.filter { job ->
        val matchesCategory = selectedCategory.isNullOrBlank() ||
                selectedCategory.equals("All", ignoreCase = true) ||
                job.category.contains(selectedCategory, ignoreCase = true) ||
                job.title.contains(selectedCategory, ignoreCase = true)

        val matchesSearch = jobSearchText.isBlank() ||
                job.title.contains(jobSearchText, ignoreCase = true) ||
                job.category.contains(jobSearchText, ignoreCase = true) ||
                job.location.contains(jobSearchText, ignoreCase = true) ||
                job.customerName.contains(jobSearchText, ignoreCase = true)

        matchesCategory && matchesSearch
    }.let { list ->
        if (sortByHighestWage) list.sortedByDescending { it.dailyRate } else list.sortedBy { it.distanceKm }
    }

    val pendingWorkRequests = filteredJobs.filter { !acceptedJobIds.contains(it.id) && it.status != "COMPLETED" }
    val myAcceptedJobs = filteredJobs.filter { acceptedJobIds.contains(it.id) || it.status == "ACCEPTED" }
    val displayedJobs = if (activeTab == 0) pendingWorkRequests else myAcceptedJobs

    data class LabourCategoryGridSpec(
        val name: String,
        val icon: ImageVector,
        val iconTint: Color,
        val circleBg: Color
    )

    val categorySpecs = listOf(
        LabourCategoryGridSpec("Mason", Icons.Default.Build, Color(0xFF1E3A8A), Color(0xFFDBEAFE)),
        LabourCategoryGridSpec("Electrician", Icons.Default.FlashOn, Color(0xFFD97706), Color(0xFFFEF3C7)),
        LabourCategoryGridSpec("Plumber", Icons.Default.Plumbing, Color(0xFF15803D), Color(0xFFDCFCE7)),
        LabourCategoryGridSpec("Painter", Icons.Default.FormatPaint, Color(0xFFDC2626), Color(0xFFFEE2E2)),
        LabourCategoryGridSpec("Carpenter", Icons.Default.Handyman, Color(0xFF6D28D9), Color(0xFFEDE9FE)),
        LabourCategoryGridSpec("More", Icons.Default.GridView, Color(0xFF334155), Color(0xFFF1F5F9))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        if (bottomNavIndex == 0) {
            // ==================== VIEW 1: WORKER HOME SCREEN (EXACT SAME AS CUSTOMER HOME SCREEN) ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp)
            ) {
                // 1. Same Top Header Bar
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
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WorkoraNavy,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(WorkoraNavy, shape = RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "WORKER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = tr("रोज़ काम पाएं और कमाएं", "Find Daily Work & Earn", "Find Daily Work & Earn"),
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
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("मोड", "Role", "Role"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                        }

                        IconButton(
                            onClick = onOpenProfile,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = WorkoraNavy, modifier = Modifier.size(22.dp))
                        }
                    }
                }

                // 2. Same Location Selector Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Card(
                        onClick = { showLocationDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(WorkoraOrange.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(19.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
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
                                        text = tr("लोकेशन बदलने के लिए टैप करें", "Tap to change location", "Tap to change location"),
                                        fontSize = 11.sp,
                                        color = WorkoraTextMuted
                                    )
                                }
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = WorkoraTextMuted)
                        }
                    }
                }

                // 3. Same Hero Banner Card with Skilled Worker Illustration
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD))
                                    )
                                )
                                .padding(horizontal = 18.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1.35f)) {
                                    Text(
                                        text = tr("अपने पास रोज़\nनया काम पाएं", "Find Daily Work\nNear You", "Find Daily Work\nNear You"),
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WorkoraNavy,
                                        lineHeight = 24.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "$workerName • $workerSkill (₹$workerWage/day)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155),
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = {
                                            val nextState = !localAvailable
                                            localAvailable = nextState
                                            onToggleAvailability()
                                            syncWorkerLocationAndAvailabilityToCloud(currentRealLocation, nextState)
                                            Toast.makeText(
                                                context,
                                                if (nextState) "You are Online & Available Today! ✓" else "Status set to Offline",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (localAvailable) WorkoraOrange else Color(0xFF64748B)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (localAvailable) Color(0xFF16A34A) else Color(0xFF64748B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (localAvailable) tr("आज काम के लिए उपलब्ध ✓", "Available for Work Today", "Available for Work Today") else tr("ऑफलाइन (ऑनलाइन करें)", "Offline (Tap to go Online)", "Offline (Tap to go Online)"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                LabourHeroIllustration(size = 104.dp)
                            }
                        }
                    }
                }

                // 4. Same Popular Categories 3x2 Grid
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
                            text = tr("लोकप्रिय काम श्रेणियाँ", "Popular Categories", "Popular Categories"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraTextDark
                        )
                        Text(
                            text = tr("सभी देखें >", "View All >", "View All >"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraNavy,
                            modifier = Modifier.clickable {
                                onCategorySelected("All")
                                bottomNavIndex = 1
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categorySpecs.chunked(3).forEach { rowSpecs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowSpecs.forEach { spec ->
                                    val isSelected = selectedCategory?.equals(spec.name, ignoreCase = true) == true
                                    Card(
                                        onClick = {
                                            if (spec.name == "More") {
                                                onCategorySelected("All")
                                            } else {
                                                onCategorySelected(spec.name)
                                            }
                                            bottomNavIndex = 1
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(92.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, if (isSelected) WorkoraOrange else Color(0xFFE2E8F0)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(spec.circleBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = spec.icon,
                                                    contentDescription = spec.name,
                                                    tint = spec.iconTint,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = spec.name,
                                                fontSize = 12.sp,
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

                // 5. Same Verified & Trusted Worker Shield Banner
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Card(
                        onClick = onOpenProfile,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDCFCE7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = tr("वेरिफाइड और भरोसेमंद कारीगर प्रोफाइल", "Verified & Trusted Worker Profile", "Verified & Trusted Worker Profile"),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WorkoraNavy
                                    )
                                    Text(
                                        text = "${pendingWorkRequests.size} Jobs Nearby • Wage: ₹$workerWage/day • Rating: 4.9 ★",
                                        fontSize = 11.sp,
                                        color = WorkoraTextMuted
                                    )
                                }
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = WorkoraNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 6. Recent Jobs Section (Identical Layout to Customer Screen)
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
                            text = tr("हाल ही के काम (Recent Jobs)", "Recent Jobs (${pendingWorkRequests.size})", "Recent Jobs (${pendingWorkRequests.size})"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraTextDark
                        )
                        Text(
                            text = tr("सभी देखें >", "View All >", "View All >"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraNavy,
                            modifier = Modifier.clickable { bottomNavIndex = 1 }
                        )
                    }

                    displayedJobs.take(3).forEach { job ->
                        val isAccepted = acceptedJobIds.contains(job.id) || job.status == "ACCEPTED"
                        ModernJobRequestCard(
                            job = job,
                            isAccepted = isAccepted,
                            onCallCustomer = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.customerPhone}")))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Phone: ${job.customerPhone}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onAcceptOrComplete = {
                                if (!isAccepted) {
                                    acceptedJobIds.add(job.id)
                                    profilePrefs.edit()
                                        .putStringSet("worker_accepted_jobs", acceptedJobIds.map { it.toString() }.toSet())
                                        .apply()
                                    Toast.makeText(context, "Work Accepted! ✓", Toast.LENGTH_SHORT).show()
                                } else {
                                    onCompleteJob(job.id)
                                    acceptedJobIds.remove(job.id)
                                    Toast.makeText(context, "Job Completed! ✓", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        } else {
            // ==================== VIEW 2: AVAILABLE WORK REQUESTS / SEARCH SCREEN (EXACT RIGHT SIDE OF REFERENCE IMAGE) ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp)
            ) {
                // 1. Top Header: Back Arrow + "Available Work" + Search Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { bottomNavIndex = 0 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WorkoraNavy)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (activeTab == 0) tr("उपलब्ध काम (Available Jobs)", "Available Work Requests", "Available Work Requests") else tr("मेरे स्वीकार किए काम", "My Accepted Jobs", "My Accepted Jobs"),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                    }
                    IconButton(onClick = { loadLiveJobsFromCloud() }) {
                        Icon(Icons.Default.Search, contentDescription = "Refresh", tint = WorkoraNavy)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 2. Location Pill with "Change" link
                    Card(
                        onClick = { showLocationDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentRealLocation,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = WorkoraTextDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = tr("बदलें", "Change", "Change"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }

                    // 3. Rounded Search Bar
                    OutlinedTextField(
                        value = jobSearchText,
                        onValueChange = { jobSearchText = it },
                        placeholder = { Text("Search by work, skill or location...", fontSize = 13.sp, color = WorkoraTextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkoraTextMuted) },
                        trailingIcon = {
                            if (jobSearchText.isNotBlank()) {
                                IconButton(onClick = { jobSearchText = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = WorkoraTextMuted)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraNavy,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 4. Category Chips Row: All, Mason, Electrician, Plumber, Painter, Carpenter
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val allCats = listOf("All", "Mason", "Electrician", "Plumber", "Painter", "Carpenter")
                        allCats.forEach { cat ->
                            val isSelected = (cat == "All" && (selectedCategory == null || selectedCategory == "All")) ||
                                    selectedCategory?.equals(cat, ignoreCase = true) == true
                            Button(
                                onClick = { onCategorySelected(cat) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) WorkoraNavy else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isSelected) WorkoraNavy else Color(0xFFE2E8F0)),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else WorkoraTextDark
                                )
                            }
                        }
                    }

                    // 5. "X Jobs Found" + "Sort by v"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${displayedJobs.size} Jobs Found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraTextDark
                        )

                        OutlinedButton(
                            onClick = { sortByHighestWage = !sortByHighestWage },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (sortByHighestWage) "Sort by: Highest Wage" else "Sort by: Nearest",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraTextDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = WorkoraTextDark, modifier = Modifier.size(15.dp))
                        }
                    }

                    // 6. Jobs Cards List
                    if (isLoadingJobs) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = WorkoraOrange)
                        }
                    } else if (displayedJobs.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No jobs found for this filter.", fontWeight = FontWeight.Bold, color = WorkoraNavy)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(onClick = { onCategorySelected("All") }) {
                                    Text("Show All Jobs")
                                }
                            }
                        }
                    } else {
                        displayedJobs.forEach { job ->
                            val isAccepted = acceptedJobIds.contains(job.id) || job.status == "ACCEPTED"
                            ModernJobRequestCard(
                                job = job,
                                isAccepted = isAccepted,
                                onCallCustomer = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${job.customerPhone}")))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Phone: ${job.customerPhone}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onAcceptOrComplete = {
                                    if (!isAccepted) {
                                        acceptedJobIds.add(job.id)
                                        profilePrefs.edit()
                                            .putStringSet("worker_accepted_jobs", acceptedJobIds.map { it.toString() }.toSet())
                                            .apply()
                                        Toast.makeText(context, "Work Accepted! ✓", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onCompleteJob(job.id)
                                        acceptedJobIds.remove(job.id)
                                        Toast.makeText(context, "Job Completed! ✓", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==================== FIXED BOTTOM NAVIGATION BAR (HOME, SEARCH, MY JOBS, PROFILE) ====================
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            bottomNavIndex = 0
                            onTabSelected(0)
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (bottomNavIndex == 0) WorkoraNavy else WorkoraTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Home",
                        fontSize = 11.sp,
                        fontWeight = if (bottomNavIndex == 0) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 0) WorkoraNavy else WorkoraTextMuted
                    )
                }

                // 2. Search
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            bottomNavIndex = 1
                            onTabSelected(0)
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (bottomNavIndex == 1 && activeTab == 0) WorkoraNavy else WorkoraTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Search",
                        fontSize = 11.sp,
                        fontWeight = if (bottomNavIndex == 1 && activeTab == 0) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 1 && activeTab == 0) WorkoraNavy else WorkoraTextMuted
                    )
                }

                // 3. My Jobs
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            bottomNavIndex = 1
                            onTabSelected(1)
                        }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF64748B).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "My Jobs", tint = WorkoraNavy, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "My Jobs",
                        fontSize = 11.sp,
                        fontWeight = if (activeTab == 1) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (activeTab == 1) WorkoraNavy else WorkoraTextMuted
                    )
                }

                // 4. Profile
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onOpenProfile() }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = WorkoraTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Profile",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = WorkoraTextMuted
                    )
                }
            }
        }
    }

    // ==================== REAL LOCATION SELECTOR DIALOG ====================
    if (showLocationDialog) {
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
                    Text("Select Real Location 📍", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use Current Live GPS Location", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedTextField(
                        value = locationSearchQuery,
                        onValueChange = {
                            locationSearchQuery = it
                            if (it.length >= 3) searchRealLocationsOnline(it)
                        },
                        label = { Text("Search Village or City...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (locationSearchQuery.trim().isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                val chosen = locationSearchQuery.trim()
                                currentRealLocation = chosen
                                syncWorkerLocationAndAvailabilityToCloud(chosen, localAvailable)
                                showLocationDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set '${locationSearchQuery.trim()}'", fontWeight = FontWeight.Bold, color = WorkoraOrange)
                        }
                    }

                    if (onlineLocationSuggestions.isNotEmpty()) {
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
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isCurrent) WorkoraOrange else WorkoraNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(loc, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isCurrent) Color.White else WorkoraTextDark)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// Reusable Modern Job Request Card matching Reference Image 100%
@Composable
private fun ModernJobRequestCard(
    job: CloudLabourJob,
    isAccepted: Boolean,
    onCallCustomer: () -> Unit,
    onAcceptOrComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isAccepted) Color(0xFF16A34A) else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    LabourJobAvatarIcon(
                        accentColor = WorkoraNavy,
                        size = 54.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = job.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraTextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${job.category} • ${job.customerName}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = WorkoraTextMuted
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "4.9",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraTextDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${job.urgency})",
                                fontSize = 11.sp,
                                color = WorkoraTextMuted
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = WorkoraTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${job.distanceKm} km away • ${job.location.split(",").firstOrNull() ?: "Silwani"}",
                                fontSize = 11.sp,
                                color = WorkoraTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isAccepted) Color(0xFFDCFCE7) else Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isAccepted) "Accepted" else "Available",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAccepted) Color(0xFF16A34A) else Color(0xFF0284C7)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = WorkoraTextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "₹${job.dailyRate}/day",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )
                }
            }

            // Quick Action Row: Call Customer & Accept Work Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCallCustomer,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, WorkoraNavy),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Customer", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy, maxLines = 1)
                }

                Button(
                    onClick = onAcceptOrComplete,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAccepted) Color(0xFF16A34A) else WorkoraOrange
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAccepted) "Mark Completed ✓" else "Accept Work ✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
