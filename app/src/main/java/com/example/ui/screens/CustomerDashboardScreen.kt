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
import androidx.compose.material.icons.filled.Star
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
import com.example.model.JobPost
import com.example.model.Worker
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

private const val CUSTOMER_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

data class ExactWorkerItem(
    val id: String = "",
    val name: String,
    val trade: String,
    val skills: String = "",
    val experience: String = "5 Years Exp.",
    val rating: Double,
    val reviewsCount: Int,
    val distanceKm: Int,
    val maxDistanceKm: String = "15 KM",
    val dailyWage: Int,
    val area: String = "Silwani, Raisen (MP)",
    val availability: String = "Available Today",
    val availableFrom: String = "Immediately",
    val availableDays: String = "All Days",
    val teamSize: String = "Individual (1 Worker)",
    val shortDescription: String = "Experienced and verified professional worker.",
    val phone: String = "+91 9876543210",
    val photoBase64: String = "",
    val shirtColor: Color = Color(0xFF083D91)
)

data class CustomerMyJobEntry(
    val key: String,
    val id: Long,
    val title: String,
    val category: String,
    val description: String,
    val dailyRate: Int,
    val budgetType: String,
    val location: String,
    val preferredDate: String,
    val numberOfDays: String,
    val workersNeeded: Int,
    val workTime: String,
    val status: String
)

