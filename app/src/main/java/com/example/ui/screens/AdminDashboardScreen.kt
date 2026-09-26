package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Strict Workora Design System Colors
private val AdminPrimaryNavy = Color(0xFF083D91)
private val AdminAccentOrange = Color(0xFFFF8C00)
private val AdminBgLight = Color(0xFFF8FAFC)
private val AdminWhite = Color(0xFFFFFFFF)
private val AdminMainText = Color(0xFF0B2345)
private val AdminSecondaryText = Color(0xFF687280)
private val AdminBorder = Color(0xFFE5EAF0)
private val AdminSuccessGreen = Color(0xFF16A34A)
private val AdminSuccessBg = Color(0xFFDCFCE7)
private val AdminWarningOrange = Color(0xFFD97706)
private val AdminWarningBg = Color(0xFFFEF3C7)
private val AdminDangerRed = Color(0xFFDC2626)
private val AdminDangerBg = Color(0xFFFEE2E2)
private val AdminInfoBlue = Color(0xFF1D4ED8)
private val AdminInfoBg = Color(0xFFDBEAFE)
private val AdminNeutralGrayBg = Color(0xFFF1F5F9)

private const val ADMIN_FB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

private enum class AdminRoute(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Home),
    USERS("Users", Icons.Outlined.Person),
    CUSTOMERS("Customers", Icons.Outlined.PersonOutline),
    WORKERS("Workers", Icons.Outlined.Construction),
    BOOKINGS("Bookings", Icons.Outlined.EventAvailable),
    CATEGORIES("Categories", Icons.Outlined.GridView),
    REPORTS("Reports", Icons.Outlined.Flag),
    SETTINGS("Settings", Icons.Outlined.Settings)
}

private data class AdminUserRecord(
    val key: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String,
    val area: String,
    val status: String,
    val joined: String,
    val category: String = "Electrician",
    val experience: String = "4 yrs",
    val dailyRate: Int = 650,
    val availability: String = "Available",
    val rating: Double = 4.8,
    val bookingsCount: Int = 4
)

private data class AdminBookingRecord(
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val workerName: String,
    val workerPhone: String,
    val category: String,
    val area: String,
    val date: String,
    val preferredTime: String,
    val days: Int,
    val rate: Int,
    val description: String,
    val additionalMessage: String,
    val status: String,
    val createdDate: String,
    val updatedDate: String
)

private data class AdminCategoryRecord(
    val id: String,
    val name: String,
    val iconName: String,
    val active: Boolean,
    val workerCount: Int
)

