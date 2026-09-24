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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
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
    onUpdateProfile: (name: String, phone: String, location: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    // Sub-screens inside Profile so navigation never fails
    var isAppSettingsOpen by remember { mutableStateOf(false) }
    var isInternalChatOpen by remember { mutableStateOf(false) }

    // Editable User Profile Details
    var currentName by remember { mutableStateOf(prefs.getString("user_name", userName) ?: userName) }
    var currentPhone by remember { mutableStateOf(prefs.getString("user_phone", userPhone) ?: userPhone) }
    var currentLocation by remember { mutableStateOf(prefs.getString("user_location", userLocation) ?: userLocation) }
    var currentSkill by remember { mutableStateOf(prefs.getString("user_skill", "Mistri / Electrician / Painter") ?: "Mistri / Electrician / Painter") }
    var currentDailyRate by remember { mutableStateOf(prefs.getString("user_rate", "500") ?: "500") }

    // App Settings States
    var isAvailableToday by remember { mutableStateOf(settingsPrefs.getBoolean("available_today", true)) }
    var allowDirectCalls by remember { mutableStateOf(settingsPrefs.getBoolean("allow_calls", true)) }
    var allowWhatsAppAlerts by remember { mutableStateOf(settingsPrefs.getBoolean("allow_whatsapp", true)) }
    var jobSoundAlerts by remember { mutableStateOf(settingsPrefs.getBoolean("job_sound_alerts", true)) }
    var selectedLanguage by remember { mutableStateOf(settingsPrefs.getString("app_language", "Hinglish") ?: "Hinglish") }
    var selectedRadius by remember { mutableStateOf(settingsPrefs.getString("search_radius", "10 KM") ?: "10 KM") }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }

    // Photos
    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val workPhotoBitmaps = remember { mutableStateListOf<ImageBitmap?>(null, null, null) }
    var selectedWorkPhotoIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        profileBitmap = loadSavedBitmap(context, "saved_profile_photo.jpg")
        for (i in 0..2) {
            workPhotoBitmaps[i] = loadSavedBitmap(context, "saved_work_photo_$i.jpg")
        }
    }

    val profileDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedBmp = saveUriToInternalStorage(context, uri, "saved_profile_photo.jpg")
            if (savedBmp != null) {
                profileBitmap = savedBmp
                Toast.makeText(context, "Profile Photo Saved! ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val profileFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedBmp = saveUriToInternalStorage(context, uri, "saved_profile_photo.jpg")
            if (savedBmp != null) {
                profileBitmap = savedBmp
                Toast.makeText(context, "Profile Photo Saved! ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val workDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && selectedWorkPhotoIndex in 0..2) {
            val savedBmp = saveUriToInternalStorage(context, uri, "saved_work_photo_$selectedWorkPhotoIndex.jpg")
            if (savedBmp != null) {
                workPhotoBitmaps[selectedWorkPhotoIndex] = savedBmp
                Toast.makeText(context, "Work Photo ${selectedWorkPhotoIndex + 1} Saved! ✓", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Work Photo ${selectedWorkPhotoIndex + 1} Saved! ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openProfileGallery() {
        try {
            profileDocumentLauncher.launch(arrayOf("image/*"))
        } catch (e: Exception) {
            profileFallbackLauncher.launch("image/*")
        }
    }

    fun openWorkPhotoGallery(index: Int) {
        selectedWorkPhotoIndex = index
        try {
            workDocumentLauncher.launch(arrayOf("image/*"))
        } catch (e: Exception) {
            workFallbackLauncher.launch("image/*")
        }
    }

    fun saveAllProfileInformation() {
        prefs.edit()
            .putString("user_name", currentName.trim())
            .putString("user_phone", currentPhone.trim())
            .putString("user_location", currentLocation.trim())
            .putString("user_skill", currentSkill.trim())
            .putString("user_rate", currentDailyRate.trim())
            .apply()

        val email = authPrefs.getString("last_logged_in_email", "user@workora.com") ?: "user@workora.com"
        val pass = authPrefs.getString("user_pass_$email", "123456") ?: "123456"

        FirebaseManager.syncUserToFirebase(
            context = context,
            name = currentName.trim(),
            email = email,
            phone = currentPhone.trim(),
            password = pass,
            role = role.name
        )

        onUpdateProfile(currentName.trim(), currentPhone.trim(), currentLocation.trim())
        Toast.makeText(context, "Profile & Photos Permanently Saved! ✓", Toast.LENGTH_LONG).show()
    }

    // 1. If user clicked Live Chat button inside Profile
    if (isInternalChatOpen) {
        ChatScreen(onBack = { isInternalChatOpen = false })
        return
    }

    // 2. If user clicked App Settings button inside Profile
    if (isAppSettingsOpen) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WorkoraBgLight)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(onClick = { isAppSettingsOpen = false }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = WorkoraNavy)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "App Settings (ऐप सेटिंग्स)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraTextDark
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Work & Privacy Settings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Available for Work Today", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkoraTextDark)
                                Text("ग्राहकों को दिखाएं कि आप आज काम के लिए उपलब्ध हैं", fontSize = 11.sp, color = WorkoraTextMuted)
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
                                Text("Allow Direct Phone Calls", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkoraTextDark)
                                Text("ग्राहक सीधे आपके नंबर पर कॉल कर सकें", fontSize = 11.sp, color = WorkoraTextMuted)
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
                                Text("WhatsApp & Chat Alerts", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkoraTextDark)
                                Text("नए काम के मैसेज अलर्ट प्राप्त करें", fontSize = 11.sp, color = WorkoraTextMuted)
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
                                Text("New Job Sound Notification", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = WorkoraTextDark)
                                Text("नया काम आने पर घंटी बजे", fontSize = 11.sp, color = WorkoraTextMuted)
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

                // Work Distance Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Work Search Distance (काम की दूरी)", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("5 KM", "10 KM", "25 KM", "All Area").forEach { radiusOption ->
                                val isSelected = selectedRadius == radiusOption
                                OutlinedButton(
                                    onClick = {
                                        selectedRadius = radiusOption
                                        settingsPrefs.edit().putString("search_radius", radiusOption).apply()
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) WorkoraOrange else Color.White
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) WorkoraOrange else WorkoraBorder)
                                ) {
                                    Text(
                                        text = radiusOption,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else WorkoraTextDark
                                    )
                                }
                            }
                        }
                    }
                }

                // Language Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("App Language (भाषा चुनें)", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Hindi", "Hinglish", "English").forEach { lang ->
                                val isSelected = selectedLanguage == lang
                                OutlinedButton(
                                    onClick = {
                                        selectedLanguage = lang
                                        settingsPrefs.edit().putString("app_language", lang).apply()
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    shape = RoundedCornerShape(10.dp),
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

                // Security & Backup Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Security & Cloud Sync", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)

                        OutlinedButton(
                            onClick = { showChangePasswordDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WorkoraOrange)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change Password (पासवर्ड बदलें)", fontWeight = FontWeight.Bold, color = WorkoraOrange)
                        }

                        Button(
                            onClick = { saveAllProfileInformation() },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sync Data to Firebase Cloud ✓", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                context.cacheDir.deleteRecursively()
                                Toast.makeText(context, "Cache Cleared! ✓", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, WorkoraBorder)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = WorkoraTextDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear App Cache", fontWeight = FontWeight.Bold, color = WorkoraTextDark)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = WorkoraNavy)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Workora App • Cloud Live v2.5", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WorkoraTextDark)
                            Text("Firebase DB: workora-d8b51", fontSize = 11.sp, color = Color(0xFF16A34A))
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
                                val email = authPrefs.getString("last_logged_in_email", "user@workora.com") ?: "user@workora.com"
                                authPrefs.edit().putString("user_pass_$email", cleanNewPass).apply()
                                FirebaseManager.syncUserToFirebase(
                                    context = context,
                                    name = currentName,
                                    email = email,
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
        return
    }

    // 3. Main Profile Screen View
    val roleTitle = if (role == UserRole.CUSTOMER) "Hirer / Customer Profile" else "Worker / Labour Profile"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = WorkoraNavy)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "My Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isAppSettingsOpen = true }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = WorkoraNavy)
                }
                Button(
                    onClick = { saveAllProfileInformation() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Save", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clickable { openProfileGallery() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
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
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { openProfileGallery() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .background(WorkoraNavy, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Upload Profile Photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { openProfileGallery() },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, WorkoraOrange),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (profileBitmap != null) "Change Profile Photo ✓" else "Upload Profile Photo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraOrange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = currentName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)

            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(WorkoraNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = roleTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live Chat + App Settings Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { isInternalChatOpen = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraNavy)
                ) {
                    Icon(imageVector = Icons.Default.Email, tint = Color.White, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Live Chat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { isAppSettingsOpen = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) {
                    Icon(imageVector = Icons.Default.Settings, tint = Color.White, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "App Settings", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // 3 Work Portfolio Photos Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "काम की 3 फोटो (Work Proof Portfolio)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraTextDark
                        )
                        Text(
                            text = "${workPhotoBitmaps.count { it != null }}/3 Saved",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (workPhotoBitmaps.count { it != null } > 0) Color(0xFF16A34A) else WorkoraOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "अपने किए हुए काम की 3 फोटो अपलोड करें ताकि ग्राहक आपका काम देखकर तुरंत काम दें:",
                        fontSize = 12.sp,
                        color = WorkoraTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (index in 0..2) {
                            val currentWorkBitmap = workPhotoBitmaps[index]
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    onClick = { openWorkPhotoGallery(index) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(105.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = WorkoraBgLight),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (currentWorkBitmap != null) Color(0xFF16A34A) else WorkoraOrange
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        if (currentWorkBitmap != null) {
                                            Image(
                                                bitmap = currentWorkBitmap,
                                                contentDescription = "Work Photo ${index + 1}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .background(Color.White, shape = CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Saved",
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddAPhoto,
                                                    contentDescription = null,
                                                    tint = WorkoraOrange,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Photo ${index + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WorkoraTextDark
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = { openWorkPhotoGallery(index) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentWorkBitmap != null) Color(0xFF16A34A) else WorkoraOrange
                                    )
                                ) {
                                    Text(
                                        text = if (currentWorkBitmap != null) "Change ${index + 1}" else "Upload ${index + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Direct Editable Information Card with Save Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Edit & Save Your Details (अपनी जानकारी यहाँ भरें)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy
                    )

                    OutlinedTextField(
                        value = currentName,
                        onValueChange = { currentName = it },
                        label = { Text("Full Name (पूरा नाम)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraOrange,
                            unfocusedBorderColor = WorkoraBorder
                        )
                    )

                    OutlinedTextField(
                        value = currentPhone,
                        onValueChange = { currentPhone = it },
                        label = { Text("Mobile Number (मोबाइल नंबर)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraOrange,
                            unfocusedBorderColor = WorkoraBorder
                        )
                    )

                    OutlinedTextField(
                        value = currentLocation,
                        onValueChange = { currentLocation = it },
                        label = { Text("Village / City (गाँव या शहर का पता)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraOrange,
                            unfocusedBorderColor = WorkoraBorder
                        )
                    )

                    OutlinedTextField(
                        value = currentSkill,
                        onValueChange = { currentSkill = it },
                        label = { Text("Your Work / Skill (आप क्या काम करते हैं?)") },
                        leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = WorkoraOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraOrange,
                            unfocusedBorderColor = WorkoraBorder
                        )
                    )

                    OutlinedTextField(
                        value = currentDailyRate,
                        onValueChange = { currentDailyRate = it },
                        label = { Text("Daily Wage ₹ (रोज़ की मजदूरी रेट)") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = WorkoraOrange) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraOrange,
                            unfocusedBorderColor = WorkoraBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { saveAllProfileInformation() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, tint = Color.White, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Information Permanently ✓",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = onSwitchRole,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Icon(imageVector = Icons.Default.SwapHoriz, tint = WorkoraNavy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Switch to ${if (role == UserRole.CUSTOMER) "Labour Mode" else "Hirer Mode"}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraNavy
                )
            }

            Button(
                onClick = {
                    authPrefs.edit().putBoolean("is_logged_in", false).apply()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, tint = Color.White, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Logout", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
