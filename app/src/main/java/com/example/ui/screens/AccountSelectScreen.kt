package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole

private val SelectNavy = Color(0xFF083D91)
private val SelectOrange = Color(0xFFFF8C00)
private val SelectRed = Color(0xFFB42318)

@Composable
fun WorkoraAdminSecurityGateDialog(
    onDismiss: () -> Unit,
    onAdminVerifiedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val cardColor = WorkoraThemeManager.surfaceColor(context)
    val textDark = WorkoraThemeManager.textPrimary(context)
    val textMuted = WorkoraThemeManager.textSecondary(context)

    var step by remember { mutableIntStateOf(1) }
    var pinInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val targetKey = "admin_security_gate"

    val savedPinHash = remember {
        WorkoraSecurityManager.readEncryptedSecret(context, "admin_master_pin_hash", "")
    }
    val isFirstTimePinSetup = savedPinHash.isBlank()

    val latestOtpHint = remember(step) {
        WorkoraRealOtpEngine.peekLatestOtpForHint(context, targetKey)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = SelectOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (step == 1) "Admin Security PIN" else "Admin OTP Verification",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textDark
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = textDark)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelectRed
                    )
                }

                if (step == 1) {
                    Text(
                        text = if (isFirstTimePinSetup) {
                            "Set your 6-digit Admin PIN:"
                        } else {
                            "Enter your 6-digit Admin PIN:"
                        },
                        fontSize = 13.sp,
                        color = textMuted
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6) pinInput = it.filter { c -> c.isDigit() } },
                        label = { Text("6-Digit PIN") },
                        placeholder = { Text("123456") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Text(
                        text = "Enter the 6-digit OTP to unlock Admin Panel:",
                        fontSize = 13.sp,
                        color = textMuted
                    )
                    if (latestOtpHint.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                            border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { otpInput = latestOtpHint }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "OTP: $latestOtpHint",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SelectNavy
                                )
                                Text(
                                    text = "Tap to Fill",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelectOrange
                                )
                            }
                        }
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
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val allowedCheck = WorkoraSecurityManager.checkLoginBruteForceAllowed(context, "admin_gate")
                    if (!allowedCheck.first) {
                        errorMsg = allowedCheck.second
                        return@Button
                    }

                    if (step == 1) {
                        if (pinInput.length != 6) {
                            errorMsg = "Please enter a 6-digit PIN"
                            return@Button
                        }
                        if (isFirstTimePinSetup) {
                            val newHash = WorkoraSecurityManager.hashPasswordSecure(pinInput)
                            WorkoraSecurityManager.saveEncryptedSecret(context, "admin_master_pin_hash", newHash)
                            WorkoraRealOtpEngine.sendRealOtp(
                                context = context,
                                phoneOrEmail = targetKey,
                                purpose = "ADMIN_UNLOCK"
                            ) {
                                step = 2
                                errorMsg = null
                            }
                        } else {
                            if (WorkoraSecurityManager.verifyPasswordSecure(pinInput, savedPinHash)) {
                                WorkoraSecurityManager.recordLoginAttempt(context, "admin_gate", true)
                                WorkoraRealOtpEngine.sendRealOtp(
                                    context = context,
                                    phoneOrEmail = targetKey,
                                    purpose = "ADMIN_UNLOCK"
                                ) {
                                    step = 2
                                    errorMsg = null
                                }
                            } else {
                                WorkoraSecurityManager.recordLoginAttempt(context, "admin_gate", false)
                                errorMsg = "Incorrect Admin PIN!"
                            }
                        }
                    } else {
                        val verifyResult = WorkoraRealOtpEngine.verifyRealOtp(context, targetKey, otpInput)
                        if (verifyResult.first) {
                            WorkoraSecurityManager.recordLoginAttempt(context, "admin_gate", true)
                            onAdminVerifiedSuccess()
                        } else {
                            WorkoraSecurityManager.recordLoginAttempt(context, "admin_gate", false)
                            errorMsg = verifyResult.second
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SelectOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (step == 1) "Continue" else "Unlock Admin Panel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
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
private fun AccountSelectLogo() {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = SelectOrange,
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
                color = SelectNavy,
                style = Stroke(width = w * 0.085f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun AccountSelectScreen(
    onSelectRole: (UserRole) -> Unit = {},
    onRoleSelected: (UserRole) -> Unit = onSelectRole,
    toastMessage: String? = null,
    onOpenAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }

    var showAdminGateDialog by remember { mutableStateOf(false) }
    var isAdminPanelOpen by remember { mutableStateOf(false) }

    val loggedEmail = remember {
        authPrefs.getString("last_logged_in_email", "example@gmail.com") ?: "example@gmail.com"
    }
    val isAdminAccount = remember {
        authPrefs.getString("saved_user_role", "") == "ADMIN"
    }

    if (isAdminPanelOpen) {
        AdminDashboardScreen(
            adminEmail = loggedEmail,
            adminTier = "SUPER_ADMIN",
            onLogoutAdmin = {
                isAdminPanelOpen = false
                onLogout()
            },
            onSwitchRoleFromAdmin = { roleStr ->
                isAdminPanelOpen = false
                if (roleStr == "CUSTOMER") {
                    onSelectRole(UserRole.CUSTOMER)
                    onRoleSelected(UserRole.CUSTOMER)
                } else {
                    onSelectRole(UserRole.LABOUR)
                    onRoleSelected(UserRole.LABOUR)
                }
            },
            onBack = { isAdminPanelOpen = false },
            onSwitchToCustomer = {
                isAdminPanelOpen = false
                onSelectRole(UserRole.CUSTOMER)
                onRoleSelected(UserRole.CUSTOMER)
            },
            onSwitchToLabour = {
                isAdminPanelOpen = false
                onSelectRole(UserRole.LABOUR)
                onRoleSelected(UserRole.LABOUR)
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AccountSelectLogo()

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "WORKORA",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SelectNavy,
            letterSpacing = 1.sp
        )
        Text(
            text = "Find. Hire. Work.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF667085)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Choose How You Want to Use Workora\n(अपना रोल चुनें)",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF102A43),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Customer Card (ग्राहक)
        Card(
            onClick = {
                authPrefs.edit().putString("saved_user_role", UserRole.CUSTOMER.name).apply()
                onSelectRole(UserRole.CUSTOMER)
                onRoleSelected(UserRole.CUSTOMER)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, SelectNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
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
                            .background(SelectNavy.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Customer",
                            tint = SelectNavy,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "I am a Customer (ग्राहक)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SelectNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find & hire trusted local workers or post a job.",
                            fontSize = 12.sp,
                            color = Color(0xFF475467)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = SelectNavy
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Worker / Labour Card (कारीगर / मज़दूर)
        Card(
            onClick = {
                authPrefs.edit().putString("saved_user_role", UserRole.LABOUR.name).apply()
                onSelectRole(UserRole.LABOUR)
                onRoleSelected(UserRole.LABOUR)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, SelectOrange),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
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
                            .background(SelectOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Worker",
                            tint = SelectOrange,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "I am a Worker (कारीगर / मज़दूर)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SelectOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find daily jobs near you & post work availability.",
                            fontSize = 12.sp,
                            color = Color(0xFF475467)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = SelectOrange
                )
            }
        }

        if (isAdminAccount) {
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = { showAdminGateDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, SelectNavy)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = SelectNavy, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Admin Panel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SelectNavy
                )
            }
        }
    }

    if (showAdminGateDialog) {
        WorkoraAdminSecurityGateDialog(
            onDismiss = { showAdminGateDialog = false },
            onAdminVerifiedSuccess = {
                showAdminGateDialog = false
                isAdminPanelOpen = true
                onOpenAdmin()
                Toast.makeText(context, "Admin Panel Unlocked ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
