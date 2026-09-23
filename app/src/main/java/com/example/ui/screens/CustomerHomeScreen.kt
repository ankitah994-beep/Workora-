package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Job
import com.example.model.UserAccount
import com.example.model.Worker
import com.example.ui.components.WorkoraToast
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraNavyDark
import com.example.ui.theme.WorkoraNavySoft
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraOrangeSoft
import com.example.ui.theme.WorkoraSuccess
import com.example.ui.theme.WorkoraSurface
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted
import com.example.ui.theme.WorkoraWarning
import com.example.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * CustomerHomeScreen composable providing:
 * 1. Top Bar: Welcoming header ("Hello, User") with a location pin ("Sector 14, Gurugram") and profile icon.
 * 2. Search & Filter: Search bar ("Search mason, plumber, electrician...") & horizontal category chips.
 * 3. Worker List: Vertical list of Worker Cards with details, wage, "Call Worker" and "Hire Now" buttons.
 * 4. Floating Action Button: Bottom-right FAB "+ Post Work".
 * 5. Clean Architecture integrated with HomeViewModel and Firestore-ready Worker data structure.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    currentUser: UserAccount? = null,
    onOpenProfile: () -> Unit = {},
    onOpenPostWork: () -> Unit = {},
    onHireWorker: (Worker) -> Unit = {},
    onPostWorkSubmitted: (title: String, category: String, desc: String, rate: Int, loc: String) -> Unit = { _, _, _, _, _ -> },
    toastMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val searchQuery by homeViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by homeViewModel.selectedCategory.collectAsStateWithLifecycle()
    val workers by homeViewModel.availableWorkers.collectAsStateWithLifecycle()
    val selectedTab by homeViewModel.selectedTab.collectAsStateWithLifecycle()
    val postedJobs by homeViewModel.postedJobs.collectAsStateWithLifecycle()
    val isLoadingPostedJobs by homeViewModel.isLoadingPostedJobs.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser) {
        val uid = currentUser?.id?.takeIf { it > 0 }?.toString()
        homeViewModel.fetchCustomerJobs(uid)
    }

    val categories = remember { listOf("All", "Mason", "Electrician", "Plumber", "Carpenter") }

    // Dialog & BottomSheet state
    var showPostWorkSheet by remember { mutableStateOf(false) }
    var workerToHire by remember { mutableStateOf<Worker?>(null) }
    var workerToCall by remember { mutableStateOf<Worker?>(null) }
    var localToast by remember { mutableStateOf<String?>(null) }

    val postWorkSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WorkoraBgLight,
        topBar = {
            CustomerHomeTopBar(
                userName = currentUser?.fullName?.split(" ")?.firstOrNull() ?: "User",
                userLocation = currentUser?.location?.takeIf { it.isNotBlank() } ?: "Sector 14, Gurugram",
                onOpenProfile = onOpenProfile
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenPostWork,
                containerColor = WorkoraOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Post Work",
                        modifier = Modifier.size(22.dp)
                    )
                },
                text = {
                    Text(
                        text = "+ Post Work",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier
                    .testTag("fab_post_work")
                    .padding(bottom = 12.dp, end = 8.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Customer Section Tabs: "Find Workers" vs "My Posted Work"
            CustomerSectionTabs(
                selectedTab = selectedTab,
                onTabSelected = { homeViewModel.setSelectedTab(it) },
                postedJobsCount = postedJobs.size
            )

            if (selectedTab == 0) {
                // Search Bar
                CustomerSearchBar(
                    query = searchQuery,
                    onQueryChanged = { homeViewModel.setSearchQuery(it) }
                )

                // Category Filter Chips
                CategoryFilterChipRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { homeViewModel.setCategory(it) }
                )

                // Worker List Header Count
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "All") "Available Workers (${workers.size})" else "$selectedCategory Workers (${workers.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkoraNavyDark
                    )
                    if (searchQuery.isNotBlank() || selectedCategory != "All") {
                        Text(
                            text = "Clear filter",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = WorkoraOrange,
                            modifier = Modifier
                                .clickable {
                                    homeViewModel.setSearchQuery("")
                                    homeViewModel.setCategory("All")
                                }
                                .padding(4.dp)
                        )
                    }
                }

                // Workers List
                if (workers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(WorkoraNavySoft, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = WorkoraNavy,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No workers found",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraTextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try adjusting your search query or trade filter to find available workers.",
                                fontSize = 14.sp,
                                color = WorkoraTextMuted,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    homeViewModel.setSearchQuery("")
                                    homeViewModel.setCategory("All")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WorkoraNavy),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Reset Filters")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("worker_list"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(workers, key = { it.id }) { worker ->
                            WorkerCard(
                                worker = worker,
                                onCallClick = { workerToCall = worker },
                                onHireClick = {
                                    workerToHire = worker
                                    onHireWorker(worker)
                                }
                            )
                        }
                    }
                }
            } else {
                // My Posted Work Section
                MyPostedJobsSection(
                    postedJobs = postedJobs,
                    isLoading = isLoadingPostedJobs,
                    onOpenPostWork = onOpenPostWork,
                    onRefresh = {
                        val uid = currentUser?.id?.takeIf { it > 0 }?.toString()
                        homeViewModel.fetchCustomerJobs(uid)
                    }
                )
            }
        }
    }

    // Call Worker Dialog
    workerToCall?.let { worker ->
        AlertDialog(
            onDismissRequest = { workerToCall = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(WorkoraNavySoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = WorkoraNavy,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Contact ${worker.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WorkoraTextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Trade: ${worker.trade} • ${worker.experienceYears} yrs experience",
                        fontSize = 14.sp,
                        color = WorkoraTextMuted
                    )
                    Text(
                        text = "Phone: ${worker.phone}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkoraNavy
                    )
                    Text(
                        text = "Would you like to initiate a phone call now?",
                        fontSize = 13.sp,
                        color = WorkoraTextDark
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${worker.phone}")
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            localToast = "Calling ${worker.phone}"
                        }
                        workerToCall = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Call Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { workerToCall = null }) {
                    Text("Cancel", color = WorkoraTextMuted)
                }
            }
        )
    }

    // Hire Worker Confirmation Dialog
    workerToHire?.let { worker ->
        AlertDialog(
            onDismissRequest = { workerToHire = null },
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(WorkoraOrangeSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = WorkoraOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Hire ${worker.name}?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = WorkoraTextDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Trade: ${worker.trade} (${worker.experienceYears} years exp)",
                        fontSize = 14.sp,
                        color = WorkoraTextDark
                    )
                    Text(
                        text = "Daily Wage: ₹${worker.dailyWage}/day",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraOrange
                    )
                    Text(
                        text = "Location: ${worker.location} (${worker.distance})",
                        fontSize = 13.sp,
                        color = WorkoraTextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A hiring request notification will be sent to the worker immediately.",
                        fontSize = 12.sp,
                        color = WorkoraTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        localToast = "Hiring request sent to ${worker.name}!"
                        workerToHire = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm Hire", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { workerToHire = null }) {
                    Text("Cancel", color = WorkoraTextMuted)
                }
            }
        )
    }

    // Post Work Modal Bottom Sheet
    if (showPostWorkSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPostWorkSheet = false },
            sheetState = postWorkSheetState,
            containerColor = WorkoraSurface
        ) {
            PostWorkSheetContent(
                categories = categories.filter { it != "All" },
                onSubmit = { title, cat, desc, rate, loc ->
                    onPostWorkSubmitted(title, cat, desc, rate, loc)
                    localToast = "Work posted successfully: $title"
                    coroutineScope.launch {
                        postWorkSheetState.hide()
                        showPostWorkSheet = false
                    }
                },
                onCancel = {
                    coroutineScope.launch {
                        postWorkSheetState.hide()
                        showPostWorkSheet = false
                    }
                }
            )
        }
    }

    // In-screen toast display
    (toastMessage ?: localToast)?.let { msg ->
        WorkoraToast(
            message = msg
        )
    }
}

