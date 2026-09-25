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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.ui.components.WorkoraHelmetLogo
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted
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

// Workora Practical Password Rule Validator
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
            ).apply {
                description = "6-Digit OTP for Workora Login & Registration"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("🔐 Workora Verification OTP: $otpCode")
            .setContentText("Aapka $target ke liye 6-digit OTP $otpCode hai. Ise kisi ke saath share na karein.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Aapka Workora Account ($target) verify karne ke liye 6-digit OTP hai: $otpCode\nYeh OTP 5 minute tak valid hai."
                )
            )
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
    val brandPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }

    val appName = remember { brandPrefs.getString("app_name", "WORKORA") ?: "WORKORA" }
    val appTagline = remember { brandPrefs.getString("app_tagline", "FIND. HIRE. WORK.") ?: "FIND. HIRE. WORK." }

    // 0 = Sign In, 1 = Mobile Registration with Practical Password Rules + OTP
    var activeAuthTab by remember { mutableIntStateOf(0) }

    // Sign In States
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    // Registration States
    var regFullName by remember { mutableStateOf("") }
    var regMobileNumber by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regVillageCity by remember { mutableStateOf("Silwani, Raisen (MP)") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }

    // Evaluate Practical Password Rules live as user types
    val regPasswordRules = remember(regPassword) { checkWorkoraPasswordRules(regPassword) }

    // OTP Verification States for Registration
    var isRegOtpSent by remember { mutableStateOf(false) }
    var generatedRegOtp by remember { mutableStateOf("") }
    var enteredRegOtp by remember { mutableStateOf("") }
    var isSendingOtp by remember { mutableStateOf(false) }

    // Forgot / Reset Password via OTP Dialog States
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotIdentifier by remember { mutableStateOf("") }
    var forgotGeneratedOtp by remember { mutableStateOf("") }
    var forgotEnteredOtp by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var isForgotOtpSent by remember { mutableStateOf(false) }
    val forgotPasswordRules = remember(forgotNewPassword) { checkWorkoraPasswordRules(forgotNewPassword) }

    fun generateAndSendOtp(targetIdentifier: String, onOtpReady: (String) -> Unit) {
        isSendingOtp = true
        val otp = Random.nextInt(100000, 999999).toString()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val safeKey = targetIdentifier.lowercase()
                    .replace(".", "_")
                    .replace("@", "_at_")
                    .replace("+", "")
                    .replace(" ", "_")
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
                Toast.makeText(
                    context,
                    "📩 6-Digit OTP Sent to $targetIdentifier! (OTP: $otp)",
                    Toast.LENGTH_LONG
                ).show()
                onOtpReady(otp)
            }
        }
    }

    fun performStrictCloudLogin(rawIdentifier: String, enteredPass: String) {
        isVerifying = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputDigits = rawIdentifier.filter { it.isDigit() }.takeLast(10)
                val conn = (URL("$AUTH_DB_URL/users.json").openConnection() as HttpURLConnection)
                var matchedEmail = ""
                var matchedName = ""
                var matchedPhone = ""
                var matchedRole = "CUSTOMER"
                var matchedStatus = "ACTIVE"
                var savedCloudPassword = ""
                var userFound = false

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

                            val emailMatches = uEmail.isNotBlank() && uEmail == rawIdentifier
                            val phoneMatches = inputDigits.length == 10 && uPhone == inputDigits

                            if (emailMatches || phoneMatches) {
                                userFound = true
                                matchedEmail = uEmail.ifBlank { "${inputDigits}@workora.in" }
                                matchedName = obj.optString("fullName", "User")
                                matchedPhone = obj.optString("phone", "+91 $inputDigits")
                                matchedRole = obj.optString("role", "CUSTOMER")
                                matchedStatus = obj.optString("accountStatus", "ACTIVE")
                                savedCloudPassword = obj.optString("password", "")
                                break
                            }
                        }
                    }
                }
                conn.disconnect()

                withContext(Dispatchers.Main) {
                    isVerifying = false

                    if (!userFound) {
                        Toast.makeText(
                            context,
                            "❌ Yeh Mobile Number ya Email registered nahi hai! Pehle 'Mobile Registration' tab se OTP verify karke account banayein.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@withContext
                    }

                    if (matchedStatus.equals("BLOCKED", ignoreCase = true)) {
                        Toast.makeText(
                            context,
                            "🚫 Aapka account Admin dwara Block kiya gaya hai!",
                            Toast.LENGTH_LONG
                        ).show()
                        return@withContext
                    }

                    if (savedCloudPassword != enteredPass) {
                        Toast.makeText(
                            context,
                            "❌ Galat Password! Kripya sahi password dalein ya niche 'Forgot Password (OTP)' par tap karein.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@withContext
                    }

                    val isMasterAdmin = matchedEmail == MASTER_ADMIN_EMAIL ||
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

                    Toast.makeText(
                        context,
                        if (isMasterAdmin) "✅ Welcome Super Admin $matchedName!" else "✅ Welcome $matchedName!",
                        Toast.LENGTH_SHORT
                    ).show()

                    onLogin(matchedEmail, enteredPass)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isVerifying = false
                    Toast.makeText(context, "Network Error: Internet check karein!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WorkoraHelmetLogo(size = 64.dp, showHalo = false)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = appName,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WorkoraNavy,
                letterSpacing = 1.5.sp
            )

            Text(
                text = appTagline,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraOrange,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Top Tabs: "Log In / Sign In" vs "Mobile Registration"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { activeAuthTab = 0 },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeAuthTab == 0) WorkoraNavy else Color.White
                    ),
                    border = BorderStroke(1.dp, if (activeAuthTab == 0) WorkoraNavy else WorkoraBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Log In / Sign In",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (activeAuthTab == 0) Color.White else WorkoraNavy
                    )
                }

                Button(
                    onClick = { activeAuthTab = 1 },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeAuthTab == 1) WorkoraOrange else Color.White
                    ),
                    border = BorderStroke(1.dp, if (activeAuthTab == 1) WorkoraOrange else WorkoraBorder),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Mobile Registration",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (activeAuthTab == 1) Color.White else WorkoraOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==================== TAB 0: STRICT SIGN IN ====================
            if (activeAuthTab == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sign In to Your Account",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraTextDark
                        )
                        Text(
                            text = "Apna registered Mobile Number ya Email aur sahi Password daal kar Log In karein",
                            fontSize = 11.sp,
                            color = WorkoraTextMuted
                        )

                        OutlinedTextField(
                            value = emailOrPhone,
                            onValueChange = { emailOrPhone = it },
                            label = { Text("Mobile Number or Email ID") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WorkoraOrange,
                                unfocusedBorderColor = WorkoraBorder
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = WorkoraTextMuted
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WorkoraOrange,
                                unfocusedBorderColor = WorkoraBorder
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password? (OTP se Reset karein)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = WorkoraOrange,
                                modifier = Modifier.clickable {
                                    forgotIdentifier = emailOrPhone
                                    isForgotOtpSent = false
                                    forgotEnteredOtp = ""
                                    forgotNewPassword = ""
                                    showForgotDialog = true
                                }
                            )
                        }

                        Button(
                            onClick = {
                                val rawInput = emailOrPhone.trim().lowercase()
                                val cleanPass = password.trim()

                                if (rawInput.isBlank() || cleanPass.isBlank()) {
                                    Toast.makeText(context, "Kripya Mobile Number/Email aur Password dalein!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                performStrictCloudLogin(rawInput, cleanPass)
                            },
                            enabled = !isVerifying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WorkoraNavy)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = "Verify Password & Log In ✓",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // ==================== TAB 1: SIGNUP WITH PRACTICAL PASSWORD RULES + 6-DIGIT OTP ====================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "New Registration (OTP Verification)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                        Text(
                            text = "Naya account banane ke liye details bharein aur 6-digit OTP verify karein",
                            fontSize = 11.sp,
                            color = WorkoraTextMuted
                        )

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Full Name (Aapka Poora Naam)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            enabled = !isRegOtpSent,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = regMobileNumber,
                            onValueChange = { regMobileNumber = it },
                            label = { Text("Mobile Number (10-Digit Number)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            enabled = !isRegOtpSent,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Gmail / Email ID") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            enabled = !isRegOtpSent,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = regVillageCity,
                            onValueChange = { regVillageCity = it },
                            label = { Text("Village / City (Gaon ya Shahar)") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            enabled = !isRegOtpSent,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Create Password (e.g. Ankit123)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = WorkoraTextMuted
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            enabled = !isRegOtpSent,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // LIVE WORKORA PRACTICAL PASSWORD RULES CHECKLIST
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WorkoraBgLight),
                            border = BorderStroke(
                                1.dp,
                                if (regPasswordRules.isMandatoryValid) Color(0xFF16A34A) else WorkoraBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Password Rules:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WorkoraNavy
                                    )
                                    val strengthLabel = when {
                                        regPasswordRules.isMandatoryValid && regPasswordRules.hasSpecialChar -> "💪 Strong Password"
                                        regPasswordRules.isMandatoryValid -> "✔ Good (Valid)"
                                        else -> "⏳ Incomplete"
                                    }
                                    val strengthColor = when {
                                        regPasswordRules.isMandatoryValid -> Color(0xFF16A34A)
                                        else -> WorkoraOrange
                                    }
                                    Text(
                                        text = strengthLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = strengthColor
                                    )
                                }

                                PasswordRuleRow(
                                    label = "Minimum 8 characters (Kam se kam 8 akshar)",
                                    isMet = regPasswordRules.hasMin8Chars,
                                    isOptional = false
                                )
                                PasswordRuleRow(
                                    label = "At least 1 letter (Kam se kam 1 letter A-Z / a-z)",
                                    isMet = regPasswordRules.hasLetter,
                                    isOptional = false
                                )
                                PasswordRuleRow(
                                    label = "At least 1 number (Kam se kam 1 number 0-9)",
                                    isMet = regPasswordRules.hasDigitNumber(),
                                    isOptional = false
                                )
                                PasswordRuleRow(
                                    label = "Special character (@, #, $ etc.) — Optional / Recommended",
                                    isMet = regPasswordRules.hasSpecialChar,
                                    isOptional = true
                                )
                            }
                        }

                        if (!isRegOtpSent) {
                            // STEP 1: SEND 6-DIGIT OTP BUTTON
                            Button(
                                onClick = {
                                    val cleanName = regFullName.trim()
                                    val digits = regMobileNumber.filter { it.isDigit() }.takeLast(10)

                                    if (cleanName.isBlank() || digits.length < 10) {
                                        Toast.makeText(context, "Kripya apna Naam aur 10-digit Mobile Number sahi dalein!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    if (!regPasswordRules.isMandatoryValid) {
                                        Toast.makeText(
                                            context,
                                            "Password mein kam se kam 8 characters, 1 letter aur 1 number hona zaroori hai!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@Button
                                    }

                                    val targetDisplay = if (regEmail.trim().contains("@")) {
                                        "+91 $digits & ${regEmail.trim()}"
                                    } else {
                                        "+91 $digits"
                                    }

                                    generateAndSendOtp(targetDisplay) { otp ->
                                        generatedRegOtp = otp
                                        enteredRegOtp = ""
                                        isRegOtpSent = true
                                    }
                                },
                                enabled = !isSendingOtp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (regPasswordRules.isMandatoryValid) WorkoraOrange else Color(0xFF94A3B8)
                                )
                            ) {
                                if (isSendingOtp) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Send 6-Digit OTP to Mobile / Gmail",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            // STEP 2: ENTER & VERIFY 6-DIGIT OTP BOX
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = BorderStroke(1.5.dp, Color(0xFF16A34A))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "📩 6-Digit OTP Sent! (Upar Notification bar mein aaya OTP dalein)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF16A34A)
                                    )

                                    OutlinedTextField(
                                        value = enteredRegOtp,
                                        onValueChange = { if (it.length <= 6) enteredRegOtp = it.filter { c -> c.isDigit() } },
                                        label = { Text("Enter 6-Digit OTP Code") },
                                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val digits = regMobileNumber.filter { it.isDigit() }.takeLast(10)
                                                generateAndSendOtp("+91 $digits") { otp ->
                                                    generatedRegOtp = otp
                                                }
                                            },
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Resend OTP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                if (enteredRegOtp.trim() != generatedRegOtp || generatedRegOtp.isBlank()) {
                                                    Toast.makeText(
                                                        context,
                                                        "❌ Galat OTP! Kripya sahi 6-digit OTP dalein.",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    return@Button
                                                }

                                                val cleanName = regFullName.trim()
                                                val digits = regMobileNumber.filter { it.isDigit() }.takeLast(10)
                                                val cleanPass = regPassword.trim()
                                                val cleanRegEmail = regEmail.trim().lowercase()

                                                val isMasterAdminReg = cleanRegEmail == MASTER_ADMIN_EMAIL ||
                                                        digits == MASTER_ADMIN_PHONE

                                                val finalEmail = if (isMasterAdminReg) {
                                                    MASTER_ADMIN_EMAIL
                                                } else if (cleanRegEmail.contains("@")) {
                                                    cleanRegEmail
                                                } else {
                                                    "${digits}@workora.in"
                                                }

                                                val assignedRole = if (isMasterAdminReg) "ADMIN" else "CUSTOMER"

                                                authPrefs.edit().apply {
                                                    putBoolean("is_logged_in", true)
                                                    putString("last_logged_in_email", finalEmail)
                                                    putString("user_pass_$finalEmail", cleanPass)
                                                    putString("phone_to_email_$digits", finalEmail)
                                                    if (isMasterAdminReg) {
                                                        putString("saved_user_role", "ADMIN")
                                                        putString("saved_admin_tier", "SUPER_ADMIN")
                                                    } else {
                                                        remove("saved_user_role")
                                                    }
                                                    apply()
                                                }

                                                profilePrefs.edit()
                                                    .putString("user_name", cleanName)
                                                    .putString("user_phone", "+91 $digits")
                                                    .putString("user_location", regVillageCity.trim())
                                                    .apply()

                                                FirebaseManager.syncUserToFirebase(
                                                    context = context,
                                                    name = cleanName,
                                                    email = finalEmail,
                                                    phone = "+91 $digits",
                                                    password = cleanPass,
                                                    role = assignedRole
                                                )

                                                Toast.makeText(
                                                    context,
                                                    "✅ OTP Verified! Account Created Successfully!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onLogin(finalEmail, cleanPass)
                                            },
                                            modifier = Modifier.weight(1.3f).height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                        ) {
                                            Text("Verify OTP & Register ✓", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                        }
                                    }

                                    Text(
                                        text = "← Edit Mobile Number / Details",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WorkoraNavy,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .clickable { isRegOtpSent = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (activeAuthTab == 0) "Naya account banana hai? " else "Pehle se account hai? ",
                    fontSize = 13.sp,
                    color = WorkoraTextMuted
                )
                Text(
                    text = if (activeAuthTab == 0) "Mobile Registration (OTP) karein" else "Log In karein",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraOrange,
                    modifier = Modifier.clickable {
                        activeAuthTab = if (activeAuthTab == 0) 1 else 0
                    }
                )
            }
        }
    }

    // ==================== FORGOT / RESET PASSWORD VIA 6-DIGIT OTP DIALOG ====================
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = {
                Text(
                    text = "🔐 Reset Password via OTP",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Apna registered Mobile Number ya Email dalein, us par 6-digit OTP aayega:",
                        fontSize = 12.sp,
                        color = WorkoraTextMuted
                    )

                    OutlinedTextField(
                        value = forgotIdentifier,
                        onValueChange = { forgotIdentifier = it },
                        label = { Text("Mobile Number or Email ID") },
                        singleLine = true,
                        enabled = !isForgotOtpSent,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isForgotOtpSent) {
                        Button(
                            onClick = {
                                val cleanTarget = forgotIdentifier.trim()
                                if (cleanTarget.length < 5) {
                                    Toast.makeText(context, "Kripya sahi Mobile Number ya Email dalein!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                generateAndSendOtp(cleanTarget) { otp ->
                                    forgotGeneratedOtp = otp
                                    isForgotOtpSent = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                        ) {
                            Text("Send 6-Digit OTP", fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }
                    } else {
                        OutlinedTextField(
                            value = forgotEnteredOtp,
                            onValueChange = { if (it.length <= 6) forgotEnteredOtp = it.filter { c -> c.isDigit() } },
                            label = { Text("Enter 6-Digit OTP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = forgotNewPassword,
                            onValueChange = { forgotNewPassword = it },
                            label = { Text("New Password (8+ chars, 1 letter, 1 number)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            PasswordRuleRow("Min 8 characters", forgotPasswordRules.hasMin8Chars, false)
                            PasswordRuleRow("At least 1 letter & 1 number", forgotPasswordRules.hasLetter && forgotPasswordRules.hasNumber, false)
                            PasswordRuleRow("Special character (Optional/Recommended)", forgotPasswordRules.hasSpecialChar, true)
                        }
                    }
                }
            },
            confirmButton = {
                if (isForgotOtpSent) {
                    Button(
                        onClick = {
                            if (forgotEnteredOtp.trim() != forgotGeneratedOtp || forgotGeneratedOtp.isBlank()) {
                                Toast.makeText(context, "❌ Galat OTP! Sahi 6-digit OTP dalein.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!forgotPasswordRules.isMandatoryValid) {
                                Toast.makeText(
                                    context,
                                    "Password mein kam se kam 8 characters, 1 letter aur 1 number zaroori hai!",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            val rawTarget = forgotIdentifier.trim().lowercase()
                            val digits = rawTarget.filter { it.isDigit() }.takeLast(10)
                            val isMaster = rawTarget == MASTER_ADMIN_EMAIL || digits == MASTER_ADMIN_PHONE
                            val targetEmail = if (isMaster) MASTER_ADMIN_EMAIL else if (rawTarget.contains("@")) rawTarget else "${digits}@workora.in"
                            val cleanNewPass = forgotNewPassword.trim()

                            authPrefs.edit().putString("user_pass_$targetEmail", cleanNewPass).apply()

                            FirebaseManager.syncUserToFirebase(
                                context = context,
                                name = if (isMaster) "Ankit Ahirwar" else (profilePrefs.getString("user_name", "User") ?: "User"),
                                email = targetEmail,
                                phone = if (digits.length == 10) "+91 $digits" else "+91 $MASTER_ADMIN_PHONE",
                                password = cleanNewPass,
                                role = if (isMaster) "ADMIN" else "CUSTOMER"
                            )

                            emailOrPhone = targetEmail
                            password = cleanNewPass
                            showForgotDialog = false
                            Toast.makeText(context, "✅ Password Reset Successful! Ab Log In karein.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("Verify OTP & Save Password ✓", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun PasswordRuleStatus.hasDigitNumber(): Boolean = this.hasNumber

@Composable
private fun PasswordRuleRow(
    label: String,
    isMet: Boolean,
    isOptional: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isOptional) Icons.Default.Star else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = when {
                isMet -> Color(0xFF16A34A)
                isOptional -> WorkoraOrange
                else -> Color(0xFF94A3B8)
            },
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isMet) FontWeight.Bold else FontWeight.Medium,
            color = if (isMet) Color(0xFF16A34A) else WorkoraTextMuted
        )
    }
}
