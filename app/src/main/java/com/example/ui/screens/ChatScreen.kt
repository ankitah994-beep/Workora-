package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CHAT_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

data class ChatContactPerson(
    val id: String,
    val name: String,
    val roleOrTrade: String,
    val phone: String,
    val lastMessage: String,
    val lastTime: String,
    val isOnline: Boolean = true,
    val unreadCount: Int = 0
)

data class DirectChatMessage(
    val id: String,
    val senderName: String,
    val text: String,
    val time: String,
    val isSentByMe: Boolean
)

@Composable
fun ChatScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }
    val chatStorePrefs = remember { context.getSharedPreferences("workora_whatsapp_chats", Context.MODE_PRIVATE) }

    val appLang = remember { settingsPrefs.getString("app_language", "Hinglish") ?: "Hinglish" }
    val myUserName = remember { profilePrefs.getString("user_name", "Ankit Ahirwar") ?: "Ankit Ahirwar" }

    fun tr(hi: String, hinglish: String, en: String): String {
        return when (appLang) {
            "Hindi" -> hi
            "English" -> en
            else -> hinglish
        }
    }

    var selectedContact by remember { mutableStateOf<ChatContactPerson?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }

    var newPersonName by remember { mutableStateOf("") }
    var newPersonRole by remember { mutableStateOf("Mistri / Worker") }
    var newPersonPhone by remember { mutableStateOf("+91 ") }

    val contactsList = remember { mutableStateListOf<ChatContactPerson>() }
    val activeMessages = remember { mutableStateListOf<DirectChatMessage>() }
    var messageInput by remember { mutableStateOf("") }

    fun loadMessagesForContact(contact: ChatContactPerson) {
        activeMessages.clear()
        val savedJson = chatStorePrefs.getString("msgs_${contact.id}", null)
        if (!savedJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(savedJson)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    activeMessages.add(
                        DirectChatMessage(
                            id = o.optString("id"),
                            senderName = o.optString("senderName"),
                            text = o.optString("text"),
                            time = o.optString("time"),
                            isSentByMe = o.optBoolean("isSentByMe", false)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Default realistic initial conversation for that contact
            activeMessages.add(
                DirectChatMessage(
                    id = "init_1",
                    senderName = contact.name,
                    text = tr(
                        "नमस्ते! मैं ${contact.name} (${contact.roleOrTrade}) आज काम के लिए उपलब्ध हूँ।",
                        "Namaste! Main ${contact.name} (${contact.roleOrTrade}) aaj kaam ke liye available hoon.",
                        "Hello! I am ${contact.name} (${contact.roleOrTrade}), available for work today."
                    ),
                    time = "09:00 AM",
                    isSentByMe = false
                )
            )
        }
    }

    fun saveMessagesForContact(contact: ChatContactPerson) {
        val arr = JSONArray()
        activeMessages.forEach { msg ->
            val o = JSONObject().apply {
                put("id", msg.id)
                put("senderName", msg.senderName)
                put("text", msg.text)
                put("time", msg.time)
                put("isSentByMe", msg.isSentByMe)
            }
            arr.put(o)
        }
        val lastMsg = activeMessages.lastOrNull()?.text ?: contact.lastMessage
        val lastTime = activeMessages.lastOrNull()?.time ?: contact.lastTime
        chatStorePrefs.edit()
            .putString("msgs_${contact.id}", arr.toString())
            .putString("last_msg_${contact.id}", lastMsg)
            .putString("last_time_${contact.id}", lastTime)
            .apply()

        val idx = contactsList.indexOfFirst { it.id == contact.id }
        if (idx >= 0) {
            contactsList[idx] = contactsList[idx].copy(lastMessage = lastMsg, lastTime = lastTime)
        }
    }

    fun loadWhatsAppStyleContacts() {
        val initialContacts = mutableListOf(
            ChatContactPerson(
                id = "admin_support",
                name = "Workora Official Support 🛡️",
                roleOrTrade = "24x7 Help & Live Broadcast Room",
                phone = "+91 6265798340",
                lastMessage = chatStorePrefs.getString("last_msg_admin_support", "Namaste! Kisi bhi sahayata ke liye message karein.") ?: "Namaste! Kisi bhi sahayata ke liye message karein.",
                lastTime = chatStorePrefs.getString("last_time_admin_support", "Online") ?: "Online",
                isOnline = true
            ),
            ChatContactPerson(
                id = "worker_sunil",
                name = "Sunil Kumar",
                roleOrTrade = "Mason (राजमिस्त्री) • Silwani",
                phone = "+91 9876543210",
                lastMessage = chatStorePrefs.getString("last_msg_worker_sunil", "Yes, I am available for wall plastering work.") ?: "Yes, I am available for wall plastering work.",
                lastTime = chatStorePrefs.getString("last_time_worker_sunil", "09:05 AM") ?: "09:05 AM",
                isOnline = true
            ),
            ChatContactPerson(
                id = "worker_ramesh",
                name = "Ramesh Vishwakarma",
                roleOrTrade = "Electrician (इलेक्ट्रीशियन) • Silwani",
                phone = "+91 9123456780",
                lastMessage = chatStorePrefs.getString("last_msg_worker_ramesh", "Bijli fitting aur motor ka kaam ho jayega.") ?: "Bijli fitting aur motor ka kaam ho jayega.",
                lastTime = chatStorePrefs.getString("last_time_worker_ramesh", "10:15 AM") ?: "10:15 AM",
                isOnline = true
            ),
            ChatContactPerson(
                id = "worker_mukesh",
                name = "Mukesh Ahirwar",
                roleOrTrade = "Painter & Putty Specialist • Raisen",
                phone = "+91 9988776655",
                lastMessage = chatStorePrefs.getString("last_msg_worker_mukesh", "Makan painting ke liye aaj aa sakta hoon.") ?: "Makan painting ke liye aaj aa sakta hoon.",
                lastTime = chatStorePrefs.getString("last_time_worker_mukesh", "Yesterday") ?: "Yesterday",
                isOnline = true
            ),
            ChatContactPerson(
                id = "worker_suresh",
                name = "Suresh Kushwaha",
                roleOrTrade = "Plumber & Pipe Fitting • Silwani",
                phone = "+91 9755443322",
                lastMessage = chatStorePrefs.getString("last_msg_worker_suresh", "Nal aur tanki fitting ke liye sampark karein.") ?: "Nal aur tanki fitting ke liye sampark karein.",
                lastTime = chatStorePrefs.getString("last_time_worker_suresh", "Yesterday") ?: "Yesterday",
                isOnline = false
            )
        )

        // Load custom contacts added via "+ New Chat"
        val savedCustomJson = chatStorePrefs.getString("custom_contacts_list", null)
        if (!savedCustomJson.isNullOrBlank()) {
            try {
                val arr = JSONArray(savedCustomJson)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val cid = o.optString("id")
                    initialContacts.add(
                        0,
                        ChatContactPerson(
                            id = cid,
                            name = o.optString("name"),
                            roleOrTrade = o.optString("role"),
                            phone = o.optString("phone"),
                            lastMessage = chatStorePrefs.getString("last_msg_$cid", "Tap to start chatting...") ?: "Tap to start chatting...",
                            lastTime = chatStorePrefs.getString("last_time_$cid", "New") ?: "New",
                            isOnline = true
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        contactsList.clear()
        contactsList.addAll(initialContacts)

        // Also fetch registered Firebase Users & Workers so you can chat with every real user!
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uConn = (URL("$CHAT_DB_URL/users.json").openConnection() as HttpURLConnection)
                val cloudPeople = mutableListOf<ChatContactPerson>()
                if (uConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(uConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val name = obj.optString("fullName", "")
                            val phone = obj.optString("phone", "+91 6265798340")
                            val role = obj.optString("role", "USER")
                            val loc = obj.optString("location", "Silwani")
                            if (name.isNotBlank() && !initialContacts.any { it.name.equals(name, true) }) {
                                val cid = "cloud_$k"
                                cloudPeople.add(
                                    ChatContactPerson(
                                        id = cid,
                                        name = name,
                                        roleOrTrade = "$role • $loc",
                                        phone = phone,
                                        lastMessage = chatStorePrefs.getString("last_msg_$cid", "Tap to message $name") ?: "Tap to message $name",
                                        lastTime = chatStorePrefs.getString("last_time_$cid", "Online") ?: "Online",
                                        isOnline = true
                                    )
                                )
                            }
                        }
                    }
                }
                uConn.disconnect()

                if (cloudPeople.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        cloudPeople.forEach { person ->
                            if (contactsList.none { it.id == person.id }) {
                                contactsList.add(person)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(Unit) {
        loadWhatsAppStyleContacts()
    }

    // ==================== SCREEN 1: INDIVIDUAL WHATSAPP CONVERSATION VIEW ====================
    if (selectedContact != null) {
        val currentPerson = selectedContact!!
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFEAE2)) // WhatsApp subtle warm chat background
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Top Conversation Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WorkoraNavy)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = { selectedContact = null },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Chats", tint = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(WorkoraOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentPerson.name.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentPerson.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "${currentPerson.roleOrTrade} • Online",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Direct Phone Call Button in Chat Header
                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentPerson.phone}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Phone: ${currentPerson.phone}", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = WorkoraOrange)
                }
            }

            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activeMessages) { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (msg.isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (msg.isSentByMe) 14.dp else 2.dp,
                                bottomEnd = if (msg.isSentByMe) 2.dp else 14.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isSentByMe) WorkoraNavy else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 14.sp,
                                    color = if (msg.isSentByMe) Color.White else WorkoraTextDark
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = msg.time,
                                    fontSize = 10.sp,
                                    color = if (msg.isSentByMe) Color.White.copy(alpha = 0.75f) else WorkoraTextMuted,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Reply Suggestions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    tr("क्या आप आज उपलब्ध हैं?", "Kya aap aaj available hain?", "Are you available today?"),
                    tr("दिहाड़ी क्या लगेगी?", "Dihadi kya lagegi?", "What is your daily rate?"),
                    tr("मुझे कॉल करें 📞", "Mujhe call karein 📞", "Please call me 📞")
                ).forEach { quickText ->
                    OutlinedButton(
                        onClick = { messageInput = quickText },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                        border = BorderStroke(1.dp, WorkoraOrange)
                    ) {
                        Text(quickText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                    }
                }
            }

            // Bottom Message Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = {
                        Text(
                            text = tr("मैसेज लिखें...", "Message likhein...", "Type a message..."),
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WorkoraOrange,
                        unfocusedBorderColor = WorkoraBorder
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val cleanText = messageInput.trim()
                        if (cleanText.isNotBlank()) {
                            val nowTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                            val myMsg = DirectChatMessage(
                                id = System.currentTimeMillis().toString(),
                                senderName = myUserName,
                                text = cleanText,
                                time = nowTime,
                                isSentByMe = true
                            )
                            activeMessages.add(myMsg)
                            messageInput = ""
                            saveMessagesForContact(currentPerson)

                            // Sync message to Firebase Cloud as well
                            FirebaseManager.sendChatMessageToCloud(
                                senderName = "$myUserName → ${currentPerson.name}",
                                messageText = cleanText,
                                timeText = nowTime
                            )
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WorkoraOrange)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
        return
    }

    // ==================== SCREEN 2: WHATSAPP STYLE ALL CHATS LIST + "+ NEW CHAT" ====================
    val filteredContacts = contactsList.filter {
        searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.roleOrTrade.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // WhatsApp Style Top App Bar
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
                            text = tr("Workora चैट्स (WhatsApp Style)", "Workora Chats (${contactsList.size})", "Workora Chats (${contactsList.size})"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = tr("किसी भी कारीगर या ग्राहक से सीधे बात करें", "Kisi bhi worker ya customer par tap karke chat karein", "Tap any worker or customer to chat"),
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showNewChatDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat", tint = WorkoraOrange)
                    }
                    IconButton(onClick = {
                        loadWhatsAppStyleContacts()
                        Toast.makeText(context, "Chats Refreshed ✓", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                }
            }

            // Search Contacts Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = tr("नाम, काम या नंबर से चैट खोजें...", "Naam, kaam ya number se chat khojein...", "Search chat by name, skill or phone..."),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = WorkoraOrange) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WorkoraOrange,
                        unfocusedBorderColor = WorkoraBorder
                    )
                )
            }

            // All Contacts / Conversations List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts) { person ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                loadMessagesForContact(person)
                                selectedContact = person
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, WorkoraBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar Circle with Online Green Dot
                            Box(modifier = Modifier.size(50.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(WorkoraNavy.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = person.name.take(1).uppercase(),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WorkoraNavy
                                    )
                                }
                                if (person.isOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(13.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                            .align(Alignment.BottomEnd)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = person.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WorkoraTextDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = person.lastTime,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkoraOrange
                                    )
                                }

                                Text(
                                    text = person.roleOrTrade,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkoraNavy
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = person.lastMessage,
                                    fontSize = 12.sp,
                                    color = WorkoraTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Floating "+ New Chat" Button (Just like WhatsApp!)
        ExtendedFloatingActionButton(
            onClick = { showNewChatDialog = true },
            containerColor = Color(0xFF16A34A),
            contentColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Chat", tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tr("+ नई चैट शुरू करें", "+ New Chat", "+ New Chat"),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }

    // "+ New Chat" Popup Dialog to start chatting with any person
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = {
                Text(
                    text = tr("नई चैट शुरू करें (+ New Chat)", "Start New Chat (+ Nayi Chat)", "Start New Chat"),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = WorkoraNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newPersonName,
                        onValueChange = { newPersonName = it },
                        label = { Text(tr("व्यक्ति का नाम (जैसे: राजू मिस्त्री)", "Person Name (e.g. Raju Mistri)", "Person Name")) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraOrange) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newPersonRole,
                        onValueChange = { newPersonRole = it },
                        label = { Text(tr("काम / रोल (जैसे: Electrician)", "Work / Role (e.g. Electrician)", "Skill / Role")) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newPersonPhone,
                        onValueChange = { newPersonPhone = it },
                        label = { Text(tr("मोबाइल नंबर", "Mobile Number", "Phone Number")) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = newPersonName.trim()
                        if (cleanName.isBlank()) {
                            Toast.makeText(context, "Kripya naam likhein!", Toast.LENGTH_SHORT).show()
                        } else {
                            val newId = "custom_${System.currentTimeMillis()}"
                            val newContact = ChatContactPerson(
                                id = newId,
                                name = cleanName,
                                roleOrTrade = newPersonRole.trim().ifBlank { "Worker / Customer" },
                                phone = newPersonPhone.trim().ifBlank { "+91 6265798340" },
                                lastMessage = "New chat started",
                                lastTime = "Just Now",
                                isOnline = true
                            )
                            contactsList.add(0, newContact)

                            // Save to custom contacts list permanently
                            val existingStr = chatStorePrefs.getString("custom_contacts_list", "[]") ?: "[]"
                            val arr = JSONArray(existingStr)
                            arr.put(JSONObject().apply {
                                put("id", newContact.id)
                                put("name", newContact.name)
                                put("role", newContact.roleOrTrade)
                                put("phone", newContact.phone)
                            })
                            chatStorePrefs.edit().putString("custom_contacts_list", arr.toString()).apply()

                            newPersonName = ""
                            showNewChatDialog = false
                            loadMessagesForContact(newContact)
                            selectedContact = newContact
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Text(tr("चैट खोलें ✓", "Start Chat ✓", "Start Chat ✓"), fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewChatDialog = false }) {
                    Text(tr("रद्द करें", "Cancel", "Cancel"))
                }
            }
        )
    }
}
