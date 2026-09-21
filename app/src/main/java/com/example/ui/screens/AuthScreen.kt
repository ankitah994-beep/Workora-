package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuthMode
import com.example.model.UserRole
import com.example.ui.components.WorkoraHelmetLogo
import com.example.ui.components.WorkoraToast
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraNavyDark
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraOrangeDark
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted

@Composable
fun AuthScreen(
    authMode: AuthMode,
    selectedRole: UserRole,
    onAuthModeChanged: (AuthMode) -> Unit,
    onRoleChanged: (UserRole) -> Unit,
    onLogin: (email: String, pass: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onRegister: (
        fullName: String,
        mobileNumber: String,
        email: String,
        pass: String,
        confirmPass: String,
        location: String,
        role: UserRole,
        onResult: (Boolean, String) -> Unit
    ) -> Unit,
    onForgotPassword: (email: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onBackToRoleSelection: () -> Unit,
    toastMessage: String?,
    modifier: Modifier = Modifier
) {
    // Login form states
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    // Register form states
    var regFullName by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regLocation by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }
    var showRegConfirmPassword by remember { mutableStateOf(false) }

    // Validation error state
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top Navigation Bar
            AuthTopBar(
                selectedRole = selectedRole,
                onBack = onBackToRoleSelection
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    WorkoraHelmetLogo(size = 46.dp, showHalo = false)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "WORKORA",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (selectedRole == UserRole.CUSTOMER) "Customer Portal • Hire Workers" else "Labour Portal • Find Work",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mode Tabs: Login vs Register
                TabRow(
                    selectedTabIndex = if (authMode == AuthMode.LOGIN) 0 else 1,
                    containerColor = Color.White,
                    contentColor = WorkoraNavy,
                    indicator = { tabPositions ->
                        val index = if (authMode == AuthMode.LOGIN) 0 else 1
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = WorkoraOrange,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(16.dp))
                ) {
                    Tab(
                        selected = authMode == AuthMode.LOGIN,
                        onClick = {
                            errorMessage = null
                            onAuthModeChanged(AuthMode.LOGIN)
                        },
                        text = {
                            Text(
                                text = "Log In",
                                fontWeight = if (authMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    )
                    Tab(
                        selected = authMode == AuthMode.REGISTER,
                        onClick = {
                            errorMessage = null
                            onAuthModeChanged(AuthMode.REGISTER)
                        },
                        text = {
                            Text(
                                text = "Create Account",
                                fontWeight = if (authMode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error Banner
                AnimatedVisibility(
                    visible = !errorMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                            border = BorderStroke(1.dp, Color(0xFFF87171))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = Color(0xFF991B1B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (authMode == AuthMode.LOGIN) {
                    // ==========================================
                    // 1. LOGIN FORM
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, WorkoraBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Welcome Back",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraTextDark
                            )
                            Text(
                                text = "Sign in to access your ${if (selectedRole == UserRole.CUSTOMER) "hirer" else "worker"} dashboard",
                                fontSize = 13.sp,
                                color = WorkoraTextMuted
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Email
                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = {
                                    loginEmail = it
                                    errorMessage = null
                                },
                                label = { Text("Email Address") },
                                placeholder = { Text("e.g. name@example.com") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_login_email"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WorkoraNavy,
                                    focusedLabelColor = WorkoraNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Password with Show/Hide
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = {
                                    loginPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Password") },
                                placeholder = { Text("Enter your password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showLoginPassword = !showLoginPassword },
                                        modifier = Modifier.testTag("btn_toggle_login_password")
                                    ) {
                                        Icon(
                                            imageVector = if (showLoginPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (showLoginPassword) "Hide password" else "Show password",
                                            tint = WorkoraTextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        onLogin(loginEmail, loginPassword) { success, msg ->
                                            if (!success) errorMessage = msg
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_login_password"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WorkoraNavy,
                                    focusedLabelColor = WorkoraNavy
                                )
                            )

                            // Forgot Password Button
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                TextButton(
                                    onClick = {
                                        forgotEmail = loginEmail
                                        showForgotDialog = true
                                    },
                                    modifier = Modifier.testTag("btn_forgot_password")
                                ) {
                                    Text(
                                        text = "Forgot Password?",
                                        color = WorkoraOrange,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Login Button
                            Button(
                                onClick = {
                                    onLogin(loginEmail, loginPassword) { success, msg ->
                                        if (!success) errorMessage = msg
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("btn_login_submit"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WorkoraOrange,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    text = "Log In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Register Link
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Don't have an account?",
                                    fontSize = 14.sp,
                                    color = WorkoraTextMuted
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Register",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkoraNavy,
                                    modifier = Modifier
                                        .testTag("link_switch_to_register")
                                        .clickable {
                                            errorMessage = null
                                            onAuthModeChanged(AuthMode.REGISTER)
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Demo Fill Helper Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                        border = BorderStroke(1.dp, WorkoraBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Quick Demo Credentials:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraTextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        loginEmail = "customer@workora.com"
                                        loginPassword = "password123"
                                        onRoleChanged(UserRole.CUSTOMER)
                                        errorMessage = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, WorkoraBorder)
                                ) {
                                    Text("Hirer Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        loginEmail = "worker@workora.com"
                                        loginPassword = "password123"
                                        onRoleChanged(UserRole.LABOUR)
                                        errorMessage = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, WorkoraBorder)
                                ) {
                                    Text("Worker Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // 2. REGISTER FORM
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, WorkoraBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Create Your Account",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraTextDark
                            )
                            Text(
                                text = "Join Workora to connect, hire, and find daily jobs",
                                fontSize = 13.sp,
                                color = WorkoraTextMuted
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Selected Role Picker
                            Text(
                                text = "Registering As:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WorkoraTextDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val isCustomer = selectedRole == UserRole.CUSTOMER
                                FilterChip(
                                    selected = isCustomer,
                                    onClick = { onRoleChanged(UserRole.CUSTOMER) },
                                    label = { Text("I want to Hire (Hirer)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PersonSearch,
                                            contentDescription = null,
                                            tint = if (isCustomer) Color.White else WorkoraNavy
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WorkoraNavy,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White,
                                        labelColor = WorkoraNavy
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chip_role_customer")
                                )

                                val isLabour = selectedRole == UserRole.LABOUR
                                FilterChip(
                                    selected = isLabour,
                                    onClick = { onRoleChanged(UserRole.LABOUR) },
                                    label = { Text("I want to Work (Worker)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Handyman,
                                            contentDescription = null,
                                            tint = if (isLabour) Color.White else WorkoraOrange
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = WorkoraOrange,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White,
                                        labelColor = WorkoraOrangeDark
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chip_role_labour")
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Full Name
                            OutlinedTextField(
                                value = regFullName,
                                onValueChange = {
                                    regFullName = it
                                    errorMessage = null
                                },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g. Ramesh Verma") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WorkoraNavy,
                                    focusedLabelColor = WorkoraNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Mobile Number
                            OutlinedTextField(
                                value = regMobile,
                                onValueChange = {
                                    regMobile = it
                                    errorMessage = null
                                },
                                label = { Text("Mobile Number") },
                                placeholder = { Text("e.g. 9876543210") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_phone"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WorkoraNavy,
                                    focusedLabelColor = WorkoraNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Email
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = {
                                    regEmail = it
                                    errorMessage = null
                                },
                                label = { Text("Email Address") },
                                placeholder = { Text("e.g. ramesh@example.com") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_email"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WorkoraNavy,
                                    focusedLabelColor = WorkoraNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = {
                                    regPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Password (min 6 chars)") },
                                placeholder = { Text("Create password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showRegPassword = !showRegPassword },
                                        modifier = Modifier.testTag("btn_toggle_reg_password")
                                    ) {
                                        Icon(
                                            imageVector = if (showRegPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (showRegPassword) "Hide password" else "Show password",
                                            tint = WorkoraTextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (showRegPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_password"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WorkoraNavy,
                                    focusedLabelColor = WorkoraNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Confirm Password
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = {
                                    regConfirmPassword = it
                                    errorMessage = null
                                },
                                label = { Text("Confirm Password") },
                                placeholder = { Text("Re-enter password") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showRegConfirmPassword = !showRegConfirmPassword },
                                        modifier = Modifier.testTag("btn_toggle_reg_confirm_password")
                                    ) {
                                        Icon(
                                            imageVector = if (showRegConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (showRegConfirmPassword) "Hide password" else "Show password",
                                            tint = WorkoraTextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (showRegConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_confirm_password"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WorkoraNavy,
                                    focusedLabelColor = WorkoraNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Area / Village / City
                            OutlinedTextField(
                                value = regLocation,
                                onValueChange = {
                                    regLocation = it
                                    errorMessage = null
                                },
                                label = { Text("Area / Village / City") },
                                placeholder = { Text("e.g. Sector 14, Gurugram") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationCity,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        onRegister(
                                            regFullName,
                                            regMobile,
                                            regEmail,
                                            regPassword,
                                            regConfirmPassword,
                                            regLocation,
                                            selectedRole
                                        ) { success, msg ->
                                            if (!success) errorMessage = msg
                                        }
                                    }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_register_location"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WorkoraNavy,
                                    focusedLabelColor = WorkoraNavy
                                )
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Create Account Button
                            Button(
                                onClick = {
                                    onRegister(
                                        regFullName,
                                        regMobile,
                                        regEmail,
                                        regPassword,
                                        regConfirmPassword,
                                        regLocation,
                                        selectedRole
                                    ) { success, msg ->
                                        if (!success) errorMessage = msg
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("btn_register_submit"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WorkoraNavy,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    text = "Create Account",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Login Link
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Already have an account?",
                                    fontSize = 14.sp,
                                    color = WorkoraTextMuted
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Log In",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WorkoraOrange,
                                    modifier = Modifier
                                        .testTag("link_switch_to_login")
                                        .clickable {
                                            errorMessage = null
                                            onAuthModeChanged(AuthMode.LOGIN)
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Forgot Password Dialog
        if (showForgotDialog) {
            AlertDialog(
                onDismissRequest = { showForgotDialog = false },
                title = {
                    Text(
                        text = "Reset Password",
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavy
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Enter your registered email address. We'll send instructions to reset your password.",
                            fontSize = 13.sp,
                            color = WorkoraTextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = forgotEmail,
                            onValueChange = { forgotEmail = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_forgot_email")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onForgotPassword(forgotEmail) { success, msg ->
                                if (success) {
                                    showForgotDialog = false
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Send Link", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotDialog = false }) {
                        Text("Cancel", color = WorkoraTextMuted)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }

        // Floating Toast
        WorkoraToast(
            message = toastMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        )
    }
}

@Composable
private fun AuthTopBar(
    selectedRole: UserRole,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("btn_auth_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WorkoraNavy
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Authentication",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraNavy
            )
        }

        Box(
            modifier = Modifier
                .background(
                    if (selectedRole == UserRole.CUSTOMER) WorkoraNavy.copy(alpha = 0.12f) else WorkoraOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = if (selectedRole == UserRole.CUSTOMER) "Hirer Role" else "Worker Role",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedRole == UserRole.CUSTOMER) WorkoraNavy else WorkoraOrangeDark
            )
        }
    }
}
