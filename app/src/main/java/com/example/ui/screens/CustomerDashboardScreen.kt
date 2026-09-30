package com.example.ui.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

private val WorkoraBlue = Color(0xFF0061FF)
private val TextDark = Color(0xFF0F172A)
private val TextGray = Color(0xFF64748B)
private val BgColor = Color(0xFFF8FAFC)

private fun decodeCustImage(base64Str: String): ImageBitmap? {
    if (base64Str.isBlank()) return null
    return try {
        val bytes = Base64.decode(base64Str, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) { null }
}

@Composable
fun CustomerDashboardScreen(onPostJobClick: () -> Unit = {}) {
    val context = LocalContext.current
    val profilePrefs = remember { context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE) }
    val userName = profilePrefs.getString("user_name", "Client") ?: "Client"
    val userLocation = profilePrefs.getString("user_location", "Silwani, MP") ?: "Silwani, MP"
    val photoB64 = profilePrefs.getString("profile_photo_base64", "") ?: ""
    
    var selectedBottomTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = { CustomerBottomNav(selectedBottomTab) { selectedBottomTab = it } },
        containerColor = BgColor
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            CustomerHeader(userName, userLocation, photoB64)
            CustomerContent(onPostJobClick)
        }
    }
}

@Composable
private fun CustomerHeader(name: String, location: String, photoB64: String) {
    val profileBmp = remember(photoB64) { decodeCustImage(photoB64) }
    Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 24.dp, vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        if (profileBmp != null) Image(bitmap = profileBmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(50.dp).clip(CircleShape))
        else Box(modifier = Modifier.size(50.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = TextGray) }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = "Hello, $name 👋", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = location, fontSize = 13.sp, color = TextGray)
            }
        }
    }
}

@Composable
private fun CustomerContent(onPostJobClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WorkoraBlue)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Need work done?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Post a job and hire trusted workers nearby.", fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onPostJobClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White), shape = RoundedCornerShape(8.dp)) {
                        Text("Post Job", color = WorkoraBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Text("Top Workers Near You", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(50.dp).background(Color(0xFFE2E8F0), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.PersonOutline, contentDescription = null, tint = TextGray) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rajesh (Carpenter)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("₹500/day", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = WorkoraBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerBottomNav(selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
        val items = listOf("Home" to Icons.Default.Home, "Search" to Icons.Default.Search, "Profile" to Icons.Default.PersonOutline)
        items.forEachIndexed { index, pair ->
            NavigationBarItem(
                icon = { Icon(pair.second, contentDescription = pair.first) },
                label = { Text(pair.first, fontSize = 11.sp) },
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = WorkoraBlue, unselectedIconColor = TextGray, selectedTextColor = WorkoraBlue, unselectedTextColor = TextGray)
            )
        }
    }
}
