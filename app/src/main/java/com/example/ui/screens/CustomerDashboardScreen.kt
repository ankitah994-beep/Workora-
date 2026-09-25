package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.JobPost
import com.example.model.Worker
import java.util.Locale

private data class PrototypeWorkerItem(
    val id: String,
    val name: String,
    val skill: String,
    val location: String,
    val dailyWage: String,
    val phone: String,
    val rating: String,
    val jobsDone: Int,
    val avatarKey: String,
    val originalWorker: Worker? = null
)

private fun extractSafeObjectField(obj: Any, vararg fieldNames: String, fallback: String = ""): String {
    for (name in fieldNames) {
        try {
            val field = obj.javaClass.getDeclaredField(name)
            field.isAccessible = true
            val value = field.get(obj)?.toString()?.trim() ?: ""
            if (value.isNotEmpty() && value != "null") {
                return value
            }
        } catch (_: Exception) {
        }
    }
    return fallback
}

private fun extractSafeJobPay(job: JobPost): String {
    val raw = extractSafeObjectField(job, "dailyRate", "dailyWage", "budget", "wage", "pay", "salary", "amount", "rate", fallback = "600")
    return when {
        raw.isEmpty() || raw == "0" -> "₹600/दिन"
        raw.startsWith("₹") -> raw
        else -> "₹$raw/दिन"
    }
}

