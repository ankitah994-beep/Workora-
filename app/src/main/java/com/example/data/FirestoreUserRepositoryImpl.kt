package com.example.data

import com.example.model.User
import com.example.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * FirestoreUserRepositoryImpl implements UserRepository using Firebase Firestore.
 */
class FirestoreUserRepositoryImpl(
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
) : UserRepository {

    private val usersCollection = "users"

    private val fallbackUser = MutableStateFlow<User?>(
        User(
            id = "1",
            name = "Ramesh Verma",
            email = "customer@workora.com",
            phoneNumber = "+91 98765 43210",
            role = UserRole.CUSTOMER,
            profileImageUrl = "",
            createdAt = System.currentTimeMillis()
        )
    )

    override suspend fun updateUserProfile(user: User): Boolean {
        fallbackUser.value = user
        val db = firestore ?: return true
        return try {
            val userMap = hashMapOf<String, Any>(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "phoneNumber" to user.phoneNumber,
                "role" to user.role.name,
                "profileImageUrl" to user.profileImageUrl,
                "createdAt" to user.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            val docId = user.id.ifBlank { "1" }
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
                        val roleStr = data["role"] as? String ?: "CUSTOMER"
                        val parsedRole = try {
                            UserRole.valueOf(roleStr.uppercase())
                        } catch (e: Exception) {
                            UserRole.CUSTOMER
                        }

                        val parsedUser = User(
                            id = data["id"] as? String ?: userId,
                            name = data["name"] as? String ?: "Customer",
                            email = data["email"] as? String ?: "",
                            phoneNumber = data["phoneNumber"] as? String ?: "",
                            role = parsedRole,
                            profileImageUrl = data["profileImageUrl"] as? String ?: "",
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
