package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UserAccount
import com.example.model.UserRole
import com.example.ui.components.WorkoraToast
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraSuccess
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted
import com.example.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    role: UserRole,
    onBack: () -> Unit,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    toastMessage: String?,
    modifier: Modifier = Modifier,
    currentUser: UserAccount? = null,
    profileViewModel: ProfileViewModel = viewModel(),
    onProfileSaved: ((String) -> Unit)? = null
) {
    val userProfile by profileViewModel.userProfile.collectAsStateWithLifecycle()
    val vmCurrentUser by profileViewModel.currentUser.collectAsStateWithLifecycle()
    val vmToast by profileViewModel.toastMessage.collectAsStateWithLifecycle()
    val activeUser = currentUser ?: vmCurrentUser

    // Initial values
    val initialName = activeUser?.fullName
        ?: if (role == UserRole.CUSTOMER) "Ramesh Verma" else (userProfile?.name ?: "Sunil Kumar")
    val initialPhone = activeUser?.mobileNumber
        ?: if (role == UserRole.CUSTOMER) "+91 98765 43210" else (userProfile?.phone ?: "+91 98123 45678")
    val initialEmail = activeUser?.email
        ?: if (role == UserRole.CUSTOMER) "ramesh.verma@workora.com" else "sunil.mason@workora.com"
    val initialArea = activeUser?.location
        ?: if (role == UserRole.CUSTOMER) "Sector 14, Gurugram" else (userProfile?.location ?: "Delhi Chowk, Delhi")
    val initialSkills = userProfile?.trade ?: "Mason"
    val initialWage = (userProfile?.dailyWage ?: 850).toString()
    val initialAvailability = userProfile?.isAvailableToday ?: true

    var nameInput by remember(initialName) { mutableStateOf(initialName) }
    var phoneInput by remember(initialPhone) { mutableStateOf(initialPhone) }
    var emailInput by remember(initialEmail) { mutableStateOf(initialEmail) }
    var areaInput by remember(initialArea) { mutableStateOf(initialArea) }
    var skillsInput by remember(initialSkills) { mutableStateOf(initialSkills) }
    var wageInput by remember(initialWage) { mutableStateOf(initialWage) }
    var isAvailableInput by remember(initialAvailability) { mutableStateOf(initialAvailability) }

    val roleTitle = if (role == UserRole.CUSTOMER) "Customer (Hirer)" else "Labour (Worker)"
    val roleColor = if (role == UserRole.CUSTOMER) WorkoraOrange else WorkoraNavy

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
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_profile_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WorkoraNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "My Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkoraNavy
                    )
                }

                Box(
                    modifier = Modifier
                        .background(roleColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = roleTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = roleColor
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(roleColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = nameInput.trim().take(1).ifBlank { "W" }.uppercase(),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = nameInput.ifBlank { "My Profile" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraTextDark
                )

                Text(
                    text = roleTitle,
                    fontSize = 13.sp,
                    color = WorkoraTextMuted,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Profile Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Account Details",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraTextDark
                        )

                        // Full Name
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Full Name") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = WorkoraNavy
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_profile_name"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = roleColor,
                                focusedLabelColor = roleColor
                            )
                        )

                        // Phone Number
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Phone Number") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = WorkoraNavy
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_profile_phone"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = roleColor,
                                focusedLabelColor = roleColor
                            )
                        )

                        // Email Address
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = WorkoraNavy
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_profile_email"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = roleColor,
                                focusedLabelColor = roleColor
                            )
                        )

                        // Area / City
                        OutlinedTextField(
                            value = areaInput,
                            onValueChange = { areaInput = it },
                            label = { Text("Area / City") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = WorkoraNavy
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_profile_area"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = roleColor,
                                focusedLabelColor = roleColor
                            )
                        )

                        // Worker specific fields: skills/trade, wage, availability
                        if (role == UserRole.LABOUR) {
                            OutlinedTextField(
                                value = skillsInput,
                                onValueChange = { skillsInput = it },
                                label = { Text("Skills / Trade") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Handyman,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_profile_skills"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = roleColor,
                                    focusedLabelColor = roleColor
                                )
                            )

                            OutlinedTextField(
                                value = wageInput,
                                onValueChange = { wageInput = it },
                                label = { Text("Daily Wage (₹/day)") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = WorkoraNavy
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_profile_wage"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = roleColor,
                                    focusedLabelColor = roleColor
                                )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isAvailableInput) WorkoraSuccess.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(
                                                if (isAvailableInput) WorkoraSuccess else WorkoraTextMuted,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isAvailableInput) "Available for Work Today" else "Not Available",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isAvailableInput) WorkoraNavy else WorkoraTextMuted
                                    )
                                }
                                Switch(
                                    checked = isAvailableInput,
                                    onCheckedChange = { isAvailableInput = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = WorkoraOrange,
                                        uncheckedThumbColor = WorkoraTextMuted,
                                        uncheckedTrackColor = Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.testTag("switch_profile_availability")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Save Profile / Update Button
                        Button(
                            onClick = {
                                val wageInt = wageInput.filter { it.isDigit() }.toIntOrNull() ?: 850
                                if (role == UserRole.CUSTOMER) {
                                    profileViewModel.updateCustomerProfile(
                                        name = nameInput,
                                        phone = phoneInput,
                                        area = areaInput
                                    ) { success ->
                                        if (success) {
                                            onProfileSaved?.invoke("Profile updated successfully!")
                                        }
                                    }
                                } else {
                                    profileViewModel.updateWorkerProfile(
                                        name = nameInput,
                                        phone = phoneInput,
                                        area = areaInput,
                                        skills = skillsInput,
                                        wage = wageInt,
                                        availability = isAvailableInput
                                    ) { success ->
                                        if (success) {
                                            onProfileSaved?.invoke("Profile updated successfully!")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_profile"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = roleColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Save Profile",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Switch Role Button
                OutlinedButton(
                    onClick = onSwitchRole,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_profile_switch_role"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, WorkoraBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = WorkoraNavy,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (role == UserRole.CUSTOMER) "Switch to Labour (Work) Mode" else "Switch to Customer (Hire) Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_profile_logout"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFEE2E2),
                        contentColor = Color(0xFFDC2626)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Floating Toast
        WorkoraToast(
            message = toastMessage ?: vmToast,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        )
    }
}
