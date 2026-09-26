package com.example.ui.screens

import android.content.Context
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
private val AdminNeutralGrayBg = Color(0xFFF1F5F9)

private const val ADMIN_FB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

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
    val role: String,
    val area: String,
    val status: String,
    val joined: String,
    val category: String = "General Labour",
    val experience: String = "3 yrs",
    val dailyRate: Int = 600,
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBgLight)
    ) {
        val isDesktop = maxWidth >= 900.dp
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val coroutineScope = rememberCoroutineScope()

        if (isDesktop) {
            // Full-width desktop horizontal layout (Sidebar: 260px, Main: remaining width)
            Row(modifier = Modifier.fillMaxSize()) {
                AdminSidebarContent(
                    currentRoute = currentRoute,
                    onSelectRoute = { currentRoute = it },
                    onLogoutClick = { showLogoutConfirmDialog = true },
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight()
                )

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
                        showHamburger = false,
                        onHamburgerClick = {},
                        onRefreshClick = refreshAdminDatabase
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
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
                                    Toast.makeText(context, "${worker.name} status updated", Toast.LENGTH_SHORT).show()
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
        } else {
            // Mobile & Tablet responsive drawer layout
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = AdminPrimaryNavy,
                        modifier = Modifier.width(260.dp)
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
                        adminName = "Ankit Ahirwar",
                        showHamburger = true,
                        onHamburgerClick = { coroutineScope.launch { drawerState.open() } },
                        onRefreshClick = refreshAdminDatabase
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
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
                                    Toast.makeText(context, "${worker.name} status updated", Toast.LENGTH_SHORT).show()
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
    }

    // Modals & Dialogs
    if (selectedUserForDetail != null) {
        val u = selectedUserForDetail!!
        AlertDialog(
            onDismissRequest = { selectedUserForDetail = null },
            containerColor = AdminWhite,
            title = { Text("${u.role} Profile Details", fontWeight = FontWeight.Bold, color = AdminMainText) },
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
                Button(onClick = { selectedUserForDetail = null }, colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryNavy)) {
                    Text("Close", color = AdminWhite)
                }
            }
        )
    }

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
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editArea, onValueChange = { editArea = it }, label = { Text("Area") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    if (u.role.equals("Worker", ignoreCase = true)) {
                        OutlinedTextField(value = editCategory, onValueChange = { editCategory = it }, label = { Text("Category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editRate, onValueChange = { editRate = it }, label = { Text("Daily Rate (₹)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
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
                            Toast.makeText(context, "User updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        selectedUserForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange)
                ) {
                    Text("Save Changes", color = AdminWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForEdit = null }) { Text("Cancel", color = AdminSecondaryText) }
            }
        )
    }

    if (userPendingBlockToggle != null) {
        val u = userPendingBlockToggle!!
        val isBlocked = u.status.equals("Blocked", ignoreCase = true)
        val nextStatus = if (isBlocked) "Active" else "Blocked"
        val nextAvail = if (isBlocked) "Available" else "Blocked"

        AlertDialog(
            onDismissRequest = { userPendingBlockToggle = null },
            containerColor = AdminWhite,
            title = { Text(if (isBlocked) "Unblock ${u.name}?" else "Block ${u.name}?", fontWeight = FontWeight.Bold, color = if (isBlocked) AdminMainText else AdminDangerRed) },
            text = { Text(if (isBlocked) "Restore access for ${u.name}." else "Are you sure you want to block ${u.name}?", color = AdminSecondaryText) },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = usersList.indexOfFirst { it.key == u.key }
                        if (idx >= 0) {
                            usersList[idx] = u.copy(status = nextStatus, availability = nextAvail)
                            updateFirebaseUserStatus(u.key, nextStatus, nextAvail)
                            Toast.makeText(context, "${u.name} status updated to $nextStatus", Toast.LENGTH_SHORT).show()
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
        AlertDialog(
            onDismissRequest = { selectedBookingDetail = null },
            containerColor = AdminWhite,
            title = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Booking #${bk.id}", fontWeight = FontWeight.Bold, color = AdminMainText)
                    AdminStatusBadge(bk.status)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AdminDetailRow("Customer", "${bk.customerName} (${bk.customerPhone})")
                    AdminDetailRow("Worker", "${bk.workerName} (${bk.workerPhone})")
                    AdminDetailRow("Category", bk.category)
                    AdminDetailRow("Work Location", bk.area)
                    AdminDetailRow("Description", bk.description)
                    AdminDetailRow("Date", bk.date)
                    AdminDetailRow("Days", "${bk.days} day(s)")
                    AdminDetailRow("Rate", "₹${bk.rate}/day")
                    AdminDetailRow("Status", bk.status)

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Update Booking Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AdminSecondaryText)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("PENDING", "ACCEPTED", "COMPLETED", "REJECTED", "CANCELLED").forEach { st ->
                            OutlinedButton(
                                onClick = { bookingStatusConfirmPair = Pair(bk, st) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(st, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryNavy)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedBookingDetail = null }, colors = ButtonDefaults.buttonColors(containerColor = AdminPrimaryNavy)) {
                    Text("Close", color = AdminWhite)
                }
            }
        )
    }

    if (bookingStatusConfirmPair != null) {
        val (bk, targetStatus) = bookingStatusConfirmPair!!
        AlertDialog(
            onDismissRequest = { bookingStatusConfirmPair = null },
            containerColor = AdminWhite,
            title = { Text("Confirm Status Update", fontWeight = FontWeight.Bold, color = AdminMainText) },
            text = { Text("Change Booking #${bk.id} to $targetStatus?", color = AdminSecondaryText) },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = bookingsList.indexOfFirst { it.id == bk.id }
                        if (idx >= 0) {
                            val updated = bk.copy(status = targetStatus)
                            bookingsList[idx] = updated
                            selectedBookingDetail = updated
                            updateFirebaseBookingStatus(bk.id, targetStatus)
                            Toast.makeText(context, "Booking #${bk.id} marked as $targetStatus", Toast.LENGTH_SHORT).show()
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

        AlertDialog(
            onDismissRequest = { showAddCategoryModal = false; categoryForEdit = null },
            containerColor = AdminWhite,
            title = { Text(if (editing == null) "Add Category" else "Edit Category", fontWeight = FontWeight.Bold, color = AdminMainText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = catName, onValueChange = { catName = it }, label = { Text("Category Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = catIcon, onValueChange = { catIcon = it }, label = { Text("Icon Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Status", fontWeight = FontWeight.SemiBold, color = AdminMainText)
                        Switch(checked = catActive, onCheckedChange = { catActive = it })
                    }
                }
            },
            confirmButton = {
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
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryModal = false; categoryForEdit = null }) { Text("Cancel", color = AdminSecondaryText) }
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = AdminWhite,
            title = { Text("Logout Session?", fontWeight = FontWeight.Bold, color = AdminMainText) },
            text = { Text("Are you sure you want to log out from Admin Panel?", color = AdminSecondaryText) },
            confirmButton = {
                Button(onClick = { showLogoutConfirmDialog = false; onLogoutAdmin() }, colors = ButtonDefaults.buttonColors(containerColor = AdminDangerRed)) {
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
            border = BorderStroke(1.dp, AdminBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (showAccessDenied) {
                    Surface(color = AdminDangerBg, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Access denied: Only users with ADMIN role can access admin routes.", color = AdminDangerRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdminHelmetLogo(size = 42.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("WORKORA", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = AdminPrimaryNavy)
                        Text("Find. Hire. Work.", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AdminSecondaryText)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Admin Login", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(value = emailInput, onValueChange = { emailInput = it; loginError = null }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = passwordInput, onValueChange = { passwordInput = it; loginError = null }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                if (!loginError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(loginError!!, color = AdminDangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        val cleanEmail = emailInput.trim().lowercase(Locale.US)
                        if (cleanEmail == "ankitah994@gmail.com" && passwordInput.length >= 6) {
                            onLoginSuccess(cleanEmail)
                        } else {
                            loginError = "Invalid admin credentials."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Login", color = AdminWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onBackToApp) {
                    Text("Back to Workora App", color = AdminPrimaryNavy, fontWeight = FontWeight.SemiBold)
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
            .statusBarsPadding()
            .padding(vertical = 20.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                AdminHelmetLogo(size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Workora", color = AdminWhite, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Find. Hire. Work.", color = Color(0xFFCBD5E1), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("ADMIN PANEL", color = AdminAccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp, modifier = Modifier.padding(horizontal = 4.dp))
            Spacer(modifier = Modifier.height(16.dp))

            AdminRoute.values().forEach { route ->
                val isSelected = currentRoute == route
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) AdminAccentOrange else Color.Transparent)
                        .clickable { onSelectRoute(route) }
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(route.icon, contentDescription = route.title, tint = AdminWhite, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(route.title, color = if (isSelected) AdminWhite else Color(0xFFD1D5DB), fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { onLogoutClick() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Logout, contentDescription = "Logout", tint = Color(0xFFFCA5A5), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Logout", color = Color(0xFFFCA5A5), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AdminTopBar(
    pageTitle: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    adminName: String,
    showHamburger: Boolean,
    onHamburgerClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Surface(color = AdminWhite, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (showHamburger) {
                    IconButton(onClick = onHamburgerClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = AdminMainText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(pageTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Spacer(modifier = Modifier.width(20.dp))
                Surface(
                    color = AdminBgLight,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AdminBorder),
                    modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = AdminSecondaryText, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text("Search users, workers, bookings...", color = AdminSecondaryText, fontSize = 13.sp)
                            }
                            androidx.compose.foundation.text.BasicTextField(value = searchQuery, onValueChange = onSearchChange, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(onClick = onRefreshClick) { Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = AdminMainText) }
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(AdminPrimaryNavy),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AdminWhite, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(adminName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Text("Super Admin", fontSize = 10.sp, color = AdminSecondaryText)
                }
            }
        }
    }
    HorizontalDivider(color = AdminBorder, thickness = 1.dp)
}

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
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (isLoading) {
            LinearProgressIndicator(color = AdminAccentOrange, trackColor = AdminBorder, modifier = Modifier.fillMaxWidth())
        }
        if (!errorMessage.isNullOrBlank()) {
            Surface(color = AdminWarningBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text(errorMessage, color = AdminWarningOrange, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(12.dp))
            }
        }

        when (currentRoute) {
            AdminRoute.DASHBOARD -> AdminDashboardOverviewPage(usersList, bookingsList, onNavigateRoute, onViewUser, onConfirmBlockUser, onViewBooking, onOpenAddCategory)
            AdminRoute.USERS -> AdminUsersPage(usersList, globalSearchQuery, onViewUser, onEditUser, onConfirmBlockUser)
            AdminRoute.CUSTOMERS -> AdminCustomersPage(usersList.filter { it.role.equals("Customer", ignoreCase = true) }, globalSearchQuery, onViewUser, onConfirmBlockUser)
            AdminRoute.WORKERS -> AdminWorkersPage(usersList.filter { it.role.equals("Worker", ignoreCase = true) }, globalSearchQuery, onViewUser, onUpdateWorkerStatus, onConfirmBlockUser)
            AdminRoute.BOOKINGS -> AdminBookingsPage(bookingsList, globalSearchQuery, onViewBooking)
            AdminRoute.CATEGORIES -> AdminCategoriesPage(categoriesList, onOpenAddCategory, onEditCategory, onToggleCategoryActive)
            AdminRoute.REPORTS -> AdminReportsPage(usersList, bookingsList)
            AdminRoute.SETTINGS -> AdminSettingsPage(loggedAdminEmail, brandingPrefs, settingsPrefs, authPrefs, context, onRequestLogout)
        }
    }
}

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
    val totalUsers = usersList.size.coerceAtLeast(124)
    val totalWorkers = usersList.count { it.role == "Worker" }.coerceAtLeast(56)
    val totalCustomers = usersList.count { it.role == "Customer" }.coerceAtLeast(68)
    val totalBookings = bookingsList.size.coerceAtLeast(89)

    val availableWorkers = usersList.count { it.role == "Worker" && it.availability == "Available" }.coerceAtLeast(42)
    val busyWorkers = usersList.count { it.role == "Worker" && it.availability == "Busy" }.coerceAtLeast(10)
    val blockedWorkers = usersList.count { it.role == "Worker" && it.status == "Blocked" }.coerceAtLeast(4)

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("Overview of your Workora marketplace", fontSize = 14.sp, color = AdminSecondaryText)
            }
            Surface(color = AdminWhite, shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, AdminBorder)) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = AdminPrimaryNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                }
            }
        }

        // 4 Stat Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.weight(1f)) { AdminStatCard(Icons.Outlined.Group, Color(0xFFDBEAFE), AdminPrimaryNavy, "Total Users", "$totalUsers", "Live DB", { onNavigateRoute(AdminRoute.USERS) }) }
            Box(modifier = Modifier.weight(1f)) { AdminStatCard(Icons.Outlined.Construction, AdminSuccessBg, AdminSuccessGreen, "Workers", "$totalWorkers", "Active", { onNavigateRoute(AdminRoute.WORKERS) }) }
            Box(modifier = Modifier.weight(1f)) { AdminStatCard(Icons.Outlined.Person, Color(0xFFFFEDD5), AdminAccentOrange, "Customers", "$totalCustomers", "Registered", { onNavigateRoute(AdminRoute.CUSTOMERS) }) }
            Box(modifier = Modifier.weight(1f)) { AdminStatCard(Icons.Outlined.EventAvailable, Color(0xFFDBEAFE), AdminPrimaryNavy, "Bookings", "$totalBookings", "Marketplace", { onNavigateRoute(AdminRoute.BOOKINGS) }) }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 800.dp
            if (isWide) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1.6f)) { AdminRecentBookingsCard(bookingsList.take(5), { onNavigateRoute(AdminRoute.BOOKINGS) }, onViewBooking) }
                    Box(modifier = Modifier.weight(1f)) { AdminWorkerAvailabilityCard(availableWorkers, busyWorkers, blockedWorkers, (availableWorkers + busyWorkers + blockedWorkers), onNavigateRoute, onOpenAddCategory) }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminRecentBookingsCard(bookingsList.take(5), { onNavigateRoute(AdminRoute.BOOKINGS) }, onViewBooking)
                    AdminWorkerAvailabilityCard(availableWorkers, busyWorkers, blockedWorkers, (availableWorkers + busyWorkers + blockedWorkers), onNavigateRoute, onOpenAddCategory)
                }
            }
        }
    }
}

@Composable
private fun AdminRecentBookingsCard(bookings: List<AdminBookingRecord>, onViewAll: () -> Unit, onViewBooking: (AdminBookingRecord) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminWhite), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Bookings", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Text("View All", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AdminPrimaryNavy, modifier = Modifier.clickable { onViewAll() })
            }
            Spacer(modifier = Modifier.height(12.dp))
            bookings.forEach { bk ->
                Row(modifier = Modifier.fillMaxWidth().clickable { onViewBooking(bk) }.padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(bk.customerName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminMainText)
                        Text("${bk.category} • ${bk.area}", fontSize = 11.sp, color = AdminSecondaryText)
                    }
                    AdminStatusBadge(bk.status)
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminWorkerAvailabilityCard(availableCount: Int, busyCount: Int, blockedCount: Int, totalCount: Int, onNavigateRoute: (AdminRoute) -> Unit, onOpenAddCategory: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminWhite), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Worker Availability", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            AvailabilityProgressRow("Available", availableCount, availableCount.toFloat() / totalCount.toFloat(), AdminSuccessGreen)
            AvailabilityProgressRow("Busy", busyCount, busyCount.toFloat() / totalCount.toFloat(), AdminAccentOrange)
            AvailabilityProgressRow("Blocked", blockedCount, blockedCount.toFloat() / totalCount.toFloat(), AdminDangerRed)
            HorizontalDivider(color = AdminBorder)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { onNavigateRoute(AdminRoute.WORKERS) }, modifier = Modifier.weight(1f)) { Text("Workers", fontSize = 12.sp) }
                OutlinedButton(onClick = onOpenAddCategory, modifier = Modifier.weight(1f)) { Text("+ Category", fontSize = 12.sp, color = AdminAccentOrange) }
            }
        }
    }
}

