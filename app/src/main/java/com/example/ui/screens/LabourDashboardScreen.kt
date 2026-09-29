package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkoraNavy = Color(0xFF083D91)
private val WorkoraOrange = Color(0xFFFF8C00)
private val WorkoraGreen = Color(0xFF22A06B)
private val WorkoraRed = Color(0xFFB42318)

@Composable
fun LabourDashboardScreen(
    onOpenChat: () -> Unit = {},
    onCompleteJob: (String) -> Unit = {},
    onNavigateProfile: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(bottom = 60.dp) // Bottom nav padding
    ) {
        // ✅ NEW: WORKER MODE BADGE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF7ED)) // Light Orange
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👷‍♂️ WORKER MODE (कारीगर)",
                color = WorkoraOrange, 
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WorkoraNavy)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "मेरा काम (My Work)",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "अपनी उपलब्धता, बुकिंग और रेटिंग मैनेज करें",
                    color = Color(0xFFE2E8F0),
                    fontSize = 12.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Availability Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, WorkoraGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("मेरी पोस्ट की गई उपलब्धता", fontWeight = FontWeight.Bold, color = WorkoraNavy)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("सक्रिय", color = WorkoraGreen, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("राजमिस्त्री • ₹500/day • 5 Years Experience", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("📍 Bhopal, Madhya Pradesh (Max 15 KM)", fontSize = 12.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, WorkoraNavy)
                        ) {
                            Text("बदलें (Edit)", color = WorkoraNavy, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WorkoraRed)
                        ) {
                            Text("उपलब्धता रोकें ✕", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Text("अप्लाई किए गए काम (2)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WorkoraNavy)

            // Applied Job Item 1
            JobAppliedCard(
                jobTitle = "Hire Sunil Kumar -...",
                jobRate = "₹850/दिन",
                onOpenChat = onOpenChat,
                onCompleteJob = { onCompleteJob("job1") }
            )

            // Applied Job Item 2
            JobAppliedCard(
                jobTitle = "raja",
                jobRate = "₹600/दिन",
                location = "Silwani, Raisen (MP)",
                onOpenChat = onOpenChat,
                onCompleteJob = { onCompleteJob("job2") }
            )
        }
    }
}

@Composable
fun JobAppliedCard(
    jobTitle: String,
    jobRate: String,
    location: String = "Main Road, Sector 12",
    onOpenChat: () -> Unit,
    onCompleteJob: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF22A06B))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE0E7FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = WorkoraNavy)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(jobTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text("राजमिस्त्री • 1 Worker(s) • 2 Days", fontSize = 12.sp, color = Color.DarkGray)
                    Text("📍 $location", fontSize = 11.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(jobRate, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = WorkoraNavy)
                    Text("9:00 AM - 6:00 PM", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // FIX: LIVE CHAT AND COMPLETE BUTTON ADDED
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("रेटिंग ★", fontSize = 11.sp, color = WorkoraNavy)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = { onOpenChat() }, // Fixed: Live Chat opens directly, no Toast
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraGreen)
                ) {
                    Text("मैसेज ✓", fontSize = 11.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = { onCompleteJob() }, // Fixed: Complete button added
                    modifier = Modifier.weight(1.1f).height(34.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraNavy)
                ) {
                    Text("पूरा हुआ ✓", fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WorkoraRed)
                ) {
                    Text("वापस लें ✕", fontSize = 11.sp, color = Color.White, maxLines = 1)
                }
            }
        }
    }
}
