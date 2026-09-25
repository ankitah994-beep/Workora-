package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Plumbing
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JobPost
import com.example.model.Worker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==================== EXACT THEME COLORS FROM REFERENCE IMAGE ====================
private val ThemeNavyBlue = Color(0xFF0B3D91)
private val ThemeOrange = Color(0xFFFF8C00)
private val ThemeBgWhite = Color(0xFFF8FAFC)
private val ThemeGrayText = Color(0xFF6B7280)
private val ThemeDarkHeading = Color(0xFF111827)
private val ThemeCardBorder = Color(0xFFE5E7EB)

private const val CUSTOMER_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

data class UIWorkerProfile(
    val key: String,
    val name: String,
    val trade: String,
    val dailyWage: Int,
    val phone: String,
    val area: String,
    val distanceText: String,
    val experienceYears: String,
    val rating: Double,
    val reviewsCount: Int,
    val aboutText: String,
    val shirtColorHex: Long = 0xFF1E3A8A,
    val hasYellowHelmet: Boolean = true
)

data class UIBookingItem(
    val id: Long,
    val worker: UIWorkerProfile,
    val workTitle: String,
    val dateText: String,
    val areaText: String,
    val status: String // "Pending" or "Completed"
)

private data class CategoryCircleItem(
    val name: String,
    val bgColor: Color,
    val icon: ImageVector
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
    val defaultUserArea = remember {
        val saved = profilePrefs.getString("user_location", "Silwani") ?: "Silwani"
        saved.split(",").firstOrNull()?.trim()?.ifBlank { "Silwani" } ?: "Silwani"
    }

    // Sub-screen states for Screen 5 (Worker Profile) & Screen 6 (Hire Request)
    var selectedWorkerForProfile by remember { mutableStateOf<UIWorkerProfile?>(null) }
    var selectedWorkerForHireRequest by remember { mutableStateOf<UIWorkerProfile?>(null) }

    // Bookings Sub-tab: 0 = Active, 1 = Completed (Screen 7)
    var bookingsSubTab by remember { mutableIntStateOf(0) }

    // Hire Request Form States (Screen 6)
    var hireDescription by remember { mutableStateOf("Need to fix home wiring and install new switch board.") }
    var hireDate by remember {
        mutableStateOf(SimpleDateFormat("d MMM yyyy", Locale.ENGLISH).format(Date()))
    }
    var hireArea by remember { mutableStateOf(defaultUserArea) }

    val searchFocusRequester = remember { FocusRequester() }

    val workersList = remember {
        mutableStateListOf(
            UIWorkerProfile(
                key = "w_ramesh",
                name = "Ramesh Kumar",
                trade = "Electrician",
                dailyWage = 500,
                phone = "+91 9876543210",
                area = defaultUserArea,
                distanceText = "5 km",
                experienceYears = "3 years",
                rating = 4.5,
                reviewsCount = 12,
                aboutText = "I am a skilled electrician with 3 years of experience in home and office electrical work. I can handle wiring, switch boards, fittings and more.",
                shirtColorHex = 0xFF1E3A8A,
                hasYellowHelmet = true
            ),
            UIWorkerProfile(
                key = "w_suresh",
                name = "Suresh Yadav",
                trade = "Painter",
                dailyWage = 450,
                phone = "+91 9123456780",
                area = defaultUserArea,
                distanceText = "7 km",
                experienceYears = "4 years",
                rating = 4.6,
                reviewsCount = 18,
                aboutText = "I am an experienced painter with 4 years of work in interior wall painting, exterior texture, putty and waterproof coating.",
                shirtColorHex = 0xFF0F766E,
                hasYellowHelmet = false
            ),
            UIWorkerProfile(
                key = "w_amit",
                name = "Amit Sharma",
                trade = "Electrician",
                dailyWage = 500,
                phone = "+91 9988776655",
                area = defaultUserArea,
                distanceText = "5 km",
                experienceYears = "3 years",
                rating = 4.5,
                reviewsCount = 12,
                aboutText = "I am a skilled electrician with 3 years of experience in home and office electrical work. I can handle wiring, switch boards, fittings and more.",
                shirtColorHex = 0xFF1D4ED8,
                hasYellowHelmet = true
            ),
            UIWorkerProfile(
                key = "w_mahesh",
                name = "Mahesh Singh",
                trade = "Mason",
                dailyWage = 400,
                phone = "+91 9755443322",
                area = defaultUserArea,
                distanceText = "4 km",
                experienceYears = "5 years",
                rating = 4.7,
                reviewsCount = 15,
                aboutText = "I am a professional mason with 5 years of experience in brickwork, wall plastering, floor tiles and RCC construction work.",
                shirtColorHex = 0xFF374151,
                hasYellowHelmet = true
            )
        )
    }

    val bookingsList = remember {
        mutableStateListOf(
            UIBookingItem(
                id = 201L,
                worker = workersList[0],
                workTitle = "Fix home wiring",
                dateText = "2 Nov 2024",
                areaText = defaultUserArea,
                status = "Pending"
            ),
            UIBookingItem(
                id = 202L,
                worker = workersList[1],
                workTitle = "Wall Painting Work",
                dateText = "28 Oct 2024",
                areaText = defaultUserArea,
                status = "Completed"
            ),
            UIBookingItem(
                id = 203L,
                worker = workersList[3],
                workTitle = "Brickwork & Plastering",
                dateText = "20 Oct 2024",
                areaText = defaultUserArea,
                status = "Completed"
            )
        )
    }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = (URL("$CUSTOMER_DB_URL/workers.json").openConnection() as HttpURLConnection)
                if (conn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        val cloudItems = mutableListOf<UIWorkerProfile>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val wName = obj.optString("name", "").trim()
                            val wTrade = obj.optString("trade", "Electrician").split("/").first().trim()
                            val wWage = obj.optInt("dailyWage", 500)
                            val wPhone = obj.optString("phone", "+91 6265798340")
                            val wLoc = obj.optString("location", defaultUserArea).split(",").first().trim()
                            if (wName.isNotBlank() && workersList.none { it.name.equals(wName, ignoreCase = true) }) {
                                cloudItems.add(
                                    UIWorkerProfile(
                                        key = k,
                                        name = wName,
                                        trade = wTrade,
                                        dailyWage = wWage,
                                        phone = wPhone,
                                        area = wLoc.ifBlank { defaultUserArea },
                                        distanceText = "5 km",
                                        experienceYears = "3 years",
                                        rating = obj.optDouble("rating", 4.5),
                                        reviewsCount = 12,
                                        aboutText = "I am a skilled ${wTrade.lowercase()} with 3 years of experience in home and office work.",
                                        hasYellowHelmet = true
                                    )
                                )
                            }
                        }
                        if (cloudItems.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                workersList.addAll(cloudItems)
                            }
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    BackHandler(enabled = selectedWorkerForHireRequest != null || selectedWorkerForProfile != null || activeTab != 0) {
        when {
            selectedWorkerForHireRequest != null -> selectedWorkerForHireRequest = null
            selectedWorkerForProfile != null -> selectedWorkerForProfile = null
            activeTab != 0 -> onTabSelected(0)
        }
    }

    // ==================== SCREEN 6: HIRE REQUEST SCREEN ====================
    if (selectedWorkerForHireRequest != null) {
        val workerToHire = selectedWorkerForHireRequest!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ThemeNavyBlue)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedWorkerForHireRequest = null },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hire Request",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = ThemeBgWhite,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 26.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Work Description ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeDarkHeading
                        )
                        Text(
                            text = "*",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hireDescription,
                        onValueChange = { hireDescription = it },
                        placeholder = {
                            Text(
                                text = "Need to fix home wiring and install new switch board.",
                                color = ThemeGrayText,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = TextStyle(
                            color = ThemeDarkHeading,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(118.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ThemeNavyBlue,
                            unfocusedBorderColor = ThemeCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Date ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeDarkHeading
                        )
                        Text(
                            text = "*",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hireDate,
                        onValueChange = { hireDate = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = ThemeGrayText,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = ThemeDarkHeading,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ThemeNavyBlue,
                            unfocusedBorderColor = ThemeCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Area ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeDarkHeading
                        )
                        Text(
                            text = "*",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = hireArea,
                        onValueChange = { hireArea = it },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Area",
                                tint = ThemeGrayText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = ThemeDarkHeading,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = ThemeNavyBlue,
                            unfocusedBorderColor = ThemeCardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = {
                            val cleanDesc = hireDescription.trim().ifBlank { "Work request for ${workerToHire.name}" }
                            val cleanArea = hireArea.trim().ifBlank { workerToHire.area }
                            val cleanDate = hireDate.trim().ifBlank { "Today" }

                            bookingsList.add(
                                0,
                                UIBookingItem(
                                    id = System.currentTimeMillis(),
                                    worker = workerToHire,
                                    workTitle = cleanDesc,
                                    dateText = cleanDate,
                                    areaText = cleanArea,
                                    status = "Pending"
                                )
                            )

                            onPostJob(
                                cleanDesc.take(40),
                                workerToHire.trade,
                                cleanDesc,
                                workerToHire.dailyWage,
                                cleanArea,
                                1,
                                cleanDate,
                                cleanDate
                            )

                            Toast.makeText(
                                context,
                                "Hire Request Sent to ${workerToHire.name}! ✓",
                                Toast.LENGTH_SHORT
                            ).show()

                            selectedWorkerForHireRequest = null
                            selectedWorkerForProfile = null
                            bookingsSubTab = 0
                            onTabSelected(1)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeOrange),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "Send Request",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        return
    }

    // ==================== SCREEN 5: WORKER PROFILE SCREEN ====================
    if (selectedWorkerForProfile != null) {
        val worker = selectedWorkerForProfile!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ThemeNavyBlue)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedWorkerForProfile = null },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Worker Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = ThemeBgWhite,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp, vertical = 26.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WorkerPortraitAvatar(
                                size = 88.dp,
                                shirtColor = Color(worker.shirtColorHex),
                                showYellowHelmet = true
                            )
                            Spacer(modifier = Modifier.width(18.dp))
                            Column {
                                Text(
                                    text = worker.name,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ThemeDarkHeading
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = worker.trade,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ThemeGrayText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Rating",
                                        tint = ThemeOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${worker.rating}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeOrange
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${worker.reviewsCount} reviews)",
                                        fontSize = 14.sp,
                                        color = ThemeGrayText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(26.dp))
                        HorizontalDivider(color = ThemeCardBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.WorkOutline,
                                    contentDescription = null,
                                    tint = ThemeNavyBlue,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = worker.experienceYears,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeDarkHeading
                                    )
                                    Text(
                                        text = "Experience",
                                        fontSize = 12.sp,
                                        color = ThemeGrayText
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = ThemeNavyBlue,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = worker.area,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeDarkHeading
                                    )
                                    Text(
                                        text = "Area",
                                        fontSize = 12.sp,
                                        color = ThemeGrayText
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = ThemeNavyBlue,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "₹${worker.dailyWage}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ThemeDarkHeading
                                    )
                                    Text(
                                        text = "Daily Rate",
                                        fontSize = 12.sp,
                                        color = ThemeGrayText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = ThemeCardBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            text = "About",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeDarkHeading
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = worker.aboutText,
                            fontSize = 14.sp,
                            color = Color(0xFF374151),
                            lineHeight = 22.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}")))
                                } catch (_: Exception) {
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, ThemeNavyBlue)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = ThemeNavyBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThemeNavyBlue)
                        }

                        Button(
                            onClick = {
                                hireDescription = "Need ${worker.trade.lowercase()} work from ${worker.name}."
                                hireArea = worker.area
                                selectedWorkerForHireRequest = worker
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemeOrange),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = "Hire",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        return
    }

    // ==================== MAIN SCAFFOLD: SCREEN 3 (CUSTOMER HOME) & SCREEN 7 (MY BOOKINGS) ====================
    val categoryItems = remember {
        listOf(
            CategoryCircleItem("Mason", ThemeOrange, Icons.Default.Construction),
            CategoryCircleItem("Painter", Color(0xFF1D4ED8), Icons.Default.FormatPaint),
            CategoryCircleItem("Electrician", Color(0xFF16A34A), Icons.Default.ElectricalServices),
            CategoryCircleItem("Plumber", Color(0xFF7E22CE), Icons.Default.Plumbing),
            CategoryCircleItem("Carpenter", Color(0xFF0E7490), Icons.Default.Build),
            CategoryCircleItem("Cleaner", ThemeOrange, Icons.Default.CleaningServices),
            CategoryCircleItem("General Labour", Color(0xFF0284C7), Icons.Default.Groups)
        )
    }

    val filteredWorkers = workersList.filter { worker ->
        val matchesQuery = searchQuery.isBlank() ||
                worker.name.contains(searchQuery, ignoreCase = true) ||
                worker.trade.contains(searchQuery, ignoreCase = true) ||
                worker.area.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory.isNullOrBlank() ||
                selectedCategory.equals("All", ignoreCase = true) ||
                worker.trade.equals(selectedCategory, ignoreCase = true) ||
                worker.trade.contains(selectedCategory, ignoreCase = true)

        matchesQuery && matchesCategory
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeNavyBlue)
            .statusBarsPadding()
    ) {
        if (activeTab == 0) {
            // ==================== SCREEN 3: CUSTOMER HOME ====================
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Navy Header: "Workora" + Role Switch + Search Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 15.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Workora",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = onSwitchRole,
                            color = Color.White.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Switch Role",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Worker Mode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                try {
                                    searchFocusRequester.requestFocus()
                                } catch (_: Exception) {
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    }
                }

                // Rounded White Main Content Container
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ThemeBgWhite,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp, vertical = 20.dp)
                            .padding(bottom = 110.dp)
                    ) {
                        // 1. Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChanged,
                            placeholder = {
                                Text(
                                    text = "Search for workers (e.g. painter, electrician...)",
                                    fontSize = 13.sp,
                                    color = ThemeGrayText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = ThemeNavyBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = ThemeDarkHeading,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF1F5F9),
                                unfocusedContainerColor = Color(0xFFF1F5F9),
                                focusedBorderColor = ThemeNavyBlue,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 2. Categories Section Header ("Categories" ... "See All")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categories",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = ThemeDarkHeading
                            )
                            Text(
                                text = if (selectedCategory.isNullOrBlank() || selectedCategory == "All") "See All" else "Show All",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ThemeGrayText,
                                modifier = Modifier.clickable { onCategorySelected("All") }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Row 1 of Categories (4 items: Mason, Painter, Electrician, Plumber)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categoryItems.take(4).forEach { item ->
                                val isSelected = selectedCategory?.equals(item.name, ignoreCase = true) == true
                                CategoryTileCard(
                                    item = item,
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (isSelected) onCategorySelected("All")
                                        else onCategorySelected(item.name)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 2 of Categories (3 items: Carpenter, Cleaner, General Labour)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categoryItems.drop(4).forEach { item ->
                                val isSelected = selectedCategory?.equals(item.name, ignoreCase = true) == true
                                CategoryTileCard(
                                    item = item,
                                    isSelected = isSelected,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (isSelected) onCategorySelected("All")
                                        else onCategorySelected(item.name)
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.weight(0.65f))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 3. Available Workers Header
                        Text(
                            text = "Available Workers",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeDarkHeading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4. Available Workers Cards List
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            filteredWorkers.forEach { worker ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedWorkerForProfile = worker
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, ThemeCardBorder),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        WorkerPortraitAvatar(
                                            size = 64.dp,
                                            shirtColor = Color(worker.shirtColorHex),
                                            showYellowHelmet = worker.hasYellowHelmet
                                        )

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = worker.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ThemeDarkHeading,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Build,
                                                    contentDescription = null,
                                                    tint = ThemeNavyBlue,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = worker.trade,
                                                    fontSize = 13.sp,
                                                    color = ThemeGrayText
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(5.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = ThemeNavyBlue,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${worker.area}, ${worker.distanceText}",
                                                    fontSize = 12.sp,
                                                    color = ThemeGrayText
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "₹${worker.dailyWage}/day",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ThemeDarkHeading
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Button(
                                            onClick = {
                                                selectedWorkerForProfile = worker
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ThemeOrange),
                                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(
                                                text = "Hire",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==================== SCREEN 7: MY BOOKINGS ====================
            val displayedBookings = bookingsList.filter { item ->
                if (bookingsSubTab == 0) item.status.equals("Pending", ignoreCase = true)
                else item.status.equals("Completed", ignoreCase = true)
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Bookings",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ThemeBgWhite,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Active", "Completed").forEachIndexed { index, title ->
                                val isSelected = bookingsSubTab == index
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { bookingsSubTab = index }
                                        .padding(top = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) ThemeNavyBlue else ThemeGrayText
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(if (isSelected) ThemeNavyBlue else Color.Transparent)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = ThemeCardBorder, thickness = 1.dp)

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 18.dp, vertical = 18.dp)
                                .padding(bottom = 110.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val listToShow = if (displayedBookings.isEmpty()) bookingsList else displayedBookings
                            listToShow.forEach { booking ->
                                val isPending = booking.status.equals("Pending", ignoreCase = true)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedWorkerForProfile = booking.worker
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, ThemeCardBorder),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            WorkerPortraitAvatar(
                                                size = 60.dp,
                                                shirtColor = Color(booking.worker.shirtColorHex),
                                                showYellowHelmet = booking.worker.hasYellowHelmet
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = booking.worker.name,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ThemeDarkHeading
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = booking.worker.trade,
                                                    fontSize = 13.sp,
                                                    color = ThemeGrayText
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "₹${booking.worker.dailyWage}/day",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ThemeGrayText
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Date: ${booking.dateText}",
                                                fontSize = 13.sp,
                                                color = ThemeGrayText
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(20.dp))
                                                    .background(
                                                        if (isPending) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                                                    )
                                                    .clickable {
                                                        if (isPending) {
                                                            val idx = bookingsList.indexOf(booking)
                                                            if (idx >= 0) {
                                                                bookingsList[idx] = booking.copy(status = "Completed")
                                                            }
                                                            onCompleteJob(booking.id)
                                                        }
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = if (isPending) "Pending" else "Completed",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isPending) Color(0xFFD97706) else Color(0xFF15803D)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== EXACT 3-TAB BOTTOM NAVIGATION BAR (Home | Bookings | Profile) ====================
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isHomeSelected = activeTab == 0
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onTabSelected(0) }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (isHomeSelected) ThemeNavyBlue else ThemeGrayText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Home",
                        fontSize = 11.sp,
                        fontWeight = if (isHomeSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isHomeSelected) ThemeOrange else ThemeGrayText
                    )
                }

                val isBookingsSelected = activeTab == 1
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onTabSelected(1) }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Bookings",
                        tint = if (isBookingsSelected) ThemeNavyBlue else ThemeGrayText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Bookings",
                        fontSize = 11.sp,
                        fontWeight = if (isBookingsSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isBookingsSelected) ThemeNavyBlue else ThemeGrayText
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenProfile() }
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonOutline,
                        contentDescription = "Profile",
                        tint = ThemeGrayText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Profile",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ThemeGrayText
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTileCard(
    item: CategoryCircleItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(94.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)
        ),
        border = if (isSelected) BorderStroke(1.5.dp, ThemeNavyBlue) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(item.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.name,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.name,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = ThemeDarkHeading,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
    }
}

/**
 * Clean, natural and professional Worker Portrait Avatar (fixes the dark mouth shape from earlier)
 */
@Composable
private fun WorkerPortraitAvatar(
    size: Dp,
    shirtColor: Color,
    showYellowHelmet: Boolean
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                )
            )
            .border(1.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // 1. Shoulders & Work Jacket
            drawArc(
                color = shirtColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.10f, h * 0.64f),
                size = Size(w * 0.80f, h * 0.62f)
            )

            // White Inner T-Shirt V-Neck
            val collarPath = Path().apply {
                moveTo(w * 0.38f, h * 0.65f)
                lineTo(w * 0.50f, h * 0.79f)
                lineTo(w * 0.62f, h * 0.65f)
                close()
            }
            drawPath(path = collarPath, color = Color.White)

            // 2. Neck
            drawRoundRect(
                color = Color(0xFFD69A6A),
                topLeft = Offset(w * 0.41f, h * 0.52f),
                size = Size(w * 0.18f, h * 0.16f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // 3. Ears
            drawCircle(
                color = Color(0xFFD69A6A),
                radius = w * 0.045f,
                center = Offset(w * 0.28f, h * 0.42f)
            )
            drawCircle(
                color = Color(0xFFD69A6A),
                radius = w * 0.045f,
                center = Offset(w * 0.72f, h * 0.42f)
            )

            // 4. Natural Face Oval
            drawOval(
                color = Color(0xFFE6AC7E),
                topLeft = Offset(w * 0.29f, h * 0.23f),
                size = Size(w * 0.42f, h * 0.38f)
            )

            // 5. Eyes & Eyebrows
            drawCircle(
                color = Color(0xFF1E293B),
                radius = w * 0.022f,
                center = Offset(w * 0.42f, h * 0.40f)
            )
            drawCircle(
                color = Color(0xFF1E293B),
                radius = w * 0.022f,
                center = Offset(w * 0.58f, h * 0.40f)
            )

            // Neat Mustache & Gentle Smile Stroke (Not filled!)
            drawArc(
                color = Color(0xFF334155),
                startAngle = 25f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(w * 0.42f, h * 0.46f),
                size = Size(w * 0.16f, h * 0.07f),
                style = Stroke(width = w * 0.026f, cap = StrokeCap.Round)
            )

            if (showYellowHelmet) {
                // Safety Helmet Dome
                drawArc(
                    color = Color(0xFFFACC15),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.26f, h * 0.12f),
                    size = Size(w * 0.48f, h * 0.32f)
                )
                // Helmet Top Ridge
                drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(w * 0.45f, h * 0.10f),
                    size = Size(w * 0.10f, h * 0.15f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                // Helmet Visor Brim
                drawRoundRect(
                    color = Color(0xFFEAB308),
                    topLeft = Offset(w * 0.23f, h * 0.26f),
                    size = Size(w * 0.54f, h * 0.048f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            } else {
                // Neat Dark Hair
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 175f,
                    sweepAngle = 190f,
                    useCenter = true,
                    topLeft = Offset(w * 0.28f, h * 0.16f),
                    size = Size(w * 0.44f, h * 0.24f)
                )
            }
        }
    }
}
