package com.example.data

import com.example.model.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * ReviewRepository interface for submitting and fetching reviews.
 */
interface ReviewRepository {
    suspend fun submitReview(review: Review): Boolean
    fun getWorkerReviews(workerId: String): Flow<List<Review>>
}

/**
 * MockReviewRepository implementation that stores reviews in a StateFlow
 * with mock data for testing and compiling.
 */
class MockReviewRepository : ReviewRepository {

    private val _reviews = MutableStateFlow<List<Review>>(
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
        val currentList = _reviews.value.toMutableList()
        currentList.add(0, review)
        _reviews.value = currentList
        return true
    }

    override fun getWorkerReviews(workerId: String): Flow<List<Review>> {
        return _reviews.asStateFlow().map { list ->
            if (workerId.isBlank()) list
            else list.filter { it.workerId.isBlank() || it.workerId == workerId }
        }
    }
}
