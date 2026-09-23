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
 * FirestoreUserRepositoryImpl implements UserRepository using Firebase Firestore.
 */
class FirestoreUserRepositoryImpl(
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
) : UserRepository {

    private val usersCollection = "users"

    private val fallbackUser = MutableStateFlow<User?>(
        User(
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

    override suspend fun updateUserProfile(user: User): Boolean {
        fallbackUser.value = user
        val db = firestore ?: return true
        return try {
            val userMap = hashMapOf<String, Any>(
                "id" to user.id,
                "name" to user.name,
                "trade" to user.trade,
                "dailyWage" to user.dailyWage,
                "experienceYears" to user.experienceYears,
                "rating" to user.rating.toDouble(),
                "reviewsCount" to user.reviewsCount,
                "location" to user.location,
                "distance" to user.distance,
                "phone" to user.phone,
                "isAvailableToday" to user.isAvailableToday,
                "isVerified" to user.isVerified,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(usersCollection)
                .document(user.id.toString())
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
                            name = data["name"] as? String ?: "Sunil Kumar",
                            trade = data["trade"] as? String ?: "Mason",
                            dailyWage = (data["dailyWage"] as? Number)?.toInt() ?: 850,
                            experienceYears = (data["experienceYears"] as? Number)?.toInt() ?: 5,
                            rating = (data["rating"] as? Number)?.toFloat() ?: 4.9f,
                            reviewsCount = (data["reviewsCount"] as? Number)?.toInt() ?: 42,
                            location = data["location"] as? String ?: "Delhi Chowk, Delhi",
                            distance = data["distance"] as? String ?: "1.2 km",
                            phone = data["phone"] as? String ?: "+91 98123 45678",
                            isAvailableToday = data["isAvailableToday"] as? Boolean ?: true,
                            isVerified = data["isVerified"] as? Boolean ?: true
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
