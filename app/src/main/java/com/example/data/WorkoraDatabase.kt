package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.model.JobApplication
import com.example.model.JobPost
import com.example.model.UserAccount
import com.example.model.WorkerProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface WorkoraDao {
    // Users & Auth
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<UserAccount?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUserSync(): UserAccount?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logoutAllUsers()

    @Query("UPDATE users SET isLoggedIn = 1 WHERE id = :userId")
    suspend fun setLoginSession(userId: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUsersCount(): Int

    // Jobs
    @Query("SELECT * FROM jobs ORDER BY timestamp DESC")
    fun getAllJobs(): Flow<List<JobPost>>

    @Query("SELECT * FROM jobs WHERE category = :category ORDER BY timestamp DESC")
    fun getJobsByCategory(category: String): Flow<List<JobPost>>

    @Query("SELECT * FROM jobs WHERE id = :id")
    suspend fun getJobById(id: Long): JobPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobPost): Long

    @Update
    suspend fun updateJob(job: JobPost)

    @Query("UPDATE jobs SET applicantsCount = applicantsCount + 1 WHERE id = :jobId")
    suspend fun incrementJobApplicants(jobId: Long)

    @Query("UPDATE jobs SET status = :status WHERE id = :jobId")
    suspend fun updateJobStatus(jobId: Long, status: String)

    @Query("SELECT COUNT(*) FROM jobs")
    suspend fun getJobsCount(): Int

    // Workers
    @Query("SELECT * FROM workers ORDER BY rating DESC")
    fun getAllWorkers(): Flow<List<WorkerProfile>>

    @Query("SELECT * FROM workers WHERE trade = :trade ORDER BY rating DESC")
    fun getWorkersByTrade(trade: String): Flow<List<WorkerProfile>>

    @Query("SELECT * FROM workers WHERE id = :id")
    suspend fun getWorkerById(id: Long): WorkerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: WorkerProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkers(workers: List<WorkerProfile>)

    @Query("UPDATE workers SET isAvailableToday = :isAvailable WHERE id = :workerId")
    suspend fun updateWorkerAvailability(workerId: Long, isAvailable: Boolean)

    @Query("SELECT COUNT(*) FROM workers")
    suspend fun getWorkersCount(): Int

    // Applications
    @Query("SELECT * FROM applications ORDER BY timestamp DESC")
    fun getAllApplications(): Flow<List<JobApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: JobApplication): Long

    @Query("UPDATE applications SET status = :status WHERE id = :appId")
    suspend fun updateApplicationStatus(appId: Long, status: String)

    @Query("SELECT * FROM applications WHERE jobId = :jobId LIMIT 1")
    suspend fun getApplicationByJobId(jobId: Long): JobApplication?

    @Query("UPDATE applications SET status = :status WHERE jobId = :jobId")
    suspend fun updateApplicationStatusByJobId(jobId: Long, status: String)
}

@Database(
    entities = [JobPost::class, WorkerProfile::class, JobApplication::class, UserAccount::class],
    version = 3,
    exportSchema = false
)
abstract class WorkoraDatabase : RoomDatabase() {
    abstract fun workoraDao(): WorkoraDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoraDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WorkoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoraDatabase::class.java,
                    "workora_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                // Seed initial sample data if empty
                scope.launch(Dispatchers.IO) {
                    if (instance.workoraDao().getWorkersCount() == 0) {
                        seedDatabase(instance.workoraDao())
                    }
                }

