package com.example.ui.screens

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom

private const val LOGIN_FIREBASE_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"
private val WorkoraNavy = Color(0xFF083D91)
private val WorkoraOrange = Color(0xFFFF8C00)
private val WorkoraGreen = Color(0xFF22A06B)
private val WorkoraRed = Color(0xFFB42318)
private val WorkoraBlue = Color(0xFF0061FF) // New Design Primary Color
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)

private fun decodeLoginBase64Image(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) { null }
}
private fun encodeLoginUriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else ""
    } catch (_: Exception) { "" }
}

object WorkoraRealOtpEngine {
    private const val OTP_PREFS = "workora_otp_store"
    const val OTP_TTL_MS = 5 * 60 * 1000L
    const val CHANNEL_ID = "workora_otp_channel"
    
    fun sha256(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) { input }
    }
    
    fun triggerSystemOtpNotification(
        context: Context, target: String, otpCode: String, purpose: String = "Verification"
    ) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID, "Workora Security OTP", NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "OTP verification alerts" }
                nm.createNotificationChannel(channel)
            }
            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)
            }
            builder.setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("Workora Verification Code")
                .setContentText("Your 6-digit OTP for $purpose is $otpCode. Do not share it.")
                .setAutoCancel(true)
            nm.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        } catch (_: Exception) {}
    }
    
    fun sendRealOtp(
        context: Context, phoneOrEmail: String, emailOptional: String = "", purpose: String = "LOGIN", onDispatched: (String) -> Unit = {}
    ) {
        val cleanTarget = phoneOrEmail.trim().lowercase()
        val otpCode = (100000 + SecureRandom().nextInt(900000)).toString()
        val otpHash = sha256(otpCode)
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(OTP_PREFS, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("otp_hash_$cleanTarget", otpHash)
            putLong("otp_exp_$cleanTarget", now + OTP_TTL_MS)
            putInt("otp_tries_$cleanTarget", 0)
            apply()
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val key = "otp_${System.currentTimeMillis()}"
                val conn = (URL("$LOGIN_FIREBASE_URL/otp_dispatches/$key.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    connectTimeout = 4000
                    readTimeout = 4000
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("target", cleanTarget)
                    put("purpose", purpose)
                    put("otpHash", otpHash)
                    put("timestamp", now)
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}
            withContext(Dispatchers.Main) {
                triggerSystemOtpNotification(context, cleanTarget, otpCode, purpose)
                onDispatched(cleanTarget)
            }
        }
    }
    
    fun verifyRealOtp(context: Context, targetKey: String, enteredOtp: String): Pair<Boolean, String> {
        val cleanTarget = targetKey.trim().lowercase()
        val cleanOtp = enteredOtp.trim()
        if (cleanOtp.length != 6) return Pair(false, "Please enter a valid 6-digit OTP.")
        val prefs = context.getSharedPreferences(OTP_PREFS, Context.MODE_PRIVATE)
        val savedHash = prefs.getString("otp_hash_$cleanTarget", null)
        val expTime = prefs.getLong("otp_exp_$cleanTarget", 0L)
        val tries = prefs.getInt("otp_tries_$cleanTarget", 0)
        if (savedHash == null) return Pair(false, "Please request a new OTP.")
        if (System.currentTimeMillis() > expTime) return Pair(false, "OTP expired. Please resend.")
        if (tries >= 5) return Pair(false, "Too many wrong attempts. Please resend OTP.")
        return if (sha256(cleanOtp) == savedHash) {
            prefs.edit().remove("otp_hash_$cleanTarget").apply()
            Pair(true, "Verified")
        } else {
            prefs.edit().putInt("otp_tries_$cleanTarget", tries + 1).apply()
            Pair(false, "Incorrect OTP! (${4 - tries} attempts left)")
        }
    }
}

