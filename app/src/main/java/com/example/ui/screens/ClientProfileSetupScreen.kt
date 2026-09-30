package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign // <--- यह इम्पोर्ट ज़रूरी था
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BorderGray = Color(0xFFE2E8F0)

private fun decodeBase64Image(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) { null }
}

private fun encodeUriToBase64(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else ""
    } catch (_: Exception) { "" }
}

@Composable
fun ClientProfileSetupScreen(
    initialPhone: String = "",
    onSetupComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    
    var currentStep by remember { mutableIntStateOf(1) }

    var profilePhotoB64 by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(initialPhone) }
    var email by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    
    var businessType by remember { mutableStateOf("Individual / Personal") }
    var previousJobs by remember { mutableStateOf("") }
    var additionalInfo by remember { mutableStateOf("") }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeUriToBase64(context, uri)
            if (encoded.isNotBlank()) {
                profilePhotoB64 = encoded
                Toast.makeText(context, "Photo Uploaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = WorkoraBlue, unfocusedBorderColor = BorderGray,
        focusedLeadingIconColor = WorkoraBlue, unfocusedLeadingIconColor = TextGray
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WorkoraBlue,
                    modifier = Modifier.size(24.dp).clickable { 
                        if (currentStep > 1) currentStep-- else onBack() 
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Client Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
            Text("$currentStep / 3", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextGray)
        }
        
        Divider(color = BorderGray, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (currentStep) {
                1 -> {
                    val profileBmp = remember(profilePhotoB64) { decodeBase64Image(profilePhotoB64) }
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                            .clickable { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBmp != null) {
                            Image(bitmap = profileBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = TextGray, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = fullName, onValueChange = { fullName = it },
                        label = { Text("Full Name") }, leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = phone, onValueChange = { if (it.length <= 10) phone = it.filter { c -> c.isDigit() } },
                        label = { Text("Phone Number") }, leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email (optional)") }, leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = location, onValueChange = { location = it },
                        label = { Text("Location") }, leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = about, onValueChange = { about = it },
                        label = { Text("About You (Optional)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                }
                
                2 -> {
                    Text("Business Type", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Individual / Personal", "Company / Business").forEach { type ->
                            val isSelected = businessType == type
                            Button(
                                onClick = { businessType = type }, modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) WorkoraBlue else Color(0xFFF1F5F9))
                            ) { Text(type, fontSize = 13.sp, color = if (isSelected) Color.White else TextGray, textAlign = TextAlign.Center) }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = previousJobs, onValueChange = { previousJobs = it },
                        label = { Text("Previous Jobs (optional)") }, placeholder = { Text("Tell us about past work...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = additionalInfo, onValueChange = { additionalInfo = it },
                        label = { Text("Additional Information") }, placeholder = { Text("Anything else you want to share?") },
                        modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                }

                3 -> {
                    Text("Review Your Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Check your details before submitting.", fontSize = 14.sp, color = TextGray, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Card(modifier = Modifier.fillMaxWidth(), border = BorderStroke(1.dp, BorderGray), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val profileBmp = remember(profilePhotoB64) { decodeBase64Image(profilePhotoB64) }
                                if (profileBmp != null) {
                                    Image(bitmap = profileBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(60.dp).clip(CircleShape))
                                } else {
                                    Box(modifier = Modifier.size(60.dp).background(Color(0xFFE2E8F0), CircleShape))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(fullName.ifBlank { "No Name" }, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                    Text(businessType, fontSize = 13.sp, color = TextGray)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = BorderGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Location", fontSize = 12.sp, color = TextGray)
                            Text(location.ifBlank { "Not specified" }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("About", fontSize = 12.sp, color = TextGray)
                            Text(about.ifBlank { "No description provided." }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (currentStep == 3) {
                OutlinedButton(
                    onClick = { currentStep = 1 },
                    modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WorkoraBlue)
                ) { Text("Edit", fontSize = 16.sp, color = WorkoraBlue) }
            }
            
            Button(
                onClick = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        profilePrefs.edit().apply {
                            putString("user_name", fullName)
                            putString("user_phone", "+91 $phone")
                            putString("user_email", email)
                            putString("user_location", location)
                            putString("client_about", about)
                            putString("client_business_type", businessType)
                            if (profilePhotoB64.isNotBlank()) putString("profile_photo_base64", profilePhotoB64)
                            apply()
                        }
                        authPrefs.edit().putString("saved_user_role", "CUSTOMER").apply()
                        
                        Toast.makeText(context, "Client Profile Created ✓", Toast.LENGTH_LONG).show()
                        onSetupComplete()
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
            ) { 
                Text(if (currentStep == 3) "Submit" else "Next", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) 
            }
        }
    }
}
