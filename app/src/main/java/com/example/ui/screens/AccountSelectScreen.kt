package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.components.RoleCard
import com.example.ui.components.WorkoraHelmetLogo
import com.example.ui.components.WorkoraToast
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted

@Composable
fun AccountSelectScreen(
    onSelectRole: (UserRole) -> Unit,
    toastMessage: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val brandPrefs = remember { context.getSharedPreferences("workora_app_branding", Context.MODE_PRIVATE) }

    var isAdminPanelVisible by remember { mutableStateOf(false) }
    // Default is strictly FALSE so normal users never see the Admin Panel card!
    var isBackendAdminVerified by remember { mutableStateOf(false) }
    var adminRoleTier by remember { mutableStateOf("SUPER_ADMIN") }

    var appName by remember { mutableStateOf(brandPrefs.getString("app_name", "WORKORA") ?: "WORKORA") }
    var appTagline by remember { mutableStateOf(brandPrefs.getString("app_tagline", "FIND. HIRE. WORK.") ?: "FIND. HIRE. WORK.") }
    var welcomeHeading by remember { mutableStateOf(brandPrefs.getString("welcome_heading", "What do you want to do?") ?: "What do you want to do?") }
    var bannerText by remember { mutableStateOf(brandPrefs.getString("banner_text", "") ?: "") }
    var customLogoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    fun refreshLocalBranding() {
        appName = brandPrefs.getString("app_name", "WORKORA") ?: "WORKORA"
        appTagline = brandPrefs.getString("app_tagline", "FIND. HIRE. WORK.") ?: "FIND. HIRE. WORK."
        welcomeHeading = brandPrefs.getString("welcome_heading", "What do you want to do?") ?: "What do you want to do?"
        bannerText = brandPrefs.getString("banner_text", "") ?: ""
        val base64 = brandPrefs.getString("logo_base64", "") ?: ""
        customLogoBitmap = if (base64.isNotBlank()) {
            try {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val activeEmail = remember {
        authPrefs.getString("last_logged_in_email", "") ?: ""
    }

    LaunchedEffect(isAdminPanelVisible) {
        refreshLocalBranding()
    }

    LaunchedEffect(Unit) {
        refreshLocalBranding()
        val isLogged = authPrefs.getBoolean("is_logged_in", false)
        if (isLogged && activeEmail.isNotBlank()) {
            FirebaseManager.checkIfEmailIsAdminOnCloud(activeEmail) { isAdmin, tier ->
                isBackendAdminVerified = isAdmin
                if (isAdmin) {
                    adminRoleTier = tier
                }
            }
        } else {
            isBackendAdminVerified = false
        }
    }

    if (isAdminPanelVisible && isBackendAdminVerified) {
        AdminDashboardScreen(
            adminEmail = activeEmail,
            adminTier = adminRoleTier,
            onLogoutAdmin = {
                refreshLocalBranding()
                isAdminPanelVisible = false
            }
        )
        return
    }

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
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (customLogoBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = customLogoBitmap!!,
                            contentDescription = "App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    WorkoraHelmetLogo(
                        size = 50.dp,
                        showHalo = false
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = appName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy,
                    letterSpacing = 1.5.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = appTagline,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraOrange,
                    letterSpacing = 1.2.sp
                )

                if (bannerText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WorkoraOrange.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "📢 $bannerText",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = welcomeHeading,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = WorkoraTextDark,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Select how you want to use the app",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = WorkoraTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    RoleCard(
                        role = UserRole.CUSTOMER,
                        onClick = { onSelectRole(UserRole.CUSTOMER) }
                    )

                    RoleCard(
                        role = UserRole.LABOUR,
                        onClick = { onSelectRole(UserRole.LABOUR) }
                    )

                    // Only visible if the logged-in user is verified as Admin on Firebase
                    if (isBackendAdminVerified) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    authPrefs.edit().putString("saved_user_role", "ADMIN").apply()
                                    isAdminPanelVisible = true
                                },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = WorkoraNavy),
                            border = BorderStroke(2.dp, WorkoraOrange),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(WorkoraOrange),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Admin",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = "$appName Admin Panel",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Edit App Logo, Users, Jobs & Controls",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = WorkoraOrange,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        WorkoraToast(
            message = toastMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        )
    }
}
