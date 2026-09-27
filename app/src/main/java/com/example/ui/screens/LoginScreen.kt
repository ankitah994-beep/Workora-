package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WorkoraHelmetLogo
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted

// Compatibility Overloads so MainActivity.kt compiles with any callback signature
@Composable
fun LoginScreen(
    onLogin: (String) -> Unit,
    onNavigateToSignUp: () -> Unit = {}
) {
    LoginScreen(
        onLogin = { identifier, _ -> onLogin(identifier) },
        onNavigateToSignUp = onNavigateToSignUp
    )
}

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit = {}
) {
    LoginScreen(
        onLogin = { _, _ -> onLogin() },
        onNavigateToSignUp = onNavigateToSignUp
    )
}

@Composable
fun LoginScreen(
    onLogin: (emailOrPhone: String, password: String) -> Unit = { _, _ -> },
    onNavigateToSignUp: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    // 0 = Mobile Number Login | 1 = Gmail / Email Login
    var loginInputType by remember { mutableIntStateOf(0) }
    // 0 = Password + Real OTP | 1 = Direct Real OTP Only
    var authMethodTab by remember { mutableIntStateOf(0) }

    var identifierInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isSendingOtp by remember { mutableStateOf(false) }
    var showLoginOtpDialog by remember { mutableStateOf(false) }
    var maskedOtpTarget by remember { mutableStateOf("") }

    // Forgot Password via Real OTP States
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotIdentifier by remember { mutableStateOf("") }
    var forgotNewPassword by remember { mutableStateOf("") }
    var showForgotOtpVerify by remember { mutableStateOf(false) }
    var forgotMaskedTarget by remember { mutableStateOf("") }

    fun validateAndSendLoginOtp() {
        val rawId = identifierInput.trim()
        if (rawId.isBlank()) {
            Toast.makeText(context, "Kripya Mobile Number ya Gmail ID dalein!", Toast.LENGTH_SHORT).show()
            return
        }

        val isEmail = loginInputType == 1 || rawId.contains("@")
        val cleanDigits = rawId.filter { it.isDigit() }.takeLast(10)

        if (!isEmail && cleanDigits.length != 10) {
            Toast.makeText(context, "Kripya sahi 10-digit Mobile Number dalein!", Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmail && !rawId.contains("@")) {
            Toast.makeText(context, "Kripya sahi Gmail / Email ID dalein!", Toast.LENGTH_SHORT).show()
            return
        }

        val lookupKey = if (isEmail) rawId.lowercase() else cleanDigits

        // 1. Check Brute-Force Lockout
        val (allowed, lockMsg) = WorkoraSecurityManager.checkLoginBruteForceAllowed(context, lookupKey)
        if (!allowed) {
            Toast.makeText(context, lockMsg, Toast.LENGTH_LONG).show()
            return
        }

        // 2. If Password + Real OTP mode is selected, validate password first
        if (authMethodTab == 0) {
            val cleanPass = passwordInput.trim()
            if (cleanPass.length < 6) {
                Toast.makeText(context, "Kripya apna Password dalein!", Toast.LENGTH_SHORT).show()
                return
            }

            val storedPass = authPrefs.getString("user_pass_$lookupKey", null)
            val storedHash = authPrefs.getString("user_hash_$lookupKey", null)

            val isKnownAdmin = (lookupKey == "ankitah994@gmail.com" || lookupKey == "6265798340")
            val passMatches = when {
                storedHash != null -> WorkoraSecurityManager.verifyPasswordSecure(cleanPass, storedHash)
                storedPass != null -> storedPass == cleanPass
                isKnownAdmin -> cleanPass.length >= 6
                else -> true // New device login; Real OTP will verify ownership
            }

            if (!passMatches) {
                WorkoraSecurityManager.recordLoginAttempt(context, lookupKey, isSuccess = false)
                Toast.makeText(context, "Galat Password! Kripya sahi password dalein.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // 3. Always dispatch a fresh 6-Digit Real OTP!
        isSendingOtp = true
        WorkoraRealOtpEngine.sendRealOtp(
            context = context,
            phoneOrEmail = if (isEmail) rawId.lowercase() else cleanDigits,
            emailOptional = if (isEmail) rawId.lowercase() else "",
            purpose = "ACCOUNT LOGIN"
        ) { masked ->
            isSendingOtp = false
            maskedOtpTarget = masked
            showLoginOtpDialog = true
            Toast.makeText(
                context,
                "Real 6-Digit OTP sent to $masked! ✓",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        WorkoraHelmetLogo(size = 68.dp, showHalo = false)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "WORKORA",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WorkoraNavy,
            letterSpacing = 1.sp
        )

        Text(
            text = "Secure Real-OTP Login • Find. Hire. Work.",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = WorkoraTextMuted
        )

        Spacer(modifier = Modifier.height(22.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Selector: Mobile Number vs Gmail ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "📱 Mobile Number", 1 to "✉️ Gmail / Email").forEach { (idx, title) ->
                        val selected = loginInputType == idx
                        Button(
                            onClick = {
                                loginInputType = idx
                                identifierInput = ""
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) WorkoraNavy else Color(0xFFF1F5F9)
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selected) Color.White else WorkoraTextDark
                            )
                        }
                    }
                }

                // Selector: Password + Real OTP vs Direct Real OTP
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0 to "🔐 Password + Real OTP", 1 to "⚡ Direct Real OTP").forEach { (idx, label) ->
                        val selected = authMethodTab == idx
                        OutlinedButton(
                            onClick = { authMethodTab = idx },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (selected) WorkoraOrange else WorkoraBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) Color(0xFFFFF0DE) else Color.White
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) WorkoraOrange else WorkoraTextDark
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = identifierInput,
                    onValueChange = { identifierInput = it },
                    label = {
                        Text(
                            if (loginInputType == 0) "10-Digit Mobile Number" else "Gmail / Email Address"
                        )
                    },
                    placeholder = {
                        Text(
                            if (loginInputType == 0) "e.g. 6265798340" else "e.g. ankitah994@gmail.com"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (loginInputType == 0) Icons.Default.Phone else Icons.Default.Email,
                            contentDescription = null,
                            tint = WorkoraOrange
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (loginInputType == 0) KeyboardType.Phone else KeyboardType.Email
                    ),
                    textStyle = TextStyle(color = WorkoraTextDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (authMethodTab == 0) {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Enter Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = WorkoraOrange
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = TextStyle(color = WorkoraTextDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                        singleLine = true,
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
                            text = "Forgot Password? Reset via Real OTP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraNavy,
                            modifier = Modifier.clickable {
                                forgotIdentifier = identifierInput
                                showForgotDialog = true
                            }
                        )
                    }
                }

                Button(
                    onClick = { validateAndSendLoginOtp() },
                    enabled = !isSendingOtp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) {
                    if (isSendingOtp) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Send Real 6-Digit OTP & Log In ✓",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Naya account banana hai? ",
                fontSize = 13.sp,
                color = WorkoraTextMuted
            )
            Text(
                text = "Create Account (Sign Up)",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WorkoraNavy,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }

    // ==================== REAL 6-DIGIT OTP VERIFICATION MODAL FOR LOGIN ====================
    if (showLoginOtpDialog) {
        val rawId = identifierInput.trim()
        val isEmail = loginInputType == 1 || rawId.contains("@")
        val cleanDigits = rawId.filter { it.isDigit() }.takeLast(10)
        val targetKey = if (isEmail) rawId.lowercase() else cleanDigits

        WorkoraRealOtpDialog(
            maskedDestination = maskedOtpTarget,
            purposeLabel = "Secure Account Login",
            onVerifyCode = { enteredCode ->
                WorkoraRealOtpEngine.verifyRealOtp(
                    context = context,
                    phoneOrEmail = targetKey,
                    emailOptional = if (isEmail) targetKey else "",
                    enteredOtp = enteredCode
                )
            },
            onResendOtp = {
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
                } else if (cleanDigits == "6265798340") {
                    "ankitah994@gmail.com"
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

                Toast.makeText(context, "Real OTP Verified! Welcome to Workora ✓", Toast.LENGTH_SHORT).show()
                onLogin(finalEmail, passwordInput.ifBlank { "OTP_VERIFIED" })
            }
        )
    }

    // ==================== FORGOT PASSWORD VIA REAL OTP DIALOG ====================
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    text = "Reset Password via Real OTP",
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
                        label = { Text("Registered Mobile or Gmail") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = forgotNewPassword,
                        onValueChange = { forgotNewPassword = it },
                        label = { Text("New Password (min 8 chars, letter + digit)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanTarget = forgotIdentifier.trim()
                        val newPass = forgotNewPassword.trim()
                        if (cleanTarget.length < 5 || !WorkoraSecurityManager.isStrongPassword(newPass)) {
                            Toast.makeText(
                                context,
                                "Enter valid Mobile/Gmail and strong password (8+ chars, letter & number)!",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        WorkoraRealOtpEngine.sendRealOtp(
                            context = context,
                            phoneOrEmail = cleanTarget,
                            emailOptional = if (cleanTarget.contains("@")) cleanTarget else "",
                            purpose = "PASSWORD RESET"
                        ) { masked ->
                            forgotMaskedTarget = masked
                            showForgotDialog = false
                            showForgotOtpVerify = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) {
                    Text("Send Real OTP", color = Color.White, fontWeight = FontWeight.Bold)
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
        val cleanTarget = forgotIdentifier.trim()
        val digits = cleanTarget.filter { it.isDigit() }.takeLast(10)
        val key = if (cleanTarget.contains("@")) cleanTarget.lowercase() else digits

        WorkoraRealOtpDialog(
            maskedDestination = forgotMaskedTarget,
            purposeLabel = "Password Reset",
            onVerifyCode = { code ->
                WorkoraRealOtpEngine.verifyRealOtp(
                    context = context,
                    phoneOrEmail = key,
                    emailOptional = if (cleanTarget.contains("@")) key else "",
                    enteredOtp = code
                )
            },
            onResendOtp = {
                WorkoraRealOtpEngine.sendRealOtp(
                    context = context,
                    phoneOrEmail = key,
                    emailOptional = if (cleanTarget.contains("@")) key else "",
                    purpose = "PASSWORD RESET"
                ) { masked ->
                    forgotMaskedTarget = masked
                }
            },
            onDismiss = { showForgotOtpVerify = false },
            onVerifiedSuccess = {
                showForgotOtpVerify = false
                val newPass = forgotNewPassword.trim()
                val hash = WorkoraSecurityManager.hashPasswordSecure(newPass)
                authPrefs.edit().apply {
                    putString("user_pass_$key", newPass)
                    putString("user_hash_$key", hash)
                    apply()
                }
                passwordInput = newPass
                Toast.makeText(context, "Password Reset Successfully via Real OTP! ✓", Toast.LENGTH_LONG).show()
            }
        )
    }
}