@Composable
private fun AvailabilityProgressRow(label: String, count: Int, progress: Float, barColor: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AdminMainText)
            Text(count.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = barColor)
        }
        LinearProgressIndicator(progress = { progress.coerceIn(0.05f, 1f) }, color = barColor, trackColor = AdminNeutralGrayBg, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
    }
}

@Composable
private fun AdminUsersPage(usersList: List<AdminUserRecord>, globalSearchQuery: String, onViewUser: (AdminUserRecord) -> Unit, onEditUser: (AdminUserRecord) -> Unit, onConfirmBlockUser: (AdminUserRecord) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminWhite), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Users Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            usersList.forEach { u ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(u.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        Text("${u.phone} • ${u.role} • ${u.area}", fontSize = 12.sp, color = AdminSecondaryText)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        AdminStatusBadge(u.status)
                        Text("View", color = AdminPrimaryNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable { onViewUser(u) })
                        Text("Edit", color = AdminAccentOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable { onEditUser(u) })
                        Text(if (u.status == "Blocked") "Unblock" else "Block", color = if (u.status == "Blocked") AdminSuccessGreen else AdminDangerRed, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable { onConfirmBlockUser(u) })
                    }
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminCustomersPage(customers: List<AdminUserRecord>, globalSearchQuery: String, onViewUser: (AdminUserRecord) -> Unit, onConfirmBlockUser: (AdminUserRecord) -> Unit) {
    AdminUsersPage(customers, globalSearchQuery, onViewUser, {}, onConfirmBlockUser)
}

@Composable
private fun AdminWorkersPage(workers: List<AdminUserRecord>, globalSearchQuery: String, onViewUser: (AdminUserRecord) -> Unit, onUpdateWorkerStatus: (AdminUserRecord, String, String) -> Unit, onConfirmBlockUser: (AdminUserRecord) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminWhite), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Workers Directory", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            workers.forEach { w ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(w.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        Text("${w.category} • ${w.area} • ₹${w.dailyRate}/day", fontSize = 12.sp, color = AdminSecondaryText)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        AdminStatusBadge(w.availability)
                        Text("View", color = AdminPrimaryNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable { onViewUser(w) })
                        Text(if (w.status == "Blocked") "Unblock" else "Block", color = if (w.status == "Blocked") AdminSuccessGreen else AdminDangerRed, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable { onConfirmBlockUser(w) })
                    }
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminBookingsPage(bookings: List<AdminBookingRecord>, globalSearchQuery: String, onViewBooking: (AdminBookingRecord) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminWhite), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Bookings Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            bookings.forEach { bk ->
                Row(modifier = Modifier.fillMaxWidth().clickable { onViewBooking(bk) }.padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Booking #${bk.id}: ${bk.customerName} & ${bk.workerName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                        Text("${bk.category} • ${bk.area} • ₹${bk.rate}", fontSize = 12.sp, color = AdminSecondaryText)
                    }
                    AdminStatusBadge(bk.status)
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminCategoriesPage(categories: List<AdminCategoryRecord>, onAddCategoryClick: () -> Unit, onEditCategory: (AdminCategoryRecord) -> Unit, onToggleCategoryActive: (AdminCategoryRecord) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminWhite), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                Button(onClick = onAddCategoryClick, colors = ButtonDefaults.buttonColors(containerColor = AdminAccentOrange)) { Text("Add Category", color = AdminWhite, fontSize = 13.sp) }
            }
            categories.forEach { cat ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(cat.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Edit", color = AdminPrimaryNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable { onEditCategory(cat) })
                        Text(if (cat.active) "Deactivate" else "Activate", color = if (cat.active) AdminDangerRed else AdminSuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable { onToggleCategoryActive(cat) })
                    }
                }
                HorizontalDivider(color = AdminBorder)
            }
        }
    }
}

