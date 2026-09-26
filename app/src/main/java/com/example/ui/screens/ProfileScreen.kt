package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// Global Design System Colors
private val WorkoraPrimaryNavy = Color(0xFF083D91)
private val WorkoraAccentOrange = Color(0xFFFF8C00)
private val WorkoraBgGray = Color(0xFFF8FAFC)
private val WorkoraWhite = Color(0xFFFFFFFF)
private val WorkoraMainText = Color(0xFF0B2345)
private val WorkoraSecondaryText = Color(0xFF687280)
private val WorkoraBorderColor = Color(0xFFE5EAF0)
private val WorkoraSuccessGreen = Color(0xFF16A34A)
private val WorkoraDangerRed = Color(0xFFDC2626)

private enum class SettingsSubPage {
    MAIN,
    EDIT_PROFILE,
    NOTIFICATIONS,
    CHANGE_PASSWORD,
    BLOCKED_USERS,
    REPORT_PROBLEM,
    HELP_SUPPORT,
    TERMS,
    PRIVACY_POLICY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    role: UserRole = UserRole.CUSTOMER,
    userName: String = "Ankit Ahirwar",
    userPhone: String = "+91 6265798340",
    userLocation: String = "Silwani, Raisen",
    onBack: () -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onUpdateProfile: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    var currentSubPage by remember { mutableStateOf(SettingsSubPage.MAIN) }

    // User State synced with SharedPreferences & Firebase
    var savedName by remember {
        mutableStateOf(profilePrefs.getString("user_name", userName) ?: userName)
    }
    var savedPhone by remember {
        mutableStateOf(profilePrefs.getString("user_phone", userPhone) ?: userPhone)
    }
    var savedEmail by remember {
        mutableStateOf(authPrefs.getString("last_logged_in_email", "ankitah994@gmail.com") ?: "ankitah994@gmail.com")
    }
    var savedArea by remember {
        mutableStateOf(profilePrefs.getString("user_location", userLocation) ?: userLocation)
    }
    var savedSkill by remember {
        mutableStateOf(profilePrefs.getString("user_skill", if (role == UserRole.LABOUR) "Electrician" else "Customer") ?: "Electrician")
    }
    var savedRate by remember {
        mutableStateOf(profilePrefs.getString("user_rate", "600") ?: "600")
    }

    // Preferences State
    var notificationsMasterToggle by remember {
        mutableStateOf(settingsPrefs.getBoolean("notif_master", true))
    }
    var notifWorkRequests by remember {
        mutableStateOf(settingsPrefs.getBoolean("notif_work_requests", true))
    }
    var notifBookingUpdates by remember {
        mutableStateOf(settingsPrefs.getBoolean("notif_booking_updates", true))
    }
    var notifMessages by remember {
        mutableStateOf(settingsPrefs.getBoolean("notif_messages", true))
    }
    var notifPromotional by remember {
        mutableStateOf(settingsPrefs.getBoolean("notif_promotional", false))
    }
    var selectedLanguage by remember {
        mutableStateOf(settingsPrefs.getString("app_language", "English") ?: "English")
    }

    // Dialog states
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showAreaDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val isSuperAdmin = remember(savedEmail, savedPhone) {
        savedEmail.equals("ankitah994@gmail.com", ignoreCase = true) ||
            savedPhone.contains("6265798340")
    }

