package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreJobRepositoryImpl
import com.example.data.FirestoreWorkerRepositoryImpl
import com.example.data.JobRepository
import com.example.data.WorkerRepository
import com.example.model.Job
import com.example.model.Worker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * HomeViewModel managing state for the Customer Home Screen where customers browse workers
 * and view their posted jobs fetched from Firestore.
 */
class HomeViewModel(
    private val workerRepository: WorkerRepository = FirestoreWorkerRepositoryImpl(),
    private val jobRepository: JobRepository = FirestoreJobRepositoryImpl(),
    private val auth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()
) : ViewModel() {

    // Default dummy list of 3-4 workers matching the requested trades and details
    val dummyWorkers = listOf(
        Worker(
            id = 1,
            name = "Rajesh Sharma",
            trade = "Mason",
            dailyWage = 800,
            experienceYears = 8,
            rating = 4.8f,
            reviewsCount = 34,
            location = "Sector 14, Gurugram",
            distance = "1.2 km away",
            phone = "+91 98765 43210",
            isAvailableToday = true,
            isVerified = true
        ),
        Worker(
            id = 2,
            name = "Vikram Singh",
            trade = "Electrician",
            dailyWage = 750,
            experienceYears = 6,
            rating = 4.9f,
            reviewsCount = 45,
            location = "DLF Phase 3, Gurugram",
            distance = "3.1 km away",
            phone = "+91 98234 56789",
            isAvailableToday = true,
            isVerified = true
        ),
        Worker(
            id = 3,
            name = "Manoj Tiwari",
            trade = "Plumber",
            dailyWage = 700,
            experienceYears = 5,
            rating = 4.7f,
            reviewsCount = 28,
            location = "Old Railway Road, Gurugram",
            distance = "2.4 km away",
            phone = "+91 98123 45678",
            isAvailableToday = true,
            isVerified = true
        ),
        Worker(
            id = 4,
            name = "Suresh Prajapati",
            trade = "Carpenter",
            dailyWage = 850,
            experienceYears = 9,
            rating = 4.8f,
            reviewsCount = 52,
            location = "MG Road, Gurugram",
            distance = "1.8 km away",
            phone = "+91 98345 67890",
            isAvailableToday = true,
            isVerified = true
        )
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Raw flow from WorkerRepository, defaulting to dummyWorkers if empty
    val rawWorkers: StateFlow<List<Worker>> = workerRepository.getAvailableWorkers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = dummyWorkers
        )

    // Exposes filtered list of available workers based on search query and category
    val availableWorkers: StateFlow<List<Worker>> = combine(
        rawWorkers,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        val sourceList = if (list.isNotEmpty()) list else dummyWorkers
        sourceList.filter { worker ->
            val matchesCategory = category == "All" || worker.trade.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    worker.name.contains(query, ignoreCase = true) ||
                    worker.trade.contains(query, ignoreCase = true) ||
                    worker.location.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = dummyWorkers
    )

    // Direct alias for available workers
    val workers: StateFlow<List<Worker>> = availableWorkers

    // Tab state: 0 = "Find Workers", 1 = "My Posted Work"
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _postedJobs = MutableStateFlow<List<Job>>(emptyList())
    val postedJobs: StateFlow<List<Job>> = _postedJobs.asStateFlow()

    private val _isLoadingPostedJobs = MutableStateFlow(false)
    val isLoadingPostedJobs: StateFlow<Boolean> = _isLoadingPostedJobs.asStateFlow()

    init {
        // Fetch jobs for currently logged in user if available
        fetchCustomerJobs()
    }

    /**
     * Fetches Job documents from the Firestore "jobs" collection where the
     * customerId matches the currently logged-in user's UID or the provided customerId.
     * Exposes the result as StateFlow.
     */
    fun fetchCustomerJobs(customerId: String? = null) {
        val uid = customerId?.takeIf { it.isNotBlank() }
            ?: auth?.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: ""

        viewModelScope.launch {
            _isLoadingPostedJobs.value = true
            jobRepository.getPostedJobsByCustomer(uid).collect { jobs ->
                _postedJobs.value = jobs
                _isLoadingPostedJobs.value = false
            }
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun getWorkerById(workerId: Long): Worker? {
        return availableWorkers.value.firstOrNull { it.id == workerId }
            ?: dummyWorkers.firstOrNull { it.id == workerId }
    }
}
