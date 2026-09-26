package com.example.ui.screens

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private val LoginNavyPrimary = Color(0xFF083D91)
private val LoginOrangeAccent = Color(0xFFFF8C00)
private val LoginBgLight = Color(0xFFF8FAFC)
private val LoginWhite = Color(0xFFFFFFFF)
private val LoginMainText = Color(0xFF0B2345)
private val LoginSecondaryText = Color(0xFF687280)
private val LoginBorderColor = Color(0xFFE5EAF0)
private val LoginDangerRed = Color(0xFFDC2626)

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit = { _, _ -> },
    onNavigateToSignUp: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    var emailOrPhoneInput by remember {
        mutableStateOf(authPrefs.getString("last_logged_in_email", "") ?: "")
    }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBgLight)
    ) {
        // Bottom Reference Blue & Orange Wave Design
        LoginBottomWaveCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(46.dp))

            LoginScreenHelmetLogo(size = 84.dp)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Workora",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LoginNavyPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Find. Hire. Work.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = LoginMainText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Login | Register Tab Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { /* Active Tab */ },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoginMainText,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(LoginNavyPrimary)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToSignUp() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Register",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LoginSecondaryText,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(LoginBorderColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // White Login Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LoginWhite),
                border = BorderStroke(1.dp, LoginBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = emailOrPhoneInput,
                        onValueChange = {
                            emailOrPhoneInput = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                text = "Email or Phone",
                                color = LoginSecondaryText,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = "Email or Phone",
                                tint = LoginSecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LoginMainText,
                            unfocusedTextColor = LoginMainText,
                            focusedBorderColor = LoginNavyPrimary,
                            unfocusedBorderColor = LoginBorderColor,
                            focusedContainerColor = LoginBgLight,
                            unfocusedContainerColor = LoginBgLight
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                text = "Password",
                                color = LoginSecondaryText,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Password",
                                tint = LoginSecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = "Toggle Password",
                                    tint = LoginSecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LoginMainText,
                            unfocusedTextColor = LoginMainText,
                            focusedBorderColor = LoginNavyPrimary,
                            unfocusedBorderColor = LoginBorderColor,
                            focusedContainerColor = LoginBgLight,
                            unfocusedContainerColor = LoginBgLight
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage!!,
                            color = LoginDangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val cleanIdentifier = emailOrPhoneInput.trim()
                            val cleanPass = passwordInput.trim()

                            when {
                                cleanIdentifier.isEmpty() -> {
                                    errorMessage = "Please enter your registered Email or Phone number"
                                }
                                cleanPass.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                }
                                else -> {
                                    isAuthenticating = true
                                    authenticateWorkoraUserOnCloud(
                                        identifier = cleanIdentifier,
                                        password = cleanPass,
                                        authPrefs = authPrefs,
                                        profilePrefs = profilePrefs
                                    ) { success: Boolean, msg: String ->
                                        isAuthenticating = false
                                        if (success) {
                                            Toast.makeText(context, "Welcome back to Workora! ✓", Toast.LENGTH_SHORT).show()
                                            onLogin(cleanIdentifier, cleanPass)
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isAuthenticating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LoginOrangeAccent,
                            contentColor = LoginWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                color = LoginWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Signing in...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Login",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account? ",
                            fontSize = 13.sp,
                            color = LoginSecondaryText
                        )
                        Text(
                            text = "Register",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoginOrangeAccent,
                            modifier = Modifier.clickable { onNavigateToSignUp() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}

@Composable
private fun LoginScreenHelmetLogo(size: Dp = 84.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawArc(
            color = LoginOrangeAccent,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.10f, h * 0.18f),
            size = Size(w * 0.80f, h * 0.82f)
        )

        drawRoundRect(
            color = LoginOrangeAccent,
            topLeft = Offset(w * 0.43f, h * 0.11f),
            size = Size(w * 0.14f, h * 0.15f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        drawRoundRect(
            color = LoginBgLight,
            topLeft = Offset(w * 0.37f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = LoginBgLight,
            topLeft = Offset(w * 0.585f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        drawRoundRect(
            color = LoginOrangeAccent,
            topLeft = Offset(w * 0.03f, h * 0.56f),
            size = Size(w * 0.94f, h * 0.11f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )
    }
}

@Composable
private fun LoginBottomWaveCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val orangeWavePath = Path().apply {
            moveTo(w * 0.34f, h * 0.66f)
            cubicTo(
                w * 0.58f, h * 0.60f,
                w * 0.78f, h * 0.16f,
                w, h * 0.24f
            )
            lineTo(w, h)
            lineTo(w * 0.34f, h)
            close()
        }
        drawPath(
            path = orangeWavePath,
            color = LoginOrangeAccent
        )

        val navyWavePath = Path().apply {
            moveTo(0f, h * 0.36f)
            cubicTo(
                w * 0.28f, h * 0.08f,
                w * 0.55f, h * 0.82f,
                w, h * 0.40f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = navyWavePath,
            color = LoginNavyPrimary
        )
    }
}

private fun formatSafeFirebaseUserKey(identifier: String): String {
    return identifier.trim().lowercase(Locale.US)
        .replace(".", "_")
        .replace("@", "_at_")
        .replace("+", "")
        .replace(" ", "")
}

private fun authenticateWorkoraUserOnCloud(
    identifier: String,
    password: String,
    authPrefs: android.content.SharedPreferences,
    profilePrefs: android.content.SharedPreferences,
    onResult: (Boolean, String) -> Unit
) {
    val cleanId = identifier.trim().lowercase(Locale.US)

    // Super Admin instant login support
    if (cleanId == "ankitah994@gmail.com" || cleanId.contains("6265798340")) {
        authPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("last_logged_in_email", cleanId)
            .putString("saved_user_role", "ADMIN")
            .putString("saved_password_$cleanId", password)
            .apply()
        onResult(true, "Admin Login Success")
        return
    }

    Thread {
        val dbUrl = "https://workora-d8b51-default-rtdb.firebaseio.com"
        try {
            val key = formatSafeFirebaseUserKey(cleanId)
            val conn = URL("$dbUrl/users/$key.json").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            if (conn.responseCode == 200) {
                val resp = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                conn.disconnect()

                if (resp.isNotBlank() && resp != "null" && resp.startsWith("{")) {
                    val obj = JSONObject(resp)
                    val cloudPass = obj.optString("password", "")
                    val status = obj.optString("status", "Active")
                    if (status.equals("Blocked", ignoreCase = true)) {
                        Handler(Looper.getMainLooper()).post {
                            onResult(false, "Your account has been blocked by Admin.")
                        }
                        return@Thread
                    }
                    if (cloudPass.isNotBlank() && cloudPass != password) {
                        Handler(Looper.getMainLooper()).post {
                            onResult(false, "Incorrect password. Please try again.")
                        }
                        return@Thread
                    }

                    // Load ONLY exact registered details from Firebase (no dummy defaults)
                    val name = obj.optString("name", "")
                    val phone = obj.optString("phone", "")
                    val state = obj.optString("state", "")
                    val loc = obj.optString("location", "")
                    val skill = obj.optString("skill", "")
                    val exp = obj.optString("experience", "")
                    val rate = obj.optInt("dailyRate", 0)
                    val roleRaw = obj.optString("role", "")

                    profilePrefs.edit()
                        .putString("user_name", name)
                        .putString("user_phone", phone)
                        .putString("user_state", state)
                        .putString("user_location", loc)
                        .putString("user_skill", skill)
                        .putString("user_experience", exp)
                        .putString("user_rate", if (rate > 0) rate.toString() else "")
                        .apply()

                    val mappedRole = if (roleRaw.equals("Worker", true) || roleRaw.equals("LABOUR", true)) "LABOUR" else "CUSTOMER"
                    authPrefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putString("last_logged_in_email", cleanId)
                        .putString("saved_user_role", mappedRole)
                        .putString("saved_password_$cleanId", password)
                        .apply()

                    Handler(Looper.getMainLooper()).post {
                        onResult(true, "Success")
                    }
                    return@Thread
                } else {
                    // User has NOT registered yet — require full registration first!
                    Handler(Looper.getMainLooper()).post {
                        onResult(false, "Account not found! Please click 'Register' to fill your complete details first.")
                    }
                    return@Thread
                }
            } else {
                conn.disconnect()
            }
        } catch (_: Exception) {
        }

        Handler(Looper.getMainLooper()).post {
            onResult(false, "Unable to verify account. Please check your internet or Register first.")
        }
    }.start()
}
