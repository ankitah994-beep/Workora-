package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkoraDatabase
import com.example.data.WorkoraRepository
import com.example.model.JobApplication
import com.example.model.JobPost
import com.example.model.ScreenState
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.model.WorkerProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkoraRepository

    init {
        val database = WorkoraDatabase.getDatabase(application, viewModelScope)
        repository = WorkoraRepository(database.workoraDao())
    }

    // ऐप अब सीधे LANGUAGE पेज से शुरू होगा
    private val _screenState = MutableStateFlow(ScreenState.LANGUAGE)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _selectedRole = MutableStateFlow<UserRole?>(UserRole.CUSTOMER)
    val selectedRole: StateFlow<UserRole?> = _selectedRole.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isWorkerAvailable = MutableStateFlow(true)
    val isWorkerAvailable: StateFlow<Boolean> = _isWorkerAvailable.asStateFlow()

    private val _customerTab = MutableStateFlow(0)
    val customerTab: StateFlow<Int> = _customerTab.asStateFlow()

    private val _labourTab = MutableStateFlow(0)
    val labourTab: StateFlow<Int> = _labourTab.asStateFlow()

    // Observable flows from repository
    private val rawWorkers: StateFlow<List<WorkerProfile>> = _selectedCategoryFilter
        .flatMapLatest { category ->
            repository.getWorkersByTrade(category)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered by category + search query
    val workers: StateFlow<List<WorkerProfile>> = combine(rawWorkers, _searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter {
                it.name.lowercase().contains(q) ||
                        it.trade.lowercase().contains(q) ||
                        it.location.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobs: StateFlow<List<JobPost>> = _selectedCategoryFilter
        .flatMapLatest { category ->
            repository.getJobsByCategory(category)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val applications: StateFlow<List<JobApplication>> = repository.allApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation methods
    fun navigateTo(newState: ScreenState) {
        _screenState.value = newState
    }

    fun selectRole(role: UserRole) {
        _selectedRole.value = role
        val roleLabel = if (role == UserRole.CUSTOMER) "Customer (Hire Workers)" else "Labour (Find Work)"
        showToast("Selected: $roleLabel")
    }

    fun setRole(role: UserRole) {
        _selectedRole.value = role
    }

    fun onUserLoggedIn(role: UserRole) {
        _selectedRole.value = role
        _screenState.value = if (role == UserRole.CUSTOMER) {
            ScreenState.CUSTOMER_HOME
        } else {
            ScreenState.LABOUR_HOME
        }
    }

    fun openProfile() {
        _screenState.value = ScreenState.PROFILE
    }

    fun closeProfile() {
        _screenState.value = if (_selectedRole.value == UserRole.LABOUR) {
            ScreenState.LABOUR_HOME
        } else {
            ScreenState.CUSTOMER_HOME
        }
    }

    fun openPostWork() {
        _screenState.value = ScreenState.POST_WORK
    }

    fun closePostWork() {
        _screenState.value = ScreenState.CUSTOMER_HOME
    }

    fun switchRole() {
        val current = _selectedRole.value ?: UserRole.CUSTOMER
        val newRole = if (current == UserRole.CUSTOMER) UserRole.LABOUR else UserRole.CUSTOMER
        _selectedRole.value = newRole
        _screenState.value = if (newRole == UserRole.CUSTOMER) ScreenState.CUSTOMER_HOME else ScreenState.LABOUR_HOME
        showToast("Switched to ${if (newRole == UserRole.CUSTOMER) "Hire" else "Work"} mode")
    }

    fun goToAccountType() {
        _selectedRole.value = null
        _screenState.value = ScreenState.ACCOUNT_SELECTION
    }

    fun logout() {
        _selectedRole.value = null
        _screenState.value = ScreenState.LOGIN
        showToast("Logged out successfully")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setCustomerTab(tabIndex: Int) {
        _customerTab.value = tabIndex
    }

    fun setLabourTab(tabIndex: Int) {
        _labourTab.value = tabIndex
    }

    fun toggleWorkerAvailability() {
        val newStatus = !_isWorkerAvailable.value
        _isWorkerAvailable.value = newStatus
        viewModelScope.launch {
            repository.setWorkerAvailability(1L, newStatus)
            showToast(if (newStatus) "Status: Available for Work today!" else "Status: Marked Busy / Offline")
        }
    }

    fun postNewJob(
        title: String,
        category: String,
        description: String,
        dailyRate: Int,
        location: String,
        workersNeeded: Int,
        urgency: String,
        dateTime: String = "Today, 9:00 AM"
    ) {
        viewModelScope.launch {
            val finalTitle = title.trim().ifEmpty { "$category Work Required" }
            val finalDesc = description.trim().ifEmpty { "Experienced $category needed at $location." }
            val finalLoc = location.trim().ifEmpty { "Main Road, Sector 12" }
            val job = JobPost(
                title = finalTitle,
                category = category,
                description = finalDesc,
                dailyRate = dailyRate,
                location = finalLoc,
                workersNeeded = workersNeeded,
                urgency = urgency,
                dateTime = dateTime,
                customerName = "Ramesh Verma",
                customerPhone = "+91 98765 43210",
                status = "PENDING"
            )
            repository.postJob(job)
            showToast("Work request created! Sent to local workers.")
            _customerTab.value = 1
        }
    }

    fun acceptJob(job: JobPost) {
        viewModelScope.launch {
            repository.acceptJobRequest(job, workerName = "Sunil Kumar", workerId = 1L)
            showToast("Request Accepted! Added to My Jobs.")
        }
    }

    fun rejectJob(job: JobPost) {
        viewModelScope.launch {
            repository.rejectJobRequest(job, workerName = "Sunil Kumar", workerId = 1L)
            showToast("Work request rejected.")
        }
    }

    fun completeJob(jobId: Long) {
        viewModelScope.launch {
            repository.completeJob(jobId)
            showToast("Work marked as Completed! Great job.")
        }
    }

    fun applyForJob(job: JobPost) {
        viewModelScope.launch {
            repository.applyForJob(job, workerName = "Sunil Kumar", workerId = 1L)
            showToast("Applied to '${job.title}'! Employer notified.")
        }
    }

    fun hireWorkerDirectly(worker: WorkerProfile) {
        viewModelScope.launch {
            showToast("Calling ${worker.name} (${worker.phone})...")
        }
    }

    fun showToast(message: String) {
        _toastMessage.value = message
        viewModelScope.launch {
            delay(2400)
            if (_toastMessage.value == message) {
                _toastMessage.value = null
            }
        }
    }

    fun dismissToast() {
        _toastMessage.value = null
    }
}
