package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.TextStyle
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
fun SignUpScreen(
    onSignUp: (name: String, email: String, phone: String, password: String) -> Unit = { _, _, _, _ -> },
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(true) }

    val hasMin8 = password.trim().length >= 8
    val hasLetter = password.any { it.isLetter() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() && !it.isWhitespace() }
    val isPasswordValid = hasMin8 && hasLetter && hasDigit

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateToLogin) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WorkoraNavy
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Create Workora Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WorkoraNavy
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        WorkoraHelmetLogo(size = 54.dp, showHalo = false)

        Spacer(modifier = Modifier.height(14.dp))

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
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name (आपका पूरा नाम)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraOrange) },
                    textStyle = TextStyle(color = WorkoraTextDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile Number (10-Digit Number)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraOrange) },
                    textStyle = TextStyle(color = WorkoraTextDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Gmail / Email ID") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = WorkoraOrange) },
                    textStyle = TextStyle(color = WorkoraTextDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Real Live Location Auto-Suggest + GPS Detection Field
                LiveLocationAutoCompleteField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Village / City (गाँव या शहर - Live Search)"
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Create Password (e.g. Ankit123)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = WorkoraOrange) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password",
                                tint = WorkoraOrange
                            )
                        }
                    },
                    textStyle = TextStyle(color = WorkoraTextDark, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WorkoraOrange,
                        unfocusedBorderColor = WorkoraBorder
                    )
                )

                // Practical Password Rules Checklist
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = WorkoraBgLight),
                    border = BorderStroke(1.dp, if (isPasswordValid) Color(0xFF16A34A) else WorkoraBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SignUpRuleItem("Minimum 8 characters (कम से कम 8 अक्षर)", hasMin8, false)
                        SignUpRuleItem("At least 1 letter (A-Z या a-z)", hasLetter, false)
                        SignUpRuleItem("At least 1 number (0-9)", hasDigit, false)
                        SignUpRuleItem("Special character (@, #, $) — Optional", hasSpecial, true)
                    }
                }

                Button(
                    onClick = {
                        val cleanName = fullName.trim()
                        val digits = mobileNumber.filter { it.isDigit() }.takeLast(10)
                        val cleanPass = password.trim()
                        val cleanEmailInput = email.trim().lowercase()
                        val cleanLocation = location.trim()

                        if (cleanName.isBlank() || digits.length < 10) {
                            Toast.makeText(context, "Kripya Naam aur 10-digit Mobile Number dalein!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (cleanLocation.isBlank()) {
                            Toast.makeText(context, "Kripya apna Village / City chunein!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!isPasswordValid) {
                            Toast.makeText(
                                context,
                                "Password mein kam se kam 8 characters, 1 letter aur 1 number zaroori hai!",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        val finalEmail = if (cleanEmailInput.contains("@")) cleanEmailInput else "${digits}@workora.in"

                        authPrefs.edit().apply {
                            putBoolean("is_logged_in", true)
                            putString("last_logged_in_email", finalEmail)
                            putString("user_pass_$finalEmail", cleanPass)
                            putString("user_pass_$digits", cleanPass)
                            apply()
                        }

                        profilePrefs.edit().apply {
                            putString("user_name", cleanName)
                            putString("user_phone", "+91 $digits")
                            putString("user_location", cleanLocation)
                            apply()
                        }

                        FirebaseManager.syncUserToFirebase(
                            context = context,
                            name = cleanName,
                            email = finalEmail,
                            phone = "+91 $digits",
                            password = cleanPass,
                            role = "CUSTOMER"
                        )

                        Toast.makeText(context, "Account Created Successfully! ✓", Toast.LENGTH_SHORT).show()
                        onSignUp(cleanName, finalEmail, "+91 $digits", cleanPass)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPasswordValid) WorkoraOrange else Color(0xFF94A3B8)
                    )
                ) {
                    Text(
                        text = "Register Account ✓",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pehle se account hai? ",
                fontSize = 13.sp,
                color = WorkoraTextMuted
            )
            Text(
                text = "Log In karein",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WorkoraNavy,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }

        Spacer(modifier = Modifier.height(180.dp))
    }
}

@Composable
private fun SignUpRuleItem(
    text: String,
    isMet: Boolean,
    isOptional: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (isOptional) Icons.Default.Star else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = when {
                isMet -> Color(0xFF16A34A)
                isOptional -> WorkoraOrange
                else -> Color(0xFF94A3B8)
            },
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isMet) FontWeight.Bold else FontWeight.Medium,
            color = if (isMet) Color(0xFF16A34A) else WorkoraTextMuted
        )
    }
}
