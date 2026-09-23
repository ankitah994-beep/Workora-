package com.example.data

import com.example.model.Review
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

/**
 * FirestoreReviewRepositoryImpl implements ReviewRepository using Firebase Firestore.
 * Provides fallback in-memory state if Firebase is offline or uninitialized.
 */
class FirestoreReviewRepositoryImpl(
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
) : ReviewRepository {

    private val reviewsCollection = "reviews"

    // Default fallback seed data
    private val fallbackReviews = MutableStateFlow(
        listOf(
            Review(
                id = "1",
                jobId = "3",
                customerId = "customer_1",
                workerId = "3",
                rating = 5.0f,
                comment = "Excellent woodwork! Finished door installation on time and very professional.",
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            Review(
                id = "2",
                jobId = "2",
                customerId = "customer_1",
                workerId = "2",
                rating = 4.8f,
                comment = "Great painting job, clean work and neat edges.",
                createdAt = System.currentTimeMillis() - 172800000L
            ),
            Review(
                id = "3",
                jobId = "1",
                customerId = "customer_1",
                workerId = "1",
                rating = 4.9f,
                comment = "Superb masonry repair. Wall is solid and clean finish.",
                createdAt = System.currentTimeMillis() - 259200000L
            )
        )
    )

    override suspend fun submitReview(review: Review): Boolean {
        // Always store in fallback state
        val currentList = fallbackReviews.value.toMutableList()
        currentList.removeAll { it.id == review.id }
        currentList.add(0, review)
        fallbackReviews.value = currentList

        val db = firestore ?: return true

        return try {
            val map = hashMapOf<String, Any>(
                "id" to review.id,
                "jobId" to review.jobId,
                "customerId" to review.customerId,
                "workerId" to review.workerId,
                "rating" to review.rating.toDouble(),
                "comment" to review.comment,
                "createdAt" to review.createdAt
            )
            db.collection(reviewsCollection)
                .document(review.id)
                .set(map, SetOptions.merge())
                .awaitTask()
            true
        } catch (e: Exception) {
            true
        }
    }

    override fun getWorkerReviews(workerId: String): Flow<List<Review>> {
        val db = firestore
        if (db == null) {
            return fallbackReviews.asStateFlow().map { list ->
                if (workerId.isBlank()) list
                else list.filter { it.workerId.isBlank() || it.workerId == workerId }
            }
        }

        return callbackFlow {
            val query = if (workerId.isNotBlank()) {
                db.collection(reviewsCollection).whereEqualTo("workerId", workerId)
            } else {
                db.collection(reviewsCollection)
            }

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    val fallback = fallbackReviews.value.filter { workerId.isBlank() || it.workerId.isBlank() || it.workerId == workerId }
                    trySend(fallback)
                    return@addSnapshotListener
                }

                val reviews = snapshot?.documents?.mapNotNull { it.toReview() } ?: emptyList()
                if (reviews.isEmpty() && fallbackReviews.value.isNotEmpty()) {
                    val fallback = fallbackReviews.value.filter { workerId.isBlank() || it.workerId.isBlank() || it.workerId == workerId }
                    trySend(fallback)
                } else {
                    trySend(reviews)
                }
            }

            awaitClose {
                listener.remove()
            }
        }
    }

    private fun DocumentSnapshot.toReview(): Review? {
        val data = this.data ?: return null
        return try {
            Review(
                id = data["id"] as? String ?: id,
                jobId = data["jobId"] as? String ?: "",
                customerId = data["customerId"] as? String ?: "",
                workerId = data["workerId"] as? String ?: "",
                rating = (data["rating"] as? Number)?.toFloat() ?: 5.0f,
                comment = data["comment"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
