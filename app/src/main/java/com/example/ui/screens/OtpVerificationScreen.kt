package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun WorkoraRealOtpVerificationDialog(
    targetKey: String, 
    maskedTargetDisplay: String, 
    purposeTitle: String = "Verify OTP",
    onResendClick: () -> Unit, 
    onDismiss: () -> Unit, 
    onVerifiedSuccess: () -> Unit
) {
    val context = LocalContext.current
    var otpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Timer logic based on the UI design (00:58)
    var timeLeft by remember { mutableIntStateOf(58) }
    
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000L) // 1 second delay
            timeLeft--
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // This makes it full screen
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Back Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF0061FF), // Workora Blue
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = purposeTitle, 
                fontSize = 28.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = Color(0xFF0F172A)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subtitle with Masked Number
            Text(
                text = "Enter the 6-digit code sent to\n$maskedTargetDisplay", 
                fontSize = 15.sp, 
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 6-Digit OTP Input Boxes
            BasicTextField(
                value = otpInput,
                onValueChange = { 
                    if (it.length <= 6) {
                        otpInput = it.filter { c -> c.isDigit() }
                        errorMessage = null // Clear error when typing
                    } 
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(6) { index ->
                            val char = when {
                                index >= otpInput.length -> ""
                                else -> otpInput[index].toString()
                            }
                            val isFocused = otpInput.length == index
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = if (isFocused) Color(0xFF0061FF) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char, 
                                    fontSize = 24.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Display
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!, 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = Color(0xFFB42318), // Workora Red
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Resend OTP Timer Display (Matching the design)
            Text(
                text = if (timeLeft > 0) "Resend OTP (00:${timeLeft.toString().padStart(2, '0')})" else "Resend OTP Now",
                fontSize = 14.sp,
                color = if (timeLeft > 0) Color(0xFF64748B) else Color(0xFF0061FF),
                fontWeight = if (timeLeft > 0) FontWeight.Normal else FontWeight.Bold,
                modifier = Modifier.clickable(enabled = timeLeft == 0) {
                    if (timeLeft == 0) {
                        errorMessage = null
                        otpInput = ""
                        timeLeft = 58 // Reset the timer
                        onResendClick()
                        Toast.makeText(context, "OTP Resent!", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Verify Button
            Button(
                onClick = {
                    if (otpInput.length < 6) {
                        errorMessage = "Please enter all 6 digits"
                        return@Button
                    }
                    val verifyResult = WorkoraRealOtpEngine.verifyRealOtp(context, targetKey, otpInput)
                    if (verifyResult.first) {
                        onVerifiedSuccess()
                    } else {
                        errorMessage = verifyResult.second
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061FF))
            ) { 
                Text("Verify", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold) 
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Resend Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Didn't receive the code? ", fontSize = 14.sp, color = Color(0xFF64748B))
                Text(
                    text = "Resend",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (timeLeft > 0) Color.Gray else Color(0xFF0061FF),
                    modifier = Modifier.clickable(enabled = timeLeft == 0) { 
                        if (timeLeft == 0) {
                            errorMessage = null
                            otpInput = ""
                            timeLeft = 58
                            onResendClick() 
                            Toast.makeText(context, "OTP Resent!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}
