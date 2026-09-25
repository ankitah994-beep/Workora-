package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JobApplication
import com.example.model.JobPost
import com.example.ui.theme.WorkoraOrange

private data class ExactLabourCardItem(
    val name: String,
    val trade: String,
    val rating: Double,
    val reviewsCount: Int,
    val distanceKm: Int,
    val dailyWage: Int,
    val shirtColor: Color
)

// 1. Top-Left Round Orange Circle with Blue 'W' Logo
@Composable
private fun ExactLabourWLogo(size: Dp = 44.dp) {
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
                color = Color(0xFFF59E0B),
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
                color = Color(0xFF1E3A8A),
                style = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
            )
        }
    }
}

// 2. Skilled Worker Illustration on Banner (Yellow Hard Hat, Blue Overalls & Wrench)
@Composable
private fun ExactLabourBannerGraphic(size: Dp = 105.dp) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Wrench in right hand
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

        // Light blue shirt
        drawRoundRect(
            color = Color(0xFF2563EB),
            topLeft = Offset(w * 0.26f, h * 0.54f),
            size = Size(w * 0.54f, h * 0.38f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )

        // Dark Navy Overalls
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

        // Neck & Face
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

        // Eyes & Smile
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

        // Yellow Safety Hard Hat
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

// 3. Exact Category Icons (Mason Trowel, Electrician Bolt, Plumber Tap, Painter Roller, Carpenter Hammer, More Grid)
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
            val w = size.width
            val h = size.height
            when (category) {
                "Mason" -> {
                    val blade = Path().apply {
                        moveTo(w * 0.75f, h * 0.20f)
                        lineTo(w * 0.30f, h * 0.45f)
                        lineTo(w * 0.55f, h * 0.70f)
                        close()
                    }
                    drawPath(blade, color = Color(0xFF1D4ED8))
                    drawLine(
                        color = Color(0xFF1E3A8A),
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
                    drawPath(bolt, color = Color(0xFFF59E0B))
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
                    drawCircle(Color(0xFF1E3A8A), radius = r, center = Offset(w * 0.32f, h * 0.32f))
                    drawCircle(Color(0xFF1E3A8A), radius = r, center = Offset(w * 0.68f, h * 0.32f))
                    drawCircle(Color(0xFF1E3A8A), radius = r, center = Offset(w * 0.32f, h * 0.68f))
                    drawCircle(Color(0xFF1E3A8A), radius = r, center = Offset(w * 0.68f, h * 0.68f))
                }
            }
        }
    }
}

