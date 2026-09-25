package com.example.ui.screens

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

private const val AUTH_DB_URL = "https://workora-d8b51-default-rtdb.firebaseio.com"
private const val MASTER_ADMIN_EMAIL = "ankitah994@gmail.com"
private const val MASTER_ADMIN_PHONE = "6265798340"

private val LoginNavy = Color(0xFF1E1F5E)
private val LoginOrange = Color(0xFFFF9800)

data class PasswordRuleStatus(
    val hasMin8Chars: Boolean,
    val hasLetter: Boolean,
    val hasNumber: Boolean,
    val hasSpecialChar: Boolean
) {
    val isMandatoryValid: Boolean
        get() = hasMin8Chars && hasLetter && hasNumber
}

private fun checkWorkoraPasswordRules(password: String): PasswordRuleStatus {
    val clean = password.trim()
    return PasswordRuleStatus(
        hasMin8Chars = clean.length >= 8,
        hasLetter = clean.any { it.isLetter() },
        hasNumber = clean.any { it.isDigit() },
        hasSpecialChar = clean.any { !it.isLetterOrDigit() && !it.isWhitespace() }
    )
}

private fun sendOtpNotificationToPhone(context: Context, target: String, otpCode: String) {
    try {
        val channelId = "workora_otp_security_channel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Workora OTP Verification",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("🔐 Workora Verification OTP: $otpCode")
            .setContentText("Your 6-digit OTP for $target is $otpCode.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(Random.nextInt(1000, 9999), notification)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onNavigateToSignUp: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    // 0 = Login, 1 = Register, 2 = Reset Password via OTP
    var activeAuthTab by remember { mutableIntStateOf(0) }

    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    var regFullName by remember { mutableStateOf("") }
    var regMobileNumber by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regVillageCity by remember { mutableStateOf("Silwani, Raisen (MP)") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(true) }
    val regPasswordRules = remember(regPassword) { checkWorkoraPasswordRules(regPassword) }

    var isRegOtpSent by remember { mutableStateOf(false) }
    var generatedRegOtp by remember { mutableStateOf("") }
    var enteredRegOtp by remember { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }

    var forgotIdentifier by remember { mutableStateOf("") }
    var forgotGeneratedOtp by remember { mutableStateOf("") }
    var forgotEnteredOtp by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var forgotPasswordVisible by remember { mutableStateOf(true) }
    var isForgotOtpSent by remember { mutableStateOf(false) }
    var isSavingResetPass by remember { mutableStateOf(false) }
    val forgotPasswordRules = remember(forgotNewPassword) { checkWorkoraPasswordRules(forgotNewPassword) }

    fun saveUserAndPasswordEverywhere(
        name: String,
        email: String,
        phoneDigits: String,
        newPass: String,
        role: String,
        onComplete: () -> Unit
    ) {
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = if (phoneDigits.length == 10) "+91 $phoneDigits" else "+91 $MASTER_ADMIN_PHONE"

        authPrefs.edit().apply {
            putString("user_pass_$cleanEmail", newPass)
            if (phoneDigits.length == 10) {
                putString("user_pass_$phoneDigits", newPass)
                putString("phone_to_email_$phoneDigits", cleanEmail)
            }
            apply()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val safeKey = cleanEmail.replace(".", "_").replace("@", "_at_").replace(" ", "_")
                val getConn = (URL("$AUTH_DB_URL/users.json").openConnection() as HttpURLConnection)
                val keysToUpdate = mutableSetOf(safeKey)
                if (getConn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(getConn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val uEmail = obj.optString("email", "").trim().lowercase()
                            val uPhone = obj.optString("phone", "").filter { it.isDigit() }.takeLast(10)
                            if (uEmail == cleanEmail || (phoneDigits.length == 10 && uPhone == phoneDigits)) {
                                keysToUpdate.add(k)
                            }
                        }
                    }
                }
                getConn.disconnect()

                for (k in keysToUpdate) {
                    val putConn = (URL("$AUTH_DB_URL/users/$k.json").openConnection() as HttpURLConnection).apply {
                        requestMethod = "PUT"
                        setRequestProperty("Content-Type", "application/json")
                        doOutput = true
                    }
                    val json = JSONObject().apply {
                        put("fullName", name)
                        put("email", cleanEmail)
                        put("phone", cleanPhone)
                        put("password", newPass)
                        put("role", role)
                        put("accountStatus", "ACTIVE")
                        put("location", profilePrefs.getString("user_location", "Silwani, Raisen (MP)") ?: "Silwani, Raisen (MP)")
                        put("updatedAt", System.currentTimeMillis())
                    }
                    OutputStreamWriter(putConn.outputStream).use { it.write(json.toString()) }
                    putConn.responseCode
                    putConn.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    fun generateAndSendOtp(targetIdentifier: String, onOtpReady: (String) -> Unit) {
        isSendingOtp = true
        val otp = Random.nextInt(100000, 999999).toString()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val safeKey = targetIdentifier.lowercase().replace(".", "_").replace("@", "_at_").replace("+", "").replace(" ", "_")
                val conn = (URL("$AUTH_DB_URL/otps/$safeKey.json").openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                }
                val json = JSONObject().apply {
                    put("target", targetIdentifier)
                    put("otp", otp)
                    put("timestamp", System.currentTimeMillis())
                }
                OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }
                conn.responseCode
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                isSendingOtp = false
                sendOtpNotificationToPhone(context, targetIdentifier, otp)
                Toast.makeText(context, "📩 6-Digit OTP Sent: $otp", Toast.LENGTH_LONG).show()
                onOtpReady(otp)
            }
        }
    }

    fun performStrictCloudLogin(rawIdentifier: String, enteredPass: String) {
        isVerifying = true
        val inputDigits = rawIdentifier.filter { it.isDigit() }.takeLast(10)
        val isMasterAdminInput = rawIdentifier == MASTER_ADMIN_EMAIL || inputDigits == MASTER_ADMIN_PHONE

        val localSavedByEmail = authPrefs.getString("user_pass_$rawIdentifier", null)
        val localSavedByPhone = if (inputDigits.length == 10) authPrefs.getString("user_pass_$inputDigits", null) else null
        val localSavedMaster = if (isMasterAdminInput) authPrefs.getString("user_pass_$MASTER_ADMIN_EMAIL", null) else null

        CoroutineScope(Dispatchers.IO).launch {
            var matchedEmail = if (isMasterAdminInput) MASTER_ADMIN_EMAIL else rawIdentifier
            var matchedName = if (isMasterAdminInput) "Ankit Ahirwar" else (profilePrefs.getString("user_name", "Rahul") ?: "Rahul")
            var matchedPhone = if (inputDigits.length == 10) "+91 $inputDigits" else "+91 $MASTER_ADMIN_PHONE"
            var matchedRole = if (isMasterAdminInput) "ADMIN" else "CUSTOMER"
            var matchedStatus = "ACTIVE"
            val cloudPasswords = mutableListOf<String>()
            var userFoundInCloud = false

            try {
                val conn = (URL("$AUTH_DB_URL/users.json").openConnection() as HttpURLConnection)
                if (conn.responseCode in 200..299) {
                    val text = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                    if (text.isNotBlank() && text != "null") {
                        val root = JSONObject(text)
                        val keys = root.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val obj = root.optJSONObject(k) ?: continue
                            val uEmail = obj.optString("email", "").trim().lowercase()
                            val uPhone = obj.optString("phone", "").filter { it.isDigit() }.takeLast(10)

                            val emailMatches = uEmail.isNotBlank() && (uEmail == rawIdentifier || (isMasterAdminInput && uEmail == MASTER_ADMIN_EMAIL))
                            val phoneMatches = inputDigits.length == 10 && uPhone == inputDigits

                            if (emailMatches || phoneMatches) {
                                userFoundInCloud = true
                                if (uEmail.isNotBlank()) matchedEmail = uEmail
                                val cName = obj.optString("fullName", "")
                                if (cName.isNotBlank()) matchedName = cName
                                val cPhone = obj.optString("phone", "")
                                if (cPhone.isNotBlank()) matchedPhone = cPhone
                                matchedRole = obj.optString("role", matchedRole)
                                matchedStatus = obj.optString("accountStatus", "ACTIVE")
                                val cPass = obj.optString("password", "")
                                if (cPass.isNotBlank()) cloudPasswords.add(cPass)
                            }
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                isVerifying = false
                val hasAnyLocalAccount = localSavedByEmail != null || localSavedByPhone != null || localSavedMaster != null

                if (!userFoundInCloud && !hasAnyLocalAccount) {
                    Toast.makeText(context, "❌ Account not found! Please Register or Reset Password via OTP.", Toast.LENGTH_LONG).show()
                    return@withContext
                }

                if (matchedStatus.equals("BLOCKED", ignoreCase = true)) {
                    Toast.makeText(context, "🚫 Account Blocked by Admin!", Toast.LENGTH_LONG).show()
                    return@withContext
                }

                val isPasswordCorrect = when {
                    localSavedByEmail != null && localSavedByEmail == enteredPass -> true
                    localSavedByPhone != null && localSavedByPhone == enteredPass -> true
                    localSavedMaster != null && localSavedMaster == enteredPass -> true
                    cloudPasswords.any { it == enteredPass } -> true
                    else -> false
                }

                if (!isPasswordCorrect) {
                    Toast.makeText(context, "❌ Wrong Password! Tap 'Forgot Password?' below to reset.", Toast.LENGTH_LONG).show()
                    return@withContext
                }

                val isMasterAdmin = isMasterAdminInput ||
                        matchedEmail == MASTER_ADMIN_EMAIL ||
                        matchedPhone.filter { it.isDigit() }.takeLast(10) == MASTER_ADMIN_PHONE ||
                        matchedRole.equals("ADMIN", ignoreCase = true)

                authPrefs.edit().apply {
                    putBoolean("is_logged_in", true)
                    putString("last_logged_in_email", matchedEmail)
                    putString("user_pass_$matchedEmail", enteredPass)
                    if (isMasterAdmin) {
                        putString("saved_user_role", "ADMIN")
                        putString("saved_admin_tier", "SUPER_ADMIN")
                    } else {
                        remove("saved_user_role")
                    }
                    apply()
                }

                profilePrefs.edit()
                    .putString("user_name", matchedName)
                    .putString("user_phone", matchedPhone)
                    .apply()

                onLogin(matchedEmail, enteredPass)
            }
        }
    }

    // Main White Login Screen (Exact match to Photo Screen 3)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        IconButton(
            onClick = { if (activeAuthTab != 0) activeAuthTab = 0 },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1F2937))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (activeAuthTab == 1) "Create Account 👋" else if (activeAuthTab == 2) "Reset Password 🔐" else "Welcome Back 👋",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (activeAuthTab == 1) "Register with OTP verification" else "Login to continue",
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Clean Underline Tabs: "Login" | "Register" (Exact match to Photo Screen 3)
        if (activeAuthTab != 2) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeAuthTab = 0 },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = if (activeAuthTab == 0) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (activeAuthTab == 0) LoginNavy else Color(0xFF9CA3AF),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    HorizontalDivider(
                        thickness = if (activeAuthTab == 0) 2.5.dp else 1.dp,
                        color = if (activeAuthTab == 0) LoginNavy else Color(0xFFE5E7EB)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeAuthTab = 1 },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Register",
                        fontSize = 16.sp,
                        fontWeight = if (activeAuthTab == 1) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (activeAuthTab == 1) LoginNavy else Color(0xFF9CA3AF),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    HorizontalDivider(
                        thickness = if (activeAuthTab == 1) 2.5.dp else 1.dp,
                        color = if (activeAuthTab == 1) LoginNavy else Color(0xFFE5E7EB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // ==================== TAB 0: LOGIN VIEW ====================
        if (activeAuthTab == 0) {
            Text("Mobile Number or Email", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B5563))
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                placeholder = { Text("98765 43210 or email", color = Color(0xFF9CA3AF)) },
                leadingIcon = {
                    Text(
                        text = "+91",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937),
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                    )
                },
                textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LoginNavy,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Password", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4B5563))
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("••••••••••", color = Color(0xFF9CA3AF)) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password",
                            tint = Color(0xFF6B7280)
                        )
                    }
                },
                textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LoginNavy,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB)
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoginNavy,
                    modifier = Modifier.clickable {
                        forgotIdentifier = emailOrPhone
                        isForgotOtpSent = false
                        forgotEnteredOtp = ""
                        forgotNewPassword = ""
                        activeAuthTab = 2
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val rawInput = emailOrPhone.trim().lowercase()
                    val cleanPass = password.trim()
                    if (rawInput.isBlank() || cleanPass.isBlank()) {
                        Toast.makeText(context, "Please enter Mobile Number/Email and Password!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    performStrictCloudLogin(rawInput, cleanPass)
                },
                enabled = !isVerifying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LoginNavy)
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // "or continue with" Divider (Exact match to Photo Screen 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                Text(
                    text = "  or continue with  ",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Circular Google & Phone OTP Buttons (Exact match to Photo Screen 3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        emailOrPhone = MASTER_ADMIN_EMAIL
                        Toast.makeText(context, "Enter your password or tap Phone OTP", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF9FAFB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFEA4335))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Google", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B5563))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        forgotIdentifier = emailOrPhone
                        isForgotOtpSent = false
                        activeAuthTab = 2
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF9FAFB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Phone OTP", tint = LoginNavy, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Phone OTP", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4B5563))
                }
            }
        }

        // ==================== TAB 1: REGISTER WITH PRACTICAL PASSWORD RULES + OTP ====================
        if (activeAuthTab == 1) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = regFullName,
                    onValueChange = { regFullName = it },
                    label = { Text("Full Name") },
                    textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    enabled = !isRegOtpSent,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = regMobileNumber,
                    onValueChange = { regMobileNumber = it },
                    label = { Text("Mobile Number (+91)") },
                    textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    enabled = !isRegOtpSent,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = regEmail,
                    onValueChange = { regEmail = it },
                    label = { Text("Email ID (Optional)") },
                    textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    enabled = !isRegOtpSent,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = regVillageCity,
                    onValueChange = { regVillageCity = it },
                    label = { Text("City / Village") },
                    textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    enabled = !isRegOtpSent,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = regPassword,
                    onValueChange = { regPassword = it },
                    label = { Text("Create Password (8+ chars, 1 letter, 1 number)") },
                    trailingIcon = {
                        IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                            Icon(
                                imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = LoginNavy
                            )
                        }
                    },
                    textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isRegOtpSent,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                    border = BorderStroke(1.dp, if (regPasswordRules.isMandatoryValid) Color(0xFF16A34A) else Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PasswordRuleRow("Minimum 8 characters", regPasswordRules.hasMin8Chars, false)
                        PasswordRuleRow("At least 1 letter (A-Z / a-z)", regPasswordRules.hasLetter, false)
                        PasswordRuleRow("At least 1 number (0-9)", regPasswordRules.hasNumber, false)
                        PasswordRuleRow("Special character (@, #, $) — Optional", regPasswordRules.hasSpecialChar, true)
                    }
                }

                if (!isRegOtpSent) {
                    Button(
                        onClick = {
                            val cleanName = regFullName.trim()
                            val digits = regMobileNumber.filter { it.isDigit() }.takeLast(10)
                            if (cleanName.isBlank() || digits.length < 10) {
                                Toast.makeText(context, "Enter Name and 10-digit Mobile Number!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!regPasswordRules.isMandatoryValid) {
                                Toast.makeText(context, "Password must have 8+ chars, 1 letter & 1 number!", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            generateAndSendOtp("+91 $digits") { otp ->
                                generatedRegOtp = otp
                                enteredRegOtp = ""
                                isRegOtpSent = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LoginNavy)
                    ) {
                        Text("Send 6-Digit OTP", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                } else {
                    OutlinedTextField(
                        value = enteredRegOtp,
                        onValueChange = { if (it.length <= 6) enteredRegOtp = it.filter { c -> c.isDigit() } },
                        label = { Text("Enter 6-Digit OTP") },
                        textStyle = TextStyle(color = Color(0xFF111827), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (enteredRegOtp.trim() != generatedRegOtp || generatedRegOtp.isBlank()) {
                                Toast.makeText(context, "❌ Invalid OTP!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val cleanName = regFullName.trim()
                            val digits = regMobileNumber.filter { it.isDigit() }.takeLast(10)
                            val cleanPass = regPassword.trim()
                            val cleanRegEmail = regEmail.trim().lowercase()
                            val isMasterAdminReg = cleanRegEmail == MASTER_ADMIN_EMAIL || digits == MASTER_ADMIN_PHONE
                            val finalEmail = if (isMasterAdminReg) MASTER_ADMIN_EMAIL else if (cleanRegEmail.contains("@")) cleanRegEmail else "${digits}@workora.in"
                            val assignedRole = if (isMasterAdminReg) "ADMIN" else "CUSTOMER"

                            saveUserAndPasswordEverywhere(cleanName, finalEmail, digits, cleanPass, assignedRole) {
                                authPrefs.edit().apply {
                                    putBoolean("is_logged_in", true)
                                    putString("last_logged_in_email", finalEmail)
                                    if (isMasterAdminReg) putString("saved_user_role", "ADMIN") else remove("saved_user_role")
                                    apply()
                                }
                                profilePrefs.edit()
                                    .putString("user_name", cleanName)
                                    .putString("user_phone", "+91 $digits")
                                    .putString("user_location", regVillageCity.trim())
                                    .apply()
                                onLogin(finalEmail, cleanPass)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("Verify OTP & Register ✓", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }

        // ==================== TAB 2: RESET PASSWORD VIA OTP ====================
        if (activeAuthTab == 2) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = forgotIdentifier,
                    onValueChange = { forgotIdentifier = it },
                    label = { Text("Mobile Number or Email ID") },
                    textStyle = TextStyle(color = Color(0xFF111827), fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    enabled = !isForgotOtpSent,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (!isForgotOtpSent) {
                    Button(
                        onClick = {
                            if (forgotIdentifier.trim().length < 5) {
                                Toast.makeText(context, "Enter valid Mobile Number or Email!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            generateAndSendOtp(forgotIdentifier.trim()) { otp ->
                                forgotGeneratedOtp = otp
                                isForgotOtpSent = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LoginNavy)
                    ) {
                        Text("Send 6-Digit OTP", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                } else {
                    OutlinedTextField(
                        value = forgotEnteredOtp,
                        onValueChange = { if (it.length <= 6) forgotEnteredOtp = it.filter { c -> c.isDigit() } },
                        label = { Text("Enter 6-Digit OTP") },
                        textStyle = TextStyle(color = Color(0xFF111827), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = forgotNewPassword,
                        onValueChange = { forgotNewPassword = it },
                        label = { Text("Enter New Password (e.g. Ankit123)") },
                        trailingIcon = {
                            IconButton(onClick = { forgotPasswordVisible = !forgotPasswordVisible }) {
                                Icon(
                                    imageVector = if (forgotPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = LoginNavy
                                )
                            }
                        },
                        textStyle = TextStyle(color = Color(0xFF111827), fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                        visualTransformation = if (forgotPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            PasswordRuleRow("Minimum 8 characters", forgotPasswordRules.hasMin8Chars, false)
                            PasswordRuleRow("At least 1 letter & 1 number", forgotPasswordRules.hasLetter && forgotPasswordRules.hasNumber, false)
                            PasswordRuleRow("Special character — Optional", forgotPasswordRules.hasSpecialChar, true)
                        }
                    }

                    Button(
                        onClick = {
                            if (forgotEnteredOtp.trim() != forgotGeneratedOtp || forgotGeneratedOtp.isBlank()) {
                                Toast.makeText(context, "❌ Wrong OTP!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!forgotPasswordRules.isMandatoryValid) {
                                Toast.makeText(context, "❌ Password must have 8+ chars, 1 letter & 1 number!", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            isSavingResetPass = true
                            val rawTarget = forgotIdentifier.trim().lowercase()
                            val digits = rawTarget.filter { it.isDigit() }.takeLast(10)
                            val isMaster = rawTarget == MASTER_ADMIN_EMAIL || rawTarget.contains("ankitah994") || digits == MASTER_ADMIN_PHONE
                            val targetEmail = if (isMaster) MASTER_ADMIN_EMAIL else if (rawTarget.contains("@")) rawTarget else "${digits}@workora.in"
                            val cleanNewPass = forgotNewPassword.trim()

                            saveUserAndPasswordEverywhere(
                                name = if (isMaster) "Ankit Ahirwar" else (profilePrefs.getString("user_name", "Rahul") ?: "Rahul"),
                                email = targetEmail,
                                phoneDigits = if (digits.length == 10) digits else MASTER_ADMIN_PHONE,
                                newPass = cleanNewPass,
                                role = if (isMaster) "ADMIN" else "CUSTOMER"
                            ) {
                                isSavingResetPass = false
                                emailOrPhone = targetEmail
                                password = cleanNewPass
                                activeAuthTab = 0
                                Toast.makeText(context, "✅ Password Saved! Tap Login now.", Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isSavingResetPass,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("Verify OTP & Save Password ✓", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(200.dp))
    }
}

@Composable
private fun PasswordRuleRow(label: String, isMet: Boolean, isOptional: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            imageVector = if (isOptional) Icons.Default.Star else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isMet) Color(0xFF16A34A) else if (isOptional) LoginOrange else Color(0xFF9CA3AF),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isMet) FontWeight.Bold else FontWeight.Medium,
            color = if (isMet) Color(0xFF16A34A) else Color(0xFF6B7280)
        )
    }
}
