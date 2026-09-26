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
import androidx.compose.material.icons.outlined.*
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
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val SignUpNavyPrimary = Color(0xFF083D91)
private val SignUpOrangeAccent = Color(0xFFFF8C00)
private val SignUpBgLight = Color(0xFFF8FAFC)
private val SignUpWhite = Color(0xFFFFFFFF)
private val SignUpMainText = Color(0xFF0B2345)
private val SignUpSecondaryText = Color(0xFF687280)
private val SignUpBorderColor = Color(0xFFE5EAF0)
private val SignUpDangerRed = Color(0xFFDC2626)

@Composable
fun SignUpScreen(
    onSignUp: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var areaLocation by remember { mutableStateOf("Silwani, Raisen") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = SignUpMainText,
        unfocusedTextColor = SignUpMainText,
        focusedBorderColor = SignUpNavyPrimary,
        unfocusedBorderColor = SignUpBorderColor,
        focusedContainerColor = SignUpBgLight,
        unfocusedContainerColor = SignUpBgLight
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SignUpBgLight)
    ) {
        // Bottom Navy & Orange Wave Design
        SignUpBottomWaveCanvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
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
            Spacer(modifier = Modifier.height(36.dp))

            // Top Workora Orange Hard-Hat Logo
            SignUpScreenHelmetLogo(size = 78.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Workora",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SignUpNavyPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Find. Hire. Work.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = SignUpMainText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Login | Register Tab Bar (Register Active)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToLogin() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = SignUpSecondaryText,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SignUpBorderColor)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { /* Active Tab */ },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Register",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignUpMainText,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(SignUpNavyPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // White Registration Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SignUpWhite),
                border = BorderStroke(1.dp, SignUpBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            errorMessage = null
                        },
                        placeholder = { Text("Full Name", color = SignUpSecondaryText, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Full Name",
                                tint = SignUpSecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            errorMessage = null
                        },
                        placeholder = { Text("Phone Number", color = SignUpSecondaryText, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = "Phone Number",
                                tint = SignUpSecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = emailAddress,
                        onValueChange = {
                            emailAddress = it
                            errorMessage = null
                        },
                        placeholder = { Text("Email Address", color = SignUpSecondaryText, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = "Email",
                                tint = SignUpSecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = areaLocation,
                        onValueChange = {
                            areaLocation = it
                            errorMessage = null
                        },
                        placeholder = { Text("City / Area / State", color = SignUpSecondaryText, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location",
                                tint = SignUpSecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        placeholder = { Text("Password", color = SignUpSecondaryText, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Password",
                                tint = SignUpSecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = "Toggle Password",
                                    tint = SignUpSecondaryText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage!!,
                            color = SignUpDangerRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val cleanName = fullName.trim()
                            val cleanPhone = phoneNumber.trim()
                            val cleanEmail = emailAddress.trim().ifEmpty { cleanPhone }
                            val cleanArea = areaLocation.trim().ifEmpty { "Silwani, Raisen" }
                            val cleanPass = password.trim()

                            when {
                                cleanName.isEmpty() -> {
                                    errorMessage = "Please enter your Full Name"
                                }
                                cleanPhone.length < 10 -> {
                                    errorMessage = "Please enter a valid 10-digit Phone Number"
                                }
                                cleanPass.length < 6 -> {
                                    errorMessage = "Password must be at least 6 characters"
                                }
                                else -> {
                                    isRegistering = true
                                    val chosenRole = authPrefs.getString("saved_user_role", "CUSTOMER") ?: "CUSTOMER"

                                    profilePrefs.edit()
                                        .putString("user_name", cleanName)
                                        .putString("user_phone", cleanPhone)
                                        .putString("user_location", cleanArea)
                                        .apply()

                                    authPrefs.edit()
                                        .putBoolean("is_logged_in", true)
                                        .putString("last_logged_in_email", cleanEmail)
                                        .putString("saved_password_$cleanEmail", cleanPass)
                                        .apply()

                                    registerNewUserToFirebase(
                                        name = cleanName,
                                        phone = cleanPhone,
                                        email = cleanEmail,
                                        area = cleanArea,
                                        role = chosenRole,
                                        password = cleanPass
                                    ) {
                                        isRegistering = false
                                        Toast.makeText(context, "Account created successfully! ✓", Toast.LENGTH_SHORT).show()
                                        onSignUp(cleanName, cleanEmail, cleanPhone, cleanPass)
                                    }
                                }
                            }
                        },
                        enabled = !isRegistering,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SignUpOrangeAccent,
                            contentColor = SignUpWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isRegistering) {
                            CircularProgressIndicator(
                                color = SignUpWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Creating Account...", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Register", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Already have an account? ",
                            fontSize = 13.sp,
                            color = SignUpSecondaryText
                        )
                        Text(
                            text = "Login",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SignUpOrangeAccent,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(180.dp))
        }
    }
}

@Composable
private fun SignUpScreenHelmetLogo(size: Dp = 78.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        drawArc(
            color = SignUpOrangeAccent,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.10f, h * 0.18f),
            size = Size(w * 0.80f, h * 0.82f)
        )

        drawRoundRect(
            color = SignUpOrangeAccent,
            topLeft = Offset(w * 0.43f, h * 0.11f),
            size = Size(w * 0.14f, h * 0.15f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        drawRoundRect(
            color = SignUpBgLight,
            topLeft = Offset(w * 0.37f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = SignUpBgLight,
            topLeft = Offset(w * 0.585f, h * 0.22f),
            size = Size(w * 0.045f, h * 0.22f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        drawRoundRect(
            color = SignUpOrangeAccent,
            topLeft = Offset(w * 0.03f, h * 0.56f),
            size = Size(w * 0.94f, h * 0.11f),
            cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
        )
    }
}

@Composable
private fun SignUpBottomWaveCanvas(modifier: Modifier = Modifier) {
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
            color = SignUpOrangeAccent
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
            color = SignUpNavyPrimary
        )
    }
}

private fun formatSignUpSafeKey(identifier: String): String {
    return identifier.trim().lowercase(Locale.US)
        .replace(".", "_")
        .replace("@", "_at_")
        .replace("+", "")
        .replace(" ", "")
}

private fun registerNewUserToFirebase(
    name: String,
    phone: String,
    email: String,
    area: String,
    role: String,
    password: String,
    onDone: () -> Unit
) {
    Thread {
        try {
            val key = formatSignUpSafeKey(email)
            val url = URL("https://workora-d8b51-default-rtdb.firebaseio.com/users/$key.json")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            val payload = JSONObject().apply {
                put("name", name)
                put("phone", phone)
                put("email", email)
                put("location", area)
                put("role", if (role.equals("LABOUR", true)) "Worker" else "Customer")
                put("password", password)
                put("status", "Active")
                put("availability", "Available")
                put("joined", SimpleDateFormat("d MMM yyyy", Locale.US).format(Date()))
            }
            OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
        Handler(Looper.getMainLooper()).post {
            onDone()
        }
    }.start()
}
