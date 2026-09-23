package com.example.data

import com.example.model.WorkerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * WorkerRepository interface defining operations to fetch worker data.
 */
interface WorkerRepository {
    fun getAvailableWorkers(): Flow<List<WorkerProfile>>
    suspend fun getWorkersByIds(workerIds: List<String>): List<WorkerProfile>
    suspend fun getWorkersByUids(uids: List<String>): List<WorkerProfile>
}

/**
 * MockWorkerRepository implementation that returns a dummy list of workers
 * so the app compiles, runs, and displays data.
 */
class MockWorkerRepository : WorkerRepository {

    private val _availableWorkers = MutableStateFlow<List<WorkerProfile>>(
        listOf(
            WorkerProfile(
                id = 1,
                name = "Rajesh Sharma",
                trade = "Mason",
                dailyWage = 800,
                experienceYears = 8,
                rating = 4.8f,
                reviewsCount = 42,
                location = "Sector 14, Gurugram",
                distance = "1.2 km",
                phone = "+91 98765 12345",
                isAvailableToday = true,
                isVerified = true
            ),
            WorkerProfile(
                id = 2,
                name = "Sunil Kumar",
                trade = "Painter",
                dailyWage = 650,
                experienceYears = 5,
                rating = 4.6f,
                reviewsCount = 29,
                location = "Old Railway Road, Gurugram",
                distance = "2.4 km",
                phone = "+91 98123 45678",
                isAvailableToday = true,
                isVerified = true
            ),
            WorkerProfile(
                id = 3,
                name = "Amit Verma",
                trade = "Carpenter",
                dailyWage = 750,
                experienceYears = 6,
                rating = 4.7f,
                reviewsCount = 35,
                location = "DLF Phase 3, Gurugram",
                distance = "3.1 km",
                phone = "+91 98234 56789",
                isAvailableToday = true,
                isVerified = true
            ),
            WorkerProfile(
                id = 4,
                name = "Manoj Singh",
                trade = "Plumber",
                dailyWage = 700,
                experienceYears = 4,
                rating = 4.5f,
                reviewsCount = 18,
                location = "Sohna Road, Gurugram",
                distance = "4.0 km",
                phone = "+91 98345 67890",
                isAvailableToday = true,
                isVerified = true
            )
        )
    )

    override fun getAvailableWorkers(): Flow<List<WorkerProfile>> = _availableWorkers.asStateFlow()

    override suspend fun getWorkersByIds(workerIds: List<String>): List<WorkerProfile> {
        val all = _availableWorkers.value
        return workerIds.mapNotNull { idStr ->
            all.firstOrNull { it.id.toString() == idStr }
                ?: WorkerProfile(
                    id = idStr.toLongOrNull() ?: 1L,
                    name = "Worker $idStr",
                    trade = "Skilled Artisan",
                    dailyWage = 750,
                    experienceYears = 5,
                    rating = 4.8f,
                    reviewsCount = 24,
                    location = "Delhi NCR",
                    phone = "+91 98765 00000"
                )
        }
    }

    override suspend fun getWorkersByUids(uids: List<String>): List<WorkerProfile> {
        return getWorkersByIds(uids)
    }
}
