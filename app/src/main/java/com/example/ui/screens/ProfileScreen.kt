package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    var showEditDialog by remember { mutableStateOf(false) }
    var currentName by remember { mutableStateOf(userName) }
    var currentPhone by remember { mutableStateOf(userPhone) }
    var currentLocation by remember { mutableStateOf(userLocation) }
    var profilePhotoUploaded by remember { mutableStateOf(false) }

    val roleTitle = if (role == UserRole.CUSTOMER) "Hirer / Customer Profile" else "Worker / Labour Profile"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
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
                Text(text = "My Profile & Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(WorkoraOrange.copy(alpha = 0.15f), shape = CircleShape)
                    .clickable { profilePhotoUploaded = !profilePhotoUploaded },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(56.dp))
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(WorkoraNavy, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Upload Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (profilePhotoUploaded) "Photo Updated Successfully!" else "Tap icon to change photo",
                fontSize = 11.sp,
                color = if (profilePhotoUploaded) Color(0xFF16A34A) else WorkoraTextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))
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
            Text(text = "Account Information", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraTextMuted)

            ProfileInfoCard(icon = Icons.Default.Person, title = "Full Name", value = currentName)
            ProfileInfoCard(icon = Icons.Default.Phone, title = "Mobile Number", value = currentPhone)
            ProfileInfoCard(icon = Icons.Default.LocationOn, title = "Location / Address", value = currentLocation)
            ProfileInfoCard(icon = Icons.Default.VerifiedUser, title = "Account Status", value = "Verified & Active")

            if (role == UserRole.LABOUR) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "My Work Portfolio (Proof of Good Work)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraTextMuted)
                Text(text = "Upload 3 photos of your completed projects so hirers trust your skill.", fontSize = 12.sp, color = WorkoraTextMuted)

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(3) { index ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, WorkoraBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = WorkoraOrange, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Work ${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WorkoraTextDark)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Preferences & Actions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WorkoraTextMuted)

            OutlinedButton(
                onClick = onSwitchRole,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, WorkoraBorder)
            ) {
                Icon(imageVector = Icons.Default.SwapHoriz, tint = WorkoraNavy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Switch to ${if (role == UserRole.CUSTOMER) "Labour Mode" else "Hirer Mode"}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)
            }

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, tint = Color.White, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Logout", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = "Edit Profile Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = currentName, onValueChange = { currentName = it }, label = { Text("Full Name") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = currentPhone, onValueChange = { currentPhone = it }, label = { Text("Mobile Number") }, singleLine = true, shape = RoundedCornerShape(10.dp))
                    OutlinedTextField(value = currentLocation, onValueChange = { currentLocation = it }, label = { Text("Location / Address") }, singleLine = true, shape = RoundedCornerShape(10.dp))
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(WorkoraBgLight, shape = CircleShape),
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
