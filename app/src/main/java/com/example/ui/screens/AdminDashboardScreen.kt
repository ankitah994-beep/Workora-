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
    STATE_CONTROL("State / Area", Icons.Outlined.LocationOn),
    REPORTS("Reports", Icons.Outlined.Flag),
    SETTINGS("Settings", Icons.Outlined.Settings)
}

internal data class AdminUserRecord(
    val key: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String,
    val area: String,
    val stateName: String,
    val status: String,
    val joined: String,
    val category: String = "",
    val experience: String = "",
    val dailyRate: Int = 0,
    val availability: String = "Available",
    val rating: Double = 0.0,
    val bookingsCount: Int = 0
)

internal data class AdminBookingRecord(
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

internal data class AdminCategoryRecord(
    val id: String,
    val name: String,
    val iconName: String,
    val active: Boolean,
    val workerCount: Int
)

internal data class AdminStateAreaRule(
    val id: String,
    val stateName: String,
    val areaKeywords: String,
    val isServiceEnabled: Boolean
)

private fun inferStateFromArea(areaText: String, explicitState: String = ""): String {
    if (explicitState.isNotBlank()) return explicitState
    if (areaText.isBlank()) return "Not Provided"
    val lower = areaText.lowercase(Locale.US)
    return when {
        lower.contains("silwani") || lower.contains("raisen") || lower.contains("bhopal") ||
            lower.contains("indore") || lower.contains("sagar") || lower.contains("vidisha") ||
            lower.contains("jabalpur") || lower.contains("gwalior") || lower.contains("mp") ||
            lower.contains("madhya") -> "Madhya Pradesh"
        lower.contains("delhi") || lower.contains("ncr") -> "Delhi NCR"
        lower.contains("noida") || lower.contains("lucknow") || lower.contains("kanpur") ||
            lower.contains("varanasi") || lower.contains("agra") || lower.contains("up") ||
            lower.contains("uttar") -> "Uttar Pradesh"
        lower.contains("mumbai") || lower.contains("pune") || lower.contains("nagpur") ||
            lower.contains("maharashtra") -> "Maharashtra"
        lower.contains("jaipur") || lower.contains("kota") || lower.contains("udaipur") ||
            lower.contains("rajasthan") -> "Rajasthan"
        lower.contains("patna") || lower.contains("bihar") -> "Bihar"
        lower.contains("ahmedabad") || lower.contains("surat") || lower.contains("gujarat") -> "Gujarat"
        else -> areaText
    }
}

@Composable
fun AdminDashboardScreen(
    adminEmail: String = "ankitah994@gmail.com",
    adminTier: String = "SUPER_ADMIN",
    onLogoutAdmin: () -> Unit = {},
    onSwitchRoleFromAdmin: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? Activity

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
    val stateAreaRulesList = remember { mutableStateListOf<AdminStateAreaRule>() }

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var selectedUserForDetail by remember { mutableStateOf<AdminUserRecord?>(null) }
    var selectedUserForEdit by remember { mutableStateOf<AdminUserRecord?>(null) }
    var userPendingBlockToggle by remember { mutableStateOf<AdminUserRecord?>(null) }
    var selectedBookingDetail by remember { mutableStateOf<AdminBookingRecord?>(null) }
    var bookingStatusConfirmPair by remember { mutableStateOf<Pair<AdminBookingRecord, String>?>(null) }
    var showAddCategoryModal by remember { mutableStateOf(false) }
    var categoryForEdit by remember { mutableStateOf<AdminCategoryRecord?>(null) }
    var showAddStateAreaModal by remember { mutableStateOf(false) }

    val refreshAdminDatabase: () -> Unit = {
        isLoadingData = true
        errorBannerMessage = null
        loadAdminDataFromFirebase(
            onLoaded = { loadedUsers, loadedBookings, loadedCategories, loadedAreaRules ->
                usersList.clear()
                usersList.addAll(loadedUsers)
                bookingsList.clear()
                bookingsList.addAll(loadedBookings)
                categoriesList.clear()
                categoriesList.addAll(loadedCategories)
                stateAreaRulesList.clear()
                stateAreaRulesList.addAll(loadedAreaRules)
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
                        onOpenStateControl = { currentRoute = AdminRoute.STATE_CONTROL },
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
                                    stateAreaRulesList = stateAreaRulesList,
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
                            AdminRoute.STATE_CONTROL -> {
                                AdminStateAreaControlPage(
                                    usersList = usersList,
                                    stateRules = stateAreaRulesList,
                                    globalSearchQuery = globalSearchQuery,
                                    onAddStateRuleClick = { showAddStateAreaModal = true },
                                    onToggleStateService = { rule ->
                                        val idx = stateAreaRulesList.indexOfFirst { it.id == rule.id }
                                        if (idx >= 0) {
                                            val updated = rule.copy(isServiceEnabled = !rule.isServiceEnabled)
                                            stateAreaRulesList[idx] = updated
                                            saveStateAreaRuleToFirebase(updated)
                                            syncDisabledAreasPrefs(settingsPrefs, stateAreaRulesList)
                                            val statusWord = if (updated.isServiceEnabled) "ACTIVE" else "DISABLED"
                                            Toast.makeText(context, "${updated.stateName}: $statusWord ✓", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onDeleteStateRule = { rule ->
                                        stateAreaRulesList.remove(rule)
                                        deleteStateAreaRuleFromFirebase(rule.id)
                                        syncDisabledAreasPrefs(settingsPrefs, stateAreaRulesList)
                                        Toast.makeText(context, "${rule.stateName} removed", Toast.LENGTH_SHORT).show()
                                    },
                                    onFilterUsersByState = { stateSearch ->
                                        globalSearchQuery = stateSearch
                                        currentRoute = AdminRoute.USERS
                                    }
                                )
                            }
                            AdminRoute.REPORTS -> {
                                AdminReportsPage(
                                    usersList = usersList,
                                    bookingsList = bookingsList,
                                    stateRules = stateAreaRulesList
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

    if (showAddStateAreaModal) {
        var newStateName by remember { mutableStateOf("") }
        var newAreaKeywords by remember { mutableStateOf("") }
        var newIsEnabled by remember { mutableStateOf(true) }

        Dialog(
            onDismissRequest = { showAddStateAreaModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(0.92f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AdminWhite),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add State / Area Service Control",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminMainText
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newStateName,
                            onValueChange = { newStateName = it },
                            label = { Text("State / Region (e.g. Madhya Pradesh)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newAreaKeywords,
                            onValueChange = { newAreaKeywords = it },
                            label = { Text("Cities / Areas (e.g. Silwani, Raisen, Bhopal)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (newIsEnabled) "App Status: ACTIVE" else "App Status: DISABLED",
                            fontWeight = FontWeight.Bold,
                            color = if (newIsEnabled) AdminSuccessGreen else AdminDangerRed
                        )
                        Switch(
                            checked = newIsEnabled,
                            onCheckedChange = { newIsEnabled = it }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddStateAreaModal = false }) {
                            Text("Cancel", color = AdminSecondaryText)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newStateName.isNotBlank()) {
                                    val cleanId = "state_" + newStateName.trim().lowercase(Locale.US).replace(" ", "_")
                                    val rule = AdminStateAreaRule(
                                        id = cleanId,
                                        stateName = newStateName.trim(),
                                        areaKeywords = newAreaKeywords.trim().ifEmpty { newStateName.trim() },
                                        isServiceEnabled = newIsEnabled
                                    )
                                    val existingIdx = stateAreaRulesList.indexOfFirst { it.id == cleanId || it.stateName.equals(rule.stateName, true) }
                                    if (existingIdx >= 0) {
                                        stateAreaRulesList[existingIdx] = rule
                                    } else {
                                        stateAreaRulesList.add(0, rule)
                                    }
                                    saveStateAreaRuleToFirebase(rule)
                                    syncDisabledAreasPrefs(settingsPrefs, stateAreaRulesList)
                                    Toast.makeText(context, "${rule.stateName} rule saved ✓", Toast.LENGTH_SHORT).show()
                                    showAddStateAreaModal = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange)
                        ) {
                            Text("Save Rule", color = AdminWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

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
                    AdminDetailRow("Name", u.name.ifBlank { "Not Provided" })
                    AdminDetailRow("Phone", u.phone.ifBlank { "Not Provided" })
                    AdminDetailRow("Email", u.email.ifBlank { "Not Provided" })
                    AdminDetailRow("Role", u.role)
                    AdminDetailRow("State", u.stateName.ifBlank { "Not Provided" })
                    AdminDetailRow("Area / City", u.area.ifBlank { "Not Provided" })
                    AdminDetailRow("Joined", u.joined.ifBlank { "Not Provided" })
                    if (u.role.equals("Worker", ignoreCase = true)) {
                        AdminDetailRow("Category", u.category.ifBlank { "Not Provided" })
                        AdminDetailRow("Experience", u.experience.ifBlank { "Not Provided" })
                        AdminDetailRow("Daily Rate", if (u.dailyRate > 0) "₹${u.dailyRate}" else "Not Provided")
                        AdminDetailRow("Availability", u.availability.ifBlank { "Available" })
                        AdminDetailRow("Rating", if (u.rating > 0.0) "${u.rating} ★" else "New (0.0 ★)")
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

    if (selectedUserForEdit != null) {
        val u = selectedUserForEdit!!
        var editName by remember(u) { mutableStateOf(u.name) }
        var editPhone by remember(u) { mutableStateOf(u.phone) }
        var editState by remember(u) { mutableStateOf(u.stateName) }
        var editArea by remember(u) { mutableStateOf(u.area) }
        var editCategory by remember(u) { mutableStateOf(u.category) }
        var editExperience by remember(u) { mutableStateOf(u.experience) }
        var editRate by remember(u) { mutableStateOf(if (u.dailyRate > 0) u.dailyRate.toString() else "") }

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
                    .widthIn(max = 640.dp)
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = editState,
                            onValueChange = { editState = it },
                            label = { Text("State") },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editArea,
                            onValueChange = { editArea = it },
                            label = { Text("Area / City") },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (u.role.equals("Worker", ignoreCase = true)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = editCategory,
                                onValueChange = { editCategory = it },
                                label = { Text("Category / Skill") },
                                singleLine = true,
                                colors = textFieldColors,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = editExperience,
                                onValueChange = { editExperience = it },
                                label = { Text("Experience (e.g. 3 yrs)") },
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
                                modifier = Modifier.weight(1f)
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
                                            stateName = editState.trim(),
                                            area = editArea.trim(),
                                            category = editCategory.trim(),
                                            experience = editExperience.trim(),
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
                                        val newCat = AdminCategoryRecord("cat_${System.currentTimeMillis()}", catName.trim(), catIcon.trim(), catActive, 0)
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

@Composable
private fun AdminStateAreaControlPage(
    usersList: List<AdminUserRecord>,
    stateRules: List<AdminStateAreaRule>,
    globalSearchQuery: String,
    onAddStateRuleClick: () -> Unit,
    onToggleStateService: (AdminStateAreaRule) -> Unit,
    onDeleteStateRule: (AdminStateAreaRule) -> Unit,
    onFilterUsersByState: (String) -> Unit
) {
    val filteredRules = remember(stateRules, globalSearchQuery) {
        if (globalSearchQuery.isBlank()) stateRules
        else stateRules.filter {
            it.stateName.contains(globalSearchQuery, true) ||
                it.areaKeywords.contains(globalSearchQuery, true)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "State & Area Wise Service Control",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AdminMainText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "State-wise Customers aur Workers ki sankhya dekhein aur kisi bhi State/Area me app ko ON ya OFF karein.",
                        fontSize = 12.sp,
                        color = AdminSecondaryText
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onAddStateRuleClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = AdminWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Add State / Area", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "State-wise Customers, Workers & App Service Toggle",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminMainText
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AdminNeutralGrayBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("State / Region", modifier = Modifier.weight(1.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Covered Cities / Areas", modifier = Modifier.weight(1.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Customers", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Workers", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Total Users", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("App Status", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("ON / OFF Control", modifier = Modifier.weight(1.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                }

                filteredRules.forEach { rule ->
                    val keywords = rule.areaKeywords.split(",").map { it.trim().lowercase(Locale.US) }.filter { it.isNotEmpty() }
                    val matchingUsers = usersList.filter { u ->
                        u.stateName.equals(rule.stateName, ignoreCase = true) ||
                            u.area.contains(rule.stateName, ignoreCase = true) ||
                            keywords.any { kw -> u.area.lowercase(Locale.US).contains(kw) || u.stateName.lowercase(Locale.US).contains(kw) }
                    }
                    val customerCount = matchingUsers.count { it.role.equals("Customer", ignoreCase = true) }
                    val workerCount = matchingUsers.count { it.role.equals("Worker", ignoreCase = true) }
                    val totalInState = customerCount + workerCount

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1.4f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = if (rule.isServiceEnabled) AdminPrimaryNavy else AdminDangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = rule.stateName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminMainText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = rule.areaKeywords,
                            modifier = Modifier.weight(1.6f),
                            fontSize = 12.sp,
                            color = AdminSecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$customerCount Customers",
                            modifier = Modifier.weight(0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AdminPrimaryNavy
                        )
                        Text(
                            text = "$workerCount Workers",
                            modifier = Modifier.weight(0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AdminAccentOrange
                        )
                        Text(
                            text = "$totalInState Total",
                            modifier = Modifier.weight(0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminMainText
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            AdminStatusBadge(if (rule.isServiceEnabled) "ACTIVE" else "DISABLED")
                        }
                        Row(
                            modifier = Modifier.weight(1.6f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Switch(
                                checked = rule.isServiceEnabled,
                                onCheckedChange = { onToggleStateService(rule) }
                            )
                            OutlinedButton(
                                onClick = { onFilterUsersByState(rule.stateName) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("View Users", fontSize = 10.sp, color = AdminPrimaryNavy, maxLines = 1, softWrap = false)
                            }
                            IconButton(
                                onClick = { onDeleteStateRule(rule) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Remove Rule",
                                    tint = AdminDangerRed,
                                    modifier = Modifier.size(18.dp)
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
private fun AdminSidebarContent(
    currentRoute: AdminRoute,
    onSelectRoute: (AdminRoute) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(AdminPrimaryNavy)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                AdminHelmetLogo(size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Workora",
                        color = AdminWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Find. Hire. Work.",
                        color = Color(0xFFD1D5DB),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "ADMIN PANEL",
                color = AdminAccentOrange,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            AdminRoute.values().forEach { route ->
                val isSelected = currentRoute == route
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) AdminAccentOrange else Color.Transparent)
                        .clickable { onSelectRoute(route) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = route.icon,
                        contentDescription = route.title,
                        tint = AdminWhite,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = route.title,
                        color = if (isSelected) AdminWhite else Color(0xFFE5E7EB),
                        fontSize = 14.sp,
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
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = "Logout",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Logout",
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AdminTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    adminName: String,
    onToggleSidebar: () -> Unit,
    onRefreshClick: () -> Unit,
    onSwitchToCustomer: () -> Unit,
    onSwitchToWorker: () -> Unit,
    onOpenStateControl: () -> Unit,
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
                                    text = "Search users, workers, states, bookings...",
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
                            text = { Text("State / Area Service Control", fontWeight = FontWeight.SemiBold, color = AdminMainText) },
                            leadingIcon = {
                                Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = AdminSuccessGreen)
                            },
                            onClick = {
                                showAdminDropdown = false
                                onOpenStateControl()
                            }
                        )
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

@Composable
private fun AdminDashboardOverviewPage(
    usersList: List<AdminUserRecord>,
    bookingsList: List<AdminBookingRecord>,
    stateAreaRulesList: List<AdminStateAreaRule>,
    globalSearchQuery: String,
    onNavigateRoute: (AdminRoute) -> Unit,
    onViewUser: (AdminUserRecord) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit,
    onViewBooking: (AdminBookingRecord) -> Unit
) {
    val totalUsersDisplay = String.format(Locale.US, "%,d", usersList.size)
    val totalWorkersDisplay = String.format(Locale.US, "%,d", usersList.count { it.role == "Worker" })
    val totalCustomersDisplay = String.format(Locale.US, "%,d", usersList.count { it.role == "Customer" })
    val totalBookingsDisplay = String.format(Locale.US, "%,d", bookingsList.size)

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
        if (q.isEmpty()) usersList.take(5)
        else usersList.filter {
            it.name.contains(q, true) ||
                it.phone.contains(q, true) ||
                it.role.contains(q, true) ||
                it.area.contains(q, true) ||
                it.stateName.contains(q, true)
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

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                color = AdminInfoBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { onNavigateRoute(AdminRoute.STATE_CONTROL) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = AdminPrimaryNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "State / Area Control (${stateAreaRulesList.count { it.isServiceEnabled }} Active)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminPrimaryNavy
                    )
                }
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
                    Text("Today", fontSize = 11.sp, color = AdminSecondaryText)
                    Text(
                        text = SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AdminMainText
                    )
                }
            }
        }
    }

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
                percent = "Live",
                onClick = { onNavigateRoute(AdminRoute.USERS) }
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            AdminReferenceStatCard(
                icon = Icons.Outlined.Construction,
                circleColor = AdminAccentOrange,
                title = "Total Workers",
                number = totalWorkersDisplay,
                percent = "Live",
                onClick = { onNavigateRoute(AdminRoute.WORKERS) }
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            AdminReferenceStatCard(
                icon = Icons.Default.Person,
                circleColor = AdminSuccessGreen,
                title = "Total Customers",
                number = totalCustomersDisplay,
                percent = "Live",
                onClick = { onNavigateRoute(AdminRoute.CUSTOMERS) }
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            AdminReferenceStatCard(
                icon = Icons.Outlined.EventAvailable,
                circleColor = AdminPrimaryNavy,
                title = "Total Bookings",
                number = totalBookingsDisplay,
                percent = "Live",
                onClick = { onNavigateRoute(AdminRoute.BOOKINGS) }
            )
        }
    }

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
                    Text("Recent Bookings", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
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

        val workerUsers = usersList.filter { it.role.equals("Worker", true) }
        val availWorkers = workerUsers.count { it.availability.equals("Available", true) && !it.status.equals("Blocked", true) }
        val busyWorkers = workerUsers.count { it.availability.equals("Busy", true) }
        val blkWorkers = workerUsers.count { it.status.equals("Blocked", true) }
        val totalW = (availWorkers + busyWorkers + blkWorkers).coerceAtLeast(1)

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
                Text("Worker Availability", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

                WorkerAvailabilityBox(
                    icon = Icons.Outlined.Construction,
                    circleBg = AdminSuccessGreen,
                    label = "Available Workers",
                    count = availWorkers.toString(),
                    percentText = "${(availWorkers * 100) / totalW}%",
                    progress = availWorkers.toFloat() / totalW.toFloat(),
                    barColor = AdminSuccessGreen
                )

                WorkerAvailabilityBox(
                    icon = Icons.Outlined.Schedule,
                    circleBg = AdminAccentOrange,
                    label = "Busy Workers",
                    count = busyWorkers.toString(),
                    percentText = "${(busyWorkers * 100) / totalW}%",
                    progress = busyWorkers.toFloat() / totalW.toFloat(),
                    barColor = AdminAccentOrange
                )

                WorkerAvailabilityBox(
                    icon = Icons.Outlined.Block,
                    circleBg = AdminDangerRed,
                    label = "Blocked Workers",
                    count = blkWorkers.toString(),
                    percentText = "${(blkWorkers * 100) / totalW}%",
                    progress = blkWorkers.toFloat() / totalW.toFloat(),
                    barColor = AdminDangerRed
                )
            }
        }
    }

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
                Text("Recent Users", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
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
                Text("Name", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Role", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("State", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Area", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Status", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Joined", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Action", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            }

            filteredRecentUsers.forEach { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1.5f), verticalAlignment = Alignment.CenterVertically) {
                        AdminMiniAvatar(user.name, Color(0xFFE0E7FF), AdminPrimaryNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(user.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AdminMainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(user.role, modifier = Modifier.weight(0.9f), fontSize = 12.sp, color = AdminSecondaryText)
                    Text(user.stateName.ifBlank { "—" }, modifier = Modifier.weight(1.2f), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AdminPrimaryNavy, maxLines = 1)
                    Text(user.area.ifBlank { "—" }, modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminSecondaryText, maxLines = 1)
                    Box(modifier = Modifier.weight(0.9f)) {
                        AdminStatusBadge(user.status)
                    }
                    Text(user.joined, modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminSecondaryText)
                    Row(
                        modifier = Modifier.weight(1.5f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminActionPillButton(
                            label = "View",
                            borderColor = Color(0xFF93C5FD),
                            textColor = AdminInfoBlue,
                            onClick = { onViewUser(user) }
                        )
                        AdminActionPillButton(
                            label = if (user.status == "Blocked") "Unblock" else "Block",
                            borderColor = Color(0xFFFCA5A5),
                            textColor = AdminDangerRed,
                            onClick = { onConfirmBlockUser(user) }
                        )
                    }
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminActionPillButton(
    label: String,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = AdminWhite,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
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
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                Text(number, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AdminMainText)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(percent, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminSuccessGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("from database", fontSize = 11.sp, color = AdminSecondaryText)
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
                Icon(imageVector = icon, contentDescription = label, tint = AdminWhite, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                Text(count, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AdminMainText)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
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

@Composable
private fun AdminUsersPage(
    title: String,
    usersList: List<AdminUserRecord>,
    globalSearchQuery: String,
    onViewUser: (AdminUserRecord) -> Unit,
    onEditUser: (AdminUserRecord) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit
) {
    var selectedStateChip by remember { mutableStateOf("All States") }
    val stateOptions = remember(usersList) {
        listOf("All States") + usersList.map { it.stateName }.filter { it.isNotBlank() }.distinct()
    }

    val filtered = remember(usersList, globalSearchQuery, selectedStateChip) {
        usersList.filter { u ->
            val matchState = selectedStateChip == "All States" || u.stateName.equals(selectedStateChip, true)
            val matchSearch = globalSearchQuery.isBlank() ||
                u.name.contains(globalSearchQuery, true) ||
                u.phone.contains(globalSearchQuery, true) ||
                u.email.contains(globalSearchQuery, true) ||
                u.area.contains(globalSearchQuery, true) ||
                u.stateName.contains(globalSearchQuery, true) ||
                u.role.contains(globalSearchQuery, true)
            matchState && matchSearch
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$title (${filtered.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    stateOptions.forEach { st ->
                        val isSelected = selectedStateChip == st
                        Surface(
                            color = if (isSelected) AdminPrimaryNavy else AdminNeutralGrayBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { selectedStateChip = st }
                        ) {
                            Text(
                                text = st,
                                color = if (isSelected) AdminWhite else AdminMainText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AdminNeutralGrayBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Name", modifier = Modifier.weight(1.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Phone Number", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("State", modifier = Modifier.weight(1.1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Area / City", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Role", modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Status", modifier = Modifier.weight(0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Actions", modifier = Modifier.weight(1.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            }

            filtered.forEach { u ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1.4f), verticalAlignment = Alignment.CenterVertically) {
                        AdminMiniAvatar(u.name, Color(0xFFDBEAFE), AdminPrimaryNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(u.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminMainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(u.phone.ifBlank { "—" }, modifier = Modifier.weight(1.2f), fontSize = 12.sp, color = AdminMainText, maxLines = 1)
                    Text(u.stateName.ifBlank { "—" }, modifier = Modifier.weight(1.1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AdminPrimaryNavy, maxLines = 1)
                    Text(u.area.ifBlank { "—" }, modifier = Modifier.weight(1f), fontSize = 12.sp, color = AdminSecondaryText, maxLines = 1)
                    Text(u.role, modifier = Modifier.weight(0.8f), fontSize = 12.sp, color = AdminSecondaryText)
                    Box(modifier = Modifier.weight(0.8f)) {
                        AdminStatusBadge(u.status)
                    }
                    Row(
                        modifier = Modifier.weight(1.8f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AdminActionPillButton(
                            label = "View",
                            borderColor = AdminBorder,
                            textColor = AdminPrimaryNavy,
                            onClick = { onViewUser(u) }
                        )
                        AdminActionPillButton(
                            label = "Edit",
                            borderColor = AdminBorder,
                            textColor = AdminAccentOrange,
                            onClick = { onEditUser(u) }
                        )
                        AdminActionPillButton(
                            label = if (u.status == "Blocked") "Unblock" else "Block",
                            borderColor = Color(0xFFFCA5A5),
                            textColor = AdminDangerRed,
                            onClick = { onConfirmBlockUser(u) }
                        )
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
                it.stateName.contains(globalSearchQuery, true) ||
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
                Text("Worker Name", modifier = Modifier.weight(1.3f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Category", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("State", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Area", modifier = Modifier.weight(0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Rate", modifier = Modifier.weight(0.75f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Availability", modifier = Modifier.weight(0.85f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Actions", modifier = Modifier.weight(2.3f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            }

            filtered.forEach { w ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1.3f), verticalAlignment = Alignment.CenterVertically) {
                        AdminMiniAvatar(w.name, Color(0xFFFEF3C7), AdminWarningOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(w.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminMainText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(w.category.ifBlank { "—" }, modifier = Modifier.weight(0.9f), fontSize = 12.sp, color = AdminSecondaryText, maxLines = 1)
                    Text(w.stateName.ifBlank { "—" }, modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AdminPrimaryNavy, maxLines = 1)
                    Text(w.area.ifBlank { "—" }, modifier = Modifier.weight(0.9f), fontSize = 12.sp, color = AdminSecondaryText, maxLines = 1)
                    Text(
                        text = if (w.dailyRate > 0) "₹${w.dailyRate}/d" else "—",
                        modifier = Modifier.weight(0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AdminMainText,
                        maxLines = 1
                    )
                    Box(modifier = Modifier.weight(0.85f)) {
                        AdminStatusBadge(w.availability)
                    }
                    Row(
                        modifier = Modifier.weight(2.3f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AdminActionPillButton(
                            label = "View",
                            borderColor = AdminBorder,
                            textColor = AdminPrimaryNavy,
                            onClick = { onViewUser(w) }
                        )
                        AdminActionPillButton(
                            label = "Edit",
                            borderColor = AdminBorder,
                            textColor = AdminAccentOrange,
                            onClick = { onEditUser(w) }
                        )
                        val isActive = w.status.equals("Active", true)
                        AdminActionPillButton(
                            label = if (isActive) "Deactivate" else "Activate",
                            borderColor = AdminBorder,
                            textColor = AdminPrimaryNavy,
                            onClick = {
                                if (isActive) onUpdateWorkerStatus(w, "Inactive", "Busy")
                                else onUpdateWorkerStatus(w, "Active", "Available")
                            }
                        )
                        AdminActionPillButton(
                            label = if (w.status == "Blocked") "Unblock" else "Block",
                            borderColor = Color(0xFFFCA5A5),
                            textColor = AdminDangerRed,
                            onClick = { onConfirmBlockUser(w) }
                        )
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
                        AdminActionPillButton(
                            label = "View Details",
                            borderColor = AdminBorder,
                            textColor = AdminPrimaryNavy,
                            onClick = { onViewBooking(bk) }
                        )
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
                        AdminActionPillButton(
                            label = "Edit",
                            borderColor = AdminBorder,
                            textColor = AdminPrimaryNavy,
                            onClick = { onEditCategory(cat) }
                        )
                        AdminActionPillButton(
                            label = if (cat.active) "Deactivate" else "Activate",
                            borderColor = AdminBorder,
                            textColor = if (cat.active) AdminDangerRed else AdminSuccessGreen,
                            onClick = { onToggleCategoryActive(cat) }
                        )
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
    bookingsList: List<AdminBookingRecord>,
    stateRules: List<AdminStateAreaRule>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Reports & State Analytics", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            AdminDetailRow("Total users", usersList.size.toString())
            AdminDetailRow("Total workers", usersList.count { it.role == "Worker" }.toString())
            AdminDetailRow("Total customers", usersList.count { it.role == "Customer" }.toString())
            AdminDetailRow("Total bookings", bookingsList.size.toString())
            AdminDetailRow("Active States / Areas", stateRules.count { it.isServiceEnabled }.toString())
            AdminDetailRow("Disabled States / Areas", stateRules.count { !it.isServiceEnabled }.toString())
        }
    }
}

// =========================================================================
// REAL WORKING ADMIN SETTINGS PAGE
// =========================================================================
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
    var supportPhone by remember { mutableStateOf(settingsPrefs.getString("support_phone", "+91 6265798340") ?: "+91 6265798340") }
    var commissionRate by remember { mutableStateOf(settingsPrefs.getString("commission_rate", "0% (Direct)") ?: "0% (Direct)") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Admin & Application Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            Text("Manage global app branding, helpline, default marketplace location and commission tier.", fontSize = 13.sp, color = AdminSecondaryText)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = loggedAdminEmail,
                    onValueChange = {},
                    label = { Text("Super Admin Email") },
                    enabled = false,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = defaultArea,
                    onValueChange = { defaultArea = it },
                    label = { Text("Default Marketplace Area / City") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = supportPhone,
                    onValueChange = { supportPhone = it },
                    label = { Text("Helpline Phone Number") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = commissionRate,
                onValueChange = { commissionRate = it },
                label = { Text("Marketplace Commission Tier") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        brandingPrefs.edit().putString("app_name", appName.trim()).apply()
                        settingsPrefs.edit()
                            .putString("default_area", defaultArea.trim())
                            .putString("support_phone", supportPhone.trim())
                            .putString("commission_rate", commissionRate.trim())
                            .apply()
                        Toast.makeText(context, "Admin Settings Saved Successfully! ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Settings", color = AdminWhite, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSwitchToCustomer,
                    border = BorderStroke(1.dp, AdminPrimaryNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Customer Mode", color = AdminPrimaryNavy, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSwitchToWorker,
                    border = BorderStroke(1.dp, AdminAccentOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Worker Mode", color = AdminAccentOrange, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onRequestLogout,
                    border = BorderStroke(1.dp, AdminDangerRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Logout Admin", color = AdminDangerRed, fontWeight = FontWeight.Bold)
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
            text = name.trim().take(1).ifEmpty { "U" }.uppercase(Locale.US),
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
        "BLOCKED", "REJECTED", "CANCELLED", "DISABLED" -> Pair(AdminDangerBg, AdminDangerRed)
        else -> Pair(AdminNeutralGrayBg, AdminSecondaryText)
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            text = status,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
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

private fun syncDisabledAreasPrefs(
    settingsPrefs: android.content.SharedPreferences,
    rules: List<AdminStateAreaRule>
) {
    val disabledTokens = rules
        .filter { !it.isServiceEnabled }
        .flatMap { r ->
            listOf(r.stateName) + r.areaKeywords.split(",").map { it.trim() }
        }
        .filter { it.isNotBlank() }
        .joinToString("|")
    settingsPrefs.edit().putString("disabled_state_areas", disabledTokens).apply()
}

private fun loadAdminDataFromFirebase(
    onLoaded: (List<AdminUserRecord>, List<AdminBookingRecord>, List<AdminCategoryRecord>, List<AdminStateAreaRule>) -> Unit,
    onError: (String) -> Unit
) {
    Thread {
        val users = mutableListOf<AdminUserRecord>()
        val bookings = mutableListOf<AdminBookingRecord>()
        val categories = mutableListOf<AdminCategoryRecord>()
        val stateRules = mutableListOf<AdminStateAreaRule>()

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

                        val rawName = obj.optString("name", "").trim()
                        val rawPhone = obj.optString("phone", "").trim()
                        val rawEmail = obj.optString("email", k.replace("_at_", "@")).trim()
                        val rawArea = obj.optString("location", "").trim()
                        val rawState = inferStateFromArea(rawArea, obj.optString("state", "").trim())
                        val rawSkill = obj.optString("skill", "").trim()
                        val rawExp = obj.optString("experience", "").trim()
                        val rawRate = obj.optInt("dailyRate", 0)
                        val rawRating = obj.optDouble("rating", 0.0).let { if (it.isNaN()) 0.0 else it }
                        val rawBookings = obj.optInt("bookingsCount", 0)

                        users.add(
                            AdminUserRecord(
                                key = k,
                                name = rawName.ifEmpty { rawEmail.substringBefore("@") },
                                phone = rawPhone,
                                email = rawEmail,
                                role = roleClean,
                                area = rawArea,
                                stateName = rawState,
                                status = obj.optString("status", "Active"),
                                joined = obj.optString("joined", ""),
                                category = rawSkill,
                                experience = rawExp,
                                dailyRate = rawRate,
                                availability = obj.optString("availability", "Available"),
                                rating = rawRating,
                                bookingsCount = rawBookings
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
                                customerName = obj.optString("customerName", ""),
                                customerPhone = obj.optString("customerPhone", ""),
                                workerName = obj.optString("workerName", ""),
                                workerPhone = obj.optString("workerPhone", ""),
                                category = obj.optString("category", ""),
                                area = obj.optString("location", ""),
                                date = obj.optString("date", ""),
                                preferredTime = obj.optString("preferredTime", "09:00 AM"),
                                days = obj.optInt("workersNeeded", 1),
                                rate = obj.optInt("dailyRate", 0),
                                description = obj.optString("description", ""),
                                additionalMessage = obj.optString("urgency", ""),
                                status = obj.optString("status", "PENDING").uppercase(Locale.US),
                                createdDate = obj.optString("createdDate", ""),
                                updatedDate = obj.optString("updatedDate", "")
                            )
                        )
                        c++
                    }
                }
            }
            jobsConn.disconnect()

            val areaConn = URL("$ADMIN_FB_URL/area_controls.json").openConnection() as HttpURLConnection
            areaConn.requestMethod = "GET"
            areaConn.connectTimeout = 5000
            if (areaConn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(areaConn.inputStream)).use { it.readText() }
                if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                    val root = JSONObject(resp)
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val obj = root.optJSONObject(k) ?: continue
                        stateRules.add(
                            AdminStateAreaRule(
                                id = obj.optString("id", k),
                                stateName = obj.optString("stateName", ""),
                                areaKeywords = obj.optString("areaKeywords", ""),
                                isServiceEnabled = obj.optBoolean("isServiceEnabled", true)
                            )
                        )
                    }
                }
            }
            areaConn.disconnect()
        } catch (_: Exception) {
        }

        if (stateRules.isEmpty()) {
            stateRules.addAll(
                listOf(
                    AdminStateAreaRule("state_mp", "Madhya Pradesh", "Silwani, Raisen, Bhopal, Indore, Sagar, Vidisha, MP", true),
                    AdminStateAreaRule("state_delhi", "Delhi NCR", "Delhi, New Delhi, Gurgaon, Faridabad", true),
                    AdminStateAreaRule("state_up", "Uttar Pradesh", "Noida, Lucknow, Kanpur, Agra, Varanasi, UP", true),
                    AdminStateAreaRule("state_mh", "Maharashtra", "Mumbai, Pune, Nagpur, Nashik", true),
                    AdminStateAreaRule("state_rj", "Rajasthan", "Jaipur, Kota, Udaipur, Jodhpur", true),
                    AdminStateAreaRule("state_gj", "Gujarat", "Ahmedabad, Surat, Vadodara", true)
                )
            )
        }

        categories.addAll(
            listOf(
                AdminCategoryRecord("cat_1", "Mason", "hammer", true, users.count { it.category.equals("Mason", true) }),
                AdminCategoryRecord("cat_2", "General Labour", "hard-hat", true, users.count { it.category.equals("General Labour", true) }),
                AdminCategoryRecord("cat_3", "Painter", "paintbrush", true, users.count { it.category.equals("Painter", true) }),
                AdminCategoryRecord("cat_4", "Electrician", "zap", true, users.count { it.category.equals("Electrician", true) }),
                AdminCategoryRecord("cat_5", "Plumber", "wrench", true, users.count { it.category.equals("Plumber", true) }),
                AdminCategoryRecord("cat_6", "Carpenter", "tool", true, users.count { it.category.equals("Carpenter", true) }),
                AdminCategoryRecord("cat_7", "Cleaner", "sparkles", true, users.count { it.category.equals("Cleaner", true) }),
                AdminCategoryRecord("cat_8", "Farm Worker", "sprout", true, users.count { it.category.equals("Farm Worker", true) }),
                AdminCategoryRecord("cat_9", "Tile Worker", "grid", true, users.count { it.category.equals("Tile Worker", true) }),
                AdminCategoryRecord("cat_10", "Other", "more", true, users.count { it.category.equals("Other", true) })
            )
        )

        Handler(Looper.getMainLooper()).post {
            onLoaded(users, bookings, categories, stateRules)
        }
    }.start()
}

private fun saveStateAreaRuleToFirebase(rule: AdminStateAreaRule) {
    Thread {
        try {
            val url = URL("$ADMIN_FB_URL/area_controls/${rule.id}.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("id", rule.id)
                put("stateName", rule.stateName)
                put("areaKeywords", rule.areaKeywords)
                put("isServiceEnabled", rule.isServiceEnabled)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun deleteStateAreaRuleFromFirebase(ruleId: String) {
    Thread {
        try {
            val url = URL("$ADMIN_FB_URL/area_controls/$ruleId.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "DELETE"
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
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
                put("state", user.stateName)
                put("location", user.area)
                put("skill", user.category)
                put("experience", user.experience)
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
            OutputStreamWriter(conn.outputStream).use { it.word(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}
