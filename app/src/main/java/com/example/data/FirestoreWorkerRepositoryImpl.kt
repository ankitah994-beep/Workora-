package com.example.data

import com.example.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

/**
 * FirestoreWorkerRepositoryImpl implements WorkerRepository using Firebase Firestore.
 */
class FirestoreWorkerRepositoryImpl(
    private val firestore: FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
) : WorkerRepository {

    private val workersCollection = "workers"

    private val fallbackWorkers = MutableStateFlow(
        listOf(
            User(
                id = 1,
                name = "Rajesh Sharma",
                trade = "Mason",
                dailyWage = 800,
                experienceYears = 8,
                rating = 4.8f,
                reviewsCount = 34,
                location = "Sector 14, Gurugram",
                distance = "1.2 km",
                phone = "+91 98765 43210",
                isAvailableToday = true,
                isVerified = true
            ),
            User(
                id = 2,
                name = "Sunil Kumar",
                trade = "Painter",
                dailyWage = 650,
                experienceYears = 5,
                rating = 4.6f,
                reviewsCount = 28,
                location = "Old Railway Road, Gurugram",
                distance = "2.4 km",
                phone = "+91 98123 45678",
                isAvailableToday = true,
                isVerified = true
            ),
            User(
                id = 3,
                name = "Amit Verma",
                trade = "Carpenter",
                dailyWage = 750,
                experienceYears = 6,
                rating = 4.9f,
                reviewsCount = 45,
                location = "DLF Phase 3, Gurugram",
                distance = "3.1 km",
                phone = "+91 98234 56789",
                isAvailableToday = true,
                isVerified = true
            ),
            User(
                id = 4,
                name = "Vikram Singh",
                trade = "Electrician",
                dailyWage = 900,
                experienceYears = 10,
                rating = 4.7f,
                reviewsCount = 52,
                location = "Sohna Road, Gurugram",
                distance = "4.0 km",
                phone = "+91 98345 67890",
                isAvailableToday = true,
                isVerified = true
            ),
            User(
                id = 5,
                name = "Manoj Yadav",
                trade = "Plumber",
                dailyWage = 700,
                experienceYears = 4,
                rating = 4.5f,
                reviewsCount = 19,
                location = "Sector 31, Gurugram",
                distance = "2.8 km",
                phone = "+91 98456 78901",
                isAvailableToday = true,
                isVerified = true
            ),
            User(
                id = 6,
                name = "Sonu Prajapati",
                trade = "Welder",
                dailyWage = 850,
                experienceYears = 7,
                rating = 4.7f,
                reviewsCount = 23,
                location = "Palam Vihar, Gurugram",
                distance = "3.5 km",
                phone = "+91 98567 89012",
                isAvailableToday = true,
                isVerified = true
            )
        )
    )

    override fun getAvailableWorkers(): Flow<List<User>> {
        val db = firestore ?: return fallbackWorkers

        return callbackFlow {
            val query = db.collection(workersCollection)
                .whereEqualTo("isAvailableToday", true)

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(fallbackWorkers.value)
                    return@addSnapshotListener
                }

                val workerList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toWorkerProfile()
                } ?: emptyList()

                if (workerList.isEmpty() && fallbackWorkers.value.isNotEmpty()) {
                    trySend(fallbackWorkers.value)
                } else {
                    trySend(workerList)
                }
            }

            awaitClose {
                listener.remove()
            }
        }
    }

    /**
     * Helper method to add or update a worker in Firestore.
     */
    suspend fun saveWorker(worker: User): Boolean {
        val db = firestore ?: return false
        return try {
            val map = hashMapOf<String, Any>(
                "id" to worker.id,
                "name" to worker.name,
                "trade" to worker.trade,
                "dailyWage" to worker.dailyWage,
                "experienceYears" to worker.experienceYears,
                "rating" to worker.rating.toDouble(),
                "reviewsCount" to worker.reviewsCount,
                "location" to worker.location,
                "distance" to worker.distance,
                "phone" to worker.phone,
                "isAvailableToday" to worker.isAvailableToday,
                "isVerified" to worker.isVerified,
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection(workersCollection)
                .document(worker.id.toString())
                .set(map, SetOptions.merge())
                .awaitTask()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Seeds initial default workers if the collection is empty.
     */
    suspend fun seedInitialWorkers(workers: List<User>): Boolean {
        val db = firestore ?: return false
        return try {
            val existing = db.collection(workersCollection).limit(1).get().awaitTask()
            if (existing.isEmpty) {
                for (worker in workers) {
                    saveWorker(worker)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun DocumentSnapshot.toWorkerProfile(): User? {
        val data = this.data ?: return null
        return try {
            User(
                id = (data["id"] as? Number)?.toLong() ?: id.toLongOrNull() ?: 0L,
                name = data["name"] as? String ?: "Worker",
                trade = data["trade"] as? String ?: "General Labour",
                dailyWage = (data["dailyWage"] as? Number)?.toInt() ?: 700,
                experienceYears = (data["experienceYears"] as? Number)?.toInt() ?: 3,
                rating = (data["rating"] as? Number)?.toFloat() ?: 4.5f,
                reviewsCount = (data["reviewsCount"] as? Number)?.toInt() ?: 10,
                location = data["location"] as? String ?: "Gurugram",
                distance = data["distance"] as? String ?: "2.0 km",
                phone = data["phone"] as? String ?: "+91 98000 00000",
                isAvailableToday = data["isAvailableToday"] as? Boolean ?: true,
                isVerified = data["isVerified"] as? Boolean ?: true
            )
        } catch (e: Exception) {
            null
        }
    }
}