// 4. Worker Circular Portrait Avatar
@Composable
private fun ExactLabourAvatar(
    shirtColor: Color,
    showYellowHelmet: Boolean,
    size: Dp = 54.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
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
    toastMessage: String? = null
) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    var currentRealLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
    }
    var showLocationModal by remember { mutableStateOf(false) }
    var locationSearchInput by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // 0 = Left Screen of Reference Image (Home), 1 = Right Screen of Reference Image (Available Workers / Search)
    var bottomNavIndex by remember { mutableIntStateOf(0) }

    // Post New Requirement Dialog State
    var showPostDialog by remember { mutableStateOf(false) }
    var postTitle by remember { mutableStateOf("House Repair Work") }
    var postCategory by remember { mutableStateOf("Mason") }
    var postWage by remember { mutableStateOf("500") }

    val exactWorkers = remember {
        mutableStateListOf(
            ExactLabourCardItem(
                name = "Ramesh Kumar",
                trade = "Mason",
                rating = 4.8,
                reviewsCount = 12,
                distanceKm = 2,
                dailyWage = 600,
                shirtColor = Color(0xFF1E3A8A)
            ),
            ExactLabourCardItem(
                name = "Suresh Patel",
                trade = "Electrician",
                rating = 4.6,
                reviewsCount = 8,
                distanceKm = 3,
                dailyWage = 550,
                shirtColor = Color(0xFF334155)
            ),
            ExactLabourCardItem(
                name = "Amit Yadav",
                trade = "Plumber",
                rating = 4.7,
                reviewsCount = 15,
                distanceKm = 4,
                dailyWage = 500,
                shirtColor = Color(0xFF0F766E)
            )
        )
    }

    val filteredWorkers = exactWorkers.filter { w ->
        val matchQuery = searchQuery.isBlank() ||
                w.name.contains(searchQuery, ignoreCase = true) ||
                w.trade.contains(searchQuery, ignoreCase = true)
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
        if (bottomNavIndex == 0) {
            // ==================== LEFT SCREEN OF REFERENCE IMAGE (HOME) ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 84.dp)
            ) {
                // 1. Top Bar: Logo + WORKORA + Bell + Hamburger Menu
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
                        ExactLabourWLogo(size = 44.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WORKORA",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Find & Hire Skilled Labour",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenProfile) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF1E293B),
                                modifier = Modifier.size(23.dp)
                            )
                        }
                        IconButton(onClick = onSwitchRole) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color(0xFF1E293B),
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
                    // 2. Location Card: Silwani, Raisen (MP) / Tap to change location >
                    Card(
                        onClick = { showLocationModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentRealLocation,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "Tap to change location",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    // 3. Banner Card: Find Skilled Workers Near You + Post New Requirement
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
                                        color = Color(0xFF0F172A),
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
                                        onClick = { showPostDialog = true },
                                        shape = RoundedCornerShape(22.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316)),
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
                                                tint = Color(0xFFF97316),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Post New Requirement",
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

                    // 4. Popular Categories Header + 3x2 Grid
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
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "View All >",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
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
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                                color = Color(0xFF0F172A)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Verified & Trusted Workers Card
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
                                        .background(Color(0xFF16A34A)),
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
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "All workers are verified for your safety.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF64748B)
                            )
                        }
                    }

                    // 6. Recent Jobs Section (Single Card - Exact Match to Reference Image)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Jobs",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "View All >",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A),
                                modifier = Modifier.clickable { bottomNavIndex = 1 }
                            )
                        }

                        Card(
                            onClick = { bottomNavIndex = 1 },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0F2FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = null,
                                            tint = Color(0xFF1D4ED8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = "House Repair Work",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = currentRealLocation,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Text(
                                            text = "Posted 2 hours ago",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹500/day",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ==================== RIGHT SCREEN OF REFERENCE IMAGE (AVAILABLE WORKERS) ====================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 84.dp)
            ) {
                // 1. Top Bar: <- Available Workers + Search Icon
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
                                tint = Color(0xFF0F172A)
                            )
                        }
                        Text(
                            text = "Available Workers",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF0F172A)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 2. Location Pill: Silwani, Raisen (MP) + Change
                    Card(
                        onClick = { showLocationModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                    tint = Color(0xFF1D4ED8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentRealLocation,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                            }
                            Text(
                                text = "Change",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }

                    // 3. Search Bar: Search by name, skill or service...
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
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
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE2E8F0),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // 4. Category Chips: All, Mason, Electrician, Plumber
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Mason", "Electrician", "Plumber").forEach { cat ->
                            val isSelected = (cat == "All" && (selectedCategory == null || selectedCategory == "All")) ||
                                    selectedCategory.equals(cat, ignoreCase = true)
                            Button(
                                onClick = { onCategorySelected(cat) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF1E3A8A) else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF0F172A)
                                )
                            }
                        }
                    }

                    // 5. "3 Workers Found" + "Sort by v"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${filteredWorkers.size} Workers Found",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sort by",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // 6. Clean Worker Cards (Exact match to Right Screen - No extra buttons inside!)
                    filteredWorkers.forEachIndexed { index, worker ->
                        Card(
                            onClick = {
                                Toast.makeText(context, "Selected ${worker.name} (${worker.trade})", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ExactLabourAvatar(
                                        shirtColor = worker.shirtColor,
                                        showYellowHelmet = index == 0,
                                        size = 54.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = worker.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = worker.trade,
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
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
                                                color = Color(0xFF0F172A)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "(${worker.reviewsCount} reviews)",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${worker.distanceKm} km away",
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE0F2FE), shape = RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Available",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0284C7)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "₹${worker.dailyWage}/day",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== BOTTOM NAVIGATION BAR (HOME, SEARCH, POST, PROFILE) ====================
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { bottomNavIndex = 0 }
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (bottomNavIndex == 0) Color(0xFF1E3A8A) else Color(0xFF64748B),
                        modifier = Modifier.size(23.dp)
                    )
                    Text(
                        text = "Home",
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (bottomNavIndex == 0) Color(0xFF1E3A8A) else Color(0xFF64748B)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { bottomNavIndex = 1 }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (bottomNavIndex == 1) Color(0xFF1E3A8A) else Color(0xFF64748B),
                        modifier = Modifier.size(23.dp)
                    )
                    Text(
                        text = "Search",
                        fontSize = 10.sp,
                        fontWeight = if (bottomNavIndex == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (bottomNavIndex == 1) Color(0xFF1E3A8A) else Color(0xFF64748B)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { showPostDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF94A3B8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Post",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Post",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onOpenProfile() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(23.dp)
                    )
                    Text(
                        text = "Profile",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }

    // Simple Location Picker Dialog
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
                        onValueChange = { locationSearchInput = it },
                        placeholder = { Text("Type village or city...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (locationSearchInput.isNotBlank()) {
                        Button(
                            onClick = {
                                currentRealLocation = locationSearchInput.trim()
                                profilePrefs.edit().putString("user_location", currentRealLocation).apply()
                                showLocationModal = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Set '${locationSearchInput.trim()}'")
                        }
                    }
                    locations.forEach { loc ->
                        Text(
                            text = loc,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentRealLocation = loc
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

    // Simple Post Requirement Dialog
    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("Post New Requirement", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = postTitle,
                        onValueChange = { postTitle = it },
                        label = { Text("Work Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = postCategory,
                        onValueChange = { postCategory = it },
                        label = { Text("Category (Mason, Electrician...)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = postWage,
                        onValueChange = { postWage = it },
                        label = { Text("Daily Wage (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPostDialog = false
                        Toast.makeText(context, "Requirement Posted ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) {
                    Text("Post", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
