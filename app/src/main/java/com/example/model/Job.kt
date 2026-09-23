package com.example.model

/**
 * Job data class representing a job posting document stored in the Firestore "jobs" collection.
 *
 * @property id The unique Firestore document ID
 * @property title The job title
 * @property category The job category (e.g., Mason, Plumber, Electrician, Carpenter)
 * @property location The physical location / address of the work
 * @property date Date & time requirement for the job
 * @property description Detailed description of the work
 * @property customerId The UID of the customer who posted the job (from FirebaseAuth)
 * @property status Current job status (default: "open")
 * @property timestamp Unix epoch timestamp in milliseconds when the job was posted
 */
data class Job(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val location: String = "",
    val date: String = "",
    val description: String = "",
    val customerId: String = "",
    val status: String = "open",
    val timestamp: Long = System.currentTimeMillis(),
    val appliedWorkers: List<String> = emptyList(),
    val assignedWorkerId: String = ""
)
