package com.example.data

import com.example.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UserRepository interface defining user profile operations (basic auth and customer accounts).
 */
interface UserRepository {
    suspend fun updateUserProfile(user: User): Boolean
    fun getUserProfile(userId: String = "1"): Flow<User?>
}

/**
 * MockUserRepository implementation to ensure it compiles and supports local state.
 */
class MockUserRepository(
    initialUser: User = User(
        id = 1L,
        uid = "1",
        fullName = "Ramesh Verma",
        mobileNumber = "+91 98765 43210",
        email = "customer@workora.com",
        password = "",
        location = "Sector 14, Gurugram",
        role = "CUSTOMER",
        isLoggedIn = true,
        createdAt = System.currentTimeMillis()
    )
) : UserRepository {

    private val _userProfile = MutableStateFlow<User?>(initialUser)

    override suspend fun updateUserProfile(user: User): Boolean {
        _userProfile.value = user
        return true
    }

    override fun getUserProfile(userId: String): Flow<User?> = _userProfile.asStateFlow()
}
