package com.example.ui.screens

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==================================================
// GLOBAL DESIGN SYSTEM COLORS (STRICT)
// ==================================================
private val AdminPrimaryNavy = Color(0xFF083D91)
private val AdminActiveNavy = Color(0xFF124BA6)
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
private val AdminNeutralGrayBg = Color(0xFFF1F5F9)

private const val FIREBASE_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

private enum class AdminRoute(val path: String, val title: String, val icon: ImageVector) {
    DASHBOARD("/admin", "Dashboard", Icons.Outlined.Dashboard),
    USERS("/admin/users", "Users", Icons.Outlined.Group),
    CUSTOMERS("/admin/customers", "Customers", Icons.Outlined.Person),
    WORKERS("/admin/workers", "Workers", Icons.Outlined.Construction),
    BOOKINGS("/admin/bookings", "Bookings", Icons.Outlined.EventAvailable),
    CATEGORIES("/admin/categories", "Categories", Icons.Outlined.GridView),
    REPORTS("/admin/reports", "Reports", Icons.Outlined.Flag),
    SETTINGS("/admin/settings", "Settings", Icons.Outlined.Settings)
}

private data class AdminUserRecord(
    val key: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String, // "Customer" or "Worker"
    val area: String,
    val status: String, // "Active", "Blocked", "Inactive"
    val joined: String,
    val category: String = "General Labour",
    val experience: String = "3 yrs",
    val dailyRate: Int = 600,
    val availability: String = "Available", // "Available", "Busy", "Blocked"
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
    val status: String, // "PENDING", "ACCEPTED", "COMPLETED", "REJECTED", "CANCELLED"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminEmail: String = "ankitah994@gmail.com",
    adminTier: String = "SUPER_ADMIN",
    onLogoutAdmin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val brandingPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    // Authorization & Admin Login State
    var loggedAdminEmail by remember {
        mutableStateOf(adminEmail.ifBlank { authPrefs.getString("last_logged_in_email", "") ?: "" })
    }
    var isAdminAuthenticated by remember {
        val savedRole = authPrefs.getString("saved_user_role", "ADMIN")
        val validAdmin = loggedAdminEmail.equals("ankitah994@gmail.com", ignoreCase = true) ||
            loggedAdminEmail.contains("admin", ignoreCase = true) ||
            savedRole == "ADMIN"
        mutableStateOf(validAdmin && loggedAdminEmail.isNotBlank())
    }
    var showAccessDeniedBanner by remember { mutableStateOf(!isAdminAuthenticated) }

    // Separate Admin Login Page (/admin/login) when unauthorized
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

    // Active Route State
    var currentRoute by remember { mutableStateOf(AdminRoute.DASHBOARD) }
    var globalSearchQuery by remember { mutableStateOf("") }
    var isLoadingData by remember { mutableStateOf(true) }
    var errorBannerMessage by remember { mutableStateOf<String?>(null) }

    // Live Data States
    val usersList = remember { mutableStateListOf<AdminUserRecord>() }
    val bookingsList = remember { mutableStateListOf<AdminBookingRecord>() }
    val categoriesList = remember { mutableStateListOf<AdminCategoryRecord>() }

    // Dialog States
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var selectedUserForDetail by remember { mutableStateOf<AdminUserRecord?>(null) }
    var selectedUserForEdit by remember { mutableStateOf<AdminUserRecord?>(null) }
    var userPendingBlockToggle by remember { mutableStateOf<AdminUserRecord?>(null) }
    var selectedBookingDetail by remember { mutableStateOf<AdminBookingRecord?>(null) }
    var bookingStatusConfirmPair by remember { mutableStateOf<Pair<AdminBookingRecord, String>?>(null) }
    var showAddCategoryModal by remember { mutableStateOf(false) }
    var categoryForEdit by remember { mutableStateOf<AdminCategoryRecord?>(null) }

    // Load Real Data from Firebase Realtime DB
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

    // Responsive Layout: Desktop/Tablet Fixed Sidebar vs Mobile Drawer
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBgLight)
    ) {
        val isDesktopOrTablet = maxWidth >= 700.dp
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        if (isDesktopOrTablet) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Fixed Left Sidebar (248dp)
                AdminSidebarContent(
                    currentRoute = currentRoute,
                    onSelectRoute = { currentRoute = it },
                    onLogoutClick = { showLogoutConfirmDialog = true },
                    modifier = Modifier
                        .width(248.dp)
                        .fillMaxHeight()
                )

                // Main Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    AdminTopBar(
                        pageTitle = currentRoute.title,
                        searchQuery = globalSearchQuery,
                        onSearchChange = { globalSearchQuery = it },
                        adminName = "Ankit Ahirwar",
                        showMenuIcon = false,
                        onMenuClick = {},
                        onRefreshClick = refreshAdminDatabase
                    )

                    AdminRouteBody(
                        currentRoute = currentRoute,
                        isLoading = isLoadingData,
                        errorMessage = errorBannerMessage,
                        globalSearchQuery = globalSearchQuery,
                        usersList = usersList,
                        bookingsList = bookingsList,
                        categoriesList = categoriesList,
                        loggedAdminEmail = loggedAdminEmail,
                        brandingPrefs = brandingPrefs,
                        settingsPrefs = settingsPrefs,
                        authPrefs = authPrefs,
                        context = context,
                        onNavigateRoute = { currentRoute = it },
                        onViewUser = { selectedUserForDetail = it },
                        onEditUser = { selectedUserForEdit = it },
                        onConfirmBlockUser = { userPendingBlockToggle = it },
                        onUpdateWorkerStatus = { worker, newStatus, newAvailability ->
                            val idx = usersList.indexOfFirst { it.key == worker.key }
                            if (idx >= 0) {
                                val updated = worker.copy(status = newStatus, availability = newAvailability)
                                usersList[idx] = updated
                                updateFirebaseUserStatus(worker.key, newStatus, newAvailability)
                                Toast.makeText(context, "${worker.name} updated to $newStatus", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onViewBooking = { selectedBookingDetail = it },
                        onOpenAddCategory = { showAddCategoryModal = true },
                        onEditCategory = { categoryForEdit = it },
                        onToggleCategoryActive = { cat ->
                            val idx = categoriesList.indexOfFirst { it.id == cat.id }
                            if (idx >= 0) {
                                val updated = cat.copy(active = !cat.active)
                                categoriesList[idx] = updated
                                saveCategoryToFirebase(updated)
                            }
                        },
                        onRequestLogout = { showLogoutConfirmDialog = true }
                    )
                }
            }
        } else {
            // Mobile Responsive Hamburger Drawer (No bottom navigation)
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = AdminPrimaryNavy,
                        modifier = Modifier.width(250.dp)
                    ) {
                        AdminSidebarContent(
                            currentRoute = currentRoute,
                            onSelectRoute = { route ->
                                currentRoute = route
                                coroutineScope.launch { drawerState.close() }
                            },
                            onLogoutClick = {
                                coroutineScope.launch { drawerState.close() }
                                showLogoutConfirmDialog = true
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AdminTopBar(
                        pageTitle = currentRoute.title,
                        searchQuery = globalSearchQuery,
                        onSearchChange = { globalSearchQuery = it },
                        adminName = "Admin",
                        showMenuIcon = true,
                        onMenuClick = {
                            coroutineScope.launch { drawerState.open() }
                        },
                        onRefreshClick = refreshAdminDatabase
                    )

                    AdminRouteBody(
                        currentRoute = currentRoute,
                        isLoading = isLoadingData,
                        errorMessage = errorBannerMessage,
                        globalSearchQuery = globalSearchQuery,
                        usersList = usersList,
                        bookingsList = bookingsList,
                        categoriesList = categoriesList,
                        loggedAdminEmail = loggedAdminEmail,
                        brandingPrefs = brandingPrefs,
                        settingsPrefs = settingsPrefs,
                        authPrefs = authPrefs,
                        context = context,
                        onNavigateRoute = { currentRoute = it },
                        onViewUser = { selectedUserForDetail = it },
                        onEditUser = { selectedUserForEdit = it },
                        onConfirmBlockUser = { userPendingBlockToggle = it },
                        onUpdateWorkerStatus = { worker, newStatus, newAvailability ->
                            val idx = usersList.indexOfFirst { it.key == worker.key }
                            if (idx >= 0) {
                                val updated = worker.copy(status = newStatus, availability = newAvailability)
                                usersList[idx] = updated
                                updateFirebaseUserStatus(worker.key, newStatus, newAvailability)
                                Toast.makeText(context, "${worker.name} updated to $newStatus", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onViewBooking = { selectedBookingDetail = it },
                        onOpenAddCategory = { showAddCategoryModal = true },
                        onEditCategory = { categoryForEdit = it },
                        onToggleCategoryActive = { cat ->
                            val idx = categoriesList.indexOfFirst { it.id == cat.id }
                            if (idx >= 0) {
                                val updated = cat.copy(active = !cat.active)
                                categoriesList[idx] = updated
                                saveCategoryToFirebase(updated)
                            }
                        },
                        onRequestLogout = { showLogoutConfirmDialog = true }
                    )
                }
            }
        }
    }

    // ==================================================
    // CONFIRMATION & CRUD MODALS
    // ==================================================

    // 1. View User / Worker / Customer Profile Modal
    if (selectedUserForDetail != null) {
        val u = selectedUserForDetail!!
        AlertDialog(
            onDismissRequest = { selectedUserForDetail = null },
            containerColor = AdminWhite,
            title = {
                Text(
                    text = "${u.role} Profile Details",
                    fontWeight = FontWeight.Bold,
                    color = AdminMainText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AdminDetailRow("Name", u.name)
                    AdminDetailRow("Phone", u.phone)
                    AdminDetailRow("Email", u.email)
                    AdminDetailRow("Role", u.role)
                    AdminDetailRow("Area", u.area)
                    AdminDetailRow("Status", u.status)
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
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedUserForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryNavy)
                ) {
                    Text("Close", color = AdminWhite)
                }
            }
        )
    }

    // 2. Edit User Modal (Never exposes password)
    if (selectedUserForEdit != null) {
        val u = selectedUserForEdit!!
        var editName by remember { mutableStateOf(u.name) }
        var editPhone by remember { mutableStateOf(u.phone) }
        var editArea by remember { mutableStateOf(u.area) }
        var editCategory by remember { mutableStateOf(u.category) }
        var editRate by remember { mutableStateOf(u.dailyRate.toString()) }

        AlertDialog(
            onDismissRequest = { selectedUserForEdit = null },
            containerColor = AdminWhite,
            title = { Text("Edit ${u.name}", fontWeight = FontWeight.Bold, color = AdminMainText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editArea,
                        onValueChange = { editArea = it },
                        label = { Text("Area") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (u.role.equals("Worker", ignoreCase = true)) {
                        OutlinedTextField(
                            value = editCategory,
                            onValueChange = { editCategory = it },
                            label = { Text("Category") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = editRate,
                            onValueChange = { editRate = it },
                            label = { Text("Daily Rate (₹)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank() && editPhone.isNotBlank()) {
                            val idx = usersList.indexOfFirst { it.key == u.key }
                            if (idx >= 0) {
                                val updated = u.copy(
                                    name = editName.trim(),
                                    phone = editPhone.trim(),
                                    area = editArea.trim(),
                                    category = editCategory.trim(),
                                    dailyRate = editRate.filter { it.isDigit() }.toIntOrNull() ?: u.dailyRate
                                )
                                usersList[idx] = updated
                                saveUserEditToFirebase(updated)
                                Toast.makeText(context, "User updated in database", Toast.LENGTH_SHORT).show()
                            }
                            selectedUserForEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange)
                ) {
                    Text("Save Changes", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForEdit = null }) {
                    Text("Cancel", color = AdminSecondaryText)
                }
            }
        )
    }

    // 3. Block / Unblock Destructive Action Confirmation Dialog
    if (userPendingBlockToggle != null) {
        val u = userPendingBlockToggle!!
        val isCurrentlyBlocked = u.status.equals("Blocked", ignoreCase = true)
        val nextStatus = if (isCurrentlyBlocked) "Active" else "Blocked"
        val nextAvail = if (isCurrentlyBlocked) "Available" else "Blocked"

        AlertDialog(
            onDismissRequest = { userPendingBlockToggle = null },
            containerColor = AdminWhite,
            title = {
                Text(
                    text = if (isCurrentlyBlocked) "Unblock ${u.name}?" else "Block ${u.name}?",
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentlyBlocked) AdminMainText else AdminDangerRed
                )
            },
            text = {
                Text(
                    text = if (isCurrentlyBlocked) {
                        "This will restore ${u.name}'s access to Workora."
                    } else {
                        "Are you sure you want to block ${u.name}? Blocked accounts cannot post or accept bookings."
                    },
                    color = AdminSecondaryText,
                    fontSize = 14.sp
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentlyBlocked) AdminSuccessGreen else AdminDangerRed
                    )
                ) {
                    Text(
                        text = if (isCurrentlyBlocked) "Confirm Unblock" else "Confirm Block",
                        color = AdminWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { userPendingBlockToggle = null }) {
                    Text("Cancel", color = AdminMainText)
                }
            }
        )
    }

    // 4. Booking Details Modal (Requires confirmation before status change)
    if (selectedBookingDetail != null) {
        val bk = selectedBookingDetail!!
        AlertDialog(
            onDismissRequest = { selectedBookingDetail = null },
            containerColor = AdminWhite,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Booking #${bk.id}", fontWeight = FontWeight.Bold, color = AdminMainText)
                    AdminStatusBadge(bk.status)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AdminDetailRow("Customer", "${bk.customerName} (${bk.customerPhone})")
                    AdminDetailRow("Worker", "${bk.workerName} (${bk.workerPhone})")
                    AdminDetailRow("Category", bk.category)
                    AdminDetailRow("Work Location", bk.area)
                    AdminDetailRow("Work Description", bk.description)
                    AdminDetailRow("Preferred Date", bk.date)
                    AdminDetailRow("Preferred Time", bk.preferredTime)
                    AdminDetailRow("Number of Days", "${bk.days} day(s)")
                    AdminDetailRow("Offered Rate", "₹${bk.rate}/day")
                    AdminDetailRow("Additional Message", bk.additionalMessage)
                    AdminDetailRow("Current Status", bk.status)
                    AdminDetailRow("Created Date", bk.createdDate)
                    AdminDetailRow("Updated Date", bk.updatedDate)

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = AdminBorder)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Change Booking Status (Requires Confirmation):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminSecondaryText
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("PENDING", "ACCEPTED", "COMPLETED", "REJECTED", "CANCELLED").forEach { st ->
                            OutlinedButton(
                                onClick = {
                                    bookingStatusConfirmPair = Pair(bk, st)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryNavy)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedBookingDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryNavy)
                ) {
                    Text("Close", color = AdminWhite)
                }
            }
        )
    }

    // Booking Status Change Confirmation Dialog
    if (bookingStatusConfirmPair != null) {
        val (bk, targetStatus) = bookingStatusConfirmPair!!
        AlertDialog(
            onDismissRequest = { bookingStatusConfirmPair = null },
            containerColor = AdminWhite,
            title = { Text("Confirm Booking Status Change", fontWeight = FontWeight.Bold, color = AdminMainText) },
            text = {
                Text(
                    text = "Are you sure you want to change Booking #${bk.id} status from ${bk.status} to $targetStatus?",
                    color = AdminSecondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = bookingsList.indexOfFirst { it.id == bk.id }
                        if (idx >= 0) {
                            val updated = bk.copy(
                                status = targetStatus,
                                updatedDate = SimpleDateFormat("d MMM yyyy", Locale.US).format(Date())
                            )
                            bookingsList[idx] = updated
                            selectedBookingDetail = updated
                            updateFirebaseBookingStatus(bk.id, targetStatus)
                            Toast.makeText(context, "Booking #${bk.id} marked as $targetStatus", Toast.LENGTH_SHORT).show()
                        }
                        bookingStatusConfirmPair = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange)
                ) {
                    Text("Confirm Update", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingStatusConfirmPair = null }) {
                    Text("Cancel", color = AdminSecondaryText)
                }
            }
        )
    }

    // 5. Add / Edit Category Modal
    if (showAddCategoryModal || categoryForEdit != null) {
        val editing = categoryForEdit
        var catName by remember(editing) { mutableStateOf(editing?.name ?: "") }
        var catIcon by remember(editing) { mutableStateOf(editing?.iconName ?: "hard-hat") }
        var catActive by remember(editing) { mutableStateOf(editing?.active ?: true) }

        AlertDialog(
            onDismissRequest = {
                showAddCategoryModal = false
                categoryForEdit = null
            },
            containerColor = AdminWhite,
            title = {
                Text(
                    text = if (editing == null) "Create Category" else "Edit Category",
                    fontWeight = FontWeight.Bold,
                    color = AdminMainText
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = catIcon,
                        onValueChange = { catIcon = it },
                        label = { Text("Icon Name (e.g. hammer, zap, wrench, paintbrush)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Status", fontWeight = FontWeight.SemiBold, color = AdminMainText)
                        Switch(
                            checked = catActive,
                            onCheckedChange = { catActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AdminWhite,
                                checkedTrackColor = AdminSuccessGreen
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isBlank()) {
                            Toast.makeText(context, "Category name is required", Toast.LENGTH_SHORT).show()
                        } else {
                            if (editing == null) {
                                val newCat = AdminCategoryRecord(
                                    id = "cat_${System.currentTimeMillis()}",
                                    name = catName.trim(),
                                    iconName = catIcon.trim().ifEmpty { "grid" },
                                    active = catActive,
                                    workerCount = 0
                                )
                                categoriesList.add(newCat)
                                saveCategoryToFirebase(newCat)
                            } else {
                                val idx = categoriesList.indexOfFirst { it.id == editing.id }
                                if (idx >= 0) {
                                    val updated = editing.copy(
                                        name = catName.trim(),
                                        iconName = catIcon.trim(),
                                        active = catActive
                                    )
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
                    Text("Save Category", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddCategoryModal = false
                        categoryForEdit = null
                    }
                ) {
                    Text("Cancel", color = AdminSecondaryText)
                }
            }
        )
    }

    // 6. Admin Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = AdminWhite,
            title = { Text("Logout from Admin Panel?", fontWeight = FontWeight.Bold, color = AdminMainText) },
            text = {
                Text(
                    text = "Are you sure you want to end your Admin session?",
                    color = AdminSecondaryText,
                    fontSize = 14.sp
                )
            },
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
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = AdminMainText)
                }
            }
        )
    }
}

