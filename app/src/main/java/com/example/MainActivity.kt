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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.JobStatus
import com.example.model.ScreenState
import com.example.model.UserRole
import com.example.ui.screens.AccountSelectScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.LabourDashboardScreen
import com.example.ui.screens.PostWorkScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.WorkoraTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.JobViewModel
import com.example.viewmodel.PostWorkViewModel
import com.example.viewmodel.ProfileViewModel
import com.example.viewmodel.ReviewViewModel
import com.example.viewmodel.WorkoraViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("MainActivity", "Workora app initialized")
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
    viewModel: WorkoraViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    jobViewModel: JobViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    postWorkViewModel: PostWorkViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val availableWorkers by homeViewModel.availableWorkers.collectAsStateWithLifecycle()
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

    // Determine current screen strictly observing currentUser authentication state
    val targetScreen = when {
        currentUser == null -> {
            when (screenState) {
                ScreenState.ACCOUNT_SELECTION -> ScreenState.ACCOUNT_SELECTION
                else -> ScreenState.AUTH
            }
        }
        screenState == ScreenState.PROFILE -> ScreenState.PROFILE
        screenState == ScreenState.POST_WORK -> ScreenState.POST_WORK
        currentUser?.role.equals("LABOUR", ignoreCase = true) || selectedRole == UserRole.LABOUR -> ScreenState.LABOUR_HOME
        else -> ScreenState.CUSTOMER_HOME
    }

    // Sync viewModel state when user logs in or out
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            val role = if (user.role.equals("LABOUR", ignoreCase = true)) UserRole.LABOUR else UserRole.CUSTOMER
            viewModel.onUserLoggedIn(role)
        }
    }

    AnimatedContent(
        targetState = targetScreen,
        transitionSpec = {
            when {
                ((initialState == ScreenState.AUTH || initialState == ScreenState.ACCOUNT_SELECTION) &&
                        (targetState == ScreenState.CUSTOMER_HOME || targetState == ScreenState.LABOUR_HOME)) -> {
                    (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                }
                (initialState == ScreenState.CUSTOMER_HOME || initialState == ScreenState.LABOUR_HOME || initialState == ScreenState.PROFILE || initialState == ScreenState.AUTH) &&
                        (targetState == ScreenState.AUTH || targetState == ScreenState.ACCOUNT_SELECTION) -> {
                    (slideInHorizontally(initialOffsetX = { -it }) + fadeIn()) togetherWith
                            (slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
                }
                targetState == ScreenState.PROFILE || targetState == ScreenState.POST_WORK -> {
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
            ScreenState.AUTH -> {
                AuthScreen(
                    authViewModel = authViewModel,
                    selectedRole = selectedRole ?: UserRole.CUSTOMER,
                    onRoleChanged = { role -> viewModel.setRole(role) },
                    onBackToRoleSelection = { viewModel.goToAccountType() },
                    onLoginSuccess = { user ->
                        val role = if (user.role.equals("LABOUR", ignoreCase = true)) UserRole.LABOUR else UserRole.CUSTOMER
                        viewModel.onUserLoggedIn(role)
                    },
                    toastMessage = toastMessage
                )
            }
            ScreenState.ACCOUNT_SELECTION -> {
                AccountSelectScreen(
                    onSelectRole = { role -> viewModel.selectRole(role) },
                    toastMessage = toastMessage
                )
            }
            ScreenState.CUSTOMER_HOME -> {
                CustomerHomeScreen(
                    homeViewModel = homeViewModel,
                    currentUser = currentUser,
                    onOpenProfile = { viewModel.openProfile() },
                    onOpenPostWork = { viewModel.openPostWork() },
                    onHireWorker = { worker ->
                        viewModel.hireWorkerDirectly(worker)
                        jobViewModel.createJobRequest(
                            title = "${worker.trade} Hiring Request",
                            workType = worker.trade,
                            description = "Direct hire request for ${worker.name}",
                            offeredWage = worker.dailyWage,
                            location = worker.location,
                            worker = worker
                        )
                    },
                    onPostWorkSubmitted = { title, cat, desc, rate, loc ->
                        viewModel.postNewJob(title, cat, desc, rate, loc, 1, "Today", "Today, 9:00 AM")
                        jobViewModel.createJobRequest(
                            title = title,
                            workType = cat,
                            description = desc,
                            offeredWage = rate,
                            location = loc
                        )
                    },
                    toastMessage = toastMessage
                )
            }
            ScreenState.POST_WORK -> {
                PostWorkScreen(
                    postWorkViewModel = postWorkViewModel,
                    currentUser = currentUser,
                    onBack = { viewModel.closePostWork() },
                    onJobSubmittedSuccessfully = { savedJob ->
                        viewModel.postNewJob(
                            title = savedJob.title,
                            category = savedJob.category,
                            description = savedJob.description,
                            dailyRate = 800,
                            location = savedJob.location,
                            workersNeeded = 1,
                            urgency = "Today",
                            dateTime = savedJob.date
                        )
                        jobViewModel.createJobRequest(
                            title = savedJob.title,
                            workType = savedJob.category,
                            description = savedJob.description,
                            offeredWage = 800,
                            location = savedJob.location,
                            dateTime = savedJob.date
                        )
                        viewModel.closePostWork()
                        viewModel.showToast("Job posted successfully!")
                    },
                    toastMessage = toastMessage
                )
            }
            ScreenState.LABOUR_HOME -> {
                LabourDashboardScreen(
                    jobViewModel = jobViewModel,
                    currentUser = currentUser,
                    jobs = jobs,
                    applications = applications,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    activeTab = labourTab,
                    onTabSelected = { viewModel.setLabourTab(it) },
                    isAvailable = isWorkerAvailable,
                    onToggleAvailability = { viewModel.toggleWorkerAvailability() },
                    onApplyJob = { job -> viewModel.applyForJob(job) },
                    onAcceptJob = { job ->
                        jobViewModel.acceptJob(job.id.toString())
                        viewModel.acceptJob(job)
                    },
                    onRejectJob = { job ->
                        jobViewModel.rejectJob(job.id.toString())
                        viewModel.rejectJob(job)
                    },
                    onCompleteJob = { jobId ->
                        jobViewModel.completeJob(jobId.toString())
                        viewModel.completeJob(jobId)
                    },
                    onSwitchRole = { viewModel.switchRole() },
                    onOpenProfile = { viewModel.openProfile() },
                    toastMessage = toastMessage
                )
            }
            ScreenState.PROFILE -> {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    currentUser = currentUser,
                    role = selectedRole ?: UserRole.CUSTOMER,
                    onBack = { viewModel.closeProfile() },
                    onSwitchRole = { viewModel.switchRole() },
                    onLogout = {
                        authViewModel.logout()
                        viewModel.logout()
                    },
                    toastMessage = toastMessage,
                    onProfileSaved = { msg ->
                        viewModel.showToast(msg)
                    }
                )
            }
        }
    }
}
