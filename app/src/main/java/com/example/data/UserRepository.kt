package com.example.data

import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UserRepository interface defining user profile operations using the User model.
 */
interface UserRepository {
    suspend fun updateUserProfile(user: User): Boolean
    fun getUserProfile(userId: String = "1"): Flow<User?>
}

/**
 * MockUserRepository implementation providing in-memory state and dummy user.
 */
class MockUserRepository(
    initialUser: User = User(
        id = "1",
        name = "Ramesh Verma",
        email = "customer@workora.com",
        phoneNumber = "+91 98765 43210",
        role = UserRole.CUSTOMER,
        profileImageUrl = "",
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