@Composable
fun WorkoraRealOtpVerificationDialog(
    targetKey: String, maskedTargetDisplay: String, purposeTitle: String = "Verify OTP",
    onResendClick: () -> Unit, onDismiss: () -> Unit, onVerifiedSuccess: () -> Unit
) {
    val context = LocalContext.current
    var otpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color.White,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = purposeTitle, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF102A43)) }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Enter the 6-digit OTP sent to $maskedTargetDisplay", fontSize = 13.sp, color = Color(0xFF475467))
                if (errorMessage != null) { Text(text = errorMessage!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraRed) }
                OutlinedTextField(
                    value = otpInput, onValueChange = { if (it.length <= 6) otpInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Enter 6-Digit OTP") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
                Text(
                    text = "Resend OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue,
                    modifier = Modifier.clickable { errorMessage = null; otpInput = ""; onResendClick() }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val verifyResult = WorkoraRealOtpEngine.verifyRealOtp(context, targetKey, otpInput)
                    if (verifyResult.first) onVerifiedSuccess() else errorMessage = verifyResult.second
                },
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue), shape = RoundedCornerShape(10.dp)
            ) { Text("Verify", color = Color.White, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Cancel") }
        }
    )
}

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit = { _, _ -> },
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToRegister: () -> Unit = onNavigateToSignUp,
    toastMessage: String? = null,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    var isRegistrationScreen by remember { mutableStateOf(false) }
    
    // Login States
    var identifierInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isSendingOtp by remember { mutableStateOf(false) }
    var showLoginOtpDialog by remember { mutableStateOf(false) }
    var maskedOtpTarget by remember { mutableStateOf("") }
    
    // Forgot Password States
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotIdentifier by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var forgotMaskedTarget by remember { mutableStateOf("") }
    var showForgotOtpVerify by remember { mutableStateOf(false) }

    // Registration States (All untouched logic, newly styled)
    var regRole by remember { mutableStateOf("LABOUR") } // Default as per new UI
    var regFullName by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regLocation by remember { mutableStateOf("") }
    var regSelectedCategory by remember { mutableStateOf("Default") }
    var regExperience by remember { mutableStateOf("") }
    var regDailyWage by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regShowPassword by remember { mutableStateOf(false) }
    var regShowConfirmPassword by remember { mutableStateOf(false) }
    
    var regProfilePhotoB64 by remember { mutableStateOf("") }
    var regWorkProof1B64 by remember { mutableStateOf("") }
    var regWorkProof2B64 by remember { mutableStateOf("") }
    var regWorkProof3B64 by remember { mutableStateOf("") }
    var activeUploadSlot by remember { mutableIntStateOf(0) }
    var showRegOtpDialog by remember { mutableStateOf(false) }
    var regOtpTargetKey by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeLoginUriToBase64(context, uri)
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

    // =========================================================================
    // SIGN UP (CREATE ACCOUNT) SCREEN - NEW DESIGN
    // =========================================================================
    if (isRegistrationScreen) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WorkoraBlue,
                    modifier = Modifier.size(28.dp).clickable { isRegistrationScreen = false }
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Join Workora today", fontSize = 15.sp, color = TextGray)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Role Toggle (Worker / Client)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(26.dp))
                        .padding(4.dp)
                ) {
                    val isWorker = regRole == "LABOUR"
                    Button(
                        onClick = { regRole = "LABOUR" },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isWorker) WorkoraBlue else Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        elevation = null
                    ) { Text("Worker", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isWorker) Color.White else TextGray) }
                    
                    Button(
                        onClick = { regRole = "CUSTOMER" },
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (!isWorker) WorkoraBlue else Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        elevation = null
                    ) { Text("Client", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (!isWorker) Color.White else TextGray) }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Profile Photo Upload (Centered)
                val profileBmp = remember(regProfilePhotoB64) { decodeLoginBase64Image(regProfilePhotoB64) }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { activeUploadSlot = 0; photoPickerLauncher.launch("image/*") }) {
                    Box(
                        modifier = Modifier.size(80.dp).background(Color(0xFFE2E8F0), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBmp != null) {
                            Image(bitmap = profileBmp, contentDescription = "Profile Photo", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = TextGray, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add Profile Photo", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue)
                    Text("(Recommended)", fontSize = 11.sp, color = TextGray)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Common TextField Colors
                val tfColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WorkoraBlue, unfocusedBorderColor = BorderGray,
                    focusedLeadingIconColor = WorkoraBlue, unfocusedLeadingIconColor = TextGray
                )

                // Form Fields
                OutlinedTextField(
                    value = regFullName, onValueChange = { regFullName = it },
                    label = { Text("Full Name") }, leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                )
                
                OutlinedTextField(
                    value = regMobile, onValueChange = { if (it.length <= 10) regMobile = it.filter { c -> c.isDigit() } },
                    label = { Text("Phone Number") }, placeholder = { Text("Enter mobile number") }, leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                )
                
                OutlinedTextField(
                    value = regEmail, onValueChange = { regEmail = it },
                    label = { Text("Email (optional)") }, placeholder = { Text("example@gmail.com") }, leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                )

                // Additional Profile Details (Preserved logic, clean UI)
                OutlinedTextField(
                    value = regLocation, onValueChange = { regLocation = it },
                    label = { Text("Location / City") }, leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                )
                
                Text("Work Category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                workCategoriesList.chunked(3).forEach { rowCats ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowCats.forEach { catItem ->
                            val isSelected = regSelectedCategory.equals(catItem, ignoreCase = true)
                            OutlinedButton(
                                onClick = { regSelectedCategory = catItem }, modifier = Modifier.weight(1f).height(40.dp), shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isSelected) WorkoraBlue else BorderGray),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) WorkoraBlue.copy(alpha = 0.1f) else Color.White),
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) { Text(catItem, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) WorkoraBlue else TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = regExperience, onValueChange = { regExperience = it }, label = { Text("Experience") }, singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = tfColors)
                    OutlinedTextField(value = regDailyWage, onValueChange = { regDailyWage = it.filter { c -> c.isDigit() } }, label = { Text("Daily Wage (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = tfColors)
                }

                // Photos Section
                Text("Upload Work Proof (3 Photos)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(1 to regWorkProof1B64, 2 to regWorkProof2B64, 3 to regWorkProof3B64).forEach { (slotNum, photoB64) ->
                        val bmp = remember(photoB64) { decodeLoginBase64Image(photoB64) }
                        Box(
                            modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(12.dp)).background(if (photoB64.isNotBlank()) Color.Transparent else Color(0xFFF8FAFC)).clickable { activeUploadSlot = slotNum; photoPickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (bmp != null) {
                                Image(bitmap = bmp, contentDescription = "Proof $slotNum", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                                    Text("Add", fontSize = 11.sp, color = TextGray)
                                }
                            }
                        }
                    }
                }

                // Password & Confirm Password
                OutlinedTextField(
                    value = regPassword, onValueChange = { regPassword = it },
                    label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = { IconButton(onClick = { regShowPassword = !regShowPassword }) { Icon(imageVector = if (regShowPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle Password", tint = TextGray) } },
                    visualTransformation = if (regShowPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                )
                
                OutlinedTextField(
                    value = regConfirmPassword, onValueChange = { regConfirmPassword = it },
                    label = { Text("Confirm Password") }, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = { IconButton(onClick = { regShowConfirmPassword = !regShowConfirmPassword }) { Icon(imageVector = if (regShowConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle Confirm", tint = TextGray) } },
                    visualTransformation = if (regShowConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sign Up Button
                Button(
                    onClick = {
                        val cleanName = regFullName.trim()
                        val cleanPhone = regMobile.filter { it.isDigit() }.takeLast(10)
                        val cleanEmail = regEmail.trim().lowercase()
                        val cleanPass = regPassword.trim()
                        val cleanConfirm = regConfirmPassword.trim()
                        
                        if (cleanName.length < 2) { Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show(); return@Button }
                        if (cleanPhone.length != 10) { Toast.makeText(context, "Please enter 10-digit mobile number", Toast.LENGTH_SHORT).show(); return@Button }
                        if (!WorkoraSecurityManager.isStrongPassword(cleanPass)) { Toast.makeText(context, "Password must be 8+ characters with 1 letter & 1 number", Toast.LENGTH_SHORT).show(); return@Button }
                        if (cleanPass != cleanConfirm) { Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show(); return@Button }
                        
                        regOtpTargetKey = cleanPhone
                        WorkoraRealOtpEngine.sendRealOtp(context = context, phoneOrEmail = cleanPhone, emailOptional = cleanEmail, purpose = "REGISTRATION") { showRegOtpDialog = true }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
                ) {
                    Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Text
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Already have an account? ", fontSize = 14.sp, color = TextGray)
                    Text(
                        text = "Login",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraBlue,
                        modifier = Modifier.clickable { isRegistrationScreen = false }
                    )
                }
            }
        }
        
        // OTP Dialog for Registration
        if (showRegOtpDialog) {
            WorkoraRealOtpVerificationDialog(
                targetKey = regOtpTargetKey, maskedTargetDisplay = "+91 $regOtpTargetKey", purposeTitle = "Verify OTP",
                onResendClick = { WorkoraRealOtpEngine.sendRealOtp(context = context, phoneOrEmail = regOtpTargetKey, emailOptional = regEmail.trim(), purpose = "REGISTRATION") {} },
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
                        if (regProfilePhotoB64.isNotBlank()) putString("profile_photo_base64", regProfilePhotoB64)
                        if (regWorkProof1B64.isNotBlank()) putString("work_photo_1", regWorkProof1B64)
                        if (regWorkProof2B64.isNotBlank()) putString("work_photo_2", regWorkProof2B64)
                        if (regWorkProof3B64.isNotBlank()) putString("work_photo_3", regWorkProof3B64)
                        apply()
                    }
                    Toast.makeText(context, "Account Created ✓", Toast.LENGTH_LONG).show()
                    onLogin(finalEmail, cleanPass)
                }
            )
        }
        return
    }

    // =========================================================================
    // LOGIN SCREEN - NEW DESIGN
    // =========================================================================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Back Arrow
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = WorkoraBlue,
            modifier = Modifier.size(28.dp).clickable { onBack() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Titles
        Text(text = "Login", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Welcome back! Please login\nto your Workora account.", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextGray, lineHeight = 22.sp)

        Spacer(modifier = Modifier.height(32.dp))

        // Unified Phone/Email Input
        OutlinedTextField(
            value = identifierInput, onValueChange = { identifierInput = it },
            label = { Text("Phone number or Email") }, leadingIcon = { Icon(Icons.Default.PhoneIphone, contentDescription = null, tint = TextGray) },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraBlue, unfocusedBorderColor = BorderGray)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = passwordInput, onValueChange = { passwordInput = it },
            label = { Text("Password") }, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextGray) },
            trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle Password", tint = TextGray) } },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WorkoraBlue, unfocusedBorderColor = BorderGray)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Forgot Password
        Text(
            text = "Forgot password?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue, textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth().clickable { showForgotDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                val rawId = identifierInput.trim()
                val isEmail = rawId.contains("@")
                val cleanDigits = rawId.filter { it.isDigit() }.takeLast(10)
                val lookupKey = if (isEmail) rawId.lowercase() else cleanDigits

                if (isEmail && rawId.length < 5) { Toast.makeText(context, "Please enter valid email", Toast.LENGTH_SHORT).show(); return@Button }
                if (!isEmail && cleanDigits.length != 10) { Toast.makeText(context, "Please enter 10-digit mobile number", Toast.LENGTH_SHORT).show(); return@Button }
                
                val (allowed, lockMsg) = WorkoraSecurityManager.checkLoginBruteForceAllowed(context, lookupKey)
                if (!allowed) { Toast.makeText(context, lockMsg, Toast.LENGTH_LONG).show(); return@Button }

                val cleanPass = passwordInput.trim()
                if (cleanPass.length < 6) { Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show(); return@Button }
                
                val storedPass = authPrefs.getString("user_pass_$lookupKey", null)
                val storedHash = authPrefs.getString("user_hash_$lookupKey", null)
                
                if (storedHash == null && storedPass == null) { Toast.makeText(context, "Account not found! Please Sign Up first.", Toast.LENGTH_LONG).show(); return@Button }
                
                val passMatches = when {
                    storedHash != null -> WorkoraSecurityManager.verifyPasswordSecure(cleanPass, storedHash)
                    storedPass != null -> storedPass == cleanPass
                    else -> false
                }
                
                if (!passMatches) {
                    WorkoraSecurityManager.recordLoginAttempt(context, lookupKey, isSuccess = false)
                    Toast.makeText(context, "Galat Password! Sahi password dalein.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                WorkoraSecurityManager.recordLoginAttempt(context, lookupKey, isSuccess = true)
                val finalEmail = if (isEmail) lookupKey else "${cleanDigits}@workora.in"
                authPrefs.edit().apply {
                    putBoolean("is_logged_in", true)
                    putString("last_logged_in_email", finalEmail)
                    apply()
                }
                if (!isEmail && cleanDigits.length == 10) { profilePrefs.edit().putString("user_phone", "+91 $cleanDigits").apply() }
                Toast.makeText(context, "Welcome to Workora! ✓", Toast.LENGTH_SHORT).show()
                onLogin(finalEmail, cleanPass)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
        ) { Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }

        Spacer(modifier = Modifier.height(24.dp))

        // OR Divider
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Divider(modifier = Modifier.weight(1f), color = BorderGray)
            Text("or", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp))
            Divider(modifier = Modifier.weight(1f), color = BorderGray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Continue with Google Button
        OutlinedButton(
            onClick = { Toast.makeText(context, "Google Login Setup Pending", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BorderGray),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("G", fontWeight = FontWeight.ExtraBold, color = Color.Red, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sign Up Link at bottom
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Don't have an account? ", fontSize = 14.sp, color = TextGray)
            Text(text = "Sign Up", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraBlue, modifier = Modifier.clickable { isRegistrationScreen = true })
        }
    }

    // Forgot Password Dialogs (Untouched Logic)
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false }, containerColor = Color.White,
            title = { Text("Reset Password", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = forgotIdentifier, onValueChange = { forgotIdentifier = it }, label = { Text("Mobile Number or Gmail") }, placeholder = { Text("1234567890 / example@gmail.com") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = forgotNewPassword, onValueChange = { forgotNewPassword = it }, label = { Text("New Password") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanId = forgotIdentifier.trim()
                        val newPass = forgotNewPassword.trim()
                        if (cleanId.length < 5 || !WorkoraSecurityManager.isStrongPassword(newPass)) { Toast.makeText(context, "Enter valid ID & 8-char password!", Toast.LENGTH_SHORT).show(); return@Button }
                        WorkoraRealOtpEngine.sendRealOtp(context = context, phoneOrEmail = cleanId, emailOptional = if (cleanId.contains("@")) cleanId else "", purpose = "PASSWORD RESET") { masked -> forgotMaskedTarget = masked; showForgotDialog = false; showForgotOtpVerify = true }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) { Text("Send OTP", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { OutlinedButton(onClick = { showForgotDialog = false }) { Text("Cancel") } }
        )
    }

    if (showForgotOtpVerify) {
        val cleanId = forgotIdentifier.trim()
        val key = if (cleanId.contains("@")) cleanId.lowercase() else cleanId.filter { it.isDigit() }.takeLast(10)
        WorkoraRealOtpVerificationDialog(
            targetKey = key, maskedTargetDisplay = forgotMaskedTarget, purposeTitle = "Verify OTP",
            onResendClick = { WorkoraRealOtpEngine.sendRealOtp(context = context, phoneOrEmail = key, emailOptional = if (cleanId.contains("@")) key else "", purpose = "PASSWORD RESET") { masked -> forgotMaskedTarget = masked } },
            onDismiss = { showForgotOtpVerify = false },
            onVerifiedSuccess = {
                showForgotOtpVerify = false
                val newHash = WorkoraSecurityManager.hashPasswordSecure(forgotNewPassword.trim())
                authPrefs.edit().apply { putString("user_pass_$key", forgotNewPassword.trim()); putString("user_hash_$key", newHash); apply() }
                passwordInput = forgotNewPassword.trim()
                Toast.makeText(context, "Password Reset ✓", Toast.LENGTH_LONG).show()
            }
        )
    }
}
