package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val WorkoraPrimaryNavy = Color(0xFF083D91)
private val WorkoraAccentOrange = Color(0xFFFF8C00)
private val WorkoraBgGray = Color(0xFFF8FAFC)
private val WorkoraWhite = Color(0xFFFFFFFF)
private val WorkoraMainText = Color(0xFF0B2345)
private val WorkoraSecondaryText = Color(0xFF687280)
private val WorkoraBorderColor = Color(0xFFE5EAF0)
private val WorkoraSuccessGreen = Color(0xFF16A34A)
private val WorkoraDangerRed = Color(0xFFDC2626)

private const val SETTINGS_FIREBASE_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"

private enum class SettingsSubPage {
    MAIN,
    EDIT_PROFILE,
    MY_BOOKINGS,
    NOTIFICATIONS,
    CHANGE_PASSWORD,
    BLOCKED_USERS,
    REPORT_PROBLEM,
    HELP_SUPPORT,
    TERMS,
    PRIVACY_POLICY
}

private data class UserBookingItem(
    val id: String,
    val title: String,
    val category: String,
    val location: String,
    val rate: Int,
    val status: String,
    val date: String,
    val customerName: String
)

private data class BlockedUserEntry(
    val key: String,
    val name: String,
    val phone: String,
    val blockedOn: String
)

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
    var isSyncing by remember { mutableStateOf(false) }

    // Real User Profile State
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
    var savedExperience by remember {
        mutableStateOf(profilePrefs.getString("user_experience", "4 yrs") ?: "4 yrs")
    }
    var savedRate by remember {
        mutableStateOf(profilePrefs.getString("user_rate", "650") ?: "650")
    }

    // Real Preferences State
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
    val isHindi = selectedLanguage.equals("Hindi", ignoreCase = true)

    // Real Firebase Lists for Bookings & Blocked Users
    val myBookingsList = remember { mutableStateListOf<UserBookingItem>() }
    val blockedUsersList = remember { mutableStateListOf<BlockedUserEntry>() }

    // Dialog states
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showAreaDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val isSuperAdmin = remember(savedEmail, savedPhone) {
        savedEmail.equals("ankitah994@gmail.com", ignoreCase = true) ||
            savedPhone.contains("6265798340") ||
            authPrefs.getString("saved_user_role", "") == "ADMIN"
    }

    // Load Real Profile, Bookings & Blocked Users from Firebase on start
    LaunchedEffect(savedEmail) {
        isSyncing = true
        fetchRealUserProfileFromFirebase(
            email = savedEmail,
            onLoaded = { name, phone, area, skill, exp, rate ->
                if (name.isNotBlank()) {
                    savedName = name
                    profilePrefs.edit().putString("user_name", name).apply()
                }
                if (phone.isNotBlank()) {
                    savedPhone = phone
                    profilePrefs.edit().putString("user_phone", phone).apply()
                }
                if (area.isNotBlank()) {
                    savedArea = area
                    profilePrefs.edit().putString("user_location", area).apply()
                }
                if (skill.isNotBlank()) {
                    savedSkill = skill
                    profilePrefs.edit().putString("user_skill", skill).apply()
                }
                if (exp.isNotBlank()) {
                    savedExperience = exp
                    profilePrefs.edit().putString("user_experience", exp).apply()
                }
                if (rate.isNotBlank()) {
                    savedRate = rate
                    profilePrefs.edit().putString("user_rate", rate).apply()
                }
                isSyncing = false
            }
        )
        fetchUserBookingsFromFirebase { list ->
            myBookingsList.clear()
            myBookingsList.addAll(list)
        }
        fetchBlockedUsersFromFirebase(savedEmail) { list ->
            blockedUsersList.clear()
            blockedUsersList.addAll(list)
        }
    }

    Scaffold(
        containerColor = WorkoraBgGray,
        bottomBar = {
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
                            label = if (isHindi) "होम" else "Home",
                            selected = false,
                            onClick = onBack
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.Search,
                            label = if (isHindi) "खोजें" else "Search",
                            selected = false,
                            onClick = onBack
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.EventNote,
                            label = if (isHindi) "बुकिंग्स" else "Bookings",
                            selected = currentSubPage == SettingsSubPage.MY_BOOKINGS,
                            onClick = { currentSubPage = SettingsSubPage.MY_BOOKINGS }
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Default.Person,
                            label = if (isHindi) "प्रोफाइल" else "Profile",
                            selected = currentSubPage != SettingsSubPage.MY_BOOKINGS,
                            onClick = { currentSubPage = SettingsSubPage.MAIN }
                        )
                    } else {
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.Home,
                            label = if (isHindi) "होम" else "Home",
                            selected = false,
                            onClick = onBack
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.NotificationsNone,
                            label = if (isHindi) "रिक्वेस्ट" else "Requests",
                            selected = currentSubPage == SettingsSubPage.NOTIFICATIONS,
                            onClick = { currentSubPage = SettingsSubPage.NOTIFICATIONS }
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Outlined.WorkOutline,
                            label = if (isHindi) "काम" else "Jobs",
                            selected = currentSubPage == SettingsSubPage.MY_BOOKINGS,
                            onClick = { currentSubPage = SettingsSubPage.MY_BOOKINGS }
                        )
                        SettingsBottomBarItem(
                            icon = Icons.Default.Person,
                            label = if (isHindi) "प्रोफाइल" else "Profile",
                            selected = currentSubPage == SettingsSubPage.MAIN,
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
                                SettingsSubPage.MAIN -> if (isHindi) "सेटिंग्स (Settings)" else "Settings"
                                SettingsSubPage.EDIT_PROFILE -> if (isHindi) "प्रोफाइल एडिट करें" else "Edit Profile"
                                SettingsSubPage.MY_BOOKINGS -> if (isHindi) "मेरी बुकिंग्स" else "My Bookings"
                                SettingsSubPage.NOTIFICATIONS -> if (isHindi) "नोटिफिकेशन सेटिंग्स" else "Notification Settings"
                                SettingsSubPage.CHANGE_PASSWORD -> if (isHindi) "पासवर्ड बदलें" else "Change Password"
                                SettingsSubPage.BLOCKED_USERS -> if (isHindi) "ब्लॉक किए गए यूजर" else "Blocked Users"
                                SettingsSubPage.REPORT_PROBLEM -> if (isHindi) "समस्या रिपोर्ट करें" else "Report a Problem"
                                SettingsSubPage.HELP_SUPPORT -> if (isHindi) "सहायता केंद्र" else "Help & Support"
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

            if (isSyncing) {
                LinearProgressIndicator(
                    color = WorkoraAccentOrange,
                    trackColor = WorkoraBorderColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            when (currentSubPage) {
                SettingsSubPage.MAIN -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top Profile Summary Card
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
                                            text = if (role == UserRole.LABOUR) "$savedSkill • ₹$savedRate/day" else "Customer Account",
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
                            SettingsSectionHeader(if (isHindi) "अकाउंट (Account)" else "Account")
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
                                        title = if (isHindi) "प्रोफाइल (Profile)" else "Profile",
                                        subtitle = if (isHindi) "अपनी व्यक्तिगत जानकारी अपडेट करें" else "Manage your personal information",
                                        onClick = { currentSubPage = SettingsSubPage.EDIT_PROFILE }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.EventNote,
                                        title = if (isHindi) "मेरी बुकिंग्स (My Bookings)" else "My Bookings",
                                        subtitle = if (isHindi) "अपनी बुकिंग और काम का इतिहास देखें" else "View your bookings and history",
                                        valueText = "${myBookingsList.size}",
                                        onClick = { currentSubPage = SettingsSubPage.MY_BOOKINGS }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Phone,
                                        title = if (isHindi) "फ़ोन नंबर (Phone Number)" else "Phone Number",
                                        subtitle = if (isHindi) "अपना मोबाइल नंबर बदलें" else "Manage your phone number",
                                        valueText = savedPhone,
                                        onClick = { showPhoneDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Email,
                                        title = if (isHindi) "ईमेल (Email)" else "Email",
                                        subtitle = if (isHindi) "अपना ईमेल पता प्रबंधित करें" else "Manage your email address",
                                        valueText = savedEmail,
                                        onClick = { showEmailDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Place,
                                        title = if (isHindi) "एरिया / शहर (Area)" else "Area",
                                        subtitle = if (isHindi) "अपना क्षेत्र बदलें" else "Change your area",
                                        valueText = savedArea,
                                        onClick = { showAreaDialog = true }
                                    )
                                }
                            }
                        }

                        // SECTION 2: Preferences
                        item {
                            SettingsSectionHeader(if (isHindi) "प्राथमिकताएं (Preferences)" else "Preferences")
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
                                                text = if (isHindi) "नोटिफिकेशन (Notifications)" else "Notifications",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = WorkoraMainText
                                            )
                                            Text(
                                                text = if (isHindi) "नोटिफिकेशन सेटिंग्स प्रबंधित करें" else "Manage your notifications",
                                                fontSize = 12.sp,
                                                color = WorkoraSecondaryText
                                            )
                                        }
                                        Switch(
                                            checked = notificationsMasterToggle,
                                            onCheckedChange = { checked ->
                                                notificationsMasterToggle = checked
                                                settingsPrefs.edit().putBoolean("notif_master", checked).apply()
                                                syncUserFieldToFirebase(savedEmail, "notificationsEnabled", checked.toString())
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
                                        title = if (isHindi) "भाषा (Language)" else "Language",
                                        subtitle = if (isHindi) "ऐप की भाषा बदलें" else "Change app language",
                                        valueText = selectedLanguage,
                                        onClick = { showLanguageDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Place,
                                        title = if (isHindi) "लोकेशन (Location)" else "Location",
                                        subtitle = savedArea,
                                        valueText = "Current area",
                                        onClick = { showAreaDialog = true }
                                    )
                                }
                            }
                        }

                        // SECTION 3: Privacy & Security
                        item {
                            SettingsSectionHeader(if (isHindi) "सुरक्षा (Privacy & Security)" else "Privacy & Security")
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
                                        title = if (isHindi) "पासवर्ड बदलें (Change Password)" else "Change Password",
                                        subtitle = if (isHindi) "अपना पासवर्ड अपडेट करें" else "Update your account password",
                                        onClick = { currentSubPage = SettingsSubPage.CHANGE_PASSWORD }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.PersonOff,
                                        title = if (isHindi) "ब्लॉक किए गए यूजर (Blocked Users)" else "Blocked Users",
                                        subtitle = if (isHindi) "ब्लॉक किए गए अकाउंट देखें" else "Manage blocked accounts",
                                        valueText = "${blockedUsersList.size}",
                                        onClick = { currentSubPage = SettingsSubPage.BLOCKED_USERS }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Flag,
                                        title = if (isHindi) "समस्या रिपोर्ट करें (Report a Problem)" else "Report a Problem",
                                        subtitle = if (isHindi) "किसी भी समस्या की जानकारी दें" else "Tell us about an issue",
                                        onClick = { currentSubPage = SettingsSubPage.REPORT_PROBLEM }
                                    )
                                }
                            }
                        }

                        // SECTION 4: Support
                        item {
                            SettingsSectionHeader(if (isHindi) "सहायता (Support)" else "Support")
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
                                        title = if (isHindi) "सहायता केंद्र (Help & Support)" else "Help & Support",
                                        subtitle = if (isHindi) "मदद लें या हमसे संपर्क करें" else "Get help or contact us",
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

                        // SECTION 5: Account Actions
                        item {
                            SettingsSectionHeader(if (isHindi) "अकाउंट एक्शन (Account)" else "Account")
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
                                        title = "Switch Role (${if (role == UserRole.CUSTOMER) "Worker Mode" else "Customer Mode"})",
                                        subtitle = "Switch between Customer and Labour view",
                                        onClick = onSwitchRole
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)

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
                                                text = if (isHindi) "लॉगआउट (Logout)" else "Logout",
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
                                                text = if (isHindi) "अकाउंट हमेशा के लिए डिलीट करें" else "Delete Account",
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

                // SUB-PAGE: Edit Profile (Live Sync to Firebase /users and /workers)
                SettingsSubPage.EDIT_PROFILE -> {
                    var editName by remember { mutableStateOf(savedName) }
                    var editPhone by remember { mutableStateOf(savedPhone) }
                    var editArea by remember { mutableStateOf(savedArea) }
                    var editSkill by remember { mutableStateOf(savedSkill) }
                    var editExp by remember { mutableStateOf(savedExperience) }
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
                                                text = if (role == UserRole.LABOUR) "Worker Account" else "Customer Account",
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
                                    OutlinedTextField(
                                        value = editSkill,
                                        onValueChange = { editSkill = it },
                                        label = { Text("Category / Skill (e.g. Electrician, Mason)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = editExp,
                                        onValueChange = { editExp = it },
                                        label = { Text("Experience (e.g. 5 yrs)") },
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

                                    Button(
                                        onClick = {
                                            if (editName.isBlank() || editPhone.isBlank()) {
                                                Toast.makeText(context, "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show()
                                            } else {
                                                savedName = editName.trim()
                                                savedPhone = editPhone.trim()
                                                savedArea = editArea.trim()
                                                savedSkill = editSkill.trim()
                                                savedExperience = editExp.trim()
                                                savedRate = editRate.filter { it.isDigit() }.ifEmpty { "600" }

                                                profilePrefs.edit()
                                                    .putString("user_name", savedName)
                                                    .putString("user_phone", savedPhone)
                                                    .putString("user_location", savedArea)
                                                    .putString("user_skill", savedSkill)
                                                    .putString("user_experience", savedExperience)
                                                    .putString("user_rate", savedRate)
                                                    .apply()

                                                saveFullProfileToFirebase(
                                                    email = savedEmail,
                                                    name = savedName,
                                                    phone = savedPhone,
                                                    area = savedArea,
                                                    role = role.name,
                                                    skill = savedSkill,
                                                    experience = savedExperience,
                                                    dailyRate = savedRate.toIntOrNull() ?: 600
                                                )

                                                onUpdateProfile(savedName, savedPhone, savedArea)
                                                Toast.makeText(context, "Profile saved & synced to cloud ✓", Toast.LENGTH_SHORT).show()
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

                // SUB-PAGE: My Bookings (Real Firebase /jobs List)
                SettingsSubPage.MY_BOOKINGS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (myBookingsList.isEmpty()) {
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
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.EventNote,
                                            contentDescription = null,
                                            tint = WorkoraSecondaryText,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "No Bookings Found",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WorkoraMainText
                                        )
                                        Text(
                                            text = "Your active and past work bookings will appear here.",
                                            fontSize = 13.sp,
                                            color = WorkoraSecondaryText
                                        )
                                    }
                                }
                            }
                        } else {
                            items(myBookingsList) { bk ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                    border = BorderStroke(1.dp, WorkoraBorderColor)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = bk.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkoraMainText
                                            )
                                            Surface(
                                                color = Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(50)
                                            ) {
                                                Text(
                                                    text = bk.status,
                                                    color = WorkoraSuccessGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${bk.category} • ${bk.location}",
                                            fontSize = 13.sp,
                                            color = WorkoraSecondaryText
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Customer: ${bk.customerName}",
                                                fontSize = 12.sp,
                                                color = WorkoraSecondaryText
                                            )
                                            Text(
                                                text = "₹${bk.rate}/day",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkoraAccentOrange
                                            )
                                        }
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
                                    syncUserFieldToFirebase(savedEmail, "notif_work_requests", it.toString())
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
                                    syncUserFieldToFirebase(savedEmail, "notif_booking_updates", it.toString())
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
                                    syncUserFieldToFirebase(savedEmail, "notif_messages", it.toString())
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
                                    syncUserFieldToFirebase(savedEmail, "notif_promotional", it.toString())
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
                                    val storedPass = authPrefs.getString("saved_password_$savedEmail", null)
                                    val hasLetter = newPassword.any { it.isLetter() }
                                    val hasDigit = newPassword.any { it.isDigit() }
                                    when {
                                        currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                                            errorMsg = "All password fields are required."
                                        }
                                        storedPass != null && storedPass != currentPassword -> {
                                            errorMsg = "Current password does not match your account password."
                                        }
                                        newPassword.length < 8 || !hasLetter || !hasDigit -> {
                                            errorMsg = "New password must be at least 8 characters and contain a letter and number."
                                        }
                                        newPassword != confirmPassword -> {
                                            errorMsg = "New password and confirmation do not match."
                                        }
                                        else -> {
                                            authPrefs.edit().putString("saved_password_$savedEmail", newPassword).apply()
                                            syncUserFieldToFirebase(savedEmail, "password", newPassword)
                                            Toast.makeText(context, "Password changed in cloud & locally ✓", Toast.LENGTH_SHORT).show()
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

                // SUB-PAGE: Blocked Users (Real Add Block & Unblock with Firebase)
                SettingsSubPage.BLOCKED_USERS -> {
                    var blockNameInput by remember { mutableStateOf("") }
                    var blockPhoneInput by remember { mutableStateOf("") }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                border = BorderStroke(1.dp, WorkoraBorderColor)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Block a User",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkoraMainText
                                    )
                                    OutlinedTextField(
                                        value = blockNameInput,
                                        onValueChange = { blockNameInput = it },
                                        label = { Text("User Name") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = blockPhoneInput,
                                        onValueChange = { blockPhoneInput = it },
                                        label = { Text("Phone Number") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            if (blockNameInput.isNotBlank() && blockPhoneInput.isNotBlank()) {
                                                val entry = BlockedUserEntry(
                                                    key = "blk_${System.currentTimeMillis()}",
                                                    name = blockNameInput.trim(),
                                                    phone = blockPhoneInput.trim(),
                                                    blockedOn = SimpleDateFormat("d MMM yyyy", Locale.US).format(Date())
                                                )
                                                blockedUsersList.add(entry)
                                                addBlockedUserToFirebase(savedEmail, entry)
                                                blockNameInput = ""
                                                blockPhoneInput = ""
                                                Toast.makeText(context, "${entry.name} blocked", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraDangerRed),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Block User", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (blockedUsersList.isEmpty()) {
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
                                            .padding(28.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PersonOff,
                                            contentDescription = null,
                                            tint = WorkoraSecondaryText,
                                            modifier = Modifier.size(44.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No Blocked Users", fontWeight = FontWeight.Bold, color = WorkoraMainText)
                                    }
                                }
                            }
                        } else {
                            items(blockedUsersList) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                    border = BorderStroke(1.dp, WorkoraBorderColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                                            Text("${item.phone} • Blocked ${item.blockedOn}", fontSize = 12.sp, color = WorkoraSecondaryText)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                blockedUsersList.remove(item)
                                                removeBlockedUserFromFirebase(savedEmail, item.key)
                                                Toast.makeText(context, "${item.name} unblocked", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Unblock", color = WorkoraSuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
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
                                        Toast.makeText(context, "Report sent to Admin Panel ✓", Toast.LENGTH_SHORT).show()
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onOpenChat,
                                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraPrimaryNavy),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Open Support Chat", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+916265798340"))
                                        context.startActivity(dial)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Call Helpline", color = WorkoraPrimaryNavy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

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
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Workora Terms & Conditions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                            Text(
                                "1. Workora connects local customers and skilled workers directly with 0% commission.\n" +
                                    "2. Users must provide accurate phone, area, and skill details.\n" +
                                    "3. Any fraudulent job posting or abusive behavior will result in immediate account suspension by the Admin.",
                                fontSize = 13.sp,
                                color = WorkoraSecondaryText
                            )
                        }
                    }
                }

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
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Workora Privacy Policy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                            Text(
                                "Your phone number, area, and work profile are used solely to match customers and workers on Workora. Passwords are never shared with third parties.",
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
                            syncUserFieldToFirebase(savedEmail, "phone", savedPhone)
                            onUpdateProfile(savedName, savedPhone, savedArea)
                            Toast.makeText(context, "Phone updated", Toast.LENGTH_SHORT).show()
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
                            syncUserFieldToFirebase(savedEmail, "email", savedEmail)
                            Toast.makeText(context, "Email updated", Toast.LENGTH_SHORT).show()
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
                            syncUserFieldToFirebase(savedEmail, "location", savedArea)
                            onUpdateProfile(savedName, savedPhone, savedArea)
                            Toast.makeText(context, "Area updated to $savedArea", Toast.LENGTH_SHORT).show()
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

    // Language Selection Dialog
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

    // Delete Account Confirmation Dialog (Real Firebase Deletion)
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = WorkoraWhite,
            title = { Text("Delete Account Permanently?", fontWeight = FontWeight.Bold, color = WorkoraDangerRed) },
            text = {
                Text(
                    text = "Warning: Account deletion is permanent and cannot be undone. All your profile data, bookings, and history will be permanently removed from Firebase.",
                    color = WorkoraMainText,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccountFromFirebase(savedEmail)
                        profilePrefs.edit().clear().apply()
                        authPrefs.edit().clear().apply()
                        Toast.makeText(context, "Account permanently deleted from server", Toast.LENGTH_SHORT).show()
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

// ==================================================
// REAL FIREBASE SYNC FUNCTIONS FOR SETTINGS
// ==================================================
private fun safeFirebaseKey(email: String): String {
    return email.trim().lowercase(Locale.US).replace(".", "_").replace("@", "_at_")
}

private fun fetchRealUserProfileFromFirebase(
    email: String,
    onLoaded: (String, String, String, String, String, String) -> Unit
) {
    Thread {
        try {
            val key = safeFirebaseKey(email)
            val conn = URL("$SETTINGS_FIREBASE_URL/users/$key.json").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            if (conn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                    val obj = JSONObject(resp)
                    val name = obj.optString("name", "")
                    val phone = obj.optString("phone", "")
                    val loc = obj.optString("location", "")
                    val skill = obj.optString("skill", "")
                    val exp = obj.optString("experience", "")
                    val rate = obj.optInt("dailyRate", 0).takeIf { it > 0 }?.toString() ?: ""
                    Handler(Looper.getMainLooper()).post {
                        onLoaded(name, phone, loc, skill, exp, rate)
                    }
                    conn.disconnect()
                    return@Thread
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post {
            onLoaded("", "", "", "", "", "")
        }
    }.start()
}

private fun saveFullProfileToFirebase(
    email: String,
    name: String,
    phone: String,
    area: String,
    role: String,
    skill: String,
    experience: String,
    dailyRate: Int
) {
    Thread {
        try {
            val key = safeFirebaseKey(email)
            val url = URL("$SETTINGS_FIREBASE_URL/users/$key.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("name", name)
                put("phone", phone)
                put("email", email)
                put("location", area)
                put("role", role)
                put("skill", skill)
                put("experience", experience)
                put("dailyRate", dailyRate)
                put("status", "Active")
                put("availability", "Available")
                put("joined", SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()))
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun syncUserFieldToFirebase(email: String, fieldName: String, value: String) {
    Thread {
        try {
            val key = safeFirebaseKey(email)
            val url = URL("$SETTINGS_FIREBASE_URL/users/$key.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put(fieldName, value)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun fetchUserBookingsFromFirebase(onLoaded: (List<UserBookingItem>) -> Unit) {
    Thread {
        val list = mutableListOf<UserBookingItem>()
        try {
            val conn = URL("$SETTINGS_FIREBASE_URL/jobs.json").openConnection() as HttpURLConnection
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
                        list.add(
                            UserBookingItem(
                                id = k,
                                title = obj.optString("title", "Work Booking"),
                                category = obj.optString("category", "General"),
                                location = obj.optString("location", "Silwani, Raisen"),
                                rate = obj.optInt("dailyRate", 600),
                                status = obj.optString("status", "PENDING").uppercase(Locale.US),
                                date = obj.optString("date", "Today"),
                                customerName = obj.optString("customerName", "Ankit Ahirwar")
                            )
                        )
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

private fun fetchBlockedUsersFromFirebase(email: String, onLoaded: (List<BlockedUserEntry>) -> Unit) {
    Thread {
        val list = mutableListOf<BlockedUserEntry>()
        try {
            val userKey = safeFirebaseKey(email)
            val conn = URL("$SETTINGS_FIREBASE_URL/blocked_users/$userKey.json").openConnection() as HttpURLConnection
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
                        list.add(
                            BlockedUserEntry(
                                key = k,
                                name = obj.optString("name", "User"),
                                phone = obj.optString("phone", ""),
                                blockedOn = obj.optString("blockedOn", "")
                            )
                        )
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

private fun addBlockedUserToFirebase(email: String, entry: BlockedUserEntry) {
    Thread {
        try {
            val userKey = safeFirebaseKey(email)
            val url = URL("$SETTINGS_FIREBASE_URL/blocked_users/$userKey/${entry.key}.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("name", entry.name)
                put("phone", entry.phone)
                put("blockedOn", entry.blockedOn)
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun removeBlockedUserFromFirebase(email: String, blockKey: String) {
    Thread {
        try {
            val userKey = safeFirebaseKey(email)
            val url = URL("$SETTINGS_FIREBASE_URL/blocked_users/$userKey/$blockKey.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "DELETE"
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun deleteAccountFromFirebase(email: String) {
    Thread {
        try {
            val userKey = safeFirebaseKey(email)
            val url = URL("$SETTINGS_FIREBASE_URL/users/$userKey.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "DELETE"
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}

private fun submitReportToFirebase(name: String, phone: String, subject: String, details: String) {
    Thread {
        try {
            val url = URL("$SETTINGS_FIREBASE_URL/reports.json")
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
                put("date", SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()))
                put("timestamp", System.currentTimeMillis())
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }.start()
}
