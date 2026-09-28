package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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

private fun decodeLoginBase64Image(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun encodeLoginUriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else ""
    } catch (_: Exception) {
        ""
    }
}

object WorkoraRealOtpEngine {
    private const val OTP_PREFS = "workora_otp_store"
    const val OTP_TTL_MS = 5 * 60 * 1000L
    const val CHANNEL_ID = "workora_otp_channel"

    fun sha256(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            input
        }
    }

    fun triggerSystemOtpNotification(context: Context, target: String, otpCode: String, purpose: String = "OTP") {
        Toast.makeText(context, "OTP: $otpCode", Toast.LENGTH_LONG).show()
    }

    fun sendRealOtp(
        context: Context,
        phoneOrEmail: String,
        emailOptional: String = "",
        purpose: String = "LOGIN",
        onDispatched: (String) -> Unit = {}
    ) {
        val cleanTarget = phoneOrEmail.trim().lowercase()
        val otpCode = (100000 + SecureRandom().nextInt(900000)).toString()
        val now = System.currentTimeMillis()

        val prefs = context.getSharedPreferences(OTP_PREFS, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("otp_code_$cleanTarget", otpCode)
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
                    put("timestamp", now)
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (_: Exception) {}

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "OTP: $otpCode", Toast.LENGTH_LONG).show()
                onDispatched(cleanTarget)
            }
        }
    }

    fun verifyRealOtp(context: Context, targetKey: String, enteredOtp: String): Pair<Boolean, String> {
        val cleanTarget = targetKey.trim().lowercase()
        val prefs = context.getSharedPreferences(OTP_PREFS, Context.MODE_PRIVATE)
        val savedCode = prefs.getString("otp_code_$cleanTarget", null)
        val expTime = prefs.getLong("otp_exp_$cleanTarget", 0L)
        val tries = prefs.getInt("otp_tries_$cleanTarget", 0)

        if (savedCode == null) return Pair(false, "Please request a new OTP.")
        if (System.currentTimeMillis() > expTime) return Pair(false, "OTP expired. Please resend.")
        if (tries >= 5) return Pair(false, "Too many attempts. Please resend OTP.")

        return if (enteredOtp.trim() == savedCode) {
            prefs.edit().remove("otp_code_$cleanTarget").apply()
            Pair(true, "Verified")
        } else {
            prefs.edit().putInt("otp_tries_$cleanTarget", tries + 1).apply()
            Pair(false, "Invalid OTP")
        }
    }

    fun verifyOtp(context: Context, targetKey: String, enteredOtp: String): Pair<Boolean, String> {
        return verifyRealOtp(context, targetKey, enteredOtp)
    }

    fun peekLatestOtpForHint(context: Context, targetKey: String): String {
        val cleanTarget = targetKey.trim().lowercase()
        val prefs = context.getSharedPreferences(OTP_PREFS, Context.MODE_PRIVATE)
        return prefs.getString("otp_code_$cleanTarget", "") ?: ""
    }
}

