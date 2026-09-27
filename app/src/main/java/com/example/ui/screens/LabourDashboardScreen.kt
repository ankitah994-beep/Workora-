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
import androidx.compose.ui.text.TextStyle
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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

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

private fun translateLabourCategory(cat: String, lang: String): String {
    if (lang != "Hindi") return cat
    return when (cat) {
        "All" -> "सभी (All)"
        "Mason" -> "राजमिस्त्री"
        "Electrician" -> "इलेक्ट्रीशियन"
        "Plumber" -> "प्लंबर"
        "Painter" -> "पेंटर"
        "Carpenter" -> "बढ़ई"
        "Labour" -> "मज़दूर"
        "Cleaner" -> "सफाईकर्मी"
        "Farm Worker" -> "कृषि मज़दूर"
        "Tile Worker" -> "टाइल्स मिस्त्री"
        "More" -> "अन्य"
        "Other" -> "अन्य"
        else -> cat
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
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    // Sync Global Theme Mode (Default = "System")
    LaunchedEffect(Unit) {
        WorkoraThemeManager.syncFromPrefs(context)
    }
    val isDark = WorkoraThemeManager.isDark(context)
    val bgColor = WorkoraThemeManager.bgColor(context)
    val cardColor = WorkoraThemeManager.surfaceColor(context)
    val subtleBgColor = WorkoraThemeManager.subtleSurfaceColor(context)
    val textDark = WorkoraThemeManager.textPrimary(context)
    val textMuted = WorkoraThemeManager.textSecondary(context)
    val borderLight = WorkoraThemeManager.borderColor(context)
    val accentBlue = WorkoraThemeManager.accentBlue(context)

    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val greenTrusted = Color(0xFF22A06B)

    // Live Language Reader
    val appLang = settingsPrefs.getString("app_language", "English") ?: "English"
    fun tr(hi: String, en: String): String = if (appLang == "Hindi") hi else en

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
            .background(bgColor)
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
                    // Top Header (3-Lines Menu Icon Removed + Global Theme Synced!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardColor)
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
                                    text = tr("रोज़ाना काम पाएं और कमाएं", liveAppTagline),
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }
                        }

                        IconButton(onClick = onOpenProfile) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = textDark,
                                modifier = Modifier.size(24.dp)
                            )
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
                            colors = CardDefaults.cardColors(containerColor = cardColor),
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
                                            text = tr(
                                                "काम का क्षेत्र बदलने के लिए टैप करें (Live GPS & Search)",
                                                "Tap to change work area (Live GPS & Search)"
                                            ),
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
                                            colors = if (isDark) {
                                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                            } else {
                                                listOf(Color(0xFFE0F2FE), Color(0xFFDBEAFE))
                                            }
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
                                            text = tr("अपने पास रोज़ाना\nकाम खोजें", "Find Daily Work\nNear You"),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textDark,
                                            lineHeight = 23.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = tr(
                                                "सीधे ग्राहकों से जुड़ें और\nरोज़ाना दिहाड़ी पाएं।",
                                                "Connect directly with nearby\ncustomers & get daily wages."
                                            ),
                                            fontSize = 12.sp,
                                            color = textMuted,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = { bottomNavIndex = 1 },
                                            shape = RoundedCornerShape(22.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = deepNavy),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                            modifier = Modifier.height(38.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = tr("काम खोजें", "Explore Jobs"),
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
                                    text = tr("श्रेणी के अनुसार काम खोजें", "Find Jobs by Category"),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Text(
                                    text = tr("सभी देखें >", "View All >"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentBlue,
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
                                            colors = CardDefaults.cardColors(containerColor = cardColor),
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
                                                    text = translateLabourCategory(catName, appLang),
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
                            onClick = onOpenProfile,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFF0F9FF)
                            ),
                            border = BorderStroke(1.dp, if (isDark) borderLight else Color(0xFFE0F2FE)),
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
                                            text = tr("सत्यापित और भरोसेमंद कारीगर प्रोफाइल", "Verified & Trusted Worker Profile"),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textDark
                                        )
                                        Text(
                                            text = tr(
                                                "आपकी प्रोफाइल सक्रिय है और ग्राहकों को दिख रही है।",
                                                "Your profile is active and visible to customers."
                                            ),
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
                                    text = tr("आपके पास उपलब्ध काम (${filteredJobs.size})", "Available Jobs Near You (${filteredJobs.size})"),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Text(
                                    text = tr("काम खोजें >", "Find Jobs >"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentBlue,
                                    modifier = Modifier.clickable { bottomNavIndex = 1 }
                                )
                            }

                            filteredJobs.take(4).forEach { job ->
                                val isApplied = appliedJobIds.contains(job.id)
                                LabourJobActionCard(
                                    job = job,
                                    appLang = appLang,
                                    cardColor = cardColor,
                                    borderColor = borderLight,
                                    textDark = textDark,
                                    textMuted = textMuted,
                                    accentBlue = accentBlue,
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
                            .background(cardColor)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { bottomNavIndex = 0 }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = textDark
                            )
                        }
                        Text(
                            text = tr("काम खोजें", "Find Jobs"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textDark
                        )
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
                            colors = CardDefaults.cardColors(containerColor = cardColor),
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
                                        tint = accentBlue,
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
                                    text = tr("बदलें", "Change"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentBlue
                                )
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                            placeholder = {
                                Text(
                                    text = tr("काम, हुनर या लोकेशन से खोजें...", "Search jobs by work, skill or location..."),
                                    fontSize = 13.sp,
                                    color = textMuted
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
                                focusedBorderColor = accentBlue,
                                unfocusedBorderColor = borderLight,
                                focusedContainerColor = cardColor,
                                unfocusedContainerColor = cardColor
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
                                        containerColor = if (isSelected) deepNavy else cardColor
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) deepNavy else borderLight),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = translateLabourCategory(cat, appLang),
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
                                text = tr("${filteredJobs.size} काम मिले", "${filteredJobs.size} Jobs Found"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textDark
                            )
                            Text(
                                text = tr("रिफ्रेश ↻", "Refresh ↻"),
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
                                appLang = appLang,
                                cardColor = cardColor,
                                borderColor = borderLight,
                                textDark = textDark,
                                textMuted = textMuted,
                                accentBlue = accentBlue,
                                isApplied = isApplied,
                                onViewDetails = { viewingJobDetails = job },
                                onApplyClick = { applyToCustomerJob(job) }
                            )
                        }
                    }
                }
            }

            2 -> {
                // ==================== 2: ➕ POST AVAILABILITY ====================
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
                            .background(if (isDark) Color(0xFF0F172A) else deepNavy)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { bottomNavIndex = 0 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column {
                            Text(
                                text = tr("काम की उपलब्धता पोस्ट करें", "Post Work Availability"),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = tr(
                                    "ग्राहक आपका कार्ड देखकर सीधे आपको काम पर रख सकेंगे",
                                    "Customers will see your card and hire you directly"
                                ),
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
                        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                                                .background(if (isSelected) deepNavy else subtleBgColor)
                                                .clickable { availWorkCategory = cat }
                                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = translateLabourCategory(cat, appLang),
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
                                textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                                label = { Text("2. Skills (आप क्या-क्या काम जानते हैं)") },
                                placeholder = { Text("e.g. Plastering, Brickwork, Tile Fitting") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = availExperience,
                                onValueChange = { availExperience = it },
                                textStyle = TextStyle(color = textDark, fontSize = 14.sp),
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
                                                .background(subtleBgColor)
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
                                                .background(brandOrange.copy(alpha = 0.15f))
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

                            LiveLocationAutoCompleteField(
                                value = availWorkArea,
                                onValueChange = {
                                    availWorkArea = it
                                    if (it.length > 3) {
                                        currentRealLocation = it
                                        profilePrefs.edit().putString("user_location", it).apply()
                                    }
                                },
                                label = "5. Work Area / Location (Live Auto-Suggest)"
                            )

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
                                                containerColor = if (selected) brandOrange.copy(alpha = 0.15f) else cardColor
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
                                    textStyle = TextStyle(color = textDark, fontSize = 14.sp),
                                    label = { Text("7. Daily Rate (₹/day)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = availFromDate,
                                    onValueChange = { availFromDate = it },
                                    textStyle = TextStyle(color = textDark, fontSize = 14.sp),
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
                                textStyle = TextStyle(color = textDark, fontSize = 14.sp),
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
                                            border = BorderStroke(1.dp, if (selected) accentBlue else borderLight),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (selected) accentBlue.copy(alpha = 0.15f) else cardColor
                                            ),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                        ) {
                                            Text(
                                                text = tSize,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) accentBlue else textDark,
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
                                textStyle = TextStyle(color = textDark, fontSize = 14.sp),
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

                                    currentRealLocation = finalArea
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
                                        text = tr("12. उपलब्धता पोस्ट करें ✓", "12. Post Availability ✓"),
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
                // ==================== 3: 📋 MY WORK ====================
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
                            .background(if (isDark) Color(0xFF0F172A) else deepNavy)
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { bottomNavIndex = 0 }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Column {
                            Text(
                                text = tr("मेरा काम (My Work)", "My Work"),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = tr(
                                    "आपकी सक्रिय उपलब्धता और अप्लाई किए गए काम",
                                    "Your active availability & applied jobs"
                                ),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
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
                            colors = CardDefaults.cardColors(containerColor = cardColor),
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
                                        text = tr("मेरी पोस्ट की गई उपलब्धता", "My Posted Availability"),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentBlue
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tr("सक्रिय", "ACTIVE"),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = greenTrusted
                                        )
                                    }
                                }
                                Text(
                                    text = "${translateLabourCategory(availWorkCategory, appLang)} • ₹$availDailyRate/day • $availExperience",
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
                            text = tr("अप्लाई किए गए काम (${myAppliedJobs.size})", "Applied / Interested Jobs (${myAppliedJobs.size})"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textDark
                        )

                        if (myAppliedJobs.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardColor),
                                border = BorderStroke(1.dp, borderLight)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = tr("अभी तक किसी काम के लिए अप्लाई नहीं किया है।", "No applied jobs yet."),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textDark
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { bottomNavIndex = 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = deepNavy)
                                    ) {
                                        Text(tr("पास के काम खोजें", "Find Jobs Near You"), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            myAppliedJobs.forEach { job ->
                                LabourJobActionCard(
                                    job = job,
                                    appLang = appLang,
                                    cardColor = cardColor,
                                    borderColor = borderLight,
                                    textDark = textDark,
                                    textMuted = textMuted,
                                    accentBlue = accentBlue,
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
                    text = tr("Workora मैसेज", "Workora Message"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // ==================== 5-ICON LABOUR BOTTOM NAVIGATION BAR ====================
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
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
                        tint = if (bottomNavIndex == 0) accentBlue else textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tr("होम", "Home"),
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 0) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 0) accentBlue else textMuted,
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
                        tint = if (bottomNavIndex == 1) accentBlue else textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tr("काम खोजें", "Find Jobs"),
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 1) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 1) accentBlue else textMuted,
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
                        text = tr("उपलब्धता डालें", "Post Availability"),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (bottomNavIndex == 2) accentBlue else brandOrange,
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
                        tint = if (bottomNavIndex == 3) accentBlue else textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tr("मेरा काम", "My Work"),
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 3) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 3) accentBlue else textMuted,
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
                        text = tr("प्रोफाइल", "Profile"),
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
            containerColor = cardColor,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(j.title, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                        Text("${translateLabourCategory(j.category, appLang)} • Posted by ${j.customerName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                    }
                    IconButton(onClick = { viewingJobDetails = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = textDark)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Rate / Budget: ₹${j.dailyRate} (${j.budgetType})", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = accentBlue)
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
                        text = if (isApplied) tr("अप्लाई हो गया ✓", "Applied ✓") else tr("अप्लाई करें ✓", "Apply / Interested ✓"),
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
                    Icon(Icons.Default.Phone, contentDescription = null, tint = accentBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(tr("कॉल करें", "Call"), color = accentBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showLocationModal) {
        WorkoraLiveLocationModal(
            currentLocation = currentRealLocation,
            onDismiss = { showLocationModal = false },
            onLocationSelected = { selectedLoc ->
                currentRealLocation = selectedLoc
                availWorkArea = selectedLoc
                profilePrefs.edit().putString("user_location", selectedLoc).apply()
                showLocationModal = false
                Toast.makeText(context, "Work Area set to $selectedLoc ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun LabourJobActionCard(
    job: WorkoraLabourJobItem,
    appLang: String,
    cardColor: Color,
    borderColor: Color,
    textDark: Color,
    textMuted: Color,
    accentBlue: Color,
    isApplied: Boolean,
    onViewDetails: () -> Unit,
    onApplyClick: () -> Unit
) {
    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val greenTrusted = Color(0xFF22A06B)
    val isHindi = appLang == "Hindi"

    Card(
        onClick = onViewDetails,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, if (isApplied) greenTrusted else borderColor),
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
                            text = "${translateLabourCategory(job.category, appLang)} • ${job.workersNeeded} Worker(s) • ${job.numberOfDays} Days",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentBlue
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
                        text = if (isHindi) "₹${job.dailyRate}/दिन" else "₹${job.dailyRate}/day",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentBlue
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
                    border = BorderStroke(1.2.dp, accentBlue),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isHindi) "जानकारी देखें" else "View Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentBlue
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
                        text = if (isApplied) {
                            if (isHindi) "रुचि भेजी गई ✓" else "Interested Sent ✓"
                        } else {
                            if (isHindi) "अप्लाई करें / रुचि दिखाएं" else "Apply / Interested"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
