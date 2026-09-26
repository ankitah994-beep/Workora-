package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
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
import androidx.compose.ui.graphics.Path
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

// =========================================================================
// GLOBAL LANGUAGE MANAGER (Shared across the entire Workora App)
// =========================================================================
internal object AppLanguageManager {
    var currentLanguage by mutableStateOf("English")

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE)
        currentLanguage = prefs.getString("app_language", "English") ?: "English"
    }

    fun setLanguage(context: Context, language: String) {
        currentLanguage = language
        context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language", language)
            .apply()
    }

    fun isHindi(context: Context): Boolean {
        init(context)
        return currentLanguage.equals("Hindi", ignoreCase = true) ||
            currentLanguage.contains("हिंदी")
    }
}

private enum class SettingsSubPage {
    MAIN,
    EDIT_PROFILE,
    MY_BOOKINGS,
    NOTIFICATIONS,
    CHANGE_PASSWORD,
    BLOCKED_USERS,
    REPORT_PROBLEM,
    HELP_SUPPORT,
    ABOUT_WORKORA,
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
    userName: String = "",
    userPhone: String = "",
    userLocation: String = "",
    onBack: () -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onUpdateProfile: (String, String, String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose { }
    }

    LaunchedEffect(Unit) {
        AppLanguageManager.init(context)
    }

    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    var currentSubPage by remember { mutableStateOf(SettingsSubPage.MAIN) }
    var isSyncing by remember { mutableStateOf(false) }

    var savedName by remember {
        mutableStateOf(profilePrefs.getString("user_name", "") ?: "")
    }
    var savedPhone by remember {
        mutableStateOf(profilePrefs.getString("user_phone", "") ?: "")
    }
    var savedEmail by remember {
        mutableStateOf(authPrefs.getString("last_logged_in_email", "") ?: "")
    }
    var savedState by remember {
        mutableStateOf(profilePrefs.getString("user_state", "") ?: "")
    }
    var savedArea by remember {
        mutableStateOf(profilePrefs.getString("user_location", "") ?: "")
    }
    var savedSkill by remember {
        mutableStateOf(profilePrefs.getString("user_skill", "") ?: "")
    }
    var savedExperience by remember {
        mutableStateOf(profilePrefs.getString("user_experience", "") ?: "")
    }
    var savedRate by remember {
        mutableStateOf(profilePrefs.getString("user_rate", "") ?: "")
    }

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

    val selectedLanguage = AppLanguageManager.currentLanguage
    val isHindi = selectedLanguage.equals("Hindi", ignoreCase = true) || selectedLanguage.contains("हिंदी")

    val myBookingsList = remember { mutableStateListOf<UserBookingItem>() }
    val blockedUsersList = remember { mutableStateListOf<BlockedUserEntry>() }

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

