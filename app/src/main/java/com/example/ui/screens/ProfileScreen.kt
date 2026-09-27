package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole

private fun decodeBase64ToBitmap(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun encodeUriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else ""
    } catch (_: Exception) {
        ""
    }
}

@Composable
fun ProfileScreen(
    role: UserRole = UserRole.CUSTOMER,
    userName: String = "Ankit Ahirwar",
    userPhone: String = "+91 6265798340",
    userLocation: String = "Silwani, Raisen (MP)",
    onBack: () -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onUpdateProfile: (name: String, phone: String, location: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    val navyColor = Color(0xFF083D91)
    val orangeColor = Color(0xFFFF8C00)
    val bgColor = Color(0xFFF8FAFC)
    val textDark = Color(0xFF102A43)
    val textMuted = Color(0xFF667085)
    val borderColor = Color(0xFFE5E7EB)
    val greenColor = Color(0xFF22A06B)

    var appLang by remember {
        mutableStateOf(settingsPrefs.getString("app_language", "Hinglish") ?: "Hinglish")
    }

    fun tr(hi: String, hinglish: String, en: String): String {
        return when (appLang) {
            "Hindi" -> hi
            "English" -> en
            else -> hinglish
        }
    }

    var savedName by remember {
        mutableStateOf(profilePrefs.getString("user_name", userName) ?: userName)
    }
    var savedPhone by remember {
        mutableStateOf(profilePrefs.getString("user_phone", userPhone) ?: userPhone)
    }
    var savedLocation by remember {
        mutableStateOf(profilePrefs.getString("user_location", userLocation) ?: userLocation)
    }
    var savedSkill by remember {
        mutableStateOf(profilePrefs.getString("user_skill", "Mason (राजमिस्त्री)") ?: "Mason (राजमिस्त्री)")
    }
    var savedWage by remember {
        mutableStateOf(profilePrefs.getString("user_rate", "600") ?: "600")
    }

    var profilePicBase64 by remember {
        mutableStateOf(profilePrefs.getString("profile_photo_base64", "") ?: "")
    }
    var workPhoto1Base64 by remember {
        mutableStateOf(profilePrefs.getString("work_photo_1", "") ?: "")
    }
    var workPhoto2Base64 by remember {
        mutableStateOf(profilePrefs.getString("work_photo_2", "") ?: "")
    }
    var workPhoto3Base64 by remember {
        mutableStateOf(profilePrefs.getString("work_photo_3", "") ?: "")
    }

    var availableToday by remember {
        mutableStateOf(settingsPrefs.getBoolean("available_today", true))
    }
    var directCallsEnabled by remember {
        mutableStateOf(settingsPrefs.getBoolean("direct_calls", true))
    }
    var whatsappAlertsEnabled by remember {
        mutableStateOf(settingsPrefs.getBoolean("whatsapp_alerts", true))
    }
    var workRadiusKm by remember {
        mutableIntStateOf(settingsPrefs.getInt("work_radius_km", 10))
    }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var activePhotoSlot by remember { mutableIntStateOf(0) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeUriToBase64(context, uri)
            if (encoded.isNotBlank()) {
                when (activePhotoSlot) {
                    0 -> {
                        profilePicBase64 = encoded
                        profilePrefs.edit().putString("profile_photo_base64", encoded).apply()
                    }
                    1 -> {
                        workPhoto1Base64 = encoded
                        profilePrefs.edit().putString("work_photo_1", encoded).apply()
                    }
                    2 -> {
                        workPhoto2Base64 = encoded
                        profilePrefs.edit().putString("work_photo_2", encoded).apply()
                    }
                    3 -> {
                        workPhoto3Base64 = encoded
                        profilePrefs.edit().putString("work_photo_3", encoded).apply()
                    }
                }
                Toast.makeText(context, "Photo Updated ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val loggedEmail = remember {
        (authPrefs.getString("last_logged_in_email", "") ?: "").trim().lowercase()
    }
    val isSuperAdmin = remember(loggedEmail, savedPhone) {
        loggedEmail == "ankitah994@gmail.com" ||
                loggedEmail.contains("ankitah994") ||
                savedPhone.filter { it.isDigit() }.takeLast(10) == "6265798340" ||
                authPrefs.getString("saved_user_role", "") == "ADMIN"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // 1. Navy Header Bar (Matches styles.css .header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(navyColor)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tr("प्रोफाइल और सेटिंग्स", "Profile & Settings", "Profile & Settings"),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = { showEditProfileDialog = true },
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = tr("बदलें", "Edit", "Edit"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 2. Profile Top Card (Matches .worker-profile-top / .profile-card)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val avatarBitmap = remember(profilePicBase64) { decodeBase64ToBitmap(profilePicBase64) }
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(navyColor)
                                .clickable {
                                    activePhotoSlot = 0
                                    imagePickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = savedName.take(1).uppercase(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = savedName,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified",
                                    tint = greenColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (role == UserRole.LABOUR) "$savedSkill • ₹$savedWage/day" else tr("ग्राहक अकाउंट (Customer)", "Customer / Hirer Account", "Customer / Hirer Account"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = orangeColor
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = textMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = savedLocation,
                                    fontSize = 12.sp,
                                    color = textMuted
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = textMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = savedPhone,
                                    fontSize = 12.sp,
                                    color = textMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-Column Info Row (Matches .info-row in styles.css)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("4.9 ★", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = navyColor)
                            Text(tr("रेटिंग", "Rating", "Rating"), fontSize = 11.sp, color = textMuted)
                        }
                        Box(modifier = Modifier.width(1.dp).height(28.dp).background(borderColor))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("₹$savedWage", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = navyColor)
                            Text(tr("दिहाड़ी/दिन", "Daily Wage", "Daily Wage"), fontSize = 11.sp, color = textMuted)
                        }
                        Box(modifier = Modifier.width(1.dp).height(28.dp).background(borderColor))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (role == UserRole.LABOUR) "WORKER" else "HIRER", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = orangeColor)
                            Text(tr("सक्रिय मोड", "Active Role", "Active Role"), fontSize = 11.sp, color = textMuted)
                        }
                    }
                }
            }

            // 3. Language Selection Card (Hindi / Hinglish / English)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = tr("ऐप की भाषा चुनें (App Language)", "App Language (भाषा चुनें)", "Select App Language"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Hindi" to "हिंदी", "Hinglish" to "Hinglish", "English" to "English").forEach { (code, label) ->
                            val selected = appLang == code
                            Button(
                                onClick = {
                                    appLang = code
                                    settingsPrefs.edit().putString("app_language", code).apply()
                                    Toast.makeText(context, "Language set to $label ✓", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) navyColor else bgColor
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else textDark
                                )
                            }
                        }
                    }
                }
            }

            // 4. Work Proof Photos (3 Slots)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = tr("काम की फ़ोटो (Work Proof Photos)", "Work Proof Photos (काम की फोटो)", "Work Proof Photos"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            1 to workPhoto1Base64,
                            2 to workPhoto2Base64,
                            3 to workPhoto3Base64
                        ).forEach { (slot, base64Data) ->
                            val bmp = remember(base64Data) { decodeBase64ToBitmap(base64Data) }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(86.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .clickable {
                                        activePhotoSlot = slot
                                        imagePickerLauncher.launch("image/*")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (bmp != null) {
                                    Image(
                                        bitmap = bmp,
                                        contentDescription = "Work Photo $slot",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.AddAPhoto,
                                            contentDescription = null,
                                            tint = orangeColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Photo $slot",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Availability & Settings Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("आज काम के लिए उपलब्ध", "Available for Work Today", "Available for Work Today"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Text(
                                text = tr("ग्राहकों को आपकी प्रोफाइल दिखेगी", "Show profile in available workers list", "Show profile in available workers list"),
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                        Switch(
                            checked = availableToday,
                            onCheckedChange = {
                                availableToday = it
                                settingsPrefs.edit().putBoolean("available_today", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = greenColor)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("सीधे फोन कॉल की अनुमति", "Allow Direct Phone Calls", "Allow Direct Phone Calls"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Text(
                                text = tr("ग्राहक सीधे कॉल कर सकेंगे", "Customers can call your mobile number", "Customers can call your mobile number"),
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                        Switch(
                            checked = directCallsEnabled,
                            onCheckedChange = {
                                directCallsEnabled = it
                                settingsPrefs.edit().putBoolean("direct_calls", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = navyColor)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tr("व्हाट्सएप और जॉब अलर्ट", "Job & Chat Notifications", "Job & Chat Notifications"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark
                            )
                            Text(
                                text = tr("नए काम की तुरंत सूचना पाएं", "Receive instant alerts for nearby jobs", "Receive instant alerts for nearby jobs"),
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                        Switch(
                            checked = whatsappAlertsEnabled,
                            onCheckedChange = {
                                whatsappAlertsEnabled = it
                                settingsPrefs.edit().putBoolean("whatsapp_alerts", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = orangeColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tr("काम की दूरी (Work Distance Radius): $workRadiusKm KM", "Work Distance Radius: $workRadiusKm KM", "Work Distance Radius: $workRadiusKm KM"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 25).forEach { km ->
                            val selected = workRadiusKm == km
                            OutlinedButton(
                                onClick = {
                                    workRadiusKm = km
                                    settingsPrefs.edit().putInt("work_radius_km", km).apply()
                                },
                                modifier = Modifier.weight(1f).height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (selected) orangeColor else borderColor),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) Color(0xFFFFF0DE) else Color.White
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "$km KM",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) orangeColor else textDark
                                )
                            }
                        }
                    }
                }
            }

            // 6. Action Menu Rows (Matches .menu-row in styles.css)
            Card(
                onClick = onSwitchRole,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = navyColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tr("रोल बदलें (Customer ⇄ Worker)", "Switch Role (Customer ⇄ Worker)", "Switch Role (Customer ⇄ Worker)"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textMuted)
                }
            }

            Card(
                onClick = onOpenChat,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = orangeColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = tr("लाइव चैट खोलें (Messages)", "Open Online Live Chat", "Open Online Live Chat"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark
                        )
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textMuted)
                }
            }

            if (isSuperAdmin) {
                Card(
                    onClick = onOpenAdmin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = navyColor),
                    border = BorderStroke(1.5.dp, orangeColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = orangeColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Open Super Admin Panel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = orangeColor)
                    }
                }
            }

            // 7. Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB42318))
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tr("लॉग आउट करें (Log Out)", "Log Out", "Log Out"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(savedName) }
        var editPhone by remember { mutableStateOf(savedPhone) }
        var editLocation by remember { mutableStateOf(savedLocation) }
        var editSkill by remember { mutableStateOf(savedSkill) }
        var editWage by remember { mutableStateOf(savedWage) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "Edit Profile Details",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = navyColor
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Mobile Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Location (Village / City)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSkill,
                        onValueChange = { editSkill = it },
                        label = { Text("Primary Skill (e.g. Mason, Electrician)") },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editWage,
                        onValueChange = { editWage = it },
                        label = { Text("Daily Wage (₹/day)") },
                        leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        savedName = editName.trim().ifBlank { "Ankit Ahirwar" }
                        savedPhone = editPhone.trim().ifBlank { "+91 6265798340" }
                        savedLocation = editLocation.trim().ifBlank { "Silwani, Raisen (MP)" }
                        savedSkill = editSkill.trim().ifBlank { "Mason" }
                        savedWage = editWage.trim().ifBlank { "600" }

                        profilePrefs.edit().apply {
                            putString("user_name", savedName)
                            putString("user_phone", savedPhone)
                            putString("user_location", savedLocation)
                            putString("user_skill", savedSkill)
                            putString("user_rate", savedWage)
                            apply()
                        }

                        onUpdateProfile(savedName, savedPhone, savedLocation)
                        showEditProfileDialog = false
                        Toast.makeText(context, "Profile Saved Successfully! ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = orangeColor)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Safeguard top-level composable so any screen in com.example.ui.screens calling LiveLocationAutoCompleteField compiles cleanly
@Composable
fun LiveLocationAutoCompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Village / City Location",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFFFF8C00)
            )
        },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}