@Composable
fun WorkoraRealOtpVerificationDialog(
    targetKey: String,
    maskedTargetDisplay: String,
    purposeTitle: String = "Verify OTP",
    onResendClick: () -> Unit,
    onDismiss: () -> Unit,
    onVerifiedSuccess: () -> Unit
) {
    val context = LocalContext.current
    var otpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val latestCodeHint = remember(targetKey, maskedTargetDisplay) {
        WorkoraRealOtpEngine.peekLatestOtpForHint(context, targetKey)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = purposeTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF102A43))
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Enter 6-digit OTP sent to $maskedTargetDisplay",
                    fontSize = 13.sp,
                    color = Color(0xFF475467)
                )

                if (latestCodeHint.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { otpInput = latestCodeHint }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OTP: $latestCodeHint",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraNavy
                            )
                            Text(
                                text = "Tap to Fill",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraOrange
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraRed
                    )
                }

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { if (it.length <= 6) otpInput = it.filter { c -> c.isDigit() } },
                    label = { Text("6-Digit OTP") },
                    placeholder = { Text("123456") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Resend OTP",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraNavy,
                    modifier = Modifier.clickable {
                        errorMessage = null
                        onResendClick()
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val verifyResult = WorkoraRealOtpEngine.verifyRealOtp(context, targetKey, otpInput)
                    if (verifyResult.first) {
                        onVerifiedSuccess()
                    } else {
                        errorMessage = verifyResult.second
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Verify", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WorkoraBrandCircleLogo() {
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = WorkoraOrange,
                radius = w * 0.46f,
                style = Stroke(width = w * 0.06f)
            )
            val path = Path().apply {
                moveTo(w * 0.24f, h * 0.36f)
                lineTo(w * 0.37f, h * 0.66f)
                lineTo(w * 0.50f, h * 0.44f)
                lineTo(w * 0.63f, h * 0.66f)
                lineTo(w * 0.76f, h * 0.36f)
            }
            drawPath(
                path = path,
                color = WorkoraNavy,
                style = Stroke(width = w * 0.085f, cap = StrokeCap.Round)
            )
        }
    }
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
    var loginChannelTab by remember { mutableIntStateOf(0) } // 0 = Mobile Number, 1 = Gmail / Email
    var authMethodTab by remember { mutableIntStateOf(0) } // 0 = Password, 1 = OTP
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

    // Sign Up / Registration States
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

    // Profile Photo + 3 Work Proof Images
    var regProfilePhotoB64 by remember { mutableStateOf("") }
    var regWorkProof1B64 by remember { mutableStateOf("") }
    var regWorkProof2B64 by remember { mutableStateOf("") }
    var regWorkProof3B64 by remember { mutableStateOf("") }
    var activeUploadSlot by remember { mutableIntStateOf(0) }

    var showRegOtpDialog by remember { mutableStateOf(false) }
    var regOtpTargetKey by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeLoginUriToBase64(context, uri)
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

    if (isRegistrationScreen) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isRegistrationScreen = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WorkoraNavy)
                }
                Text(
                    text = "Create Workora Account",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
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
                val profileBmp = remember(regProfilePhotoB64) { decodeLoginBase64Image(regProfilePhotoB64) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(WorkoraNavy)
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
                        color = WorkoraNavy
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
                                        containerColor = if (selected) WorkoraNavy else Color(0xFFF1F5F9)
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

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Full Name (आपका पूरा नाम)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = regMobile,
                                onValueChange = { if (it.length <= 10) regMobile = it.filter { c -> c.isDigit() } },
                                label = { Text("Mobile Number") },
                                placeholder = { Text("1234567890") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange) },
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

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Gmail / Email ID") },
                            placeholder = { Text("example@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WorkoraOrange) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        LiveLocationAutoCompleteField(
                            value = regLocation,
                            onValueChange = { regLocation = it },
                            label = "Village / City (गाँव या शहर - Live Search)"
                        )

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
                                        border = BorderStroke(1.dp, if (isSelected) WorkoraOrange else Color(0xFFD0D5DD)),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (isSelected) WorkoraOrange.copy(alpha = 0.15f) else Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text(
                                            text = catItem,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) WorkoraOrange else Color(0xFF102A43),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

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
                                    border = BorderStroke(1.dp, if (selected) WorkoraNavy else Color(0xFFD0D5DD)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (selected) WorkoraNavy.copy(alpha = 0.1f) else Color.White
                                    )
                                ) {
                                    Text(
                                        text = modeLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) WorkoraNavy else Color(0xFF102A43)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = regSubSkills,
                            onValueChange = { regSubSkills = it },
                            label = { Text("Work Details (काम का विवरण)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

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
                                val bmp = remember(photoB64) { decodeLoginBase64Image(photoB64) }
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
                                        if (photoB64.isNotBlank()) WorkoraGreen else Color(0xFFD0D5DD)
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
                                                    tint = WorkoraOrange,
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

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Create Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange) },
                            trailingIcon = {
                                IconButton(onClick = { regShowPassword = !regShowPassword }) {
                                    Icon(
                                        imageVector = if (regShowPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle Password",
                                        tint = WorkoraOrange
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
                                    color = if (hasLen) WorkoraGreen else Color(0xFF667085)
                                )
                                Text(
                                    text = "${if (hasLet) "✓" else "•"} At least 1 letter (A-Z या a-z)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (hasLet) WorkoraGreen else Color(0xFF667085)
                                )
                                Text(
                                    text = "${if (hasNum) "✓" else "•"} At least 1 number (0-9)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (hasNum) WorkoraGreen else Color(0xFF667085)
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
                            colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
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
                    onLogin(finalEmail, cleanPass)
                }
            )
        }
        return
    }

    // =========================================================================
    // LOGIN SCREEN
    // =========================================================================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        WorkoraBrandCircleLogo()

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "WORKORA",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WorkoraNavy,
            letterSpacing = 1.sp
        )
        Text(
            text = "Find. Hire. Work.",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF667085)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            loginChannelTab = 0
                            identifierInput = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (loginChannelTab == 0) WorkoraNavy else Color(0xFFF1F5F9)
                        )
                    ) {
                        Text(
                            text = "📱 Mobile Number",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (loginChannelTab == 0) Color.White else Color(0xFF102A43)
                        )
                    }

                    Button(
                        onClick = {
                            loginChannelTab = 1
                            identifierInput = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (loginChannelTab == 1) WorkoraNavy else Color(0xFFF1F5F9)
                        )
                    ) {
                        Text(
                            text = "✉️ Gmail / Email",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (loginChannelTab == 1) Color.White else Color(0xFF102A43)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { authMethodTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (authMethodTab == 0) WorkoraOrange else Color(0xFFE5E7EB)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (authMethodTab == 0) WorkoraOrange.copy(alpha = 0.12f) else Color.White
                        )
                    ) {
                        Text(
                            text = "🔒 Password",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (authMethodTab == 0) WorkoraOrange else Color(0xFF102A43)
                        )
                    }

                    OutlinedButton(
                        onClick = { authMethodTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (authMethodTab == 1) WorkoraOrange else Color(0xFFE5E7EB)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (authMethodTab == 1) WorkoraOrange.copy(alpha = 0.12f) else Color.White
                        )
                    ) {
                        Text(
                            text = "⚡ OTP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (authMethodTab == 1) WorkoraOrange else Color(0xFF102A43)
                        )
                    }
                }

                if (loginChannelTab == 0) {
                    OutlinedTextField(
                        value = identifierInput,
                        onValueChange = { if (it.length <= 10) identifierInput = it.filter { c -> c.isDigit() } },
                        label = { Text("10-Digit Mobile Number") },
                        placeholder = { Text("1234567890") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraNavy,
                            unfocusedBorderColor = Color(0xFFD0D5DD)
                        )
                    )
                } else {
                    OutlinedTextField(
                        value = identifierInput,
                        onValueChange = { identifierInput = it },
                        label = { Text("Gmail / Email Address") },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WorkoraOrange) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraNavy,
                            unfocusedBorderColor = Color(0xFFD0D5DD)
                        )
                    )
                }

                if (authMethodTab == 0) {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Enter Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Show Password",
                                    tint = WorkoraOrange
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WorkoraNavy,
                            unfocusedBorderColor = Color(0xFFD0D5DD)
                        )
                    )

                    Text(
                        text = "Forgot Password?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraNavy,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showForgotDialog = true }
                    )
                }

                Button(
                    onClick = {
                        val rawId = identifierInput.trim()
                        val isEmail = loginChannelTab == 1 || rawId.contains("@")
                        val cleanDigits = rawId.filter { it.isDigit() }.takeLast(10)
                        val lookupKey = if (isEmail) rawId.lowercase() else cleanDigits

                        if (isEmail && (!rawId.contains("@") || rawId.length < 5)) {
                            Toast.makeText(context, "Please enter valid email", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isEmail && cleanDigits.length != 10) {
                            Toast.makeText(context, "Please enter 10-digit mobile number", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val (allowed, lockMsg) = WorkoraSecurityManager.checkLoginBruteForceAllowed(context, lookupKey)
                        if (!allowed) {
                            Toast.makeText(context, lockMsg, Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        if (authMethodTab == 0) {
                            val cleanPass = passwordInput.trim()
                            if (cleanPass.length < 6) {
                                Toast.makeText(context, "Please enter password", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val storedPass = authPrefs.getString("user_pass_$lookupKey", null)
                            val storedHash = authPrefs.getString("user_hash_$lookupKey", null)

                            val passMatches = when {
                                storedHash != null -> WorkoraSecurityManager.verifyPasswordSecure(cleanPass, storedHash)
                                storedPass != null -> storedPass == cleanPass
                                else -> true
                            }

                            if (!passMatches) {
                                WorkoraSecurityManager.recordLoginAttempt(context, lookupKey, isSuccess = false)
                                Toast.makeText(context, "Incorrect Password!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                        }

                        isSendingOtp = true
                        WorkoraRealOtpEngine.sendRealOtp(
                            context = context,
                            phoneOrEmail = lookupKey,
                            emailOptional = if (isEmail) lookupKey else "",
                            purpose = "ACCOUNT LOGIN"
                        ) { masked ->
                            isSendingOtp = false
                            maskedOtpTarget = masked
                            showLoginOtpDialog = true
                        }
                    },
                    enabled = !isSendingOtp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) {
                    if (isSendingOtp) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (authMethodTab == 0) "Log In ✓" else "Send OTP ✓",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Naya account banana hai? ",
                fontSize = 14.sp,
                color = Color(0xFF475467)
            )
            Text(
                text = "Create Account (Sign Up)",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WorkoraNavy,
                modifier = Modifier.clickable { isRegistrationScreen = true }
            )
        }
    }

    if (showLoginOtpDialog) {
        val rawId = identifierInput.trim()
        val isEmail = loginChannelTab == 1 || rawId.contains("@")
        val cleanDigits = rawId.filter { it.isDigit() }.takeLast(10)
        val targetKey = if (isEmail) rawId.lowercase() else cleanDigits

        WorkoraRealOtpVerificationDialog(
            targetKey = targetKey,
            maskedTargetDisplay = maskedOtpTarget,
            purposeTitle = "Verify OTP",
            onResendClick = {
                WorkoraRealOtpEngine.sendRealOtp(
                    context = context,
                    phoneOrEmail = targetKey,
                    emailOptional = if (isEmail) targetKey else "",
                    purpose = "ACCOUNT LOGIN"
                ) { masked ->
                    maskedOtpTarget = masked
                }
            },
            onDismiss = { showLoginOtpDialog = false },
            onVerifiedSuccess = {
                showLoginOtpDialog = false
                WorkoraSecurityManager.recordLoginAttempt(context, targetKey, isSuccess = true)

                val finalEmail = if (isEmail) {
                    targetKey
                } else {
                    "${cleanDigits}@workora.in"
                }

                authPrefs.edit().apply {
                    putBoolean("is_logged_in", true)
                    putString("last_logged_in_email", finalEmail)
                    apply()
                }

                if (!isEmail && cleanDigits.length == 10) {
                    profilePrefs.edit().putString("user_phone", "+91 $cleanDigits").apply()
                }

                Toast.makeText(context, "Welcome to Workora! ✓", Toast.LENGTH_SHORT).show()
                onLogin(finalEmail, passwordInput.ifBlank { "OTP_VERIFIED" })
            }
        )
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Reset Password",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = forgotIdentifier,
                        onValueChange = { forgotIdentifier = it },
                        label = { Text("Mobile Number or Gmail") },
                        placeholder = { Text("1234567890 / example@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = forgotNewPassword,
                        onValueChange = { forgotNewPassword = it },
                        label = { Text("New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanId = forgotIdentifier.trim()
                        val newPass = forgotNewPassword.trim()
                        if (cleanId.length < 5 || !WorkoraSecurityManager.isStrongPassword(newPass)) {
                            Toast.makeText(context, "Enter valid ID & 8-char password!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        WorkoraRealOtpEngine.sendRealOtp(
                            context = context,
                            phoneOrEmail = cleanId,
                            emailOptional = if (cleanId.contains("@")) cleanId else "",
                            purpose = "PASSWORD RESET"
                        ) { masked ->
                            forgotMaskedTarget = masked
                            showForgotDialog = false
                            showForgotOtpVerify = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) {
                    Text("Send OTP", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showForgotOtpVerify) {
        val cleanId = forgotIdentifier.trim()
        val key = if (cleanId.contains("@")) cleanId.lowercase() else cleanId.filter { it.isDigit() }.takeLast(10)
        WorkoraRealOtpVerificationDialog(
            targetKey = key,
            maskedTargetDisplay = forgotMaskedTarget,
            purposeTitle = "Verify OTP",
            onResendClick = {
                WorkoraRealOtpEngine.sendRealOtp(
                    context = context,
                    phoneOrEmail = key,
                    emailOptional = if (cleanId.contains("@")) key else "",
                    purpose = "PASSWORD RESET"
                ) { masked ->
                    forgotMaskedTarget = masked
                }
            },
            onDismiss = { showForgotOtpVerify = false },
            onVerifiedSuccess = {
                showForgotOtpVerify = false
                val newHash = WorkoraSecurityManager.hashPasswordSecure(forgotNewPassword.trim())
                authPrefs.edit().apply {
                    putString("user_pass_$key", forgotNewPassword.trim())
                    putString("user_hash_$key", newHash)
                    apply()
                }
                passwordInput = forgotNewPassword.trim()
                Toast.makeText(context, "Password Reset ✓", Toast.LENGTH_LONG).show()
            }
        )
    }
}
