package com.example

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenState
import com.example.model.UserRole
import com.example.ui.screens.AccountSelectScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CustomerDashboardScreen
import com.example.ui.screens.FirebaseManager
import com.example.ui.screens.JobHistoryScreen
import com.example.ui.screens.LabourDashboardScreen
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchFilterScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.theme.WorkoraTheme
import com.example.viewmodel.WorkoraViewModel
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

// Session flag so Welcome Page never re-opens on orientation change
private var hasShownWelcomeOnceInSession = false
private const val MAIN_FIREBASE_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.parseColor("#0B2345")))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0B2345)
                ) {
                    WorkoraApp()
                }
            }
        }
    }
}

@Composable
fun WorkoraApp(
    viewModel: WorkoraViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val workers by viewModel.workers.collectAsStateWithLifecycle()
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val applications by viewModel.applications.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val isWorkerAvailable by viewModel.isWorkerAvailable.collectAsStateWithLifecycle()
    val customerTab by viewModel.customerTab.collectAsStateWithLifecycle()
    val labourTab by viewModel.labourTab.collectAsStateWithLifecycle()

    var showGreenWelcomeScreen by rememberSaveable {
        mutableStateOf(!hasShownWelcomeOnceInSession)
    }

    var isDirectAdminPanelOpen by rememberSaveable {
        val isLogged = authPrefs.getBoolean("is_logged_in", false)
        val savedRole = authPrefs.getString("saved_user_role", null)
        mutableStateOf(isLogged && savedRole == "ADMIN")
    }
    var activeAdminEmail by rememberSaveable {
        mutableStateOf(authPrefs.getString("last_logged_in_email", "") ?: "")
    }
    var activeAdminTier by rememberSaveable {
        mutableStateOf(authPrefs.getString("saved_admin_tier", "SUPER_ADMIN") ?: "SUPER_ADMIN")
    }

    // State / Area Service Availability Lock State
    var blockedStateAreaName by remember { mutableStateOf<String?>(null) }
    var showChangeAreaQuickDialog by remember { mutableStateOf(false) }

    val checkUserAreaServiceStatus: () -> Unit = {
        val userLoc = profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)"
        checkIfAreaIsDisabledByAdmin(
            userLocation = userLoc,
            settingsPrefs = settingsPrefs
        ) { disabledRegion ->
            blockedStateAreaName = disabledRegion
        }
    }

    LaunchedEffect(screenState, isDirectAdminPanelOpen) {
        if (!isDirectAdminPanelOpen) {
            checkUserAreaServiceStatus()
        }
    }

    val proceedAfterWelcome: () -> Unit = {
        if (showGreenWelcomeScreen) {
            hasShownWelcomeOnceInSession = true
            showGreenWelcomeScreen = false
            val isLogged = authPrefs.getBoolean("is_logged_in", false)
            val savedEmail = authPrefs.getString("last_logged_in_email", "") ?: ""
            val savedRole = authPrefs.getString("saved_user_role", null)

            if (!isLogged || savedEmail.isBlank()) {
                isDirectAdminPanelOpen = false
                viewModel.navigateTo(ScreenState.LOGIN)
            } else if (savedRole == "ADMIN") {
                isDirectAdminPanelOpen = true
            } else if (savedRole == "CUSTOMER") {
                isDirectAdminPanelOpen = false
                viewModel.selectRole(UserRole.CUSTOMER)
                viewModel.navigateTo(ScreenState.CUSTOMER_HOME)
            } else if (savedRole == "LABOUR") {
                isDirectAdminPanelOpen = false
                viewModel.selectRole(UserRole.LABOUR)
                viewModel.navigateTo(ScreenState.LABOUR_HOME)
            } else {
                isDirectAdminPanelOpen = false
                viewModel.navigateTo(ScreenState.ACCOUNT_SELECTION)
            }
        }
    }

    // Strict Orientation Control: Welcome & Normal App = PORTRAIT, Admin Panel = LANDSCAPE
    val shouldBeLandscape = !showGreenWelcomeScreen &&
        (isDirectAdminPanelOpen || screenState == ScreenState.ADMIN_DASHBOARD)

    LaunchedEffect(shouldBeLandscape) {
        val desiredOrientation = if (shouldBeLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        if (activity != null && activity.requestedOrientation != desiredOrientation) {
            activity.requestedOrientation = desiredOrientation
        }
    }

    // Fast 1-second auto transition from Welcome Screen
    LaunchedEffect(showGreenWelcomeScreen) {
        if (showGreenWelcomeScreen) {
            delay(1000L)
            proceedAfterWelcome()
        }
    }

    val handleAdminRoleSwitch: (String) -> Unit = { targetRole ->
        hasShownWelcomeOnceInSession = true
        showGreenWelcomeScreen = false
        isDirectAdminPanelOpen = false
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        checkUserAreaServiceStatus()
        if (targetRole.equals("CUSTOMER", ignoreCase = true)) {
            authPrefs.edit().putString("saved_user_role", "CUSTOMER").apply()
            viewModel.selectRole(UserRole.CUSTOMER)
            viewModel.navigateTo(ScreenState.CUSTOMER_HOME)
            Toast.makeText(context, "Switched to Customer Mode ✓", Toast.LENGTH_SHORT).show()
        } else {
            authPrefs.edit().putString("saved_user_role", "LABOUR").apply()
            viewModel.selectRole(UserRole.LABOUR)
            viewModel.navigateTo(ScreenState.LABOUR_HOME)
            Toast.makeText(context, "Switched to Worker Mode ✓", Toast.LENGTH_SHORT).show()
        }
    }

    if (showGreenWelcomeScreen) {
        LanguageSelectionScreen(
            onLanguageSelected = {
                proceedAfterWelcome()
            }
        )
        return
    }

    if (isDirectAdminPanelOpen) {
        AdminDashboardScreen(
            adminEmail = activeAdminEmail,
            adminTier = activeAdminTier,
            onLogoutAdmin = {
                hasShownWelcomeOnceInSession = true
                showGreenWelcomeScreen = false
                isDirectAdminPanelOpen = false
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                authPrefs.edit()
                    .putBoolean("is_logged_in", false)
                    .remove("last_logged_in_email")
                    .remove("saved_user_role")
                    .apply()
                viewModel.navigateTo(ScreenState.LOGIN)
            },
            onSwitchRoleFromAdmin = handleAdminRoleSwitch
        )
        return
    }

    // If Admin has disabled service in user's current State / Area, show Service Closed screen
    val isUserScreen = screenState == ScreenState.CUSTOMER_HOME ||
        screenState == ScreenState.LABOUR_HOME ||
        screenState == ScreenState.POST_JOB

    if (isUserScreen && !blockedStateAreaName.isNullOrBlank()) {
        val currentLoc = profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)"
        val loggedEmail = authPrefs.getString("last_logged_in_email", "") ?: ""
        val isSuperAdminUser = loggedEmail.equals("ankitah994@gmail.com", ignoreCase = true)

        StateServiceDisabledScreen(
            disabledRegionName = blockedStateAreaName!!,
            userCurrentLocation = currentLoc,
            isSuperAdminUser = isSuperAdminUser,
            onRefreshStatus = {
                checkUserAreaServiceStatus()
                Toast.makeText(context, "Checking service status...", Toast.LENGTH_SHORT).show()
            },
            onChangeLocationClick = { showChangeAreaQuickDialog = true },
            onOpenAdminPanel = {
                authPrefs.edit().putString("saved_user_role", "ADMIN").apply()
                isDirectAdminPanelOpen = true
            }
        )

        if (showChangeAreaQuickDialog) {
            var newAreaInput by remember { mutableStateOf(currentLoc) }
            AlertDialog(
                onDismissRequest = { showChangeAreaQuickDialog = false },
                containerColor = Color.White,
                title = {
                    Text("Change Your State / Area", fontWeight = FontWeight.Bold, color = Color(0xFF0B2345))
                },
                text = {
                    OutlinedTextField(
                        value = newAreaInput,
                        onValueChange = { newAreaInput = it },
                        label = { Text("Enter active City / State (e.g. Bhopal, Delhi)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newAreaInput.isNotBlank()) {
                                profilePrefs.edit().putString("user_location", newAreaInput.trim()).apply()
                                showChangeAreaQuickDialog = false
                                checkUserAreaServiceStatus()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00))
                    ) {
                        Text("Update Location", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showChangeAreaQuickDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        AnimatedContent(
            targetState = screenState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { currentScreen ->
            when (currentScreen) {
                ScreenState.LANGUAGE, ScreenState.LOGIN -> {
                    LoginScreen(
                        onLogin = { emailInput, _ ->
                            val cleanEmail = emailInput.trim().lowercase()
                            authPrefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("last_logged_in_email", cleanEmail)
                                .apply()
                            activeAdminEmail = cleanEmail
                            FirebaseManager.checkIfEmailIsAdminOnCloud(cleanEmail) { isAdmin, adminTier ->
                                if (isAdmin) {
                                    authPrefs.edit()
                                        .putString("saved_user_role", "ADMIN")
                                        .putString("saved_admin_tier", adminTier)
                                        .apply()
                                    activeAdminTier = adminTier
                                    isDirectAdminPanelOpen = true
                                } else {
                                    authPrefs.edit().remove("saved_user_role").apply()
                                    isDirectAdminPanelOpen = false
                                    viewModel.navigateTo(ScreenState.ACCOUNT_SELECTION)
                                }
                            }
                        },
                        onNavigateToSignUp = { viewModel.navigateTo(ScreenState.SIGN_UP) }
                    )
                }
                ScreenState.SIGN_UP -> {
                    SignUpScreen(
                        onSignUp = { _, emailInput, _, _ ->
                            val cleanEmail = emailInput.trim().lowercase()
                            authPrefs.edit()
                                .putBoolean("is_logged_in", true)
                                .putString("last_logged_in_email", cleanEmail)
                                .apply()
                            activeAdminEmail = cleanEmail
                            FirebaseManager.checkIfEmailIsAdminOnCloud(cleanEmail) { isAdmin, adminTier ->
                                if (isAdmin) {
                                    authPrefs.edit()
                                        .putString("saved_user_role", "ADMIN")
                                        .putString("saved_admin_tier", adminTier)
                                        .apply()
                                    activeAdminTier = adminTier
                                    isDirectAdminPanelOpen = true
                                } else {
                                    authPrefs.edit().remove("saved_user_role").apply()
                                    isDirectAdminPanelOpen = false
                                    viewModel.navigateTo(ScreenState.ACCOUNT_SELECTION)
                                }
                            }
                        },
                        onNavigateToLogin = { viewModel.navigateTo(ScreenState.LOGIN) }
                    )
                }
                ScreenState.ACCOUNT_SELECTION -> {
                    AccountSelectScreen(
                        onSelectRole = { role ->
                            viewModel.selectRole(role)
                            authPrefs.edit().putString("saved_user_role", role.name).apply()
                            if (role == UserRole.CUSTOMER) {
                                viewModel.navigateTo(ScreenState.CUSTOMER_HOME)
                            } else {
                                viewModel.navigateTo(ScreenState.LABOUR_HOME)
                            }
                        },
                        toastMessage = toastMessage
                    )
                }
                ScreenState.ADMIN_DASHBOARD -> {
                    AdminDashboardScreen(
                        adminEmail = activeAdminEmail,
                        adminTier = activeAdminTier,
                        onLogoutAdmin = {
                            hasShownWelcomeOnceInSession = true
                            showGreenWelcomeScreen = false
                            isDirectAdminPanelOpen = false
                            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            authPrefs.edit()
                                .putBoolean("is_logged_in", false)
                                .remove("last_logged_in_email")
                                .remove("saved_user_role")
                                .apply()
                            viewModel.navigateTo(ScreenState.LOGIN)
                        },
                        onSwitchRoleFromAdmin = handleAdminRoleSwitch
                    )
                }
                ScreenState.CUSTOMER_HOME, ScreenState.POST_JOB -> {
                    CustomerDashboardScreen(
                        currentUser = null,
                        searchQuery = searchQuery,
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                        workers = workers,
                        jobs = jobs,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.setCategoryFilter(it) },
                        activeTab = customerTab,
                        onTabSelected = { viewModel.setCustomerTab(it) },
                        onPostJob = { title, category, description, dailyRate, location, workersNeeded, urgency, _ ->
                            val cName = profilePrefs.getString("user_name", "Ankit Ahirwar") ?: "Ankit Ahirwar"
                            val cPhone = profilePrefs.getString("user_phone", "+91 6265798340") ?: "+91 6265798340"
                            FirebaseManager.postJobToFirebase(
                                title = title,
                                category = category,
                                description = description,
                                dailyRate = dailyRate,
                                location = location,
                                workersNeeded = workersNeeded,
                                urgency = urgency,
                                customerName = cName,
                                customerPhone = cPhone
                            ) {
                                Toast.makeText(context, "Work Request Sent & Saved Live! ✓", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onHireWorker = { worker ->
                            Toast.makeText(context, "Direct Work Request Sent to ${worker.name}! ✓", Toast.LENGTH_SHORT).show()
                        },
                        onCompleteJob = { jobId ->
                            viewModel.completeJob(jobId)
                        },
                        onSwitchRole = {
                            authPrefs.edit().putString("saved_user_role", "LABOUR").apply()
                            viewModel.switchRole()
                        },
                        onOpenProfile = { viewModel.openProfile() },
                        onOpenNotifications = { viewModel.navigateTo(ScreenState.NOTIFICATIONS) },
                        onOpenFilters = { viewModel.navigateTo(ScreenState.SEARCH_FILTER) },
                        toastMessage = toastMessage,
                        onOpenChat = { viewModel.navigateTo(ScreenState.CHAT) }
                    )
                }
                ScreenState.LABOUR_HOME -> {
                    LabourDashboardScreen(
                        currentUser = null,
                        jobs = jobs,
                        applications = applications,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.setCategoryFilter(it) },
                        activeTab = labourTab,
                        onTabSelected = { viewModel.setLabourTab(it) },
                        isAvailable = isWorkerAvailable,
                        onToggleAvailability = { viewModel.toggleWorkerAvailability() },
                        onApplyJob = { job -> viewModel.applyForJob(job) },
                        onAcceptJob = { job -> viewModel.acceptJob(job) },
                        onRejectJob = { job -> viewModel.rejectJob(job) },
                        onCompleteJob = { jobId -> viewModel.completeJob(jobId) },
                        onSwitchRole = {
                            authPrefs.edit().putString("saved_user_role", "CUSTOMER").apply()
                            viewModel.switchRole()
                        },
                        onOpenProfile = { viewModel.openProfile() },
                        toastMessage = toastMessage,
                        onOpenChat = { viewModel.navigateTo(ScreenState.CHAT) }
                    )
                }
                ScreenState.PROFILE -> {
                    ProfileScreen(
                        role = selectedRole ?: UserRole.CUSTOMER,
                        userName = "Ankit Ahirwar",
                        userPhone = "+91 6265798340",
                        userLocation = "Silwani, Raisen",
                        onBack = {
                            checkUserAreaServiceStatus()
                            if (selectedRole == UserRole.CUSTOMER) viewModel.navigateTo(ScreenState.CUSTOMER_HOME)
                            else viewModel.navigateTo(ScreenState.LABOUR_HOME)
                        },
                        onSwitchRole = {
                            val nextRole = if (selectedRole == UserRole.CUSTOMER) "LABOUR" else "CUSTOMER"
                            authPrefs.edit().putString("saved_user_role", nextRole).apply()
                            viewModel.switchRole()
                        },
                        onLogout = {
                            authPrefs.edit()
                                .putBoolean("is_logged_in", false)
                                .remove("last_logged_in_email")
                                .remove("saved_user_role")
                                .apply()
                            isDirectAdminPanelOpen = false
                            viewModel.navigateTo(ScreenState.LOGIN)
                        },
                        onOpenChat = { viewModel.navigateTo(ScreenState.CHAT) },
                        onOpenAdmin = {
                            hasShownWelcomeOnceInSession = true
                            showGreenWelcomeScreen = false
                            authPrefs.edit().putString("saved_user_role", "ADMIN").apply()
                            isDirectAdminPanelOpen = true
                        },
                        onUpdateProfile = { _, _, _ ->
                            checkUserAreaServiceStatus()
                        }
                    )
                }
                ScreenState.SEARCH_FILTER -> {
                    SearchFilterScreen(
                        onBack = { viewModel.navigateTo(ScreenState.CUSTOMER_HOME) },
                        onApplyFilters = { _, _, _ ->
                            viewModel.navigateTo(ScreenState.CUSTOMER_HOME)
                        }
                    )
                }
                ScreenState.CHAT -> {
                    ChatScreen(
                        onBack = {
                            if (selectedRole == UserRole.CUSTOMER) viewModel.navigateTo(ScreenState.CUSTOMER_HOME)
                            else viewModel.navigateTo(ScreenState.LABOUR_HOME)
                        }
                    )
                }
                ScreenState.NOTIFICATIONS -> {
                    NotificationScreen(
                        onBack = {
                            if (selectedRole == UserRole.CUSTOMER) viewModel.navigateTo(ScreenState.CUSTOMER_HOME)
                            else viewModel.navigateTo(ScreenState.LABOUR_HOME)
                        }
                    )
                }
                ScreenState.JOB_HISTORY -> {
                    JobHistoryScreen(
                        jobs = jobs,
                        onBack = {
                            if (selectedRole == UserRole.CUSTOMER) viewModel.navigateTo(ScreenState.CUSTOMER_HOME)
                            else viewModel.navigateTo(ScreenState.LABOUR_HOME)
                        }
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun StateServiceDisabledScreen(
    disabledRegionName: String,
    userCurrentLocation: String,
    isSuperAdminUser: Boolean,
    onRefreshStatus: () -> Unit,
    onChangeLocationClick: () -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5EAF0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOff,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Service Temporarily Closed in $disabledRegionName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0B2345),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "आपके वर्तमान क्षेत्र ($userCurrentLocation) में एडमिन द्वारा Workora ऐप की सर्विस अस्थायी रूप से बंद की गई है। सर्विस चालू होते ही आप फिर से काम देख और पोस्ट कर सकेंगे।",
                    fontSize = 14.sp,
                    color = Color(0xFF687280),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onRefreshStatus,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF083D91)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check Again (Refresh Status)", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onChangeLocationClick,
                    border = BorderStroke(1.dp, Color(0xFFFF8C00)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Change Location / Area", color = Color(0xFFFF8C00), fontWeight = FontWeight.Bold)
                }

                if (isSuperAdminUser) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = onOpenAdminPanel) {
                        Text(
                            text = "Open Admin Panel to Enable Service →",
                            color = Color(0xFF083D91),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

private fun checkIfAreaIsDisabledByAdmin(
    userLocation: String,
    settingsPrefs: android.content.SharedPreferences,
    onResult: (String?) -> Unit
) {
    val cleanUserLoc = userLocation.trim().lowercase(Locale.US)

    // 1. Instant check against local cached disabled tokens
    val cachedDisabled = settingsPrefs.getString("disabled_state_areas", "") ?: ""
    if (cachedDisabled.isNotBlank()) {
        val tokens = cachedDisabled.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        val matchedLocal = tokens.firstOrNull { token ->
            cleanUserLoc.contains(token.lowercase(Locale.US))
        }
        if (matchedLocal != null) {
            onResult(matchedLocal)
        } else {
            onResult(null)
        }
    }

    // 2. Live verification from Firebase /area_controls
    Thread {
        var matchedDisabledState: String? = null
        val disabledTokensForCache = mutableListOf<String>()
        try {
            val conn = URL("$MAIN_FIREBASE_URL/area_controls.json").openConnection() as HttpURLConnection
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
                        val isEnabled = obj.optBoolean("isServiceEnabled", true)
                        val stateName = obj.optString("stateName", "")
                        val areaKeywords = obj.optString("areaKeywords", "")
                        if (!isEnabled) {
                            val allWords = (listOf(stateName) + areaKeywords.split(","))
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            disabledTokensForCache.addAll(allWords)
                            val hit = allWords.any { w -> cleanUserLoc.contains(w.lowercase(Locale.US)) }
                            if (hit && matchedDisabledState == null) {
                                matchedDisabledState = stateName.ifBlank { areaKeywords }
                            }
                        }
                    }
                }
                settingsPrefs.edit().putString("disabled_state_areas", disabledTokensForCache.joinToString("|")).apply()
                Handler(Looper.getMainLooper()).post {
                    onResult(matchedDisabledState)
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}
