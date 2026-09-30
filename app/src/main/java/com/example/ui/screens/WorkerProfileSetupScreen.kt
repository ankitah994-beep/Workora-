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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color Palette for New Design
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
fun WorkerProfileSetupScreen(
    initialPhone: String = "",
    onSetupComplete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    
    // Step Control
    var currentStep by remember { mutableIntStateOf(1) }

    // Form States (Matches Old Logic exactly)
    var profilePhotoB64 by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(initialPhone) }
    var selectedCategory by remember { mutableStateOf("Carpenter") }
    var experience by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var serviceRadius by remember { mutableFloatStateOf(10f) }
    var dailyRate by remember { mutableStateOf("") }
    var availability by remember { mutableStateOf("Available") }
    var aboutYourself by remember { mutableStateOf("") }
    
    // Work Proof Photos
    var workProof1B64 by remember { mutableStateOf("") }
    var workProof2B64 by remember { mutableStateOf("") }
    var workProof3B64 by remember { mutableStateOf("") }
    
    var activeUploadSlot by remember { mutableIntStateOf(0) }
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val encoded = encodeUriToBase64(context, uri)
            if (encoded.isNotBlank()) {
                when (activeUploadSlot) {
                    0 -> profilePhotoB64 = encoded
                    1 -> workProof1B64 = encoded
                    2 -> workProof2B64 = encoded
                    3 -> workProof3B64 = encoded
                }
                Toast.makeText(context, "Photo Uploaded", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val categories = listOf("Select category", "Carpenter", "Electrician", "Plumber", "Painter", "Mason", "Cleaner")
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
        // Header
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
                Text("Worker Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
            Text("$currentStep / 4", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextGray)
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
                    // STEP 1: Basic Info
                    val profileBmp = remember(profilePhotoB64) { decodeBase64Image(profilePhotoB64) }
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color(0xFFF1F5F9), CircleShape)
                            .clickable { activeUploadSlot = 0; photoPickerLauncher.launch("image/*") },
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
                    
                    Text("Work Category", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Simple simulated dropdown/selection for categories
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.take(4).forEach { catItem ->
                            val isSelected = selectedCategory == catItem
                            OutlinedButton(
                                onClick = { selectedCategory = catItem },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) WorkoraBlue else BorderGray),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (isSelected) WorkoraBlue.copy(alpha = 0.1f) else Color.White),
                                contentPadding = PaddingValues(2.dp)
                            ) { Text(catItem, fontSize = 11.sp, color = if (isSelected) WorkoraBlue else TextDark, maxLines = 1) }
                        }
                    }
                }
                
                2 -> {
                    // STEP 2: Work Details
                    OutlinedTextField(
                        value = experience, onValueChange = { experience = it },
                        label = { Text("Experience (e.g. 2-5 years)") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = location, onValueChange = { location = it },
                        label = { Text("Location") }, leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Service Radius: ${serviceRadius.toInt()} km", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Slider(
                        value = serviceRadius, onValueChange = { serviceRadius = it },
                        valueRange = 1f..50f, colors = SliderDefaults.colors(thumbColor = WorkoraBlue, activeTrackColor = WorkoraBlue)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = dailyRate, onValueChange = { dailyRate = it.filter { c -> c.isDigit() } },
                        label = { Text("Daily/Hourly Rate (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Availability", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Available", "Busy", "Not Available").forEach { status ->
                            val isSelected = availability == status
                            Button(
                                onClick = { availability = status }, modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) WorkoraBlue else Color(0xFFF1F5F9)),
                                contentPadding = PaddingValues(2.dp)
                            ) { Text(status, fontSize = 12.sp, color = if (isSelected) Color.White else TextGray, maxLines = 1) }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = aboutYourself, onValueChange = { aboutYourself = it },
                        label = { Text("About Yourself (Optional)") },
                        modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), colors = tfColors
                    )
                }

                3 -> {
                    // STEP 3: Work Proof Photos
                    Text("Work Proof (Upload 3 Photos)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add clear photos of your previous work to build trust and get more job opportunities.", fontSize = 14.sp, color = TextGray, modifier = Modifier.fillMaxWidth())
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(1 to workProof1B64, 2 to workProof2B64, 3 to workProof3B64).forEach { (slotNum, photoB64) ->
                            val bmp = remember(photoB64) { decodeBase64Image(photoB64) }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (photoB64.isNotBlank()) Color.Transparent else Color(0xFFF8FAFC))
                                    .border(1.dp, if (photoB64.isNotBlank()) WorkoraBlue else BorderGray, RoundedCornerShape(12.dp))
                                    .clickable { activeUploadSlot = slotNum; photoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (bmp != null) {
                                    Image(bitmap = bmp, contentDescription = "Proof $slotNum", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = TextGray, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Photo $slotNum", fontSize = 12.sp, color = TextGray)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)), shape = RoundedCornerShape(8.dp)) {
                        Text("Accepted formats: JPG, PNG\nMax size: 5MB per photo", fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(12.dp))
                    }
                }

                4 -> {
                    // STEP 4: Review Details
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
                                    Text("$selectedCategory • $experience", fontSize = 14.sp, color = TextGray)
                                    Text("₹$dailyRate/day • ${serviceRadius.toInt()} km radius", fontSize = 13.sp, color = TextGray)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = BorderGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text("Location", fontSize = 12.sp, color = TextGray)
                            Text(location.ifBlank { "Not specified" }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Availability", fontSize = 12.sp, color = TextGray)
                            Text(availability, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = WorkoraBlue)
                        }
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (currentStep == 4) {
                OutlinedButton(
                    onClick = { currentStep = 1 },
                    modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WorkoraBlue)
                ) { Text("Edit", fontSize = 16.sp, color = WorkoraBlue) }
            }
            
            Button(
                onClick = {
                    if (currentStep < 4) {
                        currentStep++
                    } else {
                        // FINAL SUBMIT (Using Your Exact Old Save Logic)
                        val finalEmail = "${phone}@workora.in"
                        
                        // Saving exactly like your old Registration flow
                        profilePrefs.edit().apply {
                            putString("user_name", fullName)
                            putString("user_phone", "+91 $phone")
                            putString("user_location", location)
                            putString("user_skill", selectedCategory)
                            putString("user_rate", dailyRate.ifBlank { "600" })
                            putString("user_experience", experience)
                            if (profilePhotoB64.isNotBlank()) putString("profile_photo_base64", profilePhotoB64)
                            if (workProof1B64.isNotBlank()) putString("work_photo_1", workProof1B64)
                            if (workProof2B64.isNotBlank()) putString("work_photo_2", workProof2B64)
                            if (workProof3B64.isNotBlank()) putString("work_photo_3", workProof3B64)
                            apply()
                        }
                        authPrefs.edit().putString("saved_user_role", "LABOUR").apply()
                        
                        Toast.makeText(context, "Profile Setup Complete ✓", Toast.LENGTH_LONG).show()
                        onSetupComplete() // Navigate to next screen (Dashboard)
                    }
                },
                modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraBlue)
            ) { 
                Text(if (currentStep == 4) "Submit" else "Next", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) 
            }
        }
    }
}
