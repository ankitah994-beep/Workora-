package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.components.WorkoraHelmetLogo
import kotlinx.coroutines.delay

// =========================================================================
// 3-LAYER SUPER ADMIN SECURITY GATE DIALOG (REUSABLE ACROSS ENTIRE APP)
// Layer 1: Secret 6-Digit Admin Master PIN (Hardware Encrypted AES-256)
// Layer 2: Real 6-Digit Admin OTP sent to +91 6265798340 & ankitah994@gmail.com
// Layer 3: 3-Attempt Brute-Force Lockout (15 Mins) + Admin Audit Logging
// =========================================================================
@Composable
fun WorkoraAdminSecurityGateDialog(
    onDismiss: () -> Unit,
    onAdminVerifiedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val cardBg = WorkoraThemeManager.surfaceColor(context)
    val deepNavy = WorkoraThemeManager.accentBlue(context)
    val brandOrange = Color(0xFFFF8C00)
    val textDark = WorkoraThemeManager.textPrimary(context)
    val textMuted = WorkoraThemeManager.textSecondary(context)
    val borderCol = WorkoraThemeManager.borderColor(context)

    val savedEncryptedPinHash = remember {
        WorkoraSecurityManager.readEncryptedSecret(context, "super_admin_master_pin_hash", "")
    }
    var isFirstTimePinSetup by remember { mutableStateOf(savedEncryptedPinHash.isBlank()) }

    // Step 1 = PIN Verification / Setup | Step 2 = Real 6-Digit Admin OTP Verification
    var gateStep by remember { mutableIntStateOf(1) }

    var pinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }

    var otpInput by remember { mutableStateOf("") }
    var maskedTarget by remember { mutableStateOf("+91 ******8340 & an***@gmail.com") }
    var secondsLeft by remember { mutableIntStateOf(60) }
    var isSendingOtp by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gateStep, secondsLeft) {
        if (gateStep == 2 && secondsLeft > 0) {
            delay(1000L)
            secondsLeft -= 1
        }
    }

    fun dispatchAdminOtp() {
        isSendingOtp = true
        WorkoraRealOtpEngine.sendRealOtp(
            context = context,
            phoneOrEmail = "6265798340",
            emailOptional = "ankitah994@gmail.com",
            purpose = "SUPER ADMIN PANEL ACCESS"
        ) { masked ->
            isSendingOtp = false
            maskedTarget = masked
            gateStep = 2
            secondsLeft = 60
            Toast.makeText(
                context,
                "Admin Security OTP sent to $masked ✓",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardBg,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = brandOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (gateStep == 1) "Super Admin Security Gate" else "Verify Admin Real OTP",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = deepNavy
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = textDark)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (gateStep == 1) {
                    if (isFirstTimePinSetup) {
                        Text(
                            text = "Pehli baar Admin Panel surakshit karne ke liye apna 6-Digit Secret Admin PIN banayein:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textDark,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                pinInput = it.filter { c -> c.isDigit() }.take(6)
                                errorMessage = null
                            },
                            textStyle = TextStyle(color = textDark, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            label = { Text("Create 6-Digit Admin PIN") },
                            placeholder = { Text("e.g. 789456") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { pinVisible = !pinVisible }) {
                                    Icon(
                                        imageVector = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = brandOrange
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = {
                                confirmPinInput = it.filter { c -> c.isDigit() }.take(6)
                                errorMessage = null
                            },
                            textStyle = TextStyle(color = textDark, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                            label = { Text("Confirm 6-Digit Admin PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Text(
                            text = "Admin Panel kholne ke liye apna 6-Digit Secret Admin Master PIN dalein:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textDark,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                pinInput = it.filter { c -> c.isDigit() }.take(6)
                                errorMessage = null
                            },
                            label = { Text("Enter 6-Digit Admin PIN") },
                            placeholder = { Text("● ● ● ● ● ●") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { pinVisible = !pinVisible }) {
                                    Icon(
                                        imageVector = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = brandOrange
                                    )
                                }
                            },
                            textStyle = TextStyle(
                                color = deepNavy,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                letterSpacing = 5.sp
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = brandOrange,
                                unfocusedBorderColor = borderCol
                            )
                        )
                    }
                } else {
                    Text(
                        text = "Step 2/2: Admin Security 6-Digit Real OTP bheja gaya hai:\n$maskedTarget",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = textDark,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            otpInput = it.filter { c -> c.isDigit() }.take(6)
                            errorMessage = null
                        },
                        label = { Text("Enter 6-Digit Admin OTP") },
                        placeholder = { Text("● ● ● ● ● ●") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        textStyle = TextStyle(
                            color = deepNavy,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 6.sp
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandOrange,
                            unfocusedBorderColor = borderCol
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (secondsLeft > 0) "Resend in ${secondsLeft}s" else "OTP expired?",
                            fontSize = 11.sp,
                            color = textMuted
                        )
                        OutlinedButton(
                            onClick = {
                                if (secondsLeft == 0) {
                                    otpInput = ""
                                    errorMessage = null
                                    dispatchAdminOtp()
                                }
                            },
                            enabled = secondsLeft == 0,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resend OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB42318),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val (allowed, lockMsg) = WorkoraSecurityManager.checkLoginBruteForceAllowed(
                        context,
                        "super_admin_panel_gate"
                    )
                    if (!allowed) {
                        errorMessage = lockMsg
                        return@Button
                    }

                    if (gateStep == 1) {
                        if (isFirstTimePinSetup) {
                            if (pinInput.length != 6 || pinInput != confirmPinInput) {
                                errorMessage = "Dono box mein barabar 6-digit PIN dalein!"
                                return@Button
                            }
                            val saltedPinHash = WorkoraSecurityManager.hashPasswordSecure("AdminPin#$pinInput")
                            WorkoraSecurityManager.saveEncryptedSecret(
                                context,
                                "super_admin_master_pin_hash",
                                saltedPinHash
                            )
                            isFirstTimePinSetup = false
                            dispatchAdminOtp()
                        } else {
                            if (pinInput.length != 6) {
                                errorMessage = "Kripya 6-digit ka Admin PIN dalein!"
                                return@Button
                            }
                            val currentHash = WorkoraSecurityManager.readEncryptedSecret(
                                context,
                                "super_admin_master_pin_hash",
                                ""
                            )
                            val isPinValid = WorkoraSecurityManager.verifyPasswordSecure(
                                "AdminPin#$pinInput",
                                currentHash
                            )
                            if (!isPinValid) {
                                WorkoraSecurityManager.recordLoginAttempt(
                                    context,
                                    "super_admin_panel_gate",
                                    isSuccess = false
                                )
                                WorkoraSecurityManager.recordAdminAuditLog(
                                    context,
                                    "ankitah994@gmail.com",
                                    "FAILED_ADMIN_PIN_ATTEMPT",
                                    "AdminPanelGate"
                                )
                                errorMessage = "Galat Admin PIN! Unauthorized access blocked."
                                return@Button
                            }

                            dispatchAdminOtp()
                        }
                    } else {
                        val (otpOk, otpMsg) = WorkoraRealOtpEngine.verifyRealOtp(
                            context = context,
                            phoneOrEmail = "6265798340",
                            emailOptional = "ankitah994@gmail.com",
                            enteredOtp = otpInput
                        )
                        if (otpOk) {
                            WorkoraSecurityManager.recordLoginAttempt(
                                context,
                                "super_admin_panel_gate",
                                isSuccess = true
                            )
                            WorkoraSecurityManager.recordAdminAuditLog(
                                context,
                                "ankitah994@gmail.com",
                                "SUPER_ADMIN_PANEL_UNLOCKED",
                                "AdminDashboardScreen"
                            )
                            Toast.makeText(
                                context,
                                "Super Admin Security Verified ✓",
                                Toast.LENGTH_SHORT
                            ).show()
                            onAdminVerifiedSuccess()
                        } else {
                            WorkoraSecurityManager.recordLoginAttempt(
                                context,
                                "super_admin_panel_gate",
                                isSuccess = false
                            )
                            errorMessage = otpMsg
                        }
                    }
                },
                enabled = !isSendingOtp,
                colors = ButtonDefaults.buttonColors(containerColor = brandOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                if (isSendingOtp) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (gateStep == 1) Icons.Default.VerifiedUser else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (gateStep == 1) {
                            if (isFirstTimePinSetup) "Save Admin PIN & Send OTP →" else "Verify PIN & Send Admin OTP →"
                        } else {
                            "Verify Admin OTP & Unlock Panel ✓"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    )
}

@Composable
fun AccountSelectScreen(
    onSelectRole: (UserRole) -> Unit = {},
    toastMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    // Sync Global Theme Mode (Default = "System")
    LaunchedEffect(Unit) {
        WorkoraThemeManager.syncFromPrefs(context)
    }
    val screenBg = WorkoraThemeManager.bgColor(context)
    val cardBg = WorkoraThemeManager.surfaceColor(context)
    val deepNavy = Color(0xFF072A5E)
    val brandBlue = WorkoraThemeManager.accentBlue(context)
    val brandOrange = Color(0xFFFF8C00)
    val textDark = WorkoraThemeManager.textPrimary(context)
    val textMuted = WorkoraThemeManager.textSecondary(context)
    val cardBorder = WorkoraThemeManager.borderColor(context)

    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var showLanguageStep by remember { mutableStateOf(false) }
    var clickedLanguage by remember { mutableStateOf<String?>(null) }

    var showAdminSecurityGate by remember { mutableStateOf(false) }
    var isAdminDashboardOpen by remember { mutableStateOf(false) }

    val loggedEmail = remember {
        (authPrefs.getString("last_logged_in_email", "") ?: "").trim().lowercase()
    }
    val savedPhone = remember {
        (profilePrefs.getString("user_phone", "") ?: "").filter { it.isDigit() }.takeLast(10)
    }
    val isVerifiedAdmin = remember(loggedEmail, savedPhone) {
        loggedEmail == "ankitah994@gmail.com" ||
                loggedEmail.contains("ankitah994") ||
                savedPhone == "6265798340" ||
                authPrefs.getString("saved_user_role", "") == "ADMIN"
    }

    LaunchedEffect(selectedRole) {
        if (selectedRole != null && !showLanguageStep) {
            delay(220)
            showLanguageStep = true
        }
    }

    LaunchedEffect(clickedLanguage) {
        val lang = clickedLanguage
        val role = selectedRole
        if (lang != null && role != null) {
            settingsPrefs.edit()
                .putString("app_language", lang)
                .putBoolean("language_selected_once", true)
                .apply()
            authPrefs.edit()
                .putString("saved_user_role", role.name)
                .apply()
            delay(220)
            Toast.makeText(
                context,
                if (lang == "Hindi") "भाषा हिन्दी चुनी गई ✓" else "Language set to English ✓",
                Toast.LENGTH_SHORT
            ).show()
            onSelectRole(role)
        }
    }

    if (isAdminDashboardOpen) {
        AdminDashboardScreen(
            adminEmail = loggedEmail.ifBlank { "ankitah994@gmail.com" },
            adminTier = "SUPER_ADMIN",
            onLogoutAdmin = {
                isAdminDashboardOpen = false
            },
            onSwitchRoleFromAdmin = { targetRole ->
                isAdminDashboardOpen = false
                if (targetRole == "LABOUR") {
                    onSelectRole(UserRole.LABOUR)
                } else {
                    onSelectRole(UserRole.CUSTOMER)
                }
            },
            onBack = { isAdminDashboardOpen = false }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height

            val orangeWave = Path().apply {
                moveTo(w * 0.42f, h * 0.52f)
                quadraticBezierTo(w * 0.76f, h * 0.05f, w, h * 0.22f)
                lineTo(w, h)
                lineTo(w * 0.42f, h)
                close()
            }
            drawPath(path = orangeWave, color = brandOrange)

            val navyWave = Path().apply {
                moveTo(0f, h * 0.28f)
                quadraticBezierTo(w * 0.22f, h * 0.08f, w * 0.48f, h * 0.38f)
                quadraticBezierTo(w * 0.75f, h * 0.68f, w, h * 0.34f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = navyWave, color = Color(0xFF083D91))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showLanguageStep) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            showLanguageStep = false
                            selectedRole = null
                            clickedLanguage = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = brandBlue
                        )
                    }
                    Text(
                        text = "Back",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = brandBlue
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(36.dp))
            }

            WorkoraHelmetLogo(size = 76.dp, showHalo = false)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Workora",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = brandBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Find. Hire. Work.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textDark
            )

            Spacer(modifier = Modifier.height(36.dp))

            if (!showLanguageStep) {
                Text(
                    text = "What do you want to do?",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                val isHireSelected = selectedRole == UserRole.CUSTOMER
                Card(
                    onClick = { selectedRole = UserRole.CUSTOMER },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHireSelected) deepNavy else cardBg
                    ),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (isHireSelected) deepNavy else cardBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(brandOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "I want to Hire",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isHireSelected) Color.White else textDark
                                )
                                Text(
                                    text = "Find skilled workers for your work",
                                    fontSize = 14.sp,
                                    color = if (isHireSelected) Color.White.copy(alpha = 0.9f) else textMuted,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isHireSelected) Color.White else textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                val isWorkSelected = selectedRole == UserRole.LABOUR
                Card(
                    onClick = { selectedRole = UserRole.LABOUR },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWorkSelected) deepNavy else cardBg
                    ),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (isWorkSelected) deepNavy else cardBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(brandOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "I want to Work",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isWorkSelected) Color.White else textDark
                                )
                                Text(
                                    text = "Find jobs and earn money",
                                    fontSize = 14.sp,
                                    color = if (isWorkSelected) Color.White.copy(alpha = 0.9f) else textMuted,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isWorkSelected) Color.White else textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                if (isVerifiedAdmin) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showAdminSecurityGate = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = deepNavy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = brandOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔐 Open Workora Super Admin Panel (PIN + OTP)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            } else {
                Text(
                    text = "Choose Your Language\nअपनी भाषा चुनें",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                val isHindiSelected = clickedLanguage == "Hindi"
                Card(
                    onClick = { clickedLanguage = "Hindi" },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHindiSelected) deepNavy else cardBg
                    ),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (isHindiSelected) deepNavy else cardBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(brandOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "अ",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "हिन्दी (Hindi)",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isHindiSelected) Color.White else textDark
                                )
                                Text(
                                    text = "ऐप को हिन्दी भाषा में चलाएं",
                                    fontSize = 13.sp,
                                    color = if (isHindiSelected) Color.White.copy(alpha = 0.9f) else textMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isHindiSelected) Icons.Default.CheckCircle else Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isHindiSelected) Color.White else textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isEnglishSelected = clickedLanguage == "English"
                Card(
                    onClick = { clickedLanguage = "English" },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnglishSelected) deepNavy else cardBg
                    ),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (isEnglishSelected) deepNavy else cardBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(brandOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "English",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isEnglishSelected) Color.White else textDark
                                )
                                Text(
                                    text = "Continue in English language",
                                    fontSize = 13.sp,
                                    color = if (isEnglishSelected) Color.White.copy(alpha = 0.9f) else textMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isEnglishSelected) Icons.Default.CheckCircle else Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isEnglishSelected) Color.White else textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(160.dp))
        }
    }

    if (showAdminSecurityGate) {
        WorkoraAdminSecurityGateDialog(
            onDismiss = { showAdminSecurityGate = false },
            onAdminVerifiedSuccess = {
                showAdminSecurityGate = false
                isAdminDashboardOpen = true
            }
        )
    }
}