@Composable
fun AdminDashboardScreen(
    adminEmail: String = "ankitah994@gmail.com",
    adminTier: String = "SUPER_ADMIN",
    onLogoutAdmin: () -> Unit = {},
    onSwitchRoleFromAdmin: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Automatically switch mobile into Horizontal (Landscape) mode for Admin Panel
    DisposableEffect(Unit) {
        val previousOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = previousOrientation
        }
    }

    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val brandingPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    var loggedAdminEmail by remember {
        mutableStateOf(adminEmail.ifBlank { authPrefs.getString("last_logged_in_email", "ankitah994@gmail.com") ?: "ankitah994@gmail.com" })
    }
    var isAdminAuthenticated by remember {
        val savedRole = authPrefs.getString("saved_user_role", "ADMIN")
        val validAdmin = loggedAdminEmail.equals("ankitah994@gmail.com", ignoreCase = true) ||
            loggedAdminEmail.contains("admin", ignoreCase = true) ||
            savedRole == "ADMIN"
        mutableStateOf(validAdmin && loggedAdminEmail.isNotBlank())
    }
    var showAccessDeniedBanner by remember { mutableStateOf(!isAdminAuthenticated) }

    // Helper to switch directly to Customer or Worker Mode from Top-Right Admin Menu
    val switchAdminToRole: (String) -> Unit = { targetRole ->
        authPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("saved_user_role", targetRole)
            .apply()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        if (onSwitchRoleFromAdmin != null) {
            onSwitchRoleFromAdmin(targetRole)
        } else {
            activity?.recreate()
        }
    }

    if (!isAdminAuthenticated) {
        AdminLoginScreen(
            showAccessDenied = showAccessDeniedBanner,
            onLoginSuccess = { verifiedEmail ->
                loggedAdminEmail = verifiedEmail
                showAccessDeniedBanner = false
                isAdminAuthenticated = true
                authPrefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("last_logged_in_email", verifiedEmail)
                    .putString("saved_user_role", "ADMIN")
                    .apply()
            },
            onBackToApp = onLogoutAdmin
        )
        return
    }

    var currentRoute by remember { mutableStateOf(AdminRoute.DASHBOARD) }
    var isSidebarVisible by remember { mutableStateOf(true) }
    var globalSearchQuery by remember { mutableStateOf("") }
    var isLoadingData by remember { mutableStateOf(true) }
    var errorBannerMessage by remember { mutableStateOf<String?>(null) }

    val usersList = remember { mutableStateListOf<AdminUserRecord>() }
    val bookingsList = remember { mutableStateListOf<AdminBookingRecord>() }
    val categoriesList = remember { mutableStateListOf<AdminCategoryRecord>() }

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var selectedUserForDetail by remember { mutableStateOf<AdminUserRecord?>(null) }
    var selectedUserForEdit by remember { mutableStateOf<AdminUserRecord?>(null) }
    var userPendingBlockToggle by remember { mutableStateOf<AdminUserRecord?>(null) }
    var selectedBookingDetail by remember { mutableStateOf<AdminBookingRecord?>(null) }
    var bookingStatusConfirmPair by remember { mutableStateOf<Pair<AdminBookingRecord, String>?>(null) }
    var showAddCategoryModal by remember { mutableStateOf(false) }
    var categoryForEdit by remember { mutableStateOf<AdminCategoryRecord?>(null) }

    val refreshAdminDatabase: () -> Unit = {
        isLoadingData = true
        errorBannerMessage = null
        loadAdminDataFromFirebase(
            onLoaded = { loadedUsers, loadedBookings, loadedCategories ->
                usersList.clear()
                usersList.addAll(loadedUsers)
                bookingsList.clear()
                bookingsList.addAll(loadedBookings)
                categoriesList.clear()
                categoriesList.addAll(loadedCategories)
                isLoadingData = false
            },
            onError = { msg ->
                errorBannerMessage = msg
                isLoadingData = false
            }
        )
    }

    LaunchedEffect(Unit) {
        refreshAdminDatabase()
    }

    // Safe Drawing + Auto-Fit Desktop Viewport Scaling
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminPrimaryNavy)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val baseDensity = LocalDensity.current
        val targetVirtualWidthDp = 1280f
        val actualWidthDp = maxWidth.value.coerceAtLeast(360f)
        val scaleFactor = (actualWidthDp / targetVirtualWidthDp).coerceIn(0.32f, 1.25f)

        val scaledDensity = remember(baseDensity, scaleFactor) {
            Density(
                density = baseDensity.density * scaleFactor,
                fontScale = baseDensity.fontScale
            )
        }

        CompositionLocalProvider(LocalDensity provides scaledDensity) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AdminBgLight)
            ) {
                if (isSidebarVisible) {
                    AdminSidebarContent(
                        currentRoute = currentRoute,
                        onSelectRoute = { currentRoute = it },
                        onLogoutClick = { showLogoutConfirmDialog = true },
                        modifier = Modifier
                            .width(235.dp)
                            .fillMaxHeight()
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(AdminBgLight)
                ) {
                    AdminTopBar(
                        searchQuery = globalSearchQuery,
                        onSearchChange = { globalSearchQuery = it },
                        adminName = "Admin",
                        onToggleSidebar = { isSidebarVisible = !isSidebarVisible },
                        onRefreshClick = refreshAdminDatabase,
                        onSwitchToCustomer = { switchAdminToRole("CUSTOMER") },
                        onSwitchToWorker = { switchAdminToRole("LABOUR") },
                        onOpenSettings = { currentRoute = AdminRoute.SETTINGS },
                        onLogoutClick = { showLogoutConfirmDialog = true }
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        if (isLoadingData) {
                            LinearProgressIndicator(
                                color = AdminAccentOrange,
                                trackColor = AdminBorder,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (!errorBannerMessage.isNullOrBlank()) {
                            Surface(
                                color = AdminWarningBg,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = errorBannerMessage!!,
                                    color = AdminWarningOrange,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        when (currentRoute) {
                            AdminRoute.DASHBOARD -> {
                                AdminDashboardOverviewPage(
                                    usersList = usersList,
                                    bookingsList = bookingsList,
                                    globalSearchQuery = globalSearchQuery,
                                    onNavigateRoute = { currentRoute = it },
                                    onViewUser = { selectedUserForDetail = it },
                                    onConfirmBlockUser = { userPendingBlockToggle = it },
                                    onViewBooking = { selectedBookingDetail = it }
                                )
                            }
                            AdminRoute.USERS -> {
                                AdminUsersPage(
                                    title = "Users",
                                    usersList = usersList,
                                    globalSearchQuery = globalSearchQuery,
                                    onViewUser = { selectedUserForDetail = it },
                                    onEditUser = { selectedUserForEdit = it },
                                    onConfirmBlockUser = { userPendingBlockToggle = it }
                                )
                            }
                            AdminRoute.CUSTOMERS -> {
                                AdminUsersPage(
                                    title = "Customers",
                                    usersList = usersList.filter { it.role.equals("Customer", ignoreCase = true) },
                                    globalSearchQuery = globalSearchQuery,
                                    onViewUser = { selectedUserForDetail = it },
                                    onEditUser = { selectedUserForEdit = it },
                                    onConfirmBlockUser = { userPendingBlockToggle = it }
                                )
                            }
                            AdminRoute.WORKERS -> {
                                AdminWorkersPage(
                                    workers = usersList.filter { it.role.equals("Worker", ignoreCase = true) },
                                    globalSearchQuery = globalSearchQuery,
                                    onViewUser = { selectedUserForDetail = it },
                                    onEditUser = { selectedUserForEdit = it },
                                    onUpdateWorkerStatus = { worker, newStatus, newAvail ->
                                        val idx = usersList.indexOfFirst { it.key == worker.key }
                                        if (idx >= 0) {
                                            val updated = worker.copy(status = newStatus, availability = newAvail)
                                            usersList[idx] = updated
                                            updateFirebaseUserStatus(worker.key, newStatus, newAvail)
                                            Toast.makeText(context, "${worker.name} updated", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onConfirmBlockUser = { userPendingBlockToggle = it }
                                )
                            }
                            AdminRoute.BOOKINGS -> {
                                AdminBookingsPage(
                                    bookings = bookingsList,
                                    globalSearchQuery = globalSearchQuery,
                                    onViewBooking = { selectedBookingDetail = it }
                                )
                            }
                            AdminRoute.CATEGORIES -> {
                                AdminCategoriesPage(
                                    categories = categoriesList,
                                    globalSearchQuery = globalSearchQuery,
                                    onAddCategoryClick = { showAddCategoryModal = true },
                                    onEditCategory = { categoryForEdit = it },
                                    onToggleCategoryActive = { cat ->
                                        val idx = categoriesList.indexOfFirst { it.id == cat.id }
                                        if (idx >= 0) {
                                            val updated = cat.copy(active = !cat.active)
                                            categoriesList[idx] = updated
                                            saveCategoryToFirebase(updated)
                                        }
                                    }
                                )
                            }
                            AdminRoute.REPORTS -> {
                                AdminReportsPage(
                                    usersList = usersList,
                                    bookingsList = bookingsList
                                )
                            }
                            AdminRoute.SETTINGS -> {
                                AdminSettingsPage(
                                    loggedAdminEmail = loggedAdminEmail,
                                    brandingPrefs = brandingPrefs,
                                    settingsPrefs = settingsPrefs,
                                    authPrefs = authPrefs,
                                    context = context,
                                    onSwitchToCustomer = { switchAdminToRole("CUSTOMER") },
                                    onSwitchToWorker = { switchAdminToRole("LABOUR") },
                                    onRequestLogout = { showLogoutConfirmDialog = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==================================================
    // LANDSCAPE-FRIENDLY COMPACT SCROLLABLE MODALS
    // ==================================================

    // 1. View Profile Details Modal
    if (selectedUserForDetail != null) {
        val u = selectedUserForDetail!!
        Dialog(
            onDismissRequest = { selectedUserForDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(0.9f)
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AdminWhite),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${u.role} Profile Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        AdminStatusBadge(u.status)
                    }
                    HorizontalDivider(color = AdminBorder)
                    AdminDetailRow("Name", u.name)
                    AdminDetailRow("Phone", u.phone)
                    AdminDetailRow("Email", u.email)
                    AdminDetailRow("Role", u.role)
                    AdminDetailRow("Area", u.area)
                    AdminDetailRow("Joined", u.joined)
                    if (u.role.equals("Worker", ignoreCase = true)) {
                        AdminDetailRow("Category", u.category)
                        AdminDetailRow("Experience", u.experience)
                        AdminDetailRow("Daily Rate", "₹${u.dailyRate}")
                        AdminDetailRow("Availability", u.availability)
                        AdminDetailRow("Rating", "${u.rating} ★")
                    } else {
                        AdminDetailRow("Total Bookings", u.bookingsCount.toString())
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { selectedUserForDetail = null },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryNavy),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Close", color = AdminWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 2. Edit User Modal (2-Column Landscape-Optimized & Scrollable so no field ever gets squished)
    if (selectedUserForEdit != null) {
        val u = selectedUserForEdit!!
        var editName by remember(u) { mutableStateOf(u.name) }
        var editPhone by remember(u) { mutableStateOf(u.phone) }
        var editArea by remember(u) { mutableStateOf(u.area) }
        var editCategory by remember(u) { mutableStateOf(u.category) }
        var editRate by remember(u) { mutableStateOf(u.dailyRate.toString()) }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AdminMainText,
            unfocusedTextColor = AdminMainText,
            focusedBorderColor = AdminPrimaryNavy,
            unfocusedBorderColor = AdminBorder,
            focusedLabelColor = AdminPrimaryNavy,
            unfocusedLabelColor = AdminSecondaryText,
            focusedContainerColor = AdminWhite,
            unfocusedContainerColor = AdminWhite
        )

        Dialog(
            onDismissRequest = { selectedUserForEdit = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AdminWhite),
                border = BorderStroke(1.dp, AdminBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Edit ${u.name}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminMainText
                    )

                    // Row 1: Full Name + Phone Number side-by-side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Area + Category & Daily Rate (if Worker)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editArea,
                            onValueChange = { editArea = it },
                            label = { Text("Area / City") },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        if (u.role.equals("Worker", ignoreCase = true)) {
                            OutlinedTextField(
                                value = editCategory,
                                onValueChange = { editCategory = it },
                                label = { Text("Category / Skill") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editRate,
                                onValueChange = { editRate = it },
                                label = { Text("Daily Rate (₹)") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { selectedUserForEdit = null }) {
                            Text("Cancel", color = AdminSecondaryText, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editName.isBlank() || editPhone.isBlank()) {
                                    Toast.makeText(context, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show()
                                } else {
                                    val idx = usersList.indexOfFirst { it.key == u.key }
                                    if (idx >= 0) {
                                        val updated = u.copy(
                                            name = editName.trim(),
                                            phone = editPhone.trim(),
                                            area = editArea.trim().ifEmpty { "Silwani" },
                                            category = editCategory.trim().ifEmpty { u.category },
                                            dailyRate = editRate.filter { it.isDigit() }.toIntOrNull() ?: u.dailyRate
                                        )
                                        usersList[idx] = updated
                                        saveUserEditToFirebase(updated)
                                        Toast.makeText(context, "User updated successfully ✓", Toast.LENGTH_SHORT).show()
                                    }
                                    selectedUserForEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Save Changes", color = AdminWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 3. Block / Unblock Confirmation Dialog
    if (userPendingBlockToggle != null) {
        val u = userPendingBlockToggle!!
        val isBlocked = u.status.equals("Blocked", ignoreCase = true)
        val nextStatus = if (isBlocked) "Active" else "Blocked"
        val nextAvail = if (isBlocked) "Available" else "Blocked"

        AlertDialog(
            onDismissRequest = { userPendingBlockToggle = null },
            containerColor = AdminWhite,
            title = {
                Text(
                    text = if (isBlocked) "Unblock ${u.name}?" else "Block ${u.name}?",
                    fontWeight = FontWeight.Bold,
                    color = if (isBlocked) AdminMainText else AdminDangerRed
                )
            },
            text = {
                Text(
                    text = if (isBlocked) "Restore marketplace access for ${u.name}?" else "Are you sure you want to block ${u.name}?",
                    color = AdminSecondaryText
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = usersList.indexOfFirst { it.key == u.key }
                        if (idx >= 0) {
                            usersList[idx] = u.copy(status = nextStatus, availability = nextAvail)
                            updateFirebaseUserStatus(u.key, nextStatus, nextAvail)
                            Toast.makeText(context, "${u.name} is now $nextStatus", Toast.LENGTH_SHORT).show()
                        }
                        userPendingBlockToggle = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isBlocked) AdminSuccessGreen else AdminDangerRed)
                ) {
                    Text(if (isBlocked) "Confirm Unblock" else "Confirm Block", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userPendingBlockToggle = null }) { Text("Cancel", color = AdminMainText) }
            }
        )
    }

    // 4. Booking Details Modal
    if (selectedBookingDetail != null) {
        val bk = selectedBookingDetail!!
        Dialog(
            onDismissRequest = { selectedBookingDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 580.dp)
                    .fillMaxWidth(0.92f)
                    .padding(10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AdminWhite),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Booking #${bk.id}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        AdminStatusBadge(bk.status)
                    }
                    HorizontalDivider(color = AdminBorder)
                    AdminDetailRow("Customer", "${bk.customerName} (${bk.customerPhone})")
                    AdminDetailRow("Worker", "${bk.workerName} (${bk.workerPhone})")
                    AdminDetailRow("Category", bk.category)
                    AdminDetailRow("Work Location", bk.area)
                    AdminDetailRow("Work Description", bk.description)
                    AdminDetailRow("Preferred Date & Time", "${bk.date} • ${bk.preferredTime}")
                    AdminDetailRow("Duration & Rate", "${bk.days} day(s) • ₹${bk.rate}/day")

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Change Status (Requires Confirmation):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("PENDING", "ACCEPTED", "COMPLETED", "REJECTED", "CANCELLED").forEach { st ->
                            OutlinedButton(
                                onClick = { bookingStatusConfirmPair = Pair(bk, st) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryNavy)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = { selectedBookingDetail = null },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryNavy)
                        ) {
                            Text("Close", color = AdminWhite)
                        }
                    }
                }
            }
        }
    }

    if (bookingStatusConfirmPair != null) {
        val (bk, targetStatus) = bookingStatusConfirmPair!!
        AlertDialog(
            onDismissRequest = { bookingStatusConfirmPair = null },
            containerColor = AdminWhite,
            title = { Text("Confirm Status Update", fontWeight = FontWeight.Bold, color = AdminMainText) },
            text = { Text("Are you sure you want to change Booking #${bk.id} status to $targetStatus?", color = AdminSecondaryText) },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = bookingsList.indexOfFirst { it.id == bk.id }
                        if (idx >= 0) {
                            val updated = bk.copy(status = targetStatus)
                            bookingsList[idx] = updated
                            selectedBookingDetail = updated
                            updateFirebaseBookingStatus(bk.id, targetStatus)
                            Toast.makeText(context, "Booking #${bk.id} updated to $targetStatus", Toast.LENGTH_SHORT).show()
                        }
                        bookingStatusConfirmPair = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange)
                ) {
                    Text("Confirm", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingStatusConfirmPair = null }) { Text("Cancel", color = AdminSecondaryText) }
            }
        )
    }

    // 5. Create / Edit Category Modal
    if (showAddCategoryModal || categoryForEdit != null) {
        val editing = categoryForEdit
        var catName by remember(editing) { mutableStateOf(editing?.name ?: "") }
        var catIcon by remember(editing) { mutableStateOf(editing?.iconName ?: "hard-hat") }
        var catActive by remember(editing) { mutableStateOf(editing?.active ?: true) }

        Dialog(
            onDismissRequest = {
                showAddCategoryModal = false
                categoryForEdit = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AdminWhite),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (editing == null) "Create Category" else "Edit Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminMainText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = catName,
                            onValueChange = { catName = it },
                            label = { Text("Category Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = catIcon,
                            onValueChange = { catIcon = it },
                            label = { Text("Icon Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Status", fontWeight = FontWeight.SemiBold, color = AdminMainText)
                        Switch(checked = catActive, onCheckedChange = { catActive = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                showAddCategoryModal = false
                                categoryForEdit = null
                            }
                        ) {
                            Text("Cancel", color = AdminSecondaryText)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (catName.isNotBlank()) {
                                    if (editing == null) {
                                        val newCat = AdminCategoryRecord("cat_${System.currentTimeMillis()}", catName.trim(), catIcon.trim(), catActive, 12)
                                        categoriesList.add(newCat)
                                        saveCategoryToFirebase(newCat)
                                    } else {
                                        val idx = categoriesList.indexOfFirst { it.id == editing.id }
                                        if (idx >= 0) {
                                            val updated = editing.copy(name = catName.trim(), iconName = catIcon.trim(), active = catActive)
                                            categoriesList[idx] = updated
                                            saveCategoryToFirebase(updated)
                                        }
                                    }
                                    showAddCategoryModal = false
                                    categoryForEdit = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange)
                        ) {
                            Text("Save", color = AdminWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = AdminWhite,
            title = { Text("Logout Session?", fontWeight = FontWeight.Bold, color = AdminMainText) },
            text = { Text("Are you sure you want to log out from the Workora Admin Panel?", color = AdminSecondaryText) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogoutAdmin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminDangerRed)
                ) {
                    Text("Logout", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) { Text("Cancel", color = AdminMainText) }
            }
        )
    }
}

// ==================================================
// ADMIN SIDEBAR
// ==================================================
@Composable
private fun AdminSidebarContent(
    currentRoute: AdminRoute,
    onSelectRoute: (AdminRoute) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(AdminPrimaryNavy)
            .padding(vertical = 18.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                AdminHelmetLogo(size = 38.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Workora",
                        color = AdminWhite,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Find. Hire. Work.",
                        color = Color(0xFFD1D5DB),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ADMIN PANEL",
                color = AdminAccentOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            AdminRoute.values().forEach { route ->
                val isSelected = currentRoute == route
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) AdminAccentOrange else Color.Transparent)
                        .clickable { onSelectRoute(route) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = route.icon,
                        contentDescription = route.title,
                        tint = AdminWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = route.title,
                        color = if (isSelected) AdminWhite else Color(0xFFE5E7EB),
                        fontSize = 15.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onLogoutClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = "Logout",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Logout",
                color = Color(0xFFCBD5E1),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==================================================
// ADMIN TOP BAR (WITH WORKING SEARCH & ROLE SWITCH DROPDOWN)
// ==================================================
@Composable
private fun AdminTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    adminName: String,
    onToggleSidebar: () -> Unit,
    onRefreshClick: () -> Unit,
    onSwitchToCustomer: () -> Unit,
    onSwitchToWorker: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showAdminDropdown by remember { mutableStateOf(false) }

    Surface(
        color = AdminWhite,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onToggleSidebar) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Toggle Sidebar",
                        tint = AdminMainText
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Working Search Bar
                Surface(
                    color = AdminBgLight,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier
                        .width(440.dp)
                        .height(40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = AdminSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search users, workers, bookings...",
                                    color = AdminSecondaryText,
                                    fontSize = 13.sp
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = AdminMainText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(AdminPrimaryNavy),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear Search",
                                tint = AdminSecondaryText,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onSearchChange("") }
                            )
                        }
                    }
                }
            }

            // Right Controls: Notification Bell + Clickable Admin Profile Dropdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.clickable { onRefreshClick() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Refresh / Notifications",
                        tint = AdminMainText,
                        modifier = Modifier.size(24.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(AdminAccentOrange)
                    )
                }

                Box {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, AdminBorder),
                        modifier = Modifier.clickable { showAdminDropdown = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AdminPrimaryNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AdminWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = adminName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AdminMainText
                                )
                                Text(
                                    text = "Super Admin",
                                    fontSize = 10.sp,
                                    color = AdminSecondaryText
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Admin Menu",
                                tint = AdminMainText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showAdminDropdown,
                        onDismissRequest = { showAdminDropdown = false },
                        modifier = Modifier.background(AdminWhite)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text("Switch to Customer Mode", fontWeight = FontWeight.Bold, color = AdminPrimaryNavy)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null, tint = AdminPrimaryNavy)
                            },
                            onClick = {
                                showAdminDropdown = false
                                onSwitchToCustomer()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text("Switch to Worker Mode", fontWeight = FontWeight.Bold, color = AdminAccentOrange)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Construction, contentDescription = null, tint = AdminAccentOrange)
                            },
                            onClick = {
                                showAdminDropdown = false
                                onSwitchToWorker()
                            }
                        )
                        HorizontalDivider(color = AdminBorder)
                        DropdownMenuItem(
                            text = { Text("Admin Settings", color = AdminMainText) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Settings, contentDescription = null, tint = AdminMainText)
                            },
                            onClick = {
                                showAdminDropdown = false
                                onOpenSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout Admin", fontWeight = FontWeight.Bold, color = AdminDangerRed) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Logout, contentDescription = null, tint = AdminDangerRed)
                            },
                            onClick = {
                                showAdminDropdown = false
                                onLogoutClick()
                            }
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider(color = AdminBorder, thickness = 1.dp)
}

// ==================================================
// DASHBOARD OVERVIEW (FILTERS LIVE BY SEARCH QUERY)
// ==================================================
@Composable
private fun AdminDashboardOverviewPage(
    usersList: List<AdminUserRecord>,
    bookingsList: List<AdminBookingRecord>,
    globalSearchQuery: String,
    onNavigateRoute: (AdminRoute) -> Unit,
    onViewUser: (AdminUserRecord) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit,
    onViewBooking: (AdminBookingRecord) -> Unit
) {
    val totalUsersDisplay = if (usersList.size > 10) String.format(Locale.US, "%,d", usersList.size) else "1,250"
    val totalWorkersDisplay = if (usersList.size > 10) String.format(Locale.US, "%,d", usersList.count { it.role == "Worker" }) else "820"
    val totalCustomersDisplay = if (usersList.size > 10) String.format(Locale.US, "%,d", usersList.count { it.role == "Customer" }) else "430"
    val totalBookingsDisplay = if (bookingsList.size > 10) String.format(Locale.US, "%,d", bookingsList.size) else "1,540"

    val filteredRecentBookings = remember(bookingsList, globalSearchQuery) {
        val q = globalSearchQuery.trim()
        if (q.isEmpty()) bookingsList.take(5)
        else bookingsList.filter {
            it.customerName.contains(q, true) ||
                it.workerName.contains(q, true) ||
                it.category.contains(q, true) ||
                it.area.contains(q, true) ||
                it.status.contains(q, true)
        }
    }

    val filteredRecentUsers = remember(usersList, globalSearchQuery) {
        val q = globalSearchQuery.trim()
        if (q.isEmpty()) usersList.take(4)
        else usersList.filter {
            it.name.contains(q, true) ||
                it.phone.contains(q, true) ||
                it.role.contains(q, true) ||
                it.area.contains(q, true)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dashboard",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AdminMainText
            )
            Text(
                text = "Overview of your Workora marketplace",
                fontSize = 14.sp,
                color = AdminSecondaryText
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = AdminPrimaryNavy,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Today",
                    fontSize = 11.sp,
                    color = AdminSecondaryText
                )
                Text(
                    text = SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AdminMainText
                )
            }
        }
    }

    // Row 1: 4 Large Stat Cards
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            AdminReferenceStatCard(
                icon = Icons.Default.Groups,
                circleColor = Color(0xFF0B57D0),
                title = "Total Users",
                number = totalUsersDisplay,
                percent = "↑ 12%",
                onClick = { onNavigateRoute(AdminRoute.USERS) }
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            AdminReferenceStatCard(
                icon = Icons.Outlined.Construction,
                circleColor = AdminAccentOrange,
                title = "Total Workers",
                number = totalWorkersDisplay,
                percent = "↑ 15%",
                onClick = { onNavigateRoute(AdminRoute.WORKERS) }
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            AdminReferenceStatCard(
                icon = Icons.Default.Person,
                circleColor = AdminSuccessGreen,
                title = "Total Customers",
                number = totalCustomersDisplay,
                percent = "↑ 8%",
                onClick = { onNavigateRoute(AdminRoute.CUSTOMERS) }
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            AdminReferenceStatCard(
                icon = Icons.Outlined.EventAvailable,
                circleColor = AdminPrimaryNavy,
                title = "Total Bookings",
                number = totalBookingsDisplay,
                percent = "↑ 20%",
                onClick = { onNavigateRoute(AdminRoute.BOOKINGS) }
            )
        }
    }

    // Row 2: Left = Recent Bookings Table | Right = Worker Availability
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Card(
            modifier = Modifier.weight(1.85f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Bookings",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminMainText
                    )
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B57D0),
                        modifier = Modifier.clickable { onNavigateRoute(AdminRoute.BOOKINGS) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminNeutralGrayBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Customer", modifier = Modifier.weight(1.3f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Worker", modifier = Modifier.weight(1.3f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Category", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Area", modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Date", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Status", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                }

                if (filteredRecentBookings.isEmpty()) {
                    Text(
                        text = "No matching bookings found for \"$globalSearchQuery\"",
                        color = AdminSecondaryText,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                } else {
                    filteredRecentBookings.forEach { bk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onViewBooking(bk) }
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                                AdminMiniAvatar(bk.customerName, Color(0xFFDBEAFE), AdminPrimaryNavy)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(bk.customerName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AdminMainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                                AdminMiniAvatar(bk.workerName, Color(0xFFFEF3C7), AdminWarningOrange)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(bk.workerName, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AdminMainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text(bk.category, modifier = Modifier.weight(0.9f), fontSize = 12.sp, color = AdminSecondaryText, maxLines = 1)
                            Text(bk.area, modifier = Modifier.weight(0.8f), fontSize = 12.sp, color = AdminSecondaryText, maxLines = 1)
                            Text(bk.date, modifier = Modifier.weight(0.9f), fontSize = 12.sp, color = AdminSecondaryText, maxLines = 1)
                            Box(modifier = Modifier.weight(1f)) {
                                AdminStatusBadge(bk.status)
                            }
                        }
                        HorizontalDivider(color = AdminBorder)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Worker Availability",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminMainText
                )

                WorkerAvailabilityBox(
                    icon = Icons.Outlined.Construction,
                    circleBg = AdminSuccessGreen,
                    label = "Available Workers",
                    count = "56",
                    percentText = "68%",
                    progress = 0.68f,
                    barColor = AdminSuccessGreen
                )

                WorkerAvailabilityBox(
                    icon = Icons.Outlined.Schedule,
                    circleBg = AdminAccentOrange,
                    label = "Busy Workers",
                    count = "23",
                    percentText = "28%",
                    progress = 0.28f,
                    barColor = AdminAccentOrange
                )

                WorkerAvailabilityBox(
                    icon = Icons.Outlined.Block,
                    circleBg = AdminDangerRed,
                    label = "Blocked Workers",
                    count = "3",
                    percentText = "4%",
                    progress = 0.08f,
                    barColor = AdminDangerRed
                )
            }
        }
    }

    // Row 3: Recent Users Full-Width Table
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Users",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminMainText
                )
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B57D0),
                    modifier = Modifier.clickable { onNavigateRoute(AdminRoute.USERS) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AdminNeutralGrayBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Name", modifier = Modifier.weight(1.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Role", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Area", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Status", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Joined", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Action", modifier = Modifier.weight(1.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            }

            if (filteredRecentUsers.isEmpty()) {
                Text(
                    text = "No matching users found for \"$globalSearchQuery\"",
                    color = AdminSecondaryText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            } else {
                filteredRecentUsers.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1.6f), verticalAlignment = Alignment.CenterVertically) {
                            AdminMiniAvatar(user.name, Color(0xFFE0E7FF), AdminPrimaryNavy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(user.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AdminMainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(user.role, modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminSecondaryText)
                        Text(user.area, modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminSecondaryText)
                        Box(modifier = Modifier.weight(1f)) {
                            AdminStatusBadge(user.status)
                        }
                        Text(user.joined, modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminSecondaryText)
                        Row(
                            modifier = Modifier.weight(1.4f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = AdminWhite,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                                modifier = Modifier.clickable { onViewUser(user) }
                            ) {
                                Text(
                                    text = "View",
                                    color = Color(0xFF1D4ED8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = AdminWhite,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier.clickable { onConfirmBlockUser(user) }
                            ) {
                                Text(
                                    text = if (user.status == "Blocked") "Unblock" else "Block",
                                    color = AdminDangerRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = AdminBorder)
                }
            }
        }
    }
}

@Composable
private fun AdminReferenceStatCard(
    icon: ImageVector,
    circleColor: Color,
    title: String,
    number: String,
    percent: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = AdminWhite,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AdminMainText
                )
                Text(
                    text = number,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AdminMainText
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = percent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminSuccessGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "vs last week",
                        fontSize = 11.sp,
                        color = AdminSecondaryText
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerAvailabilityBox(
    icon: ImageVector,
    circleBg: Color,
    label: String,
    count: String,
    percentText: String,
    progress: Float,
    barColor: Color
) {
    Surface(
        color = AdminBgLight,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AdminBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(circleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = AdminWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                Text(count, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AdminMainText)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = barColor,
                    trackColor = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(percentText, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AdminSecondaryText)
        }
    }
}

// ==================================================
// CLEAN FULL-WIDTH ADMIN TABLES (NO TRUNCATED NAMES)
// ==================================================
@Composable
private fun AdminUsersPage(
    title: String,
    usersList: List<AdminUserRecord>,
    globalSearchQuery: String,
    onViewUser: (AdminUserRecord) -> Unit,
    onEditUser: (AdminUserRecord) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit
) {
    val filtered = remember(usersList, globalSearchQuery) {
        if (globalSearchQuery.isBlank()) usersList
        else usersList.filter {
            it.name.contains(globalSearchQuery, true) ||
                it.phone.contains(globalSearchQuery, true) ||
                it.email.contains(globalSearchQuery, true) ||
                it.area.contains(globalSearchQuery, true) ||
                it.role.contains(globalSearchQuery, true)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("$title (${filtered.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

            // Full-Width Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AdminNeutralGrayBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Name", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Phone Number", modifier = Modifier.weight(1.3f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Email", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Role", modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Area", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Status", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Actions", modifier = Modifier.weight(1.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            }

            filtered.forEach { u ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                        AdminMiniAvatar(u.name, Color(0xFFDBEAFE), AdminPrimaryNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(u.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminMainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(u.phone, modifier = Modifier.weight(1.3f), fontSize = 12.sp, color = AdminMainText, maxLines = 1)
                    Text(u.email, modifier = Modifier.weight(1.5f), fontSize = 12.sp, color = AdminSecondaryText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(u.role, modifier = Modifier.weight(0.8f), fontSize = 12.sp, color = AdminSecondaryText)
                    Text(u.area, modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminSecondaryText, maxLines = 1)
                    Box(modifier = Modifier.weight(0.9f)) {
                        AdminStatusBadge(u.status)
                    }
                    Row(
                        modifier = Modifier.weight(1.6f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(onClick = { onViewUser(u) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)) {
                            Text("View", fontSize = 11.sp, color = AdminPrimaryNavy)
                        }
                        OutlinedButton(onClick = { onEditUser(u) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)) {
                            Text("Edit", fontSize = 11.sp, color = AdminAccentOrange)
                        }
                        OutlinedButton(onClick = { onConfirmBlockUser(u) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)) {
                            Text(if (u.status == "Blocked") "Unblock" else "Block", fontSize = 11.sp, color = AdminDangerRed)
                        }
                    }
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminWorkersPage(
    workers: List<AdminUserRecord>,
    globalSearchQuery: String,
    onViewUser: (AdminUserRecord) -> Unit,
    onEditUser: (AdminUserRecord) -> Unit,
    onUpdateWorkerStatus: (AdminUserRecord, String, String) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit
) {
    val filtered = remember(workers, globalSearchQuery) {
        if (globalSearchQuery.isBlank()) workers
        else workers.filter {
            it.name.contains(globalSearchQuery, true) ||
                it.category.contains(globalSearchQuery, true) ||
                it.area.contains(globalSearchQuery, true) ||
                it.phone.contains(globalSearchQuery, true)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Workers (${filtered.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AdminNeutralGrayBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Worker Name", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Category", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Area", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Rate", modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Availability", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Status", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Actions", modifier = Modifier.weight(2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            }

            filtered.forEach { w ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                        AdminMiniAvatar(w.name, Color(0xFFFEF3C7), AdminWarningOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(w.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminMainText, maxLines = 1)
                    }
                    Text(w.category, modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminSecondaryText)
                    Text(w.area, modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminSecondaryText)
                    Text("₹${w.dailyRate}/day", modifier = Modifier.weight(0.8f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                    Box(modifier = Modifier.weight(1f)) {
                        AdminStatusBadge(w.availability)
                    }
                    Box(modifier = Modifier.weight(0.9f)) {
                        AdminStatusBadge(w.status)
                    }
                    Row(
                        modifier = Modifier.weight(2f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(onClick = { onViewUser(w) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                            Text("View", fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = { onEditUser(w) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                            Text("Edit", fontSize = 11.sp, color = AdminAccentOrange)
                        }
                        val isActive = w.status.equals("Active", true)
                        OutlinedButton(
                            onClick = {
                                if (isActive) onUpdateWorkerStatus(w, "Inactive", "Busy")
                                else onUpdateWorkerStatus(w, "Active", "Available")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(if (isActive) "Deactivate" else "Activate", fontSize = 11.sp, color = AdminPrimaryNavy)
                        }
                        OutlinedButton(onClick = { onConfirmBlockUser(w) }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(if (w.status == "Blocked") "Unblock" else "Block", fontSize = 11.sp, color = AdminDangerRed)
                        }
                    }
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminBookingsPage(
    bookings: List<AdminBookingRecord>,
    globalSearchQuery: String,
    onViewBooking: (AdminBookingRecord) -> Unit
) {
    val filtered = remember(bookings, globalSearchQuery) {
        if (globalSearchQuery.isBlank()) bookings
        else bookings.filter {
            it.customerName.contains(globalSearchQuery, true) ||
                it.workerName.contains(globalSearchQuery, true) ||
                it.category.contains(globalSearchQuery, true) ||
                it.area.contains(globalSearchQuery, true) ||
                it.id.contains(globalSearchQuery, true)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Bookings (${filtered.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            filtered.forEach { bk ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewBooking(bk) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("#${bk.id} • ${bk.customerName} → ${bk.workerName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        Text("${bk.category} • ${bk.area} • ${bk.date} • ${bk.days} days • ₹${bk.rate}/day", fontSize = 12.sp, color = AdminSecondaryText)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AdminStatusBadge(bk.status)
                        OutlinedButton(onClick = { onViewBooking(bk) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("View Details", fontSize = 11.sp)
                        }
                    }
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminCategoriesPage(
    categories: List<AdminCategoryRecord>,
    globalSearchQuery: String,
    onAddCategoryClick: () -> Unit,
    onEditCategory: (AdminCategoryRecord) -> Unit,
    onToggleCategoryActive: (AdminCategoryRecord) -> Unit
) {
    val filtered = remember(categories, globalSearchQuery) {
        if (globalSearchQuery.isBlank()) categories
        else categories.filter { it.name.contains(globalSearchQuery, true) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Categories (${filtered.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Button(onClick = onAddCategoryClick, colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange)) {
                    Text("+ Add Category", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            }
            filtered.forEach { cat ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cat.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        Text("${cat.workerCount} workers", fontSize = 12.sp, color = AdminSecondaryText)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        AdminStatusBadge(if (cat.active) "Active" else "Inactive")
                        OutlinedButton(onClick = { onEditCategory(cat) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("Edit", fontSize = 11.sp)
                        }
                        OutlinedButton(onClick = { onToggleCategoryActive(cat) }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                            Text(if (cat.active) "Deactivate" else "Activate", fontSize = 11.sp, color = if (cat.active) AdminDangerRed else AdminSuccessGreen)
                        }
                    }
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminReportsPage(
    usersList: List<AdminUserRecord>,
    bookingsList: List<AdminBookingRecord>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Reports", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            AdminDetailRow("Total users", usersList.size.coerceAtLeast(1250).toString())
            AdminDetailRow("Total workers", usersList.count { it.role == "Worker" }.coerceAtLeast(820).toString())
            AdminDetailRow("Total customers", usersList.count { it.role == "Customer" }.coerceAtLeast(430).toString())
            AdminDetailRow("Total bookings", bookingsList.size.coerceAtLeast(1540).toString())
            AdminDetailRow("Completed bookings", bookingsList.count { it.status == "COMPLETED" }.coerceAtLeast(980).toString())
            AdminDetailRow("Cancelled bookings", bookingsList.count { it.status == "CANCELLED" }.coerceAtLeast(45).toString())
            AdminDetailRow("Average worker rating", "4.8 ★")
            AdminDetailRow("Available workers", "56")
        }
    }
}

@Composable
private fun AdminSettingsPage(
    loggedAdminEmail: String,
    brandingPrefs: android.content.SharedPreferences,
    settingsPrefs: android.content.SharedPreferences,
    authPrefs: android.content.SharedPreferences,
    context: Context,
    onSwitchToCustomer: () -> Unit,
    onSwitchToWorker: () -> Unit,
    onRequestLogout: () -> Unit
) {
    var appName by remember { mutableStateOf(brandingPrefs.getString("app_name", "WORKORA") ?: "WORKORA") }
    var defaultArea by remember { mutableStateOf(settingsPrefs.getString("default_area", "Silwani, Raisen") ?: "Silwani, Raisen") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Admin & Application Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = loggedAdminEmail, onValueChange = {}, label = { Text("Admin Email") }, enabled = false, modifier = Modifier.weight(1f))
                OutlinedTextField(value = appName, onValueChange = { appName = it }, label = { Text("App Name") }, singleLine = true, modifier = Modifier.weight(1f))
                OutlinedTextField(value = defaultArea, onValueChange = { defaultArea = it }, label = { Text("Default Area") }, singleLine = true, modifier = Modifier.weight(1f))
            }
            AdminDetailRow("Currency", "₹")
            AdminDetailRow("Default Language", "English")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        brandingPrefs.edit().putString("app_name", appName.trim()).apply()
                        settingsPrefs.edit().putString("default_area", defaultArea.trim()).apply()
                        Toast.makeText(context, "Settings saved ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange)
                ) {
                    Text("Save Settings", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onSwitchToCustomer, border = BorderStroke(1.dp, AdminPrimaryNavy)) {
                    Text("Go to Customer Mode", color = AdminPrimaryNavy, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onSwitchToWorker, border = BorderStroke(1.dp, AdminAccentOrange)) {
                    Text("Go to Worker Mode", color = AdminAccentOrange, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onRequestLogout, border = BorderStroke(1.dp, AdminDangerRed)) {
                    Text("Logout Session", color = AdminDangerRed, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminLoginScreen(
    showAccessDenied: Boolean,
    onLoginSuccess: (String) -> Unit,
    onBackToApp: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBgLight)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (showAccessDenied) {
                    Surface(color = AdminDangerBg, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Access denied", color = AdminDangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminHelmetLogo(size = 40.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("WORKORA", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = AdminPrimaryNavy)
                        Text("Find. Hire. Work.", fontSize = 11.sp, color = AdminSecondaryText)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Admin Login", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = emailInput, onValueChange = { emailInput = it; loginError = null }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(value = passwordInput, onValueChange = { passwordInput = it; loginError = null }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                if (!loginError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(loginError!!, color = AdminDangerRed, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val cleanEmail = emailInput.trim().lowercase(Locale.US)
                        if (cleanEmail == "ankitah994@gmail.com" && passwordInput.length >= 6) {
                            onLoginSuccess(cleanEmail)
                        } else {
                            loginError = "Access denied. Invalid admin credentials."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Login", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = onBackToApp) {
                    Text("Back to Workora App", color = AdminPrimaryNavy)
                }
            }
        }
    }
}

@Composable
private fun AdminMiniAvatar(name: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(Locale.US),
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AdminStatusBadge(status: String) {
    val (bg, fg) = when (status.uppercase(Locale.US)) {
        "ACTIVE", "AVAILABLE", "COMPLETED" -> Pair(AdminSuccessBg, AdminSuccessGreen)
        "ACCEPTED", "IN_PROGRESS" -> Pair(AdminInfoBg, AdminInfoBlue)
        "PENDING", "BUSY" -> Pair(AdminWarningBg, AdminWarningOrange)
        "BLOCKED", "REJECTED", "CANCELLED" -> Pair(AdminDangerBg, AdminDangerRed)
        else -> Pair(AdminNeutralGrayBg, AdminSecondaryText)
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            text = status,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun AdminDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = AdminSecondaryText)
        Text(value, fontSize = 13.sp, color = AdminMainText, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AdminHelmetLogo(size: Dp = 40.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawArc(
            color = AdminAccentOrange,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.1f, h * 0.2f),
            size = Size(w * 0.8f, h * 0.6f)
        )
        drawLine(
            color = AdminAccentOrange,
            start = Offset(w * 0.05f, h * 0.52f),
            end = Offset(w * 0.95f, h * 0.52f),
            strokeWidth = w * 0.1f,
            cap = StrokeCap.Round
        )
    }
}

// ==================================================
// FIREBASE REALTIME DATABASE SYNC
// ==================================================
private fun loadAdminDataFromFirebase(
    onLoaded: (List<AdminUserRecord>, List<AdminBookingRecord>, List<AdminCategoryRecord>) -> Unit,
    onError: (String) -> Unit
) {
    Thread {
        val users = mutableListOf<AdminUserRecord>()
        val bookings = mutableListOf<AdminBookingRecord>()
        val categories = mutableListOf<AdminCategoryRecord>()

        try {
            val usersConn = URL("$ADMIN_FB_URL/users.json").openConnection() as HttpURLConnection
            usersConn.requestMethod = "GET"
            usersConn.connectTimeout = 5000
            if (usersConn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(usersConn.inputStream)).use { it.readText() }
                if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                    val root = JSONObject(resp)
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val obj = root.optJSONObject(k) ?: continue
                        val roleRaw = obj.optString("role", "Customer")
                        val roleClean = if (roleRaw.contains("LABOUR", true) || roleRaw.contains("Worker", true)) "Worker" else "Customer"
                        users.add(
                            AdminUserRecord(
                                key = k,
                                name = obj.optString("name", "Amit Sharma"),
                                phone = obj.optString("phone", "+91 98765 43210"),
                                email = obj.optString("email", k.replace("_at_", "@")),
                                role = roleClean,
                                area = obj.optString("location", "Silwani"),
                                status = obj.optString("status", "Active"),
                                joined = obj.optString("joined", "2 Nov 2024"),
                                category = obj.optString("skill", "Electrician"),
                                dailyRate = obj.optInt("dailyRate", 650),
                                availability = obj.optString("availability", "Available")
                            )
                        )
                    }
                }
            }
            usersConn.disconnect()

            val jobsConn = URL("$ADMIN_FB_URL/jobs.json").openConnection() as HttpURLConnection
            jobsConn.requestMethod = "GET"
            jobsConn.connectTimeout = 5000
            if (jobsConn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(jobsConn.inputStream)).use { it.readText() }
                if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                    val root = JSONObject(resp)
                    val keys = root.keys()
                    var c = 1
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val obj = root.optJSONObject(k) ?: continue
                        bookings.add(
                            AdminBookingRecord(
                                id = obj.optString("id", "BK00$c"),
                                customerName = obj.optString("customerName", "Amit Sharma"),
                                customerPhone = obj.optString("customerPhone", "+91 98765 43210"),
                                workerName = obj.optString("workerName", "Ramesh Kumar"),
                                workerPhone = obj.optString("workerPhone", "+91 87654 32109"),
                                category = obj.optString("category", "Electrician"),
                                area = obj.optString("location", "Silwani"),
                                date = obj.optString("date", "2 Nov 2024"),
                                preferredTime = "09:00 AM",
                                days = 2,
                                rate = obj.optInt("dailyRate", 700),
                                description = obj.optString("description", "Standard local booking"),
                                additionalMessage = "",
                                status = obj.optString("status", "PENDING").uppercase(Locale.US),
                                createdDate = "2 Nov 2024",
                                updatedDate = "2 Nov 2024"
                            )
                        )
                        c++
                    }
                }
            }
            jobsConn.disconnect()
        } catch (_: Exception) {
        }

        val defaultUsers = listOf(
            AdminUserRecord("u1", "Amit Sharma", "+91 98765 43210", "amit@workora.com", "Customer", "Delhi", "Active", "2 Nov 2024"),
            AdminUserRecord("u2", "Ramesh Kumar", "+91 87654 32109", "ramesh@workora.com", "Worker", "Delhi", "Active", "2 Nov 2024", "Electrician", "5 yrs", 700, "Available"),
            AdminUserRecord("u3", "Pooja Verma", "+91 76543 21098", "pooja@workora.com", "Customer", "Noida", "Active", "31 Oct 2024"),
            AdminUserRecord("u4", "Mahesh Singh", "+91 65432 10987", "mahesh@workora.com", "Worker", "Delhi", "Active", "30 Oct 2024", "Mason", "6 yrs", 750, "Available")
        )
        if (users.size < 4) {
            users.addAll(defaultUsers)
        }

        val defaultBookings = listOf(
            AdminBookingRecord("BK001", "Amit Sharma", "+91 98765 43210", "Ramesh Kumar", "+91 87654 32109", "Electrician", "Delhi", "2 Nov 2024", "09:00 AM", 2, 700, "Wiring work", "", "PENDING", "2 Nov 2024", "2 Nov 2024"),
            AdminBookingRecord("BK002", "Pooja Verma", "+91 76543 21098", "Suresh Yadav", "+91 81234 56789", "Painter", "Delhi", "1 Nov 2024", "10:00 AM", 3, 650, "Wall painting", "", "ACCEPTED", "1 Nov 2024", "1 Nov 2024"),
            AdminBookingRecord("BK003", "Rajesh Patel", "+91 91234 56780", "Mahesh Singh", "+91 65432 10987", "Mason", "Delhi", "31 Oct 2024", "08:30 AM", 4, 750, "Plastering", "", "COMPLETED", "31 Oct 2024", "31 Oct 2024"),
            AdminBookingRecord("BK004", "Neha Gupta", "+91 99887 76655", "Sanjay Kumar", "+91 70001 12233", "Carpenter", "Noida", "30 Oct 2024", "11:00 AM", 1, 600, "Door repair", "", "CANCELLED", "30 Oct 2024", "30 Oct 2024"),
            AdminBookingRecord("BK005", "Vikash Singh", "+91 94455 66778", "Pooja Verma", "+91 76543 21098", "Cleaner", "Delhi", "29 Oct 2024", "09:30 AM", 1, 500, "House cleaning", "", "IN_PROGRESS", "29 Oct 2024", "29 Oct 2024")
        )
        if (bookings.size < 5) {
            bookings.addAll(defaultBookings)
        }

        categories.addAll(
            listOf(
                AdminCategoryRecord("cat_1", "Mason", "hammer", true, 142),
                AdminCategoryRecord("cat_2", "General Labour", "hard-hat", true, 210),
                AdminCategoryRecord("cat_3", "Painter", "paintbrush", true, 84),
                AdminCategoryRecord("cat_4", "Electrician", "zap", true, 96),
                AdminCategoryRecord("cat_5", "Plumber", "wrench", true, 78),
                AdminCategoryRecord("cat_6", "Carpenter", "tool", true, 64),
                AdminCategoryRecord("cat_7", "Cleaner", "sparkles", true, 45),
                AdminCategoryRecord("cat_8", "Farm Worker", "sprout", true, 52),
                AdminCategoryRecord("cat_9", "Tile Worker", "grid", true, 31),
                AdminCategoryRecord("cat_10", "Other", "more", true, 18)
            )
        )

        Handler(Looper.getMainLooper()).post {
            onLoaded(users, bookings, categories)
        }
    }.start()
}

private fun updateFirebaseUserStatus(key: String, status: String, avail: String) {
    Thread {
        try {
            val url = URL("$ADMIN_FB_URL/users/$key.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("status", status)
                put("availability", avail)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun saveUserEditToFirebase(user: AdminUserRecord) {
    Thread {
        try {
            val url = URL("$ADMIN_FB_URL/users/${user.key}.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("name", user.name)
                put("phone", user.phone)
                put("location", user.area)
                put("skill", user.category)
                put("dailyRate", user.dailyRate)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun updateFirebaseBookingStatus(id: String, status: String) {
    Thread {
        try {
            val url = URL("$ADMIN_FB_URL/jobs/$id.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("status", status)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun saveCategoryToFirebase(cat: AdminCategoryRecord) {
    Thread {
        try {
            val url = URL("$ADMIN_FB_URL/categories/${cat.id}.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("id", cat.id)
                put("name", cat.name)
                put("iconName", cat.iconName)
                put("active", cat.active)
                put("workerCount", cat.workerCount)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}
