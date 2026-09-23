package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.FirebaseAuthRepositoryImpl
import com.example.data.FirestoreUserRepositoryImpl
import com.example.data.UserRepository
import com.example.model.UserAccount
import com.example.model.WorkerProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ProfileViewModel managing customer and worker profile details and persistence.
 * Distinguishes between UserAccount (for auth/customer) and WorkerProfile (for labour details).
 */
class ProfileViewModel(
    private val userRepository: UserRepository = FirestoreUserRepositoryImpl(),
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl()
) : ViewModel() {

    private val _workerProfile = MutableStateFlow(
        WorkerProfile(
            id = 1L,
            name = "Sunil Kumar",
            trade = "Mason",
            dailyWage = 850,
            experienceYears = 5,
            rating = 4.9f,
            reviewsCount = 42,
            location = "Delhi Chowk, Delhi",
            distance = "1.2 km",
            phone = "+91 98123 45678",
            isAvailableToday = true,
            isVerified = true
        )
    )

    // Worker profile flow for labour details
    val workerProfile: StateFlow<WorkerProfile?> = _workerProfile.asStateFlow()

    // Backwards-compatible alias for UI screens referencing userProfile
    val userProfile: StateFlow<WorkerProfile?> = workerProfile

    // Current logged-in user account flow (customers or auth)
    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _updateSuccess = MutableStateFlow<Boolean?>(null)
    val updateSuccess: StateFlow<Boolean?> = _updateSuccess.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    /**
     * Updates the WorkerProfile directly.
     */
    fun updateWorkerProfile(worker: WorkerProfile, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                _workerProfile.value = worker
                _updateSuccess.value = true
                _toastMessage.value = "Profile updated successfully!"

                // Also synchronize with AuthRepository's currentUser if present
                val existingAccount = authRepository.currentUser.value
                if (existingAccount != null) {
                    val updatedAccount = existingAccount.copy(
                        fullName = worker.name,
                        mobileNumber = worker.phone,
                        location = worker.location
                    )
                    authRepository.updateUser(updatedAccount)
                }
                onComplete?.invoke(true)
            } catch (e: Exception) {
                _updateSuccess.value = false
                _toastMessage.value = "Error updating profile: ${e.message}"
                onComplete?.invoke(false)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    /**
     * Backwards-compatible alias for updating worker profile.
     */
    fun updateUserProfile(worker: WorkerProfile, onComplete: ((Boolean) -> Unit)? = null) {
        updateWorkerProfile(worker, onComplete)
    }

    /**
     * Updates worker profile details: name, phone, area, skills/services, wage, availability.
     */
    fun updateWorkerProfile(
        name: String,
        phone: String,
        area: String,
        skills: String = "Mason",
        services: String = skills,
        wage: Int = 850,
        availability: Boolean = true,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val current = workerProfile.value
        val effectiveSkills = skills.trim().ifBlank { services.trim().ifBlank { current?.trade ?: "Mason" } }
        val updatedWorker = WorkerProfile(
            id = current?.id ?: 1L,
            name = name.trim().ifBlank { current?.name ?: "Worker" },
            trade = effectiveSkills,
            dailyWage = if (wage > 0) wage else (current?.dailyWage ?: 850),
            experienceYears = current?.experienceYears ?: 5,
            rating = current?.rating ?: 4.9f,
            reviewsCount = current?.reviewsCount ?: 42,
            location = area.trim().ifBlank { current?.location ?: "Delhi" },
            distance = current?.distance ?: "1.2 km",
            phone = phone.trim().ifBlank { current?.phone ?: "+91 98123 45678" },
            isAvailableToday = availability,
            isVerified = current?.isVerified ?: true
        )
        updateWorkerProfile(updatedWorker, onComplete)
    }

    /**
     * Updates customer profile details: name, phone, area.
     */
    fun updateCustomerProfile(
        name: String,
        phone: String,
        area: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val existing = authRepository.currentUser.value
                val updatedAccount = existing?.copy(
                    fullName = name.trim().ifBlank { existing.fullName },
                    mobileNumber = phone.trim().ifBlank { existing.mobileNumber },
                    location = area.trim().ifBlank { existing.location }
                ) ?: UserAccount(
                    id = 101L,
                    fullName = name.trim().ifBlank { "Customer" },
                    mobileNumber = phone.trim().ifBlank { "+91 98765 43210" },
                    email = "customer@workora.com",
                    password = "",
                    location = area.trim().ifBlank { "Gurugram" },
                    role = "CUSTOMER",
                    isLoggedIn = true
                )

                val success = authRepository.updateUser(updatedAccount)
                _updateSuccess.value = success
                _toastMessage.value = if (success) "Profile updated successfully!" else "Failed to update profile"
                onComplete?.invoke(success)
            } catch (e: Exception) {
                _updateSuccess.value = false
                _toastMessage.value = "Error updating profile: ${e.message}"
                onComplete?.invoke(false)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    /**
     * General convenience update function for any user role.
     */
    fun updateProfile(
        name: String,
        phone: String,
        area: String,
        skills: String = "Mason",
        services: String = skills,
        wage: Int = 850,
        isAvailable: Boolean = true,
        isCustomer: Boolean = false,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        if (isCustomer) {
            updateCustomerProfile(name, phone, area, onComplete)
        } else {
            updateWorkerProfile(name, phone, area, skills, services, wage, isAvailable, onComplete)
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
