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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)
private val ProfileOrange = Color(0xFFFF8C00)
private val ProfileGreen = Color(0xFF22A06B)

private fun decodeProfileBase64Image(base64Str: String?): ImageBitmap? {
    if (base64Str.isNullOrBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) { null }
}

@Composable
fun ProfileScreen(
    role: UserRole = UserRole.CUSTOMER,
    userName: String = "Workora User",
    userPhone: String = "+91 1234567890",
    userLocation: String = "",
    userExperience: String = "",
    userDailyWage: String = "",
    userAge: String = "",
    userTeamSize: String = "",
    userCategory: String = "",
    userAbout: String = "",
    onBack: () -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // File name updated to match WorkerProfileSetupScreen
    val profilePrefs = remember { context.getSharedPreferences("workora_prefs", Context.MODE_PRIVATE) }
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    
    val savedName = profilePrefs.getString("user_name", userName) ?: userName
    val savedPhone = profilePrefs.getString("user_phone", userPhone) ?: userPhone
    val savedLoc = profilePrefs.getString("user_location", userLocation) ?: userLocation
    val savedSkill = profilePrefs.getString("user_skill", userCategory) ?: userCategory
    val savedExp = profilePrefs.getString("user_experience", userExperience) ?: userExperience
    // Changed key from "user_rate" to "user_wage" to match setup screen
    val savedRate = profilePrefs.getString("user_wage", userDailyWage) ?: userDailyWage
    val savedAge = profilePrefs.getString("user_age", userAge) ?: userAge
    
    val profileB64 = profilePrefs.getString("profile_photo_base64", "") ?: ""
    val proof1B64 = profilePrefs.getString("work_photo_1", "") ?: ""
    val proof2B64 = profilePrefs.getString("work_photo_2", "") ?: ""
    val proof3B64 = profilePrefs.getString("work_photo_3", "") ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Masterplan Header Style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(28.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WorkoraBlue)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "My Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val profileBmp = remember(profileB64) { decodeProfileBase64Image(profileB64) }
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBmp != null) {
                            Image(
                                bitmap = profileBmp,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(40.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = savedName.ifBlank { "No details provided" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = savedPhone.ifBlank { "No details provided" },
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileDetailRow(Icons.Default.LocationOn, "Location", savedLoc)
                    Divider(color = BorderGray)
                    ProfileDetailRow(Icons.Default.Build, "Primary Skill", savedSkill)
                    Divider(color = BorderGray)
                    ProfileDetailRow(Icons.Default.Work, "Experience", savedExp)
                    Divider(color = BorderGray)
                    ProfileDetailRow(Icons.Default.Person, "Age", savedAge)
                    Divider(color = BorderGray)
                    ProfileDetailRow(Icons.Default.Star, "Daily Wage", if (savedRate.isNotBlank() && savedRate != "No details provided") "₹$savedRate/day" else savedRate)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Work Proof Images Section
            Text(
                text = "Work Proofs",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WorkProofImageBox(proof1B64, modifier = Modifier.weight(1f))
                WorkProofImageBox(proof2B64, modifier = Modifier.weight(1f))
                WorkProofImageBox(proof3B64, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = onSwitchRole,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
            ) {
                Text(
                    text = if (role == UserRole.CUSTOMER) "Switch to Worker" else "Switch to Customer",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444))
            ) {
                Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProfileDetailRow(icon: ImageVector, title: String, value: String) {
    val displayValue = value.trim().ifBlank { "No details provided" }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 12.sp, color = TextGray)
            Text(text = displayValue, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
        }
    }
}

@Composable
fun WorkProofImageBox(base64Str: String, modifier: Modifier = Modifier) {
    val bmp = remember(base64Str) { decodeProfileBase64Image(base64Str) }
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (bmp != null) {
                Image(
                    bitmap = bmp,
                    contentDescription = "Work Proof",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = TextGray, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("No image", fontSize = 11.sp, color = TextGray, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun WorkerProfileDialog(
    workerName: String,
    skills: String,
    experience: String,
    dailyRate: String,
    location: String,
    rating: String = "4.8 (12 verified reviews)",
    availability: String = "Available Today",
    teamSize: String = "Individual",
    about: String = "Verified Workora professional.",
    photo1B64: String = "",
    photo2B64: String = "",
    photo3B64: String = "",
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onHire: () -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit
) {
    val displaySkills = skills.ifBlank { "No details provided" }
    val displayExp = experience.ifBlank { "No details provided" }
    val displayRate = dailyRate.ifBlank { "No details provided" }
    val displayLoc = location.ifBlank { "No details provided" }
    val displayTeam = teamSize.ifBlank { "No details provided" }
    val displayAbout = about.ifBlank { "No details provided" }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        modifier = Modifier.fillMaxWidth(),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(workerName.ifBlank { "Unknown" }, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        Text("$displaySkills • $displayExp", fontSize = 12.sp, color = WorkoraBlue, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = BorderGray)

                // Details
                Text("• Skills: $displaySkills", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("• Daily Rate: $displayRate", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("• Area / Location: $displayLoc", fontSize = 13.sp, color = TextGray)
                Text("• Rating: ★ $rating", fontSize = 13.sp, color = TextGray)
                Text("• Availability: $availability", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProfileGreen)
                Text("• Team Size: $displayTeam", fontSize = 13.sp, color = TextGray)
                Text("• About: $displayAbout", fontSize = 13.sp, color = TextGray)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Work Proofs:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WorkProofImageBox(photo1B64, modifier = Modifier.weight(1f).height(60.dp))
                    WorkProofImageBox(photo2B64, modifier = Modifier.weight(1f).height(60.dp))
                    WorkProofImageBox(photo3B64, modifier = Modifier.weight(1f).height(60.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Report & Block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReport,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Text("Report", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onBlock,
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(0.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Text("Block", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Call & Hire
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCall,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.2.dp, WorkoraBlue)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = WorkoraBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call", color = WorkoraBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onHire,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
                    ) {
                        Text("Hire Now", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {}
    )
}