    LaunchedEffect(savedEmail) {
        if (savedEmail.isNotBlank()) {
            isSyncing = true
            fetchRealUserProfileFromFirebase(
                email = savedEmail,
                onLoaded = { name, phone, state, area, skill, exp, rate ->
                    savedName = name
                    savedPhone = phone
                    savedState = state
                    savedArea = area
                    savedSkill = skill
                    savedExperience = exp
                    savedRate = rate

                    profilePrefs.edit()
                        .putString("user_name", name)
                        .putString("user_phone", phone)
                        .putString("user_state", state)
                        .putString("user_location", area)
                        .putString("user_skill", skill)
                        .putString("user_experience", exp)
                        .putString("user_rate", rate)
                        .apply()

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
    }

    Scaffold(
        containerColor = WorkoraBgGray,
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsBottomWaveDecoration()

                Surface(
                    color = WorkoraWhite,
                    shadowElevation = 12.dp
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                                SettingsSubPage.MAIN -> if (isHindi) "सेटिंग्स" else "Settings"
                                SettingsSubPage.EDIT_PROFILE -> if (isHindi) "प्रोफाइल एडिट करें" else "Edit Profile"
                                SettingsSubPage.MY_BOOKINGS -> if (isHindi) "मेरी बुकिंग्स" else "My Bookings"
                                SettingsSubPage.NOTIFICATIONS -> if (isHindi) "नोटिफिकेशन" else "Notifications"
                                SettingsSubPage.CHANGE_PASSWORD -> if (isHindi) "पासवर्ड बदलें" else "Change Password"
                                SettingsSubPage.BLOCKED_USERS -> if (isHindi) "ब्लॉक किए गए यूजर" else "Blocked Users"
                                SettingsSubPage.REPORT_PROBLEM -> if (isHindi) "समस्या रिपोर्ट करें" else "Report a Problem"
                                SettingsSubPage.HELP_SUPPORT -> if (isHindi) "सहायता और समर्थन" else "Help & Support"
                                SettingsSubPage.ABOUT_WORKORA -> if (isHindi) "Workora के बारे में" else "About Workora"
                                SettingsSubPage.TERMS -> if (isHindi) "नियम और शर्तें" else "Terms & Conditions"
                                SettingsSubPage.PRIVACY_POLICY -> if (isHindi) "गोपनीयता नीति" else "Privacy Policy"
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
                                    text = if (isHindi) "एडमिन पैनल" else "Admin Panel",
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Top User Profile Card
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
                                    SettingsWorkerAvatar(size = 66.dp)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = savedName.ifBlank { savedEmail.substringBefore("@").ifBlank { if (isHindi) "Workora यूजर" else "Workora User" } },
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WorkoraMainText
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (role == UserRole.LABOUR) {
                                                savedSkill.ifBlank { if (isHindi) "कारीगर / वर्कर" else "Worker" }
                                            } else {
                                                if (isHindi) "ग्राहक (Customer)" else "Customer"
                                            },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = WorkoraSecondaryText
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = WorkoraPrimaryNavy,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val locDisplay = listOf(savedArea, savedState)
                                                .filter { it.isNotBlank() }
                                                .joinToString(", ")
                                                .ifBlank { if (isHindi) "लोकेशन सेट करने के लिए टैप करें" else "Tap to set location" }
                                            Text(
                                                text = locDisplay,
                                                fontSize = 13.sp,
                                                color = WorkoraSecondaryText
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Edit Profile",
                                        tint = WorkoraMainText
                                    )
                                }
                            }
                        }

                        // 2. Primary Settings Card (Fully Bilingual)
                        item {
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
                                        title = if (isHindi) "प्रोफाइल एडिट करें" else "Edit Profile",
                                        subtitle = if (isHindi) "अपनी व्यक्तिगत जानकारी अपडेट करें" else "Update your personal details",
                                        onClick = { currentSubPage = SettingsSubPage.EDIT_PROFILE }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.EventNote,
                                        title = if (isHindi) "मेरी बुकिंग्स" else "My Bookings",
                                        subtitle = if (isHindi) "अपनी बुकिंग और काम का इतिहास देखें" else "View your bookings and history",
                                        onClick = { currentSubPage = SettingsSubPage.MY_BOOKINGS }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Notifications,
                                        title = if (isHindi) "नोटिफिकेशन" else "Notifications",
                                        subtitle = if (isHindi) "नोटिफिकेशन प्रबंधित करें" else "Manage your notifications",
                                        onClick = { currentSubPage = SettingsSubPage.NOTIFICATIONS }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Language,
                                        title = if (isHindi) "भाषा (Language)" else "Language",
                                        subtitle = if (isHindi) "पूरे ऐप की भाषा बदलें" else "Change entire app language",
                                        valueText = if (isHindi) "हिंदी (Hindi)" else "English",
                                        onClick = { showLanguageDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.HelpOutline,
                                        title = if (isHindi) "सहायता और समर्थन" else "Help & Support",
                                        subtitle = if (isHindi) "मदद लें या हमसे संपर्क करें" else "Get help or contact us",
                                        onClick = { currentSubPage = SettingsSubPage.HELP_SUPPORT }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Info,
                                        title = if (isHindi) "Workora के बारे में" else "About Workora",
                                        subtitle = if (isHindi) "संस्करण 1.0.0" else "Version 1.0.0",
                                        onClick = { currentSubPage = SettingsSubPage.ABOUT_WORKORA }
                                    )
                                }
                            }
                        }

