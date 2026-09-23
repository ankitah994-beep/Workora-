package com.example.data

import com.example.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * FirestoreUserRepositoryImpl implements UserRepository using Firebase Firestore for basic User profiles.
 */
class FirestoreUserRepositoryImpl(
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
) : UserRepository {

    private val usersCollection = "users"

    private val fallbackUser = MutableStateFlow<User?>(
        User(
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
    )

    override suspend fun updateUserProfile(user: User): Boolean {
        fallbackUser.value = user
        val db = firestore ?: return true
        return try {
            val userMap = hashMapOf<String, Any>(
                "id" to user.id,
                "uid" to user.uid,
                "fullName" to user.fullName,
                "mobileNumber" to user.mobileNumber,
                "email" to user.email,
                "location" to user.location,
                "role" to user.role,
                "isLoggedIn" to user.isLoggedIn,
                "updatedAt" to System.currentTimeMillis()
            )

            val docId = if (user.uid.isNotBlank()) user.uid else user.id.toString()
            db.collection(usersCollection)
                .document(docId)
                .set(userMap, SetOptions.merge())
                .awaitTask()
            true
        } catch (e: Exception) {
            true
        }
    }

    override fun getUserProfile(userId: String): Flow<User?> {
        val db = firestore ?: return fallbackUser

        return callbackFlow {
            val docRef = db.collection(usersCollection).document(userId)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(fallbackUser.value)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    if (data != null) {
                        val parsedUser = User(
                            id = (data["id"] as? Number)?.toLong() ?: userId.toLongOrNull() ?: 1L,
                            uid = data["uid"] as? String ?: userId,
                            fullName = data["fullName"] as? String ?: data["name"] as? String ?: "Customer",
                            mobileNumber = data["mobileNumber"] as? String ?: data["phone"] as? String ?: "+91 98765 43210",
                            email = data["email"] as? String ?: "customer@workora.com",
                            password = data["password"] as? String ?: "",
                            location = data["location"] as? String ?: "Gurugram",
                            role = data["role"] as? String ?: "CUSTOMER",
                            isLoggedIn = data["isLoggedIn"] as? Boolean ?: true,
                            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                        fallbackUser.value = parsedUser
                        trySend(parsedUser)
                        return@addSnapshotListener
                    }
                }
                trySend(fallbackUser.value)
            }

            awaitClose {
                listener.remove()
            }
        }
    }
}