@SuppressLint("MissingPermission")
private fun fetchCustomerRealGpsAddress(
    context: Context,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetEnabled) {
            onError("कृपया फोन का GPS / Location चालू करें")
            return
        }

        val decodeLocation: (Location) -> Unit = { loc ->
            Thread {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    val addr = addresses?.firstOrNull()
                    val formatted = if (addr != null) {
                        val subLoc = addr.subLocality ?: addr.locality ?: addr.subAdminArea ?: ""
                        val city = addr.locality ?: addr.subAdminArea ?: ""
                        val dist = addr.subAdminArea ?: addr.adminArea ?: ""
                        listOf(subLoc, city, dist)
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .distinct()
                            .joinToString(", ")
                            .ifEmpty { "Silwani, Raisen (MP)" }
                    } else {
                        "Lat: ${String.format(Locale.US, "%.4f", loc.latitude)}, Lng: ${String.format(Locale.US, "%.4f", loc.longitude)}"
                    }
                    android.os.Handler(Looper.getMainLooper()).post {
                        onSuccess(formatted)
                    }
                } catch (e: Exception) {
                    android.os.Handler(Looper.getMainLooper()).post {
                        onSuccess("Silwani, Raisen (MP)")
                    }
                }
            }.start()
        }

        val lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (lastLoc != null) {
            decodeLocation(lastLoc)
        } else {
            val provider = if (isNetEnabled) LocationManager.NETWORK_PROVIDER else LocationManager.GPS_PROVIDER
            locationManager.requestSingleUpdate(
                provider,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        decodeLocation(location)
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                },
                Looper.getMainLooper()
            )
        }
    } catch (e: Exception) {
        onError("लोकेशन प्राप्त करने में समस्या आई")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    onPostJob: (String, String, String, Int, String, Int, String, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onHireWorker: (Worker) -> Unit = {},
    onCompleteJob: (Long) -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenFilters: () -> Unit = {},
    toastMessage: String? = null,
    onPostJobClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenHistory: () -> Unit = {}
) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val brandingPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }

    val appName = remember { brandingPrefs.getString("app_name", "Workora") ?: "Workora" }
    val appTagline = remember { brandingPrefs.getString("app_tagline", "FIND. HIRE. WORK.") ?: "FIND. HIRE. WORK." }

    var currentLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
    }
    var showLocationDialog by remember { mutableStateOf(false) }
    var tempLocationInput by remember { mutableStateOf(currentLocation) }
    var isDetectingGps by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isDetectingGps = true
            fetchCustomerRealGpsAddress(
                context = context,
                onSuccess = { detectedAddr ->
                    isDetectingGps = false
                    tempLocationInput = detectedAddr
                    currentLocation = detectedAddr
                    profilePrefs.edit().putString("user_location", detectedAddr).apply()
                    Toast.makeText(context, "Live GPS Location: $detectedAddr ✓", Toast.LENGTH_SHORT).show()
                },
                onError = { errMsg ->
                    isDetectingGps = false
                    Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "GPS के लिए Location Permission आवश्यक है", Toast.LENGTH_SHORT).show()
        }
    }

    val triggerRealGpsDetection: () -> Unit = {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            isDetectingGps = true
            fetchCustomerRealGpsAddress(
                context = context,
                onSuccess = { detectedAddr ->
                    isDetectingGps = false
                    tempLocationInput = detectedAddr
                    currentLocation = detectedAddr
                    profilePrefs.edit().putString("user_location", detectedAddr).apply()
                    Toast.makeText(context, "Live GPS Location: $detectedAddr ✓", Toast.LENGTH_SHORT).show()
                },
                onError = { errMsg ->
                    isDetectingGps = false
                    Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    var showPostJobDialog by remember { mutableStateOf(false) }
    var jobTitleInput by remember { mutableStateOf("") }
    var jobCategoryInput by remember { mutableStateOf("Mason") }
    var jobDescInput by remember { mutableStateOf("") }
    var jobRateInput by remember { mutableStateOf("600") }
    var jobWorkersCountInput by remember { mutableStateOf("1") }
    var jobUrgencyInput by remember { mutableStateOf("Immediate") }

    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    var localTab by remember(activeTab) { mutableStateOf(if (activeTab in 0..2) activeTab else 0) }
    var localSearchQuery by remember(searchQuery) { mutableStateOf(searchQuery) }
    var activeCategoryFilter by remember(selectedCategory) { mutableStateOf(selectedCategory ?: "All") }

    val allDisplayWorkers = remember(workers) {
        val defaultList = listOf(
            PrototypeWorkerItem(
                id = "proto_ramesh",
                name = "Ramesh Kumar",
                skill = "Mason / राजमिस्त्री",
                location = "Silwani, Raisen (MP)",
                dailyWage = "₹700/दिन",
                phone = "6265798340",
                rating = "4.9",
                jobsDone = 48,
                avatarKey = "ramesh"
            ),
            PrototypeWorkerItem(
                id = "proto_mohan",
                name = "Mohan Lal",
                skill = "Plumber / प्लंबर",
                location = "Silwani, Raisen (MP)",
                dailyWage = "₹600/दिन",
                phone = "6265798340",
                rating = "4.8",
                jobsDone = 35,
                avatarKey = "mohan"
            ),
            PrototypeWorkerItem(
                id = "proto_mahesh",
                name = "Mahesh Ahirwar",
                skill = "Electrician / इलेक्ट्रीशियन",
                location = "Raisen, MP",
                dailyWage = "₹650/दिन",
                phone = "6265798340",
                rating = "4.9",
                jobsDone = 62,
                avatarKey = "mahesh"
            ),
            PrototypeWorkerItem(
                id = "proto_suresh",
                name = "Suresh Vishwakarma",
                skill = "Carpenter & Painter / बढ़ई",
                location = "Silwani, Raisen (MP)",
                dailyWage = "₹650/दिन",
                phone = "6265798340",
                rating = "4.7",
                jobsDone = 29,
                avatarKey = "suresh"
            )
        )

        val liveMapped = workers.mapIndexed { index, w ->
            val avatarKeys = listOf("ramesh", "mohan", "mahesh", "suresh")
            val rawName = extractSafeObjectField(w, "name", "fullName", "workerName", fallback = "कुशल कारीगर")
            val rawSkill = extractSafeObjectField(w, "category", "skill", "role", "profession", "trade", fallback = "General Worker")
            val rawLoc = extractSafeObjectField(w, "location", "city", "area", "address", fallback = "Silwani, Raisen (MP)")
            val rawWage = extractSafeObjectField(w, "dailyWage", "dailyRate", "wage", "rate", "pay", "price", fallback = "500")
            val rawPhone = extractSafeObjectField(w, "phone", "mobile", "contactPhone", "phoneNumber", fallback = "6265798340")
            val rawRating = extractSafeObjectField(w, "rating", fallback = "4.8")

            val formattedWage = when {
                rawWage.isEmpty() || rawWage == "0" -> "₹500/दिन"
                rawWage.startsWith("₹") -> rawWage
                else -> "₹$rawWage/दिन"
            }

            PrototypeWorkerItem(
                id = "live_${index}_$rawName",
                name = rawName,
                skill = rawSkill,
                location = rawLoc,
                dailyWage = formattedWage,
                phone = rawPhone,
                rating = rawRating,
                jobsDone = 25 + (index * 7),
                avatarKey = avatarKeys[index % avatarKeys.size],
                originalWorker = w
            )
        }

        if (liveMapped.isEmpty()) defaultList else liveMapped + defaultList
    }

    val categories = listOf(
        "Mason / मिस्त्री",
        "Plumber / प्लंबर",
        "Electrician / इलेक्ट्रीशियन",
        "Painter / पेंटर",
        "Carpenter / बढ़ई",
        "Labour / मजदूर"
    )

    val filteredWorkers = remember(allDisplayWorkers, localSearchQuery, activeCategoryFilter) {
        allDisplayWorkers.filter { worker ->
            val matchesCategory = if (activeCategoryFilter.isBlank() || activeCategoryFilter == "All") {
                true
            } else {
                val cleanFilter = activeCategoryFilter.substringBefore("/").trim()
                worker.skill.contains(cleanFilter, ignoreCase = true)
            }
            val matchesSearch = if (localSearchQuery.isBlank()) {
                true
            } else {
                worker.name.contains(localSearchQuery, ignoreCase = true) ||
                    worker.skill.contains(localSearchQuery, ignoreCase = true) ||
                    worker.location.contains(localSearchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    val handleSettingsNavigation: () -> Unit = {
        onOpenProfile()
        onNavigateToProfile()
    }

    val handleOpenPostJob: () -> Unit = {
        showPostJobDialog = true
        onPostJobClick()
    }

    Scaffold(
        containerColor = Color(0xFFF4F7FB),
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WorkoraBottomNavButton(
                        icon = Icons.Default.Menu,
                        label = "Menu",
                        selected = localTab == 0 || localTab == 1,
                        onClick = {
                            localTab = 0
                            onTabSelected(0)
                        }
                    )

                    WorkoraBottomNavButton(
                        icon = Icons.Default.History,
                        label = "History",
                        selected = localTab == 2,
                        onClick = {
                            localTab = 2
                            onTabSelected(2)
                        }
                    )

                    WorkoraBottomNavButton(
                        icon = Icons.Default.AddCircle,
                        label = "Post Skills",
                        selected = false,
                        onClick = handleOpenPostJob
                    )

                    WorkoraBottomNavButton(
                        icon = Icons.Default.Chat,
                        label = "Chat",
                        selected = false,
                        onClick = { onOpenChat() }
                    )

                    WorkoraBottomNavButton(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        selected = false,
                        onClick = handleSettingsNavigation
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (localTab == 0) {
                // ==================== VIEW 0: MENU / HOME SCREEN ====================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Surface(
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        WorkoraLogoCanvas(size = 44.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = appName,
                                                fontSize = 21.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0D253F)
                                            )
                                            Text(
                                                text = appTagline,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFF6F00),
                                                letterSpacing = 0.9.sp
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = onOpenNotifications) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Notifications",
                                                tint = Color(0xFF0D253F)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFFEFF6FF),
                                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                            modifier = Modifier.clickable { onSwitchRole() }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SwapHoriz,
                                                    contentDescription = "Switch Role",
                                                    tint = Color(0xFF1D4ED8),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Switch Role",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1D4ED8)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            tempLocationInput = currentLocation
                                            showLocationDialog = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = "Location",
                                                tint = Color(0xFFFF6F00),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = currentLocation,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF1E293B),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "Change >",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1565C0)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            shape = RoundedCornerShape(22.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF0D253F),
                                                Color(0xFF1565C0),
                                                Color(0xFF1E88E5)
                                            )
                                        )
                                    )
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = Color(0xFFFF6F00),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "SILWANI • RAISEN • MP",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Hire Trusted Local Workers Instantly",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            lineHeight = 23.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "कुशल मिस्त्री, प्लंबर, इलेक्ट्रीशियन और मजदूर सीधे बुलाएं",
                                            fontSize = 12.sp,
                                            color = Color(0xFFE3F2FD)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    localTab = 1
                                                    onTabSelected(1)
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFFF6F00),
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "Find Workers",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = handleOpenPostJob,
                                                border = BorderStroke(1.dp, Color.White),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "Post Skills",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    WorkoraHeroWorkerCanvas(size = 98.dp)
                                }
                            }
                        }
                    }

                    item {
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
                                    color = Color(0xFF0D253F)
                                )
                                Text(
                                    text = "View All >",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6F00),
                                    modifier = Modifier.clickable {
                                        activeCategoryFilter = "All"
                                        onCategorySelected("All")
                                        localTab = 1
                                        onTabSelected(1)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                categories.take(3).forEach { cat ->
                                    WorkoraCategoryItemCard(
                                        categoryTitle = cat,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            val shortCat = cat.substringBefore("/").trim()
                                            activeCategoryFilter = shortCat
                                            onCategorySelected(shortCat)
                                            localTab = 1
                                            onTabSelected(1)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                categories.drop(3).take(3).forEach { cat ->
                                    WorkoraCategoryItemCard(
                                        categoryTitle = cat,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            val shortCat = cat.substringBefore("/").trim()
                                            activeCategoryFilter = shortCat
                                            onCategorySelected(shortCat)
                                            localTab = 1
                                            onTabSelected(1)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = BorderStroke(1.dp, Color(0xFFA5D6A7))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "Verified",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "100% Verified Local Workers",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = "Background checked • Direct Call & Live Chat • 0% Commission",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Featured Workers (${allDisplayWorkers.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0D253F)
                            )
                            TextButton(onClick = {
                                localTab = 1
                                onTabSelected(1)
                            }) {
                                Text(
                                    text = "See All >",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            }
                        }
                    }

                    items(allDisplayWorkers.take(4)) { workerItem ->
                        WorkoraPrototypeWorkerCard(
                            worker = workerItem,
                            context = context,
                            onCardClick = {
                                handleSettingsNavigation()
                            },
                            onHireClick = {
                                workerItem.originalWorker?.let(onHireWorker)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Job Posts (${jobs.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0D253F)
                            )
                            TextButton(onClick = handleOpenPostJob) {
                                Text(
                                    text = "Post Skills",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF6F00)
                                )
                            }
                        }
                    }

                    if (jobs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "अपनी जरूरत का काम पोस्ट करें और कारीगरों से सीधे कॉल पाएं",
                                        fontSize = 13.sp,
                                        color = Color(0xFF64748B),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = handleOpenPostJob,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D253F)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Post Skills")
                                    }
                                }
                            }
                        }
                    } else {
                        items(jobs.take(5)) { job ->
                            WorkoraRecentJobRowCard(
                                job = job,
                                onCompleteClick = {
                                    val jobIdLong = extractSafeObjectField(job, "id", fallback = "0").toLongOrNull() ?: 0L
                                    if (jobIdLong != 0L) onCompleteJob(jobIdLong)
                                }
                            )
                        }
                    }
                }
            } else if (localTab == 1) {
                // ==================== VIEW 1: AVAILABLE WORKERS SEARCH SCREEN ====================
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = Color.White,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            localTab = 0
                                            onTabSelected(0)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color(0xFF0D253F)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Available Workers (${filteredWorkers.size})",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0D253F)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = onOpenFilters) {
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = "Filters",
                                            tint = Color(0xFF0D253F)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFFFFF8E1),
                                        border = BorderStroke(1.dp, Color(0xFFFFE082)),
                                        modifier = Modifier.clickable {
                                            tempLocationInput = currentLocation
                                            showLocationDialog = true
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFFFF6F00),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = currentLocation.take(14),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFE65100)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = localSearchQuery,
                                onValueChange = {
                                    localSearchQuery = it
                                    onSearchQueryChanged(it)
                                },
                                placeholder = {
                                    Text(
                                        text = "Search Ramesh, Mohan, Mason, Plumber...",
                                        fontSize = 13.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF1565C0)
                                    )
                                },
                                trailingIcon = {
                                    if (localSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = {
                                            localSearchQuery = ""
                                            onSearchQueryChanged("")
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear"
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1565C0),
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val chipOptions = listOf("All", "Mason", "Plumber", "Electrician", "Painter", "Carpenter", "Labour")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chipOptions.forEach { chip ->
                                    val isSelected = activeCategoryFilter.equals(chip, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            activeCategoryFilter = chip
                                            onCategorySelected(chip)
                                        },
                                        label = {
                                            Text(
                                                text = chip,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF0D253F),
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (filteredWorkers.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonSearch,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "कोई कारीगर नहीं मिला",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        } else {
                            items(filteredWorkers) { workerItem ->
                                WorkoraPrototypeWorkerCard(
                                    worker = workerItem,
                                    context = context,
                                    onCardClick = {
                                        handleSettingsNavigation()
                                    },
                                    onHireClick = {
                                        workerItem.originalWorker?.let(onHireWorker)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // ==================== VIEW 2: HISTORY ====================
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        localTab = 0
                                        onTabSelected(0)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color(0xFF0D253F)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Work History (${jobs.size})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0D253F)
                                )
                            }

                            Button(
                                onClick = handleOpenPostJob,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Post Skills",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (jobs.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(52.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "अभी तक कोई काम हिस्ट्री में नहीं है",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = handleOpenPostJob,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D253F)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Post Skills")
                                    }
                                }
                            }
                        } else {
                            items(jobs) { job ->
                                WorkoraRecentJobRowCard(
                                    job = job,
                                    onCompleteClick = {
                                        val jobIdLong = extractSafeObjectField(job, "id", fallback = "0").toLongOrNull() ?: 0L
                                        if (jobIdLong != 0L) onCompleteJob(jobIdLong)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationDialog) {
        val quickLocations = listOf(
            "Silwani, Raisen (MP)",
            "Raisen, MP",
            "Begamganj, Raisen",
            "Gairatganj, Raisen",
            "Udaipura, Raisen",
            "Bareli, Raisen",
            "Bhopal, MP",
            "Sagar, MP",
            "Vidisha, MP"
        )

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            containerColor = Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFFF6F00)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "अपनी लोकेशन अपडेट करें",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0D253F),
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = triggerRealGpsDetection,
                        enabled = !isDetectingGps,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF1B5E20)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isDetectingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF1B5E20)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GPS से लोकेशन खोजी जा रही है...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "GPS",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Use Current GPS Location (मेरी लाइव लोकेशन)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = tempLocationInput,
                        onValueChange = { tempLocationInput = it },
                        label = { Text("शहर / गाँव / तहसील (जैसे: Silwani, Raisen)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF1565C0),
                            unfocusedBorderColor = Color(0xFF94A3B8)
                        )
                    )

                    Text(
                        text = "तुरंत चुनें (Popular Locations):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickLocations.forEach { loc ->
                            val isSelected = tempLocationInput.equals(loc, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    tempLocationInput = loc
                                    currentLocation = loc
                                    profilePrefs.edit().putString("user_location", loc).apply()
                                    showLocationDialog = false
                                },
                                label = {
                                    Text(
                                        text = loc,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0D253F),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempLocationInput.isNotBlank()) {
                            currentLocation = tempLocationInput.trim()
                            profilePrefs.edit().putString("user_location", currentLocation).apply()
                        }
                        showLocationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D253F))
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPostJobDialog) {
        AlertDialog(
            onDismissRequest = { showPostJobDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Post Skills",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0D253F)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = jobTitleInput,
                        onValueChange = { jobTitleInput = it },
                        label = { Text("काम का नाम (जैसे: मकान की जुड़ाई / प्लंबिंग)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = jobCategoryInput,
                        onValueChange = { jobCategoryInput = it },
                        label = { Text("कैटेगरी (Mason / Plumber / Electrician / Labour)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = jobRateInput,
                        onValueChange = { jobRateInput = it },
                        label = { Text("दिहाड़ी / बजट ₹ (जैसे: 600)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = jobWorkersCountInput,
                        onValueChange = { jobWorkersCountInput = it },
                        label = { Text("कितने कारीगर चाहिए? (जैसे: 2)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = jobDescInput,
                        onValueChange = { jobDescInput = it },
                        label = { Text("काम का विवरण (Description)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalTitle = jobTitleInput.trim().ifEmpty { "$jobCategoryInput का काम" }
                        val finalCat = jobCategoryInput.trim().ifEmpty { "General" }
                        val finalDesc = jobDescInput.trim().ifEmpty { "तुरंत काम के लिए संपर्क करें" }
                        val finalRate = jobRateInput.filter { it.isDigit() }.toIntOrNull() ?: 600
                        val finalWorkers = jobWorkersCountInput.filter { it.isDigit() }.toIntOrNull() ?: 1

                        onPostJob(
                            finalTitle,
                            finalCat,
                            finalDesc,
                            finalRate,
                            currentLocation,
                            finalWorkers,
                            jobUrgencyInput,
                            ""
                        )
                        jobTitleInput = ""
                        jobDescInput = ""
                        showPostJobDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6F00))
                ) {
                    Text("Post Skills ✓", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostJobDialog = false }) {
                    Text("Cancel", color = Color(0xFF475569), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun WorkoraLogoCanvas(size: Dp = 44.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0D253F), Color(0xFF1565C0))
            ),
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(w * 0.24f, h * 0.24f)
        )

        drawRoundRect(
            color = Color(0xFFFF6F00),
            topLeft = Offset(w * 0.22f, h * 0.16f),
            size = Size(w * 0.56f, h * 0.1f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        val wPath = Path().apply {
            moveTo(w * 0.22f, h * 0.36f)
            lineTo(w * 0.35f, h * 0.78f)
            lineTo(w * 0.5f, h * 0.5f)
            lineTo(w * 0.65f, h * 0.78f)
            lineTo(w * 0.78f, h * 0.36f)
        }
        drawPath(
            path = wPath,
            color = Color.White,
            style = Stroke(width = w * 0.1f, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun WorkoraWorkerAvatarCanvas(
    avatarKey: String,
    size: Dp = 56.dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val bgColor = when (avatarKey) {
            "ramesh" -> Color(0xFFE3F2FD)
            "mohan" -> Color(0xFFFFF3E0)
            "mahesh" -> Color(0xFFE8F5E9)
            else -> Color(0xFFF3E5F5)
        }
        val shirtColor = when (avatarKey) {
            "ramesh" -> Color(0xFF1565C0)
            "mohan" -> Color(0xFFE65100)
            "mahesh" -> Color(0xFF2E7D32)
            else -> Color(0xFF6A1B9A)
        }
        val hatColor = when (avatarKey) {
            "ramesh" -> Color(0xFFFFC107)
            "mohan" -> Color(0xFF1976D2)
            "mahesh" -> Color(0xFFFF8F00)
            else -> Color(0xFFFFD54F)
        }

        drawCircle(
            color = bgColor,
            radius = w * 0.5f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        drawArc(
            color = shirtColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.18f, h * 0.62f),
            size = Size(w * 0.64f, h * 0.55f)
        )

        drawCircle(
            color = Color(0xFFFFCC80),
            radius = w * 0.2f,
            center = Offset(w * 0.5f, h * 0.44f)
        )

        drawArc(
            color = hatColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.28f, h * 0.16f),
            size = Size(w * 0.44f, h * 0.32f)
        )
        drawLine(
            color = hatColor,
            start = Offset(w * 0.24f, h * 0.32f),
            end = Offset(w * 0.76f, h * 0.32f),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun WorkoraPrototypeWorkerCard(
    worker: PrototypeWorkerItem,
    context: Context,
    onCardClick: () -> Unit = {},
    onHireClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkoraWorkerAvatarCanvas(
                    avatarKey = worker.avatarKey,
                    size = 56.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = worker.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0D253F)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = worker.skill,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1565C0)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${worker.rating} (${worker.jobsDone} jobs)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = worker.location,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFE082))
                ) {
                    Text(
                        text = worker.dailyWage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onHireClick()
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D253F))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Call Now",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WorkoraBottomNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) Color(0xFFFF6F00) else Color(0xFF64748B)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = tint,
            maxLines = 1
        )
    }
}

@Composable
private fun WorkoraCategoryItemCard(
    categoryTitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val parts = categoryTitle.split("/")
    val englishTitle = parts.firstOrNull()?.trim() ?: categoryTitle
    val hindiTitle = parts.getOrNull(1)?.trim() ?: ""

    Card(
        modifier = modifier
            .height(106.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .clip(CircleShape)
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                WorkoraCustomCategoryIcon(categoryName = englishTitle, size = 26.dp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = englishTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D253F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (hindiTitle.isNotEmpty()) {
                Text(
                    text = hindiTitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun WorkoraCustomCategoryIcon(
    categoryName: String,
    size: Dp = 26.dp
) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val primary = Color(0xFF0D253F)
        val accent = Color(0xFFFF6F00)

        when {
            categoryName.contains("Mason", ignoreCase = true) -> {
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(w * 0.1f, h * 0.55f),
                    size = Size(w * 0.38f, h * 0.25f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(w * 0.52f, h * 0.55f),
                    size = Size(w * 0.38f, h * 0.25f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.28f, h * 0.24f),
                    size = Size(w * 0.44f, h * 0.25f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
            categoryName.contains("Plumber", ignoreCase = true) -> {
                drawLine(
                    color = primary,
                    start = Offset(w * 0.2f, h * 0.5f),
                    end = Offset(w * 0.8f, h * 0.5f),
                    strokeWidth = w * 0.16f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = accent,
                    radius = w * 0.16f,
                    center = Offset(w * 0.5f, h * 0.25f)
                )
            }
            categoryName.contains("Electrician", ignoreCase = true) -> {
                val bolt = Path().apply {
                    moveTo(w * 0.55f, h * 0.1f)
                    lineTo(w * 0.25f, h * 0.52f)
                    lineTo(w * 0.5f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.9f)
                    lineTo(w * 0.75f, h * 0.45f)
                    lineTo(w * 0.52f, h * 0.45f)
                    close()
                }
                drawPath(path = bolt, color = accent)
            }
            categoryName.contains("Painter", ignoreCase = true) -> {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.2f, h * 0.18f),
                    size = Size(w * 0.6f, h * 0.26f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawLine(
                    color = primary,
                    start = Offset(w * 0.5f, h * 0.44f),
                    end = Offset(w * 0.5f, h * 0.85f),
                    strokeWidth = w * 0.12f,
                    cap = StrokeCap.Round
                )
            }
            categoryName.contains("Carpenter", ignoreCase = true) -> {
                drawLine(
                    color = primary,
                    start = Offset(w * 0.3f, h * 0.78f),
                    end = Offset(w * 0.7f, h * 0.32f),
                    strokeWidth = w * 0.12f,
                    cap = StrokeCap.Round
                )
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.48f, h * 0.16f),
                    size = Size(w * 0.36f, h * 0.2f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
            else -> {
                drawArc(
                    color = accent,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.15f, h * 0.25f),
                    size = Size(w * 0.7f, h * 0.55f)
                )
                drawLine(
                    color = primary,
                    start = Offset(w * 0.1f, h * 0.55f),
                    end = Offset(w * 0.9f, h * 0.55f),
                    strokeWidth = w * 0.1f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun WorkoraHeroWorkerCanvas(size: Dp = 98.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = Color.White.copy(alpha = 0.16f),
            radius = w * 0.48f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        drawRoundRect(
            color = Color(0xFFFF6F00),
            topLeft = Offset(w * 0.22f, h * 0.58f),
            size = Size(w * 0.56f, h * 0.34f),
            cornerRadius = CornerRadius(18f, 18f)
        )

        drawCircle(
            color = Color(0xFFFFCC80),
            radius = w * 0.18f,
            center = Offset(w * 0.5f, h * 0.42f)
        )

        drawArc(
            color = Color(0xFFFFD54F),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.28f, h * 0.16f),
            size = Size(w * 0.44f, h * 0.32f)
        )

        drawLine(
            color = Color(0xFFFFA000),
            start = Offset(w * 0.24f, h * 0.32f),
            end = Offset(w * 0.76f, h * 0.32f),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color(0xFFECEFF1),
            start = Offset(w * 0.78f, h * 0.75f),
            end = Offset(w * 0.88f, h * 0.38f),
            strokeWidth = w * 0.07f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFFECEFF1),
            radius = w * 0.08f,
            center = Offset(w * 0.88f, h * 0.34f),
            style = Stroke(width = w * 0.04f)
        )
    }
}

@Composable
private fun WorkoraRecentJobRowCard(
    job: JobPost,
    onCompleteClick: () -> Unit = {}
) {
    val payDisplay = remember(job) { extractSafeJobPay(job) }
    val rawTitle = extractSafeObjectField(job, "title", fallback = "काम उपलब्ध है")
    val rawCategory = extractSafeObjectField(job, "category", fallback = "General")
    val rawLocation = extractSafeObjectField(job, "location", fallback = "Silwani")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onCompleteClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rawTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0D253F)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$rawCategory • $rawLocation",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Surface(
                color = Color(0xFFFFF8E1),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFFFE082))
            ) {
                Text(
                    text = payDisplay,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE65100),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
