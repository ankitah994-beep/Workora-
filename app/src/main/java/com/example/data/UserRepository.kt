package com.example.data

import com.example.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UserRepository interface defining user and worker profile operations.
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
) : UserRepository {

    private val _userProfile = MutableStateFlow<User?>(initialUser)

    override suspend fun updateUserProfile(user: User): Boolean {
        _userProfile.value = user
        return true
    }

    override fun getUserProfile(userId: String): Flow<User?> = _userProfile.asStateFlow()
}
