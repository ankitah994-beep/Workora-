package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.FirebaseAuthRepositoryImpl
import com.example.data.FirestoreUserRepositoryImpl
import com.example.data.UserRepository
import com.example.model.User
import com.example.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ProfileViewModel managing customer and worker profile details and persistence.
 * Takes UserRepository (and optional AuthRepository) as dependencies.
 */
class ProfileViewModel(
    private val userRepository: UserRepository = FirestoreUserRepositoryImpl(),
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl()
) : ViewModel() {

    // User profile flow for worker
    val userProfile: StateFlow<User?> = userRepository.getUserProfile("1")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = User(
                id = 1,
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

    // Current logged-in user account flow (customers or workers)
    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _updateSuccess = MutableStateFlow<Boolean?>(null)
    val updateSuccess: StateFlow<Boolean?> = _updateSuccess.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    /**
     * Updates the full User object directly.
     */
    fun updateUserProfile(user: User, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isUpdating.value = true
            try {
                val success = userRepository.updateUserProfile(user)
                _updateSuccess.value = success
                if (success) {
                    _toastMessage.value = "Profile updated successfully!"
                    // Also synchronize with AuthRepository's currentUser if present
                    val existingAccount = authRepository.currentUser.value
                    if (existingAccount != null) {
                        val updatedAccount = existingAccount.copy(
                            fullName = user.name,
                            mobileNumber = user.phone,
                            location = user.location
                        )
                        authRepository.updateUser(updatedAccount)
                    }
                } else {
                    _toastMessage.value = "Failed to update profile"
                }
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
        val current = userProfile.value
        val effectiveSkills = skills.trim().ifBlank { services.trim().ifBlank { current?.trade ?: "Mason" } }
        val updatedUser = User(
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
        updateUserProfile(updatedUser, onComplete)
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
