package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.UserRole
import com.example.ui.theme.MyApplicationTheme

/**
 * Workora Authentication Screen.
 * Exact parameter signature matching MainActivity.kt:
 * - selectedRole: UserRole
 * - onRoleChanged: (UserRole) -> Unit
 * - onBackToRoleSelection: () -> Unit
 * - onLoginSuccess: (User) -> Unit
 * - toastMessage: (String) -> Unit
 */
@Composable
fun AuthScreen(
    selectedRole: UserRole = UserRole.WORKER,
    onRoleChanged: (UserRole) -> Unit = {},
    onBackToRoleSelection: () -> Unit = {},
    onLoginSuccess: (User) -> Unit = {},
    toastMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Sign In, 1: Register
    val isSignUp = selectedTabIndex == 1

    // Form inputs state
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Mock OTP verification step
    var isOtpVerificationStep by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("123456") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // High contrast dark text (#0F172A) to guarantee text visibility on light background
    val inputTextColor = Color(0xFF0F172A)
    val inputTextStyle = TextStyle(
        color = inputTextColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal
    )

    val workoraTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = inputTextColor,
        unfocusedTextColor = inputTextColor,
        disabledTextColor = Color(0xFF64748B),
        errorTextColor = Color(0xFFDC2626),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color(0xFFF8FAFC),
        focusedBorderColor = Color(0xFF1D4ED8),
        unfocusedBorderColor = Color(0xFFCBD5E1),
        focusedLabelColor = Color(0xFF1D4ED8),
        unfocusedLabelColor = Color(0xFF475569),
        focusedPlaceholderColor = Color(0xFF94A3B8),
        unfocusedPlaceholderColor = Color(0xFF94A3B8),
        cursorColor = Color(0xFF1D4ED8),
        focusedLeadingIconColor = Color(0xFF1D4ED8),
        unfocusedLeadingIconColor = Color(0xFF64748B),
        focusedTrailingIconColor = Color(0xFF64748B),
        unfocusedTrailingIconColor = Color(0xFF94A3B8)
    )

    fun handleCompleteAuth(finalRole: UserRole) {
        val user = User(
            id = "usr_${System.currentTimeMillis()}",
            name = if (name.isNotBlank()) name.trim() else if (isSignUp) "New Worker" else "Demo User",
            email = if (email.isNotBlank()) email.trim() else "worker@workora.com",
            phoneNumber = if (phoneNumber.isNotBlank()) phoneNumber.trim() else "+1 555-0199",
            role = finalRole,
            profileImageUrl = "",
            createdAt = System.currentTimeMillis()
        )
        toastMessage("Authentication successful")
        onLoginSuccess(user)
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_screen"),
        color = Color(0xFFF1F5F9)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onBackToRoleSelection() },
                    modifier = Modifier.testTag("btn_back_to_role_selection")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF334155)
                    )
                }
            }

            // App Logo & Header
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Engineering,
                    contentDescription = "Workora Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Workora",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Workforce & Labor Management Platform",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isOtpVerificationStep) {
                        // Tabs: Sign In / Create Account
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Color(0xFF1D4ED8),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = selectedTabIndex == 0,
                                onClick = {
                                    selectedTabIndex = 0
                                    errorMessage = null
                                },
                                text = {
                                    Text(
                                        text = "Sign In",
                                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTabIndex == 0) Color(0xFF1D4ED8) else Color(0xFF64748B)
                                    )
                                },
                                modifier = Modifier.testTag("tab_sign_in")
                            )
                            Tab(
                                selected = selectedTabIndex == 1,
                                onClick = {
                                    selectedTabIndex = 1
                                    errorMessage = null
                                },
                                text = {
                                    Text(
                                        text = "Create Account",
                                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTabIndex == 1) Color(0xFF1D4ED8) else Color(0xFF64748B)
                                    )
                                },
                                modifier = Modifier.testTag("tab_register")
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Role Selection for registration
                        if (isSignUp) {
                            Text(
                                text = "Account Role:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF334155),
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedRole == UserRole.WORKER,
                                    onClick = { onRoleChanged(UserRole.WORKER) },
                                    label = { Text("Worker / Labor") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFDBEAFE),
                                        selectedLabelColor = Color(0xFF1D4ED8)
                                    ),
                                    modifier = Modifier.weight(1f).testTag("chip_worker")
                                )
                                FilterChip(
                                    selected = selectedRole == UserRole.EMPLOYER,
                                    onClick = { onRoleChanged(UserRole.EMPLOYER) },
                                    label = { Text("Employer") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFDBEAFE),
                                        selectedLabelColor = Color(0xFF1D4ED8)
                                    ),
                                    modifier = Modifier.weight(1f).testTag("chip_employer")
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Form Fields
                        AnimatedVisibility(visible = isSignUp) {
                            Column {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { text: String -> name = text },
                                    label = { Text("Name") },
                                    placeholder = { Text("e.g. Alex Johnson") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = "Name Icon")
                                    },
                                    singleLine = true,
                                    textStyle = inputTextStyle,
                                    colors = workoraTextFieldColors,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_name")
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }

                        // Mobile Number
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { text: String -> phoneNumber = text },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("+1 (555) 000-0000") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = "Mobile Icon")
                            },
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = workoraTextFieldColors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_mobile")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Email Address
                        OutlinedTextField(
                            value = email,
                            onValueChange = { text: String -> email = text },
                            label = { Text("Email Address") },
                            placeholder = { Text("alex@example.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email Icon")
                            },
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = workoraTextFieldColors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_email")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password
                        OutlinedTextField(
                            value = password,
                            onValueChange = { text: String -> password = text },
                            label = { Text("Password") },
                            placeholder = { Text("Enter your password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password Icon")
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible },
                                    modifier = Modifier.testTag("toggle_password_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            colors = workoraTextFieldColors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_password")
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFDC2626),
                                fontSize = 13.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Primary Action Button
                        Button(
                            onClick = {
                                errorMessage = null
                                if (isSignUp && name.isBlank()) {
                                    errorMessage = "Please enter your name"
                                    return@Button
                                }
                                if (phoneNumber.isBlank() && email.isBlank()) {
                                    errorMessage = "Please enter a mobile number or email"
                                    return@Button
                                }
                                if (password.isBlank()) {
                                    errorMessage = "Please enter a password"
                                    return@Button
                                }

                                isOtpVerificationStep = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_primary_auth"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1D4ED8),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = if (isSignUp) "Continue to Verification" else "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Mock Bypass Button
                        OutlinedButton(
                            onClick = {
                                handleCompleteAuth(selectedRole)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_quick_bypass"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF1D4ED8)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF16A34A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quick Demo Sign-In (Skip All)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // OTP Verification Screen
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security Shield",
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Verify Mobile Number",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Code sent to ${if (phoneNumber.isNotBlank()) phoneNumber else "+1 (555) 0199"}",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                        ) {
                            Text(
                                text = "Mock Mode Active: Real OTP skipped. You can enter any code or tap Skip OTP.",
                                fontSize = 12.sp,
                                color = Color(0xFF92400E),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = otpCode,
                            onValueChange = { text: String -> if (text.length <= 6) otpCode = text },
                            label = { Text("6-Digit OTP Code") },
                            placeholder = { Text("123456") },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = inputTextColor,
                                textAlign = TextAlign.Center,
                                letterSpacing = 4.sp,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            colors = workoraTextFieldColors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_otp")
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                handleCompleteAuth(selectedRole)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_verify_otp"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1D4ED8),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Verify & Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = {
                                handleCompleteAuth(selectedRole)
                            },
                            modifier = Modifier.testTag("btn_skip_otp")
                        ) {
                            Text(
                                text = "Skip OTP (Mock Repository)",
                                color = Color(0xFF475569),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(
                            onClick = { isOtpVerificationStep = false },
                            modifier = Modifier.testTag("btn_back_to_auth")
                        ) {
                            Text("← Back to edit credentials", color = Color(0xFF1D4ED8))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Workora Labor Network • Secure & Verified",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    MyApplicationTheme {
        AuthScreen(
            selectedRole = UserRole.WORKER,
            onRoleChanged = {},
            onBackToRoleSelection = {},
            onLoginSuccess = {},
            toastMessage = {}
        )
    }
}
