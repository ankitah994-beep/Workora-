package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreJobRepositoryImpl
import com.example.data.JobRepository
import com.example.model.Job
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * PostWorkViewModel manages form inputs, validation, and saving jobs
 * directly to Firebase Firestore into the "jobs" collection.
 */
class PostWorkViewModel(
    private val jobRepository: JobRepository = FirestoreJobRepositoryImpl(),
    private val auth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()
) : ViewModel() {

    val categories = listOf("Mason", "Plumber", "Electrician", "Carpenter")

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _category = MutableStateFlow("Mason")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _location = MutableStateFlow("Sector 14, Gurugram")
    val location: StateFlow<String> = _location.asStateFlow()

    private val _dateTime = MutableStateFlow("Today, 9:00 AM")
    val dateTime: StateFlow<String> = _dateTime.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _dailyWage = MutableStateFlow("800")
    val dailyWage: StateFlow<String> = _dailyWage.asStateFlow()

    private val _workersNeeded = MutableStateFlow(1)
    val workersNeeded: StateFlow<Int> = _workersNeeded.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun setTitle(value: String) {
        _title.value = value
        if (_errorMessage.value != null) _errorMessage.value = null
    }

    fun setCategory(value: String) {
        _category.value = value
    }

    fun setLocation(value: String) {
        _location.value = value
        if (_errorMessage.value != null) _errorMessage.value = null
    }

    fun setDateTime(value: String) {
        _dateTime.value = value
        if (_errorMessage.value != null) _errorMessage.value = null
    }

    fun setDescription(value: String) {
        _description.value = value
    }

    fun setDailyWage(value: String) {
        if (value.all { it.isDigit() }) {
            _dailyWage.value = value
        }
    }

    fun setWorkersNeeded(value: Int) {
        if (value in 1..20) {
            _workersNeeded.value = value
        }
    }

    fun setInitialUserLocation(userLocation: String?) {
        if (!userLocation.isNullOrBlank() && _location.value == "Sector 14, Gurugram") {
            _location.value = userLocation
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun validate(): Boolean {
        if (_title.value.trim().isBlank()) {
            _errorMessage.value = "Please enter a job title"
            return false
        }
        if (_location.value.trim().isBlank()) {
            _errorMessage.value = "Please specify the job location"
            return false
        }
        if (_dateTime.value.trim().isBlank()) {
            _errorMessage.value = "Please enter date & time requirements"
            return false
        }
        _errorMessage.value = null
        return true
    }

    /**
     * Gathers all form fields, obtains current user's UID from FirebaseAuth,
     * and saves a new Job document in the Firestore "jobs" collection.
     */
    fun submitJob(
        fallbackCustomerId: String = "",
        onSuccess: (Job) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (!validate()) {
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _errorMessage.value = null
            _successMessage.value = null

            // 1. Fetch current user's UID from FirebaseAuth
            val currentUid = auth?.currentUser?.uid?.takeIf { it.isNotBlank() }
                ?: fallbackCustomerId.takeIf { it.isNotBlank() }
                ?: "anonymous_user"

            // 2. Build Job data class object
            val newJob = Job(
                id = "",
                title = _title.value.trim(),
                category = _category.value.trim(),
                location = _location.value.trim(),
                date = _dateTime.value.trim(),
                description = _description.value.trim(),
                customerId = currentUid,
                status = "open",
                timestamp = System.currentTimeMillis()
            )

            // 3. Save to Firestore jobs collection via repository
            val result = jobRepository.saveJob(newJob)
            _isSubmitting.value = false

            result.onSuccess { savedJob ->
                _successMessage.value = "Job posted successfully!"
                resetForm()
                onSuccess(savedJob)
            }.onFailure { exception ->
                val errorMsg = exception.localizedMessage ?: "Failed to post job. Please try again."
                _errorMessage.value = errorMsg
                onError(errorMsg)
            }
        }
    }

    fun resetForm() {
        _title.value = ""
        _category.value = "Mason"
        _location.value = "Sector 14, Gurugram"
        _dateTime.value = "Today, 9:00 AM"
        _description.value = ""
        _dailyWage.value = "800"
        _workersNeeded.value = 1
        _errorMessage.value = null
        _isSubmitting.value = false
    }

    fun setSubmitting(submitting: Boolean) {
        _isSubmitting.value = submitting
    }
}
