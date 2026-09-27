package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CHAT_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

private data class ChatContactItem(
    val id: String,
    val name: String,
    val roleType: String, // "WORKER" or "CUSTOMER"
    val tradeSubtitle: String,
    val phone: String,
    val lastMessage: String,
    val timeLabel: String,
    val unreadCount: Int,
    val hasYellowHelmet: Boolean,
    val isFemaleAvatar: Boolean,
    val shirtColor: Color
)

private data class ChatMessageRow(
    val id: Long,
    val text: String,
    val time: String,
    val isSentByMe: Boolean,
    val dateHeader: String? = null
)

@Composable
private fun ChatAvatarCircle(
    hasYellowHelmet: Boolean,
    isFemale: Boolean,
    shirtColor: Color,
    size: Dp = 50.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFE2E8F0)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Hair back for female avatar
            if (isFemale) {
                drawCircle(
                    color = Color(0xFF27272A),
                    radius = w * 0.26f,
                    center = Offset(w * 0.50f, h * 0.44f)
                )
            }

            // Shoulders / Shirt
            drawArc(
                color = shirtColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.14f, h * 0.62f),
                size = Size(w * 0.72f, h * 0.54f)
            )

            // Neck
            drawRect(
                color = Color(0xFFE0A96D),
                topLeft = Offset(w * 0.42f, h * 0.52f),
                size = Size(w * 0.16f, h * 0.14f)
            )

            // Face
            drawCircle(
                color = Color(0xFFF1C27D),
                radius = w * 0.20f,
                center = Offset(w * 0.50f, h * 0.40f)
            )

            // Beard for male avatars
            if (!isFemale) {
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 10f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(w * 0.34f, h * 0.36f),
                    size = Size(w * 0.32f, h * 0.22f)
                )
            }

            if (hasYellowHelmet) {
                // Yellow Safety Hard Hat
                drawArc(
                    color = Color(0xFFFACC15),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.27f, h * 0.13f),
                    size = Size(w * 0.46f, h * 0.32f)
                )
                drawRoundRect(
                    color = Color(0xFFEAB308),
                    topLeft = Offset(w * 0.24f, h * 0.27f),
                    size = Size(w * 0.52f, h * 0.05f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            } else {
                // Dark Hair
                drawArc(
                    color = Color(0xFF18181B),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.29f, h * 0.16f),
                    size = Size(w * 0.42f, h * 0.26f)
                )
            }
        }
    }
}

