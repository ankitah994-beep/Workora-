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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
fun SignUpScreen(
    onSignUp: (name: String, email: String, phone: String, pass: String) -> Unit = { _, _, _, _ -> },
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    fun performRealSignUp() {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim()
        val cleanPass = password.trim()

        if (cleanName.isEmpty() || cleanEmail.isEmpty() || cleanPhone.isEmpty() || cleanPass.isEmpty()) {
            Toast.makeText(context, "Kripya sabhi details bharein!", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            Toast.makeText(context, "Sahi Email address dalein!", Toast.LENGTH_SHORT).show()
            return
        }

        if (cleanPhone.length < 10) {
            Toast.makeText(context, "10 ank ka Mobile number dalein!", Toast.LENGTH_SHORT).show()
            return
        }

        if (cleanPass.length < 6) {
            Toast.makeText(context, "Password kam se kam 6 akshar ka hona chahiye!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if user already exists
        if (authPrefs.contains("user_pass_$cleanEmail")) {
            Toast.makeText(context, "Yeh Email pehle se registered hai! Login karein.", Toast.LENGTH_LONG).show()
            return
        }

        // Save new user permanently in local database
        authPrefs.edit()
            .putString("user_name_$cleanEmail", cleanName)
            .putString("user_phone_$cleanEmail", cleanPhone)
            .putString("user_pass_$cleanEmail", cleanPass)
            .putString("last_logged_in_email", cleanEmail)
            .putBoolean("is_logged_in", true)
            .apply()

        // Also update profile SharedPreferences
        val profilePrefs = context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE)
        profilePrefs.edit()
            .putString("user_name", cleanName)
            .putString("user_phone", cleanPhone)
            .apply()

        Toast.makeText(context, "Account Safaltapoorvak Ban Gaya! ✓", Toast.LENGTH_SHORT).show()
        onSignUp(cleanName, cleanEmail, cleanPhone, cleanPass)
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
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WorkoraNavy
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Sign up to start hiring or finding work instantly",
            fontSize = 14.sp,
            color = WorkoraTextMuted
        )

        Spacer(modifier = Modifier.height(30.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraOrange) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WorkoraTextDark,
                unfocusedTextColor = WorkoraTextDark,
                focusedBorderColor = WorkoraOrange,
                unfocusedBorderColor = WorkoraBorder
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

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
                focusedTextColor = WorkoraTextDark,
                unfocusedTextColor = WorkoraTextDark,
                focusedBorderColor = WorkoraOrange,
                unfocusedBorderColor = WorkoraBorder
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10) phone = it },
            label = { Text("Mobile Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WorkoraTextDark,
                unfocusedTextColor = WorkoraTextDark,
                focusedBorderColor = WorkoraOrange,
                unfocusedBorderColor = WorkoraBorder
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (Min 6 chars)") },
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

        Spacer(modifier = Modifier.height(26.dp))

        Button(
            onClick = { performRealSignUp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
        ) {
            Text(text = "Sign Up & Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Pehle se account hai? ", color = WorkoraTextMuted, fontSize = 14.sp)
            Text(
                text = "Log In",
                color = WorkoraOrange,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
