package com.example.ui.screens

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

    var email by remember { mutableStateOf(authPrefs.getString("last_logged_in_email", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WorkoraHelmetLogo(size = 64.dp, showHalo = false)

            Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Login to Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraTextDark
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WorkoraOrange) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

                    Button(
                        onClick = {
                            val cleanEmail = email.trim().lowercase()
                            val cleanPass = password.trim()

                            if (cleanEmail.isBlank() || cleanPass.isBlank()) {
                                Toast.makeText(context, "Kripya Email aur Password dalein!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isVerifying = true
                            FirebaseManager.verifyUserAndRoleFromFirebase(cleanEmail, cleanPass) { isSuccess, cloudName, cloudPhone, isAdmin, adminTier, errorMsg ->
                                isVerifying = false
                                if (isSuccess) {
                                    authPrefs.edit()
                                        .putBoolean("is_logged_in", true)
                                        .putString("last_logged_in_email", cleanEmail)
                                        .putString("user_pass_$cleanEmail", cleanPass)
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
                                        Toast.makeText(context, "Welcome $cloudName! ✓", Toast.LENGTH_SHORT).show()
                                    }
                                    onLogin(cleanEmail, cleanPass)
                                } else {
                                    val savedLocalPass = authPrefs.getString("user_pass_$cleanEmail", null)
                                    if (savedLocalPass != null && savedLocalPass == cleanPass) {
                                        authPrefs.edit()
                                            .putBoolean("is_logged_in", true)
                                            .putString("last_logged_in_email", cleanEmail)
                                            .apply()
                                        onLogin(cleanEmail, cleanPass)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            errorMsg ?: "Account nahi mila ya Password galat hai!",
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
                                text = "Log In",
                                fontSize = 16.sp,
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
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = WorkoraTextMuted
                )
                Text(
                    text = "Sign Up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraOrange,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                )
            }
        }
    }
}
