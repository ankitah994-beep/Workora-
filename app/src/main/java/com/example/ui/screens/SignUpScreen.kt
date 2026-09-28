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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SignUpNavy = Color(0xFF083D91)
private val SignUpOrange = Color(0xFFFF8C00)
private val SignUpGreen = Color(0xFF22A06B)

private fun decodeSignUpBase64Image(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun encodeSignUpUriToBase64(context: Context, uri: Uri): String {
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
fun SignUpScreen(
    onSignUp: (name: String, email: String, phone: String, password: String) -> Unit = { _, _, _, _ -> },
    onNavigateToLogin: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    var regRole by remember { mutableStateOf("CUSTOMER") } // "CUSTOMER" | "LABOUR"
    var regFullName by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regAge by remember { mutableStateOf("") }
    var regLocation by remember { mutableStateOf("") }
    var regSelectedCategory by remember { mutableStateOf("Default") }
    var regSubSkills by remember { mutableStateOf("") }
    var regExperience by remember { mutableStateOf("") }
    var regDailyWage by remember { mutableStateOf("") }
    var regTeamSize by remember { mutableStateOf("Individual (अकेले)") }
    var regPassword by remember { mutableStateOf("") }
    var regShowPassword by remember { mutableStateOf(false) }

    // Profile Photo + 3 Work Proof Photos
    var regProfilePhotoB64 by remember { mutableStateOf("") }
    var regWorkProof1B64 by remember { mutableStateOf("") }
    var regWorkProof2B64 by remember { mutableStateOf("") }
    var regWorkProof3B64 by remember { mutableStateOf("") }
    var activeUploadSlot by remember { mutableIntStateOf(0) } // 0=Profile, 1=Proof1, 2=Proof2, 3=Proof3

    var showRegOtpDialog by remember { mutableStateOf(false) }
    var regOtpTargetKey by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeSignUpUriToBase64(context, uri)
            if (encoded.isNotBlank()) {
                when (activeUploadSlot) {
                    0 -> regProfilePhotoB64 = encoded
                    1 -> regWorkProof1B64 = encoded
                    2 -> regWorkProof2B64 = encoded
                    3 -> regWorkProof3B64 = encoded
                }
                Toast.makeText(context, "Photo Added ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val workCategoriesList = listOf(
        "Default", "Mason", "Electrician", "Plumber",
        "Painter", "Carpenter", "Labour", "Cleaner",
        "Farm Worker", "Tile Worker", "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onNavigateToLogin()
                    onBack()
                }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SignUpNavy)
            }
            Text(
                text = "Create Workora Account",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SignUpNavy
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Photo Circle
            val profileBmp = remember(regProfilePhotoB64) { decodeSignUpBase64Image(regProfilePhotoB64) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(SignUpNavy)
                        .clickable {
                            activeUploadSlot = 0
                            photoPickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBmp != null) {
                        Image(
                            bitmap = profileBmp,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Profile Photo",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Profile Photo",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignUpNavy
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Account Type
                    Text(
                        text = "Account Type",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF102A43)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "CUSTOMER" to "Customer (ग्राहक)",
                            "LABOUR" to "Worker (कारीगर)"
                        ).forEach { (rKey, rLabel) ->
                            val selected = regRole == rKey
                            Button(
                                onClick = { regRole = rKey },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) SignUpNavy else Color(0xFFF1F5F9)
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = rLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selected) Color.White else Color(0xFF102A43)
                                )
                            }
                        }
                    }

                    // 2. Full Name
                    OutlinedTextField(
                        value = regFullName,
                        onValueChange = { regFullName = it },
                        label = { Text("Full Name (आपका पूरा नाम)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SignUpOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // 3. Mobile Number (1234567890) & Age
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = regMobile,
                            onValueChange = { if (it.length <= 10) regMobile = it.filter { c -> c.isDigit() } },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("1234567890") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = SignUpOrange) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1.4f),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = regAge,
                            onValueChange = { if (it.length <= 2) regAge = it.filter { c -> c.isDigit() } },
                            label = { Text("Age (उम्र)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // 4. Gmail / Email ID (example@gmail.com)
                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("Gmail / Email ID") },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SignUpOrange) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // 5. Village / City Live Search
                    LiveLocationAutoCompleteField(
                        value = regLocation,
                        onValueChange = { regLocation = it },
                        label = "Village / City (गाँव या शहर - Live Search)"
                    )

                    // 6. Select Work Category (Selection Buttons Only)
                    Text(
                        text = "Select Work Category (काम की श्रेणी चुनें)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF102A43)
                    )
                    workCategoriesList.chunked(3).forEach { rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCats.forEach { catItem ->
                                val isSelected = regSelectedCategory.equals(catItem, ignoreCase = true)
                                OutlinedButton(
                                    onClick = { regSelectedCategory = catItem },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isSelected) SignUpOrange else Color(0xFFD0D5DD)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) SignUpOrange.copy(alpha = 0.15f) else Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = catItem,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SignUpOrange else Color(0xFF102A43),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // 7. Experience & Daily Wage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = regExperience,
                            onValueChange = { regExperience = it },
                            label = { Text("Experience (अनुभव)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = regDailyWage,
                            onValueChange = { regDailyWage = it.filter { c -> c.isDigit() } },
                            label = { Text("Daily Wage (₹/day)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    // 8. Work Mode / Team Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Individual (अकेले)", "Team / ठेकेदार").forEach { modeLabel ->
                            val selected = regTeamSize == modeLabel
                            OutlinedButton(
                                onClick = { regTeamSize = modeLabel },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (selected) SignUpNavy else Color(0xFFD0D5DD)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selected) SignUpNavy.copy(alpha = 0.1f) else Color.White
                                )
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) SignUpNavy else Color(0xFF102A43)
                                )
                            }
                        }
                    }

                    // 9. Work Details
                    OutlinedTextField(
                        value = regSubSkills,
                        onValueChange = { regSubSkills = it },
                        label = { Text("Work Details (काम का विवरण)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    // 10. Upload 3 Work Proof Photos
                    Text(
                        text = "Work Proof Photos (काम के प्रमाण की 3 फोटो)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF102A43)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            1 to regWorkProof1B64,
                            2 to regWorkProof2B64,
                            3 to regWorkProof3B64
                        ).forEach { (slotNum, photoB64) ->
                            val bmp = remember(photoB64) { decodeSignUpBase64Image(photoB64) }
                            Card(
                                onClick = {
                                    activeUploadSlot = slotNum
                                    photoPickerLauncher.launch("image/*")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(92.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (photoB64.isNotBlank()) Color(0xFFDCFCE7) else Color(0xFFF8FAFC)
                                ),
                                border = BorderStroke(
                                    1.2.dp,
                                    if (photoB64.isNotBlank()) SignUpGreen else Color(0xFFD0D5DD)
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp,
                                            contentDescription = "Photo $slotNum",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddAPhoto,
                                                contentDescription = null,
                                                tint = SignUpOrange,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Photo $slotNum",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF475467)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 11. Create Password
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Create Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = SignUpOrange) },
                        trailingIcon = {
                            IconButton(onClick = { regShowPassword = !regShowPassword }) {
                                Icon(
                                    imageVector = if (regShowPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Password",
                                    tint = SignUpOrange
                                )
                            }
                        },
                        visualTransformation = if (regShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val hasLen = regPassword.length >= 8
                            val hasLet = regPassword.any { it.isLetter() }
                            val hasNum = regPassword.any { it.isDigit() }
                            Text(
                                text = "${if (hasLen) "✓" else "•"} Minimum 8 characters (कम से कम 8 अक्षर)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasLen) SignUpGreen else Color(0xFF667085)
                            )
                            Text(
                                text = "${if (hasLet) "✓" else "•"} At least 1 letter (A-Z या a-z)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasLet) SignUpGreen else Color(0xFF667085)
                            )
                            Text(
                                text = "${if (hasNum) "✓" else "•"} At least 1 number (0-9)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasNum) SignUpGreen else Color(0xFF667085)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val cleanName = regFullName.trim()
                            val cleanPhone = regMobile.filter { it.isDigit() }.takeLast(10)
                            val cleanEmail = regEmail.trim().lowercase()
                            val cleanPass = regPassword.trim()

                            if (cleanName.length < 2) {
                                Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (cleanPhone.length != 10) {
                                Toast.makeText(context, "Please enter 10-digit mobile number", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!WorkoraSecurityManager.isStrongPassword(cleanPass)) {
                                Toast.makeText(context, "Password must be 8+ characters with 1 letter & 1 number", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            regOtpTargetKey = cleanPhone
                            WorkoraRealOtpEngine.sendRealOtp(
                                context = context,
                                phoneOrEmail = cleanPhone,
                                emailOptional = cleanEmail,
                                purpose = "REGISTRATION"
                            ) {
                                showRegOtpDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SignUpOrange)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showRegOtpDialog) {
        WorkoraRealOtpVerificationDialog(
            targetKey = regOtpTargetKey,
            maskedTargetDisplay = "+91 $regOtpTargetKey",
            purposeTitle = "Verify OTP",
            onResendClick = {
                WorkoraRealOtpEngine.sendRealOtp(
                    context = context,
                    phoneOrEmail = regOtpTargetKey,
                    emailOptional = regEmail.trim(),
                    purpose = "REGISTRATION"
                ) {}
            },
            onDismiss = { showRegOtpDialog = false },
            onVerifiedSuccess = {
                showRegOtpDialog = false
                val cleanName = regFullName.trim()
                val cleanPhone = regMobile.filter { it.isDigit() }.takeLast(10)
                val finalEmail = regEmail.trim().lowercase().ifBlank { "${cleanPhone}@workora.in" }
                val cleanLoc = regLocation.trim().ifBlank { "Silwani, Raisen (MP)" }
                val cleanPass = regPassword.trim()
                val passHash = WorkoraSecurityManager.hashPasswordSecure(cleanPass)

                authPrefs.edit().apply {
                    putBoolean("is_logged_in", true)
                    putString("last_logged_in_email", finalEmail)
                    putString("saved_user_role", regRole)
                    putString("user_pass_$cleanPhone", cleanPass)
                    putString("user_hash_$cleanPhone", passHash)
                    putString("user_pass_$finalEmail", cleanPass)
                    putString("user_hash_$finalEmail", passHash)
                    apply()
                }

                profilePrefs.edit().apply {
                    putString("user_name", cleanName)
                    putString("user_phone", "+91 $cleanPhone")
                    putString("user_location", cleanLoc)
                    putString("user_skill", regSelectedCategory)
                    putString("user_rate", regDailyWage.ifBlank { "600" })
                    putString("user_experience", regExperience)
                    putString("user_age", regAge)
                    if (regProfilePhotoB64.isNotBlank()) putString("profile_photo_base64", regProfilePhotoB64)
                    if (regWorkProof1B64.isNotBlank()) putString("work_photo_1", regWorkProof1B64)
                    if (regWorkProof2B64.isNotBlank()) putString("work_photo_2", regWorkProof2B64)
                    if (regWorkProof3B64.isNotBlank()) putString("work_photo_3", regWorkProof3B64)
                    apply()
                }

                FirebaseManager.syncUserToFirebase(
                    context = context,
                    name = cleanName,
                    email = finalEmail,
                    phone = "+91 $cleanPhone",
                    password = "",
                    role = regRole
                )

                Toast.makeText(context, "Account Created ✓", Toast.LENGTH_LONG).show()
                onSignUp(cleanName, finalEmail, "+91 $cleanPhone", cleanPass)
            }
        )
    }
}
