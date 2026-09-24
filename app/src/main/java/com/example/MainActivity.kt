package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenState
import com.example.model.UserRole
import com.example.ui.screens.AccountSelectScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CustomerDashboardScreen
import com.example.ui.screens.JobHistoryScreen
import com.example.ui.screens.LabourDashboardScreen
import com.example.ui.screens.LanguageSelectionScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.PostJobScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchFilterScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.theme.WorkoraTheme
import com.example.viewmodel.WorkoraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkoraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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

    AnimatedContent(
        targetState = screenState,
        transitionSpec = {
            (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith
                    (slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
        },
        label = "screen_transition"
    ) { currentScreen ->
        when (currentScreen) {
            ScreenState.LANGUAGE -> {
                LanguageSelectionScreen(
                    onLanguageSelected = { viewModel.navigateTo(ScreenState.LOGIN) }
                )
            }
            ScreenState.LOGIN -> {
                LoginScreen(
                    onLogin = { _, _ -> viewModel.navigateTo(ScreenState.ACCOUNT_SELECTION) },
                    onNavigateToSignUp = { viewModel.navigateTo(ScreenState.SIGN_UP) }
                )
            }
            ScreenState.SIGN_UP -> {
                SignUpScreen(
                    onSignUp = { _, _, _, _ -> viewModel.navigateTo(ScreenState.ACCOUNT_SELECTION) },
                    onNavigateToLogin = { viewModel.navigateTo(ScreenState.LOGIN) }
                )
            }
            ScreenState.ACCOUNT_SELECTION -> {
                AccountSelectScreen(
                    onSelectRole = { role -> 
                        viewModel.selectRole(role)
                        if (role == UserRole.CUSTOMER) {
                            viewModel.navigateTo(ScreenState.CUSTOMER_HOME)
                        } else {
                            viewModel.navigateTo(ScreenState.LABOUR_HOME)
                        }
                    },
                    toastMessage = toastMessage
                )
            }
            ScreenState.CUSTOMER_HOME -> {
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
                    onPostJob = { _, _, _, _, _, _, _, _ ->
                        viewModel.navigateTo(ScreenState.POST_JOB)
                    },
                    onHireWorker = { worker ->
                        viewModel.navigateTo(ScreenState.CHAT)
                    },
                    onCompleteJob = { jobId ->
                        viewModel.completeJob(jobId)
                    },
                    onSwitchRole = { viewModel.switchRole() },
                    onOpenProfile = { viewModel.openProfile() },
                    onOpenNotifications = { viewModel.navigateTo(ScreenState.NOTIFICATIONS) },
                    onOpenFilters = { viewModel.navigateTo(ScreenState.SEARCH_FILTER) },
                    toastMessage = toastMessage
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
                    onSwitchRole = { viewModel.switchRole() },
                    onOpenProfile = { viewModel.openProfile() },
                    toastMessage = toastMessage
                )
            }
            ScreenState.PROFILE -> {
                ProfileScreen(
                    role = selectedRole ?: UserRole.CUSTOMER,
                    userName = "Ankit Ahirwar",
                    userPhone = "+91 98765 43210",
                    userLocation = "Silwani, Raisen",
                    onBack = { 
                        if (selectedRole == UserRole.CUSTOMER) viewModel.navigateTo(ScreenState.CUSTOMER_HOME) 
                        else viewModel.navigateTo(ScreenState.LABOUR_HOME)
                    },
                    onSwitchRole = { viewModel.switchRole() },
                    onLogout = { viewModel.navigateTo(ScreenState.LOGIN) },
                    onUpdateProfile = { _, _, _ -> }
                )
            }
            ScreenState.POST_JOB -> {
                PostJobScreen(
                    onBack = { viewModel.navigateTo(ScreenState.CUSTOMER_HOME) },
                    onSubmit = { viewModel.navigateTo(ScreenState.CUSTOMER_HOME) }
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
