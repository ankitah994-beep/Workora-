package com.example.ui.screens

import android.content.Context
import com.example.model.AdminAuditLog
import com.example.model.JobPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class CloudChatMessage(
    val id: String,
    val senderName: String,
    val messageText: String,
    val timeText: String,
    val isSentByMe: Boolean
)

data class LiveAdminMetrics(
    val totalUsers: Int = 0,
    val totalWorkers: Int = 0,
    val totalCustomers: Int = 0,
    val activeWorkers: Int = 0,
    val activeJobs: Int = 0,
    val completedJobs: Int = 0,
    val pendingRequests: Int = 0,
    val reportsCount: Int = 0
)

object FirebaseManager {

    private const val FIREBASE_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

    private fun toSafeKey(input: String): String {
        return input.trim().lowercase().replace(".", "_").replace("@", "_at_").replace(" ", "_")
    }

    private fun sha256Hash(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            input.hashCode().toString()
        }
    }

    private fun parseAdminJsonStatus(adminText: String): Pair<Boolean, String> {
        if (adminText.isBlank() || adminText == "null") return Pair(false, "SUPER_ADMIN")
        return try {
            val adminJson = JSONObject(adminText)
            val rawActive = adminJson.opt("isActive")
            val isActive = when (rawActive) {
                is Boolean -> rawActive
                is String -> rawActive.trim().equals("true", ignoreCase = true)
                else -> true
            }
            val rawRole = adminJson.optString("role", "").trim()
            val tier = if (rawRole.isEmpty()) "SUPER_ADMIN" else rawRole
            Pair(isActive, tier)
        } catch (e: Exception) {
            Pair(false, "SUPER_ADMIN")
        }
    }

    /**
     * Direct Live Check: Checks if the logged-in email is registered inside /admins/{safeEmailKey}
     */
    fun checkIfEmailIsAdminOnCloud(
        email: String,
        onResult: (isAdmin: Boolean, adminTier: String) -> Unit
    ) {
        if (email.isBlank()) {
            onResult(false, "SUPER_ADMIN")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val safeEmailKey = toSafeKey(email)
                val adminUrl = URL("$FIREBASE_DB_URL/admins/$safeEmailKey.json")
                val adminConn = (adminUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 6000
                    readTimeout = 6000
                }

                var isBackendAdmin = false
                var detectedTier = "SUPER_ADMIN"

                if (adminConn.responseCode in 200..299) {
                    val adminText = BufferedReader(InputStreamReader(adminConn.inputStream)).use { it.readText() }
                    val parsed = parseAdminJsonStatus(adminText)
                    isBackendAdmin = parsed.first
                    detectedTier = parsed.second
                }
                adminConn.disconnect()

                withContext(Dispatchers.Main) {
                    onResult(isBackendAdmin, detectedTier)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "SUPER_ADMIN")
                }
            }
        }
    }

    fun syncUserToFirebase(
        context: Context,
        name: String,
        email: String,
        phone: String,
        password: String,
        role: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sanitizedRole = if (role.uppercase() == "ADMIN") "USER" else role
                val safeEmailKey = toSafeKey(email)
                val profilePrefs = context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE)
                val location = profilePrefs.getString("user_location", "Silwani, Raisen") ?: "Silwani, Raisen"
                val skill = profilePrefs.getString("user_skill", "General Worker") ?: "General Worker"
                val dailyRate = (profilePrefs.getString("user_rate", "500") ?: "500").toIntOrNull() ?: 500

                val userUrl = URL("$FIREBASE_DB_URL/users/$safeEmailKey.json")
                val userConn = (userUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val userJson = JSONObject().apply {
                    put("fullName", name)
                    put("email", email)
                    put("phone", phone)
                    put("authVerifierHash", sha256Hash(password))
                    put("location", location)
                    put("skill", skill)
                    put("dailyRate", dailyRate)
                    put("role", sanitizedRole)
                    put("accountStatus", "ACTIVE")
                    put("updatedAt", System.currentTimeMillis())
                }

                OutputStreamWriter(userConn.outputStream).use { it.write(userJson.toString()) }
                userConn.responseCode
                userConn.disconnect()

                val workerUrl = URL("$FIREBASE_DB_URL/workers/$safeEmailKey.json")
                val workerConn = (workerUrl.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                val workerJson = JSONObject().apply {
                    put("id", System.currentTimeMillis())
                    put("name", name)
                    put("trade", skill)
                    put("dailyWage", dailyRate)
                    put("experienceYears", 3)
                    put("rating", 4.8)
                    put("reviewsCount", 12)
                    put("location", location)
                    put("distance", "1.2 km")
                    put("phone", phone)
                    put("isAvailableToday", true)
                    put("isVerified", true)
                }

                OutputStreamWriter(workerConn.outputStream).use { it.write(workerJson.toString()) }
                workerConn.responseCode
                workerConn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun verifyUserAndRoleFromFirebase(
        email: String,
        passwordInput: String,
        onResult: (isSuccess: Boolean, name: String?, phone: String?, isAdmin: Boolean, adminTier: String?, errorMsg: String?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val safeEmailKey = toSafeKey(email)
                val url = URL("$FIREBASE_DB_URL/users/$safeEmailKey.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                if (conn.responseCode in 200..299) {
                    val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    conn.disconnect()

                    if (responseText.isBlank() || responseText == "null") {
                        withContext(Dispatchers.Main) {
                            onResult(false, null, null, false, null, "Account nahi mila! Kripya pehle Sign Up karein.")
                        }
                        return@launch
                    }

                    val json = JSONObject(responseText)
                    val savedHash = json.optString("authVerifierHash", "")
                    val legacyPass = json.optString("password", "")
                    val savedName = json.optString("fullName", "Workora User")
                    val savedPhone = json.optString("phone", "")
                    val accountStatus = json.optString("accountStatus", "ACTIVE")

                    if (accountStatus == "BLOCKED" || accountStatus == "SUSPENDED") {
                        withContext(Dispatchers.Main) {
                            onResult(false, null, null, false, null, "Aapka account $accountStatus hai. Support se sampark karein.")
                        }
                        return@launch
                    }

                    val inputHash = sha256Hash(passwordInput)
                    val isPasswordMatch = (savedHash.isNotEmpty() && savedHash == inputHash) ||
                            (legacyPass.isNotEmpty() && legacyPass == passwordInput)

                    if (!isPasswordMatch) {
                        withContext(Dispatchers.Main) {
                            onResult(false, null, null, false, null, "Galat Password! Kripya sahi password dalein.")
                        }
                        return@launch
                    }

                    val adminUrl = URL("$FIREBASE_DB_URL/admins/$safeEmailKey.json")
                    val adminConn = (adminUrl.openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 6000
                        readTimeout = 6000
                    }

                    var isBackendAdmin = false
                    var detectedAdminTier = "SUPER_ADMIN"

                    if (adminConn.responseCode in 200..299) {
                        val adminText = BufferedReader(InputStreamReader(adminConn.inputStream)).use { it.readText() }
                        val parsed = parseAdminJsonStatus(adminText)
                        isBackendAdmin = parsed.first
                        detectedAdminTier = parsed.second
                    }
                    adminConn.disconnect()

                    if (isBackendAdmin) {
                        recordAdminAuditLog(
                            adminEmail = email,
                            adminTier = detectedAdminTier,
                            actionType = "ADMIN_LOGIN",
                            targetEntity = "AdminSession",
                            details = "Authorized Admin logged in verified via /admins node"
                        )
                    }

                    withContext(Dispatchers.Main) {
                        onResult(true, savedName, savedPhone, isBackendAdmin, detectedAdminTier, null)
                    }
                } else {
                    conn.disconnect()
                    withContext(Dispatchers.Main) { onResult(false, null, null, false, null, null) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onResult(false, null, null, false, null, null) }
            }
        }
    }

    fun verifyUserFromFirebase(
        email: String,
        passwordInput: String,
        onResult: (isSuccess: Boolean, name: String?, phone: String?, errorMsg: String?) -> Unit
    ) {
        verifyUserAndRoleFromFirebase(email, passwordInput) { isSuccess, name, phone, _, _, errorMsg ->
            onResult(isSuccess, name, phone, errorMsg)
        }
    }

    fun recordAdminAuditLog(
        adminEmail: String,
        adminTier: String,
        actionType: String,
        targetEntity: String,
        details: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logId = System.currentTimeMillis()
                val url = URL("$FIREBASE_DB_URL/audit_logs/log_$logId.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("logId", logId.toString())
                    put("adminEmail", adminEmail)
                    put("adminTier", adminTier)
                    put("actionType", actionType)
                    put("targetEntity", targetEntity)
                    put("details", details)
                    put("timestamp", logId)
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchLiveAdminMetrics(onResult: (LiveAdminMetrics, List<AdminAuditLog>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var totalUsers = 0
                var totalWorkers = 0
                var totalCustomers = 0
                var activeWorkers = 0
                var activeJobs = 0
                var completedJobs = 0
                var pendingRequests = 0

                val usersConn = (URL("$FIREBASE_DB_URL/users.json").openConnection() as HttpURLConnection)
                if (usersConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(usersConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val u = root.optJSONObject(keys.next()) ?: continue
                            totalUsers++
                            val r = u.optString("role", "USER")
                            if (r == "LABOUR") totalWorkers++ else totalCustomers++
                        }
                    }
                }
                usersConn.disconnect()

                val workersConn = (URL("$FIREBASE_DB_URL/workers.json").openConnection() as HttpURLConnection)
                if (workersConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(workersConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        var wCount = 0
                        while (keys.hasNext()) {
                            val w = root.optJSONObject(keys.next()) ?: continue
                            wCount++
                            if (w.optBoolean("isAvailableToday", true)) activeWorkers++
                        }
                        if (wCount > totalWorkers) totalWorkers = wCount
                    }
                }
                workersConn.disconnect()

                val jobsConn = (URL("$FIREBASE_DB_URL/jobs.json").openConnection() as HttpURLConnection)
                if (jobsConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(jobsConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val j = root.optJSONObject(keys.next()) ?: continue
                            when (j.optString("status", "PENDING")) {
                                "COMPLETED" -> completedJobs++
                                "PENDING" -> {
                                    pendingRequests++
                                    activeJobs++
                                }
                                else -> activeJobs++
                            }
                        }
                    }
                }
                jobsConn.disconnect()

                val logsList = mutableListOf<AdminAuditLog>()
                val logsConn = (URL("$FIREBASE_DB_URL/audit_logs.json").openConnection() as HttpURLConnection)
                if (logsConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(logsConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val l = root.optJSONObject(key) ?: continue
                            logsList.add(
                                AdminAuditLog(
                                    logId = l.optString("logId", key),
                                    adminEmail = l.optString("adminEmail", "admin"),
                                    adminTier = l.optString("adminTier", "SUPER_ADMIN"),
                                    actionType = l.optString("actionType", "ACTION"),
                                    targetEntity = l.optString("targetEntity", "System"),
                                    details = l.optString("details", ""),
                                    timestamp = l.optLong("timestamp", 0L)
                                )
                            )
                        }
                    }
                }
                logsConn.disconnect()

                withContext(Dispatchers.Main) {
                    onResult(
                        LiveAdminMetrics(
                            totalUsers = totalUsers,
                            totalWorkers = totalWorkers,
                            totalCustomers = totalCustomers,
                            activeWorkers = activeWorkers,
                            activeJobs = activeJobs,
                            completedJobs = completedJobs,
                            pendingRequests = pendingRequests,
                            reportsCount = 0
                        ),
                        logsList.sortedByDescending { it.timestamp }.take(15)
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(LiveAdminMetrics(), emptyList())
                }
            }
        }
    }

    fun postJobToFirebase(
        title: String,
        category: String,
        description: String,
        dailyRate: Int,
        location: String,
        workersNeeded: Int,
        urgency: String,
        customerName: String,
        customerPhone: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val jobId = System.currentTimeMillis()
                val url = URL("$FIREBASE_DB_URL/jobs/job_$jobId.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                val json = JSONObject().apply {
                    put("id", jobId)
                    put("title", title)
                    put("category", category)
                    put("description", description)
                    put("dailyRate", dailyRate)
                    put("location", location)
                    put("workersNeeded", workersNeeded)
                    put("urgency", urgency)
                    put("dateTime", "Today, Immediate")
                    put("customerName", customerName)
                    put("customerPhone", customerPhone)
                    put("status", "PENDING")
                    put("timestamp", jobId)
                }

                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                val success = conn.responseCode in 200..299
                conn.disconnect()

                withContext(Dispatchers.Main) { onComplete(success) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    fun fetchOnlineJobs(onJobsLoaded: (List<JobPost>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$FIREBASE_DB_URL/jobs.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                if (conn.responseCode in 200..299) {
                    val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    conn.disconnect()

                    val list = mutableListOf<JobPost>()
                    if (responseText.isNotBlank() && responseText != "null") {
                        val root = JSONObject(responseText)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = root.optJSONObject(key) ?: continue
                            list.add(
                                JobPost(
                                    id = obj.optLong("id", System.currentTimeMillis()),
                                    title = obj.optString("title", "Work Needed"),
                                    category = obj.optString("category", "General"),
                                    description = obj.optString("description", ""),
                                    dailyRate = obj.optInt("dailyRate", 500),
                                    location = obj.optString("location", "Silwani"),
                                    workersNeeded = obj.optInt("workersNeeded", 1),
                                    urgency = obj.optString("urgency", "Today"),
                                    dateTime = obj.optString("dateTime", "Today"),
                                    customerName = obj.optString("customerName", "Customer"),
                                    customerPhone = obj.optString("customerPhone", ""),
                                    status = obj.optString("status", "PENDING")
                                )
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        onJobsLoaded(list.sortedByDescending { it.id })
                    }
                } else {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendChatMessageToCloud(
        senderName: String,
        messageText: String,
        timeText: String,
        onDone: () -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val msgId = System.currentTimeMillis()
                val url = URL("$FIREBASE_DB_URL/chats/msg_$msgId.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }

                val json = JSONObject().apply {
                    put("id", msgId.toString())
                    put("senderName", senderName)
                    put("messageText", messageText)
                    put("timeText", timeText)
                    put("timestamp", msgId)
                }

                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
                withContext(Dispatchers.Main) { onDone() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onDone() }
            }
        }
    }

    fun fetchCloudChatMessages(currentUserName: String, onLoaded: (List<CloudChatMessage>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("$FIREBASE_DB_URL/chats.json")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                if (conn.responseCode in 200..299) {
                    val responseText = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    conn.disconnect()

                    val messages = mutableListOf<CloudChatMessage>()
                    if (responseText.isNotBlank() && responseText != "null") {
                        val root = JSONObject(responseText)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val obj = root.optJSONObject(key) ?: continue
                            val sender = obj.optString("senderName", "User")
                            messages.add(
                                CloudChatMessage(
                                    id = obj.optString("id", key),
                                    senderName = sender,
                                    messageText = obj.optString("messageText", ""),
                                    timeText = obj.optString("timeText", "Now"),
                                    isSentByMe = sender.equals(currentUserName, ignoreCase = true)
                                )
                            )
                        }
                    }
                    withContext(Dispatchers.Main) {
                        onLoaded(messages.sortedBy { it.id })
                    }
                } else {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
