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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    fun performRealLogin() {
        val cleanEmail = email.trim().lowercase()
        val cleanPass = password.trim()

        if (cleanEmail.isEmpty() || cleanPass.isEmpty()) {
            Toast.makeText(context, "Email aur Password dono bharein!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if user is registered
        val savedPassword = authPrefs.getString("user_pass_$cleanEmail", null)
        val savedName = authPrefs.getString("user_name_$cleanEmail", null)
        val savedPhone = authPrefs.getString("user_phone_$cleanEmail", null)

        if (savedPassword == null) {
            Toast.makeText(context, "Account nahi mila! Kripya pehle Sign Up karein.", Toast.LENGTH_LONG).show()
            return
        }

        if (savedPassword != cleanPass) {
            Toast.makeText(context, "Galat Password! Kripya sahi password dalein.", Toast.LENGTH_SHORT).show()
            return
        }

        // Login Success -> Save Session
        authPrefs.edit()
            .putString("last_logged_in_email", cleanEmail)
            .putBoolean("is_logged_in", true)
            .apply()

        // Sync Profile Data
        if (savedName != null && savedPhone != null) {
            val profilePrefs = context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE)
            profilePrefs.edit()
                .putString("user_name", savedName)
                .putString("user_phone", savedPhone)
                .apply()
        }

        Toast.makeText(context, "Login Safal Raha! Swagatam $savedName ✓", Toast.LENGTH_SHORT).show()
        onLogin(cleanEmail, cleanPass)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Welcome Back!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WorkoraNavy
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Sign in to access your Workora account",
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
            onClick = { performRealLogin() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
        ) {
            Text(text = "Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
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
