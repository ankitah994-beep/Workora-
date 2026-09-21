package com.example.data

import com.example.model.JobApplication
import com.example.model.JobPost
import com.example.model.UserAccount
import com.example.model.WorkerProfile
import kotlinx.coroutines.flow.Flow

class WorkoraRepository(private val dao: WorkoraDao) {

    val allJobs: Flow<List<JobPost>> = dao.getAllJobs()
    val allWorkers: Flow<List<WorkerProfile>> = dao.getAllWorkers()
    val allApplications: Flow<List<JobApplication>> = dao.getAllApplications()
    val loggedInUser: Flow<UserAccount?> = dao.getLoggedInUser()

    suspend fun getLoggedInUserSync(): UserAccount? {
        return dao.getLoggedInUserSync()
    }

    suspend fun getUserByEmail(email: String): UserAccount? {
        return dao.getUserByEmail(email)
    }

    suspend fun registerUser(user: UserAccount): Long {
        dao.logoutAllUsers()
        val id = dao.insertUser(user.copy(isLoggedIn = true))
        return id
    }

    suspend fun loginUser(userId: Long) {
        dao.logoutAllUsers()
        dao.setLoginSession(userId)
    }

    suspend fun logout() {
        dao.logoutAllUsers()
    }

    fun getJobsByCategory(category: String): Flow<List<JobPost>> {
        return if (category == "All") dao.getAllJobs() else dao.getJobsByCategory(category)
    }

    fun getWorkersByTrade(trade: String): Flow<List<WorkerProfile>> {
        return if (trade == "All") dao.getAllWorkers() else dao.getWorkersByTrade(trade)
    }

    suspend fun postJob(job: JobPost): Long {
        return dao.insertJob(job)
    }

    suspend fun updateJobStatus(jobId: Long, status: String) {
        dao.updateJobStatus(jobId, status)
    }

    suspend fun applyForJob(job: JobPost, workerName: String, workerId: Long = 1): Long {
        val application = JobApplication(
            jobId = job.id,
            workerId = workerId,
            workerName = workerName,
            jobTitle = job.title,
            category = job.category,
            dailyRate = job.dailyRate,
            location = job.location,
            dateTime = job.dateTime,
            status = "PENDING"
        )
        dao.incrementJobApplicants(job.id)
        return dao.insertApplication(application)
    }

    suspend fun acceptJobRequest(job: JobPost, workerName: String = "Sunil Kumar", workerId: Long = 1L): Long {
        dao.updateJobStatus(job.id, "ACCEPTED")
        val existing = dao.getApplicationByJobId(job.id)
        return if (existing != null) {
            dao.updateApplicationStatus(existing.id, "ACCEPTED")
            existing.id
        } else {
            val app = JobApplication(
                jobId = job.id,
                workerId = workerId,
                workerName = workerName,
                jobTitle = job.title,
                category = job.category,
                dailyRate = job.dailyRate,
                location = job.location,
                dateTime = job.dateTime,
                status = "ACCEPTED"
            )
            dao.insertApplication(app)
        }
    }

    suspend fun rejectJobRequest(job: JobPost, workerName: String = "Sunil Kumar", workerId: Long = 1L) {
        dao.updateJobStatus(job.id, "REJECTED")
        val existing = dao.getApplicationByJobId(job.id)
        if (existing != null) {
            dao.updateApplicationStatus(existing.id, "REJECTED")
        } else {
            val app = JobApplication(
                jobId = job.id,
                workerId = workerId,
                workerName = workerName,
                jobTitle = job.title,
                category = job.category,
                dailyRate = job.dailyRate,
                location = job.location,
                dateTime = job.dateTime,
                status = "REJECTED"
            )
            dao.insertApplication(app)
        }
    }

    suspend fun completeJob(jobId: Long) {
        dao.updateJobStatus(jobId, "COMPLETED")
        dao.updateApplicationStatusByJobId(jobId, "COMPLETED")
    }

    suspend fun setWorkerAvailability(workerId: Long, isAvailable: Boolean) {
        dao.updateWorkerAvailability(workerId, isAvailable)
    }

    suspend fun updateApplicationStatus(appId: Long, status: String) {
        dao.updateApplicationStatus(appId, status)
    }
}
