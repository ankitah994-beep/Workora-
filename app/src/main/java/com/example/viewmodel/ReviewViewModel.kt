package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreReviewRepositoryImpl
import com.example.data.ReviewRepository
import com.example.model.Review
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ReviewViewModel manages submitting reviews and observing reviews for workers.
 */
class ReviewViewModel(
    private val reviewRepository: ReviewRepository = FirestoreReviewRepositoryImpl()
) : ViewModel() {

    private val _workerId = MutableStateFlow("1")
    val workerId: StateFlow<String> = _workerId.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _reviewSubmitted = MutableStateFlow<Boolean?>(null)
    val reviewSubmitted: StateFlow<Boolean?> = _reviewSubmitted.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val workerReviews: StateFlow<List<Review>> = _workerId
        .flatMapLatest { id -> reviewRepository.getWorkerReviews(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun fetchWorkerReviews(workerId: String) {
        _workerId.value = workerId
    }

    fun submitReview(
        review: Review,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val success = reviewRepository.submitReview(review)
            _isSubmitting.value = false
            _reviewSubmitted.value = success
            onComplete?.invoke(success)
        }
    }

    fun submitReview(
        jobId: String,
        customerId: String,
        workerId: String,
        rating: Float,
        comment: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val review = Review(
            id = System.currentTimeMillis().toString(),
            jobId = jobId,
            customerId = customerId,
            workerId = workerId,
            rating = rating,
            comment = comment,
            createdAt = System.currentTimeMillis()
        )
        submitReview(review, onComplete)
    }

    fun resetSubmissionStatus() {
        _reviewSubmitted.value = null
    }
}
