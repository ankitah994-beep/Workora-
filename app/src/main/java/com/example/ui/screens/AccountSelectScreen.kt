package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserRole

// --- 1. आपका पुराना एडमिन गेट डायलॉग (सुरक्षित) ---
@Composable
fun WorkoraAdminSecurityGateDialog(
    onDismiss: () -> Unit,
    onAdminVerifiedSuccess: () -> Unit
) {
    val context = LocalContext.current
    val cardColor = Color.White
    val textDark = Color.Black
    val textMuted = Color.Gray
    var step by remember { mutableIntStateOf(1) }
    var pinInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val targetKey = "admin_security_gate"
    val savedPinHash = remember { WorkoraSecurityManager.readEncryptedSecret(context, "admin_master_pin_hash", "") }
    val isFirstTimePinSetup = savedPinHash.isBlank()

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
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFFF8C00))
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
                    Text(text = errorMsg!!, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB42318))
                }
                if (step == 1) {
                    Text(text = if (isFirstTimePinSetup) "Set your 6-digit Admin PIN:" else "Enter your 6-digit Admin PIN:", fontSize = 13.sp, color = textMuted)
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6) pinInput = it.filter { c -> c.isDigit() } },
                        label = { Text("6-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Text(text = "Enter the 6-digit OTP from notification to unlock Admin Panel:", fontSize = 13.sp, color = textMuted)
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 6) otpInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Enter 6-Digit OTP") },
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
                            WorkoraRealOtpEngine.sendRealOtp(context, targetKey, "ADMIN_UNLOCK") {
                                step = 2
                                errorMsg = null
                            }
                        } else {
                            if (WorkoraSecurityManager.verifyPasswordSecure(pinInput, savedPinHash)) {
                                WorkoraSecurityManager.recordLoginAttempt(context, "admin_gate", true)
                                WorkoraRealOtpEngine.sendRealOtp(context, targetKey, "ADMIN_UNLOCK") {
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = if (step == 1) "Continue" else "Unlock Admin Panel", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel")
            }
        }
    )
}

// --- 2. Role Selection & Admin Navigation Screen ---
@Composable
fun AccountSelectScreen(
    onSelectRole: (UserRole) -> Unit = {},
    onRoleSelected: (UserRole) -> Unit = onSelectRole,
    toastMessage: String? = null,
    onOpenAdmin: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    
    var showAdminGateDialog by remember { mutableStateOf(false) }
    var isAdminPanelOpen by remember { mutableStateOf(false) }

    if (isAdminPanelOpen) {
        // CLEAN ADMIN DASHBOARD CALL (MATCHING MASTERPLAN)
        AdminDashboardScreen(
            onNavigateToUsers = {},
            onNavigateToJobs = {},
            onNavigateToReports = {},
            onNavigateToAppControl = {},
            onNavigateToStateControl = {}
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF0061FF),
                modifier = Modifier
                    .size(28.dp)
                    .clickable { activity?.finish() }
            )
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Admin",
                tint = Color(0xFFE2E8F0),
                modifier = Modifier.clickable { showAdminGateDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Choose Your Role",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "What do you want to do?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF64748B)
        )
        
        Spacer(modifier = Modifier.height(36.dp))

        // Worker Role Card
        Card(
            onClick = {
                authPrefs.edit().putString("saved_user_role", UserRole.LABOUR.name).apply()
                onSelectRole(UserRole.LABOUR)
                onRoleSelected(UserRole.LABOUR)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_role_worker),
                    contentDescription = "Worker",
                    modifier = Modifier.size(70.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "I want to work",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Find local jobs and earn",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF0061FF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Client Role Card
        Card(
            onClick = {
                authPrefs.edit().putString("saved_user_role", UserRole.CUSTOMER.name).apply()
                onSelectRole(UserRole.CUSTOMER)
                onRoleSelected(UserRole.CUSTOMER)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_role_client),
                    contentDescription = "Client",
                    modifier = Modifier.size(70.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "I want to hire",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Post work and find workers",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF0061FF),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_city_bottom),
                contentDescription = "City Area",
                modifier = Modifier.height(60.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Join thousands of people\nin your local area",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAdminGateDialog) {
        WorkoraAdminSecurityGateDialog(
            onDismiss = { showAdminGateDialog = false },
            onAdminVerifiedSuccess = {
                showAdminGateDialog = false
                authPrefs.edit().putString("saved_user_role", "ADMIN").apply()
                isAdminPanelOpen = true
                onOpenAdmin()
                Toast.makeText(context, "Admin Panel Unlocked ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
