package com.example.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