private fun decodeWorkerPhoto(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun encodeJobPhotoUri(context: Context, uri: Uri): String {
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
private fun ExactWorkoraWLogo(size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
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

@Composable
private fun ExactBannerWorkerGraphic(size: Dp = 105.dp) {
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
private fun ExactCategoryIconBox(category: String) {
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

@Composable
private fun ExactWorkerAvatar(
    photoBase64: String = "",
    shirtColor: Color,
    showYellowHelmet: Boolean,
    size: Dp = 56.dp
) {
    val decodedBitmap = remember(photoBase64) { decodeWorkerPhoto(photoBase64) }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
        if (decodedBitmap != null) {
            Image(
                bitmap = decodedBitmap,
                contentDescription = "Worker Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                drawArc(
                    color = shirtColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.14f, h * 0.62f),
                    size = Size(w * 0.72f, h * 0.54f)
                )
                drawRect(
                    color = Color(0xFFE0A96D),
                    topLeft = Offset(w * 0.42f, h * 0.52f),
                    size = Size(w * 0.16f, h * 0.14f)
                )
                drawCircle(
                    color = Color(0xFFF1C27D),
                    radius = w * 0.20f,
                    center = Offset(w * 0.50f, h * 0.40f)
                )
                if (showYellowHelmet) {
                    drawArc(
                        color = Color(0xFFFACC15),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.28f, h * 0.14f),
                        size = Size(w * 0.44f, h * 0.32f)
                    )
                } else {
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.30f, h * 0.17f),
                        size = Size(w * 0.40f, h * 0.26f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
    toastMessage: String? = null,
    onOpenChat: () -> Unit = {},
    onPostJobClick: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val textDark = Color(0xFF102A43)
    val textMuted = Color(0xFF667085)
    val borderLight = Color(0xFFE5E7EB)
    val greenTrusted = Color(0xFF22A06B)

    var currentRealLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
    }
    var showLocationModal by remember { mutableStateOf(false) }
    var locationSearchInput by remember { mutableStateOf("") }

    // 5-Icon Bottom Navigation State:
    // 0 = 🏠 Home | 1 = 🔍 Find Workers | 2 = ➕ Post Job | 3 = 📋 My Jobs | 4 = 👤 Profile
    var bottomNavIndex by remember { mutableIntStateOf(0) }

    var viewingWorkerProfile by remember { mutableStateOf<ExactWorkerItem?>(null) }

    // ==================== CUSTOMER - POST A JOB (ALL 13 FIELDS) ====================
    var jobWorkName by remember { mutableStateOf("") }
    var jobCategory by remember { mutableStateOf("Mason") }
    var jobDescription by remember { mutableStateOf("") }
    val jobWorkPhotos = remember { mutableStateListOf<String>() }
    var jobWorkLocation by remember { mutableStateOf(currentRealLocation) }
    val liveLocationSuggestions = remember { mutableStateListOf<String>() }
    var isFetchingLocationSuggestions by remember { mutableStateOf(false) }
    var jobPreferredDate by remember { mutableStateOf("Tomorrow Morning") }
    var jobNumberOfDays by remember { mutableStateOf("3") }
    var jobNumberOfWorkers by remember { mutableStateOf("2") }
    var jobRateOrBudget by remember { mutableStateOf("600") }
    var jobBudgetType by remember { mutableStateOf("Per Day (₹/day)") }
    var jobWorkTime by remember { mutableStateOf("9:00 AM – 6:00 PM") }
    var jobSpecialRequirement by remember { mutableStateOf("") }
    var jobAbout by remember { mutableStateOf("") }
    var isPostingJobToCloud by remember { mutableStateOf(false) }

    val postJobCategories = listOf(
        "Mason", "Labour", "Painter", "Electrician", "Plumber",
        "Carpenter", "Cleaner", "Farm Worker", "Tile Worker", "Other"
    )

    val myPostedJobsList = remember {
        mutableStateListOf(
            CustomerMyJobEntry(
                key = "my_job_1",
                id = 101L,
                title = "House Repair & Wall Plastering",
                category = "Mason",
                description = "Boundary wall plastering and brickwork needed at Silwani.",
                dailyRate = 600,
                budgetType = "Per Day (₹/day)",
                location = currentRealLocation,
                preferredDate = "Tomorrow Morning",
                numberOfDays = "3",
                workersNeeded = 2,
                workTime = "9:00 AM – 6:00 PM",
                status = "ACTIVE"
            )
        )
    }

    val jobPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && jobWorkPhotos.size < 5) {
            val encoded = encodeJobPhotoUri(context, uri)
            if (encoded.isNotBlank()) {
                jobWorkPhotos.add(encoded)
                Toast.makeText(context, "Photo ${jobWorkPhotos.size}/5 added ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun fetchLiveLocationSuggestions(query: String) {
        val clean = query.trim()
        if (clean.length < 2) {
            liveLocationSuggestions.clear()
            return
        }
        isFetchingLocationSuggestions = true
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
                liveLocationSuggestions.clear()
                liveLocationSuggestions.addAll(suggestions.take(6))
                isFetchingLocationSuggestions = false
            }
        }
    }

    val exactWorkers = remember {
        mutableStateListOf(
            ExactWorkerItem(
                id = "w_ramesh",
                name = "Ramesh Kumar",
                trade = "Mason",
                skills = "Brickwork, Plastering, RCC",
                experience = "6 Years Exp.",
                rating = 4.8,
                reviewsCount = 12,
                distanceKm = 2,
                maxDistanceKm = "15 KM",
                dailyWage = 600,
                area = "Silwani, Raisen (MP)",
                availability = "Available Today",
                availableFrom = "Today",
                availableDays = "All 7 Days",
                teamSize = "Team of 3 Workers",
                shortDescription = "Expert Rajmistri for house construction, plastering and tile work.",
                phone = "+91 9876543210",
                shirtColor = Color(0xFF083D91)
            ),
            ExactWorkerItem(
                id = "w_suresh",
                name = "Suresh Patel",
                trade = "Electrician",
                skills = "House Wiring, Inverter, Motor",
                experience = "5 Years Exp.",
                rating = 4.6,
                reviewsCount = 8,
                distanceKm = 3,
                maxDistanceKm = "20 KM",
                dailyWage = 550,
                area = "Silwani, Raisen (MP)",
                availability = "Available Today",
                availableFrom = "Today",
                availableDays = "Mon – Sat",
                teamSize = "Individual (1 Worker)",
                shortDescription = "Complete domestic & shop wiring, MCB box and ceiling fan fitting.",
                phone = "+91 9123456780",
                shirtColor = Color(0xFF334155)
            ),
            ExactWorkerItem(
                id = "w_amit",
                name = "Amit Yadav",
                trade = "Plumber",
                skills = "Pipe Fitting, Water Tank, Motor",
                experience = "4 Years Exp.",
                rating = 4.7,
                reviewsCount = 15,
                distanceKm = 4,
                maxDistanceKm = "15 KM",
                dailyWage = 500,
                area = "Silwani, Raisen (MP)",
                availability = "Available Today",
                availableFrom = "Immediately",
                availableDays = "All Days",
                teamSize = "Individual (1 Worker)",
                shortDescription = "Fast bathroom fitting, pipeline leakage repair and tank installation.",
                phone = "+91 9988776655",
                shirtColor = Color(0xFF0F766E)
            )
        )
    }

    fun loadLiveWorkersAndMyJobsFromFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = URL("$CUSTOMER_DB_URL/workers.json").openConnection() as HttpURLConnection
                if (conn.responseCode in 200..299) {
                    val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                        val root = JSONObject(resp)
                        val keys = root.keys()
                        val cloudList = mutableListOf<ExactWorkerItem>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val name = obj.optString("name", "")
                            if (name.isNotBlank()) {
                                cloudList.add(
                                    ExactWorkerItem(
                                        id = k,
                                        name = name,
                                        trade = obj.optString("trade", "Mason"),
                                        skills = obj.optString("skills", obj.optString("trade", "Skilled Work")),
                                        experience = obj.optString("experience", "4 Years Exp."),
                                        rating = obj.optDouble("rating", 4.8),
                                        reviewsCount = obj.optInt("reviewsCount", 10),
                                        distanceKm = obj.optInt("distanceKm", 2),
                                        maxDistanceKm = obj.optString("maxDistance", "15 KM"),
                                        dailyWage = obj.optInt("dailyWage", 600),
                                        area = obj.optString("location", currentRealLocation),
                                        availability = if (obj.optBoolean("isAvailableToday", true)) "Available Today" else "Busy",
                                        availableFrom = obj.optString("availableFrom", "Today"),
                                        availableDays = obj.optString("availableDays", "All Days"),
                                        teamSize = obj.optString("teamSize", "Individual"),
                                        shortDescription = obj.optString("description", "Verified Workora professional."),
                                        phone = obj.optString("phone", "+91 9876543210"),
                                        photoBase64 = obj.optString("photoBase64", ""),
                                        shirtColor = deepNavy
                                    )
                                )
                            }
                        }
                        if (cloudList.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                cloudList.forEach { cw ->
                                    if (exactWorkers.none { it.name.equals(cw.name, ignoreCase = true) }) {
                                        exactWorkers.add(0, cw)
                                    }
                                }
                            }
                        }
                    }
                }
                conn.disconnect()

                val jConn = URL("$CUSTOMER_DB_URL/jobs.json").openConnection() as HttpURLConnection
                if (jConn.responseCode in 200..299) {
                    val jResp = BufferedReader(InputStreamReader(jConn.inputStream)).use { it.readText() }
                    if (jResp.isNotBlank() && jResp != "null" && jResp.startsWith("{")) {
                        val root = JSONObject(jResp)
                        val keys = root.keys()
                        val loadedJobs = mutableListOf<CustomerMyJobEntry>()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val title = obj.optString("title", "")
                            if (title.isNotBlank()) {
                                loadedJobs.add(
                                    CustomerMyJobEntry(
                                        key = k,
                                        id = obj.optLong("id", System.currentTimeMillis()),
                                        title = title,
                                        category = obj.optString("category", "Mason"),
                                        description = obj.optString("description", ""),
                                        dailyRate = obj.optInt("dailyRate", 600),
                                        budgetType = obj.optString("budgetType", "Per Day (₹/day)"),
                                        location = obj.optString("location", currentRealLocation),
                                        preferredDate = obj.optString("preferredDate", "Today"),
                                        numberOfDays = obj.optString("numberOfDays", "2"),
                                        workersNeeded = obj.optInt("workersNeeded", 1),
                                        workTime = obj.optString("workTime", "9:00 AM – 6:00 PM"),
                                        status = obj.optString("status", "ACTIVE")
                                    )
                                )
                            }
                        }
                        if (loadedJobs.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                loadedJobs.sortedByDescending { it.id }.forEach { cj ->
                                    if (myPostedJobsList.none { it.id == cj.id }) {
                                        myPostedJobsList.add(0, cj)
                                    }
                                }
                            }
                        }
                    }
                }
                jConn.disconnect()
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        loadLiveWorkersAndMyJobsFromFirebase()
    }

    val filteredWorkers = exactWorkers.filter { w ->
        val matchQuery = searchQuery.isBlank() ||
                w.name.contains(searchQuery, ignoreCase = true) ||
                w.trade.contains(searchQuery, ignoreCase = true) ||
                w.skills.contains(searchQuery, ignoreCase = true) ||
                w.area.contains(searchQuery, ignoreCase = true)
        val matchCat = selectedCategory.isNullOrBlank() ||
                selectedCategory.equals("All", ignoreCase = true) ||
                w.trade.equals(selectedCategory, ignoreCase = true)
        matchQuery && matchCat
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
                            ExactWorkoraWLogo(size = 44.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "WORKORA",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Find & Hire Skilled Labour",
                                    fontSize = 11.sp,
                                    color = textMuted
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onOpenNotifications) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = textDark,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                            // Menu / Settings inside Profile
                            IconButton(
                                onClick = {
                                    onOpenProfile()
                                    onNavigateToProfile()
                                }
                            ) {
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
                                            text = "Tap to change location",
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
                                            text = "Find Skilled\nWorkers Near You",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textDark,
                                            lineHeight = 23.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Get your work done easily\nand safely.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))

                                        Button(
                                            onClick = {
                                                jobWorkLocation = currentRealLocation
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
                                                text = "Post Job",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    ExactBannerWorkerGraphic(size = 102.dp)
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
                                    text = "Popular Categories",
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
                                                ExactCategoryIconBox(category = catName)
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
                            onClick = { bottomNavIndex = 1 },
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
                                            text = "Verified & Trusted Workers",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textDark
                                        )
                                        Text(
                                            text = "All workers are verified for your safety.",
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
                                    text = "Available Workers",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Text(
                                    text = "Find Workers >",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = deepNavy,
                                    modifier = Modifier.clickable { bottomNavIndex = 1 }
                                )
                            }

                            filteredWorkers.take(3).forEachIndexed { idx, worker ->
                                CustomerWorkerRichCard(
                                    worker = worker,
                                    showHelmet = idx == 0,
                                    onViewProfile = { viewingWorkerProfile = worker },
                                    onHireClick = {
                                        jobCategory = worker.trade
                                        jobRateOrBudget = worker.dailyWage.toString()
                                        jobWorkLocation = currentRealLocation
                                        jobWorkName = "Hire ${worker.name} (${worker.trade})"
                                        bottomNavIndex = 2
                                    }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // ==================== 1: 🔍 FIND WORKERS ====================
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
                                text = "Find Workers",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textDark
                            )
                        }
                        Button(
                            onClick = {
                                jobWorkLocation = currentRealLocation
                                bottomNavIndex = 2
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post Job", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                            onValueChange = onSearchQueryChanged,
                            placeholder = {
                                Text(
                                    text = "Search by name, skill or service...",
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
                            (listOf("All") + postJobCategories).forEach { cat ->
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
                                text = "${filteredWorkers.size} Workers Found",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textDark
                            )
                            Text(
                                text = "Refresh ↻",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = brandOrange,
                                modifier = Modifier.clickable { loadLiveWorkersAndMyJobsFromFirebase() }
                            )
                        }

                        filteredWorkers.forEachIndexed { index, worker ->
                            CustomerWorkerRichCard(
                                worker = worker,
                                showHelmet = index % 2 == 0,
                                onViewProfile = { viewingWorkerProfile = worker },
                                onHireClick = {
                                    jobCategory = worker.trade
                                    jobRateOrBudget = worker.dailyWage.toString()
                                    jobWorkLocation = currentRealLocation
                                    jobWorkName = "Hire ${worker.name} (${worker.trade})"
                                    bottomNavIndex = 2
                                }
                            )
                        }
                    }
                }
            }

            2 -> {
                // ==================== 2: ➕ POST JOB (FULL 13-POINT SCREEN) ====================
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
                                text = "Post a Job",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Fill work details so nearby workers can apply",
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
                            OutlinedTextField(
                                value = jobWorkName,
                                onValueChange = { jobWorkName = it },
                                label = { Text("1. Work Name (काम का नाम)") },
                                placeholder = { Text("e.g. House Wall Plastering / Wiring Work") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "2. Select Work Category (श्रेणी चुनें)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    postJobCategories.forEach { cat ->
                                        val isSelected = jobCategory.equals(cat, ignoreCase = true)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(if (isSelected) deepNavy else Color(0xFFF1F5F9))
                                                .clickable { jobCategory = cat }
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
                                value = jobDescription,
                                onValueChange = { jobDescription = it },
                                label = { Text("3. Work Description (काम के बारे में जानकारी)") },
                                placeholder = { Text("Describe what work needs to be done...") },
                                minLines = 3,
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
                                        text = "4. Work Photos (${jobWorkPhotos.size}/5 Uploaded)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = textDark
                                    )
                                    if (jobWorkPhotos.size < 5) {
                                        Text(
                                            text = "+ Add Photo",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = brandOrange,
                                            modifier = Modifier.clickable {
                                                jobPhotoPicker.launch("image/*")
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
                                    jobWorkPhotos.forEachIndexed { idx, base64Pic ->
                                        val bmp = remember(base64Pic) { decodeWorkerPhoto(base64Pic) }
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
                                                    .clickable { jobWorkPhotos.removeAt(idx) },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(13.dp))
                                            }
                                        }
                                    }

                                    if (jobWorkPhotos.size < 5) {
                                        Box(
                                            modifier = Modifier
                                                .size(74.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFFF0DE))
                                                .clickable { jobPhotoPicker.launch("image/*") },
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
                                    value = jobWorkLocation,
                                    onValueChange = {
                                        jobWorkLocation = it
                                        fetchLiveLocationSuggestions(it)
                                    },
                                    label = { Text("5. Work Location (Area / Village / City)") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = brandOrange) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                jobWorkLocation = currentRealLocation
                                                liveLocationSuggestions.clear()
                                            }
                                        ) {
                                            Icon(Icons.Default.MyLocation, contentDescription = "Use Current Location", tint = deepNavy)
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                if (isFetchingLocationSuggestions) {
                                    Text("Searching live locations...", fontSize = 11.sp, color = deepNavy)
                                }

                                if (liveLocationSuggestions.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                                        border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            liveLocationSuggestions.forEach { suggestion ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            jobWorkLocation = suggestion
                                                            currentRealLocation = suggestion
                                                            profilePrefs.edit().putString("user_location", suggestion).apply()
                                                            liveLocationSuggestions.clear()
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = jobPreferredDate,
                                    onValueChange = { jobPreferredDate = it },
                                    label = { Text("6. Preferred Date") },
                                    placeholder = { Text("e.g. 28 Sep / Today") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = jobNumberOfDays,
                                    onValueChange = { jobNumberOfDays = it.filter { c -> c.isDigit() } },
                                    label = { Text("7. No. of Days") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = jobNumberOfWorkers,
                                    onValueChange = { jobNumberOfWorkers = it.filter { c -> c.isDigit() } },
                                    label = { Text("8. Workers Needed") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.9f),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = jobRateOrBudget,
                                    onValueChange = { jobRateOrBudget = it.filter { c -> c.isDigit() } },
                                    label = { Text("9. Rate / Budget (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Per Day (₹/day)", "Total Budget (कुल बजट)").forEach { bType ->
                                    val selected = jobBudgetType == bType
                                    OutlinedButton(
                                        onClick = { jobBudgetType = bType },
                                        modifier = Modifier.weight(1f).height(36.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, if (selected) brandOrange else borderLight),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (selected) Color(0xFFFFF0DE) else Color.White
                                        ),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = bType,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) brandOrange else textDark
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = jobWorkTime,
                                onValueChange = { jobWorkTime = it },
                                label = { Text("10. Work Time (काम का समय)") },
                                placeholder = { Text("e.g. 9:00 AM to 6:00 PM") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = jobSpecialRequirement,
                                onValueChange = { jobSpecialRequirement = it },
                                label = { Text("11. Special Requirement (कोई विशेष आवश्यकता)") },
                                placeholder = { Text("e.g. Must bring own tools / helmet") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            OutlinedTextField(
                                value = jobAbout,
                                onValueChange = { jobAbout = it },
                                label = { Text("12. About Site / Customer Note") },
                                placeholder = { Text("Landmark, house details or extra note...") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Button(
                                onClick = {
                                    val cleanTitle = jobWorkName.trim().ifBlank { "$jobCategory Work Needed" }
                                    val wageInt = jobRateOrBudget.toIntOrNull() ?: 600
                                    val workersCount = jobNumberOfWorkers.toIntOrNull() ?: 1
                                    val daysCount = jobNumberOfDays.ifBlank { "1" }
                                    val finalLoc = jobWorkLocation.trim().ifBlank { currentRealLocation }
                                    val customerName = profilePrefs.getString("user_name", "Ankit Ahirwar") ?: "Ankit Ahirwar"
                                    val customerPhone = profilePrefs.getString("user_phone", "+91 6265798340") ?: "+91 6265798340"

                                    val fullCombinedDesc = buildString {
                                        append(jobDescription.trim().ifBlank { "$cleanTitle at $finalLoc" })
                                        append(" | Days: $daysCount | Workers: $workersCount | Time: $jobWorkTime | Type: $jobBudgetType")
                                        if (jobSpecialRequirement.isNotBlank()) append(" | Special: ${jobSpecialRequirement.trim()}")
                                        if (jobAbout.isNotBlank()) append(" | About: ${jobAbout.trim()}")
                                    }

                                    isPostingJobToCloud = true
                                    onPostJob(
                                        cleanTitle,
                                        jobCategory,
                                        fullCombinedDesc,
                                        wageInt,
                                        finalLoc,
                                        workersCount,
                                        "$jobPreferredDate ($daysCount Days)",
                                        jobWorkTime
                                    )

                                    val newJobId = System.currentTimeMillis()
                                    myPostedJobsList.add(
                                        0,
                                        CustomerMyJobEntry(
                                            key = "job_$newJobId",
                                            id = newJobId,
                                            title = cleanTitle,
                                            category = jobCategory,
                                            description = fullCombinedDesc,
                                            dailyRate = wageInt,
                                            budgetType = jobBudgetType,
                                            location = finalLoc,
                                            preferredDate = jobPreferredDate,
                                            numberOfDays = daysCount,
                                            workersNeeded = workersCount,
                                            workTime = jobWorkTime,
                                            status = "ACTIVE"
                                        )
                                    )

                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val conn = (URL("$CUSTOMER_DB_URL/jobs/job_$newJobId.json").openConnection() as HttpURLConnection).apply {
                                                requestMethod = "PUT"
                                                setRequestProperty("Content-Type", "application/json")
                                                doOutput = true
                                            }
                                            val json = JSONObject().apply {
                                                put("id", newJobId)
                                                put("title", cleanTitle)
                                                put("category", jobCategory)
                                                put("description", fullCombinedDesc)
                                                put("dailyRate", wageInt)
                                                put("budgetType", jobBudgetType)
                                                put("location", finalLoc)
                                                put("preferredDate", jobPreferredDate)
                                                put("numberOfDays", daysCount)
                                                put("workersNeeded", workersCount)
                                                put("workTime", jobWorkTime)
                                                put("specialRequirement", jobSpecialRequirement.trim())
                                                put("about", jobAbout.trim())
                                                put("photosCount", jobWorkPhotos.size)
                                                put("customerName", customerName)
                                                put("customerPhone", customerPhone)
                                                put("urgency", "$jobPreferredDate • $daysCount Days")
                                                put("status", "OPEN")
                                            }
                                            OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                            conn.responseCode
                                            conn.disconnect()
                                        } catch (_: Exception) {
                                        }

                                        withContext(Dispatchers.Main) {
                                            isPostingJobToCloud = false
                                            jobWorkName = ""
                                            jobDescription = ""
                                            jobSpecialRequirement = ""
                                            jobAbout = ""
                                            jobWorkPhotos.clear()
                                            bottomNavIndex = 3 // Go directly to 📋 My Jobs after posting
                                            Toast.makeText(
                                                context,
                                                "Job Posted Live! Added to My Jobs ✓",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                enabled = !isPostingJobToCloud,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                            ) {
                                if (isPostingJobToCloud) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "13. Post Job ✓",
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
                // ==================== 3: 📋 MY JOBS (ACTIVITY TAB) ====================
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
                                    text = "My Jobs",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Track your posted requirements & worker responses",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                jobWorkLocation = currentRealLocation
                                bottomNavIndex = 2
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Post Job", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        myPostedJobsList.forEach { myJob ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, borderLight),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = myJob.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = textDark
                                            )
                                            Text(
                                                text = "${myJob.category} • ${myJob.workersNeeded} Worker(s) • ${myJob.numberOfDays} Days",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = deepNavy
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = myJob.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = greenTrusted
                                            )
                                        }
                                    }

                                    Text(
                                        text = "📍 ${myJob.location} • 🗓️ ${myJob.preferredDate} (${myJob.workTime})",
                                        fontSize = 12.sp,
                                        color = textMuted
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "₹${myJob.dailyRate} (${myJob.budgetType})",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = brandOrange
                                        )

                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = onOpenChat,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, deepNavy),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deepNavy)
                                            }
                                            Button(
                                                onClick = {
                                                    onCompleteJob(myJob.id)
                                                    Toast.makeText(context, "Job marked Completed ✓", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = greenTrusted),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                modifier = Modifier.height(34.dp)
                                            ) {
                                                Text("Complete ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

        // Floating Online Live Chat Pill Button
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
                    contentDescription = "Online Live Chat",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Online Live Chat",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // ==================== 5-ICON CUSTOMER BOTTOM NAVIGATION BAR ====================
        // 🏠 Home | 🔍 Find Workers | ➕ Post Job (Prominent Orange) | 📋 My Jobs | 👤 Profile
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
                // 1. 🏠 Home
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

                // 2. 🔍 Find Workers
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { bottomNavIndex = 1 }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Find Workers",
                        tint = if (bottomNavIndex == 1) deepNavy else textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Find Workers",
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 1) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 1) deepNavy else textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 3. ➕ Post Job (Prominent Orange Center Button)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1.1f)
                        .clickable {
                            jobWorkLocation = currentRealLocation
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
                            contentDescription = "Post Job",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Post Job",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (bottomNavIndex == 2) deepNavy else brandOrange,
                        maxLines = 1
                    )
                }

                // 4. 📋 My Jobs
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { bottomNavIndex = 3 }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "My Jobs",
                        tint = if (bottomNavIndex == 3) deepNavy else textMuted,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "My Jobs",
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 3) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (bottomNavIndex == 3) deepNavy else textMuted,
                        maxLines = 1
                    )
                }

                // 5. 👤 Profile
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onOpenProfile()
                            onNavigateToProfile()
                        }
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

    // Worker Full Profile Dialog
    if (viewingWorkerProfile != null) {
        val w = viewingWorkerProfile!!
        AlertDialog(
            onDismissRequest = { viewingWorkerProfile = null },
            containerColor = Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ExactWorkerAvatar(
                            photoBase64 = w.photoBase64,
                            shirtColor = w.shirtColor,
                            showYellowHelmet = true,
                            size = 50.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(w.name, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                            Text("${w.trade} • ${w.experience}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                        }
                    }
                    IconButton(onClick = { viewingWorkerProfile = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Skills: ${w.skills}", fontSize = 13.sp, color = textDark, fontWeight = FontWeight.SemiBold)
                    Text("• Daily Rate: ₹${w.dailyWage}/day", fontSize = 13.sp, color = deepNavy, fontWeight = FontWeight.ExtraBold)
                    Text("• Area / Location: ${w.area} (Max ${w.maxDistanceKm})", fontSize = 12.sp, color = textMuted)
                    Text("• Rating: ★ ${w.rating} (${w.reviewsCount} verified reviews)", fontSize = 12.sp, color = textDark)
                    Text("• Availability: ${w.availability} (From: ${w.availableFrom}, ${w.availableDays})", fontSize = 12.sp, color = greenTrusted, fontWeight = FontWeight.Bold)
                    Text("• Team Size: ${w.teamSize}", fontSize = 12.sp, color = textDark)
                    Text("• About: ${w.shortDescription}", fontSize = 12.sp, color = textMuted)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val selected = w
                        viewingWorkerProfile = null
                        jobCategory = selected.trade
                        jobRateOrBudget = selected.dailyWage.toString()
                        jobWorkLocation = currentRealLocation
                        jobWorkName = "Hire ${selected.name} (${selected.trade})"
                        bottomNavIndex = 2
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange)
                ) {
                    Text("Hire Now", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        try {
                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${w.phone}")))
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

    // Location Picker Dialog
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
            title = { Text("Select Location", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = locationSearchInput,
                        onValueChange = {
                            locationSearchInput = it
                            fetchLiveLocationSuggestions(it)
                        },
                        placeholder = { Text("Type village or city...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (locationSearchInput.isNotBlank()) {
                        Button(
                            onClick = {
                                currentRealLocation = locationSearchInput.trim()
                                jobWorkLocation = currentRealLocation
                                profilePrefs.edit().putString("user_location", currentRealLocation).apply()
                                showLocationModal = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set '${locationSearchInput.trim()}'", color = Color.White)
                        }
                    }
                    (liveLocationSuggestions + locations).distinct().take(7).forEach { loc ->
                        Text(
                            text = "📍 $loc",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentRealLocation = loc
                                    jobWorkLocation = loc
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
private fun CustomerWorkerRichCard(
    worker: ExactWorkerItem,
    showHelmet: Boolean,
    onViewProfile: () -> Unit,
    onHireClick: () -> Unit
) {
    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val textDark = Color(0xFF102A43)
    val textMuted = Color(0xFF667085)

    Card(
        onClick = onViewProfile,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
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
                    ExactWorkerAvatar(
                        photoBase64 = worker.photoBase64,
                        shirtColor = worker.shirtColor,
                        showYellowHelmet = showHelmet,
                        size = 56.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = worker.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${worker.trade} • ${worker.experience}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = deepNavy
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${worker.rating}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${worker.reviewsCount} reviews) • ${worker.teamSize}",
                                fontSize = 11.sp,
                                color = textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = brandOrange,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${worker.area} • ${worker.distanceKm} km away",
                                fontSize = 11.sp,
                                color = textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = worker.availability,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF15803D)
                        )
                    }
                    Text(
                        text = "₹${worker.dailyWage}/day",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = deepNavy
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, deepNavy),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "View Profile",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = deepNavy
                    )
                }

                Button(
                    onClick = onHireClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Hire",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
