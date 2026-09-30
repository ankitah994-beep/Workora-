package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val BgColor = Color(0xFFF8FAFC)

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WorkoraBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        }

        // Body Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Settings Group
            Text(text = "Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingItemRow(icon = Icons.Default.Language, title = "App Language", subtitle = "English / हिंदी") {
                        Toast.makeText(context, "Language selection clicked", Toast.LENGTH_SHORT).show()
                    }
                    Divider(color = BorderGray, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingItemRow(icon = Icons.Default.Notifications, title = "Push Notifications", subtitle = "Manage alerts & reminders") {
                        Toast.makeText(context, "Notifications settings clicked", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Security & Privacy Group
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Security & Privacy", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingItemRow(icon = Icons.Default.Lock, title = "Privacy Policy", subtitle = "Read our data policy") {
                        Toast.makeText(context, "Privacy Policy clicked", Toast.LENGTH_SHORT).show()
                    }
                    Divider(color = BorderGray, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingItemRow(icon = Icons.Default.Security, title = "Terms & Conditions", subtitle = "Platform usage rules") {
                        Toast.makeText(context, "Terms clicked", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // About Group
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "About", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingItemRow(icon = Icons.AutoMirrored.Filled.Help, title = "Help & Support", subtitle = "Contact Workora team") {
                        Toast.makeText(context, "Help & Support clicked", Toast.LENGTH_SHORT).show()
                    }
                    Divider(color = BorderGray, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingItemRow(icon = Icons.Default.Info, title = "App Version", subtitle = "v2.0 (Masterplan Build)") {
                        Toast.makeText(context, "Workora v2.0 Up to date", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WorkoraBlue,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(text = subtitle, fontSize = 12.sp, color = TextGray)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(20.dp)
        )
    }
}
