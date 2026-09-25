package com.example

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.parseColor("#15803D")))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF15803D)
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
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

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

    var showGreenWelcomeScreen by remember { mutableStateOf(true) }

    var isDirectAdminPanelOpen by remember {
        val isLogged = authPrefs.getBoolean("is_logged_in", false)
        val savedRole = authPrefs.getString("saved_user_role", null)
        mutableStateOf(isLogged && savedRole == "ADMIN")
    }
    var activeAdminEmail by remember {
        mutableStateOf(authPrefs.getString("last_logged_in_email", "") ?: "")
    }
    var activeAdminTier by remember {
        mutableStateOf(authPrefs.getString("saved_admin_tier", "SUPER_ADMIN") ?: "SUPER_ADMIN")
    }

    LaunchedEffect(Unit) {
        val isLogged = authPrefs.getBoolean("is_logged_in", false)
        val savedEmail = authPrefs.getString("last_logged_in_email", "") ?: ""
        if (isLogged && savedEmail.isNotBlank()) {
            FirebaseManager.checkIfEmailIsAdminOnCloud(savedEmail) { isAdmin, adminTier ->
                if (isAdmin && authPrefs.getString("saved_user_role", null) == "ADMIN") {
                    activeAdminEmail = savedEmail
                    activeAdminTier = adminTier
                    isDirectAdminPanelOpen = true
                }
            }
        } else {
            isDirectAdminPanelOpen = false
        }
    }

    if (showGreenWelcomeScreen) {
        LanguageSelectionScreen(
            onLanguageSelected = {
                showGreenWelcomeScreen = false
                val isLogged = authPrefs.getBoolean("is_logged_in", false)
                val savedEmail = authPrefs.getString("last_logged_in_email", "") ?: ""
                if (!isLogged || savedEmail.isBlank()) {
                    isDirectAdminPanelOpen = false
                    viewModel.navigateTo(ScreenState.LOGIN)
                } else if (!isDirectAdminPanelOpen) {
                    viewModel.navigateTo(ScreenState.ACCOUNT_SELECTION)
                }
            }
        )
        return
    }

    if (isDirectAdminPanelOpen) {
        AdminDashboardScreen(
            adminEmail = activeAdminEmail,
            adminTier = activeAdminTier,
            onLogoutAdmin = {
                isDirectAdminPanelOpen = false
                authPrefs.edit()
                    .putBoolean("is_logged_in", false)
                    .remove("last_logged_in_email")
                    .remove("saved_user_role")
                    .apply()
                viewModel.navigateTo(ScreenState.LOGIN)
            }
        )
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
                            isDirectAdminPanelOpen = false
                            authPrefs.edit()
                                .putBoolean("is_logged_in", false)
                                .remove("last_logged_in_email")
                                .remove("saved_user_role")
                                .apply()
                            viewModel.navigateTo(ScreenState.LOGIN)
                        }
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
                        onOpenAdmin = { isDirectAdminPanelOpen = true },
                        onUpdateProfile = { _, _, _ -> }
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
