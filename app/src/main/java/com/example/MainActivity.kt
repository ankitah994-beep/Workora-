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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenState
import com.example.model.UserRole
import com.example.ui.screens.AccountSelectScreen
import com.example.ui.screens.CustomerDashboardScreen
import com.example.ui.screens.LabourDashboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.WorkoraTheme
import com.example.viewmodel.WorkoraViewModel
import ​com.example.CrashReportScreen
import com.example.CrashStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // VERY FIRST: look for a crash report saved by WorkoraApplication.
        val pendingReport: String? = CrashStore.read(applicationContext)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var crashReport by rememberSaveable { mutableStateOf<String?>(pendingReport) }
            val report: String? = crashReport

            if (report != null) {
                CrashReportScreen(
                    report = report,
                    onContinue = {
                        CrashStore.clear(applicationContext)
                        crashReport = null
                    }
                )
            } else {
                WorkoraTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        WorkoraApp()
                    }
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
            when {
                initialState == ScreenState.ACCOUNT_SELECTION &&
                        (targetState == ScreenState.CUSTOMER_HOME || targetState == ScreenState.LABOUR_HOME) -> {
                    (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                }
                targetState == ScreenState.PROFILE -> {
                    (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                }
                else -> {
                    (slideInHorizontally(initialOffsetX = { -it }) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
                }
            }
        },
        label = "screen_transition"
    ) { currentScreen ->
        when (currentScreen) {
            ScreenState.ACCOUNT_SELECTION -> {
                AccountSelectScreen(
                    onSelectRole = { role -> viewModel.selectRole(role) },
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
                    onPostJob = { title, cat, desc, rate, loc, count, urg, dt ->
                        viewModel.postNewJob(title, cat, desc, rate, loc, count, urg, dt)
                    },
                    onHireWorker = { worker ->
                        viewModel.hireWorkerDirectly(worker)
                    },
                    onCompleteJob = { jobId ->
                        viewModel.completeJob(jobId)
                    },
                    onSwitchRole = { viewModel.switchRole() },
                    onOpenProfile = { viewModel.openProfile() },
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
                    onBack = { viewModel.closeProfile() },
                    onSwitchRole = { viewModel.switchRole() },
                    onLogout = { viewModel.logout() },
                    toastMessage = toastMessage
                )
            }
            ScreenState.AUTH -> {
                AccountSelectScreen(
                    onSelectRole = { role -> viewModel.selectRole(role) },
                    toastMessage = toastMessage
                )
            }
            ScreenState.POST_WORK -> {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
