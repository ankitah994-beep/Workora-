package com.example.data

import com.example.model.Job
import com.example.model.JobRequest
import com.example.model.JobStatus
import com.example.model.WorkerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * JobRepository interface for managing job requests / bookings and Firestore jobs collection.
 */
interface JobRepository {
    suspend fun saveJob(job: Job): Result<Job>
    fun getPostedJobsByCustomer(customerId: String): Flow<List<Job>>
    suspend fun createJobRequest(jobRequest: JobRequest): Boolean
    fun getCustomerJobs(customerId: String): Flow<List<JobRequest>>
    fun getWorkerJobs(workerId: String): Flow<List<JobRequest>>
    suspend fun updateJobStatus(jobId: String, newStatus: JobStatus): Boolean
    suspend fun applyForJob(jobId: String, workerUid: String): Boolean
    suspend fun hireWorker(jobId: String, workerUid: String): Boolean
    suspend fun getWorkersByUids(uids: List<String>): List<WorkerProfile>
}

/**
 * MockJobRepository implementation that stores job requests in a StateFlow
 * with mock data for testing and compiling.
 */
class MockJobRepository : JobRepository {

    private val _savedJobs = MutableStateFlow<List<Job>>(emptyList())
    val savedJobs: Flow<List<Job>> = _savedJobs.asStateFlow()

    override suspend fun applyForJob(jobId: String, workerUid: String): Boolean = true

    override suspend fun hireWorker(jobId: String, workerUid: String): Boolean = true

    override suspend fun getWorkersByUids(uids: List<String>): List<WorkerProfile> = emptyList()

    override suspend fun saveJob(job: Job): Result<Job> {
        val finalId = if (job.id.isNotBlank()) job.id else System.currentTimeMillis().toString()
        val finalJob = job.copy(id = finalId)
        val current = _savedJobs.value.toMutableList()
        current.add(0, finalJob)
        _savedJobs.value = current
        return Result.success(finalJob)
    }

    override fun getPostedJobsByCustomer(customerId: String): Flow<List<Job>> {
        return _savedJobs.asStateFlow().map { list ->
            if (customerId.isBlank()) list
            else list.filter { it.customerId == customerId }
        }
    }

    private val _jobs = MutableStateFlow<List<JobRequest>>(
        listOf(
            JobRequest(
                id = "1",
                customerId = "customer_1",
                customerName = "Ramesh Verma",
                customerPhone = "+91 98765 43210",
                workerId = "1",
                workerName = "Rajesh Sharma",
                title = "Wall Plastering & Brickwork",
                workType = "Mason",
                description = "Need experienced mason for boundary wall plastering and cement repair work.",
                location = "Sector 14, Main Market, Gurugram",
                dateTime = "Today, 9:00 AM",
                offeredWage = 800,
                workersNeeded = 1,
                urgency = "Today",
                status = JobStatus.PENDING
            ),
            JobRequest(
                id = "2",
                customerId = "customer_1",
                customerName = "Ramesh Verma",
                customerPhone = "+91 98765 43210",
                workerId = "2",
                workerName = "Sunil Kumar",
                title = "Interior Painting 2BHK",
                workType = "Painter",
                description = "Living room and 2 bedrooms wall putty and plastic paint work.",
                location = "Sector 12, Gurugram",
                dateTime = "Tomorrow, 8:30 AM",
                offeredWage = 650,
                workersNeeded = 2,
                urgency = "Flexible",
                status = JobStatus.ACCEPTED
            ),
            JobRequest(
                id = "3",
                customerId = "customer_1",
                customerName = "Ramesh Verma",
                customerPhone = "+91 98765 43210",
                workerId = "3",
                workerName = "Amit Verma",
                title = "Wooden Door & Frame Fitting",
                workType = "Carpenter",
                description = "Door frame installation and lock fixing for main entrance.",
                location = "DLF Phase 3, Gurugram",
                dateTime = "Yesterday, 10:00 AM",
                offeredWage = 750,
                workersNeeded = 1,
                urgency = "Done",
                status = JobStatus.COMPLETED
            )
        )
    )

    override suspend fun createJobRequest(jobRequest: JobRequest): Boolean {
        val currentList = _jobs.value.toMutableList()
        // Ensure new request has status PENDING
        val newRequest = if (jobRequest.status != JobStatus.PENDING) {
            jobRequest.copy(status = JobStatus.PENDING)
        } else {
            jobRequest
        }
        currentList.add(0, newRequest)
        _jobs.value = currentList
        return true
    }

    override fun getCustomerJobs(customerId: String): Flow<List<JobRequest>> {
        return _jobs.asStateFlow().map { list ->
            if (customerId.isBlank()) list
            else list.filter { it.customerId == customerId }
        }
    }

    override fun getWorkerJobs(workerId: String): Flow<List<JobRequest>> {
        return _jobs.asStateFlow().map { list ->
            if (workerId.isBlank()) {
                list
            } else {
                list.filter { it.workerId.isBlank() || it.workerId == workerId }
            }
        }
    }

    override suspend fun updateJobStatus(jobId: String, newStatus: JobStatus): Boolean {
        val currentList = _jobs.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == jobId }
        return if (index != -1) {
            currentList[index] = currentList[index].copy(status = newStatus)
            _jobs.value = currentList
            true
        } else {
            false
        }
    }
}
