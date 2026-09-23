package com.example

import com.example.data.MockJobRepository
import com.example.data.MockWorkerRepository
import com.example.model.JobRequest
import com.example.model.JobStatus
import com.example.viewmodel.HomeViewModel
import com.example.viewmodel.JobViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun jobRepository_createAndGetCustomerJobs() = runBlocking {
        val repo = MockJobRepository()
        val initialJobs = repo.getCustomerJobs("customer_1").first()
        assertTrue(initialJobs.isNotEmpty())

        val newRequest = JobRequest(
            customerId = "customer_1",
            title = "Plumbing Work",
            workType = "Plumber",
            description = "Fix water pipe leakage",
            offeredWage = 700,
            status = JobStatus.PENDING
        )

        val success = repo.createJobRequest(newRequest)
        assertTrue(success)

        val updatedJobs = repo.getCustomerJobs("customer_1").first()
        assertEquals(newRequest.title, updatedJobs.first().title)
        assertEquals(JobStatus.PENDING, updatedJobs.first().status)
    }

    @Test
    fun jobViewModel_createsPendingJobRequest() = runBlocking {
        val repo = MockJobRepository()
        val viewModel = JobViewModel(repo)

        var callbackSuccess = false
        viewModel.createJobRequest(
            title = "Wall Painting",
            workType = "Painter",
            description = "Paint living room",
            offeredWage = 650,
            location = "Sector 14",
            onComplete = { callbackSuccess = it }
        )

        // Give coroutine a moment to complete
        kotlinx.coroutines.delay(100)

        assertTrue(callbackSuccess)
        val created = viewModel.lastCreatedJob.value
        assertNotNull(created)
        assertEquals(JobStatus.PENDING, created?.status)
        assertEquals("Wall Painting", created?.title)
    }

    @Test
    fun workerRepository_and_homeViewModel() = runBlocking {
        val workerRepo = MockWorkerRepository()
        val workers = workerRepo.getAvailableWorkers().first()
        assertTrue(workers.isNotEmpty())
        assertEquals("Mason", workers.first().trade)

        val homeViewModel = HomeViewModel(workerRepo)
        assertNotNull(homeViewModel)
    }

    @Test
    fun jobRepository_workerJobs_and_updateStatus() = runBlocking {
        val repo = MockJobRepository()
        val workerJobs = repo.getWorkerJobs("1").first()
        assertTrue(workerJobs.isNotEmpty())

        val targetJob = workerJobs.first()
        assertEquals("1", targetJob.id)

        val acceptSuccess = repo.updateJobStatus("1", JobStatus.ACCEPTED)
        assertTrue(acceptSuccess)

        val afterAccept = repo.getWorkerJobs("1").first()
        assertEquals(JobStatus.ACCEPTED, afterAccept.first { it.id == "1" }.status)

        val rejectSuccess = repo.updateJobStatus("1", JobStatus.REJECTED)
        assertTrue(rejectSuccess)

        val afterReject = repo.getWorkerJobs("1").first()
        assertEquals(JobStatus.REJECTED, afterReject.first { it.id == "1" }.status)
    }

    @Test
    fun jobViewModel_acceptAndRejectJob() = runBlocking {
        val repo = MockJobRepository()
        val viewModel = JobViewModel(repo)

        var acceptDone = false
        viewModel.acceptJob("1") { acceptDone = it }
        kotlinx.coroutines.delay(100)
        assertTrue(acceptDone)

        val jobsAfterAccept = repo.getWorkerJobs("1").first()
        assertEquals(JobStatus.ACCEPTED, jobsAfterAccept.first { it.id == "1" }.status)

        var rejectDone = false
        viewModel.rejectJob("1") { rejectDone = it }
        kotlinx.coroutines.delay(100)
        assertTrue(rejectDone)

        val jobsAfterReject = repo.getWorkerJobs("1").first()
        assertEquals(JobStatus.REJECTED, jobsAfterReject.first { it.id == "1" }.status)
    }

    @Test
    fun jobViewModel_completeJob_updatesStatusToCompleted() = runBlocking {
        val repo = MockJobRepository()
        val viewModel = JobViewModel(repo)

        var completeDone = false
        viewModel.completeJob("1") { completeDone = it }
        kotlinx.coroutines.delay(100)
        assertTrue(completeDone)

        val jobsAfterComplete = repo.getWorkerJobs("1").first()
        assertEquals(JobStatus.COMPLETED, jobsAfterComplete.first { it.id == "1" }.status)
    }

    @Test
    fun reviewRepository_submitAndGetWorkerReviews() = runBlocking {
        val repo = com.example.data.MockReviewRepository()
        val initialReviews = repo.getWorkerReviews("1").first()
        assertTrue(initialReviews.isNotEmpty())

        val newReview = com.example.model.Review(
            id = "test_review_1",
            jobId = "job_99",
            customerId = "customer_1",
            workerId = "1",
            rating = 5.0f,
            comment = "Exceptional plumbing service!"
        )

        val success = repo.submitReview(newReview)
        assertTrue(success)

        val updatedReviews = repo.getWorkerReviews("1").first()
        val found = updatedReviews.find { it.id == "test_review_1" }
        assertNotNull(found)
        assertEquals(5.0f, found?.rating)
        assertEquals("Exceptional plumbing service!", found?.comment)
    }

    @Test
    fun reviewViewModel_submitReview_and_observeReviews() = runBlocking {
        val repo = com.example.data.MockReviewRepository()
        val viewModel = com.example.viewmodel.ReviewViewModel(repo)

        var submittedSuccess = false
        viewModel.submitReview(
            jobId = "job_101",
            customerId = "customer_1",
            workerId = "1",
            rating = 4.5f,
            comment = "Prompt and skilled work"
        ) { submittedSuccess = it }

        kotlinx.coroutines.delay(100)
        assertTrue(submittedSuccess)

        val reviews = repo.getWorkerReviews("1").first()
        val submitted = reviews.find { it.jobId == "job_101" }
        assertNotNull(submitted)
        assertEquals(4.5f, submitted?.rating)
        assertEquals("Prompt and skilled work", submitted?.comment)
    }

    @Test
    fun userRepository_updateUserProfile_updatesSuccessfully() = runBlocking {
        val repo: com.example.data.UserRepository = com.example.data.MockUserRepository()
        val updatedUser = com.example.model.User(
            id = 1,
            name = "Ravi Sharma",
            trade = "Electrician",
            dailyWage = 950,
            experienceYears = 7,
            rating = 4.8f,
            reviewsCount = 30,
            location = "Sector 29, Gurugram",
            distance = "2.1 km",
            phone = "+91 99999 88888",
            isAvailableToday = true,
            isVerified = true
        )

        val success = repo.updateUserProfile(updatedUser)
        assertTrue(success)

        val retrieved = repo.getUserProfile("1").first()
        assertNotNull(retrieved)
        assertEquals("Ravi Sharma", retrieved?.name)
        assertEquals("Electrician", retrieved?.trade)
        assertEquals(950, retrieved?.dailyWage)
        assertEquals("+91 99999 88888", retrieved?.phone)
        assertEquals("Sector 29, Gurugram", retrieved?.location)
    }

    @Test
    fun profileViewModel_updateWorkerProfile_persistsChanges() = runBlocking {
        val userRepo = com.example.data.MockUserRepository()
        val authRepo = com.example.data.MockAuthRepository()
        val viewModel = com.example.viewmodel.ProfileViewModel(userRepo, authRepo)

        var isDone = false
        viewModel.updateWorkerProfile(
            name = "Amit Kumar",
            phone = "+91 98765 12345",
            area = "Noida Sector 62",
            skills = "Carpenter",
            wage = 900,
            availability = false
        ) { isDone = it }

        kotlinx.coroutines.delay(100)
        assertTrue(isDone)

        val updated = userRepo.getUserProfile("1").first()
        assertNotNull(updated)
        assertEquals("Amit Kumar", updated?.name)
        assertEquals("Carpenter", updated?.trade)
        assertEquals(900, updated?.dailyWage)
        assertEquals(false, updated?.isAvailableToday)
        assertEquals("+91 98765 12345", updated?.phone)
        assertEquals("Noida Sector 62", updated?.location)
    }

    @Test
    fun profileViewModel_updateCustomerProfile_persistsChanges() = runBlocking {
        val userRepo = com.example.data.MockUserRepository()
        val authRepo = com.example.data.MockAuthRepository()
        // Log in a mock customer first
        authRepo.loginWithEmail("test@workora.com", "pass", com.example.model.UserRole.CUSTOMER)
        val viewModel = com.example.viewmodel.ProfileViewModel(userRepo, authRepo)

        var isDone = false
        viewModel.updateCustomerProfile(
            name = "Rajesh Gupta",
            phone = "+91 91234 56789",
            area = "South Extension, Delhi"
        ) { isDone = it }

        kotlinx.coroutines.delay(100)
        assertTrue(isDone)

        val currentAccount = authRepo.currentUser.value
        assertNotNull(currentAccount)
        assertEquals("Rajesh Gupta", currentAccount?.fullName)
        assertEquals("+91 91234 56789", currentAccount?.mobileNumber)
        assertEquals("South Extension, Delhi", currentAccount?.location)
    }

    @Test
    fun firestoreJobRepository_createAndQueryJobs() = runBlocking {
        val repo = com.example.data.FirestoreJobRepositoryImpl(firestore = null)
        val newJob = JobRequest(
            id = "test_job_1",
            customerId = "customer_test",
            title = "Gate Welding",
            workType = "Welder",
            offeredWage = 850
        )
        val created = repo.createJobRequest(newJob)
        assertTrue(created)

        val customerJobs = repo.getCustomerJobs("customer_test").first()
        assertTrue(customerJobs.any { it.id == "test_job_1" && it.title == "Gate Welding" })

        val updated = repo.updateJobStatus("test_job_1", JobStatus.ACCEPTED)
        assertTrue(updated)
    }

    @Test
    fun firestoreReviewRepository_submitAndQueryReviews() = runBlocking {
        val repo = com.example.data.FirestoreReviewRepositoryImpl(firestore = null)
        val newReview = com.example.model.Review(
            id = "review_test_1",
            jobId = "test_job_1",
            customerId = "customer_test",
            workerId = "worker_42",
            rating = 5.0f,
            comment = "Outstanding craftsmanship!"
        )
        val submitted = repo.submitReview(newReview)
        assertTrue(submitted)

        val reviews = repo.getWorkerReviews("worker_42").first()
        assertTrue(reviews.any { it.id == "review_test_1" && it.rating == 5.0f })
    }
}


