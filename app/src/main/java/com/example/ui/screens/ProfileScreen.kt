package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted

private fun loadBitmapFromUri(context: Context, uri: Uri): ImageBitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ProfileScreen(
    role: UserRole = UserRole.CUSTOMER,
    userName: String = "Ankit Ahirwar",
    userPhone: String = "+91 98765 43210",
    userLocation: String = "Silwani, Raisen",
    onBack: () -> Unit = {},
    onSwitchRole: () -> Unit = {},
    onLogout: () -> Unit = {},
    onUpdateProfile: (name: String, phone: String, location: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var currentName by remember { mutableStateOf(userName) }
    var currentPhone by remember { mutableStateOf(userPhone) }
    var currentLocation by remember { mutableStateOf(userLocation) }

    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val workPhotoBitmaps = remember {
        mutableStateListOf<ImageBitmap?>(null, null, null)
    }
    var selectedWorkPhotoIndex by remember { mutableIntStateOf(0) }

    // 1. Profile Photo Pickers (Primary + Fallback)
    val profileFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profileBitmap = loadBitmapFromUri(context, uri)
            Toast.makeText(context, "Profile Photo Updated!", Toast.LENGTH_SHORT).show()
        }
    }

    val profileDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            profileBitmap = loadBitmapFromUri(context, uri)
            Toast.makeText(context, "Profile Photo Updated!", Toast.LENGTH_SHORT).show()
        }
    }

    // 2. Work Portfolio 3 Photos Pickers (Primary + Fallback)
    val workFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val bmp = loadBitmapFromUri(context, uri)
            if (bmp != null && selectedWorkPhotoIndex in 0..2) {
                workPhotoBitmaps[selectedWorkPhotoIndex] = bmp
                Toast.makeText(context, "Work Photo ${selectedWorkPhotoIndex + 1} Uploaded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val workDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val bmp = loadBitmapFromUri(context, uri)
            if (bmp != null && selectedWorkPhotoIndex in 0..2) {
                workPhotoBitmaps[selectedWorkPhotoIndex] = bmp
                Toast.makeText(context, "Work Photo ${selectedWorkPhotoIndex + 1} Uploaded!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openProfileGallery() {
        Toast.makeText(context, "Opening Gallery for Profile Photo...", Toast.LENGTH_SHORT).show()
        try {
            profileDocumentLauncher.launch(arrayOf("image/*"))
        } catch (e: Exception) {
            try {
                profileFallbackLauncher.launch("image/*")
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to open gallery on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openWorkPhotoGallery(index: Int) {
        selectedWorkPhotoIndex = index
        Toast.makeText(context, "Select Work Photo ${index + 1} from Gallery...", Toast.LENGTH_SHORT).show()
        try {
            workDocumentLauncher.launch(arrayOf("image/*"))
        } catch (e: Exception) {
            try {
                workFallbackLauncher.launch("image/*")
            } catch (ex: Exception) {
                Toast.makeText(context, "Unable to open gallery on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val roleTitle = if (role == UserRole.CUSTOMER) "Hirer / Customer Profile" else "Worker / Labour Profile"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = WorkoraNavy)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "My Profile & Work Photos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
            }
            OutlinedButton(
                onClick = { showEditDialog = true },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, WorkoraOrange),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = WorkoraOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Edit All", color = WorkoraOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Photo Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clickable { openProfileGallery() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(WorkoraOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!,
                            contentDescription = "Profile Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = WorkoraOrange,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { openProfileGallery() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .background(WorkoraNavy, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Upload Profile Photo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { openProfileGallery() },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, WorkoraOrange),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (profileBitmap != null) "Change Profile Photo" else "Upload Profile Photo",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraOrange
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = currentName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WorkoraTextDark)

            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(WorkoraNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(text = roleTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 3 Work Portfolio Photos Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "काम की 3 फोटो (Work Proof Portfolio)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraTextDark
                        )
                        Text(
                            text = "${workPhotoBitmaps.count { it != null }}/3 Added",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WorkoraOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "अपने किए हुए काम की 3 फोटो अपलोड करें ताकि ग्राहक आपका काम देखकर तुरंत काम दें। नीचे दिए गए बटन या बॉक्स पर टैप करें:",
                        fontSize = 12.sp,
                        color = WorkoraTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (index in 0..2) {
                            val currentWorkBitmap = workPhotoBitmaps[index]
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Native onClick on Card so touch never fails
                                Card(
                                    onClick = { openWorkPhotoGallery(index) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(105.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = WorkoraBgLight),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (currentWorkBitmap != null) Color(0xFF16A34A) else WorkoraOrange
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        if (currentWorkBitmap != null) {
                                            Image(
                                                bitmap = currentWorkBitmap,
                                                contentDescription = "Work Photo ${index + 1}",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .background(Color.White, shape = CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Uploaded",
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddAPhoto,
                                                    contentDescription = null,
                                                    tint = WorkoraOrange,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Photo ${index + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WorkoraTextDark
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Dedicated Button below each box so clicking is 100% guaranteed
                                Button(
                                    onClick = { openWorkPhotoGallery(index) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(30.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (currentWorkBitmap != null) Color(0xFF16A34A) else WorkoraOrange
                                    )
                                ) {
                                    Text(
                                        text = if (currentWorkBitmap != null) "Change ${index + 1}" else "Upload ${index + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Account Information", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraTextMuted)

            ProfileInfoCard(icon = Icons.Default.Person, title = "Full Name", value = currentName)
            ProfileInfoCard(icon = Icons.Default.Phone, title = "Mobile Number", value = currentPhone)
            ProfileInfoCard(icon = Icons.Default.LocationOn, title = "Location / Address", value = currentLocation)
            ProfileInfoCard(icon = Icons.Default.VerifiedUser, title = "Account Status", value = "Verified & Active")

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Preferences & Actions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraTextMuted)

            OutlinedButton(
                onClick = onSwitchRole,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Icon(imageVector = Icons.Default.SwapHoriz, tint = WorkoraNavy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Switch to ${if (role == UserRole.CUSTOMER) "Labour Mode" else "Hirer Mode"}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkoraNavy
                )
            }

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, tint = Color.White, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Logout", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = "Edit Profile Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = currentName,
                        onValueChange = { currentName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = currentPhone,
                        onValueChange = { currentPhone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = currentLocation,
                        onValueChange = { currentLocation = it },
                        label = { Text("Location / Address") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(currentName, currentPhone, currentLocation)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
                ) {
                    Text("Save All Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileInfoCard(icon: ImageVector, title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, WorkoraBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(WorkoraBgLight, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = WorkoraNavy, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 12.sp, color = WorkoraTextMuted)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
            }
        }
    }
}
