package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WorkoraCloudChatMessage(
    val id: String,
    val senderName: String,
    val senderPhone: String,
    val senderRole: String,
    val text: String,
    val timeLabel: String,
    val timestamp: Long
)

object FirebaseManager {
    const val DATABASE_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

    // Checks admin authorization from Firebase /admins node (No hardcoded personal email)
    fun checkIfEmailIsAdminOnCloud(
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val clean = email.trim().lowercase()
        if (clean.isBlank()) {
            onResult(false, "NONE")
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            var isAdmin = false
            var tier = "NONE"
            try {
                val conn = URL("$DATABASE_URL/admins.json").openConnection() as HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                if (conn.responseCode in 200..299) {
                    val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (resp.isNotBlank() && resp != "null" && resp.lowercase().contains(clean)) {
                        isAdmin = true
                        tier = "SUPER_ADMIN"
                    }
                }
                conn.disconnect()
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) { onResult(isAdmin, tier) }
        }
    }

    fun checkIfEmailIsAdminOnCloud(
        context: Context,
        email: String,
        onResult: (Boolean, String) -> Unit
    ) {
        checkIfEmailIsAdminOnCloud(email, onResult)
    }

    fun postJobToFirebase(
        context: Context? = null,
        title: String = "Work Needed",
        category: String = "Mason",
        description: String = "",
        dailyRate: Int = 600,
        location: String = "Silwani, Raisen (MP)",
        workersNeeded: Int = 1,
        urgency: String = "Immediate",
        dateTime: String = "9:00 AM - 6:00 PM",
        customerName: String = "Customer",
        customerPhone: String = "",
        onSuccess: (Boolean) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var ok = false
            try {
                val newJobId = System.currentTimeMillis()
                val conn = (URL("$DATABASE_URL/jobs/job_$newJobId.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("id", newJobId)
                    put("title", title)
                    put("category", category)
                    put("description", description)
                    put("dailyRate", dailyRate)
                    put("location", location)
                    put("workersNeeded", workersNeeded)
                    put("urgency", urgency)
                    put("dateTime", dateTime)
                    put("customerName", customerName)
                    put("customerPhone", customerPhone)
                    put("status", "OPEN")
                    put("createdAt", newJobId)
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                ok = conn.responseCode in 200..299
                conn.disconnect()
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) { onSuccess(ok) }
        }
    }

    fun postJobToFirebase(job: JobPost, onSuccess: (Boolean) -> Unit = {}) {
        postJobToFirebase(
            title = job.title,
            category = job.category,
            description = job.description,
            dailyRate = job.dailyRate,
            location = job.location,
            workersNeeded = job.workersNeeded,
            urgency = job.urgency,
            dateTime = job.dateTime,
            customerName = job.customerName,
            customerPhone = job.customerPhone,
            onSuccess = onSuccess
        )
    }

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

    fun syncUserToFirebase(
        context: Context,
        name: String,
        email: String,
        phone: String,
        password: String,
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
                    put("status", "PENDING")
                    put("createdAt", System.currentTimeMillis())
                    put("updatedAt", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
                withContext(Dispatchers.Main) { onSuccess(bookingId) }
            } catch (_: Exception) {}
        }
    }

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
            withContext(Dispatchers.Main) { onComplete(success) }
        }
    }

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
                        OutputStreamWriter(outputStream).use {
                            it.write(JSONObject().apply {
                                put("blocked", true)
                                put("timestamp", System.currentTimeMillis())
                            }.toString())
                        }
                    } else {
                        requestMethod = "DELETE"
                    }
                    connectTimeout = 5000
                    readTimeout = 5000
                }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) { onDone() }
        }
    }

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
            withContext(Dispatchers.Main) { onDone(ok) }
        }
    }

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
                    put("status", "VERIFICATION_PENDING")
                    put("timestamp", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                ok = conn.responseCode in 200..299
                conn.disconnect()
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) { onDone(ok) }
        }
    }

    fun deleteAndAnonymizeAccount(userPhone: String, onDone: (Boolean) -> Unit) {
        val cleanPhone = userPhone.filter { it.isDigit() }.takeLast(10)
        CoroutineScope(Dispatchers.IO).launch {
            var ok = false
            try {
                val wConn = (URL("$DATABASE_URL/workers/w_$cleanPhone.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                }
                wConn.responseCode
                wConn.disconnect()

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
            withContext(Dispatchers.Main) { onDone(ok) }
        }
    }
}

@Composable
fun WorkoraLiveChatDialog(
    appLang: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }

    val myName = remember { profilePrefs.getString("user_name", "Workora User") ?: "Workora User" }
    val myPhone = remember { profilePrefs.getString("user_phone", "") ?: "" }
    val myRole = remember { authPrefs.getString("saved_user_role", "CUSTOMER") ?: "CUSTOMER" }
    val myLocation = remember { profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)" }

        val cardColor = Color.White
    val subtleBg = Color(0xFFF8FAFC)
    val textDark = Color.Black
    val textMuted = Color.Gray
    val borderCol = Color(0xFFE2E8F0)
    val accentBlue = Color(0xFF083D91)

    val deepNavy = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val greenTrusted = Color(0xFF22A06B)
    val isHindi = appLang == "Hindi"

    var messageInput by remember { mutableStateOf("") }
    var isSyncingChat by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf<WorkoraCloudChatMessage>(
            WorkoraCloudChatMessage(
                id = "welcome_msg",
                senderName = "Workora Support",
                senderPhone = "",
                senderRole = "SUPPORT",
                text = if (isHindi) {
                    "नमस्ते! Workora मैसेज में आपका स्वागत है। यहाँ से आप सीधे कारीगरों और ग्राहकों से लाइव चैट कर सकते हैं।"
                } else {
                    "Welcome to Workora Message! Chat directly with verified workers and customers."
                },
                timeLabel = "Live",
                timestamp = 1L
            )
        )
    }

    fun loadMessagesFromFirebase() {
        isSyncingChat = true
        CoroutineScope(Dispatchers.IO).launch {
            val fetched = mutableListOf<WorkoraCloudChatMessage>()
            try {
                val conn = URL("${FirebaseManager.DATABASE_URL}/messages.json").openConnection() as HttpURLConnection
                conn.connectTimeout = 4500
                conn.readTimeout = 4500
                if (conn.responseCode in 200..299) {
                    val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                        val root = JSONObject(resp)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val txt = obj.optString("text", "")
                            if (txt.isNotBlank()) {
                                fetched.add(
                                    WorkoraCloudChatMessage(
                                        id = k,
                                        senderName = obj.optString("senderName", "User"),
                                        senderPhone = obj.optString("senderPhone", ""),
                                        senderRole = obj.optString("senderRole", "USER"),
                                        text = txt,
                                        timeLabel = obj.optString("timeLabel", "Just now"),
                                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                                    )
                                )
                            }
                        }
                    }
                }
                conn.disconnect()
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                isSyncingChat = false
                if (fetched.isNotEmpty()) {
                    val sortedList = fetched.sortedBy { m: WorkoraCloudChatMessage -> m.timestamp }.takeLast(25)
                    for (msg in sortedList) {
                        if (chatMessages.none { existing: WorkoraCloudChatMessage -> existing.id == msg.id }) {
                            chatMessages.add(msg)
                        }
                    }
                }
            }
        }
    }

    fun sendMessageToFirebase(rawText: String) {
        val cleanText = WorkoraSecurityManager.sanitizeUserInput(rawText, 300)
        if (cleanText.isBlank()) return

        val now = System.currentTimeMillis()
        val timeFormatted = try {
            SimpleDateFormat("hh:mm a", Locale.US).format(Date(now))
        } catch (_: Exception) { "Now" }
        val msgKey = "msg_$now"

        val newMsg = WorkoraCloudChatMessage(
            id = msgKey,
            senderName = myName,
            senderPhone = myPhone,
            senderRole = myRole,
            text = cleanText,
            timeLabel = timeFormatted,
            timestamp = now
        )
        chatMessages.add(newMsg)
        messageInput = ""

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val conn = (URL("${FirebaseManager.DATABASE_URL}/messages/$msgKey.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 5000
                    readTimeout = 5000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("senderName", myName)
                    put("senderPhone", myPhone)
                    put("senderRole", myRole)
                    put("location", myLocation)
                    put("text", cleanText)
                    put("timeLabel", timeFormatted)
                    put("timestamp", now)
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) { loadMessagesFromFirebase() }

    val quickReplies = if (isHindi) {
        listOf("कल काम के लिए उपलब्ध हैं?", "दिहाड़ी ₹600/दिन पक्की है ✓", "काम की लोकेशन भेजें 📍")
    } else {
        listOf("Are you available tomorrow?", "₹600/day rate confirmed ✓", "Please share work location 📍")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(deepNavy), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(if (isHindi) "Workora लाइव मैसेज" else "Workora Live Message", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = textDark)
                        Text("🟢 Live Firebase Chat • $myLocation", fontSize = 11.sp, color = greenTrusted, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { loadMessagesFromFirebase() }, modifier = Modifier.size(32.dp)) {
                        if (isSyncingChat) CircularProgressIndicator(color = brandOrange, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                        else Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = accentBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = textDark)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(12.dp)).background(subtleBg).padding(8.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chatMessages.forEach { msg: WorkoraCloudChatMessage ->
                        val isMe = msg.senderName.equals(myName, ignoreCase = true)
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                            Card(
                                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = if (isMe) 12.dp else 2.dp, bottomEnd = if (isMe) 2.dp else 12.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isMe) deepNavy else cardColor),
                                border = BorderStroke(1.dp, if (isMe) deepNavy else borderCol)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (isMe) (if (isHindi) "आप ($myName)" else "You ($myName)") else msg.senderName, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = if (isMe) brandOrange else accentBlue)
                                        Text("• ${msg.timeLabel}", fontSize = 10.sp, color = if (isMe) Color.White.copy(alpha = 0.75f) else textMuted)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(msg.text, fontSize = 13.sp, color = if (isMe) Color.White else textDark, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickReplies.forEach { replyText ->
                        OutlinedButton(
                            onClick = { sendMessageToFirebase(replyText) },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, brandOrange),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(replyText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = brandOrange)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text(if (isHindi) "यहाँ मैसेज लिखें..." else "Type message...", fontSize = 13.sp) },
                        textStyle = TextStyle(color = textDark, fontSize = 13.sp),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    Button(
                        onClick = { sendMessageToFirebase(messageInput) },
                        enabled = messageInput.trim().isNotEmpty(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:18001200000")))
                    } catch (_: Exception) {}
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, accentBlue)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = accentBlue, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isHindi) "सपोर्ट कॉल" else "Support Call", color = accentBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    )
}
