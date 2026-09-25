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
    userPhone: String = "+91 6265798340",
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

    var isSavedState by remember { mutableStateOf(false) }

    var currentName by remember { mutableStateOf(prefs.getString("user_name", userName) ?: userName) }
    var currentPhone by remember { mutableStateOf(prefs.getString("user_phone", userPhone) ?: userPhone) }
    var currentLocation by remember { mutableStateOf(prefs.getString("user_location", userLocation) ?: userLocation) }
    var currentSkill by remember { mutableStateOf(prefs.getString("user_skill", "Mistri / Electrician / Painter") ?: "Mistri / Electrician / Painter") }
    var currentDailyRate by remember { mutableStateOf(prefs.getString("user_rate", "500") ?: "500") }

    var isAvailableToday by remember { mutableStateOf(settingsPrefs.getBoolean("available_today", true)) }
    var allowDirectCalls by remember { mutableStateOf(settingsPrefs.getBoolean("allow_calls", true)) }
    var allowWhatsAppAlerts by remember { mutableStateOf(settingsPrefs.getBoolean("allow_whatsapp", true)) }
    var jobSoundAlerts by remember { mutableStateOf(settingsPrefs.getBoolean("job_sound_alerts", true)) }
    var selectedLanguage by remember { mutableStateOf(settingsPrefs.getString("app_language", "Hinglish") ?: "Hinglish") }
    var selectedRadius by remember { mutableStateOf(settingsPrefs.getString("search_radius", "10 KM") ?: "10 KM") }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }

    // Dynamic Translator Function based on selectedLanguage ("Hindi", "Hinglish", "English")
    fun tr(hindi: String, hinglish: String, english: String): String {
        return when (selectedLanguage) {
            "Hindi" -> hindi
            "English" -> english
            else -> hinglish
        }
    }

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
                isSavedState = false
                Toast.makeText(context, tr("प्रोफाइल फोटो चुन ली गई! सेव दबाएं ✓", "Profile Photo Selected! Save Dabayein ✓", "Profile Photo Selected! Tap Save ✓"), Toast.LENGTH_SHORT).show()
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
                isSavedState = false
                Toast.makeText(context, tr("काम की फोटो ${selectedWorkPhotoIndex + 1} सेव हो गई! ✓", "Work Photo ${selectedWorkPhotoIndex + 1} Saved! ✓", "Work Photo ${selectedWorkPhotoIndex + 1} Saved! ✓"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveAllProfileAndSettings() {
        isSavedState = true
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
        Toast.makeText(
            context,
            tr("सेटिंग्स और प्रोफाइल सेव हो गई! ✓", "Settings & Profile Saved! ✓", "Settings & Profile Saved! ✓"),
            Toast.LENGTH_SHORT
        ).show()
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

    val roleTitle = if (role == UserRole.CUSTOMER) {
        tr("ग्राहक / काम देने वाला मोड", "Customer / Hirer Mode", "Customer / Hirer Mode")
    } else {
        tr("कारीगर / मजदूर मोड", "Worker / Labour Mode", "Worker / Labour Mode")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header Bar
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
                IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = WorkoraOrange, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = tr("सेटिंग्स और प्रोफाइल एडिट", "Settings & Profile Edit", "Settings & Profile Edit"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(text = "$activeEmail • $selectedLanguage", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
                }
            }

            Button(
                onClick = { saveAllProfileAndSettings() },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSavedState) Color(0xFF16A34A) else Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Save",
                    tint = if (isSavedState) Color.White else WorkoraOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSavedState) tr("सेव ✓", "Saved ✓", "Saved ✓") else tr("सेव करें", "Save", "Save"),
                    color = if (isSavedState) Color.White else WorkoraNavy,
                    fontSize = 12.sp,
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
            if (isVerifiedAdminUser) {
                Button(
                    onClick = {
                        authPrefs.edit().putString("saved_user_role", "ADMIN").apply()
                        isInternalAdminOpen = true
                        onOpenAdmin()
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(imageVector = Icons.Default.VerifiedUser, tint = Color.White, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tr("Workora एडमिन पैनल खोलें ($verifiedAdminTier)", "Open Workora Admin Panel ($verifiedAdminTier)", "Open Workora Admin Panel ($verifiedAdminTier)"),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            // ==================== SECTION 1: EDIT PROFILE & PHOTOS ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = tr("1. अपनी प्रोफाइल और फोटो एडिट करें", "1. Edit Profile & Photo (Proflie Edit Karein)", "1. Edit Your Profile & Photos"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clickable { profileFallbackLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
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
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = WorkoraOrange,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(26.dp)
                                    .background(WorkoraNavy, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Change Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = currentName, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)
                            Text(text = roleTitle, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraOrange)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { profileFallbackLauncher.launch("image/*") },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(tr("प्रोफाइल फोटो बदलें", "Profile Photo Badlein", "Change Profile Photo"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = currentName,
                        onValueChange = {
                            currentName = it
                            isSavedState = false
                        },
                        label = { Text(tr("पूरा नाम", "Full Name (Poora Naam)", "Full Name"), fontSize = 12.sp) },
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
                            isSavedState = false
                        },
                        label = { Text(tr("मोबाइल नंबर", "Mobile Number", "Mobile Number"), fontSize = 12.sp) },
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
                            isSavedState = false
                        },
                        label = { Text(tr("गाँव या शहर का पता", "Village / City (Gaon ya Shahar)", "Village / City Address"), fontSize = 12.sp) },
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
                            isSavedState = false
                        },
                        label = { Text(tr("आपका काम / हुनर (जैसे: मिस्त्री)", "Aapka Kaam / Skill (e.g. Mistri)", "Your Work / Skill"), fontSize = 12.sp) },
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
                            isSavedState = false
                        },
                        label = { Text(tr("रोज़ की दिहाड़ी रेट ₹", "Daily Wage ₹ (Roz ki Dihadi)", "Daily Wage Rate ₹"), fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraOrange, unfocusedBorderColor = WorkoraBorder)
                    )

                    Text(
                        text = tr("काम की 3 फोटो (${workPhotoBitmaps.count { it != null }}/3 सेव):", "Kaam ki 3 Photo (${workPhotoBitmaps.count { it != null }}/3 Saved):", "3 Work Proof Photos (${workPhotoBitmaps.count { it != null }}/3 Saved):"),
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
                                            Text(tr("फोटो ${index + 1}", "Photo ${index + 1}", "Photo ${index + 1}"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { saveAllProfileAndSettings() },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSavedState) Color(0xFF16A34A) else WorkoraNavy
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            tint = if (isSavedState) Color.White else WorkoraOrange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSavedState) tr("प्रोफाइल सेव हो गई! ✓", "Profile Saved! ✓", "Profile Saved! ✓") else tr("प्रोफाइल सेव करें", "Save Profile Changes", "Save Profile Changes"),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            // ==================== SECTION 2: WORK & PRIVACY SETTINGS ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = tr("2. काम और प्राइवेसी सेटिंग्स", "2. Work & Privacy Settings", "2. Work & Privacy Settings"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tr("आज काम के लिए उपलब्ध हैं", "Available for Work Today", "Available for Work Today"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                            Text(tr("ग्राहकों को दिखाएं कि आप आज काम के लिए तैयार हैं", "Grahakon ko dikhayein ki aap aaj kaam ke liye available hain", "Show customers you are available today"), fontSize = 10.sp, color = WorkoraTextMuted)
                        }
                        Switch(
                            checked = isAvailableToday,
                            onCheckedChange = {
                                isAvailableToday = it
                                isSavedState = false
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
                            Text(tr("सीधे फ़ोन कॉल की अनुमति दें", "Allow Direct Phone Calls", "Allow Direct Phone Calls"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                            Text(tr("ग्राहक सीधे आपके नंबर पर कॉल कर सकें", "Grahak seedhe aapke number par call kar sakein", "Let customers call your phone number directly"), fontSize = 10.sp, color = WorkoraTextMuted)
                        }
                        Switch(
                            checked = allowDirectCalls,
                            onCheckedChange = {
                                allowDirectCalls = it
                                isSavedState = false
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
                            Text(tr("व्हाट्सएप और लाइव चैट अलर्ट", "WhatsApp & Live Chat Alerts", "WhatsApp & Live Chat Alerts"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                            Text(tr("नए काम और चैट के मैसेज अलर्ट प्राप्त करें", "Naye kaam aur chat ke message alert prapt karein", "Receive new job and live chat alerts"), fontSize = 10.sp, color = WorkoraTextMuted)
                        }
                        Switch(
                            checked = allowWhatsAppAlerts,
                            onCheckedChange = {
                                allowWhatsAppAlerts = it
                                isSavedState = false
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
                            Text(tr("नए काम की घंटी (साउंड अलर्ट)", "New Job Sound Notification", "New Job Sound Notification"), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                            Text(tr("नया काम आने पर घंटी बजे", "Naya kaam aane par ghanti baje", "Play sound alert when a new job arrives"), fontSize = 10.sp, color = WorkoraTextMuted)
                        }
                        Switch(
                            checked = jobSoundAlerts,
                            onCheckedChange = {
                                jobSoundAlerts = it
                                isSavedState = false
                                settingsPrefs.edit().putBoolean("job_sound_alerts", it).apply()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = WorkoraOrange)
                        )
                    }
                }
            }

            // ==================== SECTION 3: DISTANCE & LIVE LANGUAGE SWITCHER ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = tr("3. काम की दूरी चुनें", "3. Work Search Distance (Kaam ki Doori)", "3. Work Search Distance"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("5 KM", "10 KM", "25 KM", "All Area").forEach { radiusOption ->
                            val isSelected = selectedRadius == radiusOption
                            OutlinedButton(
                                onClick = {
                                    selectedRadius = radiusOption
                                    isSavedState = false
                                    settingsPrefs.edit().putString("search_radius", radiusOption).apply()
                                    Toast.makeText(context, "Distance: $radiusOption ✓", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(vertical = 4.dp),
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
                    Text(
                        text = tr("4. ऐप की भाषा चुनें (तुरंत बदलेगी)", "4. App Language (Tap karte hi badlegi)", "4. Select App Language (Instant Switch)"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "Hindi" to "हिंदी (Hindi)",
                            "Hinglish" to "Hinglish",
                            "English" to "English"
                        ).forEach { (langKey, langLabel) ->
                            val isSelected = selectedLanguage == langKey
                            OutlinedButton(
                                onClick = {
                                    selectedLanguage = langKey
                                    isSavedState = false
                                    settingsPrefs.edit().putString("app_language", langKey).apply()
                                    Toast.makeText(
                                        context,
                                        when (langKey) {
                                            "Hindi" -> "ऐप की भाषा हिंदी कर दी गई है! ✓"
                                            "English" -> "App Language changed to English! ✓"
                                            else -> "App Language Hinglish ho gayi hai! ✓"
                                        },
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) WorkoraNavy else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isSelected) WorkoraNavy else WorkoraBorder)
                            ) {
                                Text(
                                    text = langLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else WorkoraTextDark
                                )
                            }
                        }
                    }
                }
            }

            // ==================== SECTION 4: SECURITY, PASSWORD & ACCOUNT CONTROLS ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = tr("5. सुरक्षा, पासवर्ड और अकाउंट कंट्रोल", "5. Security, Cloud Sync & Account", "5. Security, Cloud Sync & Account"),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )

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
                            Text(tr("पासवर्ड बदलें", "Change Password", "Change Password"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraOrange)
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
                            Text(tr("व्हाट्सएप चैट्स खोलें", "Open All Chats", "Open All Chats"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
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
                            Text(tr("क्लाउड सिंक ✓", "Sync to Cloud ✓", "Sync to Cloud ✓"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                context.cacheDir.deleteRecursively()
                                Toast.makeText(context, tr("कैश साफ़ हो गया! ✓", "App Cache Cleared! ✓", "App Cache Cleared! ✓"), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, WorkoraBorder),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = WorkoraTextDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(tr("कैश साफ़ करें", "Clear Cache", "Clear Cache"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
                        }
                    }

                    OutlinedButton(
                        onClick = onSwitchRole,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, WorkoraNavy)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (role == UserRole.CUSTOMER) {
                                tr("मजदूर / कारीगर मोड में बदलें", "Switch to Labour / Worker Mode", "Switch to Labour / Worker Mode")
                            } else {
                                tr("ग्राहक / काम देने वाले मोड में बदलें", "Switch to Customer / Hirer Mode", "Switch to Customer / Hirer Mode")
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraNavy
                        )
                    }

                    Button(
                        onClick = {
                            authPrefs.edit().putBoolean("is_logged_in", false).remove("saved_user_role").apply()
                            onLogout()
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, tint = Color.White, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = tr("लॉगआउट करें", "Logout Account", "Logout Account"), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text(tr("पासवर्ड बदलें", "Change Password", "Change Password"), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = newPasswordInput,
                    onValueChange = { newPasswordInput = it },
                    label = { Text(tr("नया पासवर्ड (कम से कम 6 अक्षर)", "New Password (Min 6 chars)", "New Password (Min 6 chars)")) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanNewPass = newPasswordInput.trim()
                        if (cleanNewPass.length < 6) {
                            Toast.makeText(context, tr("कम से कम 6 अक्षर डालें!", "Kam se kam 6 akshar dalein!", "Enter at least 6 characters!"), Toast.LENGTH_SHORT).show()
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
                            isSavedState = true
                            Toast.makeText(context, tr("पासवर्ड बदल गया! ✓", "Password Updated! ✓", "Password Updated! ✓"), Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) {
                    Text(tr("सेव करें", "Save Password", "Save Password"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showChangePasswordDialog = false }) {
                    Text(tr("रद्द करें", "Cancel", "Cancel"))
                }
            }
        )
    }
}
