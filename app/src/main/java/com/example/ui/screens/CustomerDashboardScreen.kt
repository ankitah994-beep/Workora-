package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountCircle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.JobPost
import com.example.model.UserAccount
import com.example.model.WorkerProfile
import com.example.ui.components.WorkoraHelmetLogo
import com.example.ui.components.WorkoraToast
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraNavyDark
import com.example.ui.theme.WorkoraNavySoft
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraOrangeSoft
import com.example.ui.theme.WorkoraSuccess
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted
import com.example.ui.theme.WorkoraWarning
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.JobViewModel
import com.example.viewmodel.ReviewViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    homeViewModel: HomeViewModel = viewModel(),
    jobViewModel: JobViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    currentUser: UserAccount?,
    searchQuery: String = "",
    onSearchQueryChanged: (String) -> Unit = {},
    workers: List<WorkerProfile> = emptyList(),
    jobs: List<JobPost>,
    selectedCategory: String = "All",
    onCategorySelected: (String) -> Unit = {},
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    onPostJob: (title: String, category: String, desc: String, rate: Int, loc: String, count: Int, urgency: String, dateTime: String) -> Unit,
    onHireWorker: (WorkerProfile) -> Unit,
    onCompleteJob: (Long) -> Unit = {},
    onSwitchRole: () -> Unit,
    onOpenProfile: () -> Unit,
    toastMessage: String?,
    modifier: Modifier = Modifier
) {
    // Observe workers list from HomeViewModel
    val vmWorkers by homeViewModel.availableWorkers.collectAsStateWithLifecycle()
    val displayWorkers = if (workers.isNotEmpty()) workers else vmWorkers

    // Observe job requests from JobViewModel
    val customerJobRequests by jobViewModel.customerJobs.collectAsStateWithLifecycle()
    val displayJobs = if (jobs.isNotEmpty()) {
        jobs
    } else {
        customerJobRequests.map { req ->
            JobPost(
                id = req.id.toLongOrNull() ?: 1L,
                title = req.title,
                category = req.workType,
                description = req.description,
                dailyRate = req.offeredWage,
                location = req.location,
                workersNeeded = req.workersNeeded,
                urgency = req.urgency,
                dateTime = req.dateTime,
                customerName = req.customerName,
                customerPhone = req.customerPhone,
                status = req.status.name,
                timestamp = req.timestamp
            )
        }
    }

    val categories = listOf("All", "Mason", "Electrician", "Plumber", "Carpenter", "Painter", "Construction Helper")
    var showPostJobSheet by remember { mutableStateOf(false) }
    var selectedWorkerForHire by remember { mutableStateOf<WorkerProfile?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Observe reviews from ReviewViewModel
    val workerReviews by reviewViewModel.workerReviews.collectAsStateWithLifecycle()
    var selectedJobForReview by remember { mutableStateOf<JobPost?>(null) }
    var reviewRating by remember { mutableFloatStateOf(5.0f) }
    var reviewComment by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            CustomerHeaderBar(
                currentUser = currentUser,
                onSwitchRole = onSwitchRole,
                onOpenProfile = onOpenProfile
            )

            // Hello, User & Location Banner
            CustomerWelcomeSection(
                userName = currentUser?.fullName ?: "User",
                location = currentUser?.location ?: "Sector 14, Gurugram",
                searchQuery = searchQuery,
                onSearchQueryChanged = onSearchQueryChanged
            )

            // Tabs: Explore Workers vs My Posted Jobs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = WorkoraNavy,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = WorkoraOrange,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { onTabSelected(0) },
                    text = {
                        Text(
                            text = "Find Workers (${displayWorkers.size})",
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { onTabSelected(1) },
                    text = {
                        Text(
                            text = "My Posted Work (${displayJobs.size})",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            // Category Chips Row (applicable to both or workers)
            if (activeTab == 0) {
                CategoryChipsRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            // Main Content Area
            if (activeTab == 0) {
                // Workers List
                if (displayWorkers.isEmpty()) {
                    EmptyListState(
                        title = "No workers in $selectedCategory",
                        subtitle = "Try selecting 'All' or another category"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(displayWorkers, key = { it.id }) { worker ->
                            WorkerCardItem(
                                worker = worker,
                                onHire = {
                                    selectedWorkerForHire = worker
                                    showPostJobSheet = true
                                    jobViewModel.createJobRequest(
                                        title = "Work Request for ${worker.name}",
                                        workType = worker.trade,
                                        description = "Direct booking request for ${worker.name} (${worker.trade}, ${worker.experienceYears} yrs experience).",
                                        offeredWage = worker.dailyWage,
                                        location = worker.location,
                                        workersNeeded = 1,
                                        urgency = "Today",
                                        dateTime = "Today, 9:00 AM",
                                        worker = worker,
                                        customerId = currentUser?.id?.toString() ?: "customer_1",
                                        customerName = currentUser?.fullName ?: "Ramesh Verma",
                                        customerPhone = currentUser?.mobileNumber ?: "+91 98765 43210"
                                    )
                                },
                                onCall = { onHireWorker(worker) }
                            )
                        }
                    }
                }
            } else {
                // Posted Jobs List
                if (displayJobs.isEmpty()) {
                    EmptyListState(
                        title = "No work posted yet",
                        subtitle = "Tap '+ Post Work' below to find skilled workers"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(displayJobs, key = { it.id }) { job ->
                            val existingReview = workerReviews.find { it.jobId == job.id.toString() }
                            val origJobId = job.id.toString()
                            PostedJobCardItem(
                                job = job,
                                reviewedRating = existingReview?.rating,
                                onComplete = {
                                    jobViewModel.completeJob(origJobId)
                                    onCompleteJob(job.id)
                                },
                                onReview = {
                                    selectedJobForReview = job
                                    reviewRating = 5.0f
                                    reviewComment = ""
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button to Post Work
        ExtendedFloatingActionButton(
            onClick = {
                selectedWorkerForHire = null
                showPostJobSheet = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("fab_post_job"),
            containerColor = WorkoraOrange,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            icon = { Icon(Icons.Default.Add, contentDescription = "Post Work") },
            text = { Text("Post Work", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
        )

        // Bottom Sheet for Posting Work
        if (showPostJobSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showPostJobSheet = false
                    selectedWorkerForHire = null
                },
                sheetState = sheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                PostJobSheetContent(
                    initialWorker = selectedWorkerForHire,
                    onDismiss = {
                        coroutineScope.launch {
                            sheetState.hide()
                            showPostJobSheet = false
                            selectedWorkerForHire = null
                        }
                    },
                    onSubmit = { title, cat, desc, rate, loc, count, urg, dt ->
                        jobViewModel.createJobRequest(
                            title = title,
                            workType = cat,
                            description = desc,
                            offeredWage = rate,
                            location = loc,
                            workersNeeded = count,
                            urgency = urg,
                            dateTime = dt,
                            worker = selectedWorkerForHire,
                            customerId = currentUser?.id?.toString() ?: "customer_1",
                            customerName = currentUser?.fullName ?: "Ramesh Verma",
                            customerPhone = currentUser?.mobileNumber ?: "+91 98765 43210"
                        )
                        onPostJob(title, cat, desc, rate, loc, count, urg, dt)
                        coroutineScope.launch {
                            sheetState.hide()
                            showPostJobSheet = false
                            selectedWorkerForHire = null
                        }
                    }
                )
            }
        }

        // Rating & Review Dialog
        if (selectedJobForReview != null) {
            val targetJob = selectedJobForReview!!
            AlertDialog(
                onDismissRequest = { selectedJobForReview = null },
                title = {
                    Text(
                        text = "Rate & Review Worker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = WorkoraTextDark
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "How was the work for '${targetJob.title}'?",
                            fontSize = 13.sp,
                            color = WorkoraTextMuted
                        )

                        // Star rating row (1 to 5)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("rating_stars_container"),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { starIndex ->
                                val isSelected = starIndex <= reviewRating
                                IconButton(
                                    onClick = { reviewRating = starIndex.toFloat() },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("rating_star_$starIndex")
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Star $starIndex",
                                        tint = if (isSelected) WorkoraWarning else WorkoraBorder,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = when (reviewRating.toInt()) {
                                5 -> "5.0 ★ Excellent Work!"
                                4 -> "4.0 ★ Very Good"
                                3 -> "3.0 ★ Good"
                                2 -> "2.0 ★ Fair"
                                else -> "1.0 ★ Needs Improvement"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraOrange,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_review_comment"),
                            label = { Text("Write your review") },
                            placeholder = { Text("e.g. Completed work on time, very polite and professional") },
                            minLines = 3,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val commentToSubmit = reviewComment.trim().ifEmpty {
                                "Completed work professionally with high quality."
                            }
                            reviewViewModel.submitReview(
                                jobId = targetJob.id.toString(),
                                customerId = currentUser?.id?.toString() ?: "customer_1",
                                workerId = "1",
                                rating = reviewRating,
                                comment = commentToSubmit
                            )
                            selectedJobForReview = null
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange),
                        modifier = Modifier.testTag("btn_submit_review")
                    ) {
                        Text("Submit Review", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { selectedJobForReview = null },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            )
        }

        // Workora Toast Notification
        WorkoraToast(
            message = toastMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
private fun CustomerHeaderBar(
    currentUser: UserAccount?,
    onSwitchRole: () -> Unit,
    onOpenProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WorkoraHelmetLogo(size = 36.dp, showHalo = false)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "WORKORA",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = WorkoraNavy,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(WorkoraOrange.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "HIRER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraOrange
                        )
                    }
                }
                Text(
                    text = "Find & Hire Skilled Labour",
                    fontSize = 12.sp,
                    color = WorkoraTextMuted
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Switch Role Button
            OutlinedButton(
                onClick = onSwitchRole,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, WorkoraBorder),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("btn_switch_role")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Switch",
                    tint = WorkoraNavy,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Role",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraNavy
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Profile Button
            IconButton(
                onClick = onOpenProfile,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("btn_customer_profile")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "My Profile",
                    tint = WorkoraNavy,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomerWelcomeSection(
    userName: String,
    location: String,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, $userName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraTextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = WorkoraOrange,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkoraTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_customer_search"),
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
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = WorkoraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = WorkoraBgLight,
                unfocusedContainerColor = WorkoraBgLight,
                focusedBorderColor = WorkoraNavy,
                unfocusedBorderColor = WorkoraBorder
            )
        )
    }
}

@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            val isSelected = cat == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(cat) },
                label = {
                    Text(
                        text = cat,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = WorkoraNavy,
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = WorkoraTextDark
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if (isSelected) WorkoraNavy else WorkoraBorder
                )
            )
        }
    }
}

@Composable
fun WorkerCardItem(
    worker: WorkerProfile,
    onHire: () -> Unit,
    onCall: () -> Unit = onHire
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("worker_card_${worker.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, WorkoraBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Badge
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(WorkoraOrange.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = worker.name,
                        tint = WorkoraOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = worker.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraTextDark
                        )
                        if (worker.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified",
                                tint = WorkoraSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${worker.trade} • ${worker.experienceYears} yrs exp",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = WorkoraNavy
                    )
                }

                // Daily Wage Tag
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${worker.dailyWage}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraOrange
                    )
                    Text(
                        text = "/ day",
                        fontSize = 11.sp,
                        color = WorkoraTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub info row: Rating + Distance + Availability
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = WorkoraWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${worker.rating} (${worker.reviewsCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkoraTextDark
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Distance",
                        tint = WorkoraTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = worker.distance,
                        fontSize = 12.sp,
                        color = WorkoraTextMuted
                    )
                }

                // Availability tag
                Box(
                    modifier = Modifier
                        .background(
                            if (worker.isAvailableToday) WorkoraSuccess.copy(alpha = 0.12f)
                            else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (worker.isAvailableToday) "Available Today" else "Busy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (worker.isAvailableToday) WorkoraSuccess else WorkoraTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = WorkoraNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Call Worker",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavy
                    )
                }

                Button(
                    onClick = onHire,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_hire_now_${worker.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WorkoraNavy,
                        contentColor = Color.White
                    )
                ) {
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

@Composable
fun PostedJobCardItem(
    job: JobPost,
    reviewedRating: Float? = null,
    onComplete: () -> Unit = {},
    onReview: () -> Unit = {}
) {
    val statusColor = when (job.status) {
        "ACCEPTED" -> Color(0xFF16A34A)
        "COMPLETED" -> Color(0xFF0284C7)
        "REJECTED" -> Color(0xFFDC2626)
        else -> Color(0xFFD97706) // PENDING / OPEN
    }
    val statusBg = when (job.status) {
        "ACCEPTED" -> Color(0xFFDCFCE7)
        "COMPLETED" -> Color(0xFFE0F2FE)
        "REJECTED" -> Color(0xFFFEE2E2)
        else -> Color(0xFFFEF3C7)
    }
    val statusLabel = when (job.status) {
        "ACCEPTED" -> "✓ ACCEPTED • Worker Assigned"
        "COMPLETED" -> "✓ COMPLETED"
        "REJECTED" -> "✕ REJECTED"
        else -> "● PENDING • Waiting for Worker"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("posted_job_${job.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, WorkoraBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Category, Status badge & Urgency
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(WorkoraNavySoft, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = job.category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavyDark
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusBg, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = job.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraTextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = job.description,
                fontSize = 13.sp,
                color = WorkoraTextMuted,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date/Time & Location
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Scheduled Time",
                        tint = WorkoraNavy,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = job.dateTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WorkoraNavy
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = WorkoraTextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = job.location,
                        fontSize = 12.sp,
                        color = WorkoraTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Wage & Urgency
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Wage: ₹${job.dailyRate} / day",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraOrange
                )

                Text(
                    text = "Workers: ${job.workersNeeded} • ${job.urgency}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WorkoraTextMuted
                )
            }

            // If ACCEPTED, show Action Button to Complete
            if (job.status == "ACCEPTED") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onComplete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WorkoraSuccess,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_complete_job_${job.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark Work as Completed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else if (job.status == "COMPLETED") {
                Spacer(modifier = Modifier.height(12.dp))
                if (reviewedRating != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = WorkoraSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Review Submitted",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraSuccess
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = WorkoraWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$reviewedRating ★",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraTextDark
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onReview,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WorkoraNavy,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_review_job_${job.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Submit Review",
                            tint = WorkoraWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rate & Review Worker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobSheetContent(
    initialWorker: WorkerProfile? = null,
    onDismiss: () -> Unit,
    onSubmit: (title: String, category: String, desc: String, rate: Int, loc: String, count: Int, urgency: String, dateTime: String) -> Unit
) {
    val categories = listOf("Mason", "Electrician", "Plumber", "Carpenter", "Painter", "Construction Helper")
    var title by remember(initialWorker) {
        mutableStateOf(initialWorker?.let { "Hire ${it.name} - ${it.trade}" } ?: "")
    }
    var selectedCategory by remember(initialWorker) {
        mutableStateOf(
            if (initialWorker != null && categories.contains(initialWorker.trade)) {
                initialWorker.trade
            } else {
                categories[0]
            }
        )
    }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }
    var description by remember(initialWorker) {
        mutableStateOf(
            initialWorker?.let { "Work request for ${it.name} (${it.trade}, ${it.experienceYears} yrs experience)." } ?: ""
        )
    }
    var dailyRate by remember(initialWorker) {
        mutableStateOf(initialWorker?.dailyWage?.toString() ?: "850")
    }
    var location by remember { mutableStateOf("Main Road, Sector 12") }
    var dateTime by remember { mutableStateOf("Today, 9:00 AM") }
    var workersNeeded by remember { mutableIntStateOf(1) }
    var urgency by remember { mutableStateOf("Today") }

    val dateTimePresets = listOf("Today, 9:00 AM", "Today, 2:00 PM", "Tomorrow, 8:30 AM", "Urgent / Now")
    val locationPresets = listOf("Sector 12", "Labour Chowk", "Model Town", "Defence Colony")
    val wagePresets = listOf("700", "850", "1000", "1200")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = if (initialWorker != null) "Hire ${initialWorker.name}" else "Create Work Request",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = WorkoraTextDark
        )
        Text(
            text = if (initialWorker != null) "Send direct work details to this skilled worker" else "Local workers will see your request and respond immediately",
            fontSize = 13.sp,
            color = WorkoraTextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Work Title (e.g. Wall Plastering)") },
            placeholder = { Text("What work do you need done?") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_job_title"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WorkoraNavy,
                focusedLabelColor = WorkoraNavy
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Selector Dropdown (Work Type)
        ExposedDropdownMenuBox(
            expanded = expandedCategoryDropdown,
            onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
        ) {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Work Type / Skill Required") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expandedCategoryDropdown,
                onDismissRequest = { expandedCategoryDropdown = false }
            ) {
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            selectedCategory = cat
                            expandedCategoryDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Date & Time
        Column {
            OutlinedTextField(
                value = dateTime,
                onValueChange = { dateTime = it },
                label = { Text("Work Date & Time") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = WorkoraNavy,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_job_datetime"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Date/Time chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dateTimePresets.forEach { preset ->
                    FilterChip(
                        selected = dateTime == preset,
                        onClick = { dateTime = preset },
                        label = { Text(preset, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WorkoraNavy,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Offered Wage (Daily Rate) & Location
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = dailyRate,
                    onValueChange = { dailyRate = it },
                    label = { Text("Offered Wage (₹/day)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_job_wage"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Work Location") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = WorkoraOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("input_job_location"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Wage quick chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quick Wage:", fontSize = 11.sp, color = WorkoraTextMuted)
                wagePresets.forEach { w ->
                    FilterChip(
                        selected = dailyRate == w,
                        onClick = { dailyRate = w },
                        label = { Text("₹$w", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WorkoraOrange,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Job Details & Requirements") },
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_job_desc"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val rateVal = dailyRate.toIntOrNull() ?: 850
                val validTitle = if (title.isBlank()) "$selectedCategory Needed" else title
                val validDesc = if (description.isBlank()) "Required skilled $selectedCategory for work at $location." else description
                val validLoc = if (location.isBlank()) "Sector 12, Main Road" else location
                val validDateTime = if (dateTime.isBlank()) "Today, 9:00 AM" else dateTime
                onSubmit(validTitle, selectedCategory, validDesc, rateVal, validLoc, workersNeeded, urgency, validDateTime)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_submit_job"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange, contentColor = Color.White)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (initialWorker != null) "Send Work Request" else "Post Work Request",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyListState(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Handyman,
            contentDescription = null,
            tint = WorkoraTextMuted.copy(alpha = 0.6f),
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = WorkoraTextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = WorkoraTextMuted
        )
    }
}
