package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

private const val NOTIF_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

data class LiveNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeText: String,
    val type: String, // "ADMIN", "JOB", "SYSTEM"
    val contactPhone: String = "+91 6265798340",
    val isRead: Boolean = false
)

@Composable
fun NotificationScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val readPrefs = remember { context.getSharedPreferences("workora_notif_state", Context.MODE_PRIVATE) }

    var isLoading by remember { mutableStateOf(true) }
    var selectedFilterTab by remember { mutableIntStateOf(0) }
    val notifications = remember { mutableStateListOf<LiveNotificationItem>() }
    var selectedNotification by remember { mutableStateOf<LiveNotificationItem?>(null) }

    fun isNotifRead(id: String): Boolean = readPrefs.getBoolean("read_$id", false)
    fun isNotifDeleted(id: String): Boolean = readPrefs.getBoolean("deleted_$id", false)

    fun markAsRead(id: String) {
        readPrefs.edit().putBoolean("read_$id", true).apply()
        val idx = notifications.indexOfFirst { it.id == id }
        if (idx >= 0) {
            notifications[idx] = notifications[idx].copy(isRead = true)
        }
    }

    fun deleteNotification(id: String) {
        readPrefs.edit().putBoolean("deleted_$id", true).apply()
        notifications.removeAll { it.id == id }
        Toast.makeText(context, "Notification Hata Diya Gaya ✓", Toast.LENGTH_SHORT).show()
    }

    fun markAllAsRead() {
        val editor = readPrefs.edit()
        for (i in notifications.indices) {
            val item = notifications[i]
            editor.putBoolean("read_${item.id}", true)
            notifications[i] = item.copy(isRead = true)
        }
        editor.apply()
        Toast.makeText(context, "Sabhi Notifications Read Mark Ho Gaye! ✓", Toast.LENGTH_SHORT).show()
    }

    fun loadLiveNotificationsFromFirebase() {
        isLoading = true
        CoroutineScope(Dispatchers.IO).launch {
            val loadedList = mutableListOf<LiveNotificationItem>()
            try {
                // 1. Fetch Admin Banner Announcement from app_branding
                var supportPhone = "+91 6265798340"
                val bConn = (URL("$NOTIF_DB_URL/app_branding.json").openConnection() as HttpURLConnection)
                if (bConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(bConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val obj = JSONObject(text)
                        supportPhone = obj.optString("supportPhone", "+91 6265798340")
                        val banner = obj.optString("bannerText", "")
                        val appName = obj.optString("appName", "WORKORA")
                        if (banner.isNotBlank() && !isNotifDeleted("banner_official")) {
                            loadedList.add(
                                LiveNotificationItem(
                                    id = "banner_official",
                                    title = "📢 $appName Official Notice",
                                    message = banner,
                                    timeText = "Pinned by Admin",
                                    type = "ADMIN",
                                    contactPhone = supportPhone,
                                    isRead = isNotifRead("banner_official")
                                )
                            )
                        }
                    }
                }
                bConn.disconnect()

                // 2. Fetch Admin Broadcasts & Live Chat Alerts
                val cConn = (URL("$NOTIF_DB_URL/chats.json").openConnection() as HttpURLConnection)
                if (cConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(cConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val id = obj.optString("id", k)
                            if (isNotifDeleted("chat_$id")) continue

                            val sender = obj.optString("senderName", "Workora")
                            val msg = obj.optString("messageText", "")
                            val time = obj.optString("timeText", "Recent")
                            val isAdminMsg = sender.contains("ADMIN", ignoreCase = true) || msg.startsWith("📢")

                            loadedList.add(
                                LiveNotificationItem(
                                    id = "chat_$id",
                                    title = if (isAdminMsg) "🛡️ $sender Broadcast" else "💬 New Message from $sender",
                                    message = msg,
                                    timeText = time,
                                    type = if (isAdminMsg) "ADMIN" else "SYSTEM",
                                    contactPhone = supportPhone,
                                    isRead = isNotifRead("chat_$id")
                                )
                            )
                        }
                    }
                }
                cConn.disconnect()

                // 3. Fetch Live Jobs as Job Alerts
                val jConn = (URL("$NOTIF_DB_URL/jobs.json").openConnection() as HttpURLConnection)
                if (jConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(jConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val jobId = obj.optLong("id", System.currentTimeMillis()).toString()
                            if (isNotifDeleted("job_$jobId")) continue

                            val title = obj.optString("title", "New Work Available")
                            val category = obj.optString("category", "General")
                            val wage = obj.optInt("dailyRate", 500)
                            val loc = obj.optString("location", "Silwani")
                            val customer = obj.optString("customerName", "Customer")
                            val phone = obj.optString("customerPhone", supportPhone).ifBlank { supportPhone }

                            loadedList.add(
                                LiveNotificationItem(
                                    id = "job_$jobId",
                                    title = "🛠️ Naya Kaam: $title",
                                    message = "$category • दिहाड़ी: ₹$wage/दिन • स्थान: $loc\nग्राहक: $customer ($phone)",
                                    timeText = "Live Job Alert",
                                    type = "JOB",
                                    contactPhone = phone,
                                    isRead = isNotifRead("job_$jobId")
                                )
                            )
                        }
                    }
                }
                jConn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Default Welcome Notification if list is empty
            if (loadedList.isEmpty() && !isNotifDeleted("welcome_default")) {
                loadedList.add(
                    LiveNotificationItem(
                        id = "welcome_default",
                        title = "🎉 Welcome to Workora App!",
                        message = "Aapka account सफलतापूर्वक जुड़ गया है। यहाँ आपको नए काम (Job Alerts) और Admin के सभी ज़रूरी संदेश लाइव मिलेंगे। किसी भी नोटिफिकेशन पर टैप करके पूरी जानकारी देखें।",
                        timeText = "Just Now",
                        type = "SYSTEM",
                        contactPhone = "+91 6265798340",
                        isRead = isNotifRead("welcome_default")
                    )
                )
            }

            withContext(Dispatchers.Main) {
                notifications.clear()
                notifications.addAll(loadedList.reversed())
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadLiveNotificationsFromFirebase()
    }

    val filteredList = notifications.filter { item ->
        when (selectedFilterTab) {
            1 -> !item.isRead
            2 -> item.type == "JOB"
            3 -> item.type == "ADMIN"
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WorkoraNavy)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Notifications (सूचनाएं)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "${notifications.count { !it.isRead }} Unread • Tap any card to open",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Refreshing Notifications...", Toast.LENGTH_SHORT).show()
                        loadLiveNotificationsFromFirebase()
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = WorkoraOrange)
                }
                IconButton(onClick = { markAllAsRead() }) {
                    Icon(Icons.Default.DoneAll, contentDescription = "Mark All Read", tint = Color.White)
                }
            }
        }

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                "All (${notifications.size})",
                "Unread (${notifications.count { !it.isRead }})",
                "🛠️ Job Alerts (${notifications.count { it.type == "JOB" }})",
                "🛡️ Admin Notices (${notifications.count { it.type == "ADMIN" }})"
            )
            tabs.forEachIndexed { idx, title ->
                val isSelected = selectedFilterTab == idx
                Button(
                    onClick = { selectedFilterTab = idx },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) WorkoraOrange else WorkoraBgLight
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else WorkoraNavy
                    )
                }
            }
        }

        // Notifications List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "Loading live notifications from Firebase...",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavy
                    )
                }
            } else if (filteredList.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = WorkoraOrange,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Koi Notification Nahi Hai",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upar Refresh (🔄) button dabayein ya Admin Panel se naya Broadcast bhejein.",
                            fontSize = 12.sp,
                            color = WorkoraTextMuted
                        )
                    }
                }
            } else {
                filteredList.forEach { item ->
                    val accentColor = when (item.type) {
                        "ADMIN" -> Color(0xFF16A34A)
                        "JOB" -> WorkoraOrange
                        else -> WorkoraNavy
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                markAsRead(item.id)
                                selectedNotification = item
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isRead) Color.White else Color(0xFFFFF8F1)
                        ),
                        border = BorderStroke(
                            width = if (item.isRead) 1.dp else 1.8.dp,
                            color = if (item.isRead) WorkoraBorder else accentColor
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isRead) 1.dp else 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (item.type) {
                                        "ADMIN" -> Icons.Default.VerifiedUser
                                        "JOB" -> Icons.Default.Build
                                        else -> Icons.Default.Notifications
                                    },
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WorkoraTextDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (!item.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(WorkoraOrange)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = item.message,
                                    fontSize = 13.sp,
                                    color = WorkoraTextDark,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🕒 ${item.timeText}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkoraTextMuted
                                    )
                                    Text(
                                        text = "Tap to Open Details →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Popup Dialog when user taps any notification!
    if (selectedNotification != null) {
        val current = selectedNotification!!
        AlertDialog(
            onDismissRequest = { selectedNotification = null },
            title = {
                Text(
                    text = current.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = current.message,
                        fontSize = 14.sp,
                        color = WorkoraTextDark,
                        lineHeight = 20.sp
                    )
                    Text(
                        text = "Time: ${current.timeText}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraTextMuted
                    )
                    if (current.contactPhone.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${current.contactPhone}"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Phone: ${current.contactPhone}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF16A34A))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Call Now (${current.contactPhone})",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedNotification = null },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraNavy)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Done ✓", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        deleteNotification(current.id)
                        selectedNotification = null
                    },
                    border = BorderStroke(1.dp, Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                }
            }
        )
    }
}
