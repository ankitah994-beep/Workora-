package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileSetupScreen(
    onSetupComplete: () -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var wage by remember { mutableStateOf("") }
    
    // Auto-suggest variables
    var locationExpanded by remember { mutableStateOf(false) }
    var locationText by remember { mutableStateOf("") }
    val locationOptions = listOf("Bhopal", "Indore", "Silwani", "Raisen", "Delhi")

    var expExpanded by remember { mutableStateOf(false) }
    var expText by remember { mutableStateOf("") }
    val expOptions = listOf("Fresher", "1 Year", "2 Years", "3 Years", "4 Years", "5+ Years")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color(0xFF0061FF),
            modifier = Modifier.size(28.dp).clickable { onBack() }
        )
        Spacer(modifier = Modifier.height(20.dp))
        
        OutlinedTextField(
            value = fullName, onValueChange = { fullName = it },
            label = { Text("Full Name") }, leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = phone, onValueChange = { phone = it },
            label = { Text("Phone Number") }, leadingIcon = { Icon(Icons.Default.Phone, null) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email (optional)") }, leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Location Dropdown
        ExposedDropdownMenuBox(
            expanded = locationExpanded,
            onExpandedChange = { locationExpanded = !locationExpanded }
        ) {
            OutlinedTextField(
                value = locationText,
                onValueChange = { locationText = it },
                label = { Text("Location / City") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0061FF))
            )
            ExposedDropdownMenu(
                expanded = locationExpanded,
                onDismissRequest = { locationExpanded = false }
            ) {
                locationOptions.filter { it.contains(locationText, ignoreCase = true) }.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { locationText = option; locationExpanded = false }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Work Category", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Placeholder for Work Category Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061FF).copy(alpha = 0.1f), contentColor = Color(0xFF0061FF))) { Text("Painter") }
            Button(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF64748B))) { Text("Plumber") }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Experience Dropdown
            ExposedDropdownMenuBox(
                expanded = expExpanded,
                onExpandedChange = { expExpanded = !expExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = expText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Experience") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expExpanded,
                    onDismissRequest = { expExpanded = false }
                ) {
                    expOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { expText = option; expExpanded = false }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = wage, onValueChange = { wage = it },
                label = { Text("Daily Wage (₹)") },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        Button(
            onClick = onSetupComplete,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061FF))
        ) {
            Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}