/**
 * Top Bar with welcoming header ("Hello, User"), location pin ("Sector 14, Gurugram"), and profile icon.
 */
@Composable
fun CustomerHomeTopBar(
    userName: String,
    userLocation: String,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = WorkoraNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Welcoming greeting and location
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hello, $userName 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.testTag("welcome_header_text")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Pin",
                        tint = WorkoraOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = userLocation,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.testTag("location_text")
                    )
                }
            }

            // Profile Icon
            IconButton(
                onClick = onOpenProfile,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .testTag("profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

/**
 * Search Bar with placeholder "Search mason, plumber, electrician...".
 */
@Composable
fun CustomerSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = {
                Text(
                    text = "Search mason, plumber, electrician...",
                    fontSize = 14.sp,
                    color = WorkoraTextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = WorkoraNavy,
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Search",
                            tint = WorkoraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = WorkoraSurface,
                unfocusedContainerColor = WorkoraSurface,
                focusedBorderColor = WorkoraNavy,
                unfocusedBorderColor = WorkoraBorder,
                cursorColor = WorkoraNavy
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input")
        )
    }
}

/**
 * Horizontal scrollable category chip row (All, Mason, Electrician, Plumber, Carpenter).
 */
@Composable
fun CategoryFilterChipRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.equals(selectedCategory, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else WorkoraTextDark
                    )
                },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                } else null,
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WorkoraNavy,
                    selectedLabelColor = Color.White,
                    containerColor = WorkoraSurface,
                    labelColor = WorkoraTextDark
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) WorkoraNavy else WorkoraBorder
                ),
                modifier = Modifier.testTag("category_chip_${category.lowercase()}")
            )
        }
    }
}

