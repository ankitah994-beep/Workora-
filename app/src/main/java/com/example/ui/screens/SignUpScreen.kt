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
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SignNavy = Color(0xFF083D91)
private val SignOrange = Color(0xFFFF8C00)
private val SignGreen = Color(0xFF22A06B)

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
    onNavigateToLogin: () -> Unit = {},
    onSignUpSuccess: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    // States
    var regRole by remember { mutableStateOf("CUSTOMER") }
    var regFullName by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regLocation by remember { mutableStateOf("") }
    var regSelectedCategory by remember { mutableStateOf("Default") }
    var regExperience by remember { mutableStateOf("") }
    var regDailyWage by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regShowPassword by remember { mutableStateOf(false) }

    // Photos
    var regProfilePhotoB64 by remember { mutableStateOf("") }
    var regWorkProof1B64 by remember { mutableStateOf("") }
    var regWorkProof2B64 by remember { mutableStateOf("") }
    var regWorkProof3B64 by remember { mutableStateOf("") }
    var activeUploadSlot by remember { mutableIntStateOf(0) }

    var showRegOtpDialog by remember { mutableStateOf(false) }
    var regOtpTargetKey by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeSignUpUriToBase64(context, uri)
            if (encoded.isNotBlank()) {
                when (activeUploadSlot) {
                    0 -> regProfilePhotoB64 = encoded
                    1 -> regWorkProof1B64 = encoded
                    2 -> regWorkProof2B64 = encoded
                    3 -> regWorkProof3B64 = encoded
                }
                Toast.makeText(context, "Photo Selected ✓", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val workCategoriesList = listOf("Default", "Mason", "Electrician", "Plumber", "Painter", "Carpenter", "Labour", "Cleaner", "Farm Worker", "Tile Worker", "Other")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateToLogin) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SignNavy) }
            Text("Create Workora Account", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = SignNavy)
        }

        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Profile Photo
            val profileBmp = remember(regProfilePhotoB64) { decodeSignUpBase64Image(regProfilePhotoB64) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(SignNavy).clickable { activeUploadSlot = 0; photoPickerLauncher.launch("image/*") }, contentAlignment = Alignment.Center) {
                    if (profileBmp != null) Image(bitmap = profileBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Profile Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SignNavy)
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    
                    // Role
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("CUSTOMER" to "Customer (ग्राहक)", "LABOUR" to "Worker (कारीगर)").forEach { (rKey, rLabel) ->
                            val selected = regRole == rKey
                            Button(onClick = { regRole = rKey }, modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = if (selected) SignNavy else Color(0xFFF1F5F9)), contentPadding = PaddingValues(4.dp)) {
                                Text(rLabel, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (selected) Color.White else Color(0xFF102A43), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    OutlinedTextField(value = regFullName, onValueChange = { regFullName = it }, label = { Text("Full Name (आपका पूरा नाम)") }, leadingIcon = { Icon(Icons.Default.Person, null, tint = SignOrange) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(value = regMobile, onValueChange = { if (it.length <= 10) regMobile = it.filter { c -> c.isDigit() } }, label = { Text("Mobile Number") }, placeholder = { Text("1234567890") }, leadingIcon = { Icon(Icons.Default.Phone, null, tint = SignOrange) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(value = regEmail, onValueChange = { regEmail = it }, label = { Text("Gmail / Email ID") }, placeholder = { Text("example@gmail.com") }, leadingIcon = { Icon(Icons.Default.Email, null, tint = SignOrange) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))

                    // FIX: Replaced custom LiveLocationAutoCompleteField with standard OutlinedTextField
                    OutlinedTextField(
                        value = regLocation,
                        onValueChange = { regLocation = it },
                        label = { Text("Village / City (गाँव या शहर)") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = SignOrange) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Text("Work Category (काम की श्रेणी चुनें)", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF102A43))
                    workCategoriesList.chunked(3).forEach { rowCats ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowCats.forEach { catItem ->
                                val isSelected = regSelectedCategory.equals(catItem, ignoreCase = true)
                                OutlinedButton(onClick = { regSelectedCategory = catItem }, modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, if (isSelected) SignOrange else Color(0xFFD0D5DD)), colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) SignOrange.copy(alpha = 0.15f) else Color.White), contentPadding = PaddingValues(4.dp)) {
                                    Text(catItem, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) SignOrange else Color(0xFF102A43), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = regExperience, onValueChange = { regExperience = it }, label = { Text("Experience") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp))
                        OutlinedTextField(value = regDailyWage, onValueChange = { regDailyWage = it.filter { c -> c.isDigit() } }, label = { Text("Daily Wage (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp))
                    }

                    Text("Work Proof Photos (काम के प्रमाण की 3 फोटो)", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF102A43))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(1 to regWorkProof1B64, 2 to regWorkProof2B64, 3 to regWorkProof3B64).forEach { (slotNum, photoB64) ->
                            val bmp = remember(photoB64) { decodeSignUpBase64Image(photoB64) }
                            Card(onClick = { activeUploadSlot = slotNum; photoPickerLauncher.launch("image/*") }, modifier = Modifier.weight(1f).height(92.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (photoB64.isNotBlank()) Color(0xFFDCFCE7) else Color(0xFFF8FAFC)), border = BorderStroke(1.2.dp, if (photoB64.isNotBlank()) SignGreen else Color(0xFFD0D5DD))) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (bmp != null) Image(bitmap = bmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    else Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(Icons.Default.AddAPhoto, null, tint = SignOrange, modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Photo $slotNum", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475467), maxLines = 1)
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(value = regPassword, onValueChange = { regPassword = it }, label = { Text("Create Password") }, leadingIcon = { Icon(Icons.Default.Lock, null, tint = SignOrange) }, trailingIcon = { IconButton(onClick = { regShowPassword = !regShowPassword }) { Icon(if (regShowPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = SignOrange) } }, visualTransformation = if (regShowPassword) VisualTransformation.None else PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)), border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val hasLen = regPassword.length >= 8
                            val hasLet = regPassword.any { it.isLetter() }
                            val hasNum = regPassword.any { it.isDigit() }
                            Text("${if (hasLen) "✓" else "•"} Minimum 8 characters", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (hasLen) SignGreen else Color(0xFF667085))
                            Text("${if (hasLet) "✓" else "•"} At least 1 letter", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (hasLet) SignGreen else Color(0xFF667085))
                            Text("${if (hasNum) "✓" else "•"} At least 1 number", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (hasNum) SignGreen else Color(0xFF667085))
                        }
                    }

                    Button(onClick = {
                        val cleanName = regFullName.trim()
                        val cleanPhone = regMobile.filter { it.isDigit() }.takeLast(10)
                        val cleanPass = regPassword.trim()
                        if (cleanName.length < 2 || cleanPhone.length != 10 || !WorkoraSecurityManager.isStrongPassword(cleanPass)) {
                            Toast.makeText(context, "Please enter valid details & 8-char password", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        regOtpTargetKey = cleanPhone
                        WorkoraRealOtpEngine.sendRealOtp(context, cleanPhone, regEmail.trim(), "REGISTRATION") { showRegOtpDialog = true }
                    }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = SignOrange)) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
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
            onResendClick = { WorkoraRealOtpEngine.sendRealOtp(context, regOtpTargetKey, regEmail.trim(), "REGISTRATION") {} },
            onDismiss = { showRegOtpDialog = false },
            onVerifiedSuccess = {
                showRegOtpDialog = false
                val cleanPhone = regMobile.filter { it.isDigit() }.takeLast(10)
                val finalEmail = regEmail.trim().lowercase().ifBlank { "${cleanPhone}@workora.in" }
                val cleanPass = regPassword.trim()
                
                authPrefs.edit().apply {
                    putBoolean("is_logged_in", true)
                    putString("last_logged_in_email", finalEmail)
                    putString("saved_user_role", regRole)
                    putString("user_pass_$cleanPhone", cleanPass)
                    putString("user_pass_$finalEmail", cleanPass)
                    apply()
                }
                profilePrefs.edit().apply {
                    putString("user_name", regFullName.trim())
                    putString("user_phone", "+91 $cleanPhone")
                    putString("user_location", regLocation.trim())
                    putString("user_skill", regSelectedCategory)
                    putString("user_rate", regDailyWage)
                    putString("user_experience", regExperience)
                    apply()
                }
                Toast.makeText(context, "Account Created ✓", Toast.LENGTH_LONG).show()
                onSignUpSuccess(finalEmail, cleanPass)
            }
        )
    }
}
