package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.example.model.JobPost
import com.example.model.Worker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    workers: List<Worker>,
    jobs: List<JobPost>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    onPostJobClick: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSwitchRole: () -> Unit
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

    // View 0 = Home Screen, View 1 = Available Workers Search Screen
    var currentTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var activeCategoryFilter by remember { mutableStateOf(selectedCategory ?: "All") }

    val categories = listOf(
        "Mason / मिस्त्री",
        "Plumber / प्लंबर",
        "Electrician / इलेक्ट्रीशियन",
        "Painter / पेंटर",
        "Carpenter / बढ़ई",
        "Labour / मजदूर"
    )

    val filteredWorkers = remember(workers, searchQuery, activeCategoryFilter) {
        workers.filter { worker ->
            val matchesCategory = if (activeCategoryFilter.isBlank() || activeCategoryFilter == "All") {
                true
            } else {
                val cleanFilter = activeCategoryFilter.substringBefore("/").trim()
                worker.skill.contains(cleanFilter, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                worker.name.contains(searchQuery, ignoreCase = true) ||
                    worker.skill.contains(searchQuery, ignoreCase = true) ||
                    worker.location.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F7FB),
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomerBottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 }
                    )
                    CustomerBottomNavItem(
                        icon = Icons.Default.Search,
                        label = "Search",
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 }
                    )
                    CustomerBottomNavItem(
                        icon = Icons.Default.AddCircle,
                        label = "Post",
                        selected = false,
                        onClick = onPostJobClick
                    )
                    CustomerBottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        selected = false,
                        onClick = onNavigateToProfile
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
            if (currentTab == 0) {
                // ==================== VIEW 0: HOME SCREEN ====================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // 1. Header with W Logo, App Name & Location Card
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
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "W",
                                                color = Color.White,
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = appName,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0D47A1)
                                            )
                                            Text(
                                                text = appTagline,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF64748B),
                                                letterSpacing = 0.8.sp
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(
                                            onClick = onSwitchRole,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SwapHoriz,
                                                contentDescription = "Switch Role",
                                                tint = Color(0xFF1565C0),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Switch Role",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1565C0)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Location Bar Card
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF1F5F9),
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
                                                tint = Color(0xFF1565C0),
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
                                            text = "बदलें >",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1565C0)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Reference Hero Banner with Yellow Helmet & Wrench Worker Illustration
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF0D47A1),
                                                Color(0xFF1976D2),
                                                Color(0xFF42A5F5)
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
                                            color = Color(0xFFFFC107),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "TRUSTED CARIGARS",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0D47A1),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "कुशल कारीगर और मजदूर अब एक क्लिक पर!",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            lineHeight = 23.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "मिस्त्री • प्लंबर • इलेक्ट्रीशियन • पेंटर • मजदूर",
                                            fontSize = 12.sp,
                                            color = Color(0xFFE3F2FD)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { currentTab = 1 },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.White,
                                                    contentColor = Color(0xFF0D47A1)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "कारीगर खोजें",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = onPostJobClick,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "+ काम पोस्ट करें",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Worker Canvas Illustration (Yellow Helmet + Wrench)
                                    CustomerHeroWorkerIllustration(size = 96.dp)
                                }
                            }
                        }
                    }

                    // 3. 3x2 Popular Categories Grid
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
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "View All",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0),
                                    modifier = Modifier.clickable {
                                        activeCategoryFilter = "All"
                                        onCategorySelected("All")
                                        currentTab = 1
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Row 1 of 3x2 Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                categories.take(3).forEach { cat ->
                                    CustomerCategoryGridCard(
                                        categoryTitle = cat,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            val shortCat = cat.substringBefore("/").trim()
                                            activeCategoryFilter = shortCat
                                            onCategorySelected(shortCat)
                                            currentTab = 1
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Row 2 of 3x2 Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                categories.drop(3).take(3).forEach { cat ->
                                    CustomerCategoryGridCard(
                                        categoryTitle = cat,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            val shortCat = cat.substringBefore("/").trim()
                                            activeCategoryFilter = shortCat
                                            onCategorySelected(shortCat)
                                            currentTab = 1
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 4. Green Shield Verified Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
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
                                        text = "सीधे कॉल या WhatsApp पर बात करें • कोई बिचौलिया नहीं",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }

                    // 5. Recent Jobs Section
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Jobs (${jobs.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B)
                            )
                            TextButton(onClick = onPostJobClick) {
                                Text(
                                    text = "+ Post New Job",
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
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "अभी कोई जॉब पोस्ट नहीं है",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = onPostJobClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("पहला काम पोस्ट करें")
                                    }
                                }
                            }
                        }
                    } else {
                        items(jobs.take(5)) { job ->
                            CustomerRecentJobCard(job = job)
                        }
                    }
                }
            } else {
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
                                        onClick = { currentTab = 0 },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color(0xFF1E293B)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Available Workers (${filteredWorkers.size})",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }

                                // Location Pill
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFFE3F2FD),
                                    modifier = Modifier.clickable { showLocationDialog = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFF1565C0),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentLocation.take(14),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1565C0)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "कारीगर का नाम, काम या शहर खोजें...",
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

                            // Category Filter Chips
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
                                            selectedContainerColor = Color(0xFF1565C0),
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
                                        text = "इस कैटेगरी में अभी कोई कारीगर नहीं मिला",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF475569),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(filteredWorkers) { worker ->
                                CustomerWorkerCard(worker = worker, context = context)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = {
                Text(
                    text = "अपनी लोकेशन अपडेट करें",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = tempLocationInput,
                    onValueChange = { tempLocationInput = it },
                    label = { Text("शहर / गाँव / तहसील (जैसे: Silwani, Raisen)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempLocationInput.isNotBlank()) {
                            currentLocation = tempLocationInput.trim()
                            profilePrefs.edit().putString("user_location", currentLocation).apply()
                        }
                        showLocationDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CustomerBottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) Color(0xFF1565C0) else Color(0xFF64748B)
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
private fun CustomerCategoryGridCard(
    categoryTitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val parts = categoryTitle.split("/")
    val englishTitle = parts.firstOrNull()?.trim() ?: categoryTitle
    val hindiTitle = parts.getOrNull(1)?.trim() ?: ""

    Card(
        modifier = modifier
            .height(104.dp)
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
                CustomerCategoryCanvasIcon(categoryName = englishTitle, size = 26.dp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = englishTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
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
private fun CustomerCategoryCanvasIcon(
    categoryName: String,
    size: Dp = 26.dp
) {
    // FIXED: Using capital Modifier.size(size) and this.size.width / this.size.height
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val primary = Color(0xFF1565C0)
        val accent = Color(0xFFFFA000)

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
private fun CustomerHeroWorkerIllustration(size: Dp = 96.dp) {
    // FIXED: Using capital Modifier.size(size) and this.size.width / this.size.height
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = w * 0.48f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        drawRoundRect(
            color = Color(0xFFFFC107),
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
private fun CustomerWorkerCard(
    worker: Worker,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
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
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = worker.name.take(1).uppercase().ifBlank { "W" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1565C0)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = worker.name.ifBlank { "कुशल कारीगर" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
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
                        text = worker.skill.ifBlank { "General Worker" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = worker.location.ifBlank { "Silwani, Raisen" },
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFE082))
                ) {
                    Text(
                        text = if (worker.dailyWage.startsWith("₹")) worker.dailyWage else "₹${worker.dailyWage}/दिन",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val phone = worker.phone.ifBlank { "6265798340" }
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "कॉल करें",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        val rawPhone = worker.phone.ifBlank { "6265798340" }.filter { it.isDigit() }
                        val formattedPhone = if (rawPhone.length == 10) "91$rawPhone" else rawPhone
                        val msg = Uri.encode("नमस्ते ${worker.name}, मैंने Workora App पर आपकी प्रोफाइल (${worker.skill}) देखी। मुझे काम के लिए आपकी आवश्यकता है।")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$formattedPhone?text=$msg"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp उपलब्ध नहीं है", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32))
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerRecentJobCard(job: JobPost) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
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
                    text = job.title.ifBlank { "काम उपलब्ध है" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${job.category} • ${job.location}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Surface(
                color = Color(0xFFE3F2FD),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (job.wage.startsWith("₹")) job.wage else "₹${job.wage}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
