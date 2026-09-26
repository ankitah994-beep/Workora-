package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private val LabourNavyPrimary = Color(0xFF083D91)
private val LabourDarkNavyBtn = Color(0xFF0B2345)
private val LabourOrangeAccent = Color(0xFFFF8C00)
private val LabourBgLight = Color(0xFFF8FAFC)
private val LabourWhite = Color(0xFFFFFFFF)
private val LabourMainText = Color(0xFF0B2345)
private val LabourSecondaryText = Color(0xFF687280)
private val LabourBorder = Color(0xFFE5EAF0)
private val LabourSuccessGreen = Color(0xFF16A34A)
private val LabourRateBadgeBg = Color(0xFFFEF9C3)
private val LabourRateBadgeBorder = Color(0xFFFDE047)
private val LabourRateTextOrange = Color(0xFFD97706)

private const val LABOUR_FB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

internal data class LabourWorkRequestItem(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val customerName: String,
    val customerPhone: String,
    val location: String,
    val stateName: String,
    val dailyRate: Int,
    val workersNeeded: Int,
    val date: String,
    val status: String,
    val urgency: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <JobT, CatT, TabT> LabourDashboardScreen(
    currentUser: User? = null,
    jobs: List<JobT> = emptyList(),
    applications: List<*> = emptyList<Any?>(),
    selectedCategory: CatT,
    onCategorySelected: (CatT) -> Unit = {},
    activeTab: TabT,
    onTabSelected: (TabT) -> Unit = {},
    isAvailable: Boolean = true,
    onToggleAvailability: () -> Unit = {},
    onApplyJob: (JobT) -> Unit = {},
    onAcceptJob: (JobT) -> Unit = {},
    onRejectJob: (JobT) -> Unit = {},
    onCompleteJob: (Long) -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    toastMessage: String? = null,
    onOpenChat: () -> Unit = {}
) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val chatPrefs = remember { context.getSharedPreferences("workora_active_chat", Context.MODE_PRIVATE) }

    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    val workerName = profilePrefs.getString("user_name", "")?.ifBlank {
        authPrefs.getString("last_logged_in_email", "Worker")?.substringBefore("@") ?: "Worker"
    } ?: "Worker"
    val workerSkill = profilePrefs.getString("user_skill", "")?.ifBlank { "Skilled Worker" } ?: "Skilled Worker"
    val workerArea = profilePrefs.getString("user_location", "")?.ifBlank { "Local Area" } ?: "Local Area"
    val workerExp = profilePrefs.getString("user_experience", "")?.ifBlank { "Experienced" } ?: "Experienced"
    val workerRate = profilePrefs.getString("user_rate", "")?.ifBlank { "600" } ?: "600"

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedCustomerRequestForProfile by remember { mutableStateOf<LabourWorkRequestItem?>(null) }

    val defaultCustomerRequests = remember {
        listOf(
            LabourWorkRequestItem(
                id = "req_1",
                title = "Fix home wiring & switchboards",
                category = "Electrician",
                description = "Need an experienced electrician to fix house wiring and install 4 new modular switchboards.",
                customerName = "Amit Sharma",
                customerPhone = "+91 9876543210",
                location = "Main Market, Silwani",
                stateName = "Madhya Pradesh",
                dailyRate = 800,
                workersNeeded = 1,
                date = "Today",
                status = "PENDING",
                urgency = "Urgent"
            ),
            LabourWorkRequestItem(
                id = "req_2",
                title = "House Plaster & Brick Work",
                category = "Mason",
                description = "Need skilled mason for boundary wall construction and outer plastering work for 4 days.",
                customerName = "Rajesh Patel",
                customerPhone = "+91 9123456780",
                location = "Civil Lines, Raisen",
                stateName = "Madhya Pradesh",
                dailyRate = 850,
                workersNeeded = 2,
                date = "Today",
                status = "PENDING",
                urgency = "Normal"
            ),
            LabourWorkRequestItem(
                id = "req_3",
                title = "Bathroom Pipeline & Tank Fitting",
                category = "Plumber",
                description = "Overhead water tank installation and bathroom tap fitting work.",
                customerName = "Vikash Singh",
                customerPhone = "+91 9425012345",
                location = "Bhopal Road, Silwani",
                stateName = "Madhya Pradesh",
                dailyRate = 850,
                workersNeeded = 1,
                date = "26 Sep 2026",
                status = "PENDING",
                urgency = "Normal"
            ),
            LabourWorkRequestItem(
                id = "req_4",
                title = "2 BHK Full Interior Wall Painting",
                category = "Painter",
                description = "Wall putty and two coats of plastic emulsion paint required for 2 BHK house.",
                customerName = "Pooja Verma",
                customerPhone = "+91 7654321098",
                location = "New Colony, Raisen",
                stateName = "Madhya Pradesh",
                dailyRate = 750,
                workersNeeded = 2,
                date = "26 Sep 2026",
                status = "PENDING",
                urgency = "Normal"
            )
        )
    }

    val workRequestsList = remember {
        mutableStateListOf<LabourWorkRequestItem>().apply { addAll(defaultCustomerRequests) }
    }

    LaunchedEffect(Unit) {
        fetchLiveCustomerRequestsFromFirebase { cloudRequests ->
            val merged = mutableListOf<LabourWorkRequestItem>()
            merged.addAll(cloudRequests)
            defaultCustomerRequests.forEach { def ->
                if (merged.none { it.id == def.id || it.title.equals(def.title, true) }) {
                    merged.add(def)
                }
            }
            workRequestsList.clear()
            workRequestsList.addAll(merged)
        }
    }

    val filteredRequests = remember(workRequestsList.size, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") workRequestsList
        else workRequestsList.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    val openDirectLiveChatWithCustomer: (LabourWorkRequestItem) -> Unit = { req ->
        chatPrefs.edit()
            .putString("chat_partner_id", req.id)
            .putString("chat_partner_name", req.customerName)
            .putString("chat_partner_role", "Customer • ${req.title}")
            .putString("chat_partner_phone", req.customerPhone)
            .putString("chat_partner_area", req.location)
            .apply()
        onOpenChat()
    }

    if (selectedCustomerRequestForProfile != null) {
        val req = selectedCustomerRequestForProfile!!
        CustomerAndJobDetailProfileScreen(
            request = req,
            onBack = { selectedCustomerRequestForProfile = null },
            onCallCustomer = {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${req.customerPhone}"))
                context.startActivity(dialIntent)
            },
            onChatWithCustomer = {
                selectedCustomerRequestForProfile = null
                openDirectLiveChatWithCustomer(req)
            },
            onAcceptWorkRequest = {
                val idx = workRequestsList.indexOfFirst { it.id == req.id }
                if (idx >= 0) {
                    val updated = req.copy(status = "ACCEPTED")
                    workRequestsList[idx] = updated
                    selectedCustomerRequestForProfile = updated
                    updateJobStatusInFirebase(req.id, "ACCEPTED", workerName)
                    Toast.makeText(context, "Work Request Accepted! ✓", Toast.LENGTH_SHORT).show()
                }
            }
        )
        return
    }

    Scaffold(
        containerColor = LabourBgLight,
        bottomBar = {
            Surface(
                color = LabourWhite,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabourBottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        isSelected = true,
                        onClick = { selectedCategoryFilter = "All" }
                    )
                    LabourBottomNavItem(
                        icon = Icons.Outlined.WorkOutline,
                        label = "My Jobs",
                        isSelected = false,
                        onClick = { selectedCategoryFilter = "All" }
                    )
                    LabourBottomNavItem(
                        icon = Icons.Outlined.Chat,
                        label = "Chat",
                        isSelected = false,
                        onClick = onOpenChat
                    )
                    LabourBottomNavItem(
                        icon = Icons.Outlined.Person,
                        label = "Profile",
                        isSelected = false,
                        onClick = onOpenProfile
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LabourNavyPrimary)
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Workora",
                            color = LabourWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Worker Dashboard",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = LabourOrangeAccent,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { onSwitchRole() }
                        ) {
                            Text(
                                text = "Customer Mode",
                                color = LabourWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = LabourWhite),
                    border = BorderStroke(1.dp, LabourBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CustomerHardHatAvatar(size = 68.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = workerName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LabourMainText
                                )
                                Text(
                                    text = workerSkill,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LabourNavyPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 $workerArea • Exp: $workerExp",
                                    fontSize = 12.sp,
                                    color = LabourSecondaryText
                                )
                                Text(
                                    text = "Daily Rate: ₹$workerRate/day",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LabourOrangeAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = if (isAvailable) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isAvailable) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAvailable) "Available for work" else "Currently Busy",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAvailable) LabourSuccessGreen else Color(0xFFDC2626)
                                )
                                Switch(
                                    checked = isAvailable,
                                    onCheckedChange = { onToggleAvailability() },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = LabourWhite,
                                        checkedTrackColor = LabourSuccessGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                val cats = listOf("All", "Mason", "Electrician", "Plumber", "Painter", "Carpenter", "General Labour")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cats.forEach { c ->
                        val selected = selectedCategoryFilter.equals(c, ignoreCase = true)
                        Surface(
                            color = if (selected) LabourNavyPrimary else LabourWhite,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, if (selected) LabourNavyPrimary else LabourBorder),
                            modifier = Modifier.clickable { selectedCategoryFilter = c }
                        ) {
                            Text(
                                text = c,
                                color = if (selected) LabourWhite else LabourMainText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customer Work Requests (${filteredRequests.size})",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LabourMainText
                    )
                    Text(
                        text = "See All >",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LabourNavyPrimary,
                        modifier = Modifier.clickable { selectedCategoryFilter = "All" }
                    )
                }
            }

            items(filteredRequests, key = { it.id }) { req ->
                CustomerWorkRequestCard(
                    request = req,
                    onCardClick = {
                        selectedCustomerRequestForProfile = req
                    },
                    onCallCustomerClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${req.customerPhone}"))
                        context.startActivity(dialIntent)
                    },
                    onMessageIconClick = {
                        openDirectLiveChatWithCustomer(req)
                    }
                )
            }
        }
    }
}

