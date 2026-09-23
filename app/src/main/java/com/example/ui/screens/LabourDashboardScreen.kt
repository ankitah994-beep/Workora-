package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import com.example.model.JobApplication
import com.example.model.JobPost
import com.example.model.JobStatus
import com.example.model.UserAccount
import com.example.ui.components.WorkoraHelmetLogo
import com.example.ui.components.WorkoraToast
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraNavyDark
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraOrangeDark
import com.example.ui.theme.WorkoraSuccess
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted
import com.example.ui.theme.WorkoraWarning
import com.example.viewmodel.JobViewModel

@Composable
fun LabourDashboardScreen(
    jobViewModel: JobViewModel = viewModel(),
    currentUser: UserAccount?,
    jobs: List<JobPost>,
    applications: List<JobApplication>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    isAvailable: Boolean,
    onToggleAvailability: () -> Unit,
    onApplyJob: (JobPost) -> Unit,
    onAcceptJob: (JobPost) -> Unit = {},
    onRejectJob: (JobPost) -> Unit = {},
    onCompleteJob: (Long) -> Unit = {},
    onSwitchRole: () -> Unit,
    onOpenProfile: () -> Unit,
    toastMessage: String?,
    modifier: Modifier = Modifier
) {
    val workerJobRequests by jobViewModel.workerJobs.collectAsStateWithLifecycle()

    LaunchedEffect(currentUser?.id) {
        currentUser?.id?.let {
            jobViewModel.fetchWorkerJobs(it.toString())
        }
    }

    val idLookup = remember(workerJobRequests) {
        workerJobRequests.associate { req ->
            (req.id.toLongOrNull() ?: req.hashCode().toLong()) to req.id
        }
    }

    val displayJobs = if (workerJobRequests.isNotEmpty()) {
        workerJobRequests
            .filter { req ->
                if (selectedCategory == "All") true
                else req.workType.equals(selectedCategory, ignoreCase = true)
            }
            .map { req ->
                JobPost(
                    id = req.id.toLongOrNull() ?: req.hashCode().toLong(),
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
    } else {
        jobs
    }

    val acceptedRequests = workerJobRequests.filter {
        it.status == JobStatus.ACCEPTED || it.status == JobStatus.COMPLETED
    }
    val displayApplications = if (applications.isNotEmpty()) {
        applications
    } else {
        acceptedRequests.map { req ->
            JobApplication(
                id = req.id.toLongOrNull() ?: req.hashCode().toLong(),
                jobId = req.id.toLongOrNull() ?: req.hashCode().toLong(),
                workerId = currentUser?.id ?: 1L,
                workerName = currentUser?.fullName ?: "Sunil Kumar",
                jobTitle = req.title,
                category = req.workType,
                dailyRate = req.offeredWage,
                location = req.location,
                dateTime = req.dateTime,
                status = if (req.status == JobStatus.COMPLETED) "COMPLETED" else "ACCEPTED",
                timestamp = req.timestamp
            )
        }
    }

    val categories = listOf("All", "Mason", "Electrician", "Plumber", "Carpenter", "Painter", "Construction Helper")

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
            LabourHeaderBar(
                currentUser = currentUser,
                onSwitchRole = onSwitchRole,
                onOpenProfile = onOpenProfile
            )

            // Hello, User & Location Banner
            LabourGreetingSection(
                userName = currentUser?.fullName ?: "User",
                location = currentUser?.location ?: "Delhi Chowk, Delhi"
            )

            // Worker Availability Card (Available / Busy switch)
            WorkerAvailabilityCard(
                workerName = currentUser?.fullName ?: "Sunil Kumar",
                workerLocation = currentUser?.location ?: "Delhi Chowk",
                isAvailable = isAvailable,
                onToggleAvailability = onToggleAvailability
            )

            // Basic Dashboard Cards
            LabourDashboardCardsRow(
                jobsCount = displayJobs.size,
                dailyWage = 850,
                rating = "4.9 ★"
            )

            // Tabs
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
                            text = "Work Requests (${displayJobs.size})",
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
                            text = "My Jobs (${displayApplications.size})",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            if (activeTab == 0) {
                CategoryChipsRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = onCategorySelected
                )

                if (displayJobs.isEmpty()) {
                    EmptyListState(
                        title = "No work requests in $selectedCategory",
                        subtitle = "Select 'All' to see all available local work requests"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(displayJobs, key = { it.id }) { job ->
                            val origJobId = idLookup[job.id] ?: job.id.toString()
                            LabourJobCardItem(
                                job = job,
                                onAccept = {
                                    jobViewModel.acceptJob(origJobId)
                                    onAcceptJob(job)
                                },
                                onReject = {
                                    jobViewModel.rejectJob(origJobId)
                                    onRejectJob(job)
                                },
                                onComplete = {
                                    jobViewModel.completeJob(origJobId)
                                    onCompleteJob(job.id)
                                }
                            )
                        }
                    }
                }
            } else {
                // My Jobs Tab
                if (displayApplications.isEmpty()) {
                    EmptyListState(
                        title = "No jobs accepted yet",
                        subtitle = "Check 'Work Requests' tab and Accept customer requests to see them here"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 40.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(displayApplications, key = { it.id }) { app ->
                            val origJobId = idLookup[app.jobId] ?: app.jobId.toString()
                            ApplicationCardItem(
                                application = app,
                                onComplete = {
                                    jobViewModel.completeJob(origJobId)
                                    onCompleteJob(app.jobId)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Toast
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
private fun LabourHeaderBar(
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
                            .background(WorkoraNavyDark, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "WORKER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = "Find Daily Work & Earn",
                    fontSize = 12.sp,
                    color = WorkoraTextMuted
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
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

            IconButton(
                onClick = onOpenProfile,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("btn_labour_profile")
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
private fun LabourGreetingSection(
    userName: String,
    location: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp)
    ) {
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

@Composable
private fun LabourDashboardCardsRow(
    jobsCount: Int,
    dailyWage: Int,
    rating: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DashboardStatCard(
            modifier = Modifier.weight(1f),
            title = "Jobs Available",
            value = "$jobsCount Nearby",
            icon = Icons.Default.Construction,
            iconTint = WorkoraOrange
        )
        DashboardStatCard(
            modifier = Modifier.weight(1f),
            title = "Daily Wage",
            value = "₹$dailyWage",
            icon = Icons.Default.CurrencyRupee,
            iconTint = WorkoraNavy
        )
        DashboardStatCard(
            modifier = Modifier.weight(1f),
            title = "My Rating",
            value = rating,
            icon = Icons.Default.Star,
            iconTint = Color(0xFFF59E0B)
        )
    }
}

@Composable
private fun DashboardStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, WorkoraBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = WorkoraTextMuted,
                maxLines = 1
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraTextDark,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WorkerAvailabilityCard(
    workerName: String,
    workerLocation: String,
    isAvailable: Boolean,
    onToggleAvailability: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) WorkoraNavy else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (isAvailable) null else BorderStroke(1.dp, WorkoraBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (isAvailable) WorkoraSuccess else WorkoraTextMuted,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAvailable) "Available for Work Today" else "Not Available",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvailable) Color.White else WorkoraTextDark
                    )
                }

                Switch(
                    checked = isAvailable,
                    onCheckedChange = { onToggleAvailability() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WorkoraOrange,
                        uncheckedThumbColor = WorkoraTextMuted,
                        uncheckedTrackColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.testTag("switch_worker_availability")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Profile info line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$workerName ($workerLocation)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAvailable) Color.White.copy(alpha = 0.9f) else WorkoraNavy
                    )
                    Text(
                        text = "Expected: ₹850 / day • 4.9 ★",
                        fontSize = 12.sp,
                        color = if (isAvailable) Color(0xFFBFDBFE) else WorkoraTextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (isAvailable) Color.White.copy(alpha = 0.15f) else WorkoraOrange.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isAvailable) "Visible to Hirers" else "Offline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAvailable) Color.White else WorkoraOrangeDark
                    )
                }
            }
        }
    }
}

@Composable
fun LabourJobCardItem(
    job: JobPost,
    onAccept: () -> Unit = {},
    onReject: () -> Unit = {},
    onComplete: () -> Unit = {}
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
        "ACCEPTED" -> "✓ ACCEPTED"
        "COMPLETED" -> "✓ COMPLETED"
        "REJECTED" -> "✕ REJECTED"
        else -> "● PENDING REQUEST"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("job_card_${job.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, WorkoraBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Category & Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(WorkoraNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = job.category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavy
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

            // Scheduled Date/Time & Location
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

            // Wage and Employer details bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                    fontWeight = FontWeight.SemiBold,
                    color = WorkoraTextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons based on status
            when (job.status) {
                "ACCEPTED" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "✓ In Progress • Added to My Jobs",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A)
                        )

                        Button(
                            onClick = onComplete,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("btn_complete_job_${job.id}")
                        ) {
                            Text("Mark Completed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "COMPLETED" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0F2FE), shape = RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ Work Completed Successfully",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
                "REJECTED" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEE2E2), shape = RoundedCornerShape(10.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕ Request Declined",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
                else -> {
                    // PENDING / OPEN: Worker can Accept or Reject
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_reject_job_${job.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = onAccept,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WorkoraSuccess,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("btn_accept_job_${job.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept Request", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationCardItem(
    application: JobApplication,
    onComplete: () -> Unit = {}
) {
    val statusColor = when (application.status) {
        "ACCEPTED" -> Color(0xFF16A34A)
        "COMPLETED" -> Color(0xFF0284C7)
        "REJECTED" -> Color(0xFFDC2626)
        else -> Color(0xFFD97706) // PENDING / APPLIED
    }
    val statusBg = when (application.status) {
        "ACCEPTED" -> Color(0xFFDCFCE7)
        "COMPLETED" -> Color(0xFFE0F2FE)
        "REJECTED" -> Color(0xFFFEE2E2)
        else -> Color(0xFFFEF3C7)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_card_${application.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, WorkoraBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .background(WorkoraNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = application.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavy
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusBg, shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (application.status) {
                            "ACCEPTED" -> "✓ ACCEPTED"
                            "COMPLETED" -> "✓ COMPLETED"
                            "REJECTED" -> "✕ REJECTED"
                            else -> "● PENDING"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = application.jobTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraTextDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = WorkoraNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = application.dateTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = WorkoraNavy
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = WorkoraTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = application.location,
                        fontSize = 12.sp,
                        color = WorkoraTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Daily Rate: ₹${application.dailyRate} / day",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraOrange
                )

                if (application.status == "ACCEPTED") {
                    Button(
                        onClick = onComplete,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Mark Done", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
