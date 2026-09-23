package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AuthRepository
import com.example.data.FirebaseAuthRepositoryImpl
import com.example.model.UserAccount
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepositoryImpl()
) : ViewModel() {

    // Observed current user state from the AuthRepository
    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    /**
     * Handles phone number login via the AuthRepository.
     */
    fun loginWithPhone(
        phoneNumber: String,
        role: UserRole = UserRole.CUSTOMER,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val cleanPhone = phoneNumber.trim()
        if (cleanPhone.isBlank()) {
            val error = "Please enter your phone number"
            _authError.value = error
            onResult(false, error)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val result = authRepository.loginWithPhone(cleanPhone, role)
                result.fold(
                    onSuccess = { user ->
                        _isLoading.value = false
                        _toastMessage.value = "Logged in successfully as ${user.fullName}"
                        onResult(true, "Login successful")
                    },
                    onFailure = { error ->
                        _isLoading.value = false
                        val msg = error.localizedMessage ?: "Phone login failed"
                        _authError.value = msg
                        onResult(false, msg)
                    }
                )
            } catch (e: Exception) {
                _isLoading.value = false
                val msg = e.localizedMessage ?: "An unexpected error occurred"
                _authError.value = msg
                onResult(false, msg)
            }
        }
    }

    /**
     * Handles general login (supporting both phone number and email inputs).
     */
    fun login(
        emailOrPhone: String,
        pass: String = "",
        role: UserRole = UserRole.CUSTOMER,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val input = emailOrPhone.trim()
        if (input.isBlank()) {
            val error = "Please enter your phone number or email"
            _authError.value = error
            onResult(false, error)
            return
        }

        val isPhone = !input.contains("@") && input.any { it.isDigit() }
        if (isPhone) {
            loginWithPhone(input, role, onResult)
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                _authError.value = null
                try {
                    val result = authRepository.loginWithEmail(input, pass, role)
                    result.fold(
                        onSuccess = { user ->
                            _isLoading.value = false
                            _toastMessage.value = "Logged in successfully as ${user.fullName}"
                            onResult(true, "Login successful")
                        },
                        onFailure = { error ->
                            _isLoading.value = false
                            val msg = error.localizedMessage ?: "Invalid credentials"
                            _authError.value = msg
                            onResult(false, msg)
                        }
                    )
                } catch (e: Exception) {
                    _isLoading.value = false
                    val msg = e.localizedMessage ?: "An error occurred during login"
                    _authError.value = msg
                    onResult(false, msg)
                }
            }
        }
    }

    /**
     * Handles new user registration.
     */
    fun register(
        fullName: String,
        mobileNumber: String,
        email: String,
        pass: String,
        confirmPass: String,
        location: String,
        role: UserRole,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (fullName.isBlank()) {
            _authError.value = "Please enter your full name"
            onResult(false, "Please enter your full name")
            return
        }
        if (mobileNumber.isBlank() && email.isBlank()) {
            _authError.value = "Please enter a mobile number or email"
            onResult(false, "Please enter a mobile number or email")
            return
        }
        if (pass.length < 6) {
            _authError.value = "Password must be at least 6 characters"
            onResult(false, "Password must be at least 6 characters")
            return
        }
        if (pass != confirmPass) {
            _authError.value = "Passwords do not match"
            onResult(false, "Passwords do not match")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            try {
                val result = authRepository.register(
                    fullName = fullName.trim(),
                    mobileNumber = mobileNumber.trim(),
                    email = email.trim(),
                    password = pass,
                    location = location.trim(),
                    role = role
                )
                result.fold(
                    onSuccess = { user ->
                        _isLoading.value = false
                        _toastMessage.value = "Account created for ${user.fullName}"
                        onResult(true, "Registration successful")
                    },
                    onFailure = { error ->
                        _isLoading.value = false
                        val msg = error.localizedMessage ?: "Registration failed"
                        _authError.value = msg
                        onResult(false, msg)
                    }
                )
            } catch (e: Exception) {
                _isLoading.value = false
                val msg = e.localizedMessage ?: "An error occurred during registration"
                _authError.value = msg
                onResult(false, msg)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _toastMessage.value = "Logged out successfully"
        }
    }

    fun clearError() {
        _authError.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    companion object {
        fun provideFactory(
            repository: AuthRepository = FirebaseAuthRepositoryImpl()
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(repository) as T
            }
        }
    }
}