/**
 * Modern Material 3 Worker Card displaying:
 * - Worker's Name, Trade, and Experience (e.g., Rajesh Sharma - Mason • 8 yrs exp)
 * - Rating, Distance, and Availability status (e.g., "Available Today" in green)
 * - Daily Wage at top right (e.g., ₹800/day)
 * - Bottom row: "Call Worker" button with phone icon + prominent solid "Hire Now" button in primary color
 */
@Composable
fun WorkerCard(
    worker: Worker,
    onCallClick: () -> Unit,
    onHireClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("worker_card_${worker.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WorkoraSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, WorkoraBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Section: Info on Left, Wage on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Details: Name, Trade, Experience
                Column(modifier = Modifier.weight(1f)) {
                    // Trade & Name line: "Rajesh Sharma - Mason • 8 yrs exp"
                    Text(
                        text = "${worker.name} - ${worker.trade} • ${worker.experienceYears} yrs exp",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraTextDark,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Rating & Distance Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Rating Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .background(Color(0xFFFFFBEB), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = WorkoraWarning,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${worker.rating}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                            Text(
                                text = "(${worker.reviewsCount})",
                                fontSize = 11.sp,
                                color = WorkoraTextMuted
                            )
                        }

                        Text(text = "•", fontSize = 12.sp, color = WorkoraTextMuted)

                        // Distance
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = WorkoraNavy,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = worker.distance,
                                fontSize = 12.sp,
                                color = WorkoraTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Availability Status in Green
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(WorkoraSuccess, CircleShape)
                        )
                        Text(
                            text = if (worker.isAvailableToday) "Available Today" else "Busy Today",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (worker.isAvailableToday) WorkoraSuccess else WorkoraTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Top Right: Daily Wage (e.g. ₹800/day)
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "₹${worker.dailyWage}",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraOrange
                    )
                    Text(
                        text = "/day",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = WorkoraTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom row: "Call Worker" text button with phone icon + Solid "Hire Now" button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Call Worker" button with phone icon
                OutlinedButton(
                    onClick = onCallClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("call_worker_button_${worker.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, WorkoraNavy),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = WorkoraNavy
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call Worker",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Solid prominent "Hire Now" button in primary color (WorkoraOrange)
                Button(
                    onClick = onHireClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("hire_now_button_${worker.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WorkoraOrange,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "Hire",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hire Now",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Bottom Sheet content for posting work
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostWorkSheetContent(
    categories: List<String>,
    onSubmit: (title: String, category: String, desc: String, rate: Int, loc: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "Mason") }
    var description by remember { mutableStateOf("") }
    var dailyRateText by remember { mutableStateOf("700") }
    var location by remember { mutableStateOf("Sector 14, Gurugram") }
    var expandedDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Post New Work",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraNavyDark
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = WorkoraTextMuted)
            }
        }

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Work Title (e.g., Bathroom Tile Mason Needed)") },
            placeholder = { Text("Briefly describe the task") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("post_work_title_input"),
            singleLine = true
        )

        // Category Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = !expandedDropdown },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category / Trade") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            selectedCategory = cat
                            expandedDropdown = false
                        }
                    )
                }
            }
        }

        // Daily Rate & Location Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = dailyRateText,
                onValueChange = { if (it.all { char -> char.isDigit() }) dailyRateText = it },
                label = { Text("Daily Wage (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f),
                singleLine = true
            )
        }

        // Description Input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Work Details / Instructions") },
            placeholder = { Text("Hours, specific requirements, tools needed...") },
            minLines = 3,
            maxLines = 4,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Submit Button
        Button(
            onClick = {
                if (title.isNotBlank()) {
                    val rate = dailyRateText.toIntOrNull() ?: 700
                    onSubmit(title, selectedCategory, description, rate, location)
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_post_work_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WorkoraOrange,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Post Work Now",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Modern segmented tabs for switching between Find Workers and My Posted Work.
 */
@Composable
fun CustomerSectionTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    postedJobsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(Color.White, RoundedCornerShape(14.dp))
            .border(1.dp, WorkoraBorder, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Tab 0: Find Workers
        val isWorkersSelected = selectedTab == 0
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isWorkersSelected) WorkoraNavy else Color.Transparent)
                .clickable { onTabSelected(0) }
                .padding(vertical = 9.dp)
                .testTag("tab_find_workers"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (isWorkersSelected) Color.White else WorkoraTextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Find Workers",
                    fontSize = 13.sp,
                    fontWeight = if (isWorkersSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isWorkersSelected) Color.White else WorkoraTextDark
                )
            }
        }

        // Tab 1: My Posted Work
        val isPostedSelected = selectedTab == 1
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isPostedSelected) WorkoraNavy else Color.Transparent)
                .clickable { onTabSelected(1) }
                .padding(vertical = 9.dp)
                .testTag("tab_posted_work"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = null,
                    tint = if (isPostedSelected) Color.White else WorkoraTextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "My Posted Work",
                    fontSize = 13.sp,
                    fontWeight = if (isPostedSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isPostedSelected) Color.White else WorkoraTextDark
                )
                if (postedJobsCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isPostedSelected) WorkoraOrange else WorkoraNavySoft,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = postedJobsCount.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPostedSelected) Color.White else WorkoraNavy
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real-time list and empty state for jobs posted by the customer.
 */