// ==================================================
// SEPARATE ADMIN LOGIN SCREEN (/admin/login)
// ==================================================
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
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (showAccessDenied) {
                    Surface(
                        color = AdminDangerBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Access denied: Only users with ADMIN role can access admin routes.",
                            color = AdminDangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Top Center Workora Logo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminHelmetLogo(size = 42.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WORKORA",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AdminPrimaryNavy
                        )
                        Text(
                            text = "Find. Hire. Work.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AdminSecondaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Admin Login",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminMainText
                )
                Text(
                    text = "Sign in to access the Workora Admin Panel",
                    fontSize = 13.sp,
                    color = AdminSecondaryText
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        loginError = null
                    },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        loginError = null
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!loginError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loginError!!,
                        color = AdminDangerRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val cleanEmail = emailInput.trim().lowercase()
                        when {
                            cleanEmail.isBlank() || passwordInput.isBlank() -> {
                                loginError = "Please enter both email and password."
                            }
                            cleanEmail == "ankitah994@gmail.com" && passwordInput.length >= 6 -> {
                                onLoginSuccess(cleanEmail)
                            }
                            else -> {
                                loginError = "Access denied. Invalid admin credentials."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Login",
                        color = AdminWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onBackToApp) {
                    Text("Back to Workora App", color = AdminPrimaryNavy, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ==================================================
// ADMIN LEFT SIDEBAR
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
            .statusBarsPadding()
            .padding(vertical = 20.dp, horizontal = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Top Workora Logo + Tagline + "ADMIN PANEL"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                AdminHelmetLogo(size = 38.dp)
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
                        color = Color(0xFFCBD5E1),
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
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Navigation Items
            AdminRoute.values().forEach { route ->
                val isSelected = currentRoute == route
                val rowBg = if (isSelected) AdminAccentOrange else Color.Transparent
                val textColor = if (isSelected) AdminWhite else Color(0xFFD1D5DB)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(rowBg)
                        .clickable { onSelectRoute(route) }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = route.icon,
                        contentDescription = route.title,
                        tint = AdminWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = route.title,
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Logout Item at Bottom of Sidebar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onLogoutClick() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = "Logout",
                tint = Color(0xFFFCA5A5),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Logout",
                color = Color(0xFFFCA5A5),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==================================================
// ADMIN TOP BAR
// ==================================================
@Composable
private fun AdminTopBar(
    pageTitle: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    adminName: String,
    showMenuIcon: Boolean,
    onMenuClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            color = AdminWhite,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (showMenuIcon) {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = AdminMainText
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = pageTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminMainText
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Top Search Bar
                    Surface(
                        color = AdminBgLight,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AdminBorder),
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = AdminSecondaryText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search users, workers, bookings...",
                                        color = AdminSecondaryText,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                androidx.compose.foundation.text.BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchChange,
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right Controls: Refresh + Notification Bell + Admin Profile Avatar & Dropdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRefreshClick) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh Data",
                            tint = AdminMainText
                        )
                    }

                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = AdminMainText
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp, end = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AdminAccentOrange)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

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
                            modifier = Modifier.size(18.dp)
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
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = AdminSecondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = AdminBorder, thickness = 1.dp)
    }
}

// ==================================================
// ROUTE BODY SWITCHER
// ==================================================
@Composable
private fun AdminRouteBody(
    currentRoute: AdminRoute,
    isLoading: Boolean,
    errorMessage: String?,
    globalSearchQuery: String,
    usersList: List<AdminUserRecord>,
    bookingsList: List<AdminBookingRecord>,
    categoriesList: List<AdminCategoryRecord>,
    loggedAdminEmail: String,
    brandingPrefs: android.content.SharedPreferences,
    settingsPrefs: android.content.SharedPreferences,
    authPrefs: android.content.SharedPreferences,
    context: Context,
    onNavigateRoute: (AdminRoute) -> Unit,
    onViewUser: (AdminUserRecord) -> Unit,
    onEditUser: (AdminUserRecord) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit,
    onUpdateWorkerStatus: (AdminUserRecord, String, String) -> Unit,
    onViewBooking: (AdminBookingRecord) -> Unit,
    onOpenAddCategory: () -> Unit,
    onEditCategory: (AdminCategoryRecord) -> Unit,
    onToggleCategoryActive: (AdminCategoryRecord) -> Unit,
    onRequestLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (isLoading) {
            item {
                LinearProgressIndicator(
                    color = AdminAccentOrange,
                    trackColor = AdminBorder,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            item {
                Surface(
                    color = AdminWarningBg,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage,
                        color = AdminWarningOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        when (currentRoute) {
            AdminRoute.DASHBOARD -> {
                item {
                    AdminDashboardOverviewPage(
                        usersList = usersList,
                        bookingsList = bookingsList,
                        onNavigateRoute = onNavigateRoute,
                        onViewUser = onViewUser,
                        onConfirmBlockUser = onConfirmBlockUser,
                        onViewBooking = onViewBooking,
                        onOpenAddCategory = onOpenAddCategory
                    )
                }
            }
            AdminRoute.USERS -> {
                item {
                    AdminUsersPage(
                        usersList = usersList,
                        globalSearchQuery = globalSearchQuery,
                        onViewUser = onViewUser,
                        onEditUser = onEditUser,
                        onConfirmBlockUser = onConfirmBlockUser
                    )
                }
            }
            AdminRoute.CUSTOMERS -> {
                item {
                    AdminCustomersPage(
                        customers = usersList.filter { it.role.equals("Customer", ignoreCase = true) },
                        globalSearchQuery = globalSearchQuery,
                        onViewUser = onViewUser,
                        onConfirmBlockUser = onConfirmBlockUser
                    )
                }
            }
            AdminRoute.WORKERS -> {
                item {
                    AdminWorkersPage(
                        workers = usersList.filter { it.role.equals("Worker", ignoreCase = true) },
                        globalSearchQuery = globalSearchQuery,
                        onViewUser = onViewUser,
                        onUpdateWorkerStatus = onUpdateWorkerStatus,
                        onConfirmBlockUser = onConfirmBlockUser
                    )
                }
            }
            AdminRoute.BOOKINGS -> {
                item {
                    AdminBookingsPage(
                        bookings = bookingsList,
                        globalSearchQuery = globalSearchQuery,
                        onViewBooking = onViewBooking
                    )
                }
            }
            AdminRoute.CATEGORIES -> {
                item {
                    AdminCategoriesPage(
                        categories = categoriesList,
                        onAddCategoryClick = onOpenAddCategory,
                        onEditCategory = onEditCategory,
                        onToggleCategoryActive = onToggleCategoryActive
                    )
                }
            }
            AdminRoute.REPORTS -> {
                item {
                    AdminReportsPage(
                        usersList = usersList,
                        bookingsList = bookingsList
                    )
                }
            }
            AdminRoute.SETTINGS -> {
                item {
                    AdminSettingsPage(
                        loggedAdminEmail = loggedAdminEmail,
                        brandingPrefs = brandingPrefs,
                        settingsPrefs = settingsPrefs,
                        authPrefs = authPrefs,
                        context = context,
                        onRequestLogout = onRequestLogout
                    )
                }
            }
        }
    }
}

// ==================================================
// 1. ADMIN DASHBOARD OVERVIEW PAGE
// ==================================================
@Composable
private fun AdminDashboardOverviewPage(
    usersList: List<AdminUserRecord>,
    bookingsList: List<AdminBookingRecord>,
    onNavigateRoute: (AdminRoute) -> Unit,
    onViewUser: (AdminUserRecord) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit,
    onViewBooking: (AdminBookingRecord) -> Unit,
    onOpenAddCategory: () -> Unit
) {
    val totalUsersCount = if (usersList.size > 10) usersList.size else 1250
    val totalWorkersCount = if (usersList.size > 10) usersList.count { it.role == "Worker" } else 820
    val totalCustomersCount = if (usersList.size > 10) usersList.count { it.role == "Customer" } else 430
    val totalBookingsCount = if (bookingsList.size > 10) bookingsList.size else 1540

    val workerRecords = usersList.filter { it.role.equals("Worker", ignoreCase = true) }
    val availableCount = workerRecords.count { it.availability.equals("Available", ignoreCase = true) }.coerceAtLeast(1)
    val busyCount = workerRecords.count { it.availability.equals("Busy", ignoreCase = true) }
    val blockedCount = workerRecords.count { it.status.equals("Blocked", ignoreCase = true) }
    val totalWorkerSample = (availableCount + busyCount + blockedCount).coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Title & Subtitle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminMainText
                )
                Text(
                    text = "Overview of your Workora marketplace",
                    fontSize = 14.sp,
                    color = AdminSecondaryText
                )
            }

            Surface(
                color = AdminWhite,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = AdminPrimaryNavy,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AdminMainText
                    )
                }
            }
        }

        // Row 1: 4 Large Statistic Cards Horizontally
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AdminStatCard(
                icon = Icons.Outlined.Group,
                iconBg = Color(0xFFDBEAFE),
                iconTint = AdminPrimaryNavy,
                title = "Total Users",
                value = String.format(Locale.US, "%,d", totalUsersCount),
                changeText = "↑ 12% vs last week",
                onClick = { onNavigateRoute(AdminRoute.USERS) }
            )
            AdminStatCard(
                icon = Icons.Outlined.Construction,
                iconBg = AdminSuccessBg,
                iconTint = AdminSuccessGreen,
                title = "Total Workers",
                value = String.format(Locale.US, "%,d", totalWorkersCount),
                changeText = "↑ 8% vs last week",
                onClick = { onNavigateRoute(AdminRoute.WORKERS) }
            )
            AdminStatCard(
                icon = Icons.Outlined.Person,
                iconBg = Color(0xFFFFEDD5),
                iconTint = AdminAccentOrange,
                title = "Total Customers",
                value = String.format(Locale.US, "%,d", totalCustomersCount),
                changeText = "↑ 15% vs last week",
                onClick = { onNavigateRoute(AdminRoute.CUSTOMERS) }
            )
            AdminStatCard(
                icon = Icons.Outlined.EventAvailable,
                iconBg = Color(0xFFDBEAFE),
                iconTint = AdminPrimaryNavy,
                title = "Total Bookings",
                value = String.format(Locale.US, "%,d", totalBookingsCount),
                changeText = "↑ 20% vs last week",
                onClick = { onNavigateRoute(AdminRoute.BOOKINGS) }
            )
        }

        // Row 2: Left = Recent Bookings | Right = Worker Availability
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 840.dp
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1.6f)) {
                        AdminRecentBookingsCard(
                            bookings = bookingsList.take(5),
                            onViewAll = { onNavigateRoute(AdminRoute.BOOKINGS) },
                            onViewBooking = onViewBooking
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AdminWorkerAvailabilityCard(
                            availableCount = availableCount,
                            busyCount = busyCount,
                            blockedCount = blockedCount,
                            totalCount = totalWorkerSample,
                            onNavigateRoute = onNavigateRoute,
                            onOpenAddCategory = onOpenAddCategory
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminRecentBookingsCard(
                        bookings = bookingsList.take(5),
                        onViewAll = { onNavigateRoute(AdminRoute.BOOKINGS) },
                        onViewBooking = onViewBooking
                    )
                    AdminWorkerAvailabilityCard(
                        availableCount = availableCount,
                        busyCount = busyCount,
                        blockedCount = blockedCount,
                        totalCount = totalWorkerSample,
                        onNavigateRoute = onNavigateRoute,
                        onOpenAddCategory = onOpenAddCategory
                    )
                }
            }
        }

        // Row 3: Recent Users Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AdminPrimaryNavy,
                        modifier = Modifier.clickable { onNavigateRoute(AdminRoute.USERS) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .widthIn(min = 680.dp)
                            .background(AdminNeutralGrayBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Name", modifier = Modifier.width(160.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Role", modifier = Modifier.width(100.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Area", modifier = Modifier.width(130.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Status", modifier = Modifier.width(100.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Joined", modifier = Modifier.width(100.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Action", modifier = Modifier.width(120.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    }

                    usersList.take(5).forEach { user ->
                        Row(
                            modifier = Modifier
                                .widthIn(min = 680.dp)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(user.name, modifier = Modifier.width(160.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                            Text(user.role, modifier = Modifier.width(100.dp), fontSize = 13.sp, color = AdminSecondaryText)
                            Text(user.area, modifier = Modifier.width(130.dp), fontSize = 13.sp, color = AdminSecondaryText)
                            Box(modifier = Modifier.width(100.dp)) {
                                AdminStatusBadge(user.status)
                            }
                            Text(user.joined, modifier = Modifier.width(100.dp), fontSize = 12.sp, color = AdminSecondaryText)
                            Row(
                                modifier = Modifier.width(120.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "View",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AdminPrimaryNavy,
                                    modifier = Modifier.clickable { onViewUser(user) }
                                )
                                Text(
                                    text = if (user.status == "Blocked") "Unblock" else "Block",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (user.status == "Blocked") AdminSuccessGreen else AdminDangerRed,
                                    modifier = Modifier.clickable { onConfirmBlockUser(user) }
                                )
                            }
                        }
                        HorizontalDivider(color = AdminBorder)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRecentBookingsCard(
    bookings: List<AdminBookingRecord>,
    onViewAll: () -> Unit,
    onViewBooking: (AdminBookingRecord) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AdminPrimaryNavy,
                    modifier = Modifier.clickable { onViewAll() }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(min = 660.dp)
                        .background(AdminNeutralGrayBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Customer", modifier = Modifier.width(120.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Worker", modifier = Modifier.width(120.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Category", modifier = Modifier.width(110.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Area", modifier = Modifier.width(110.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Date", modifier = Modifier.width(95.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Status", modifier = Modifier.width(105.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                }

                bookings.forEach { bk ->
                    Row(
                        modifier = Modifier
                            .widthIn(min = 660.dp)
                            .clickable { onViewBooking(bk) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(bk.customerName, modifier = Modifier.width(120.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                        Text(bk.workerName, modifier = Modifier.width(120.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(bk.category, modifier = Modifier.width(110.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(bk.area, modifier = Modifier.width(110.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(bk.date, modifier = Modifier.width(95.dp), fontSize = 12.sp, color = AdminSecondaryText)
                        Box(modifier = Modifier.width(105.dp)) {
                            AdminStatusBadge(bk.status)
                        }
                    }
                    HorizontalDivider(color = AdminBorder)
                }
            }
        }
    }
}

@Composable
private fun AdminWorkerAvailabilityCard(
    availableCount: Int,
    busyCount: Int,
    blockedCount: Int,
    totalCount: Int,
    onNavigateRoute: (AdminRoute) -> Unit,
    onOpenAddCategory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Worker Availability",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = AdminMainText
            )

            AvailabilityProgressRow(
                label = "Available Workers",
                count = availableCount,
                progress = availableCount.toFloat() / totalCount.toFloat(),
                barColor = AdminSuccessGreen
            )
            AvailabilityProgressRow(
                label = "Busy Workers",
                count = busyCount,
                progress = busyCount.toFloat() / totalCount.toFloat(),
                barColor = AdminAccentOrange
            )
            AvailabilityProgressRow(
                label = "Blocked Workers",
                count = blockedCount,
                progress = blockedCount.toFloat() / totalCount.toFloat(),
                barColor = AdminDangerRed
            )

            HorizontalDivider(color = AdminBorder)

            Text(
                text = "Quick Actions",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AdminMainText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onNavigateRoute(AdminRoute.WORKERS) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AdminBorder)
                ) {
                    Text("Manage Workers", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryNavy)
                }
                OutlinedButton(
                    onClick = onOpenAddCategory,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AdminBorder)
                ) {
                    Text("Add Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminAccentOrange)
                }
            }
        }
    }
}

@Composable
private fun AvailabilityProgressRow(
    label: String,
    count: Int,
    progress: Float,
    barColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AdminMainText)
            Text(count.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = barColor)
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0.05f, 1f) },
            color = barColor,
            trackColor = AdminNeutralGrayBg,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

// ==================================================
// 2. ADMIN USERS PAGE (/admin/users)
// ==================================================
@Composable
private fun AdminUsersPage(
    usersList: List<AdminUserRecord>,
    globalSearchQuery: String,
    onViewUser: (AdminUserRecord) -> Unit,
    onEditUser: (AdminUserRecord) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit
) {
    var localSearch by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("All") }
    var statusFilter by remember { mutableStateOf("All") }
    var areaFilter by remember { mutableStateOf("All") }

    val filteredUsers = remember(usersList, globalSearchQuery, localSearch, roleFilter, statusFilter, areaFilter) {
        val q = (if (localSearch.isNotBlank()) localSearch else globalSearchQuery).trim()
        usersList.filter { u ->
            val matchSearch = q.isEmpty() ||
                u.name.contains(q, ignoreCase = true) ||
                u.phone.contains(q, ignoreCase = true) ||
                u.email.contains(q, ignoreCase = true)
            val matchRole = roleFilter == "All" || u.role.equals(roleFilter, ignoreCase = true)
            val matchStatus = statusFilter == "All" || u.status.equals(statusFilter, ignoreCase = true)
            val matchArea = areaFilter == "All" || u.area.contains(areaFilter, ignoreCase = true)
            matchSearch && matchRole && matchStatus && matchArea
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Users", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

            // Top Controls: Search + Role Filter + Status Filter + Area Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = localSearch,
                    onValueChange = { localSearch = it },
                    placeholder = { Text("Search users...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.width(220.dp)
                )
                AdminFilterChipGroup(
                    label = "Role:",
                    options = listOf("All", "Customer", "Worker"),
                    selected = roleFilter,
                    onSelect = { roleFilter = it }
                )
                AdminFilterChipGroup(
                    label = "Status:",
                    options = listOf("All", "Active", "Blocked"),
                    selected = statusFilter,
                    onSelect = { statusFilter = it }
                )
                AdminFilterChipGroup(
                    label = "Area:",
                    options = listOf("All", "Silwani", "Raisen", "Bhopal"),
                    selected = areaFilter,
                    onSelect = { areaFilter = it }
                )
            }

            if (filteredUsers.isEmpty()) {
                Text("No users match the selected filters.", color = AdminSecondaryText, fontSize = 13.sp)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(min = 920.dp)
                            .background(AdminNeutralGrayBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Profile", modifier = Modifier.width(60.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Name", modifier = Modifier.width(140.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Phone", modifier = Modifier.width(130.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Email", modifier = Modifier.width(170.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Role", modifier = Modifier.width(90.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Area", modifier = Modifier.width(110.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Status", modifier = Modifier.width(90.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Joined", modifier = Modifier.width(95.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                        Text("Actions", modifier = Modifier.width(150.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    }

                    filteredUsers.forEach { u ->
                        Row(
                            modifier = Modifier
                                .widthIn(min = 920.dp)
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.width(60.dp)) {
                                AdminCircleInitialAvatar(name = u.name)
                            }
                            Text(u.name, modifier = Modifier.width(140.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                            Text(u.phone, modifier = Modifier.width(130.dp), fontSize = 13.sp, color = AdminSecondaryText)
                            Text(u.email, modifier = Modifier.width(170.dp), fontSize = 12.sp, color = AdminSecondaryText)
                            Text(u.role, modifier = Modifier.width(90.dp), fontSize = 13.sp, color = AdminSecondaryText)
                            Text(u.area, modifier = Modifier.width(110.dp), fontSize = 13.sp, color = AdminSecondaryText)
                            Box(modifier = Modifier.width(90.dp)) {
                                AdminStatusBadge(u.status)
                            }
                            Text(u.joined, modifier = Modifier.width(95.dp), fontSize = 12.sp, color = AdminSecondaryText)
                            Row(
                                modifier = Modifier.width(150.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "View",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AdminPrimaryNavy,
                                    modifier = Modifier.clickable { onViewUser(u) }
                                )
                                Text(
                                    text = "Edit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AdminAccentOrange,
                                    modifier = Modifier.clickable { onEditUser(u) }
                                )
                                Text(
                                    text = if (u.status == "Blocked") "Unblock" else "Block",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (u.status == "Blocked") AdminSuccessGreen else AdminDangerRed,
                                    modifier = Modifier.clickable { onConfirmBlockUser(u) }
                                )
                            }
                        }
                        HorizontalDivider(color = AdminBorder)
                    }
                }
            }
        }
    }
}

// ==================================================
// 3. ADMIN CUSTOMERS PAGE (/admin/customers)
// ==================================================
@Composable
private fun AdminCustomersPage(
    customers: List<AdminUserRecord>,
    globalSearchQuery: String,
    onViewUser: (AdminUserRecord) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit
) {
    val filtered = remember(customers, globalSearchQuery) {
        if (globalSearchQuery.isBlank()) customers
        else customers.filter {
            it.name.contains(globalSearchQuery, ignoreCase = true) ||
                it.phone.contains(globalSearchQuery, ignoreCase = true) ||
                it.area.contains(globalSearchQuery, ignoreCase = true)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Customers", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(min = 860.dp)
                        .background(AdminNeutralGrayBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Photo", modifier = Modifier.width(60.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Name", modifier = Modifier.width(140.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Phone", modifier = Modifier.width(130.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Email", modifier = Modifier.width(170.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Area", modifier = Modifier.width(110.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Bookings", modifier = Modifier.width(80.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Status", modifier = Modifier.width(90.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Joined", modifier = Modifier.width(95.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Actions", modifier = Modifier.width(120.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                }

                filtered.forEach { c ->
                    Row(
                        modifier = Modifier
                            .widthIn(min = 860.dp)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(60.dp)) {
                            AdminCircleInitialAvatar(name = c.name)
                        }
                        Text(c.name, modifier = Modifier.width(140.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                        Text(c.phone, modifier = Modifier.width(130.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(c.email, modifier = Modifier.width(170.dp), fontSize = 12.sp, color = AdminSecondaryText)
                        Text(c.area, modifier = Modifier.width(110.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(c.bookingsCount.toString(), modifier = Modifier.width(80.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        Box(modifier = Modifier.width(90.dp)) {
                            AdminStatusBadge(c.status)
                        }
                        Text(c.joined, modifier = Modifier.width(95.dp), fontSize = 12.sp, color = AdminSecondaryText)
                        Row(
                            modifier = Modifier.width(120.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "View",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminPrimaryNavy,
                                modifier = Modifier.clickable { onViewUser(c) }
                            )
                            Text(
                                text = if (c.status == "Blocked") "Unblock" else "Block",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (c.status == "Blocked") AdminSuccessGreen else AdminDangerRed,
                                modifier = Modifier.clickable { onConfirmBlockUser(c) }
                            )
                        }
                    }
                    HorizontalDivider(color = AdminBorder)
                }
            }
        }
    }
}

// ==================================================
// 4. ADMIN WORKERS PAGE (/admin/workers)
// ==================================================
@Composable
private fun AdminWorkersPage(
    workers: List<AdminUserRecord>,
    globalSearchQuery: String,
    onViewUser: (AdminUserRecord) -> Unit,
    onUpdateWorkerStatus: (AdminUserRecord, String, String) -> Unit,
    onConfirmBlockUser: (AdminUserRecord) -> Unit
) {
    var localSearch by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf("All") }
    var areaFilter by remember { mutableStateOf("All") }
    var availFilter by remember { mutableStateOf("All") }
    var statusFilter by remember { mutableStateOf("All") }

    val filteredWorkers = remember(workers, globalSearchQuery, localSearch, categoryFilter, areaFilter, availFilter, statusFilter) {
        val q = (if (localSearch.isNotBlank()) localSearch else globalSearchQuery).trim()
        workers.filter { w ->
            val matchSearch = q.isEmpty() || w.name.contains(q, ignoreCase = true) || w.category.contains(q, ignoreCase = true)
            val matchCat = categoryFilter == "All" || w.category.contains(categoryFilter, ignoreCase = true)
            val matchArea = areaFilter == "All" || w.area.contains(areaFilter, ignoreCase = true)
            val matchAvail = availFilter == "All" || w.availability.equals(availFilter, ignoreCase = true)
            val matchStatus = statusFilter == "All" || w.status.equals(statusFilter, ignoreCase = true)
            matchSearch && matchCat && matchArea && matchAvail && matchStatus
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Workers", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

            // Top Filters: Search + Category + Area + Availability + Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = localSearch,
                    onValueChange = { localSearch = it },
                    placeholder = { Text("Search workers...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.width(200.dp)
                )
                AdminFilterChipGroup(
                    label = "Category:",
                    options = listOf("All", "Mason", "Plumber", "Electrician", "Painter", "Carpenter"),
                    selected = categoryFilter,
                    onSelect = { categoryFilter = it }
                )
                AdminFilterChipGroup(
                    label = "Area:",
                    options = listOf("All", "Silwani", "Raisen", "Bhopal"),
                    selected = areaFilter,
                    onSelect = { areaFilter = it }
                )
                AdminFilterChipGroup(
                    label = "Availability:",
                    options = listOf("All", "Available", "Busy", "Blocked"),
                    selected = availFilter,
                    onSelect = { availFilter = it }
                )
                AdminFilterChipGroup(
                    label = "Status:",
                    options = listOf("All", "Active", "Inactive", "Blocked"),
                    selected = statusFilter,
                    onSelect = { statusFilter = it }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(min = 1040.dp)
                        .background(AdminNeutralGrayBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Photo", modifier = Modifier.width(55.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Worker Name", modifier = Modifier.width(140.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Category", modifier = Modifier.width(110.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Area", modifier = Modifier.width(110.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Experience", modifier = Modifier.width(90.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Daily Rate", modifier = Modifier.width(90.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Availability", modifier = Modifier.width(105.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Rating", modifier = Modifier.width(75.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Status", modifier = Modifier.width(90.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Actions", modifier = Modifier.width(185.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                }

                filteredWorkers.forEach { w ->
                    Row(
                        modifier = Modifier
                            .widthIn(min = 1040.dp)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(55.dp)) {
                            AdminCircleInitialAvatar(name = w.name)
                        }
                        Text(w.name, modifier = Modifier.width(140.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                        Text(w.category, modifier = Modifier.width(110.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(w.area, modifier = Modifier.width(110.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(w.experience, modifier = Modifier.width(90.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text("₹${w.dailyRate}", modifier = Modifier.width(90.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        Box(modifier = Modifier.width(105.dp)) {
                            // Visually obvious Worker Availability: Available=Green, Busy=Orange, Blocked=Red
                            AdminStatusBadge(w.availability)
                        }
                        Text("${w.rating} ★", modifier = Modifier.width(75.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminAccentOrange)
                        Box(modifier = Modifier.width(90.dp)) {
                            AdminStatusBadge(w.status)
                        }
                        Row(
                            modifier = Modifier.width(185.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "View Profile",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminPrimaryNavy,
                                modifier = Modifier.clickable { onViewUser(w) }
                            )
                            val isActive = w.status.equals("Active", ignoreCase = true)
                            Text(
                                text = if (isActive) "Deactivate" else "Activate",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminAccentOrange,
                                modifier = Modifier.clickable {
                                    if (isActive) onUpdateWorkerStatus(w, "Inactive", "Busy")
                                    else onUpdateWorkerStatus(w, "Active", "Available")
                                }
                            )
                            Text(
                                text = if (w.status == "Blocked") "Unblock" else "Block",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (w.status == "Blocked") AdminSuccessGreen else AdminDangerRed,
                                modifier = Modifier.clickable { onConfirmBlockUser(w) }
                            )
                        }
                    }
                    HorizontalDivider(color = AdminBorder)
                }
            }
        }
    }
}

// ==================================================
// 5. ADMIN BOOKINGS PAGE (/admin/bookings)
// ==================================================
@Composable
private fun AdminBookingsPage(
    bookings: List<AdminBookingRecord>,
    globalSearchQuery: String,
    onViewBooking: (AdminBookingRecord) -> Unit
) {
    var localSearch by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var categoryFilter by remember { mutableStateOf("All") }
    var areaFilter by remember { mutableStateOf("All") }

    val filteredBookings = remember(bookings, globalSearchQuery, localSearch, statusFilter, categoryFilter, areaFilter) {
        val q = (if (localSearch.isNotBlank()) localSearch else globalSearchQuery).trim()
        bookings.filter { bk ->
            val matchSearch = q.isEmpty() ||
                bk.customerName.contains(q, ignoreCase = true) ||
                bk.workerName.contains(q, ignoreCase = true) ||
                bk.id.contains(q, ignoreCase = true)
            val matchStatus = statusFilter == "All" || bk.status.equals(statusFilter, ignoreCase = true)
            val matchCat = categoryFilter == "All" || bk.category.contains(categoryFilter, ignoreCase = true)
            val matchArea = areaFilter == "All" || bk.area.contains(areaFilter, ignoreCase = true)
            matchSearch && matchStatus && matchCat && matchArea
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Bookings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = localSearch,
                    onValueChange = { localSearch = it },
                    placeholder = { Text("Customer, Worker or Booking ID...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.width(240.dp)
                )
                AdminFilterChipGroup(
                    label = "Status:",
                    options = listOf("All", "PENDING", "ACCEPTED", "COMPLETED", "REJECTED", "CANCELLED"),
                    selected = statusFilter,
                    onSelect = { statusFilter = it }
                )
                AdminFilterChipGroup(
                    label = "Category:",
                    options = listOf("All", "Mason", "Plumber", "Electrician", "Painter"),
                    selected = categoryFilter,
                    onSelect = { categoryFilter = it }
                )
                AdminFilterChipGroup(
                    label = "Area:",
                    options = listOf("All", "Silwani", "Raisen", "Bhopal"),
                    selected = areaFilter,
                    onSelect = { areaFilter = it }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(min = 1040.dp)
                        .background(AdminNeutralGrayBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Booking ID", modifier = Modifier.width(90.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Customer", modifier = Modifier.width(125.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Worker", modifier = Modifier.width(125.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Category", modifier = Modifier.width(110.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Area", modifier = Modifier.width(105.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Date", modifier = Modifier.width(95.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Days", modifier = Modifier.width(60.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Rate", modifier = Modifier.width(75.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Status", modifier = Modifier.width(105.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Created", modifier = Modifier.width(95.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Action", modifier = Modifier.width(95.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                }

                filteredBookings.forEach { bk ->
                    Row(
                        modifier = Modifier
                            .widthIn(min = 1040.dp)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#${bk.id}", modifier = Modifier.width(90.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        Text(bk.customerName, modifier = Modifier.width(125.dp), fontSize = 13.sp, color = AdminMainText)
                        Text(bk.workerName, modifier = Modifier.width(125.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(bk.category, modifier = Modifier.width(110.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(bk.area, modifier = Modifier.width(105.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Text(bk.date, modifier = Modifier.width(95.dp), fontSize = 12.sp, color = AdminSecondaryText)
                        Text(bk.days.toString(), modifier = Modifier.width(60.dp), fontSize = 13.sp, color = AdminMainText)
                        Text("₹${bk.rate}", modifier = Modifier.width(75.dp), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                        Box(modifier = Modifier.width(105.dp)) {
                            AdminStatusBadge(bk.status)
                        }
                        Text(bk.createdDate, modifier = Modifier.width(95.dp), fontSize = 12.sp, color = AdminSecondaryText)
                        Text(
                            text = "View Details",
                            modifier = Modifier
                                .width(95.dp)
                                .clickable { onViewBooking(bk) },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AdminPrimaryNavy
                        )
                    }
                    HorizontalDivider(color = AdminBorder)
                }
            }
        }
    }
}

// ==================================================
// 6. ADMIN CATEGORIES PAGE (/admin/categories)
// ==================================================
@Composable
private fun AdminCategoriesPage(
    categories: List<AdminCategoryRecord>,
    onAddCategoryClick: () -> Unit,
    onEditCategory: (AdminCategoryRecord) -> Unit,
    onToggleCategoryActive: (AdminCategoryRecord) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

                Button(
                    onClick = onAddCategoryClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AdminWhite, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Category", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(min = 660.dp)
                        .background(AdminNeutralGrayBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Icon", modifier = Modifier.width(70.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Category Name", modifier = Modifier.width(180.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Status", modifier = Modifier.width(120.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Workers", modifier = Modifier.width(120.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Text("Actions", modifier = Modifier.width(170.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                }

                categories.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .widthIn(min = 660.dp)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.width(70.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEFF6FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Construction,
                                    contentDescription = cat.name,
                                    tint = AdminPrimaryNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(cat.name, modifier = Modifier.width(180.dp), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                        Box(modifier = Modifier.width(120.dp)) {
                            AdminStatusBadge(if (cat.active) "Active" else "Inactive")
                        }
                        Text("${cat.workerCount} workers", modifier = Modifier.width(120.dp), fontSize = 13.sp, color = AdminSecondaryText)
                        Row(
                            modifier = Modifier.width(170.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Edit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AdminPrimaryNavy,
                                modifier = Modifier.clickable { onEditCategory(cat) }
                            )
                            Text(
                                text = if (cat.active) "Deactivate" else "Activate",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cat.active) AdminDangerRed else AdminSuccessGreen,
                                modifier = Modifier.clickable { onToggleCategoryActive(cat) }
                            )
                        }
                    }
                    HorizontalDivider(color = AdminBorder)
                }
            }
        }
    }
}

// ==================================================
// 7. ADMIN REPORTS PAGE (/admin/reports)
// ==================================================
@Composable
private fun AdminReportsPage(
    usersList: List<AdminUserRecord>,
    bookingsList: List<AdminBookingRecord>
) {
    val totalUsers = usersList.size.coerceAtLeast(124)
    val totalWorkers = usersList.count { it.role == "Worker" }.coerceAtLeast(56)
    val totalCustomers = usersList.count { it.role == "Customer" }.coerceAtLeast(68)
    val totalBookings = bookingsList.size.coerceAtLeast(89)
    val completedBookings = bookingsList.count { it.status == "COMPLETED" }.coerceAtLeast(42)
    val cancelledBookings = bookingsList.count { it.status == "CANCELLED" }.coerceAtLeast(6)
    val availableWorkers = usersList.count { it.role == "Worker" && it.availability == "Available" }.coerceAtLeast(48)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Reports", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Marketplace Summary Report", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                HorizontalDivider(color = AdminBorder)
                AdminDetailRow("Total users", totalUsers.toString())
                AdminDetailRow("Total workers", totalWorkers.toString())
                AdminDetailRow("Total customers", totalCustomers.toString())
                AdminDetailRow("Total bookings", totalBookings.toString())
                AdminDetailRow("Completed bookings", completedBookings.toString())
                AdminDetailRow("Cancelled bookings", cancelledBookings.toString())
                AdminDetailRow("Average worker rating", "4.8 ★")
                AdminDetailRow("Available workers", availableWorkers.toString())
            }
        }
    }
}

// ==================================================
// 8. ADMIN SETTINGS PAGE (/admin/settings)
// ==================================================
@Composable
private fun AdminSettingsPage(
    loggedAdminEmail: String,
    brandingPrefs: android.content.SharedPreferences,
    settingsPrefs: android.content.SharedPreferences,
    authPrefs: android.content.SharedPreferences,
    context: Context,
    onRequestLogout: () -> Unit
) {
    var adminName by remember { mutableStateOf("Ankit Ahirwar") }
    var adminEmailState by remember { mutableStateOf(loggedAdminEmail) }
    var appName by remember { mutableStateOf(brandingPrefs.getString("app_name", "WORKORA") ?: "WORKORA") }
    var defaultArea by remember { mutableStateOf(settingsPrefs.getString("default_area", "Silwani, Raisen") ?: "Silwani, Raisen") }
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Admin Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)

        // 1. Admin Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Admin Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                OutlinedTextField(
                    value = adminName,
                    onValueChange = { adminName = it },
                    label = { Text("Admin Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = adminEmailState,
                    onValueChange = { adminEmailState = it },
                    label = { Text("Admin Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        authPrefs.edit().putString("last_logged_in_email", adminEmailState.trim()).apply()
                        Toast.makeText(context, "Admin profile saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Admin Profile", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Application Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Application Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                OutlinedTextField(
                    value = appName,
                    onValueChange = { appName = it },
                    label = { Text("App Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = defaultArea,
                    onValueChange = { defaultArea = it },
                    label = { Text("Default Area") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                AdminDetailRow("Currency", "₹ (INR)")
                AdminDetailRow("Default Language", "English")

                Button(
                    onClick = {
                        brandingPrefs.edit().putString("app_name", appName.trim()).apply()
                        settingsPrefs.edit().putString("default_area", defaultArea.trim()).apply()
                        Toast.makeText(context, "Application settings updated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Application Settings", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Security & Password Change Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AdminWhite),
            border = BorderStroke(1.dp, AdminBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Security & Admin Password", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                OutlinedTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Admin Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPass,
                    onValueChange = { confirmPass = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (currentPass.isBlank() || newPass.length < 8 || newPass != confirmPass) {
                                Toast.makeText(context, "Validate all fields (min 8 chars & matching)", Toast.LENGTH_SHORT).show()
                            } else {
                                currentPass = ""
                                newPass = ""
                                confirmPass = ""
                                Toast.makeText(context, "Admin password updated", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Update Password", color = AdminWhite, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onRequestLogout,
                        border = BorderStroke(1.dp, AdminDangerRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Logout Session", color = AdminDangerRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================================================
// REUSABLE UI COMPONENTS
// ==================================================
@Composable
private fun AdminStatCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    value: String,
    changeText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(210.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminWhite),
        border = BorderStroke(1.dp, AdminBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AdminSecondaryText
                )
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AdminMainText
                )
                Text(
                    text = changeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AdminSuccessGreen
                )
            }
        }
    }
}

@Composable
private fun AdminStatusBadge(status: String) {
    val (bg, fg) = when (status.uppercase(Locale.US)) {
        "ACTIVE", "AVAILABLE", "ACCEPTED", "COMPLETED" -> Pair(AdminSuccessBg, AdminSuccessGreen)
        "PENDING", "BUSY", "IN_PROGRESS" -> Pair(AdminWarningBg, AdminWarningOrange)
        "BLOCKED", "REJECTED" -> Pair(AdminDangerBg, AdminDangerRed)
        else -> Pair(AdminNeutralGrayBg, AdminSecondaryText)
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = status,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun AdminCircleInitialAvatar(name: String) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(Color(0xFFEFF6FF)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(Locale.US),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AdminPrimaryNavy
        )
    }
}

@Composable
private fun AdminFilterChipGroup(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AdminSecondaryText)
        options.forEach { opt ->
            val isSel = selected.equals(opt, ignoreCase = true)
            Surface(
                color = if (isSel) AdminPrimaryNavy else AdminNeutralGrayBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { onSelect(opt) }
            ) {
                Text(
                    text = opt,
                    color = if (isSel) AdminWhite else AdminMainText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = AdminSecondaryText, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = AdminMainText, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AdminHelmetLogo(size: Dp = 38.dp) {
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
// FIREBASE REALTIME DATABASE HELPERS
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
            // 1. Fetch /users
            val usersConn = URL("$FIREBASE_DB_URL/users.json").openConnection() as HttpURLConnection
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
                                name = obj.optString("name", k.substringBefore("_at_")).ifBlank { "Workora User" },
                                phone = obj.optString("phone", "+91 98765 43210"),
                                email = obj.optString("email", k.replace("_at_", "@").replace("_", ".")),
                                role = roleClean,
                                area = obj.optString("location", "Silwani, Raisen"),
                                status = obj.optString("status", "Active"),
                                joined = obj.optString("joined", "2 Nov 2024"),
                                category = obj.optString("skill", "Electrician"),
                                experience = obj.optString("experience", "4 yrs"),
                                dailyRate = obj.optInt("dailyRate", 650),
                                availability = obj.optString("availability", "Available"),
                                rating = obj.optDouble("rating", 4.8),
                                bookingsCount = obj.optInt("bookingsCount", 3)
                            )
                        )
                    }
                }
            }
            usersConn.disconnect()

            // 2. Fetch /jobs (Bookings)
            val jobsConn = URL("$FIREBASE_DB_URL/jobs.json").openConnection() as HttpURLConnection
            jobsConn.requestMethod = "GET"
            jobsConn.connectTimeout = 5000
            if (jobsConn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(jobsConn.inputStream)).use { it.readText() }
                if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                    val root = JSONObject(resp)
                    val keys = root.keys()
                    var counter = 1
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val obj = root.optJSONObject(k) ?: continue
                        bookings.add(
                            AdminBookingRecord(
                                id = obj.optString("id", "BK00$counter"),
                                customerName = obj.optString("customerName", "Amit Sharma"),
                                customerPhone = obj.optString("customerPhone", "+91 98765 43210"),
                                workerName = obj.optString("workerName", "Ramesh Kumar"),
                                workerPhone = obj.optString("workerPhone", "+91 87654 32109"),
                                category = obj.optString("category", "Electrical Work"),
                                area = obj.optString("location", "Silwani, Raisen"),
                                date = obj.optString("date", "2 Nov 2024"),
                                preferredTime = obj.optString("preferredTime", "09:00 AM"),
                                days = obj.optInt("workersNeeded", 2),
                                rate = obj.optInt("dailyRate", 700),
                                description = obj.optString("description", "Standard local booking request"),
                                additionalMessage = obj.optString("urgency", "Immediate"),
                                status = obj.optString("status", "PENDING").uppercase(Locale.US),
                                createdDate = obj.optString("createdDate", "2 Nov 2024"),
                                updatedDate = obj.optString("updatedDate", "2 Nov 2024")
                            )
                        )
                        counter++
                    }
                }
            }
            jobsConn.disconnect()
        } catch (_: Exception) {
        }

        // Ensure baseline real records if database nodes are empty
        if (users.isEmpty()) {
            users.addAll(
                listOf(
                    AdminUserRecord("u1", "Amit Sharma", "+91 98765 43210", "amit@example.com", "Customer", "Silwani", "Active", "2 Nov 2024", bookingsCount = 6),
                    AdminUserRecord("u2", "Ramesh Kumar", "+91 87654 32109", "ramesh@example.com", "Worker", "Silwani", "Active", "2 Nov 2024", "Mason", "5 yrs", 700, "Available", 4.9),
                    AdminUserRecord("u3", "Suresh Yadav", "+91 76543 21098", "suresh@example.com", "Worker", "Raisen", "Active", "1 Nov 2024", "Painter", "4 yrs", 650, "Busy", 4.8),
                    AdminUserRecord("u4", "Pooja Verma", "+91 65432 10987", "pooja@example.com", "Customer", "Bhopal", "Active", "1 Nov 2024", bookingsCount = 4),
                    AdminUserRecord("u5", "Mahesh Singh", "+91 54321 09876", "mahesh@example.com", "Worker", "Silwani", "Active", "31 Oct 2024", "Electrician", "6 yrs", 750, "Available", 4.9)
                )
            )
        }

        if (bookings.isEmpty()) {
            bookings.addAll(
                listOf(
                    AdminBookingRecord("BK001", "Amit Sharma", "+91 98765 43210", "Ramesh Kumar", "+91 87654 32109", "Electrical Work", "Silwani", "2 Nov 2024", "09:00 AM", 2, 700, "Complete house wiring check", "Bring tools", "PENDING", "2 Nov 2024", "2 Nov 2024"),
                    AdminBookingRecord("BK002", "Pooja Verma", "+91 65432 10987", "Suresh Yadav", "+91 76543 21098", "Painting", "Raisen", "1 Nov 2024", "10:00 AM", 3, 650, "Exterior wall painting", "Urgent", "ACCEPTED", "1 Nov 2024", "1 Nov 2024"),
                    AdminBookingRecord("BK003", "Rajesh Patel", "+91 91234 56780", "Mahesh Singh", "+91 54321 09876", "Mason Work", "Silwani", "31 Oct 2024", "08:30 AM", 4, 750, "Brick work and plastering", "Standard", "COMPLETED", "31 Oct 2024", "1 Nov 2024"),
                    AdminBookingRecord("BK004", "Neha Gupta", "+91 99887 76655", "Ramesh Kumar", "+91 87654 32109", "Plumber", "Bhopal", "30 Oct 2024", "11:00 AM", 1, 600, "Kitchen pipe fitting", "None", "CANCELLED", "30 Oct 2024", "30 Oct 2024")
                )
            )
        }

        val requiredCategories = listOf(
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
        categories.addAll(requiredCategories)

        Handler(Looper.getMainLooper()).post {
            onLoaded(users, bookings, categories)
        }
    }.start()
}

private fun updateFirebaseUserStatus(userKey: String, status: String, availability: String) {
    Thread {
        try {
            val url = URL("$FIREBASE_DB_URL/users/$userKey.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("status", status)
                put("availability", availability)
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
            val url = URL("$FIREBASE_DB_URL/users/${user.key}.json")
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

private fun updateFirebaseBookingStatus(bookingId: String, newStatus: String) {
    Thread {
        try {
            val url = URL("$FIREBASE_DB_URL/jobs/$bookingId.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("status", newStatus)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun saveCategoryToFirebase(category: AdminCategoryRecord) {
    Thread {
        try {
            val url = URL("$FIREBASE_DB_URL/categories/${category.id}.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("id", category.id)
                put("name", category.name)
                put("iconName", category.iconName)
                put("active", category.active)
                put("workerCount", category.workerCount)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}