                        // 3. Account & Security Details Card (100% Translated in Hindi & English)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                                border = BorderStroke(1.dp, WorkoraBorderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Phone,
                                        title = if (isHindi) "मोबाइल नंबर" else "Phone Number",
                                        subtitle = if (isHindi) "अपना फोन नंबर प्रबंधित करें" else "Manage your phone number",
                                        valueText = savedPhone.ifBlank { if (isHindi) "सेट नहीं है" else "Not set" },
                                        onClick = { showPhoneDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Email,
                                        title = if (isHindi) "ईमेल पता" else "Email",
                                        subtitle = if (isHindi) "अपना ईमेल पता प्रबंधित करें" else "Manage your email address",
                                        valueText = savedEmail.ifBlank { if (isHindi) "सेट नहीं है" else "Not set" },
                                        onClick = { showEmailDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Place,
                                        title = if (isHindi) "शहर / राज्य (Area / State)" else "Area / State",
                                        subtitle = if (isHindi) "अपना क्षेत्र या राज्य बदलें" else "Change your area or state",
                                        valueText = savedArea.ifBlank { if (isHindi) "सेट नहीं है" else "Not set" },
                                        onClick = { showAreaDialog = true }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Lock,
                                        title = if (isHindi) "पासवर्ड बदलें" else "Change Password",
                                        subtitle = if (isHindi) "अपने अकाउंट का पासवर्ड अपडेट करें" else "Update your account password",
                                        onClick = { currentSubPage = SettingsSubPage.CHANGE_PASSWORD }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.PersonOff,
                                        title = if (isHindi) "ब्लॉक किए गए यूजर" else "Blocked Users",
                                        subtitle = if (isHindi) "ब्लॉक किए गए अकाउंट प्रबंधित करें" else "Manage blocked accounts",
                                        valueText = "${blockedUsersList.size}",
                                        onClick = { currentSubPage = SettingsSubPage.BLOCKED_USERS }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Flag,
                                        title = if (isHindi) "समस्या रिपोर्ट करें" else "Report a Problem",
                                        subtitle = if (isHindi) "हमें किसी समस्या के बारे में बताएं" else "Tell us about an issue",
                                        onClick = { currentSubPage = SettingsSubPage.REPORT_PROBLEM }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Description,
                                        title = if (isHindi) "नियम और शर्तें" else "Terms & Conditions",
                                        subtitle = if (isHindi) "Workora की सेवा शर्तें पढ़ें" else "Read Workora terms of service",
                                        onClick = { currentSubPage = SettingsSubPage.TERMS }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.Shield,
                                        title = if (isHindi) "गोपनीयता नीति (Privacy Policy)" else "Privacy Policy",
                                        subtitle = if (isHindi) "हम आपके डेटा की सुरक्षा कैसे करते हैं" else "How we protect your data",
                                        onClick = { currentSubPage = SettingsSubPage.PRIVACY_POLICY }
                                    )
                                    HorizontalDivider(color = WorkoraBorderColor, thickness = 1.dp)
                                    SettingsRowItem(
                                        icon = Icons.Outlined.SwapHoriz,
                                        title = if (isHindi) {
                                            "रोल बदलें (${if (role == UserRole.CUSTOMER) "वर्कर मोड" else "कस्टमर मोड"})"
                                        } else {
                                            "Switch Role (${if (role == UserRole.CUSTOMER) "Worker Mode" else "Customer Mode"})"
                                        },
                                        subtitle = if (isHindi) "कस्टमर और वर्कर मोड के बीच बदलें" else "Switch between Customer and Worker view",
                                        onClick = onSwitchRole
                                    )
                                }
                            }
                        }

                        // 4. Logout & Delete Account Card (Fully Bilingual)
                        item {
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
                                            .clickable { showLogoutDialog = true }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFFEE2E2)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Logout,
                                                contentDescription = "Logout",
                                                tint = WorkoraDangerRed,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isHindi) "लॉगआउट (Logout)" else "Logout",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkoraDangerRed
                                            )
                                            Text(
                                                text = if (isHindi) "अपने अकाउंट से साइन आउट करें" else "Sign out from your account",
                                                fontSize = 12.sp,
                                                color = WorkoraSecondaryText
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = WorkoraMainText
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
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFFFEE2E2)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.DeleteForever,
                                                contentDescription = "Delete Account",
                                                tint = WorkoraDangerRed,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isHindi) "अकाउंट डिलीट करें" else "Delete Account",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WorkoraDangerRed
                                            )
                                            Text(
                                                text = if (isHindi) "अपना अकाउंट स्थायी रूप से हटाएं" else "Permanently remove your account",
                                                fontSize = 12.sp,
                                                color = WorkoraSecondaryText
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            tint = WorkoraMainText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                SettingsSubPage.EDIT_PROFILE -> {
                    var editName by remember { mutableStateOf(savedName) }
                    var editPhone by remember { mutableStateOf(savedPhone) }
                    var editState by remember { mutableStateOf(savedState) }
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
                                            Text(editName.ifBlank { if (isHindi) "आपकी प्रोफाइल" else "Your Profile" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                                            Text(
                                                text = if (role == UserRole.LABOUR) editSkill.ifBlank { if (isHindi) "कारीगर" else "Worker" } else if (isHindi) "ग्राहक" else "Customer",
                                                fontSize = 13.sp,
                                                color = WorkoraAccentOrange,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text(if (isHindi) "पूरा नाम *" else "Full Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text(if (isHindi) "मोबाइल नंबर *" else "Phone Number *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = editState, onValueChange = { editState = it }, label = { Text(if (isHindi) "राज्य (State) *" else "State *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = editArea, onValueChange = { editArea = it }, label = { Text(if (isHindi) "शहर / गाँव (Area / City) *" else "Area / City *") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                                    if (role == UserRole.LABOUR) {
                                        OutlinedTextField(value = editSkill, onValueChange = { editSkill = it }, label = { Text(if (isHindi) "काम की श्रेणी (Skill) *" else "Category / Skill *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = editExp, onValueChange = { editExp = it }, label = { Text(if (isHindi) "अनुभव (जैसे 3 yrs) *" else "Experience (e.g. 3 yrs) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(value = editRate, onValueChange = { editRate = it }, label = { Text(if (isHindi) "प्रतिदिन रेट (₹) *" else "Daily Rate (₹) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    }

                                    Button(
                                        onClick = {
                                            if (editName.isBlank() || editPhone.isBlank() || editArea.isBlank()) {
                                                Toast.makeText(context, if (isHindi) "कृपया नाम, फोन और एरिया भरें" else "Name, Phone and Area cannot be empty", Toast.LENGTH_SHORT).show()
                                            } else {
                                                savedName = editName.trim()
                                                savedPhone = editPhone.trim()
                                                savedState = editState.trim()
                                                savedArea = editArea.trim()
                                                savedSkill = editSkill.trim()
                                                savedExperience = editExp.trim()
                                                savedRate = editRate.filter { it.isDigit() }

                                                profilePrefs.edit()
                                                    .putString("user_name", savedName)
                                                    .putString("user_phone", savedPhone)
                                                    .putString("user_state", savedState)
                                                    .putString("user_location", savedArea)
                                                    .putString("user_skill", savedSkill)
                                                    .putString("user_experience", savedExperience)
                                                    .putString("user_rate", savedRate)
                                                    .apply()

                                                saveFullProfileToFirebase(
                                                    email = savedEmail,
                                                    name = savedName,
                                                    phone = savedPhone,
                                                    state = savedState,
                                                    area = savedArea,
                                                    role = if (role == UserRole.LABOUR) "Worker" else "Customer",
                                                    skill = savedSkill,
                                                    experience = savedExperience,
                                                    dailyRate = savedRate.toIntOrNull() ?: 0
                                                )

                                                onUpdateProfile(savedName, savedPhone, savedArea)
                                                Toast.makeText(context, if (isHindi) "प्रोफाइल अपडेट हो गई ✓" else "Profile updated ✓", Toast.LENGTH_SHORT).show()
                                                currentSubPage = SettingsSubPage.MAIN
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text(if (isHindi) "बदलाव सेव करें" else "Save Changes", color = WorkoraWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                }

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
                                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Outlined.EventNote, contentDescription = null, tint = WorkoraSecondaryText, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(if (isHindi) "अभी कोई बुकिंग नहीं है" else "No Bookings Yet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
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
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(bk.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                                            Text(bk.status, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraSuccessGreen)
                                        }
                                        Text("${bk.category} • ${bk.location}", fontSize = 13.sp, color = WorkoraSecondaryText)
                                        Text("₹${bk.rate}/${if (isHindi) "दिन" else "day"} • ${bk.date}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WorkoraAccentOrange)
                                    }
                                }
                            }
                        }
                    }
                }

                SettingsSubPage.NOTIFICATIONS -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            NotificationToggleRow(
                                title = if (isHindi) "सभी नोटिफिकेशन" else "All Notifications",
                                subtitle = if (isHindi) "मास्टर नोटिफिकेशन स्विच" else "Master notification switch",
                                checked = notificationsMasterToggle,
                                onCheckedChange = {
                                    notificationsMasterToggle = it
                                    settingsPrefs.edit().putBoolean("notif_master", it).apply()
                                }
                            )
                            HorizontalDivider(color = WorkoraBorderColor)
                            NotificationToggleRow(
                                title = if (isHindi) "काम की रिक्वेस्ट" else "Work requests",
                                subtitle = if (isHindi) "नए काम के अलर्ट प्राप्त करें" else "Receive alerts for new work requests",
                                checked = notifWorkRequests,
                                onCheckedChange = {
                                    notifWorkRequests = it
                                    settingsPrefs.edit().putBoolean("notif_work_requests", it).apply()
                                }
                            )
                            HorizontalDivider(color = WorkoraBorderColor)
                            NotificationToggleRow(
                                title = if (isHindi) "बुकिंग अपडेट्स" else "Booking updates",
                                subtitle = if (isHindi) "आपकी बुकिंग की स्थिति में बदलाव" else "Status changes on your active bookings",
                                checked = notifBookingUpdates,
                                onCheckedChange = {
                                    notifBookingUpdates = it
                                    settingsPrefs.edit().putBoolean("notif_booking_updates", it).apply()
                                }
                            )
                            HorizontalDivider(color = WorkoraBorderColor)
                            NotificationToggleRow(
                                title = if (isHindi) "चैट मैसेज" else "Messages",
                                subtitle = if (isHindi) "डायरेक्ट लाइव चैट नोटिफिकेशन" else "Direct chat notifications",
                                checked = notifMessages,
                                onCheckedChange = {
                                    notifMessages = it
                                    settingsPrefs.edit().putBoolean("notif_messages", it).apply()
                                }
                            )
                            HorizontalDivider(color = WorkoraBorderColor)
                            NotificationToggleRow(
                                title = if (isHindi) "प्रमोशनल अपडेट्स" else "Promotional notifications",
                                subtitle = if (isHindi) "टिप्स और नई घोषणाएं" else "Platform tips and announcements",
                                checked = notifPromotional,
                                onCheckedChange = {
                                    notifPromotional = it
                                    settingsPrefs.edit().putBoolean("notif_promotional", it).apply()
                                }
                            )
                        }
                    }
                }

                SettingsSubPage.CHANGE_PASSWORD -> {
                    var currentPassword by remember { mutableStateOf("") }
                    var newPassword by remember { mutableStateOf("") }
                    var confirmPassword by remember { mutableStateOf("") }
                    var errorMsg by remember { mutableStateOf<String?>(null) }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (isHindi) "पासवर्ड बदलें" else "Change Password", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                            OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it; errorMsg = null }, label = { Text(if (isHindi) "वर्तमान पासवर्ड" else "Current Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = newPassword, onValueChange = { newPassword = it; errorMsg = null }, label = { Text(if (isHindi) "नया पासवर्ड" else "New Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it; errorMsg = null }, label = { Text(if (isHindi) "नया पासवर्ड कन्फर्म करें" else "Confirm New Password") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())

                            if (!errorMsg.isNullOrBlank()) {
                                Text(errorMsg!!, color = WorkoraDangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    if (currentPassword.isBlank() || newPassword.length < 6 || newPassword != confirmPassword) {
                                        errorMsg = if (isHindi) "कृपया कम से कम 6 अक्षरों का सही पासवर्ड भरें।" else "Please enter valid matching passwords (min 6 characters)."
                                    } else {
                                        authPrefs.edit().putString("saved_password_$savedEmail", newPassword).apply()
                                        syncUserFieldToFirebase(savedEmail, "password", newPassword)
                                        Toast.makeText(context, if (isHindi) "पासवर्ड बदल दिया गया ✓" else "Password updated ✓", Toast.LENGTH_SHORT).show()
                                        currentSubPage = SettingsSubPage.MAIN
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(if (isHindi) "पासवर्ड अपडेट करें" else "Change Password", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SettingsSubPage.BLOCKED_USERS -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (isHindi) "कोई ब्लॉक यूजर नहीं है" else "No Blocked Users", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                            Text(if (isHindi) "आपने किसी भी यूजर को ब्लॉक नहीं किया है।" else "You haven't blocked any users on Workora.", fontSize = 13.sp, color = WorkoraSecondaryText)
                        }
                    }
                }

                SettingsSubPage.REPORT_PROBLEM -> {
                    var subject by remember { mutableStateOf("") }
                    var details by remember { mutableStateOf("") }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (isHindi) "समस्या रिपोर्ट करें" else "Report a Problem", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                            OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text(if (isHindi) "विषय (Subject)" else "Subject") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text(if (isHindi) "समस्या का विवरण" else "Problem Details") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                            Button(
                                onClick = {
                                    if (subject.isNotBlank() && details.isNotBlank()) {
                                        submitReportToFirebase(savedName, savedPhone, subject, details)
                                        Toast.makeText(context, if (isHindi) "रिपोर्ट भेज दी गई ✓" else "Report submitted ✓", Toast.LENGTH_SHORT).show()
                                        currentSubPage = SettingsSubPage.MAIN
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isHindi) "रिपोर्ट सबमिट करें" else "Submit Report", color = WorkoraWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SettingsSubPage.HELP_SUPPORT -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(if (isHindi) "सहायता और समर्थन" else "Help & Support", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                            Text("Support Email: ankitah994@gmail.com\nHelpline: +91 6265798340", fontSize = 14.sp, color = WorkoraSecondaryText)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = onOpenChat, colors = ButtonDefaults.buttonColors(containerColor = WorkoraPrimaryNavy), modifier = Modifier.weight(1f)) {
                                    Text(if (isHindi) "लाइव चैट सहायता" else "Live Support Chat", color = WorkoraWhite)
                                }
                                OutlinedButton(
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:+916265798340")))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (isHindi) "हेल्पलाइन कॉल करें" else "Call Helpline", color = WorkoraPrimaryNavy)
                                }
                            }
                        }
                    }
                }

                SettingsSubPage.ABOUT_WORKORA -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("WORKORA — Find. Hire. Work.", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = WorkoraPrimaryNavy)
                            Text(if (isHindi) "संस्करण 1.0.0" else "Version 1.0.0", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WorkoraAccentOrange)
                            Text(
                                text = if (isHindi) "Workora स्थानीय कारीगरों और ग्राहकों को बिना किसी कमीशन (0% Commission) के सीधे जोड़ता है।" else "Workora connects verified local workers and customers directly with 0% commission.",
                                fontSize = 13.sp,
                                color = WorkoraSecondaryText
                            )
                        }
                    }
                }

                SettingsSubPage.TERMS -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (isHindi) "नियम और शर्तें" else "Terms & Conditions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                            Text(
                                text = if (isHindi) "1. 0% कमीशन के साथ सीधी बुकिंग।\n2. सही प्रोफाइल और लोकेशन देना अनिवार्य है।\n3. गलत गतिविधि होने पर अकाउंट ब्लॉक किया जा सकता है।" else "1. Direct hiring with 0% commission.\n2. Accurate profile & location required.\n3. Fraudulent activity leads to account block.",
                                fontSize = 13.sp,
                                color = WorkoraSecondaryText
                            )
                        }
                    }
                }

                SettingsSubPage.PRIVACY_POLICY -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoraWhite),
                        border = BorderStroke(1.dp, WorkoraBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (isHindi) "गोपनीयता नीति" else "Privacy Policy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraMainText)
                            Text(
                                text = if (isHindi) "आपका व्यक्तिगत डेटा और मोबाइल नंबर पूरी तरह सुरक्षित है और केवल Workora पर काम व बुकिंग से जोड़ने के लिए उपयोग किया जाता है।" else "Your personal data and phone number are securely stored and used only for connecting bookings on Workora.",
                                fontSize = 13.sp,
                                color = WorkoraSecondaryText
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPhoneDialog) {
        var tempPhone by remember { mutableStateOf(savedPhone) }
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            containerColor = WorkoraWhite,
            title = { Text(if (isHindi) "मोबाइल नंबर अपडेट करें" else "Update Phone Number", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = { OutlinedTextField(value = tempPhone, onValueChange = { tempPhone = it }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempPhone.isNotBlank()) {
                            savedPhone = tempPhone.trim()
                            profilePrefs.edit().putString("user_phone", savedPhone).apply()
                            syncUserFieldToFirebase(savedEmail, "phone", savedPhone)
                        }
                        showPhoneDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange)
                ) { Text(if (isHindi) "सेव करें" else "Save", color = WorkoraWhite) }
            },
            dismissButton = { TextButton(onClick = { showPhoneDialog = false }) { Text(if (isHindi) "रद्द करें" else "Cancel") } }
        )
    }

    if (showEmailDialog) {
        var tempEmail by remember { mutableStateOf(savedEmail) }
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            containerColor = WorkoraWhite,
            title = { Text(if (isHindi) "ईमेल अपडेट करें" else "Update Email", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = { OutlinedTextField(value = tempEmail, onValueChange = { tempEmail = it }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempEmail.contains("@")) {
                            savedEmail = tempEmail.trim()
                            authPrefs.edit().putString("last_logged_in_email", savedEmail).apply()
                        }
                        showEmailDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange)
                ) { Text(if (isHindi) "सेव करें" else "Save", color = WorkoraWhite) }
            },
            dismissButton = { TextButton(onClick = { showEmailDialog = false }) { Text(if (isHindi) "रद्द करें" else "Cancel") } }
        )
    }

    if (showAreaDialog) {
        var tempState by remember { mutableStateOf(savedState) }
        var tempArea by remember { mutableStateOf(savedArea) }
        AlertDialog(
            onDismissRequest = { showAreaDialog = false },
            containerColor = WorkoraWhite,
            title = { Text(if (isHindi) "राज्य और शहर बदलें" else "Change State & Area", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = tempState, onValueChange = { tempState = it }, label = { Text(if (isHindi) "राज्य (State)" else "State") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = tempArea, onValueChange = { tempArea = it }, label = { Text(if (isHindi) "शहर / क्षेत्र (City / Area)" else "City / Area") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempArea.isNotBlank()) {
                            savedState = tempState.trim()
                            savedArea = tempArea.trim()
                            profilePrefs.edit()
                                .putString("user_state", savedState)
                                .putString("user_location", savedArea)
                                .apply()
                            syncUserFieldToFirebase(savedEmail, "state", savedState)
                            syncUserFieldToFirebase(savedEmail, "location", savedArea)
                            onUpdateProfile(savedName, savedPhone, savedArea)
                        }
                        showAreaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraAccentOrange)
                ) { Text(if (isHindi) "सेव करें" else "Save", color = WorkoraWhite) }
            },
            dismissButton = { TextButton(onClick = { showAreaDialog = false }) { Text(if (isHindi) "रद्द करें" else "Cancel") } }
        )
    }

    if (showLanguageDialog) {
        val languages = listOf("English" to "English", "Hindi" to "हिंदी (Hindi)")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = WorkoraWhite,
            title = { Text(if (isHindi) "पूरे ऐप की भाषा चुनें" else "Select App Language", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = {
                Column {
                    languages.forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    AppLanguageManager.setLanguage(context, code)
                                    Toast.makeText(
                                        context,
                                        if (code == "Hindi") "पूरे ऐप की भाषा हिंदी कर दी गई है ✓" else "App language changed to English ✓",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage.equals(code, true),
                                onClick = {
                                    AppLanguageManager.setLanguage(context, code)
                                    Toast.makeText(
                                        context,
                                        if (code == "Hindi") "पूरे ऐप की भाषा हिंदी कर दी गई है ✓" else "App language changed to English ✓",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 15.sp, color = WorkoraMainText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguageDialog = false }) { Text(if (isHindi) "बंद करें" else "Close") } }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = WorkoraWhite,
            title = { Text(if (isHindi) "लॉगआउट कन्फर्म करें" else "Confirm Logout", fontWeight = FontWeight.Bold, color = WorkoraMainText) },
            text = { Text(if (isHindi) "क्या आप वाकई अपने अकाउंट से लॉगआउट करना चाहते हैं?" else "Are you sure you want to sign out from your account?", color = WorkoraSecondaryText) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraDangerRed)
                ) { Text(if (isHindi) "लॉगआउट" else "Logout", color = WorkoraWhite, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text(if (isHindi) "रद्द करें" else "Cancel") } }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = WorkoraWhite,
            title = { Text(if (isHindi) "अकाउंट स्थायी रूप से डिलीट करें?" else "Delete Account Permanently?", fontWeight = FontWeight.Bold, color = WorkoraDangerRed) },
            text = { Text(if (isHindi) "चेतावनी: अकाउंट डिलीट होने के बाद वापस नहीं लाया जा सकता।" else "Warning: Account deletion is permanent and cannot be undone.", color = WorkoraMainText) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        deleteAccountFromFirebase(savedEmail)
                        profilePrefs.edit().clear().apply()
                        authPrefs.edit().clear().apply()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraDangerRed)
                ) { Text(if (isHindi) "स्थायी रूप से हटाएं" else "Delete Permanently", color = WorkoraWhite, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDeleteAccountDialog = false }) { Text(if (isHindi) "रद्द करें" else "Cancel") } }
        )
    }
}

@Composable
private fun SettingsBottomWaveDecoration() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(WorkoraBgGray)
    ) {
        val w = size.width
        val h = size.height

        val orangePath = Path().apply {
            moveTo(w * 0.45f, h)
            cubicTo(w * 0.65f, h * 0.1f, w * 0.85f, 0f, w, h * 0.35f)
            lineTo(w, h)
            close()
        }
        drawPath(path = orangePath, color = WorkoraAccentOrange)

        val navyPath = Path().apply {
            moveTo(0f, h * 0.25f)
            cubicTo(w * 0.28f, 0f, w * 0.48f, h * 0.95f, w, h * 0.45f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = navyPath, color = WorkoraPrimaryNavy)
    }
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
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
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
                fontWeight = FontWeight.Bold,
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
            tint = WorkoraMainText
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
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = WorkoraMainText)
            Text(subtitle, fontSize = 12.sp, color = WorkoraSecondaryText)
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
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
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
private fun SettingsWorkerAvatar(size: Dp = 66.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = Color(0xFFE2E8F0),
            radius = w * 0.5f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
        drawArc(
            color = WorkoraSuccessGreen,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.16f, h * 0.60f),
            size = Size(w * 0.68f, h * 0.54f)
        )
        drawCircle(
            color = Color(0xFFFFCC80),
            radius = w * 0.21f,
            center = Offset(w * 0.5f, h * 0.43f)
        )
        drawArc(
            color = WorkoraAccentOrange,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.25f, h * 0.15f),
            size = Size(w * 0.50f, h * 0.32f)
        )
        drawLine(
            color = WorkoraAccentOrange,
            start = Offset(w * 0.22f, h * 0.31f),
            end = Offset(w * 0.78f, h * 0.31f),
            strokeWidth = w * 0.06f,
            cap = StrokeCap.Round
        )
    }
}

