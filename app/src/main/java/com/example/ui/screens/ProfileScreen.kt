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
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    onBack: () -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onLogout: () -> Unit = {},
    onUpdateProfile: (name: String, phone: String, location: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }

    var currentName by remember { mutableStateOf(prefs.getString("user_name", userName) ?: userName) }
    var currentPhone by remember { mutableStateOf(prefs.getString("user_phone", userPhone) ?: userPhone) }
    var currentLocation by remember { mutableStateOf(prefs.getString("user_location", userLocation) ?: userLocation) }
    var currentSkill by remember { mutableStateOf(prefs.getString("user_skill", "Mistri / Electrician / Painter") ?: "Mistri / Electrician / Painter") }
    var currentDailyRate by remember { mutableStateOf(prefs.getString("user_rate", "500") ?: "500") }

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
        Toast.makeText(context, "Aapki poori Profile & Photos Permanent Save ho gayi! ✓", Toast.LENGTH_LONG).show()
    }

    val roleTitle = if (role == UserRole.CUSTOMER) "Hirer / Customer Profile" else "Worker / Labour Profile"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar with direct Save button
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
            Button(
                onClick = { saveAllProfileInformation() },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Save Profile", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Photo Section
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

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = WorkoraOrange) },
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

                    // Big Green Permanent Save Button
                    Button(
                        onClick = { saveAllProfileInformation() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(imageVector = Icons.Default.Save, tint = Color.White, contentDescription = null)
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
                    // Clear auto-login session ONLY when user explicitly taps Logout
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