@Composable
fun MyPostedJobsSection(
    postedJobs: List<Job>,
    isLoading: Boolean,
    onOpenPostWork: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isLoading && postedJobs.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = WorkoraNavy,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("posted_jobs_loading")
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Loading your posted jobs...",
                    fontSize = 14.sp,
                    color = WorkoraTextMuted
                )
            }
        }
    } else if (postedJobs.isEmpty()) {
        // Empty State with friendly message and icon
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(WorkoraNavySoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = "No Posted Jobs",
                        tint = WorkoraNavy,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "You haven't posted any jobs yet.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraTextDark,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Need work done? Post your job requirements with date, time, and location to connect directly with skilled local workers.",
                    fontSize = 14.sp,
                    color = WorkoraTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = onOpenPostWork,
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("btn_empty_post_job")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Post a Job",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    } else {
        // Vertical real-time list of Material 3 Cards
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("posted_jobs_list"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Job Postings (${postedJobs.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkoraNavyDark
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onRefresh() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = WorkoraNavy,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Refresh",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = WorkoraNavy
                        )
                    }
                }
            }

            items(postedJobs, key = { it.id }) { job ->
                PostedJobCard(job = job)
            }
        }
    }
}

/**
 * Material 3 Card displaying a customer's posted job:
 * Title, Category, Date, Location, Status badge ("Open", etc.).
 */
@Composable
fun PostedJobCard(
    job: Job,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("posted_job_card_${job.id}"),
        colors = CardDefaults.cardColors(containerColor = WorkoraSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, WorkoraBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Category Badge & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip / Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(WorkoraNavySoft, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Work,
                        contentDescription = null,
                        tint = WorkoraNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = job.category.ifBlank { "General Work" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavy
                    )
                }

                // Status Badge (e.g. "Open")
                val isOpen = job.status.equals("open", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .background(
                            if (isOpen) WorkoraSuccess.copy(alpha = 0.12f) else WorkoraOrangeSoft,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (isOpen) WorkoraSuccess else WorkoraOrange,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = job.status.replaceFirstChar { it.uppercase() }.ifBlank { "Open" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOpen) WorkoraSuccess else WorkoraOrange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Job Title
            Text(
                text = job.title.ifBlank { "${job.category} Job" },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraTextDark
            )

            if (job.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = job.description,
                    fontSize = 14.sp,
                    color = WorkoraTextMuted,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata row: Date & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (job.date.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Date",
                            tint = WorkoraTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.date,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = WorkoraTextDark
                        )
                    }
                }

                if (job.location.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = WorkoraOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = job.location,
                            fontSize = 13.sp,
                            color = WorkoraTextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
