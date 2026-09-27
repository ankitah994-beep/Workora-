package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.components.WorkoraHelmetLogo
import kotlinx.coroutines.delay

@Composable
fun AccountSelectScreen(
    onSelectRole: (UserRole) -> Unit = {},
    toastMessage: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val settingsPrefs = remember { context.getSharedPreferences("workora_app_settings", Context.MODE_PRIVATE) }

    val deepNavy = Color(0xFF072A5E)
    val brandBlue = Color(0xFF083D91)
    val brandOrange = Color(0xFFFF8C00)
    val textDark = Color(0xFF0F172A)
    val textMuted = Color(0xFF475569)
    val cardBorder = Color(0xFFE2E8F0)

    // Both cards are unselected (null -> White) initially!
    // When user clicks one, it turns Blue and moves to Language Selection step.
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var showLanguageStep by remember { mutableStateOf(false) }
    var clickedLanguage by remember { mutableStateOf<String?>(null) }

    var isAdminDashboardOpen by remember { mutableStateOf(false) }

    val loggedEmail = remember {
        (authPrefs.getString("last_logged_in_email", "") ?: "").trim().lowercase()
    }
    val savedPhone = remember {
        (profilePrefs.getString("user_phone", "") ?: "").filter { it.isDigit() }.takeLast(10)
    }
    val isVerifiedAdmin = remember(loggedEmail, savedPhone) {
        loggedEmail == "ankitah994@gmail.com" ||
                loggedEmail.contains("ankitah994") ||
                savedPhone == "6265798340" ||
                authPrefs.getString("saved_user_role", "") == "ADMIN"
    }

    // Smooth transition from Role click (Blue highlight) -> Language Selection step
    LaunchedEffect(selectedRole) {
        if (selectedRole != null && !showLanguageStep) {
            delay(220)
            showLanguageStep = true
        }
    }

    // Smooth transition after Language click (Blue highlight) -> Enter App Dashboard
    LaunchedEffect(clickedLanguage) {
        val lang = clickedLanguage
        val role = selectedRole
        if (lang != null && role != null) {
            settingsPrefs.edit()
                .putString("app_language", lang)
                .putBoolean("language_selected_once", true)
                .apply()
            authPrefs.edit()
                .putString("saved_user_role", role.name)
                .apply()
            delay(220)
            Toast.makeText(
                context,
                if (lang == "Hindi") "भाषा हिन्दी चुनी गई ✓" else "Language set to English ✓",
                Toast.LENGTH_SHORT
            ).show()
            onSelectRole(role)
        }
    }

    if (isAdminDashboardOpen) {
        AdminDashboardScreen(
            adminEmail = loggedEmail.ifBlank { "ankitah994@gmail.com" },
            adminTier = "SUPER_ADMIN",
            onLogoutAdmin = {
                isAdminDashboardOpen = false
            },
            onSwitchRoleFromAdmin = { targetRole ->
                isAdminDashboardOpen = false
                if (targetRole == "LABOUR") {
                    onSelectRole(UserRole.LABOUR)
                } else {
                    onSelectRole(UserRole.CUSTOMER)
                }
            },
            onBack = { isAdminDashboardOpen = false }
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Bottom Decorative Navy + Orange Waves (Matches Image 1)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .align(Alignment.BottomCenter)
        ) {
            val w = size.width
            val h = size.height

            val orangeWave = Path().apply {
                moveTo(w * 0.42f, h * 0.52f)
                quadraticBezierTo(w * 0.76f, h * 0.05f, w, h * 0.22f)
                lineTo(w, h)
                lineTo(w * 0.42f, h)
                close()
            }
            drawPath(path = orangeWave, color = brandOrange)

            val navyWave = Path().apply {
                moveTo(0f, h * 0.28f)
                quadraticBezierTo(w * 0.22f, h * 0.08f, w * 0.48f, h * 0.38f)
                quadraticBezierTo(w * 0.75f, h * 0.68f, w, h * 0.34f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = navyWave, color = brandBlue)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showLanguageStep) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            showLanguageStep = false
                            selectedRole = null
                            clickedLanguage = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = deepNavy
                        )
                    }
                    Text(
                        text = "Back",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = deepNavy
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(36.dp))
            }

            // Top Orange Helmet Logo
            WorkoraHelmetLogo(size = 76.dp, showHalo = false)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Workora",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = brandBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Find. Hire. Work.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textDark
            )

            Spacer(modifier = Modifier.height(36.dp))

            if (!showLanguageStep) {
                // ==================== STEP 1: WHAT DO YOU WANT TO DO? ====================
                Text(
                    text = "What do you want to do?",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // CARD 1: I want to Hire (White by default, turns Blue ONLY when clicked!)
                val isHireSelected = selectedRole == UserRole.CUSTOMER
                Card(
                    onClick = { selectedRole = UserRole.CUSTOMER },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHireSelected) deepNavy else Color.White
                    ),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (isHireSelected) deepNavy else cardBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(brandOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "I want to Hire",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isHireSelected) Color.White else textDark
                                )
                                Text(
                                    text = "Find skilled workers for your work",
                                    fontSize = 14.sp,
                                    color = if (isHireSelected) Color.White.copy(alpha = 0.9f) else textMuted,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isHireSelected) Color.White else textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // CARD 2: I want to Work (White by default, turns Blue ONLY when clicked!)
                val isWorkSelected = selectedRole == UserRole.LABOUR
                Card(
                    onClick = { selectedRole = UserRole.LABOUR },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWorkSelected) deepNavy else Color.White
                    ),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (isWorkSelected) deepNavy else cardBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(brandOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "I want to Work",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isWorkSelected) Color.White else textDark
                                )
                                Text(
                                    text = "Find jobs and earn money",
                                    fontSize = 14.sp,
                                    color = if (isWorkSelected) Color.White.copy(alpha = 0.9f) else textMuted,
                                    lineHeight = 19.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isWorkSelected) Color.White else textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Super Admin Entry Button (Only for Verified Admin)
                if (isVerifiedAdmin) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { isAdminDashboardOpen = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = deepNavy)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = brandOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Open Workora Super Admin Panel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            } else {
                // ==================== STEP 2: LANGUAGE SELECTION (AFTER ROLE SELECTION) ====================
                Text(
                    text = "Choose Your Language\nअपनी भाषा चुनें",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Option 1: हिन्दी (Hindi) - White by default, turns Blue when clicked
                val isHindiSelected = clickedLanguage == "Hindi"
                Card(
                    onClick = { clickedLanguage = "Hindi" },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHindiSelected) deepNavy else Color.White
                    ),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (isHindiSelected) deepNavy else cardBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(brandOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "अ",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "हिन्दी (Hindi)",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isHindiSelected) Color.White else textDark
                                )
                                Text(
                                    text = "ऐप को हिन्दी भाषा में चलाएं",
                                    fontSize = 13.sp,
                                    color = if (isHindiSelected) Color.White.copy(alpha = 0.9f) else textMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isHindiSelected) Icons.Default.CheckCircle else Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isHindiSelected) Color.White else textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option 2: English - White by default, turns Blue when clicked
                val isEnglishSelected = clickedLanguage == "English"
                Card(
                    onClick = { clickedLanguage = "English" },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnglishSelected) deepNavy else Color.White
                    ),
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = if (isEnglishSelected) deepNavy else cardBorder
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(brandOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "English",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isEnglishSelected) Color.White else textDark
                                )
                                Text(
                                    text = "Continue in English language",
                                    fontSize = 13.sp,
                                    color = if (isEnglishSelected) Color.White.copy(alpha = 0.9f) else textMuted
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isEnglishSelected) Icons.Default.CheckCircle else Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (isEnglishSelected) Color.White else textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(160.dp))
        }
    }
}
