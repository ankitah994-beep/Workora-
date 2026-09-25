package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.ui.components.WorkoraHelmetLogo
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted

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

    // 0 = Log In / Sign In, 1 = Mobile Number & Email Registration (Sign Up)
    var activeAuthTab by remember { mutableIntStateOf(0) }

    // Log In states
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    // Mobile Registration / Sign Up states
    var regFullName by remember { mutableStateOf("") }
    var regMobileNumber by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regVillageCity by remember { mutableStateOf("Silwani, Raisen") }
    var regPassword by remember { mutableStateOf("") }

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

            // Top Switch Tabs: "Log In / Sign In" vs "Mobile Registration"
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

            // ==================== TAB 0: LOG IN / SIGN IN (MOBILE OR EMAIL) ====================
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
                            text = "अपना मोबाइल नंबर या ईमेल और पासवर्ड डालकर लॉग इन करें",
                            fontSize = 12.sp,
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
                            label = { Text("Password (पासवर्ड)") },
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

                        Button(
                            onClick = {
                                val rawInput = emailOrPhone.trim().lowercase()
                                val cleanPass = password.trim()

                                if (rawInput.isBlank() || cleanPass.isBlank()) {
                                    Toast.makeText(context, "Kripya Mobile Number/Email aur Password dalein!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Support both Mobile Number and Email ID login seamlessly
                                val cleanIdentifier = if (!rawInput.contains("@") && rawInput.any { it.isDigit() }) {
                                    val digits = rawInput.filter { it.isDigit() }.takeLast(10)
                                    val mappedEmail = authPrefs.getString("phone_to_email_$digits", null)
                                    mappedEmail ?: "${digits}@workora.in"
                                } else {
                                    rawInput
                                }

                                isVerifying = true
                                FirebaseManager.verifyUserAndRoleFromFirebase(cleanIdentifier, cleanPass) { isSuccess, cloudName, cloudPhone, isAdmin, adminTier, errorMsg ->
                                    isVerifying = false
                                    if (isSuccess) {
                                        authPrefs.edit()
                                            .putBoolean("is_logged_in", true)
                                            .putString("last_logged_in_email", cleanIdentifier)
                                            .putString("user_pass_$cleanIdentifier", cleanPass)
                                            .apply()

                                        if (!cloudName.isNullOrBlank()) {
                                            profilePrefs.edit().putString("user_name", cloudName).apply()
                                        }
                                        if (!cloudPhone.isNullOrBlank()) {
                                            profilePrefs.edit().putString("user_phone", cloudPhone).apply()
                                        }

                                        if (isAdmin) {
                                            authPrefs.edit()
                                                .putString("saved_user_role", "ADMIN")
                                                .putString("saved_admin_tier", adminTier ?: "SUPER_ADMIN")
                                                .apply()
                                            Toast.makeText(context, "Welcome Workora Admin ($cloudName) ✓", Toast.LENGTH_SHORT).show()
                                        } else {
                                            authPrefs.edit().remove("saved_user_role").apply()
                                            Toast.makeText(context, "Welcome $cloudName! ✓", Toast.LENGTH_SHORT).show()
                                        }
                                        onLogin(cleanIdentifier, cleanPass)
                                    } else {
                                        val savedLocalPass = authPrefs.getString("user_pass_$cleanIdentifier", null)
                                        if (savedLocalPass != null && savedLocalPass == cleanPass) {
                                            authPrefs.edit()
                                                .putBoolean("is_logged_in", true)
                                                .putString("last_logged_in_email", cleanIdentifier)
                                                .apply()
                                            onLogin(cleanIdentifier, cleanPass)
                                        } else {
                                            Toast.makeText(
                                                context,
                                                errorMsg ?: "Account nahi mila! Naya account banane ke liye Mobile Registration tab dabayein.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
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
                                    text = "Log In / Sign In ✓",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // ==================== TAB 1: MOBILE NUMBER & EMAIL REGISTRATION ====================
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
                            text = "New Mobile Number Registration",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                        Text(
                            text = "नया अकाउंट बनाने के लिए अपना नाम और मोबाइल नंबर दर्ज करें",
                            fontSize = 12.sp,
                            color = WorkoraTextMuted
                        )

                        OutlinedTextField(
                            value = regFullName,
                            onValueChange = { regFullName = it },
                            label = { Text("Full Name (आपका पूरा नाम)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = regMobileNumber,
                            onValueChange = { regMobileNumber = it },
                            label = { Text("Mobile Number (10 अंकों का मोबाइल नंबर)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email ID (Optional / वैकल्पिक)") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = regVillageCity,
                            onValueChange = { regVillageCity = it },
                            label = { Text("Village / City (गाँव या शहर)") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Create Password (नया पासवर्ड बनाएं)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val cleanName = regFullName.trim()
                                val cleanPhone = regMobileNumber.trim()
                                val digits = cleanPhone.filter { it.isDigit() }.takeLast(10)
                                val cleanPass = regPassword.trim()

                                if (cleanName.isBlank() || digits.length < 10 || cleanPass.length < 4) {
                                    Toast.makeText(context, "Kripya Naam, 10 digit Mobile Number aur Password dalein!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val finalEmail = if (regEmail.trim().contains("@")) {
                                    regEmail.trim().lowercase()
                                } else {
                                    "${digits}@workora.in"
                                }

                                authPrefs.edit()
                                    .putBoolean("is_logged_in", true)
                                    .putString("last_logged_in_email", finalEmail)
                                    .putString("user_pass_$finalEmail", cleanPass)
                                    .putString("phone_to_email_$digits", finalEmail)
                                    .remove("saved_user_role")
                                    .apply()

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
                                    role = "CUSTOMER"
                                )

                                Toast.makeText(context, "Mobile Registration Successful! Welcome $cleanName ✓", Toast.LENGTH_LONG).show()
                                onLogin(finalEmail, cleanPass)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text(
                                text = "Register Mobile Number & Continue ✓",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
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
                    text = if (activeAuthTab == 0) "Mobile Registration karein" else "Log In karein",
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
}