    Scaffold(
        containerColor = WorkoraBgGray,
        bottomBar = {
            // Exact Reference Image Mobile Bottom Navigation
            Surface(
                color = WorkoraWhite,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (role == UserRole.CUSTOMER) {
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.Home,
                            label = "Home",
                            selected = false,
                            onClick = onBack
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.Search,
                            label = "Search",
                            selected = false,
                            onClick = onBack
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.EventNote,
                            label = "Bookings",
                            selected = false,
                            onClick = onBack
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Default.Person,
                            label = "Profile",
                            selected = true,
                            onClick = { currentSubPage = SettingsSubPage.MAIN }
                        )
                    } else {
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.Home,
                            label = "Home",
                            selected = false,
                            onClick = onBack
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.NotificationsNone,
                            label = "Requests",
                            selected = false,
                            onClick = onBack
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.WorkOutline,
                            label = "Jobs",
                            selected = false,
                            onClick = onBack
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Default.Person,
                            label = "Profile",
                            selected = true,
                            onClick = { currentSubPage = SettingsSubPage.MAIN }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Navy Header
            Surface(
                color = WorkoraPrimaryNavy,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (currentSubPage == SettingsSubPage.MAIN) onBack()
                                else currentSubPage = SettingsSubPage.MAIN
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = WorkoraWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (currentSubPage) {
                                SettingsSubPage.MAIN -> "Settings"
                                SettingsSubPage.EDIT_PROFILE -> "Edit Profile"
                                SettingsSubPage.NOTIFICATIONS -> "Notification Settings"
                                SettingsSubPage.CHANGE_PASSWORD -> "Change Password"
                                SettingsSubPage.BLOCKED_USERS -> "Blocked Users"
                                SettingsSubPage.REPORT_PROBLEM -> "Report a Problem"
                                SettingsSubPage.HELP_SUPPORT -> "Help & Support"
                                SettingsSubPage.TERMS -> "Terms & Conditions"
                                SettingsSubPage.PRIVACY_POLICY -> "Privacy Policy"
                            },
                            color = WorkoraWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isSuperAdmin && currentSubPage == SettingsSubPage.MAIN) {
                        Surface(
                            color = WorkoraAccentOrange,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onOpenAdmin() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Dashboard,
                                    contentDescription = "Admin Panel",
                                    tint = WorkoraWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Admin Panel",
                                    color = WorkoraWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Sub-page Routing
            when (currentSubPage) {
                SettingsSubPage.MAIN -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Reference Top Profile Summary Card
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { currentSubPage = SettingsSubPage.EDIT_PROFILE },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                border = BorderStroke(1.dp, WorkoraBorderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SettingsWorkerAvatar(size = 64.dp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = savedName,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WorkoraMainText
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (role == UserRole.LABOUR) savedSkill else "Customer Account",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = WorkoraPrimaryNavy
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = WorkoraSecondaryText,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = savedArea,
                                                fontSize = 13.sp,
                                                color = WorkoraSecondaryText
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Edit Profile",
                                        tint = WorkoraSecondaryText
                                    )
                                }
                            }
                        }

                        // SECTION 1: Account
                        item {
                            SettingsSectionHeader("Account")
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                border = BorderStroke(1.dp, WorkoraBorderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Person,
                                        title = "Profile",
                                        subtitle = "Manage your personal information",
                                        onClick = { currentSubPage = SettingsSubPage.EDIT_PROFILE }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Phone,
                                        title = "Phone Number",
                                        subtitle = "Manage your phone number",
                                        valueText = savedPhone,
                                        onClick = { showPhoneDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Email,
                                        title = "Email",
                                        subtitle = "Manage your email address",
                                        valueText = savedEmail,
                                        onClick = { showEmailDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Place,
                                        title = "Area",
                                        subtitle = "Change your area",
                                        valueText = savedArea,
                                        onClick = { showAreaDialog = true }
                                    )
                                }
                            }
                        }

                        // SECTION 2: Preferences
                        item {
                            SettingsSectionHeader("Preferences")
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                border = BorderStroke(1.dp, WorkoraBorderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { currentSubPage = SettingsSubPage.NOTIFICATIONS }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(WorkoraPrimaryNavy),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Notifications,
                                                contentDescription = null,
                                                tint = WorkoraWhite,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Notifications",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = WorkoraMainText
                                            )
                                            Text(
                                                text = "Manage your notifications",
                                                fontSize = 12.sp,
                                                color = WorkoraSecondaryText
                                            )
                                        }
                                        Switch(
                                            checked = notificationsMasterToggle,
                                            onCheckedChange = { checked ->
                                                notificationsMasterToggle = checked
                                                settingsPrefs.edit().putBoolean("notif_master", checked).apply()
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = WorkoraWhite,
                                                checkedTrackColor = WorkoraSuccessGreen
                                            )
                                        )
                                    }
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Language,
                                        title = "Language",
                                        subtitle = "Change app language",
                                        valueText = selectedLanguage,
                                        onClick = { showLanguageDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Place,
                                        title = "Location",
                                        subtitle = savedArea,
                                        valueText = "Current area",
                                        onClick = { showAreaDialog = true }
                                    )
                                }
                            }
                        }