@Composable
fun ChatScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val chatNavy = Color(0xFF083D91)
    val chatOrange = Color(0xFFFF8C00)
    val chatBg = Color(0xFFF4F7FB)

    // Filter tabs: 0 = All Chats, 1 = Workers, 2 = Customers
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    // Currently opened contact for full conversation screen (null = show Chat List)
    var activeContact by remember { mutableStateOf<ChatContactItem?>(null) }
    var messageInput by remember { mutableStateOf("") }

    // New Chat Dialog state
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactTrade by remember { mutableStateOf("Electrician") }
    var newContactPhone by remember { mutableStateOf("") }

    val contactsList = remember {
        mutableStateListOf(
            ChatContactItem(
                id = "c_ramesh",
                name = "Ramesh Kumar",
                roleType = "WORKER",
                tradeSubtitle = "Electrician",
                phone = "+91 9876543210",
                lastMessage = "Ok, I will come tomorrow at 9 AM.",
                timeLabel = "10:24 AM",
                unreadCount = 2,
                hasYellowHelmet = true,
                isFemaleAvatar = false,
                shirtColor = Color(0xFF0F172A)
            ),
            ChatContactItem(
                id = "c_suresh",
                name = "Suresh Yadav",
                roleType = "CUSTOMER",
                tradeSubtitle = "Customer • Silwani",
                phone = "+91 9123456780",
                lastMessage = "Is the work still available?",
                timeLabel = "Yesterday",
                unreadCount = 1,
                hasYellowHelmet = false,
                isFemaleAvatar = false,
                shirtColor = Color(0xFF1D4ED8)
            ),
            ChatContactItem(
                id = "c_mohan",
                name = "Mohan Verma",
                roleType = "WORKER",
                tradeSubtitle = "Mason (राजमिस्त्री)",
                phone = "+91 9988776655",
                lastMessage = "Yes, I can do it. ₹800/day.",
                timeLabel = "Yesterday",
                unreadCount = 0,
                hasYellowHelmet = true,
                isFemaleAvatar = false,
                shirtColor = Color(0xFF1E3A8A)
            ),
            ChatContactItem(
                id = "c_pooja",
                name = "Pooja Sharma",
                roleType = "CUSTOMER",
                tradeSubtitle = "Customer • Raisen",
                phone = "+91 9755443322",
                lastMessage = "Thanks for your help!",
                timeLabel = "2 Nov",
                unreadCount = 0,
                hasYellowHelmet = false,
                isFemaleAvatar = true,
                shirtColor = Color(0xFF334155)
            ),
            ChatContactItem(
                id = "c_vikash",
                name = "Vikash Singh",
                roleType = "CUSTOMER",
                tradeSubtitle = "Customer • Silwani",
                phone = "+91 9654321098",
                lastMessage = "Can you come tomorrow?",
                timeLabel = "1 Nov",
                unreadCount = 1,
                hasYellowHelmet = false,
                isFemaleAvatar = false,
                shirtColor = Color(0xFF1E40AF)
            ),
            ChatContactItem(
                id = "c_mahesh",
                name = "Mahesh Singh",
                roleType = "WORKER",
                tradeSubtitle = "Plumber",
                phone = "+91 9543210987",
                lastMessage = "Work confirm ho gaya hai.",
                timeLabel = "31 Oct",
                unreadCount = 0,
                hasYellowHelmet = true,
                isFemaleAvatar = false,
                shirtColor = Color(0xFF0F172A)
            ),
            ChatContactItem(
                id = "c_neha",
                name = "Neha Gupta",
                roleType = "CUSTOMER",
                tradeSubtitle = "Customer • Bhopal",
                phone = "+91 9432109876",
                lastMessage = "Great service!",
                timeLabel = "28 Oct",
                unreadCount = 0,
                hasYellowHelmet = false,
                isFemaleAvatar = true,
                shirtColor = Color(0xFF2563EB)
            ),
            ChatContactItem(
                id = "c_sanjay",
                name = "Sanjay Kumar",
                roleType = "CUSTOMER",
                tradeSubtitle = "Customer • Begamganj",
                phone = "+91 9321098765",
                lastMessage = "Thank you.",
                timeLabel = "25 Oct",
                unreadCount = 0,
                hasYellowHelmet = false,
                isFemaleAvatar = false,
                shirtColor = Color(0xFF1E3A8A)
            )
        )
    }

    // Conversation messages matching the exact right-side screenshot
    val conversationMessages = remember {
        mutableStateListOf(
            ChatMessageRow(
                id = 1L,
                text = "Hi Ramesh ji, are you available\nfor the work this week?",
                time = "09:15 AM",
                isSentByMe = true,
                dateHeader = "2 Nov 2024"
            ),
            ChatMessageRow(
                id = 2L,
                text = "Yes, I am available.\nWhich work do you need?",
                time = "09:28 AM",
                isSentByMe = false
            ),
            ChatMessageRow(
                id = 3L,
                text = "I need electrical fitting at my home.\nCan you come tomorrow?",
                time = "09:32 AM",
                isSentByMe = true
            ),
            ChatMessageRow(
                id = 4L,
                text = "Ok, I will come tomorrow\nat 9 AM. Shall I bring the tools?",
                time = "09:40 AM",
                isSentByMe = false
            ),
            ChatMessageRow(
                id = 5L,
                text = "Yes, please. Thanks!",
                time = "09:41 AM",
                isSentByMe = true
            ),
            ChatMessageRow(
                id = 6L,
                text = "I am on my way. Reaching in 15 minutes.",
                time = "08:45 AM",
                isSentByMe = false,
                dateHeader = "3 Nov 2024"
            ),
            ChatMessageRow(
                id = 7L,
                text = "Great, I'm waiting.",
                time = "08:47 AM",
                isSentByMe = true
            )
        )
    }

    val filteredContacts = contactsList.filter { item ->
        val matchesTab = when (selectedTab) {
            1 -> item.roleType == "WORKER"
            2 -> item.roleType == "CUSTOMER"
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.lastMessage.contains(searchQuery, ignoreCase = true) ||
                item.tradeSubtitle.contains(searchQuery, ignoreCase = true)
        matchesTab && matchesSearch
    }

    if (activeContact == null) {
        // ==================== LEFT SCREEN: ALL CHATS LIST VIEW ====================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(chatNavy)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // 1. Navy Header Bar: "Workora" + Search Icon + Compose New Chat Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(chatNavy)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Workora",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {},
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = { showNewChatDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "New Chat",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // 2. White Rounded Main Body Container
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(chatBg)
                ) {
                    // Top White Section with Search Box + 3 Tabs
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(top = 14.dp, start = 16.dp, end = 16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Search chats...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color(0xFF64748B)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE2E8F0),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tabs: All Chats | Workers | Customers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val tabs = listOf("All Chats", "Workers", "Customers")
                            tabs.forEachIndexed { idx, title ->
                                val isSelected = selectedTab == idx
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTab = idx }
                                        .padding(top = 6.dp)
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(
                                                if (isSelected) chatOrange else Color.Transparent,
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // 3. Scrollable List of Chat Contact Cards
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredContacts.forEach { contact ->
                            Card(
                                onClick = {
                                    val index = contactsList.indexOfFirst { it.id == contact.id }
                                    if (index >= 0) {
                                        contactsList[index] = contact.copy(unreadCount = 0)
                                    }
                                    activeContact = contact
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFEFF3F8)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ChatAvatarCircle(
                                        hasYellowHelmet = contact.hasYellowHelmet,
                                        isFemale = contact.isFemaleAvatar,
                                        shirtColor = contact.shirtColor,
                                        size = 52.dp
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = contact.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = contact.lastMessage,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF475569),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = contact.timeLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF64748B)
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (contact.unreadCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(chatOrange),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${contact.unreadCount}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                            }
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = Color(0xFF1E3A8A),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 4. Bottom Navigation Bar (Home, Search, Bookings, Profile)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onBack() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = chatNavy,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Home",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chatNavy
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { selectedTab = 1 }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Search",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onBack() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Bookings",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(23.dp)
                                )
                                Text(
                                    text = "Bookings",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onBack() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Profile",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // ==================== RIGHT SCREEN: INDIVIDUAL CONVERSATION VIEW ====================
        val partner = activeContact!!
        val scrollState = rememberScrollState()

        LaunchedEffect(conversationMessages.size) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(chatBg)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // 1. Navy Blue Top Header: Back Arrow + Avatar + Name & Trade + Call + 3-Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(chatNavy)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = { activeContact = null }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Chats",
                            tint = Color.White
                        )
                    }

                    ChatAvatarCircle(
                        hasYellowHelmet = partner.hasYellowHelmet,
                        isFemale = partner.isFemaleAvatar,
                        shirtColor = partner.shirtColor,
                        size = 46.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = partner.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = partner.tradeSubtitle,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            try {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${partner.phone}"))
                                context.startActivity(dialIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Call: ${partner.phone}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            Toast.makeText(context, "${partner.name} (${partner.phone})", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 2. Scrollable Messages Area with Date Pills & Bubbles
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                conversationMessages.forEach { msg ->
                    if (!msg.dateHeader.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE2E8F0), shape = RoundedCornerShape(14.dp))
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = msg.dateHeader,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    if (msg.isSentByMe) {
                        // Outgoing Message (Right-aligned Navy Blue Bubble with Double Tick)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 4.dp
                                ),
                                colors = CardDefaults.cardColors(containerColor = chatNavy),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.widthIn(max = 285.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        lineHeight = 19.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = msg.time,
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.DoneAll,
                                            contentDescription = "Read",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Incoming Message (Left-aligned Avatar + White Bubble)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Top
                        ) {
                            ChatAvatarCircle(
                                hasYellowHelmet = partner.hasYellowHelmet,
                                isFemale = partner.isFemaleAvatar,
                                shirtColor = partner.shirtColor,
                                size = 34.dp
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 16.dp,
                                    bottomEnd = 16.dp
                                ),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.widthIn(max = 275.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        fontSize = 14.sp,
                                        color = Color(0xFF0F172A),
                                        lineHeight = 19.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.time,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Bottom Message Input Bar: Paperclip + "Type a message..." + Emoji + Circular Navy Send Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        Toast.makeText(context, "Attach Work Photo / Location", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(22.dp)
                    )
                }

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = {
                        Text(
                            text = "Type a message...",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { messageInput += " 👍" }) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "Emoji",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    textStyle = TextStyle(color = Color(0xFF0F172A), fontSize = 14.sp),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE2E8F0),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(chatNavy)
                        .clickable {
                            val cleanText = messageInput.trim()
                            if (cleanText.isNotEmpty()) {
                                val nowTime = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
                                conversationMessages.add(
                                    ChatMessageRow(
                                        id = System.currentTimeMillis(),
                                        text = cleanText,
                                        time = nowTime,
                                        isSentByMe = true
                                    )
                                )
                                val idx = contactsList.indexOfFirst { it.id == partner.id }
                                if (idx >= 0) {
                                    contactsList[idx] = contactsList[idx].copy(
                                        lastMessage = cleanText,
                                        timeLabel = nowTime
                                    )
                                }
                                messageInput = ""

                                // Sync message to Firebase Realtime DB in background
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val msgId = "msg_${System.currentTimeMillis()}"
                                        val conn = (URL("$CHAT_DB_URL/chats/${partner.id}/$msgId.json").openConnection() as HttpURLConnection).apply {
                                            requestMethod = "PUT"
                                            setRequestProperty("Content-Type", "application/json")
                                            doOutput = true
                                        }
                                        val json = JSONObject().apply {
                                            put("sender", "Me")
                                            put("receiver", partner.name)
                                            put("text", cleanText)
                                            put("time", nowTime)
                                            put("timestamp", System.currentTimeMillis())
                                        }
                                        OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                                        conn.responseCode
                                        conn.disconnect()
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Dialog to start a New Chat
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = {
                Text(
                    text = "Start New Chat",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = chatNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("Worker or Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContactTrade,
                        onValueChange = { newContactTrade = it },
                        label = { Text("Skill / Role (e.g. Electrician, Mason)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContactPhone,
                        onValueChange = { newContactPhone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newContactName.isNotBlank()) {
                            val newItem = ChatContactItem(
                                id = "c_${System.currentTimeMillis()}",
                                name = newContactName.trim(),
                                roleType = "WORKER",
                                tradeSubtitle = newContactTrade.trim().ifBlank { "Worker" },
                                phone = newContactPhone.trim().ifBlank { "+91 9876543210" },
                                lastMessage = "Tap to start chatting...",
                                timeLabel = "Just now",
                                unreadCount = 0,
                                hasYellowHelmet = true,
                                isFemaleAvatar = false,
                                shirtColor = chatNavy
                            )
                            contactsList.add(0, newItem)
                            newContactName = ""
                            newContactPhone = ""
                            showNewChatDialog = false
                            activeContact = newItem
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = chatOrange)
                ) {
                    Text("Open Chat", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewChatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