private fun safeFirebaseKey(email: String): String {
    return email.trim().lowercase(Locale.US)
        .replace(".", "_")
        .replace("@", "_at_")
        .replace("+", "")
        .replace(" ", "")
}

private fun fetchRealUserProfileFromFirebase(
    email: String,
    onLoaded: (String, String, String, String, String, String, String) -> Unit
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
                    val state = obj.optString("state", "")
                    val loc = obj.optString("location", "")
                    val skill = obj.optString("skill", "")
                    val exp = obj.optString("experience", "")
                    val rate = obj.optInt("dailyRate", 0).takeIf { it > 0 }?.toString() ?: ""
                    Handler(Looper.getMainLooper()).post {
                        onLoaded(name, phone, state, loc, skill, exp, rate)
                    }
                    conn.disconnect()
                    return@Thread
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post {
            onLoaded("", "", "", "", "", "", "")
        }
    }.start()
}

private fun saveFullProfileToFirebase(
    email: String,
    name: String,
    phone: String,
    state: String,
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
                put("state", state)
                put("location", area)
                put("role", role)
                put("skill", skill)
                put("experience", experience)
                put("dailyRate", dailyRate)
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
            val payload = JSONObject().apply { put(fieldName, value) }
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
                                category = obj.optString("category", ""),
                                location = obj.optString("location", ""),
                                rate = obj.optInt("dailyRate", 0),
                                status = obj.optString("status", "PENDING").uppercase(Locale.US),
                                date = obj.optString("date", ""),
                                customerName = obj.optString("customerName", "")
                            )
                        )
                    }
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post { onLoaded(list) }
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
        Handler(Looper.getMainLooper()).post { onLoaded(list) }
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
