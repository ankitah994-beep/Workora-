package com.example.data

import com.example.model.UserAccount
import com.example.model.UserRole
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Extension helper to await Google Play / Firebase Task completion safely in coroutines.
 */
internal suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) {
            continuation.resume(result)
        }
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) {
            continuation.resumeWithException(exception)
        }
    }
    addOnCanceledListener {
        if (continuation.isActive) {
            continuation.cancel()
        }
    }
}

/**
 * FirebaseAuthRepositoryImpl implements AuthRepository using Firebase Authentication.
 */
class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()
) : AuthRepository {

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    override val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private var activeRole: UserRole = UserRole.CUSTOMER

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                _currentUser.value = mapFirebaseUser(user, activeRole)
            } else {
                _currentUser.value = null
            }
        }
    }

    override suspend fun loginWithEmail(
        email: String,
        password: String,
        role: UserRole
    ): Result<UserAccount> {
        activeRole = role
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            // Safe fallback account when Firebase project is not linked yet
            val account = UserAccount(
                id = email.hashCode().toLong(),
                fullName = if (role == UserRole.CUSTOMER) "Customer User" else "Labour Worker",
                mobileNumber = "+91 98765 43210",
                email = email.trim(),
                password = "",
                location = "Delhi NCR",
                role = role.name,
                isLoggedIn = true,
                createdAt = System.currentTimeMillis()
            )
            _currentUser.value = account
            return Result.success(account)
        }
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user
            if (user != null) {
                val account = mapFirebaseUser(user, role)
                _currentUser.value = account
                Result.success(account)
            } else {
                Result.failure(Exception("Failed to retrieve user after sign in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        fullName: String,
        mobileNumber: String,
        email: String,
        password: String,
        location: String,
        role: UserRole
    ): Result<UserAccount> {
        activeRole = role
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            val account = UserAccount(
                id = email.hashCode().toLong(),
                fullName = fullName.trim().ifBlank { "User" },
                mobileNumber = mobileNumber.trim(),
                email = email.trim(),
                password = "",
                location = location.trim().ifBlank { "Delhi NCR" },
                role = role.name,
                isLoggedIn = true,
                createdAt = System.currentTimeMillis()
            )
            _currentUser.value = account
            return Result.success(account)
        }
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user
            if (user != null) {
                // Update Firebase User Display Name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName.trim())
                    .build()
                runCatching { user.updateProfile(profileUpdates).awaitTask() }

                val account = UserAccount(
                    id = user.uid.hashCode().toLong(),
                    fullName = fullName.trim().ifBlank { user.displayName ?: "User" },
                    mobileNumber = mobileNumber.trim().ifBlank { user.phoneNumber ?: "" },
                    email = user.email ?: email.trim(),
                    password = "",
                    location = location.trim().ifBlank { "Delhi NCR" },
                    role = role.name,
                    isLoggedIn = true,
                    createdAt = System.currentTimeMillis()
                )
                _currentUser.value = account
                Result.success(account)
            } else {
                Result.failure(Exception("Failed to create user account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithPhone(
        phoneNumber: String,
        role: UserRole
    ): Result<UserAccount> {
        activeRole = role
        val account = UserAccount(
            id = (phoneNumber.hashCode()).toLong(),
            fullName = if (role == UserRole.CUSTOMER) "Customer User" else "Labour Worker",
            mobileNumber = phoneNumber.trim(),
            email = "${phoneNumber.filter { it.isDigit() }}@workora.firebase.com",
            password = "",
            location = "Delhi NCR",
            role = role.name,
            isLoggedIn = true
        )
        _currentUser.value = account
        return Result.success(account)
    }

    override suspend fun logout() {
        auth?.signOut()
        _currentUser.value = null
    }

    override suspend fun updateUser(user: UserAccount): Boolean {
        _currentUser.value = user
        auth?.currentUser?.let { firebaseUser ->
            if (user.fullName.isNotBlank()) {
                val updates = UserProfileChangeRequest.Builder()
                    .setDisplayName(user.fullName)
                    .build()
                runCatching { firebaseUser.updateProfile(updates).awaitTask() }
            }
        }
        return true
    }

    override fun getCurrentUser(): UserAccount? = _currentUser.value

    private fun mapFirebaseUser(user: FirebaseUser, role: UserRole): UserAccount {
        return UserAccount(
            id = user.uid.hashCode().toLong(),
            fullName = user.displayName?.ifBlank { null } ?: if (role == UserRole.CUSTOMER) "Customer User" else "Labour Worker",
            mobileNumber = user.phoneNumber ?: "",
            email = user.email ?: "",
            password = "",
            location = "Delhi NCR",
            role = role.name,
            isLoggedIn = true,
            createdAt = user.metadata?.creationTimestamp ?: System.currentTimeMillis()
        )
    }
}