@Composable
internal fun CustomerWorkRequestCard(
    request: LabourWorkRequestItem,
    onCardClick: () -> Unit,
    onCallCustomerClick: () -> Unit,
    onMessageIconClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LabourWhite),
        border = BorderStroke(1.dp, LabourBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDBEAFE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = request.customerName.take(1).uppercase(Locale.US),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LabourNavyPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.customerName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LabourMainText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${request.title} (${request.category})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LabourNavyPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LabourSecondaryText,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${request.location} • ${request.date}",
                            fontSize = 12.sp,
                            color = LabourSecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = LabourRateBadgeBg,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LabourRateBadgeBorder)
                ) {
                    Text(
                        text = "₹${request.dailyRate}/दिन",
                        color = LabourRateTextOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCallCustomerClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LabourDarkNavyBtn,
                        contentColor = LabourWhite
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Now",
                        tint = LabourWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Call Now",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = LabourWhite
                    )
                }

                Surface(
                    color = LabourNavyPrimary,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onMessageIconClick() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Chat,
                            contentDescription = "Live Chat with Customer",
                            tint = LabourWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CustomerAndJobDetailProfileScreen(
    request: LabourWorkRequestItem,
    onBack: () -> Unit,
    onCallCustomer: () -> Unit,
    onChatWithCustomer: () -> Unit,
    onAcceptWorkRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LabourBgLight)
    ) {
        Surface(
            color = LabourNavyPrimary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = LabourWhite)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Customer & Work Profile",
                    color = LabourWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = LabourWhite),
                border = BorderStroke(1.dp, LabourBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = request.customerName.take(1).uppercase(Locale.US),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LabourNavyPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = request.customerName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LabourMainText
                            )
                            Text(
                                text = "Verified Customer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = LabourSuccessGreen
                            )
                            Text(
                                text = "📍 ${request.location}",
                                fontSize = 13.sp,
                                color = LabourSecondaryText
                            )
                        }
                    }

                    HorizontalDivider(color = LabourBorder)

                    Text("Work Title: ${request.title}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LabourMainText)
                    Text("Category Needed: ${request.category}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = LabourNavyPrimary)
                    Text("Offered Rate: ₹${request.dailyRate}/दिन • Workers Needed: ${request.workersNeeded}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LabourOrangeAccent)
                    Text("Customer Phone: ${request.customerPhone}", fontSize = 14.sp, color = LabourMainText)
                    Text("Work Description:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LabourMainText)
                    Text(request.description, fontSize = 14.sp, color = LabourSecondaryText)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCallCustomer,
                    colors = ButtonDefaults.buttonColors(containerColor = LabourDarkNavyBtn),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = LabourWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Now", color = LabourWhite, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onChatWithCustomer,
                    colors = ButtonDefaults.buttonColors(containerColor = LabourNavyPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Outlined.Chat, contentDescription = null, tint = LabourWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Live Chat", color = LabourWhite, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onAcceptWorkRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (request.status == "ACCEPTED") LabourSuccessGreen else LabourOrangeAccent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (request.status == "ACCEPTED") "Work Request Accepted ✓" else "Accept Work Request",
                    color = LabourWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
internal fun CustomerHardHatAvatar(size: Dp = 68.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(color = Color(0xFFE2E8F0), radius = w * 0.5f, center = Offset(w * 0.5f, h * 0.5f))
        drawArc(
            color = LabourSuccessGreen,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.16f, h * 0.62f),
            size = Size(w * 0.68f, h * 0.48f)
        )
        drawCircle(color = Color(0xFFFCD34D), radius = w * 0.21f, center = Offset(w * 0.5f, h * 0.45f))
        drawArc(
            color = LabourOrangeAccent,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.24f, h * 0.16f),
            size = Size(w * 0.52f, h * 0.34f)
        )
        drawLine(
            color = LabourOrangeAccent,
            start = Offset(w * 0.20f, h * 0.33f),
            end = Offset(w * 0.80f, h * 0.33f),
            strokeWidth = w * 0.065f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
internal fun LabourBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) LabourOrangeAccent else Color(0xFF475569)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = tint
        )
    }
}

internal fun fetchLiveCustomerRequestsFromFirebase(
    onLoaded: (List<LabourWorkRequestItem>) -> Unit
) {
    Thread {
        val list = mutableListOf<LabourWorkRequestItem>()
        try {
            val conn = URL("$LABOUR_FB_URL/jobs.json").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            if (conn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                    val root = JSONObject(resp)
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val obj = root.optJSONObject(k) ?: continue
                        list.add(
                            LabourWorkRequestItem(
                                id = obj.optString("id", k),
                                title = obj.optString("title", "Work Request"),
                                category = obj.optString("category", "General Labour"),
                                description = obj.optString("description", "Local work request"),
                                customerName = obj.optString("customerName", "Customer"),
                                customerPhone = obj.optString("customerPhone", "+91 9876543210"),
                                location = obj.optString("location", "Silwani"),
                                stateName = obj.optString("state", "Madhya Pradesh"),
                                dailyRate = obj.optInt("dailyRate", 600),
                                workersNeeded = obj.optInt("workersNeeded", 1),
                                date = obj.optString("date", "Today"),
                                status = obj.optString("status", "PENDING").uppercase(Locale.US),
                                urgency = obj.optString("urgency", "Normal")
                            )
                        )
                    }
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post {
            onLoaded(list)
        }
    }.start()
}

internal fun updateJobStatusInFirebase(jobId: String, status: String, workerName: String) {
    Thread {
        try {
            val url = URL("$LABOUR_FB_URL/jobs/$jobId.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("status", status)
                put("workerName", workerName)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}
