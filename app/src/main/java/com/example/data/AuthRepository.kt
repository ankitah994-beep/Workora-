package com.example.data

import com.example.model.UserAccount
import com.example.model.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AuthRepository {
    val currentUser: StateFlow<UserAccount?>

    suspend fun loginWithPhone(
        phoneNumber: String,
        role: UserRole = UserRole.CUSTOMER
    ): Result<UserAccount>

    suspend fun loginWithEmail(
        email: String,
        password: String,
        role: UserRole = UserRole.CUSTOMER
    ): Result<UserAccount>

    suspend fun register(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        location: String,
        role: UserRole
    ): Result<UserAccount>

    suspend fun logout()

    suspend fun updateUser(user: UserAccount): Boolean = true

    fun getCurrentUser(): UserAccount?
}

/**
 * Simple MockAuthRepository implementation so the app compiles and runs.
 * Can be swapped with FirebaseAuthRepository later.
 */
class MockAuthRepository : AuthRepository {
    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    override val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    override suspend fun loginWithPhone(
        phoneNumber: String,
        role: UserRole
    ): Result<UserAccount> {
        delay(250) // simulate auth network latency
        val cleanPhone = phoneNumber.trim()
        val isCustomer = role == UserRole.CUSTOMER

        val user = UserAccount(
            id = if (isCustomer) 101L else 201L,
            fullName = if (isCustomer) "Ramesh Verma" else "Sunil Kumar",
            mobileNumber = cleanPhone.ifBlank { if (isCustomer) "+91 98765 43210" else "+91 98123 45678" },
            email = if (isCustomer) "ramesh.verma@workora.com" else "sunil.mason@workora.com",
            password = "",
            location = if (isCustomer) "Sector 14, Gurugram" else "Delhi Chowk, Delhi",
            role = role.name,
            isLoggedIn = true,
            createdAt = System.currentTimeMillis()
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun loginWithEmail(
        email: String,
        password: String,
        role: UserRole
    ): Result<UserAccount> {
        delay(250)
        val cleanEmail = email.trim()
        val isCustomer = role == UserRole.CUSTOMER

        val user = UserAccount(
            id = if (isCustomer) 101L else 201L,
            fullName = if (isCustomer) "Ramesh Verma" else "Sunil Kumar",
            mobileNumber = if (isCustomer) "+91 98765 43210" else "+91 98123 45678",
            email = cleanEmail.ifBlank { if (isCustomer) "customer@workora.com" else "worker@workora.com" },
            password = password,
            location = if (isCustomer) "Sector 14, Gurugram" else "Delhi Chowk, Delhi",
            role = role.name,
            isLoggedIn = true,
            createdAt = System.currentTimeMillis()
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun register(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        location: String,
        role: UserRole
    ): Result<UserAccount> {
        delay(250)
        val user = UserAccount(
            id = System.currentTimeMillis(),
            fullName = fullName.trim().ifBlank { "Workora User" },
            mobileNumber = mobileNumber.trim().ifBlank { "+91 98765 43210" },
            email = email.trim().ifBlank { "user@workora.com" },
            password = password,
            location = location.trim().ifBlank { "Gurugram" },
            role = role.name,
            isLoggedIn = true,
            createdAt = System.currentTimeMillis()
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun updateUser(user: UserAccount): Boolean {
        _currentUser.value = user
        return true
    }

    override fun getCurrentUser(): UserAccount? = _currentUser.value
}
