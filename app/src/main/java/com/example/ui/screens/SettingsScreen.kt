package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onBack: () -> Unit = {},
    onNavigateLanguage: () -> Unit = {},
    onNavigatePrivacy: () -> Unit = {},
    onNavigateTerms: () -> Unit = {},
    onNavigateHelp: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Fully functional states for interactive elements
    var notificationsEnabled by remember { mutableStateOf(true) }
    var currentLanguage by remember { mutableStateOf("English") }

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
            // Preferences Section
            Text(text = "Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    // Language Selection Row with functional action
                    SettingClickableRow(
                        icon = Icons.Default.Language,
                        title = "App Language",
                        subtitle = currentLanguage,
                        onClick = {
                            currentLanguage = if (currentLanguage == "English") "हिंदी" else "English"
                            Toast.makeText(context, "Language switched to $currentLanguage", Toast.LENGTH_SHORT).show()
                            onNavigateLanguage()
                        }
                    )
                    Divider(color = BorderGray, modifier = Modifier.padding(horizontal = 16.dp))
                    
                    // Functional Notification Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Push Notifications", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(text = if (notificationsEnabled) "Enabled" else "Disabled", fontSize = 12.sp, color = TextGray)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { 
                                notificationsEnabled = it
                                Toast.makeText(context, if (it) "Notifications Enabled" else "Notifications Disabled", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WorkoraBlue)
                        )
                    }
                }
            }

            // Security & Privacy Section
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Security & Privacy", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingClickableRow(
                        icon = Icons.Default.Lock,
                        title = "Privacy Policy",
                        subtitle = "Read our data policy",
                        onClick = {
                            Toast.makeText(context, "Opening Privacy Policy", Toast.LENGTH_SHORT).show()
                            onNavigatePrivacy()
                        }
                    )
                    Divider(color = BorderGray, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingClickableRow(
                        icon = Icons.Default.Security,
                        title = "Terms & Conditions",
                        subtitle = "Platform usage rules",
                        onClick = {
                            Toast.makeText(context, "Opening Terms & Conditions", Toast.LENGTH_SHORT).show()
                            onNavigateTerms()
                        }
                    )
                }
            }

            // About Section
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "About", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextGray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingClickableRow(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Help & Support",
                        subtitle = "Contact Workora team",
                        onClick = {
                            Toast.makeText(context, "Opening Help & Support", Toast.LENGTH_SHORT).show()
                            onNavigateHelp()
                        }
                    )
                    Divider(color = BorderGray, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingClickableRow(
                        icon = Icons.Default.Info,
                        title = "App Version",
                        subtitle = "v2.0 (Masterplan Build)",
                        onClick = {
                            Toast.makeText(context, "Workora v2.0 is up to date", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingClickableRow(
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