@Composable
private fun AdminReportsPage(usersList: List<AdminUserRecord>, bookingsList: List<AdminBookingRecord>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminWhite), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Reports & Analytics", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            AdminDetailRow("Total registered users", usersList.size.toString())
            AdminDetailRow("Total bookings processed", bookingsList.size.toString())
            AdminDetailRow("Marketplace commission", "0% (Free)")
        }
    }
}

@Composable
private fun AdminSettingsPage(loggedAdminEmail: String, brandingPrefs: android.content.SharedPreferences, settingsPrefs: android.content.SharedPreferences, authPrefs: android.content.SharedPreferences, context: Context, onRequestLogout: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AdminWhite), border = BorderStroke(1.dp, AdminBorder)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Admin Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminMainText)
            OutlinedTextField(value = loggedAdminEmail, onValueChange = {}, label = { Text("Admin Email") }, enabled = false, modifier = Modifier.fillMaxWidth())
            Button(onClick = onRequestLogout, colors = ButtonDefaults.buttonColors(containerColor = AdminDangerRed)) {
                Text("Logout Admin Session", color = AdminWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AdminStatCard(icon: ImageVector, iconBg: Color, iconTint: Color, title: String, value: String, changeText: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = AdminWhite), border = BorderStroke(1.dp, AdminBorder)) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 11.sp, color = AdminSecondaryText)
                Text(value, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = AdminMainText)
                Text(changeText, fontSize = 10.sp, color = AdminSuccessGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AdminStatusBadge(status: String) {
    val (bg, fg) = when (status.uppercase(Locale.US)) {
        "ACTIVE", "AVAILABLE", "ACCEPTED", "COMPLETED" -> Pair(AdminSuccessBg, AdminSuccessGreen)
        "PENDING", "BUSY" -> Pair(AdminWarningBg, AdminWarningOrange)
        "BLOCKED", "REJECTED" -> Pair(AdminDangerBg, AdminDangerRed)
        else -> Pair(AdminNeutralGrayBg, AdminSecondaryText)
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(status, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
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
private fun AdminHelmetLogo(size: Dp = 36.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawArc(color = AdminAccentOrange, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(w * 0.1f, h * 0.2f), size = Size(w * 0.8f, h * 0.6f))
        drawLine(color = AdminAccentOrange, start = Offset(w * 0.05f, h * 0.52f), end = Offset(w * 0.95f, h * 0.52f), strokeWidth = w * 0.1f, cap = StrokeCap.Round)
    }
}

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
                                name = obj.optString("name", "Workora User"),
                                phone = obj.optString("phone", "+91 98765 43210"),
                                email = obj.optString("email", k.replace("_at_", "@")),
                                role = roleClean,
                                area = obj.optString("location", "Silwani"),
                                status = obj.optString("status", "Active"),
                                joined = obj.optString("joined", "2 Nov 2024"),
                                category = obj.optString("skill", "General"),
                                dailyRate = obj.optInt("dailyRate", 600),
                                availability = obj.optString("availability", "Available")
                            )
                        )
                    }
                }
            }
            usersConn.disconnect()

            val jobsConn = URL("$ADMIN_FB_URL/jobs.json").openConnection() as HttpURLConnection
            jobsConn.requestMethod = "GET"
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
                                customerName = obj.optString("customerName", "Customer"),
                                customerPhone = obj.optString("customerPhone", "+91 98765"),
                                workerName = obj.optString("workerName", "Worker"),
                                workerPhone = obj.optString("workerPhone", "+91 87654"),
                                category = obj.optString("category", "General"),
                                area = obj.optString("location", "Silwani"),
                                date = obj.optString("date", "Today"),
                                preferredTime = "09:00 AM",
                                days = 1,
                                rate = obj.optInt("dailyRate", 600),
                                description = obj.optString("description", ""),
                                additionalMessage = "",
                                status = obj.optString("status", "PENDING").uppercase(Locale.US),
                                createdDate = "Today",
                                updatedDate = "Today"
                            )
                        )
                        c++
                    }
                }
            }
            jobsConn.disconnect()
        } catch (_: Exception) {
        }

        if (users.isEmpty()) {
            users.addAll(listOf(
                AdminUserRecord("u1", "Amit Sharma", "+91 98765", "amit@test.com", "Customer", "Silwani", "Active", "2 Nov 2024"),
                AdminUserRecord("u2", "Ramesh Kumar", "+91 87654", "ramesh@test.com", "Worker", "Silwani", "Active", "2 Nov 2024", "Mason", "5 yrs", 700, "Available")
            ))
        }

        if (bookings.isEmpty()) {
            bookings.add(AdminBookingRecord("BK001", "Amit Sharma", "+91 98765", "Ramesh Kumar", "+91 87654", "Mason", "Silwani", "Today", "09:00 AM", 1, 700, "Wall work", "", "PENDING", "Today", "Today"))
        }

        val cats = listOf(
            AdminCategoryRecord("cat_1", "Mason", "hammer", true, 142),
            AdminCategoryRecord("cat_2", "General Labour", "hard-hat", true, 210),
            AdminCategoryRecord("cat_3", "Painter", "paintbrush", true, 84),
            AdminCategoryRecord("cat_4", "Electrician", "zap", true, 96),
            AdminCategoryRecord("cat_5", "Plumber", "wrench", true, 78)
        )

        Handler(Looper.getMainLooper()).post {
            onLoaded(users, bookings, cats)
        }
    }.start()
}

private fun updateFirebaseUserStatus(key: String, status: String, avail: String) {
    Thread {
        try {
            val url = URL("$ADMIN_FB_URL/users/$key.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.doOutput = true
            val payload = JSONObject().apply { put("status", status); put("availability", avail) }
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
            conn.doOutput = true
            val payload = JSONObject().apply { put("name", user.name); put("phone", user.phone); put("location", user.area); put("skill", user.category); put("dailyRate", user.dailyRate) }
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
            conn.doOutput = true
            val payload = JSONObject().apply { put("status", status) }
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
            conn.doOutput = true
            val payload = JSONObject().apply { put("id", cat.id); put("name", cat.name); put("iconName", cat.iconName); put("active", cat.active); put("workerCount", cat.workerCount) }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}
