package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraTheme

data class ChatMessage(val text: String, val isFromMe: Boolean, val time: String)

@Composable
fun ChatScreen(
    onBack: () -> Unit = {},
    chatUserName: String = "Sunil Kumar",
    chatUserRole: String = "Mason"
) {
    val colors = MaterialTheme.colorScheme
    var messageText by remember { mutableStateOf("") }

    // Dummy messages for UI preview
    val messages = listOf(
        ChatMessage("Hello, I need some plastering work done.", true, "10:00 AM"),
        ChatMessage("Hi! Yes, I am available. Where is the site?", false, "10:05 AM"),
        ChatMessage("It's in Sector 14, near the main market.", true, "10:06 AM"),
        ChatMessage("Okay, I can come tomorrow morning at 9 AM. My daily wage is ₹850.", false, "10:10 AM"),
        ChatMessage("That works for me. See you tomorrow.", true, "10:12 AM")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onSurface)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = colors.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = chatUserName, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold, 
                    color = colors.onSurface
                )
                Text(
                    text = chatUserRole, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = colors.onSurfaceVariant
                )
            }
        }

        Divider(color = colors.outlineVariant)

        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }
        }

        // Message Input Bar
        Surface(
            color = colors.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { 
                        // Message send logic will go here
                        messageText = "" 
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.primary, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Send, 
                        contentDescription = "Send", 
                        tint = colors.onPrimary, 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val colors = MaterialTheme.colorScheme
    val alignment = if (message.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isFromMe) colors.primary else colors.secondaryContainer
    val textColor = if (message.isFromMe) colors.onPrimary else colors.onSecondaryContainer
    
    // Changing corner radius based on sender
    val shape = if (message.isFromMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start) {
            Box(
                modifier = Modifier
                    .background(bubbleColor, shape)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message.time,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    WorkoraTheme {
        ChatScreen()
    }
}
