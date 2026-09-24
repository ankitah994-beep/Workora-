package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted

@Composable
fun LoginScreen(
    onLogin: (email: String, pass: String) -> Unit = { _, _ -> },
    onNavigateToSignUp: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }

    val pendingAction = remember { authPrefs.getString("welcome_next_action", null) }
    var showWelcomeFirst by remember { mutableStateOf(pendingAction == null) }

    LaunchedEffect(Unit) {
        if (pendingAction == "SIGNUP") {
            authPrefs.edit().remove("welcome_next_action").apply()
            onNavigateToSignUp()
        } else if (pendingAction == "LOGIN") {
            authPrefs.edit().remove("welcome_next_action").apply()
            showWelcomeFirst = false
        }
    }

    if (showWelcomeFirst) {
        WorkoraWelcomeContent(
            onGetStartedClick = { onNavigateToSignUp() },
            onLoginClick = { showWelcomeFirst = false }
        )
        return
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isCheckingCloud by remember { mutableStateOf(false) }

    fun completeLoginSuccess(
        cleanEmail: String,
        cleanPass: String,
        userName: String,
        userPhone: String,
        isAdmin: Boolean,
        adminTier: String?
    ) {
        val editor = authPrefs.edit()
            .putString("user_name_$cleanEmail", userName)
            .putString("user_phone_$cleanEmail", userPhone)
            .putString("user_pass_$cleanEmail", cleanPass)
            .putString("last_logged_in_email", cleanEmail)
            .putBoolean("is_logged_in", true)

        if (isAdmin) {
            editor.putString("saved_user_role", "ADMIN")
            editor.putString("saved_admin_tier", adminTier ?: "SUPER_ADMIN")
        } else if (authPrefs.getString("saved_user_role", null) == "ADMIN") {
            editor.remove("saved_user_role")
        }
        editor.apply()

        val profilePrefs = context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE)
        profilePrefs.edit()
            .putString("user_name", userName)
            .putString("user_phone", userPhone)
            .apply()

        val welcomeMsg = if (isAdmin) "Welcome Workora Admin ($userName) ✓" else "Login Safal Raha! Swagatam $userName ✓"
        Toast.makeText(context, welcomeMsg, Toast.LENGTH_SHORT).show()
        onLogin(cleanEmail, cleanPass)
    }

    fun performRealLogin() {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = password.trim()

        if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
            Toast.makeText(context, "Email aur Password dono bharein!", Toast.LENGTH_SHORT).show()
            return
        }

        isCheckingCloud = true
        FirebaseManager.verifyUserAndRoleFromFirebase(cleanEmail, cleanPass) { isSuccess, cloudName, cloudPhone, isAdmin, adminTier, errorMsg ->
            isCheckingCloud = false
            if (isSuccess && cloudName != null) {
                completeLoginSuccess(cleanEmail, cleanPass, cloudName, cloudPhone ?: "", isAdmin, adminTier)
            } else if (errorMsg != null) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            } else {
                val savedPassword = authPrefs.getString("user_pass_$cleanEmail", null)
                val savedName = authPrefs.getString("user_name_$cleanEmail", "Workora User") ?: "Workora User"
                val savedPhone = authPrefs.getString("user_phone_$cleanEmail", "") ?: ""

                if (savedPassword == null) {
                    Toast.makeText(context, "Account nahi mila! Kripya pehle Sign Up karein.", Toast.LENGTH_LONG).show()
                } else if (savedPassword != cleanPass) {
                    Toast.makeText(context, "Galat Password! Kripya sahi password dalein.", Toast.LENGTH_SHORT).show()
                } else {
                    completeLoginSuccess(cleanEmail, cleanPass, savedName, savedPhone, false, null)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showWelcomeFirst = true }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = WorkoraNavy)
            }
            Text(
                text = "Back to Welcome",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = WorkoraNavy,
                modifier = Modifier.clickable { showWelcomeFirst = true }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome Back!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WorkoraNavy
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Sign in with your Workora account",
            fontSize = 14.sp,
            color = WorkoraTextMuted
        )

        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Registered Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WorkoraOrange) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WorkoraTextDark,
                unfocusedTextColor = WorkoraTextDark,
                focusedBorderColor = WorkoraOrange,
                unfocusedBorderColor = WorkoraBorder
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange) },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WorkoraTextDark,
                unfocusedTextColor = WorkoraTextDark,
                focusedBorderColor = WorkoraOrange,
                unfocusedBorderColor = WorkoraBorder
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = { if (!isCheckingCloud) performRealLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
        ) {
            Text(
                text = if (isCheckingCloud) "Verifying Account & Role..." else "Log In",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Naya account banana hai? ", color = WorkoraTextMuted, fontSize = 14.sp)
            Text(
                text = "Sign Up",
                color = WorkoraOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
