package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    LABOUR
}

enum class AuthMode {
    LOGIN,
    REGISTER
}

enum class ScreenState {
    SPLASH,
    ACCOUNT_SELECTION,
    AUTH,
    CUSTOMER_HOME,
    LABOUR_HOME,
    PROFILE
}

@Entity(tableName = "users")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val mobileNumber: String,
    val email: String,
    val password: String,
    val location: String, // Area / Village / City
    val role: String, // "CUSTOMER" or "LABOUR"
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class JobPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val description: String,
    val dailyRate: Int,
    val location: String,
    val workersNeeded: Int = 1,
    val urgency: String = "Today",
    val dateTime: String = "Today, 9:00 AM",
    val customerName: String = "Ramesh Verma",
    val customerPhone: String = "+91 98765 43210",
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED, COMPLETED
    val applicantsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "workers")
data class WorkerProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val trade: String,
    val dailyWage: Int,
    val experienceYears: Int,
    val rating: Float,
    val reviewsCount: Int,
    val location: String,
    val distance: String,
    val phone: String,
    val isAvailableToday: Boolean = true,
    val isVerified: Boolean = true
)

@Entity(tableName = "applications")
data class JobApplication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val workerId: Long = 1,
    val workerName: String,
    val jobTitle: String,
    val category: String,
    val dailyRate: Int,
    val location: String = "Delhi Chowk",
    val dateTime: String = "Today, 9:00 AM",
    val status: String = "ACCEPTED", // PENDING, ACCEPTED, REJECTED, COMPLETED
    val timestamp: Long = System.currentTimeMillis()
)
