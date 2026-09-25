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
import com.example.model.JobApplication
import com.example.model.JobPost
import java.util.Locale

private fun extractSafeField(obj: Any, vararg fieldNames: String, fallback: String = ""): String {
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

private fun extractSafeJobRate(job: JobPost): String {
    val raw = extractSafeField(
        job,
        "dailyRate",
        "dailyWage",
        "budget",
        "wage",
        "pay",
        "salary",
        "amount",
        "rate",
        fallback = "600"
    )
    return when {
        raw.isEmpty() || raw == "0" -> "₹600/दिन"
        raw.startsWith("₹") -> raw
        else -> "₹$raw/दिन"
    }
}

@SuppressLint("MissingPermission")
private fun fetchRealGpsAddress(
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
    toastMessage: String? = null,
    onNavigateToProfile: () -> Unit = {}
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
            fetchRealGpsAddress(
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
            fetchRealGpsAddress(
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

    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    // 0 = Home View, 1 = Find Work View, 2 = Applied Jobs View
    var localTab by remember(activeTab) { mutableStateOf(if (activeTab in 0..2) activeTab else 0) }
    var searchQuery by remember { mutableStateOf("") }
    var activeCategoryFilter by remember(selectedCategory) { mutableStateOf(selectedCategory ?: "All") }

    val categories = listOf(
        "Mason / मिस्त्री",
        "Plumber / प्लंबर",
        "Electrician / इलेक्ट्रीशियन",
        "Painter / पेंटर",
        "Carpenter / बढ़ई",
        "Labour / मजदूर"
    )

    val filteredJobs = remember(jobs, searchQuery, activeCategoryFilter) {
        jobs.filter { job ->
            val jobCat = extractSafeField(job, "category", fallback = "")
            val jobTitle = extractSafeField(job, "title", fallback = "")
            val jobLoc = extractSafeField(job, "location", fallback = "")
            val jobDesc = extractSafeField(job, "description", fallback = "")

            val matchesCategory = if (activeCategoryFilter.isBlank() || activeCategoryFilter == "All") {
                true
            } else {
                val cleanFilter = activeCategoryFilter.substringBefore("/").trim()
                jobCat.contains(cleanFilter, ignoreCase = true) ||
                    jobTitle.contains(cleanFilter, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                jobTitle.contains(searchQuery, ignoreCase = true) ||
                    jobCat.contains(searchQuery, ignoreCase = true) ||
                    jobLoc.contains(searchQuery, ignoreCase = true) ||
                    jobDesc.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    val appliedJobIds = remember(applications) {
        applications.mapNotNull { app ->
            extractSafeField(app, "jobId", "id", fallback = "").takeIf { it.isNotEmpty() }
        }.toSet()
    }

    val handleProfileClick: () -> Unit = {
        onOpenProfile()
        onNavigateToProfile()
    }

    Scaffold(
        containerColor = Color(0xFFF4F7FB),
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 14.dp,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabourBottomNavButton(
                        icon = Icons.Default.Home,
                        label = "Home",
                        selected = localTab == 0,
                        onClick = {
                            localTab = 0
                            onTabSelected(0)
                        }
                    )
                    LabourBottomNavButton(
                        icon = Icons.Default.Search,
                        label = "Find Work",
                        selected = localTab == 1,
                        onClick = {
                            localTab = 1
                            onTabSelected(1)
                        }
                    )
                    LabourBottomNavButton(
                        icon = Icons.Default.CheckCircle,
                        label = "Applied (${applications.size})",
                        selected = localTab == 2,
                        onClick = {
                            localTab = 2
                            onTabSelected(2)
                        }
                    )
                    LabourBottomNavButton(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        selected = false,
                        onClick = handleProfileClick
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
                // ==================== VIEW 0: HOME SCREEN ====================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // 1. Top Branding & Duty Header
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
                                        LabourWorkoraLogoCanvas(size = 44.dp)
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
                                        // Online / Offline Duty Toggle
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isAvailable) Color(0xFFA5D6A7) else Color(0xFFEF9A9A)
                                            ),
                                            modifier = Modifier.clickable {
                                                onToggleAvailability()
                                                profilePrefs.edit()
                                                    .putBoolean("worker_available", !isAvailable)
                                                    .apply()
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isAvailable) "Online" else "Offline",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFFEFF6FF),
                                            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                            modifier = Modifier.clickable { onSwitchRole() }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                                                    text = "Switch",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1D4ED8)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Location Card
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

                    // 2. Hero Banner (Navy & Orange Theme with Worker Helmet & Wrench Illustration)
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
                                                text = "WORKER DASHBOARD",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "अपने पास के ताज़ा काम (Jobs) तुरंत पाएं!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            lineHeight = 23.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "सीधे मालिक से बात करें • 0% कमीशन",
                                            fontSize = 12.sp,
                                            color = Color(0xFFE3F2FD)
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
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
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = "काम खोजें (Find Work)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    LabourHeroWorkerIllustration(size = 98.dp)
                                }
                            }
                        }
                    }

                    // 3. Popular Categories 3x2 Grid
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
                                    text = "Popular Categories (काम चुनें)",
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
                                    LabourCategoryGridCard(
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
                                    LabourCategoryGridCard(
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

                    // 4. Green Shield Verified Trust Card
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
                                        text = "100% Verified Local Jobs",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = "सीधे ग्राहक को कॉल या WhatsApp करें • पूरी दिहाड़ी पाएं",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Recent Available Jobs Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Available Jobs (${jobs.size})",
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

                    if (jobs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "अभी कोई नया काम पोस्ट नहीं हुआ है",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    Text(
                                        text = "नए काम देखने के लिए थोड़ा इंतज़ार करें",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(jobs.take(10)) { job ->
                            val jobIdStr = extractSafeField(job, "id", fallback = "")
                            val jobIdLong = jobIdStr.toLongOrNull() ?: 0L
                            val isApplied = jobIdStr.isNotEmpty() && appliedJobIds.contains(jobIdStr)
                            LabourJobCard(
                                job = job,
                                isApplied = isApplied,
                                onApplyClick = { onApplyJob(job) },
                                onAcceptClick = { onAcceptJob(job) },
                                onRejectClick = { onRejectJob(job) },
                                onCompleteClick = {
                                    if (jobIdLong != 0L) onCompleteJob(jobIdLong)
                                },
                                context = context
                            )
                        }
                    }
                }
            } else if (localTab == 1) {
                // ==================== VIEW 1: FIND WORK SEARCH SCREEN ====================
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
                                        text = "Available Jobs (${filteredJobs.size})",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0D253F)
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

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "काम, शहर या मिस्त्री खोजें (जैसे: Silwani, Mason)...",
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
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
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
                        if (filteredJobs.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SearchOff,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "इस कैटेगरी में अभी कोई काम नहीं मिला",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        } else {
                            items(filteredJobs) { job ->
                                val jobIdStr = extractSafeField(job, "id", fallback = "")
                                val jobIdLong = jobIdStr.toLongOrNull() ?: 0L
                                val isApplied = jobIdStr.isNotEmpty() && appliedJobIds.contains(jobIdStr)
                                LabourJobCard(
                                    job = job,
                                    isApplied = isApplied,
                                    onApplyClick = { onApplyJob(job) },
                                    onAcceptClick = { onAcceptJob(job) },
                                    onRejectClick = { onRejectJob(job) },
                                    onCompleteClick = {
                                        if (jobIdLong != 0L) onCompleteJob(jobIdLong)
                                    },
                                    context = context
                                )
                            }
                        }
                    }
                }
            } else {
                // ==================== VIEW 2: APPLIED JOBS ====================
                Column(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                text = "मेरे आवेदन (Applied Jobs - ${applications.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0D253F)
                            )
                        }
                    }

                    val appliedJobPosts = remember(jobs, appliedJobIds) {
                        jobs.filter { job ->
                            val jId = extractSafeField(job, "id", fallback = "")
                            jId.isNotEmpty() && appliedJobIds.contains(jId)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (appliedJobPosts.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(36.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentLate,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(52.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "आपने अभी तक किसी काम के लिए Apply नहीं किया है",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(appliedJobPosts) { job ->
                                val jobIdStr = extractSafeField(job, "id", fallback = "")
                                val jobIdLong = jobIdStr.toLongOrNull() ?: 0L
                                LabourJobCard(
                                    job = job,
                                    isApplied = true,
                                    onApplyClick = {},
                                    onAcceptClick = { onAcceptJob(job) },
                                    onRejectClick = { onRejectJob(job) },
                                    onCompleteClick = {
                                        if (jobIdLong != 0L) onCompleteJob(jobIdLong)
                                    },
                                    context = context
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Real GPS + Quick City Chips Location Dialog
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
                    // Real Live GPS Detection Button
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
}

@Composable
private fun LabourWorkoraLogoCanvas(size: Dp = 44.dp) {
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
private fun LabourBottomNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) Color(0xFFFF6F00) else Color(0xFF64748B)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            color = tint
        )
    }
}

@Composable
private fun LabourCategoryGridCard(
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
                LabourCategoryCanvasIcon(categoryName = englishTitle, size = 26.dp)
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
private fun LabourCategoryCanvasIcon(
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
private fun LabourHeroWorkerIllustration(size: Dp = 98.dp) {
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
private fun LabourJobCard(
    job: JobPost,
    isApplied: Boolean,
    onApplyClick: () -> Unit,
    onAcceptClick: () -> Unit = {},
    onRejectClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
    context: Context
) {
    val jobCategory = remember(job) { extractSafeField(job, "category", fallback = "General Work") }
    val jobTitle = remember(job) { extractSafeField(job, "title", fallback = "दिहाड़ी / कारीगर का काम") }
    val jobDesc = remember(job) { extractSafeField(job, "description", fallback = "") }
    val jobLoc = remember(job) { extractSafeField(job, "location", fallback = "Silwani, Raisen (MP)") }
    val employerName = remember(job) { extractSafeField(job, "customerName", "employerName", "postedBy", fallback = "") }
    val contactPhone = remember(job) { extractSafeField(job, "customerPhone", "contactPhone", "phone", fallback = "6265798340") }
    val wageDisplay = remember(job) { extractSafeJobRate(job) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onCompleteClick() },
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = jobCategory,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isApplied) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "✓ Applied",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = jobTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0D253F)
                    )
                }

                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFE082))
                ) {
                    Text(
                        text = wageDisplay,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (jobDesc.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = jobDesc,
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = jobLoc,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
                if (employerName.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = employerName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onApplyClick()
                        onAcceptClick()
                    },
                    enabled = !isApplied,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0D253F),
                        disabledContainerColor = Color(0xFFCBD5E1)
                    )
                ) {
                    Text(
                        text = if (isApplied) "Applied ✓" else "Apply करें",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contactPhone"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF1565C0))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(18.dp)
                    )
                }

                OutlinedButton(
                    onClick = {
                        val rawPhone = contactPhone.filter { it.isDigit() }
                        val formattedPhone = if (rawPhone.length == 10) "91$rawPhone" else rawPhone
                        val msg = Uri.encode("नमस्ते, मैंने Workora App पर आपका '$jobTitle' ($jobLoc) का काम देखा। मैं यह काम करने के लिए उपलब्ध हूँ।")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedPhone?text=$msg"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp उपलब्ध नहीं है", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32))
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
