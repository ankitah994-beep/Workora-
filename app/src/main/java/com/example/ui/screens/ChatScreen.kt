package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val BgColor = Color.White
private val IncomingBubbleBg = Color(0xFFF1F5F9)

data class ChatMessageData(
    val text: String,
    val isMe: Boolean,
    val timestamp: String,
    val isLocationCard: Boolean = false
)

@Composable
fun ChatScreen(
    otherPersonName: String = "Amit Kumar",
    otherPersonStatus: String = "Online",
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var messageInput by remember { mutableStateOf("") }
    
    // Initial messages matching the exact design reference image
    val messageList = remember {
        mutableStateListOf(
            ChatMessageData("Hello, I am interested in your job post. I have 3 years of experience in painting work.", false, "10:15 AM"),
            ChatMessageData("Great! Can you come tomorrow morning for a visit?", true, "10:25 AM"),
            ChatMessageData("Yes, I can come at 10 AM. Please share the location.", false, "10:22 AM"),
            ChatMessageData("Sure, I have shared the location in the job details.", true, "10:25 AM"),
            ChatMessageData("Raisen\nView Location", true, "10:25 AM", isLocationCard = true)
        )
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            messageList.add(ChatMessageData("📷 [Photo Shared]", true, "Just now"))
            Toast.makeText(context, "Photo Sent Successfully", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Exact Blue Header matching the screenshot
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(WorkoraBlue)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name & Status
            Column(modifier = Modifier.weight(1f)) {
                Text(text = otherPersonName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = otherPersonStatus, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
            }
            
            // Action Icons (Call, Video, More)
            IconButton(onClick = { Toast.makeText(context, "Calling $otherPersonName...", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { Toast.makeText(context, "Starting video call...", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            items(messageList) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isMe) Arrangement.End else Arrangement.Start
                ) {
                    if (msg.isLocationCard) {
                        // Location Preview Card matching screenshot style
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = WorkoraBlue),
                            modifier = Modifier.width(240.dp).clickable {
                                Toast.makeText(context, "Opening Raisen Location...", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Raisen", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = "View Location", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                }
                            }
                        }
                    } else {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (msg.isMe) WorkoraBlue else IncomingBubbleBg
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp).width(280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 14.sp,
                                    color = if (msg.isMe) Color.White else TextDark,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = msg.timestamp,
                                        fontSize = 10.sp,
                                        color = if (msg.isMe) Color.White.copy(alpha = 0.7f) else TextGray
                                    )
                                    if (msg.isMe) {
                                        Text(text = "✓✓", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Bottom Input Bar with Attachment and Send Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Attachment (Paperclip icon like screenshot)
            IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach File", tint = TextGray)
            }

            // Text Input Field
            OutlinedTextField(
                value = messageInput,
                onValueChange = { messageInput = it },
                placeholder = { Text("Type a message...", color = TextGray) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (messageInput.isNotBlank()) {
                            messageList.add(ChatMessageData(messageInput, true, "Just now"))
                            messageInput = ""
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderGray,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark,
                    focusedContainerColor = BgColor,
                    unfocusedContainerColor = BgColor
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (messageInput.isNotBlank()) {
                        messageList.add(ChatMessageData(messageInput, true, "Just now"))
                        messageInput = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(WorkoraBlue, CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}
