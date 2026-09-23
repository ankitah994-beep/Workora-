package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreJobRepositoryImpl
import com.example.data.JobRepository
import com.example.model.JobRequest
import com.example.model.JobStatus
import com.example.model.WorkerProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * JobViewModel managing job requests, customer bookings, and worker job requests.
 * Takes JobRepository as a constructor parameter.
 */
class JobViewModel(
    private val jobRepository: JobRepository = FirestoreJobRepositoryImpl()
) : ViewModel() {

    private val _customerId = MutableStateFlow("customer_1")
    val customerId: StateFlow<String> = _customerId.asStateFlow()

    private val _workerId = MutableStateFlow("1")
    val workerId: StateFlow<String> = _workerId.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _lastCreatedJob = MutableStateFlow<JobRequest?>(null)
    val lastCreatedJob: StateFlow<JobRequest?> = _lastCreatedJob.asStateFlow()

    private val _requestSuccess = MutableStateFlow<Boolean?>(null)
    val requestSuccess: StateFlow<Boolean?> = _requestSuccess.asStateFlow()

    val customerJobs: StateFlow<List<JobRequest>> = jobRepository.getCustomerJobs("customer_1")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val workerJobs: StateFlow<List<JobRequest>> = _workerId
        .flatMapLatest { id -> jobRepository.getWorkerJobs(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Set the worker ID and fetch incoming jobs for the worker.
     */
    fun fetchWorkerJobs(workerId: String) {
        _workerId.value = workerId
    }

    fun setWorkerId(workerId: String) {
        _workerId.value = workerId
    }

    /**
     * Accepts a job request, updating its status to JobStatus.ACCEPTED.
     */
    fun acceptJob(
        jobId: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val success = jobRepository.updateJobStatus(jobId, JobStatus.ACCEPTED)
            onComplete?.invoke(success)
        }
    }

    /**
     * Rejects a job request, updating its status to JobStatus.REJECTED.
     */
    fun rejectJob(
        jobId: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val success = jobRepository.updateJobStatus(jobId, JobStatus.REJECTED)
            onComplete?.invoke(success)
        }
    }

    /**
     * Completes a job request, updating its status to JobStatus.COMPLETED.
     */
    fun completeJob(
        jobId: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val success = jobRepository.updateJobStatus(jobId, JobStatus.COMPLETED)
            onComplete?.invoke(success)
        }
    }

    /**
     * Updates a job request status to any JobStatus.
     */
    fun updateJobStatus(
        jobId: String,
        newStatus: JobStatus,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val success = jobRepository.updateJobStatus(jobId, newStatus)
            onComplete?.invoke(success)
        }
    }

    /**
     * Creates a new job request with the status explicitly set to JobStatus.PENDING.
     */
    fun createJobRequest(
        jobRequest: JobRequest,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            // Status is explicitly set to JobStatus.PENDING
            val pendingJobRequest = jobRequest.copy(status = JobStatus.PENDING)
            val success = jobRepository.createJobRequest(pendingJobRequest)
            _isSubmitting.value = false
            _requestSuccess.value = success
            if (success) {
                _lastCreatedJob.value = pendingJobRequest
            }
            onComplete?.invoke(success)
        }
    }

    /**
     * Overloaded convenience function to create a new job request from parameters,
     * ensuring the status is set to JobStatus.PENDING.
     */
    fun createJobRequest(
        title: String,
        workType: String,
        description: String,
        offeredWage: Int,
        location: String,
        workersNeeded: Int = 1,
        urgency: String = "Today",
        dateTime: String = "Today, 9:00 AM",
        worker: WorkerProfile? = null,
        customerId: String = "customer_1",
        customerName: String = "Ramesh Verma",
        customerPhone: String = "+91 98765 43210",
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val newRequest = JobRequest(
            id = System.currentTimeMillis().toString(),
            customerId = customerId,
            customerName = customerName,
            customerPhone = customerPhone,
            workerId = worker?.id?.toString() ?: "",
            workerName = worker?.name ?: "",
            title = title,
            workType = workType,
            description = description,
            location = location,
            dateTime = dateTime,
            offeredWage = offeredWage,
            workersNeeded = workersNeeded,
            urgency = urgency,
            status = JobStatus.PENDING,
            timestamp = System.currentTimeMillis()
        )
        createJobRequest(newRequest, onComplete)
    }

    fun setCustomerId(id: String) {
        _customerId.value = id
    }

    fun resetRequestStatus() {
        _requestSuccess.value = null
    }

    /**
     * Updates the specific job document in Firestore by adding workerUid into appliedWorkers array.
     */
    fun applyForJob(
        jobId: String,
        workerUid: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val success = jobRepository.applyForJob(jobId, workerUid)
            onComplete?.invoke(success)
        }
    }

    /**
     * Updates the job document by changing its status to "Assigned" and setting assignedWorkerId to workerUid.
     */
    fun hireWorker(
        jobId: String,
        workerUid: String,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val success = jobRepository.hireWorker(jobId, workerUid)
            onComplete?.invoke(success)
        }
    }

    /**
     * Queries the users collection and returns a list of worker profiles for the provided UIDs.
     */
    suspend fun getWorkersByUids(uids: List<String>): List<WorkerProfile> {
        return jobRepository.getWorkersByUids(uids)
    }

    /**
     * Overloaded callback version for querying workers by UIDs.
     */
    fun getWorkersByUids(
        uids: List<String>,
        onResult: (List<WorkerProfile>) -> Unit
    ) {
        viewModelScope.launch {
            val workers = jobRepository.getWorkersByUids(uids)
            onResult(workers)
        }
    }
}

/**
 * WorkerJobsViewModel convenience alias for worker jobs management.
 */
typealias WorkerJobsViewModel = JobViewModel