                instance
            }
        }

        private suspend fun seedDatabase(dao: WorkoraDao) {
            // Seed default users for testing
            if (dao.getUsersCount() == 0) {
                dao.insertUser(
                    UserAccount(
                        fullName = "Ramesh Verma",
                        mobileNumber = "9876543210",
                        email = "customer@workora.com",
                        password = "password123",
                        location = "Sector 14, Gurugram",
                        role = "CUSTOMER",
                        isLoggedIn = false
                    )
                )
                dao.insertUser(
                    UserAccount(
                        fullName = "Sunil Kumar",
                        mobileNumber = "9811122334",
                        email = "worker@workora.com",
                        password = "password123",
                        location = "Labour Chowk, Delhi",
                        role = "LABOUR",
                        isLoggedIn = false
                    )
                )
            }
            val sampleWorkers = listOf(
                WorkerProfile(
                    name = "Sunil Kumar",
                    trade = "Mason",
                    dailyWage = 850,
                    experienceYears = 8,
                    rating = 4.9f,
                    reviewsCount = 42,
                    location = "Sector 14, Metro Road",
                    distance = "0.8 km",
                    phone = "+91 98111 22334",
                    isAvailableToday = true,
                    isVerified = true
                ),
                WorkerProfile(
                    name = "Mohammad Aslam",
                    trade = "Electrician",
                    dailyWage = 950,
                    experienceYears = 6,
                    rating = 4.8f,
                    reviewsCount = 38,
                    location = "Main Market Area",
                    distance = "1.4 km",
                    phone = "+91 98222 33445",
                    isAvailableToday = true,
                    isVerified = true
                ),
                WorkerProfile(
                    name = "Rajesh Sharma",
                    trade = "Plumber",
                    dailyWage = 900,
                    experienceYears = 10,
                    rating = 4.9f,
                    reviewsCount = 57,
                    location = "Civil Lines Colony",
                    distance = "2.1 km",
                    phone = "+91 98333 44556",
                    isAvailableToday = true,
                    isVerified = true
                ),
                WorkerProfile(
                    name = "Vicky Vishwakarma",
                    trade = "Carpenter",
                    dailyWage = 850,
                    experienceYears = 7,
                    rating = 4.7f,
                    reviewsCount = 29,
                    location = "Industrial Area Gate 2",
                    distance = "3.0 km",
                    phone = "+91 98444 55667",
                    isAvailableToday = false,
                    isVerified = true
                ),
                WorkerProfile(
                    name = "Dinesh Prajapati",
                    trade = "Painter",
                    dailyWage = 750,
                    experienceYears = 5,
                    rating = 4.6f,
                    reviewsCount = 23,
                    location = "Green Valley Apartments",
                    distance = "1.8 km",
                    phone = "+91 98555 66778",
                    isAvailableToday = true,
                    isVerified = true
                ),
                WorkerProfile(
                    name = "Babu Lal",
                    trade = "Construction Helper",
                    dailyWage = 600,
                    experienceYears = 4,
                    rating = 4.8f,
                    reviewsCount = 31,
                    location = "Labour Chowk",
                    distance = "0.5 km",
                    phone = "+91 98666 77889",
                    isAvailableToday = true,
                    isVerified = true
                )
            )
            dao.insertWorkers(sampleWorkers)

            if (dao.getJobsCount() == 0) {
                val sampleJobs = listOf(
                    JobPost(
                        title = "Tile Fitting & Wall Plastering",
                        category = "Mason",
                        description = "Need 2 experienced masons for floor tiling and bathroom wall plastering work.",
                        dailyRate = 900,
                        location = "Model Town Phase 2",
                        workersNeeded = 2,
                        urgency = "Today",
                        dateTime = "Today, 9:00 AM",
                        customerName = "Anil Kapoor",
                        customerPhone = "+91 98700 11223",
                        status = "PENDING",
                        applicantsCount = 1
                    ),
                    JobPost(
                        title = "Full House Conduit Wiring",
                        category = "Electrician",
                        description = "Complete wiring for 3BHK flat including MCB box fitting and switches.",
                        dailyRate = 1000,
                        location = "Sunset Heights, Tower B",
                        workersNeeded = 1,
                        urgency = "Tomorrow",
                        dateTime = "Tomorrow, 10:00 AM",
                        customerName = "Pooja Singhal",
                        customerPhone = "+91 98711 22334",
                        status = "PENDING",
                        applicantsCount = 0
                    ),
                    JobPost(
                        title = "Water Tank & Pipeline Repair",
                        category = "Plumber",
                        description = "Rooftop PVC water tank connection leaking. Need quick fix & new valve installation.",
                        dailyRate = 850,
                        location = "Shanti Niketan",
                        workersNeeded = 1,
                        urgency = "Urgent",
                        dateTime = "Today, 2:00 PM",
                        customerName = "Dr. S. K. Gupta",
                        customerPhone = "+91 98722 33445",
                        status = "ACCEPTED",
                        applicantsCount = 1
                    ),
                    JobPost(
                        title = "Exterior Weather Coat Painting",
                        category = "Painter",
                        description = "Exterior wall painting for double storey bungalow. Scaffolding provided.",
                        dailyRate = 800,
                        location = "Defence Colony",
                        workersNeeded = 3,
                        urgency = "In 2 Days",
                        dateTime = "Thursday, 8:30 AM",
                        customerName = "Harpreet Singh",
                        customerPhone = "+91 98733 44556",
                        status = "PENDING",
                        applicantsCount = 2
                    )
                )
                sampleJobs.forEach { dao.insertJob(it) }

                // Seed accepted job application for demonstration in "My Jobs"
                dao.insertApplication(
                    JobApplication(
                        jobId = 3L,
                        workerId = 1L,
                        workerName = "Sunil Kumar",
                        jobTitle = "Water Tank & Pipeline Repair",
                        category = "Plumber",
                        dailyRate = 850,
                        location = "Shanti Niketan",
                        dateTime = "Today, 2:00 PM",
                        status = "ACCEPTED"
                    )
                )
            }
        }
    }
}
