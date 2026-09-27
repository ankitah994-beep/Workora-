package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JobApplication
import com.example.model.JobPost
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

private const val LABOUR_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

data class WorkoraLabourJobItem(
    val key: String,
    val id: Long,
    val title: String,
    val category: String,
    val description: String,
    val dailyRate: Int,
    val budgetType: String = "Per Day (₹/day)",
    val location: String,
    val preferredDate: String = "Immediately",
    val numberOfDays: String = "3",
    val workersNeeded: Int = 2,
    val workTime: String = "9:00 AM – 6:00 PM",
    val specialRequirement: String = "",
    val about: String = "",
    val customerName: String = "Customer",
    val customerPhone: String = "+91 6265798340"
)

private fun decodeLabourBase64Photo(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun encodeLabourPhotoUri(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else ""
    } catch (_: Exception) {
        ""
    }
}

@Composable
private fun ExactLabourWLogo(
    customLogoBase64: String = "",
    size: Dp = 44.dp
) {
    val customBmp = remember(customLogoBase64) { decodeLabourBase64Photo(customLogoBase64) }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (customBmp != null) {
            Image(
                bitmap = customBmp,
                contentDescription = "App Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height
                drawCircle(
                    color = Color(0xFFFF8C00),
                    radius = w * 0.46f,
                    style = Stroke(width = w * 0.08f)
                )
                val path = Path().apply {
                    moveTo(w * 0.26f, h * 0.36f)
                    lineTo(w * 0.38f, h * 0.66f)
                    lineTo(w * 0.50f, h * 0.46f)
                    lineTo(w * 0.62f, h * 0.66f)
                    lineTo(w * 0.74f, h * 0.36f)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF083D91),
                    style = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun ExactLabourBannerGraphic(size: Dp = 105.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawLine(
            color = Color(0xFF334155),
            start = Offset(w * 0.16f, h * 0.44f),
            end = Offset(w * 0.28f, h * 0.74f),
            strokeWidth = w * 0.055f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFF475569),
            radius = w * 0.075f,
            center = Offset(w * 0.15f, h * 0.40f)
        )
        drawCircle(
            color = Color(0xFFE0F2FE),
            radius = w * 0.032f,
            center = Offset(w * 0.13f, h * 0.38f)
        )

        drawRoundRect(
            color = Color(0xFF2563EB),
            topLeft = Offset(w * 0.26f, h * 0.54f),
            size = Size(w * 0.54f, h * 0.38f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )

        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(w * 0.34f, h * 0.58f),
            size = Size(w * 0.38f, h * 0.34f),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )
        drawRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(w * 0.36f, h * 0.52f),
            size = Size(w * 0.06f, h * 0.10f)
        )
        drawRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(w * 0.64f, h * 0.52f),
            size = Size(w * 0.06f, h * 0.10f)
        )

        drawRoundRect(
            color = Color(0xFFFDBA74),
            topLeft = Offset(w * 0.46f, h * 0.44f),
            size = Size(w * 0.14f, h * 0.12f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawCircle(
            color = Color(0xFFFED7AA),
            radius = w * 0.16f,
            center = Offset(w * 0.53f, h * 0.34f)
        )

        drawCircle(color = Color(0xFF1E293B), radius = w * 0.018f, center = Offset(w * 0.48f, h * 0.33f))
        drawCircle(color = Color(0xFF1E293B), radius = w * 0.018f, center = Offset(w * 0.58f, h * 0.33f))
        drawArc(
            color = Color(0xFF1E293B),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(w * 0.48f, h * 0.35f),
            size = Size(w * 0.10f, h * 0.06f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        val helmetPath = Path().apply {
            moveTo(w * 0.35f, h * 0.26f)
            cubicTo(w * 0.35f, h * 0.10f, w * 0.71f, h * 0.10f, w * 0.71f, h * 0.26f)
            close()
        }
        drawPath(path = helmetPath, color = Color(0xFFFACC15))
        drawRoundRect(
            color = Color(0xFFEAB308),
            topLeft = Offset(w * 0.32f, h * 0.24f),
            size = Size(w * 0.42f, h * 0.045f),
            cornerRadius = CornerRadius(10f, 10f)
        )
    }
}

@Composable
private fun ExactLabourCategoryIconBox(category: String) {
    val bgColor = when (category) {
        "Mason" -> Color(0xFFE0F2FE)
        "Electrician" -> Color(0xFFFEF3C7)
        "Plumber" -> Color(0xFFDCFCE7)
        "Painter" -> Color(0xFFFEE2E2)
        "Carpenter" -> Color(0xFFEDE9FE)
        else -> Color(0xFFF1F5F9)
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(22.dp)) {
            val w = this.size.width
            val h = this.size.height
            when (category) {
                "Mason" -> {
                    val blade = Path().apply {
                        moveTo(w * 0.75f, h * 0.20f)
                        lineTo(w * 0.30f, h * 0.45f)
                        lineTo(w * 0.55f, h * 0.70f)
                        close()
                    }
                    drawPath(blade, color = Color(0xFF083D91))
                    drawLine(
                        color = Color(0xFF083D91),
                        start = Offset(w * 0.42f, h * 0.58f),
                        end = Offset(w * 0.20f, h * 0.80f),
                        strokeWidth = w * 0.12f,
                        cap = StrokeCap.Round
                    )
                }
                "Electrician" -> {
                    val bolt = Path().apply {
                        moveTo(w * 0.58f, h * 0.10f)
                        lineTo(w * 0.25f, h * 0.55f)
                        lineTo(w * 0.50f, h * 0.55f)
                        lineTo(w * 0.40f, h * 0.90f)
                        lineTo(w * 0.75f, h * 0.45f)
                        lineTo(w * 0.50f, h * 0.45f)
                        close()
                    }
                    drawPath(bolt, color = Color(0xFFFF8C00))
                }
                "Plumber" -> {
                    drawRoundRect(
                        color = Color(0xFF16A34A),
                        topLeft = Offset(w * 0.20f, h * 0.42f),
                        size = Size(w * 0.55f, h * 0.18f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = Color(0xFF16A34A),
                        topLeft = Offset(w * 0.58f, h * 0.42f),
                        size = Size(w * 0.18f, h * 0.36f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawLine(
                        color = Color(0xFF15803D),
                        start = Offset(w * 0.32f, h * 0.25f),
                        end = Offset(w * 0.52f, h * 0.25f),
                        strokeWidth = w * 0.12f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFF15803D),
                        start = Offset(w * 0.42f, h * 0.25f),
                        end = Offset(w * 0.42f, h * 0.42f),
                        strokeWidth = w * 0.10f
                    )
                }
                "Painter" -> {
                    drawRoundRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(w * 0.20f, h * 0.18f),
                        size = Size(w * 0.56f, h * 0.24f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawLine(
                        color = Color(0xFF991B1B),
                        start = Offset(w * 0.48f, h * 0.42f),
                        end = Offset(w * 0.48f, h * 0.84f),
                        strokeWidth = w * 0.12f,
                        cap = StrokeCap.Round
                    )
                }
                "Carpenter" -> {
                    drawLine(
                        color = Color(0xFF6D28D9),
                        start = Offset(w * 0.24f, h * 0.78f),
                        end = Offset(w * 0.64f, h * 0.34f),
                        strokeWidth = w * 0.12f,
                        cap = StrokeCap.Round
                    )
                    drawRoundRect(
                        color = Color(0xFF5B21B6),
                        topLeft = Offset(w * 0.42f, h * 0.18f),
                        size = Size(w * 0.40f, h * 0.18f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }
                else -> {
                    val r = w * 0.13f
                    drawCircle(Color(0xFF083D91), radius = r, center = Offset(w * 0.32f, h * 0.32f))
                    drawCircle(Color(0xFF083D91), radius = r, center = Offset(w * 0.68f, h * 0.32f))
                    drawCircle(Color(0xFF083D91), radius = r, center = Offset(w * 0.32f, h * 0.68f))
                    drawCircle(Color(0xFF083D91), radius = r, center = Offset(w * 0.68f, h * 0.68f))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
    onOpenChat: () -> Unit = {}
) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val brandingPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }

    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val textDark = Color(0xFF102A43)
    val textMuted = Color(0xFF667085)
    val borderLight = Color(0xFFE5E7EB)
    val greenTrusted = Color(0xFF22A06B)

    // Live Admin Branding States (Synced with Admin Panel "Edit App")
    var liveAppName by remember {
        mutableStateOf(brandingPrefs.getString("app_name", "WORKORA") ?: "WORKORA")
    }
    var liveAppTagline by remember {
        mutableStateOf(brandingPrefs.getString("app_tagline", "Find Daily Work & Earn") ?: "Find Daily Work & Earn")
    }
    var liveLogoBase64 by remember {
        mutableStateOf(brandingPrefs.getString("logo_base64", "") ?: "")
    }

    var currentRealLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
    }
    var showLocationModal by remember { mutableStateOf(false) }
    var locationSearchInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // 5-Icon Bottom Navigation State:
    // 0 = 🏠 Home | 1 = 🔎 Find Jobs | 2 = ➕ Post Availability | 3 = 📋 My Work | 4 = 👤 Profile
    var bottomNavIndex by remember { mutableIntStateOf(0) }

    val appliedJobIds = remember {
        val saved = profilePrefs.getStringSet("applied_job_ids", emptySet()) ?: emptySet()
        mutableStateListOf<Long>().apply { addAll(saved.mapNotNull { it.toLongOrNull() }) }
    }

    var viewingJobDetails by remember { mutableStateOf<WorkoraLabourJobItem?>(null) }

    // ==================== LABOUR - POST WORK AVAILABILITY (ALL 12 FIELDS) ====================
    var availWorkCategory by remember {
        mutableStateOf(profilePrefs.getString("user_skill", "Mason") ?: "Mason")
    }
    var availSkills by remember { mutableStateOf("Brickwork, Plastering, Tile & Marble Fitting") }
    var availExperience by remember { mutableStateOf("5 Years Experience") }
    val availWorkPhotos = remember { mutableStateListOf<String>() }
    var availWorkArea by remember { mutableStateOf(currentRealLocation) }
    val liveAreaSuggestions = remember { mutableStateListOf<String>() }
    var isFetchingAreaSuggestions by remember { mutableStateOf(false) }
    var availMaxDistance by remember { mutableStateOf("15 KM") }
    var availDailyRate by remember {
        mutableStateOf(profilePrefs.getString("user_rate", "600") ?: "600")
    }
    var availFromDate by remember { mutableStateOf("Immediately / Today") }
    var availDays by remember { mutableStateOf("All 7 Days (Mon – Sun)") }
    var availTeamSize by remember { mutableStateOf("Individual (Akela)") }
    var availShortDescription by remember { mutableStateOf("") }
    var isPostingAvailability by remember { mutableStateOf(false) }

    val allCategories = listOf(
        "Mason", "Labour", "Painter", "Electrician", "Plumber",
        "Carpenter", "Cleaner", "Farm Worker", "Tile Worker", "Other"
    )

    val workPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && availWorkPhotos.size < 5) {
            val encoded = encodeLabourPhotoUri(context, uri)
            if (encoded.isNotBlank()) {
                availWorkPhotos.add(encoded)
                Toast.makeText(context, "Work Photo ${availWorkPhotos.size}/5 added ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun fetchLiveAreaSuggestions(query: String) {
        val clean = query.trim()
        if (clean.length < 2) {
            liveAreaSuggestions.clear()
            return
        }
        isFetchingAreaSuggestions = true
        CoroutineScope(Dispatchers.IO).launch {
            val suggestions = mutableListOf<String>()
            val presetLocal = listOf(
                "Silwani, Raisen (MP)",
                "Raisen, Madhya Pradesh",
                "Begamganj, Raisen (MP)",
                "Gairatganj, Raisen (MP)",
                "Bareli, Raisen (MP)",
                "Udaipura, Raisen (MP)",
                "Bhopal, Madhya Pradesh",
                "Sagar, Madhya Pradesh",
                "Vidisha, Madhya Pradesh",
                "Indore, Madhya Pradesh"
            ).filter { it.contains(clean, ignoreCase = true) }
            suggestions.addAll(presetLocal)

            try {
                val encoded = URLEncoder.encode("$clean, India", "UTF-8")
                val url = URL("https://nominatim.openstreetmap.org/search?q=$encoded&format=json&addressdetails=1&limit=5")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    setRequestProperty("User-Agent", "WorkoraAndroidApp/1.0")
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                if (conn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    val arr = JSONArray(text)
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val display = obj.optString("display_name", "")
                        val shortName = display.split(",").take(3).joinToString(", ").trim()
                        if (shortName.isNotBlank() && !suggestions.contains(shortName)) {
                            suggestions.add(shortName)
                        }
                    }
                }
                conn.disconnect()
            } catch (_: Exception) {
            }

            withContext(Dispatchers.Main) {
                liveAreaSuggestions.clear()
                liveAreaSuggestions.addAll(suggestions.take(6))
                isFetchingAreaSuggestions = false
            }
        }
    }

    val liveAvailableJobs = remember {
        mutableStateListOf(
            WorkoraLabourJobItem(
                key = "job_sample_1",
                id = 101L,
                title = "House Repair & Wall Plastering",
                category = "Mason",
                description = "Boundary wall plastering and brickwork needed at Silwani.",
                dailyRate = 600,
                budgetType = "Per Day (₹/day)",
                location = currentRealLocation,
                preferredDate = "Tomorrow Morning",
                numberOfDays = "4",
                workersNeeded = 2,
                workTime = "9:00 AM – 6:00 PM",
                specialRequirement = "Bring trowel & level tools",
                about = "Near Main Market, Silwani",
                customerName = "Ramesh Verma",
                customerPhone = "+91 9876543210"
            ),
            WorkoraLabourJobItem(
                key = "job_sample_2",
                id = 102L,
                title = "Complete House Wiring & Fan Fitting",
                category = "Electrician",
                description = "2 rooms concealed wiring and switchboard installation.",
                dailyRate = 550,
                budgetType = "Per Day (₹/day)",
                location = currentRealLocation,
                preferredDate = "Today",
                numberOfDays = "2",
                workersNeeded = 1,
                workTime = "10:00 AM – 6:00 PM",
                specialRequirement = "Must have drill machine",
                about = "Ward No. 5, Silwani",
                customerPhone = "+91 9123456780",
                customerName = "Suresh Yadav"
            ),
            WorkoraLabourJobItem(
                key = "job_sample_3",
                id = 103L,
                title = "Water Tank & Bathroom Pipe Fitting",
                category = "Plumber",
                description = "1000L overhead tank installation and tap fitting.",
                dailyRate = 500,
                budgetType = "Per Day (₹/day)",
                location = currentRealLocation,
                preferredDate = "This Week",
                numberOfDays = "2",
                workersNeeded = 1,
                workTime = "9:00 AM – 5:00 PM",
                specialRequirement = "CPVC pipe fitting experience",
                about = "Bhopal Road, Silwani",
                customerName = "Vikash Singh",
                customerPhone = "+91 9654321098"
            )
        )
    }

    fun loadCustomerPostedJobsFromFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 0. Sync Admin App Branding Live from Firebase /app_branding
                val bConn = URL("$LABOUR_DB_URL/app_branding.json").openConnection() as HttpURLConnection
                if (bConn.responseCode in 200..299) {
                    val bResp = BufferedReader(InputStreamReader(bConn.inputStream)).use { it.readText() }
                    if (bResp.isNotBlank() && bResp != "null" && bResp.startsWith("{")) {
                        val bObj = JSONObject(bResp)
                        val cName = bObj.optString("app_name", liveAppName)
                        val cTag = bObj.optString("app_tagline", liveAppTagline)
                        brandingPrefs.edit().apply {
                            putString("app_name", cName)
                            putString("app_tagline", cTag)
                            apply()
                        }
                        withContext(Dispatchers.Main) {
                            liveAppName = cName
                            liveAppTagline = cTag
                        }
                    }
                }
                bConn.disconnect()

                // 1. Sync Jobs
                val conn = URL("$LABOUR_DB_URL/jobs.json").openConnection() as HttpURLConnection
                if (conn.responseCode in 200..299) {
                    val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                        val root = JSONObject(resp)
                        val keys = root.keys()
                        val loaded = mutableListOf<WorkoraLabourJobItem>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val title = obj.optString("title", "")
                            if (title.isNotBlank()) {
                                loaded.add(
                                    WorkoraLabourJobItem(
                                        key = k,
                                        id = obj.optLong("id", System.currentTimeMillis()),
                                        title = title,
                                        category = obj.optString("category", "Mason"),
                                        description = obj.optString("description", "Work needed"),
                                        dailyRate = obj.optInt("dailyRate", 500),
                                        budgetType = obj.optString("budgetType", "Per Day (₹/day)"),
                                        location = obj.optString("location", currentRealLocation),
                                        preferredDate = obj.optString("preferredDate", "Today"),
                                        numberOfDays = obj.optString("numberOfDays", "2"),
                                        workersNeeded = obj.optInt("workersNeeded", 1),
                                        workTime = obj.optString("workTime", "9:00 AM – 6:00 PM"),
                                        specialRequirement = obj.optString("specialRequirement", ""),
                                        about = obj.optString("about", ""),
                                        customerName = obj.optString("customerName", "Customer"),
                                        customerPhone = obj.optString("customerPhone", "+91 6265798340")
                                    )
                                )
                            }
                        }
                        if (loaded.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                loaded.sortedByDescending { it.id }.forEach { cj ->
                                    if (liveAvailableJobs.none { it.id == cj.id }) {
                                        liveAvailableJobs.add(0, cj)
                                    }
                                }
                            }
                        }
                    }
                }
                conn.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        loadCustomerPostedJobsFromFirebase()
    }

    val filteredJobs = liveAvailableJobs.filter { j ->
        val matchQuery = searchQuery.isBlank() ||
                j.title.contains(searchQuery, ignoreCase = true) ||
                j.category.contains(searchQuery, ignoreCase = true) ||
                j.location.contains(searchQuery, ignoreCase = true) ||
                j.description.contains(searchQuery, ignoreCase = true)
        val matchCat = selectedCategory.isNullOrBlank() ||
                selectedCategory.equals("All", ignoreCase = true) ||
                j.category.equals(selectedCategory, ignoreCase = true)
        matchQuery && matchCat
    }

    fun applyToCustomerJob(job: WorkoraLabourJobItem) {
        if (!appliedJobIds.contains(job.id)) {
            appliedJobIds.add(job.id)
            profilePrefs.edit()
                .putStringSet("applied_job_ids", appliedJobIds.map { it.toString() }.toSet())
                .apply()
        }
        val workerName = profilePrefs.getString("user_name", "Ankit Ahirwar") ?: "Ankit Ahirwar"
        val workerPhone = profilePrefs.getString("user_phone", "+91 6265798340") ?: "+91 6265798340"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appKey = "app_${System.currentTimeMillis()}"
                val conn = (URL("$LABOUR_DB_URL/job_applications/${job.key}/$appKey.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("jobId", job.id)
                    put("jobTitle", job.title)
                    put("workerName", workerName)
                    put("workerPhone", workerPhone)
                    put("workerSkill", availWorkCategory)
                    put("status", "INTERESTED")
                    put("timestamp", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {
            }
        }
        Toast.makeText(
            context,
            "Applied / Interested sent to ${job.customerName}! ✓",
            Toast.LENGTH_LONG
        ).show()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        when (bottomNavIndex) {
            0 -> {
                // ==================== 0: 🏠 HOME ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 96.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onSwitchRole() }
                        ) {
                            ExactLabourWLogo(customLogoBase64 = liveLogoBase64, size = 44.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = liveAppName,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = liveAppTagline,
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onOpenProfile) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = textDark,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                            IconButton(onClick = onOpenProfile) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu & Settings",
                                    tint = textDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            onClick = { showLocationModal = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, borderLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = brandOrange,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentRealLocation,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textDark
                                        )
                                        Text(
                                            text = "Tap to change work area",
                                            fontSize = 11.sp,
                                            color = textMuted
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = textMuted
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFE0F2FE), Color(0xFFDBEAFE))
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Post Your Work\nAvailability",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textDark,
                                            lineHeight = 23.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Let nearby customers find\nand hire you directly.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                availWorkArea = currentRealLocation
                                                bottomNavIndex = 2
                                            },
                                            shape = RoundedCornerShape(22.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                            modifier = Modifier.height(38.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = brandOrange,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Post Availability",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    ExactLabourBannerGraphic(size = 102.dp)
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Find Jobs by Category",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Text(
                                    text = "View All >",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = deepNavy,
                                    modifier = Modifier.clickable {
                                        onCategorySelected("All")
                                        bottomNavIndex = 1
                                    }
                                )
                            }

                            val categories = listOf("Mason", "Electrician", "Plumber", "Painter", "Carpenter", "More")
                            categories.chunked(3).forEach { rowCats ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowCats.forEach { catName ->
                                        Card(
                                            onClick = {
                                                onCategorySelected(if (catName == "More") "All" else catName)
                                                bottomNavIndex = 1
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(90.dp),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, borderLight),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                ExactLabourCategoryIconBox(category = catName)
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = catName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = textDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            onClick = { bottomNavIndex = 2 },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                            border = BorderStroke(1.dp, Color(0xFFE0F2FE)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(greenTrusted),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Verified & Trusted Worker Profile",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textDark
                                        )
                                        Text(
                                            text = "Tap to update your availability, rate & photos.",
                                            fontSize = 11.sp,
                                            color = textMuted
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = textMuted
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Available Jobs Near You (${filteredJobs.size})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Text(
                                    text = "Find Jobs >",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = deepNavy,
                                    modifier = Modifier.clickable { bottomNavIndex = 1 }
                                )
                            }

                            filteredJobs.take(4).forEach { job ->
                                val isApplied = appliedJobIds.contains(job.id)
                                LabourJobActionCard(
                                    job = job,
                                    isApplied = isApplied,
                                    onViewDetails = { viewingJobDetails = job },
                                    onApplyClick = { applyToCustomerJob(job) }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // ==================== 1: 🔎 FIND JOBS ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 96.dp)
                ) {
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
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = textDark
                                )
                            }
                            Text(
                                text = "Find Jobs",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textDark
                            )
                        }
                        Button(
                            onClick = {
                                availWorkArea = currentRealLocation
                                bottomNavIndex = 2
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post Availability", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            onClick = { showLocationModal = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, borderLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = deepNavy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = currentRealLocation,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = textDark
                                    )
                                }
                                Text(
                                    text = "Change",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = deepNavy
                                )
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Search jobs by work, skill or location...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = deepNavy,
                                unfocusedBorderColor = borderLight,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (listOf("All") + allCategories).forEach { cat ->
                                val isSelected = (cat == "All" && (selectedCategory == null || selectedCategory == "All")) ||
                                        selectedCategory.equals(cat, ignoreCase = true)
                                Button(
                                    onClick = { onCategorySelected(cat) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) deepNavy else Color.White
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) deepNavy else borderLight),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else textDark
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${filteredJobs.size} Jobs Found",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textDark
                            )
                            Text(
                                text = "Refresh ↻",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandOrange,
                                modifier = Modifier.clickable { loadCustomerPostedJobsFromFirebase() }
                            )
                        }

                        filteredJobs.forEach { job ->
                            val isApplied = appliedJobIds.contains(job.id)
                            LabourJobActionCard(
                                job = job,
                                isApplied = isApplied,
                                onViewDetails = { viewingJobDetails = job },
                                onApplyClick = { applyToCustomerJob(job) }
                            )
                        }
                    }
                }
            }

            2 -> {
                // ==================== 2: ➕ POST AVAILABILITY (FULL 12-POINT SCREEN) ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(deepNavy)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { bottomNavIndex = 0 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column {
                            Text(
                                text = "Post Work Availability",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Customers will see your card and hire you directly",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, borderLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "1. Work Category (आपका मुख्य काम)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    allCategories.forEach { cat ->
                                        val isSelected = availWorkCategory.equals(cat, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) deepNavy else Color(0xFFF1F5F9))
                                                .clickable { availWorkCategory = cat }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else textDark
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = availSkills,
                                onValueChange = { availSkills = it },
                                label = { Text("2. Skills (आप क्या-क्या काम जानते हैं)") },
                                placeholder = { Text("e.g. Plastering, Brickwork, Tile Fitting") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = availExperience,
                                onValueChange = { availExperience = it },
                                label = { Text("3. Experience (काम का अनुभव)") },
                                placeholder = { Text("e.g. 5 Years Experience") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "4. Work Photos (${availWorkPhotos.size}/5 Uploaded)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = textDark
                                    )
                                    if (availWorkPhotos.size < 5) {
                                        Text(
                                            text = "+ Add Photo",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = brandOrange,
                                            modifier = Modifier.clickable {
                                                workPhotoPicker.launch("image/*")
                                            }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    availWorkPhotos.forEachIndexed { idx, base64Pic ->
                                        val bmp = remember(base64Pic) { decodeLabourBase64Photo(base64Pic) }
                                        Box(
                                            modifier = Modifier
                                                .size(74.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF1F5F9))
                                        ) {
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp,
                                                    contentDescription = "Work Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(3.dp)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Black.copy(alpha = 0.65f))
                                                    .clickable { availWorkPhotos.removeAt(idx) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(13.dp))
                                            }
                                        }
                                    }

                                    if (availWorkPhotos.size < 5) {
                                        Box(
                                            modifier = Modifier
                                                .size(74.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFFF0DE))
                                                .clickable { workPhotoPicker.launch("image/*") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = brandOrange, modifier = Modifier.size(22.dp))
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text("Upload", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                                            }
                                        }
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = availWorkArea,
                                    onValueChange = {
                                        availWorkArea = it
                                        fetchLiveAreaSuggestions(it)
                                    },
                                    label = { Text("5. Work Area / Location (गाँव या शहर)") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = brandOrange) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                availWorkArea = currentRealLocation
                                                liveAreaSuggestions.clear()
                                            }
                                        ) {
                                            Icon(Icons.Default.MyLocation, contentDescription = "Current Location", tint = deepNavy)
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                if (isFetchingAreaSuggestions) {
                                    Text("Searching locations...", fontSize = 11.sp, color = deepNavy)
                                }

                                if (liveAreaSuggestions.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                                        border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            liveAreaSuggestions.forEach { suggestion ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            availWorkArea = suggestion
                                                            currentRealLocation = suggestion
                                                            profilePrefs.edit().putString("user_location", suggestion).apply()
                                                            liveAreaSuggestions.clear()
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = deepNavy, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(suggestion, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textDark)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "6. Maximum Distance (कितनी दूर तक काम कर सकते हैं)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("5 KM", "10 KM", "15 KM", "25 KM").forEach { dist ->
                                        val selected = availMaxDistance == dist
                                        OutlinedButton(
                                            onClick = { availMaxDistance = dist },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, if (selected) brandOrange else borderLight),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (selected) Color(0xFFFFF0DE) else Color.White
                                            ),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(
                                                text = dist,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) brandOrange else textDark
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = availDailyRate,
                                    onValueChange = { availDailyRate = it.filter { c -> c.isDigit() } },
                                    label = { Text("7. Daily Rate (₹/day)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = availFromDate,
                                    onValueChange = { availFromDate = it },
                                    label = { Text("8. Available From") },
                                    placeholder = { Text("Today / Tomorrow") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = availDays,
                                onValueChange = { availDays = it },
                                label = { Text("9. Available Days (किन दिनों में उपलब्ध हैं)") },
                                placeholder = { Text("e.g. All 7 Days / Mon to Sat") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "10. Team Size (अकेले या टीम के साथ)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Individual (Akela)", "2–3 Workers", "5+ Team").forEach { tSize ->
                                        val selected = availTeamSize == tSize
                                        OutlinedButton(
                                            onClick = { availTeamSize = tSize },
                                            modifier = Modifier.weight(1f).height(38.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, if (selected) deepNavy else borderLight),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (selected) Color(0xFFE0F2FE) else Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text(
                                                text = tSize,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) deepNavy else textDark,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = availShortDescription,
                                onValueChange = { availShortDescription = it },
                                label = { Text("11. Short Description (अपने काम का विवरण)") },
                                placeholder = { Text("Tell customers about your work quality and tools...") },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    val workerName = profilePrefs.getString("user_name", "Ankit Ahirwar") ?: "Ankit Ahirwar"
                                    val workerPhone = profilePrefs.getString("user_phone", "+91 6265798340") ?: "+91 6265798340"
                                    val rateInt = availDailyRate.toIntOrNull() ?: 600
                                    val finalArea = availWorkArea.trim().ifBlank { currentRealLocation }
                                    val profilePic = availWorkPhotos.firstOrNull()
                                        ?: (profilePrefs.getString("profile_photo_base64", "") ?: "")

                                    profilePrefs.edit().apply {
                                        putString("user_skill", availWorkCategory)
                                        putString("user_rate", rateInt.toString())
                                        putString("user_location", finalArea)
                                        apply()
                                    }

                                    isPostingAvailability = true
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val cleanDigits = workerPhone.filter { it.isDigit() }.takeLast(10).ifBlank { "6265798340" }
                                            val conn = (URL("$LABOUR_DB_URL/workers/w_$cleanDigits.json").openConnection() as HttpURLConnection).apply {
                                                requestMethod = "PUT"
                                                setRequestProperty("Content-Type", "application/json")
                                                doOutput = true
                                            }
                                            val json = JSONObject().apply {
                                                put("name", workerName)
                                                put("trade", availWorkCategory)
                                                put("skills", availSkills.trim())
                                                put("experience", availExperience.trim())
                                                put("dailyWage", rateInt)
                                                put("location", finalArea)
                                                put("maxDistance", availMaxDistance)
                                                put("availableFrom", availFromDate.trim())
                                                put("availableDays", availDays.trim())
                                                put("teamSize", availTeamSize)
                                                put("description", availShortDescription.trim().ifBlank { "Verified $availWorkCategory ($availExperience)" })
                                                put("phone", workerPhone)
                                                put("photoBase64", profilePic)
                                                put("rating", 4.9)
                                                put("reviewsCount", 14)
                                                put("distanceKm", 2)
                                                put("isAvailableToday", true)
                                                put("isVerified", true)
                                                put("updatedAt", System.currentTimeMillis())
                                            }
                                            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                            conn.responseCode
                                            conn.disconnect()
                                        } catch (_: Exception) {
                                        }

                                        withContext(Dispatchers.Main) {
                                            isPostingAvailability = false
                                            bottomNavIndex = 3
                                            Toast.makeText(
                                                context,
                                                "Availability Posted Live! Customers can now view & hire you ✓",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                enabled = !isPostingAvailability,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                            ) {
                                if (isPostingAvailability) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "12. Post Availability ✓",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // ==================== 3: 📋 MY WORK (WORKER ACTIVITY TAB) ====================
                val myAppliedJobs = liveAvailableJobs.filter { appliedJobIds.contains(it.id) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 96.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(deepNavy)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { bottomNavIndex = 0 }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Column {
                                Text(
                                    text = "My Work",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Your active availability & applied jobs",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                availWorkArea = currentRealLocation
                                bottomNavIndex = 2
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.2.dp, greenTrusted)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "My Posted Availability",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = deepNavy
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = greenTrusted
                                        )
                                    }
                                }
                                Text(
                                    text = "$availWorkCategory • ₹$availDailyRate/day • $availExperience",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark
                                )
                                Text(
                                    text = "📍 $availWorkArea (Max $availMaxDistance) • $availTeamSize",
                                    fontSize = 12.sp,
                                    color = textMuted
                                )
                            }
                        }

                        Text(
                            text = "Applied / Interested Jobs (${myAppliedJobs.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textDark
                        )

                        if (myAppliedJobs.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, borderLight)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No applied jobs yet.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textDark
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { bottomNavIndex = 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = deepNavy)
                                    ) {
                                        Text("Find Jobs Near You", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            myAppliedJobs.forEach { job ->
                                LabourJobActionCard(
                                    job = job,
                                    isApplied = true,
                                    onViewDetails = { viewingJobDetails = job },
                                    onApplyClick = { onOpenChat() }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Workora Message Pill Button
        if (bottomNavIndex != 2) {
            Button(
                onClick = onOpenChat,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = deepNavy),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 78.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "Workora Message",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Workora Message",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // ==================== 5-ICON LABOUR BOTTOM NAVIGATION BAR ====================
        // 🏠 Home | 🔎 Find Jobs | ➕ Post Availability (Prominent Orange) | 📋 My Work | 👤 Profile
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { bottomNavIndex = 0 }
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (bottomNavIndex == 0) deepNavy else textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Home",
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 0) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 0) deepNavy else textMuted,
                        maxLines = 1
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { bottomNavIndex = 1 }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Find Jobs",
                        tint = if (bottomNavIndex == 1) deepNavy else textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Find Jobs",
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 1) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 1) deepNavy else textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable {
                            availWorkArea = currentRealLocation
                            bottomNavIndex = 2
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(brandOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Post Availability",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Post Availability",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (bottomNavIndex == 2) deepNavy else brandOrange,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { bottomNavIndex = 3 }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "My Work",
                        tint = if (bottomNavIndex == 3) deepNavy else textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "My Work",
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 3) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 3) deepNavy else textMuted,
                        maxLines = 1
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenProfile() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Profile",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = textMuted,
                        maxLines = 1
                    )
                }
            }
        }
    }

    if (viewingJobDetails != null) {
        val j = viewingJobDetails!!
        val isApplied = appliedJobIds.contains(j.id)
        AlertDialog(
            onDismissRequest = { viewingJobDetails = null },
            containerColor = Color.White,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(j.title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                        Text("${j.category} • Posted by ${j.customerName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                    }
                    IconButton(onClick = { viewingJobDetails = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Rate / Budget: ₹${j.dailyRate} (${j.budgetType})", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = deepNavy)
                    Text("• Location: ${j.location}", fontSize = 12.sp, color = textDark, fontWeight = FontWeight.SemiBold)
                    Text("• Preferred Date: ${j.preferredDate} (${j.numberOfDays} Days)", fontSize = 12.sp, color = textDark)
                    Text("• Workers Needed: ${j.workersNeeded} Worker(s) • Time: ${j.workTime}", fontSize = 12.sp, color = textDark)
                    if (j.specialRequirement.isNotBlank()) {
                        Text("• Special Requirement: ${j.specialRequirement}", fontSize = 12.sp, color = brandOrange, fontWeight = FontWeight.Bold)
                    }
                    if (j.about.isNotBlank()) {
                        Text("• About Site: ${j.about}", fontSize = 12.sp, color = textMuted)
                    }
                    Text("• Work Details: ${j.description}", fontSize = 12.sp, color = textMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        applyToCustomerJob(j)
                        viewingJobDetails = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApplied) greenTrusted else brandOrange
                    )
                ) {
                    Text(
                        text = if (isApplied) "Applied ✓" else "Apply / Interested ✓",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${j.customerPhone}")))
                        } catch (_: Exception) {
                        }
                    }
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = deepNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", color = deepNavy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showLocationModal) {
        val locations = listOf(
            "Silwani, Raisen (MP)",
            "Raisen, Madhya Pradesh",
            "Begamganj, Raisen (MP)",
            "Gairatganj, Raisen (MP)",
            "Bareli, Raisen (MP)",
            "Bhopal, Madhya Pradesh"
        )
        AlertDialog(
            onDismissRequest = { showLocationModal = false },
            title = { Text("Select Work Area", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = locationSearchInput,
                        onValueChange = {
                            locationSearchInput = it
                            fetchLiveAreaSuggestions(it)
                        },
                        placeholder = { Text("Type village or city...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (locationSearchInput.isNotBlank()) {
                        Button(
                            onClick = {
                                currentRealLocation = locationSearchInput.trim()
                                availWorkArea = currentRealLocation
                                profilePrefs.edit().putString("user_location", currentRealLocation).apply()
                                showLocationModal = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set '${locationSearchInput.trim()}'", color = Color.White)
                        }
                    }
                    (liveAreaSuggestions + locations).distinct().take(7).forEach { loc ->
                        Text(
                            text = "📍 $loc",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentRealLocation = loc
                                    availWorkArea = loc
                                    profilePrefs.edit().putString("user_location", loc).apply()
                                    showLocationModal = false
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun LabourJobActionCard(
    job: WorkoraLabourJobItem,
    isApplied: Boolean,
    onViewDetails: () -> Unit,
    onApplyClick: () -> Unit
) {
    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val textDark = Color(0xFF102A43)
    val textMuted = Color(0xFF667085)
    val greenTrusted = Color(0xFF22A06B)

    Card(
        onClick = onViewDetails,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isApplied) greenTrusted else Color(0xFFE5E7EB)),
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
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = deepNavy,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = job.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${job.category} • ${job.workersNeeded} Worker(s) • ${job.numberOfDays} Days",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = deepNavy
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = brandOrange,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${job.location} • ${job.preferredDate}",
                                fontSize = 11.sp,
                                color = textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${job.dailyRate}/day",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = deepNavy
                    )
                    Text(
                        text = job.workTime,
                        fontSize = 10.sp,
                        color = textMuted
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, deepNavy),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "View Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = deepNavy
                    )
                }

                Button(
                    onClick = onApplyClick,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApplied) greenTrusted else brandOrange
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isApplied) "Interested Sent ✓" else "Apply / Interested",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
