package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ChatNavyPrimary = Color(0xFF083D91)
private val ChatOrangeAccent = Color(0xFFFF8C00)
private val ChatBgLight = Color(0xFFF8FAFC)
private val ChatWhite = Color(0xFFFFFFFF)
private val ChatMainText = Color(0xFF0B2345)
private val ChatSecondaryText = Color(0xFF687280)
private val ChatBorder = Color(0xFFE5EAF0)
private val ChatSuccessGreen = Color(0xFF16A34A)

private const val CHAT_FB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

private data class ChatContactItem(
    val id: String,
    val name: String,
    val roleOrSkill: String,
    val phone: String,
    val area: String,
    val lastMessage: String,
    val time: String
)

private data class LiveChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val time: String,
    val isSentByMe: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val chatPrefs = remember { context.getSharedPreferences("workora_active_chat", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    val myUserName = remember {
        profilePrefs.getString("user_name", "")?.ifBlank { "Me" } ?: "Me"
    }

    // Read partner selected via the Message icon on Customer/Worker cards
    val initialPartnerId = remember { chatPrefs.getString("chat_partner_id", "") ?: "" }
    val initialPartnerName = remember { chatPrefs.getString("chat_partner_name", "") ?: "" }
    val initialPartnerRole = remember { chatPrefs.getString("chat_partner_role", "Verified Member") ?: "Verified Member" }
    val initialPartnerPhone = remember { chatPrefs.getString("chat_partner_phone", "+91 9826012345") ?: "+91 9826012345" }
    val initialPartnerArea = remember { chatPrefs.getString("chat_partner_area", "Local Area") ?: "Local Area" }

    var activePartner by remember {
        mutableStateOf(
            if (initialPartnerName.isNotBlank()) {
                ChatContactItem(
                    id = initialPartnerId.ifBlank { "chat_${initialPartnerName.lowercase(Locale.US).replace(" ", "_")}" },
                    name = initialPartnerName,
                    roleOrSkill = initialPartnerRole,
                    phone = initialPartnerPhone,
                    area = initialPartnerArea,
                    lastMessage = "Tap to start live chat",
                    time = "Online"
                )
            } else {
                null
            }
        )
    }

    val contactsList = remember {
        mutableStateListOf(
            ChatContactItem("w_sunil", "Sunil Kumar", "Mason", "+91 9826012345", "Sector 4, Silwani", "नमस्ते, काम कब से शुरू करना है?", "10:30 AM"),
            ChatContactItem("w_rajesh", "Rajesh Sharma", "Plumber", "+91 9755098765", "Civil Lines, Raisen", "पाइप फिटिंग का काम हो जाएगा।", "Yesterday"),
            ChatContactItem("w_aslam", "Mohammad Aslam", "Electrician", "+91 9926543210", "Main Market, Silwani", "मैं आज उपलब्ध हूँ।", "Yesterday"),
            ChatContactItem("req_1", "Amit Sharma", "Customer", "+91 9876543210", "Main Market, Silwani", "घर की वायरिंग ठीक करवानी है।", "Today")
        )
    }

    // Ensure the clicked partner is also in the conversation list
    LaunchedEffect(activePartner) {
        val p = activePartner
        if (p != null && contactsList.none { it.name.equals(p.name, ignoreCase = true) }) {
            contactsList.add(0, p)
        }
    }

    var messageInput by remember { mutableStateOf("") }
    val messagesList = remember { mutableStateListOf<LiveChatMessage>() }

    // Load messages for the currently open chat partner from Firebase
    LaunchedEffect(activePartner?.id) {
        val partner = activePartner ?: return@LaunchedEffect
        messagesList.clear()
        messagesList.add(
            LiveChatMessage(
                id = "welcome_1",
                senderName = partner.name,
                text = "नमस्ते! मैं ${partner.name} (${partner.roleOrSkill})। बताइए क्या काम है?",
                time = "Live",
                isSentByMe = false
            )
        )
        loadMessagesFromFirebase(partner.id, myUserName) { cloudMsgs ->
            if (cloudMsgs.isNotEmpty()) {
                messagesList.clear()
                messagesList.addAll(cloudMsgs)
            }
        }
    }

    val sendLiveMessage: (String) -> Unit = { rawText ->
        val clean = rawText.trim()
        val partner = activePartner
        if (clean.isNotEmpty() && partner != null) {
            val nowTime = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
            val newMsg = LiveChatMessage(
                id = "msg_${System.currentTimeMillis()}",
                senderName = myUserName,
                text = clean,
                time = nowTime,
                isSentByMe = true
            )
            messagesList.add(newMsg)
            messageInput = ""

            // Save to Firebase Realtime Database
            pushMessageToFirebase(
                roomId = partner.id,
                senderName = myUserName,
                text = clean,
                time = nowTime
            )
        }
    }

    // If no specific partner is open, show the All Chats list
    if (activePartner == null) {
        Scaffold(
            containerColor = ChatBgLight,
            topBar = {
                Surface(color = ChatNavyPrimary, shadowElevation = 4.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ChatWhite)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Workora Live Chat",
                                color = ChatWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Direct 0% Commission Messaging",
                                color = Color(0xFFCBD5E1),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(contactsList, key = { it.id }) { contact ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activePartner = contact },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ChatWhite),
                        border = BorderStroke(1.dp, ChatBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBEAFE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = contact.name.take(1).uppercase(Locale.US),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ChatNavyPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = contact.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ChatMainText
                                    )
                                    Text(
                                        text = contact.time,
                                        fontSize = 11.sp,
                                        color = ChatSuccessGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "${contact.roleOrSkill} • ${contact.area}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ChatNavyPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = contact.lastMessage,
                                    fontSize = 13.sp,
                                    color = ChatSecondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // Active 1-on-1 Live Chat Room with Selected Worker / Customer
    val partner = activePartner!!
    Scaffold(
        containerColor = ChatBgLight,
        topBar = {
            Surface(color = ChatNavyPrimary, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                chatPrefs.edit().remove("chat_partner_name").apply()
                                onBack()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ChatWhite)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(ChatOrangeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = partner.name.take(1).uppercase(Locale.US),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ChatWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = partner.name,
                                color = ChatWhite,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background( Color(0xFF4ADE80))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${partner.roleOrSkill} • Online",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Direct Call Button inside Live Chat Header
                        IconButton(
                            onClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${partner.phone}"))
                                context.startActivity(dialIntent)
                            }
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = ChatWhite)
                        }

                        // Switch to All Chats List
                        TextButton(
                            onClick = { activePartner = null }
                        ) {
                            Text("All Chats", color = ChatOrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChatWhite)
                    .navigationBarsPadding()
            ) {
                // Quick Reply Suggestions Bar
                val quickReplies = listOf(
                    "नमस्ते, क्या आप आज काम के लिए उपलब्ध हैं?",
                    "आपका डेली रेट क्या है?",
                    "अपना लोकेशन भेजें",
                    "मुझे अभी कॉल करें"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickReplies.forEach { reply ->
                        Surface(
                            color = ChatBgLight,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, ChatBorder),
                            modifier = Modifier.clickable { sendLiveMessage(reply) }
                        ) {
                            Text(
                                text = reply,
                                fontSize = 12.sp,
                                color = ChatNavyPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = ChatBorder)

                // Message Input Box + Send Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = {
                            Text(
                                text = "Type a message to ${partner.name}...",
                                fontSize = 14.sp,
                                color = ChatSecondaryText
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ChatBgLight,
                            unfocusedContainerColor = ChatBgLight,
                            focusedBorderColor = ChatNavyPrimary,
                            unfocusedBorderColor = ChatBorder,
                            focusedTextColor = ChatMainText,
                            unfocusedTextColor = ChatMainText
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    FloatingActionButton(
                        onClick = { sendLiveMessage(messageInput) },
                        containerColor = ChatOrangeAccent,
                        contentColor = ChatWhite,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send Message",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messagesList, key = { it.id }) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isSentByMe) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (msg.isSentByMe) ChatNavyPrimary else ChatWhite,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (msg.isSentByMe) 16.dp else 4.dp,
                            bottomEnd = if (msg.isSentByMe) 4.dp else 16.dp
                        ),
                        border = if (msg.isSentByMe) null else BorderStroke(1.dp, ChatBorder),
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 290.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = msg.text,
                                fontSize = 14.sp,
                                color = if (msg.isSentByMe) ChatWhite else ChatMainText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.time,
                                fontSize = 10.sp,
                                color = if (msg.isSentByMe) Color(0xFFCBD5E1) else ChatSecondaryText,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun loadMessagesFromFirebase(
    roomId: String,
    myUserName: String,
    onLoaded: (List<LiveChatMessage>) -> Unit
) {
    val cleanRoom = roomId.trim().lowercase(Locale.US).replace(" ", "_")
    Thread {
        val list = mutableListOf<LiveChatMessage>()
        try {
            val conn = URL("$CHAT_FB_URL/chats/$cleanRoom.json").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            if (conn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                    val root = JSONObject(resp)
                    val keys = root.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val obj = root.optJSONObject(k) ?: continue
                        val sender = obj.optString("senderName", "")
                        val text = obj.optString("text", "")
                        val time = obj.optString("time", "")
                        if (text.isNotBlank()) {
                            list.add(
                                LiveChatMessage(
                                    id = k,
                                    senderName = sender,
                                    text = text,
                                    time = time,
                                    isSentByMe = sender.equals(myUserName, ignoreCase = true)
                                )
                            )
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post {
            onLoaded(list)
        }
    }.start()
}

private fun pushMessageToFirebase(
    roomId: String,
    senderName: String,
    text: String,
    time: String
) {
    val cleanRoom = roomId.trim().lowercase(Locale.US).replace(" ", "_")
    Thread {
        try {
            val msgKey = "m_${System.currentTimeMillis()}"
            val url = URL("$CHAT_FB_URL/chats/$cleanRoom/$msgKey.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("senderName", senderName)
                put("text", text)
                put("time", time)
                put("timestamp", System.currentTimeMillis())
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}
