package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WorkoraTheme

// Dummy Data Class
data class NotificationItemData(
    val id: Int,
    val title: String,
    val description: String,
    val time: String,
    val isUnread: Boolean,
    val type: NotificationType
)

enum class NotificationType {
    SUCCESS, MESSAGE, SYSTEM
}

@Composable
fun NotificationScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onNotificationClick: (NotificationItemData) -> Unit = {} // NEW: Click listener add kiya
) {
    val colors = MaterialTheme.colorScheme

    val dummyNotifications = listOf(
        NotificationItemData(
            id = 1,
            title = "Job Accepted!",
            description = "Sunil Kumar has accepted your request for Wall Plastering.",
            time = "10 mins ago",
            isUnread = true,
            type = NotificationType.SUCCESS
        ),
        NotificationItemData(
            id = 2,
            title = "New Message",
            description = "Rajesh Sharma: I have reached the location.",
            time = "1 hour ago",
            isUnread = true,
            type = NotificationType.MESSAGE
        ),
        NotificationItemData(
            id = 3,
            title = "Profile Verified",
            description = "Your worker profile has been successfully verified by Admin.",
            time = "Yesterday",
            isUnread = false,
            type = NotificationType.SYSTEM
        ),
        NotificationItemData(
            id = 4,
            title = "Payment Received",
            description = "You received ₹850 for the Plastering job.",
            time = "2 days ago",
            isUnread = false,
            type = NotificationType.SUCCESS
        )
    )

    Column(
        modifier = modifier
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
                .padding(16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
            }
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyNotifications) { notification ->
                NotificationCard(
                    notification = notification,
                    onClick = { onNotificationClick(notification) } // Click pass kar diya
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItemData,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val (icon: ImageVector, iconTint: Color, iconBg: Color) = when (notification.type) {
        NotificationType.SUCCESS -> Triple(Icons.Default.CheckCircle, colors.primary, colors.primaryContainer)
        NotificationType.MESSAGE -> Triple(Icons.Default.Email, colors.secondary, colors.secondaryContainer)
        NotificationType.SYSTEM -> Triple(Icons.Default.Info, colors.tertiary, colors.tertiaryContainer)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Card par click enable kar diya
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isUnread) colors.surfaceVariant.copy(alpha = 0.5f) else colors.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isUnread) FontWeight.Bold else FontWeight.Medium,
                        color = colors.onSurface
                    )
                    if (notification.isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.error)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.outline
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationScreenPreview() {
    WorkoraTheme {
        NotificationScreen()
    }
}