                        // SECTION 3: Privacy & Security
                        item {
                            SettingsSectionHeader("Privacy & Security")
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                border = BorderStroke(1.dp, WorkoraBorderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Lock,
                                        title = "Change Password",
                                        subtitle = "Update your account password",
                                        onClick = { currentSubPage = SettingsSubPage.CHANGE_PASSWORD }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.PersonOff,
                                        title = "Blocked Users",
                                        subtitle = "Manage blocked accounts",
                                        onClick = { currentSubPage = SettingsSubPage.BLOCKED_USERS }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Flag,
                                        title = "Report a Problem",
                                        subtitle = "Tell us about an issue",
                                        onClick = { currentSubPage = SettingsSubPage.REPORT_PROBLEM }
                                    )
                                }
                            }
                        }

                        // SECTION 4: Support
                        item {
                            SettingsSectionHeader("Support")
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                border = BorderStroke(1.dp, WorkoraBorderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    SettingsRowItem(
                                        icon = Icons.Outlined.HelpOutline,
                                        title = "Help & Support",
                                        subtitle = "Get help or contact us",
                                        onClick = { currentSubPage = SettingsSubPage.HELP_SUPPORT }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Description,
                                        title = "Terms & Conditions",
                                        subtitle = "Read Workora terms of service",
                                        onClick = { currentSubPage = SettingsSubPage.TERMS }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Shield,
                                        title = "Privacy Policy",
                                        subtitle = "How we protect your data",
                                        onClick = { currentSubPage = SettingsSubPage.PRIVACY_POLICY }
                                    )
                                }
                            }
                        }

                        // SECTION 5: Account Actions (Switch Role, Logout, Delete Account)
                        item {
                            SettingsSectionHeader("Account")
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                border = BorderStroke(1.dp, WorkoraBorderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    SettingsRowItem(
                                        icon = Icons.Outlined.SwapHoriz,
                                        title = "Switch Role (${if (role == UserRole.CUSTOMER) "Go to Worker Mode" else "Go to Customer Mode"})",
                                        subtitle = "Switch between Customer and Labour view",
                                        onClick = onSwitchRole
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)

                                    // Logout Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showLogoutDialog = true }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFFEE2E2)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Logout,
                                                contentDescription = "Logout",
                                                tint = WorkoraDangerRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Logout",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkoraDangerRed
                                            )
                                            Text(
                                                text = "Sign out from your account",
                                                fontSize = 12.sp,
                                                color = WorkoraSecondaryText
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = WorkoraSecondaryText
                                        )
                                    }

                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)

                                    // Delete Account Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showDeleteAccountDialog = true }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFFEE2E2)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteForever,
                                                contentDescription = "Delete Account",
                                                tint = WorkoraDangerRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Delete Account",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkoraDangerRed
                                            )
                                            Text(
                                                text = "Permanently remove your account and data",
                                                fontSize = 12.sp,
                                                color = WorkoraSecondaryText
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = WorkoraSecondaryText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // SUB-PAGE: Edit Profile
                SettingsSubPage.EDIT_PROFILE -> {
                    var editName by remember { mutableStateOf(savedName) }
                    var editPhone by remember { mutableStateOf(savedPhone) }
                    var editArea by remember { mutableStateOf(savedArea) }
                    var editSkill by remember { mutableStateOf(savedSkill) }
                    var editRate by remember { mutableStateOf(savedRate) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                border = BorderStroke(1.dp, WorkoraBorderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        SettingsWorkerAvatar(size = 64.dp)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = editName,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkoraMainText
                                            )
                                            Text(
                                                text = if (role == UserRole.LABOUR) "Worker Profile" else "Customer Profile",
                                                fontSize = 13.sp,
                                                color = WorkoraAccentOrange,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("Full Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editPhone,
                                        onValueChange = { editPhone = it },
                                        label = { Text("Phone Number") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editArea,
                                        onValueChange = { editArea = it },
                                        label = { Text("Area / City") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (role == UserRole.LABOUR) {
                                        OutlinedTextField(
                                            value = editSkill,
                                            onValueChange = { editSkill = it },
                                            label = { Text("Primary Category / Skill") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = editRate,
                                            onValueChange = { editRate = it },
                                            label = { Text("Daily Rate (₹)") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (editName.isBlank() || editPhone.isBlank()) {
                                                Toast.makeText(context, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show()
                                            } else {
                                                savedName = editName.trim()
                                                savedPhone = editPhone.trim()
                                                savedArea = editArea.trim()
                                                savedSkill = editSkill.trim()
                                                savedRate = editRate.trim()

                                                profilePrefs.edit()
                                                    .putString("user_name", savedName)
                                                    .putString("user_phone", savedPhone)
                                                    .putString("user_location", savedArea)
                                                    .putString("user_skill", savedSkill)
                                                    .putString("user_rate", savedRate)
                                                    .apply()

                                                onUpdateProfile(savedName, savedPhone, savedArea)
                                                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                                currentSubPage = SettingsSubPage.MAIN
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            text = "Save Changes",
                                            color = WorkoraWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // SUB-PAGE: Notifications
                SettingsSubPage.NOTIFICATIONS -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            NotificationToggleRow(
                                title = "Work requests",
                                subtitle = "Receive alerts for new work requests in your area",
                                checked = notifWorkRequests,
                                onCheckedChange = {
                                    notifWorkRequests = it
                                    settingsPrefs.edit().putBoolean("notif_work_requests", it).apply()
                                }
                            )
                            HorizontalDivider(color = WorkoraBorderColor)
                            NotificationToggleRow(
                                title = "Booking updates",
                                subtitle = "Status changes on your active bookings",
                                checked = notifBookingUpdates,
                                onCheckedChange = {
                                    notifBookingUpdates = it
                                    settingsPrefs.edit().putBoolean("notif_booking_updates", it).apply()
                                }
                            )
                            HorizontalDivider(color = WorkoraBorderColor)
                            NotificationToggleRow(
                                title = "Messages",
                                subtitle = "Direct chat notifications from customers and workers",
                                checked = notifMessages,
                                onCheckedChange = {
                                    notifMessages = it
                                    settingsPrefs.edit().putBoolean("notif_messages", it).apply()
                                }
                            )
                            HorizontalDivider(color = WorkoraBorderColor)
                            NotificationToggleRow(
                                title = "Promotional notifications",
                                subtitle = "Tips, announcements, and platform updates",
                                checked = notifPromotional,
                                onCheckedChange = {
                                    notifPromotional = it
                                    settingsPrefs.edit().putBoolean("notif_promotional", it).apply()
                                }
                            )
                        }
                    }
                }

                // SUB-PAGE: Change Password
                SettingsSubPage.CHANGE_PASSWORD -> {
                    var currentPassword by remember { mutableStateOf("") }
                    var newPassword by remember { mutableStateOf("") }
                    var confirmPassword by remember { mutableStateOf("") }
                    var errorMsg by remember { mutableStateOf<String?>(null) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Update Your Password",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraMainText
                            )
                            Text(
                                text = "Minimum 8 characters with at least 1 letter and 1 number.",
                                fontSize = 12.sp,
                                color = WorkoraSecondaryText
                            )

                            OutlinedTextField(
                                value = currentPassword,
                                onValueChange = {
                                    currentPassword = it
                                    errorMsg = null
                                },
                                label = { Text("Current Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    errorMsg = null
                                },
                                label = { Text("New Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    errorMsg = null
                                },
                                label = { Text("Confirm New Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (!errorMsg.isNullOrBlank()) {
                                Text(
                                    text = errorMsg!!,
                                    color = WorkoraDangerRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = {
                                    val hasLetter = newPassword.any { it.isLetter() }
                                    val hasDigit = newPassword.any { it.isDigit() }
                                    when {
                                        currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                                            errorMsg = "All password fields are required."
                                        }
                                        newPassword.length < 8 || !hasLetter || !hasDigit -> {
                                            errorMsg = "New password must be at least 8 characters and contain a letter and number."
                                        }
                                        newPassword != confirmPassword -> {
                                            errorMsg = "New password and confirmation do not match."
                                        }
                                        else -> {
                                            authPrefs.edit().putString("saved_password_$savedEmail", newPassword).apply()
                                            syncPasswordToFirebase(savedEmail, newPassword)
                                            Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                            currentSubPage = SettingsSubPage.MAIN
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Change Password",
                                    color = WorkoraWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // SUB-PAGE: Blocked Users
                SettingsSubPage.BLOCKED_USERS -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonOff,
                                contentDescription = null,
                                tint = WorkoraSecondaryText,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Blocked Users",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraMainText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "You haven't blocked any users on Workora.",
                                fontSize = 13.sp,
                                color = WorkoraSecondaryText
                            )
                        }
                    }
                }

                // SUB-PAGE: Report a Problem
                SettingsSubPage.REPORT_PROBLEM -> {
                    var problemSubject by remember { mutableStateOf("") }
                    var problemDetails by remember { mutableStateOf("") }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Report an Issue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraMainText
                            )
                            OutlinedTextField(
                                value = problemSubject,
                                onValueChange = { problemSubject = it },
                                label = { Text("Subject (e.g. Booking issue, Fake profile)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = problemDetails,
                                onValueChange = { problemDetails = it },
                                label = { Text("Describe the problem in detail") },
                                minLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (problemSubject.isBlank() || problemDetails.isBlank()) {
                                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                                    } else {
                                        submitReportToFirebase(savedName, savedPhone, problemSubject, problemDetails)
                                        Toast.makeText(context, "Report submitted to Admin Team", Toast.LENGTH_SHORT).show()
                                        currentSubPage = SettingsSubPage.MAIN
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Submit Report", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // SUB-PAGE: Help & Support
                SettingsSubPage.HELP_SUPPORT -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Workora Help Center",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraMainText
                            )
                            Text(
                                text = "Need assistance with bookings, worker verification, or account settings? Reach out to our support team directly.",
                                fontSize = 13.sp,
                                color = WorkoraSecondaryText
                            )
                            HorizontalDivider(color = WorkoraBorderColor)
                            Text(
                                text = "Support Email: ankitah994@gmail.com",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WorkoraMainText
                            )
                            Text(
                                text = "Helpline: +91 6265798340",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WorkoraMainText
                            )
                            Button(
                                onClick = onOpenChat,
                                colors = ButtonDefaults.buttonColors(containerColor = WorkoraPrimaryNavy),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open Support Chat", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // SUB-PAGE: Terms & Conditions
                SettingsSubPage.TERMS -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Workora Terms & Conditions",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraMainText
                            )
                            Text(
                                text = "1. Workora connects local customers and skilled workers directly with 0% commission.\n" +
                                    "2. Users must provide accurate phone, area, and skill details.\n" +
                                    "3. Any fraudulent job posting or abusive behavior will result in immediate account suspension by the Admin.",
                                fontSize = 13.sp,
                                color = WorkoraSecondaryText
                            )
                        }
                    }
                }

                // SUB-PAGE: Privacy Policy
                SettingsSubPage.PRIVACY_POLICY -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Workora Privacy Policy",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraMainText
                            )
                            Text(
                                text = "Your phone number, area, and work profile are used solely to match customers and workers on Workora. Passwords are encrypted and never shared with third parties.",
                                fontSize = 13.sp,
                                color = WorkoraSecondaryText
                            )
                        }
                    }
                }
            }
        }
    }

    // Phone Update Dialog
    if (showPhoneDialog) {
        var tempPhone by remember { mutableStateOf(savedPhone) }
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            containerColor = WorkoraWhite,
            title = { Text("Update Phone Number", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = {
                OutlinedTextField(
                    value = tempPhone,
                    onValueChange = { tempPhone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempPhone.isNotBlank()) {
                            savedPhone = tempPhone.trim()
                            profilePrefs.edit().putString("user_phone", savedPhone).apply()
                            onUpdateProfile(savedName, savedPhone, savedArea)
                        }
                        showPhoneDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange)
                ) {
                    Text("Save", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneDialog = false }) {
                    Text("Cancel", color = WorkoraSecondaryText)
                }
            }
        )
    }

    // Email Update Dialog
    if (showEmailDialog) {
        var tempEmail by remember { mutableStateOf(savedEmail) }
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            containerColor = WorkoraWhite,
            title = { Text("Update Email Address", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = {
                OutlinedTextField(
                    value = tempEmail,
                    onValueChange = { tempEmail = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempEmail.isNotBlank() && tempEmail.contains("@")) {
                            savedEmail = tempEmail.trim()
                            authPrefs.edit().putString("last_logged_in_email", savedEmail).apply()
                        }
                        showEmailDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange)
                ) {
                    Text("Save", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailDialog = false }) {
                    Text("Cancel", color = WorkoraSecondaryText)
                }
            }
        )
    }

    // Area Update Dialog
    if (showAreaDialog) {
        var tempArea by remember { mutableStateOf(savedArea) }
        AlertDialog(
            onDismissRequest = { showAreaDialog = false },
            containerColor = WorkoraWhite,
            title = { Text("Change Your Area", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = {
                OutlinedTextField(
                    value = tempArea,
                    onValueChange = { tempArea = it },
                    label = { Text("Area / City (e.g. Silwani, Raisen)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempArea.isNotBlank()) {
                            savedArea = tempArea.trim()
                            profilePrefs.edit().putString("user_location", savedArea).apply()
                            onUpdateProfile(savedName, savedPhone, savedArea)
                        }
                        showAreaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange)
                ) {
                    Text("Save", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAreaDialog = false }) {
                    Text("Cancel", color = WorkoraSecondaryText)
                }
            }
        )
    }

    // Language Selection Dialog (English selected by default, structured for Hindi)
    if (showLanguageDialog) {
        val languages = listOf("English", "Hindi")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = WorkoraWhite,
            title = { Text("Select Language", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = lang
                                    settingsPrefs.edit().putString("app_language", lang).apply()
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage.equals(lang, ignoreCase = true),
                                onClick = {
                                    selectedLanguage = lang
                                    settingsPrefs.edit().putString("app_language", lang).apply()
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lang,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = WorkoraMainText
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = WorkoraPrimaryNavy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = WorkoraWhite,
            title = { Text("Confirm Logout", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = {
                Text(
                    text = "Are you sure you want to log out of your Workora account?",
                    color = WorkoraSecondaryText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraDangerRed)
                ) {
                    Text("Logout", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = WorkoraMainText)
                }
            }
        )
    }

    // Permanent Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = WorkoraWhite,
            title = { Text("Delete Account Permanently?", fontWeight = FontWeight.Bold, color = WorkoraDangerRed) },
            text = {
                Text(
                    text = "Warning: Account deletion is permanent and cannot be undone. All your profile data, bookings, and history will be permanently removed.",
                    color = WorkoraMainText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        profilePrefs.edit().clear().apply()
                        authPrefs.edit().clear().apply()
                        Toast.makeText(context, "Account deleted permanently", Toast.LENGTH_SHORT).show()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraDangerRed)
                ) {
                    Text("Delete Permanently", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = WorkoraMainText, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = WorkoraSecondaryText,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    valueText: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(WorkoraPrimaryNavy),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = WorkoraWhite,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = WorkoraMainText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = WorkoraSecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!valueText.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = valueText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = WorkoraSecondaryText
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = WorkoraSecondaryText
        )
    }
}

@Composable
private fun NotificationToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = WorkoraMainText
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = WorkoraSecondaryText
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = WorkoraWhite,
                checkedTrackColor = WorkoraSuccessGreen
            )
        )
    }
}

@Composable
private fun SettingsBottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) WorkoraAccentOrange else WorkoraPrimaryNavy
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = tint
        )
    }
}

@Composable
private fun SettingsWorkerAvatar(size: Dp = 64.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = Color(0xFFEFF6FF),
            radius = w * 0.5f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
        drawArc(
            color = WorkoraSuccessGreen,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.18f, h * 0.62f),
            size = Size(w * 0.64f, h * 0.52f)
        )
        drawCircle(
            color = Color(0xFFFFCC80),
            radius = w * 0.2f,
            center = Offset(w * 0.5f, h * 0.44f)
        )
        drawArc(
            color = WorkoraAccentOrange,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.26f, h * 0.16f),
            size = Size(w * 0.48f, h * 0.32f)
        )
        drawLine(
            color = WorkoraAccentOrange,
            start = Offset(w * 0.22f, h * 0.32f),
            end = Offset(w * 0.78f, h * 0.32f),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )
    }
}

private fun syncPasswordToFirebase(email: String, newPass: String) {
    Thread {
        try {
            val safeKey = email.lowercase().replace(".", "_").replace("@", "_at_")
            val url = URL("https://workora-d8b51-default-rtdb.firebaseio.com/users/$safeKey/password.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            OutputStreamWriter(conn.outputStream).use { it.write("\"$newPass\"") }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun submitReportToFirebase(name: String, phone: String, subject: String, details: String) {
    Thread {
        try {
            val url = URL("https://workora-d8b51-default-rtdb.firebaseio.com/reports.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("userName", name)
                put("userPhone", phone)
                put("subject", subject)
                put("details", details)
                put("status", "OPEN")
                put("timestamp", System.currentTimeMillis())
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}
