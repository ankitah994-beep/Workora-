package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OtpNavy = Color(0xFF083D91)
private val OtpOrange = Color(0xFFFF8C00)
private val OtpRed = Color(0xFFB42318)
private val OtpBg = Color(0xFFF8FAFC)

@Composable
fun OtpVerificationScreen(
    targetKey: String = "1234567890",
    maskedTargetDisplay: String = "1234567890",
    purposeTitle: String = "Verify OTP",
    onResendClick: () -> Unit = {},
    onVerifiedSuccess: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var otpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OtpBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OtpNavy)
            }
            Text(
                text = purposeTitle,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OtpNavy
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Enter the 6-digit OTP sent to $maskedTargetDisplay",
                fontSize = 14.sp,
                color = Color(0xFF475467),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OtpRed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = otpInput,
                onValueChange = { if (it.length <= 6) otpInput = it.filter { c -> c.isDigit() } },
                label = { Text("Enter 6-Digit OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Resend OTP",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = OtpNavy,
                modifier = Modifier
                    .align(Alignment.Start)
                    .clickable {
                        errorMessage = null
                        otpInput = ""
                        onResendClick()
                    }
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Assuming WorkoraRealOtpEngine is accessible globally
                    val verifyResult = WorkoraRealOtpEngine.verifyRealOtp(context, targetKey, otpInput)
                    if (verifyResult.first) {
                        Toast.makeText(context, "Verification Successful ✓", Toast.LENGTH_SHORT).show()
                        onVerifiedSuccess()
                    } else {
                        errorMessage = verifyResult.second
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OtpOrange)
            ) {
                Text("Verify OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
