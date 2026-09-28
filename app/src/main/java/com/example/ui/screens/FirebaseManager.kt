package com.example.ui.screens

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FirebaseManager {
    // Production Firebase Realtime Database Base URL
    const val DATABASE_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

    // 1. Log Admin Audit (Point 4 & 8)
    fun logAdminAudit(adminIdentifier: String, action: String, target: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logKey = "audit_${System.currentTimeMillis()}"
                val conn = (URL("$DATABASE_URL/admin_audit_logs/$logKey.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("adminIdentifier", adminIdentifier)
                    put("action", action)
                    put("target", target)
                    put("timestamp", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
        }
    }

    // 2. Sync User (Password is NEVER stored in /users - Point 2)
    fun syncUserToFirebase(
        context: Context,
        name: String,
        email: String,
        phone: String,
        password: String, // Kept in memory only, not pushed to plain user json
        role: String
    ) {
        val cleanPhone = phone.filter { it.isDigit() }.takeLast(10).ifBlank { "0000000000" }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = (URL("$DATABASE_URL/users/u_$cleanPhone.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("name", name)
                    put("email", email)
                    put("phone", phone)
                    put("role", role)
                    put("accountStatus", "ACTIVE")
                    put("phoneVerified", true)
                    put("emailVerified", email.isNotBlank())
                    put("identityVerified", false)
                    put("updatedAt", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
        }
    }

    // 3. Create Booking Record (Point 5 - Two-Way Hiring Flow)
    fun createBooking(
        customerPhone: String,
        customerName: String,
        workerPhone: String,
        workerName: String,
        jobTitle: String,
        category: String,
        dailyRate: Int,
        location: String,
        onSuccess: (String) -> Unit = {}
    ) {
        val bookingId = "bk_${System.currentTimeMillis()}"
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = (URL("$DATABASE_URL/bookings/$bookingId.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("bookingId", bookingId)
                    put("customerPhone", customerPhone)
                    put("customerName", customerName)
                    put("workerPhone", workerPhone)
                    put("workerName", workerName)
                    put("jobTitle", jobTitle)
                    put("category", category)
                    put("dailyRate", dailyRate)
                    put("location", location)
                    put("status", "PENDING") // PENDING -> ACCEPTED -> IN_PROGRESS -> COMPLETED -> CANCELLED
                    put("createdAt", System.currentTimeMillis())
                    put("updatedAt", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
                onSuccess(bookingId)
            } catch (_: Exception) {}
        }
    }

    // 4. Update Booking Status (Point 5 & 6)
    fun updateBookingStatus(bookingId: String, newStatus: String, cancelReason: String = "") {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = (URL("$DATABASE_URL/bookings/$bookingId.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PATCH"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("status", newStatus)
                    put("updatedAt", System.currentTimeMillis())
                    if (cancelReason.isNotBlank()) {
                        put("cancelReason", cancelReason)
                        put("cancelledAt", System.currentTimeMillis())
                    }
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
        }
    }

    // 5. Submit Report (Point 7 - 7 Reasons)
    fun submitReport(
        reporterPhone: String,
        reportedPhone: String,
        reportedName: String,
        reason: String,
        description: String,
        onComplete: (Boolean) -> Unit
    ) {
        val reportId = "rep_${System.currentTimeMillis()}"
        CoroutineScope(Dispatchers.IO).launch {
            var success = false
            try {
                val conn = (URL("$DATABASE_URL/reports/$reportId.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("reportId", reportId)
                    put("reporterPhone", reporterPhone)
                    put("reportedPhone", reportedPhone)
                    put("reportedName", reportedName)
                    put("reason", reason)
                    put("description", description)
                    put("status", "PENDING_REVIEW")
                    put("timestamp", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                success = conn.responseCode in 200..299
                conn.disconnect()
            } catch (_: Exception) {}
            onComplete(success)
        }
    }

    // 6. Block & Unblock User (Point 7)
    fun setBlockStatus(myPhone: String, targetPhone: String, isBlocked: Boolean, onDone: () -> Unit = {}) {
        val myClean = myPhone.filter { it.isDigit() }.takeLast(10)
        val targetClean = targetPhone.filter { it.isDigit() }.takeLast(10)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = (URL("$DATABASE_URL/blocked_users/u_$myClean/u_$targetClean.json").openConnection() as HttpURLConnection).apply {
                    if (isBlocked) {
                        requestMethod = "PUT"
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                        OutputStreamWriter(outputStream).use { it.write(JSONObject().apply { put("blocked", true); put("timestamp", System.currentTimeMillis()) }.toString()) }
                    } else {
                        requestMethod = "DELETE"
                    }
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
            onDone()
        }
    }

    // 7. Submit Review & Rating (Point 10 - 1 to 5 Stars)
    fun submitReview(
        bookingId: String,
        reviewerPhone: String,
        reviewerName: String,
        targetPhone: String,
        rating: Int,
        comment: String,
        onDone: (Boolean) -> Unit
    ) {
        val revId = "rev_${System.currentTimeMillis()}"
        CoroutineScope(Dispatchers.IO).launch {
            var ok = false
            try {
                val conn = (URL("$DATABASE_URL/reviews/$revId.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("reviewId", revId)
                    put("bookingId", bookingId)
                    put("reviewerPhone", reviewerPhone)
                    put("reviewerName", reviewerName)
                    put("targetPhone", targetPhone)
                    put("rating", rating.coerceIn(1, 5))
                    put("comment", comment)
                    put("timestamp", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                ok = conn.responseCode in 200..299
                conn.disconnect()
            } catch (_: Exception) {}
            onDone(ok)
        }
    }

    // 8. Submit KYC Verification Document (Point 9)
    fun submitKycDocument(userPhone: String, docType: String, docNumber: String, docBase64: String, onDone: (Boolean) -> Unit) {
        val cleanPhone = userPhone.filter { it.isDigit() }.takeLast(10)
        CoroutineScope(Dispatchers.IO).launch {
            var ok = false
            try {
                val conn = (URL("$DATABASE_URL/kyc_verifications/u_$cleanPhone.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("userPhone", userPhone)
                    put("docType", docType)
                    put("docNumber", docNumber)
                    put("docBase64", docBase64)
                    put("status", "VERIFICATION_PENDING") // VERIFICATION_PENDING -> VERIFIED -> REJECTED
                    put("timestamp", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                ok = conn.responseCode in 200..299
                conn.disconnect()
            } catch (_: Exception) {}
            onDone(ok)
        }
    }

    // 9. Account Deletion & Data Anonymization (Point 13)
    fun deleteAndAnonymizeAccount(userPhone: String, onDone: (Boolean) -> Unit) {
        val cleanPhone = userPhone.filter { it.isDigit() }.takeLast(10)
        CoroutineScope(Dispatchers.IO).launch {
            var ok = false
            try {
                // Delete from active /workers
                val wConn = (URL("$DATABASE_URL/workers/w_$cleanPhone.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                }
                wConn.responseCode
                wConn.disconnect()

                // Anonymize user record in /users (Retain anonymous historical reference)
                val uConn = (URL("$DATABASE_URL/users/u_$cleanPhone.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PATCH"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val anonJson = JSONObject().apply {
                    put("name", "Deleted User")
                    put("email", "deleted@workora.in")
                    put("accountStatus", "DELETED")
                    put("deletedAt", System.currentTimeMillis())
                }
                OutputStreamWriter(uConn.outputStream).use { it.write(anonJson.toString()) }
                ok = uConn.responseCode in 200..299
                uConn.disconnect()
            } catch (_: Exception) {}
            onDone(ok)
        }
    }
}
