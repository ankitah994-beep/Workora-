package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted
import java.io.File
import java.io.FileOutputStream

private fun saveUriToInternalStorage(context: Context, uri: Uri, fileName: String): ImageBitmap? {
    return try {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
        if (bitmap != null) {
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            bitmap.asImageBitmap()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

private fun loadSavedBitmap(context: Context, fileName: String): ImageBitmap? {
    return try {
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ProfileScreen(
    role: UserRole = UserRole.CUSTOMER,
    userName: String = "Ankit Ahirwar",
    userPhone: String = "+91 98765 43210",
    userLocation: String = "Silwani, Raisen",
    toastMessage: String? = null,
    onBack: () -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onLogout: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenAdmin: () -> Unit = {},
    onUpdateProfile: (name: String, phone: String, location: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    var isInternalChatOpen by remember { mutableStateOf(false) }
    var isInternalAdminOpen by remember { mutableStateOf(false) }
    var isVerifiedAdminUser by remember { mutableStateOf(false) }
    var verifiedAdminTier by remember { mutableStateOf("SUPER_ADMIN") }

    // False = Save Icon is Orange, True = Save Icon turns White
    var isProfileSaved by remember { mutableStateOf(false) }

    var currentName by remember { mutableStateOf(prefs.getString("user_name", userName) ?: userName) }
    var currentPhone by remember { mutableStateOf(prefs.getString("user_phone", userPhone) ?: userPhone) }
    var currentLocation by remember { mutableStateOf(prefs.getString("user_location", userLocation) ?: userLocation) }
    var currentSkill by remember { mutableStateOf(prefs.getString("user_skill", "Mistri / Electrician / Painter") ?: "Mistri / Electrician / Painter") }
    var currentDailyRate by remember { mutableStateOf(prefs.getString("user_rate", "500") ?: "500") }

    // Complete App Settings States
    var isAvailableToday by remember { mutableStateOf(settingsPrefs.getBoolean("available_today", true)) }
    var allowDirectCalls by remember { mutableStateOf(settingsPrefs.getBoolean("allow_calls", true)) }
    var allowWhatsAppAlerts by remember { mutableStateOf(settingsPrefs.getBoolean("allow_whatsapp", true)) }
    var jobSoundAlerts by remember { mutableStateOf(settingsPrefs.getBoolean("job_sound_alerts", true)) }
    var selectedLanguage by remember { mutableStateOf(settingsPrefs.getString("app_language", "Hinglish") ?: "Hinglish") }
    var selectedRadius by remember { mutableStateOf(settingsPrefs.getString("search_radius", "10 KM") ?: "10 KM") }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }

    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val workPhotoBitmaps = remember { mutableStateListOf<ImageBitmap?>(null, null, null) }
    var selectedWorkPhotoIndex by remember { mutableIntStateOf(0) }

    val activeEmail = remember {
        val saved = authPrefs.getString("last_logged_in_email", "") ?: ""
        if (saved.isNotBlank()) saved else "ankitah994@gmail.com"
    }

    LaunchedEffect(Unit) {
        profileBitmap = loadSavedBitmap(context, "saved_profile_photo.jpg")
        for (i in 0..2) {
            workPhotoBitmaps[i] = loadSavedBitmap(context, "saved_work_photo_$i.jpg")
        }
        FirebaseManager.checkIfEmailIsAdminOnCloud(activeEmail) { isAdmin, tier ->
            isVerifiedAdminUser = isAdmin
            verifiedAdminTier = tier
        }
    }

    val profileFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedBmp = saveUriToInternalStorage(context, uri, "saved_profile_photo.jpg")
            if (savedBmp != null) {
                profileBitmap = savedBmp
                isProfileSaved = false
                Toast.makeText(context, "Profile Photo Selected! ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val workFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && selectedWorkPhotoIndex in 0..2) {
            val savedBmp = saveUriToInternalStorage(context, uri, "saved_work_photo_$selectedWorkPhotoIndex.jpg")
            if (savedBmp != null) {
                workPhotoBitmaps[selectedWorkPhotoIndex] = savedBmp
                isProfileSaved = false
                Toast.makeText(context, "Work Photo ${selectedWorkPhotoIndex + 1} Saved! ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveAllProfileAndSettings() {
        isProfileSaved = true
        prefs.edit()
            .putString("user_name", currentName.trim())
            .putString("user_phone", currentPhone.trim())
            .putString("user_location", currentLocation.trim())
            .putString("user_skill", currentSkill.trim())
            .putString("user_rate", currentDailyRate.trim())
            .apply()

        val pass = authPrefs.getString("user_pass_$activeEmail", "123456") ?: "123456"

        FirebaseManager.syncUserToFirebase(
            context = context,
            name = currentName.trim(),
            email = activeEmail,
            phone = currentPhone.trim(),
            password = pass,
            role = role.name
        )

        onUpdateProfile(currentName.trim(), currentPhone.trim(), currentLocation.trim())
        Toast.makeText(context, "Profile & Settings Saved! (Save Icon White ✓)", Toast.LENGTH_LONG).show()
    }

    if (isInternalAdminOpen) {
        AdminDashboardScreen(
            adminEmail = activeEmail,
            adminTier = verifiedAdminTier,
            onLogoutAdmin = {
                isInternalAdminOpen = false
            }
        )
        return
    }

    if (isInternalChatOpen) {
        ChatScreen(onBack = { isInternalChatOpen = false })
        return
    }

    val roleTitle = if (role == UserRole.CUSTOMER) "Customer / Hirer Mode" else "Worker / Labour Mode"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header with Settings Icon (⚙️) + Save Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(WorkoraNavy)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(WorkoraOrange),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Settings & Edit Profile", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text(text = "$roleTitle • $activeEmail", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            Button(
                onClick = { saveAllProfileAndSettings() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isProfileSaved) Color(0xFF16A34A) else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Save",
                    tint = if (isProfileSaved) Color.White else WorkoraOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isProfileSaved) "Saved" else "Save",
                    color = if (isProfileSaved) Color.White else WorkoraNavy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Admin Panel Quick Button (If User is Verified Admin)
            if (isVerifiedAdminUser) {
                Button(
                    onClick = {
                        authPrefs.edit().putString("saved_user_role", "ADMIN").apply()
                        isInternalAdminOpen = true
                        onOpenAdmin()
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, tint = Color.White, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Open Workora Admin Panel ($verifiedAdminTier)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            // ==================== 1. EDIT PROFILE & PHOTOS SECTION ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraOrange)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "1. Edit Profile & Photo (प्रोफाइल एडिट करें)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                    }

                    // Compact Profile Photo + Name Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clickable { profileFallbackLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(WorkoraOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profileBitmap != null) {
                                    Image(
                                        bitmap = profileBitmap!!,
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = WorkoraOrange,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(24.dp)
                                    .background(WorkoraNavy, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = currentName, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                            Text(text = currentSkill, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraOrange)
                            Text(text = "📍 $currentLocation • ₹$currentDailyRate/day", fontSize = 11.sp, color = WorkoraTextMuted)
                        }
                    }

                    OutlinedTextField(
                        value = currentName,
                        onValueChange = {
                            currentName = it
                            isProfileSaved = false
                        },
                        label = { Text("Full Name (पूरा नाम)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraOrange, unfocusedBorderColor = WorkoraBorder)
                    )

                    OutlinedTextField(
                        value = currentPhone,
                        onValueChange = {
                            currentPhone = it
                            isProfileSaved = false
                        },
                        label = { Text("Mobile Number (मोबाइल नंबर)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraOrange, unfocusedBorderColor = WorkoraBorder)
                    )

                    OutlinedTextField(
                        value = currentLocation,
                        onValueChange = {
                            currentLocation = it
                            isProfileSaved = false
                        },
                        label = { Text("Village / City (गाँव या शहर का पता)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraOrange, unfocusedBorderColor = WorkoraBorder)
                    )

                    OutlinedTextField(
                        value = currentSkill,
                        onValueChange = {
                            currentSkill = it
                            isProfileSaved = false
                        },
                        label = { Text("Your Work / Skill (आप क्या काम करते हैं?)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraOrange, unfocusedBorderColor = WorkoraBorder)
                    )

                    OutlinedTextField(
                        value = currentDailyRate,
                        onValueChange = {
                            currentDailyRate = it
                            isProfileSaved = false
                        },
                        label = { Text("Daily Wage ₹ (रोज़ की मजदूरी रेट)", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraOrange, unfocusedBorderColor = WorkoraBorder)
                    )

                    // 3 Work Proof Photos inside Profile Edit
                    Text(
                        text = "काम की 3 फोटो (Work Proof Portfolio - ${workPhotoBitmaps.count { it != null }}/3):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavy
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (index in 0..2) {
                            val bmp = workPhotoBitmaps[index]
                            Card(
                                onClick = {
                                    selectedWorkPhotoIndex = index
                                    workFallbackLauncher.launch("image/*")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(78.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = WorkoraBgLight),
                                border = BorderStroke(1.dp, if (bmp != null) Color(0xFF16A34A) else WorkoraOrange)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp,
                                            contentDescription = "Work Photo ${index + 1}",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(20.dp))
                                            Text("Photo ${index + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Save Button (Save Icon Orange -> White after saving!)
                    Button(
                        onClick = { saveAllProfileAndSettings() },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isProfileSaved) Color(0xFF16A34A) else WorkoraNavy
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            tint = if (isProfileSaved) Color.White else WorkoraOrange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isProfileSaved) "Profile Saved! (Icon White ✓)" else "Save Profile Changes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // ==================== 2. WORK & PRIVACY SETTINGS SECTION ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "2. Work & Privacy Settings (ऐप सेटिंग्स)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Available for Work Today", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                            Text("ग्राहकों को दिखाएं कि आप आज काम के लिए उपलब्ध हैं", fontSize = 10.sp, color = WorkoraTextMuted)
                        }
                        Switch(
                            checked = isAvailableToday,
                            onCheckedChange = {
                                isAvailableToday = it
                                settingsPrefs.edit().putBoolean("available_today", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF16A34A))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Direct Phone Calls", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                            Text("ग्राहक सीधे आपके नंबर पर कॉल कर सकें", fontSize = 10.sp, color = WorkoraTextMuted)
                        }
                        Switch(
                            checked = allowDirectCalls,
                            onCheckedChange = {
                                allowDirectCalls = it
                                settingsPrefs.edit().putBoolean("allow_calls", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WorkoraOrange)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("WhatsApp & Live Chat Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                            Text("नए काम और चैट के मैसेज अलर्ट प्राप्त करें", fontSize = 10.sp, color = WorkoraTextMuted)
                        }
                        Switch(
                            checked = allowWhatsAppAlerts,
                            onCheckedChange = {
                                allowWhatsAppAlerts = it
                                settingsPrefs.edit().putBoolean("allow_whatsapp", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WorkoraOrange)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("New Job Sound Notification", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                            Text("नया काम आने पर घंटी बजे", fontSize = 10.sp, color = WorkoraTextMuted)
                        }
                        Switch(
                            checked = jobSoundAlerts,
                            onCheckedChange = {
                                jobSoundAlerts = it
                                settingsPrefs.edit().putBoolean("job_sound_alerts", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WorkoraOrange)
                        )
                    }
                }
            }

            // ==================== 3. WORK SEARCH DISTANCE & LANGUAGE ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Work Search Distance (काम की दूरी)", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("5 KM", "10 KM", "25 KM", "All Area").forEach { radiusOption ->
                            val isSelected = selectedRadius == radiusOption
                            OutlinedButton(
                                onClick = {
                                    selectedRadius = radiusOption
                                    settingsPrefs.edit().putString("search_radius", radiusOption).apply()
                                    Toast.makeText(context, "Search Distance: $radiusOption ✓", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) WorkoraOrange else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isSelected) WorkoraOrange else WorkoraBorder)
                            ) {
                                Text(
                                    text = radiusOption,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else WorkoraTextDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("4. App Language (भाषा चुनें)", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Hindi", "Hinglish", "English").forEach { lang ->
                            val isSelected = selectedLanguage == lang
                            OutlinedButton(
                                onClick = {
                                    selectedLanguage = lang
                                    settingsPrefs.edit().putString("app_language", lang).apply()
                                    Toast.makeText(context, "Language: $lang ✓", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) WorkoraNavy else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isSelected) WorkoraNavy else WorkoraBorder)
                            ) {
                                Text(
                                    text = lang,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else WorkoraTextDark
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 5. SECURITY, CLOUD SYNC & ACCOUNT CONTROLS ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("5. Security, Cloud Sync & Account", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showChangePasswordDialog = true },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, WorkoraOrange),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change Password", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraOrange)
                        }

                        OutlinedButton(
                            onClick = { isInternalChatOpen = true },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, WorkoraNavy),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Live Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { saveAllProfileAndSettings() },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync to Cloud ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                context.cacheDir.deleteRecursively()
                                Toast.makeText(context, "App Cache Cleared! ✓", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, WorkoraBorder),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = WorkoraTextDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Cache", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
                        }
                    }

                    OutlinedButton(
                        onClick = onSwitchRole,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, WorkoraNavy)
                    ) {
                        Icon(imageVector = Icons.Default.SwapHoriz, tint = WorkoraNavy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Switch to ${if (role == UserRole.CUSTOMER) "Labour / Worker Mode" else "Customer / Hirer Mode"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraNavy
                        )
                    }

                    Button(
                        onClick = {
                            authPrefs.edit().putBoolean("is_logged_in", false).apply()
                            onLogout()
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, tint = Color.White, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Logout Account", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPasswordInput,
                    onValueChange = { newPasswordInput = it },
                    label = { Text("New Password (Min 6 chars)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanNewPass = newPasswordInput.trim()
                        if (cleanNewPass.length < 6) {
                            Toast.makeText(context, "Kam se kam 6 akshar dalein!", Toast.LENGTH_SHORT).show()
                        } else {
                            authPrefs.edit().putString("user_pass_$activeEmail", cleanNewPass).apply()
                            FirebaseManager.syncUserToFirebase(
                                context = context,
                                name = currentName,
                                email = activeEmail,
                                phone = currentPhone,
                                password = cleanNewPass,
                                role = role.name
                            )
                            newPasswordInput = ""
                            showChangePasswordDialog = false
                            Toast.makeText(context, "Password Saved on Cloud! ✓", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) {
                    Text("Save Password", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
