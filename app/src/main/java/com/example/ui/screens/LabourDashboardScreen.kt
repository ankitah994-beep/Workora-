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

private fun translateLabourCategory(category: String, isHindi: Boolean): String {
    if (!isHindi) return category
    return when (category.trim().lowercase(Locale.US)) {
        "all" -> "सभी (All)"
        "mason" -> "राजमिस्त्री (Mason)"
        "electrician" -> "इलेक्ट्रीशियन (Electrician)"
        "plumber" -> "प्लंबर (Plumber)"
        "painter" -> "पेंटर (Painter)"
        "carpenter" -> "बढ़ई (Carpenter)"
        "general labour" -> "मजदूर (General Labour)"
        "tile worker" -> "टाइल्स कारीगर (Tile Worker)"
        "cleaner" -> "सफाई कर्मी (Cleaner)"
        "farm worker" -> "कृषि मजदूर (Farm Worker)"
        else -> category
    }
}

private fun matchesLabourSmartSearch(req: LabourWorkRequestItem, rawQuery: String): Boolean {
    val q = rawQuery.trim().lowercase(Locale.US)
    if (q.isEmpty()) return true

    val hindiCat = translateLabourCategory(req.category, true).lowercase(Locale.US)
    val synonyms = when (req.category.trim().lowercase(Locale.US)) {
        "mason" -> "राजमिस्त्री मिस्त्री प्लास्टर ईंट चुनाई mistri brick plaster"
        "electrician" -> "इलेक्ट्रीशियन बिजली वायरिंग पंखा लाइट स्विच bijli wiring fan light"
        "plumber" -> "प्लंबर नल पाइप टंकी फिटिंग मोटर nal pipe tank fitting"
        "painter" -> "पेंटर पुट्टी पेंट रंगाई कलर putty paint color"
        "carpenter" -> "बढ़ई फर्नीचर लकड़ी दरवाजा खिड़की badhai wood door furniture"
        "general labour" -> "मजदूर लेबर हेल्पर लोडिंग खुदाई majdur labour helper"
        "tile worker" -> "टाइल्स पत्थर मार्बल ग्रेनाइट फर्श tile tiles marble granite worker"
        "cleaner" -> "सफाई क्लीनर झाड़ू पोछा टैंक safai cleaner cleaning"
        "farm worker" -> "कृषि खेती किसान फसल कटाई kheti farm kisan"
        else -> ""
    }

    val blob = buildString {
        append(req.title.lowercase(Locale.US)).append(" ")
        append(req.category.lowercase(Locale.US)).append(" ")
        append(hindiCat).append(" ")
        append(synonyms).append(" ")
        append(req.description.lowercase(Locale.US)).append(" ")
        append(req.customerName.lowercase(Locale.US)).append(" ")
        append(req.customerPhone.lowercase(Locale.US)).append(" ")
        append(req.location.lowercase(Locale.US)).append(" ")
        append(req.stateName.lowercase(Locale.US)).append(" ")
        append(req.dailyRate.toString()).append(" ")
        append("work job kaam काम वर्कर")
    }

    if (blob.contains(q)) return true
    val words = q.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return words.isNotEmpty() && words.all { blob.contains(it) }
}

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

    LaunchedEffect(Unit) {
        AppLanguageManager.init(context)
    }
    val selectedLanguage = AppLanguageManager.currentLanguage
    val isHindi = selectedLanguage.equals("Hindi", ignoreCase = true) || selectedLanguage.contains("हिंदी")

    LaunchedEffect(toastMessage) {
        if (!toastMessage.isNullOrBlank()) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
        }
    }

    val workerName = profilePrefs.getString("user_name", "")?.ifBlank {
        authPrefs.getString("last_logged_in_email", if (isHindi) "कारीगर" else "Worker")?.substringBefore("@") ?: "Worker"
    } ?: "Worker"
    val workerSkill = profilePrefs.getString("user_skill", "")?.ifBlank { if (isHindi) "कुशल कारीगर" else "Skilled Worker" } ?: "Skilled Worker"
    val workerArea = profilePrefs.getString("user_location", "")?.ifBlank { if (isHindi) "स्थानीय क्षेत्र" else "Local Area" } ?: "Local Area"
    val workerExp = profilePrefs.getString("user_experience", "")?.ifBlank { if (isHindi) "अनुभवी" else "Experienced" } ?: "Experienced"
    val workerRate = profilePrefs.getString("user_rate", "")?.ifBlank { "600" } ?: "600"
    val workerProfilePhoto = profilePrefs.getString("user_profile_photo", "") ?: ""

    var localSearchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var sortMode by remember { mutableStateOf("DEFAULT") } // DEFAULT, HIGH_RATE, URGENT_FIRST
    var showSortDialog by remember { mutableStateOf(false) }
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
            ),
            LabourWorkRequestItem(
                id = "req_5",
                title = "Floor Vitrified Tile Fitting",
                category = "Tile Worker",
                description = "Need experienced tile worker for hall and kitchen floor tile fitting.",
                customerName = "Sandeep Jain",
                customerPhone = "+91 9826112233",
                location = "Main Road, Silwani",
                stateName = "Madhya Pradesh",
                dailyRate = 900,
                workersNeeded = 1,
                date = "Today",
                status = "PENDING",
                urgency = "Urgent"
            ),
            LabourWorkRequestItem(
                id = "req_6",
                title = "Full House & Water Tank Cleaning",
                category = "Cleaner",
                description = "Deep cleaning of 3 rooms and overhead water tank cleaning work.",
                customerName = "Neha Dubey",
                customerPhone = "+91 9755112244",
                location = "Station Road, Raisen",
                stateName = "Madhya Pradesh",
                dailyRate = 550,
                workersNeeded = 1,
                date = "Today",
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

    // Real-time reactive filtering without stale remember cache
    val filteredRequests = workRequestsList
        .filter { req ->
            val matchCat = selectedCategoryFilter == "All" || req.category.equals(selectedCategoryFilter, ignoreCase = true)
            matchCat && matchesLabourSmartSearch(req, localSearchQuery)
        }
        .let { list ->
            when (sortMode) {
                "HIGH_RATE" -> list.sortedByDescending { it.dailyRate }
                "URGENT_FIRST" -> list.sortedByDescending { it.urgency.equals("Urgent", true) }
                else -> list
            }
        }

    val openDirectLiveChatWithCustomer: (LabourWorkRequestItem) -> Unit = { req ->
        chatPrefs.edit()
            .putString("chat_partner_id", req.id)
            .putString("chat_partner_name", req.customerName)
            .putString("chat_partner_role", "${if (isHindi) "ग्राहक" else "Customer"} • ${req.title}")
            .putString("chat_partner_phone", req.customerPhone)
            .putString("chat_partner_area", req.location)
            .apply()
        onOpenChat()
    }

    if (selectedCustomerRequestForProfile != null) {
        val req = selectedCustomerRequestForProfile!!
        CustomerAndJobDetailProfileScreen(
            request = req,
            isHindi = isHindi,
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
                    Toast.makeText(
                        context,
                        if (isHindi) "काम की रिक्वेस्ट स्वीकार कर ली गई! ✓" else "Work Request Accepted! ✓",
                        Toast.LENGTH_SHORT
                    ).show()
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
                        label = if (isHindi) "होम" else "Home",
                        isSelected = true,
                        onClick = {
                            selectedCategoryFilter = "All"
                            localSearchQuery = ""
                        }
                    )
                    LabourBottomNavItem(
                        icon = Icons.Outlined.WorkOutline,
                        label = if (isHindi) "मेरे काम" else "My Jobs",
                        isSelected = false,
                        onClick = {
                            selectedCategoryFilter = "All"
                            localSearchQuery = ""
                        }
                    )
                    LabourBottomNavItem(
                        icon = Icons.Outlined.Chat,
                        label = if (isHindi) "चैट" else "Chat",
                        isSelected = false,
                        onClick = onOpenChat
                    )
                    LabourBottomNavItem(
                        icon = Icons.Outlined.Person,
                        label = if (isHindi) "प्रोफाइल" else "Profile",
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
            // Top Navy Header + Real Working Search Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LabourNavyPrimary)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = if (isHindi) "कारीगर डैशबोर्ड" else "Worker Dashboard",
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
                                    text = if (isHindi) "कस्टमर मोड" else "Customer Mode",
                                    color = LabourWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = localSearchQuery,
                        onValueChange = { query ->
                            localSearchQuery = query
                            if (query.isNotBlank() && selectedCategoryFilter != "All") {
                                selectedCategoryFilter = "All"
                            }
                        },
                        placeholder = {
                            Text(
                                text = if (isHindi) "काम या ग्राहक खोजें (जैसे mason, cleaner, Silwani...)" else "Search work or customer (e.g. mason, cleaner, Silwani...)",
                                fontSize = 13.sp,
                                color = LabourSecondaryText
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = LabourSecondaryText)
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (localSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { localSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = LabourSecondaryText)
                                    }
                                }
                                IconButton(onClick = { showSortDialog = true }) {
                                    Icon(Icons.Outlined.Tune, contentDescription = "Filter", tint = LabourNavyPrimary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LabourWhite,
                            unfocusedContainerColor = LabourWhite,
                            focusedBorderColor = LabourOrangeAccent,
                            unfocusedBorderColor = LabourWhite,
                            focusedTextColor = LabourMainText,
                            unfocusedTextColor = LabourMainText
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Worker Summary + Availability Card
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
                            if (workerProfilePhoto.isNotBlank()) {
                                UserProfilePhotoView(base64Photo = workerProfilePhoto, size = 68.dp)
                            } else {
                                CustomerHardHatAvatar(size = 68.dp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = workerName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = LabourMainText
                                )
                                Text(
                                    text = translateLabourCategory(workerSkill, isHindi),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LabourNavyPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📍 $workerArea • ${if (isHindi) "अनुभव:" else "Exp:"} $workerExp",
                                    fontSize = 12.sp,
                                    color = LabourSecondaryText
                                )
                                Text(
                                    text = "${if (isHindi) "प्रतिदिन रेट:" else "Daily Rate:"} ₹$workerRate/${if (isHindi) "दिन" else "day"}",
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
                                    text = if (isAvailable) {
                                        if (isHindi) "काम के लिए उपलब्ध (Available)" else "Available for work"
                                    } else {
                                        if (isHindi) "अभी व्यस्त (Currently Busy)" else "Currently Busy"
                                    },
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

            // Category Filter Chips
            item {
                val cats = listOf(
                    "All", "Mason", "Electrician", "Plumber", "Painter",
                    "Carpenter", "General Labour", "Tile Worker", "Cleaner", "Farm Worker"
                )
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
                            modifier = Modifier.clickable {
                                selectedCategoryFilter = c
                                if (localSearchQuery.isNotBlank()) {
                                    localSearchQuery = ""
                                }
                            }
                        ) {
                            Text(
                                text = translateLabourCategory(c, isHindi),
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
                        text = if (isHindi) "ग्राहकों के काम की रिक्वेस्ट (${filteredRequests.size})" else "Customer Work Requests (${filteredRequests.size})",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LabourMainText
                    )
                    Text(
                        text = if (isHindi) "सभी देखें >" else "See All >",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LabourNavyPrimary,
                        modifier = Modifier.clickable {
                            selectedCategoryFilter = "All"
                            localSearchQuery = ""
                            sortMode = "DEFAULT"
                        }
                    )
                }
            }

            if (filteredRequests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = LabourWhite),
                        border = BorderStroke(1.dp, LabourBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SearchOff,
                                contentDescription = null,
                                tint = LabourSecondaryText,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isHindi) "कोई काम की रिक्वेस्ट नहीं मिली" else "No work requests found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = LabourMainText
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    localSearchQuery = ""
                                    selectedCategoryFilter = "All"
                                    sortMode = "DEFAULT"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LabourOrangeAccent)
                            ) {
                                Text(if (isHindi) "सभी काम दिखाएं" else "Show All Work Requests", color = LabourWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(filteredRequests, key = { it.id }) { req ->
                    CustomerWorkRequestCard(
                        request = req,
                        isHindi = isHindi,
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

    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            containerColor = LabourWhite,
            title = {
                Text(
                    text = if (isHindi) "काम फ़िल्टर और सॉर्ट करें" else "Filter & Sort Work Requests",
                    fontWeight = FontWeight.Bold,
                    color = LabourMainText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(
                        "DEFAULT" to (if (isHindi) "डिफ़ॉल्ट (सभी नए काम)" else "Default (Latest Requests)"),
                        "HIGH_RATE" to (if (isHindi) "सबसे ज्यादा मजदूरी रेट पहले (High Rate ₹)" else "Highest Daily Rate First (₹)"),
                        "URGENT_FIRST" to (if (isHindi) "अर्जेंट काम पहले (Urgent First)" else "Urgent Work First")
                    )
                    options.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    sortMode = key
                                    showSortDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortMode == key,
                                onClick = {
                                    sortMode = key
                                    showSortDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 14.sp, color = LabourMainText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        sortMode = "DEFAULT"
                        selectedCategoryFilter = "All"
                        localSearchQuery = ""
                        showSortDialog = false
                    }
                ) {
                    Text(if (isHindi) "रीसेट करें" else "Reset All", color = LabourOrangeAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text(if (isHindi) "बंद करें" else "Close", color = LabourSecondaryText)
                }
            }
        )
    }
}

@Composable
internal fun CustomerWorkRequestCard(
    request: LabourWorkRequestItem,
    isHindi: Boolean,
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
                        text = "${request.title} (${translateLabourCategory(request.category, isHindi)})",
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
                        text = "₹${request.dailyRate}/${if (isHindi) "दिन" else "day"}",
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
                        text = if (isHindi) "अभी कॉल करें (Call Now)" else "Call Now",
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
    isHindi: Boolean,
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
                    text = if (isHindi) "ग्राहक और काम का विवरण" else "Customer & Work Profile",
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
                                text = if (isHindi) "सत्यापित ग्राहक (Verified Customer)" else "Verified Customer",
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

                    Text(
                        text = "${if (isHindi) "काम:" else "Work Title:"} ${request.title}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = LabourMainText
                    )
                    Text(
                        text = "${if (isHindi) "श्रेणी:" else "Category Needed:"} ${translateLabourCategory(request.category, isHindi)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LabourNavyPrimary
                    )
                    Text(
                        text = "${if (isHindi) "प्रतिदिन रेट:" else "Offered Rate:"} ₹${request.dailyRate}/${if (isHindi) "दिन" else "day"} • ${if (isHindi) "कारीगर चाहिए:" else "Workers Needed:"} ${request.workersNeeded}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LabourOrangeAccent
                    )
                    Text(
                        text = "${if (isHindi) "ग्राहक का नंबर:" else "Customer Phone:"} ${request.customerPhone}",
                        fontSize = 14.sp,
                        color = LabourMainText
                    )
                    Text(
                        text = if (isHindi) "काम का पूरा विवरण:" else "Work Description:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = LabourMainText
                    )
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
                    Text(if (isHindi) "कॉल करें" else "Call Now", color = LabourWhite, fontWeight = FontWeight.Bold)
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
                    Text(if (isHindi) "लाइव चैट" else "Live Chat", color = LabourWhite, fontWeight = FontWeight.Bold)
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
                    text = if (request.status == "ACCEPTED") {
                        if (isHindi) "काम स्वीकार किया गया ✓" else "Work Request Accepted ✓"
                    } else {
                        if (isHindi) "काम स्वीकार करें (Accept Work)" else "Accept Work Request"
                    },
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
