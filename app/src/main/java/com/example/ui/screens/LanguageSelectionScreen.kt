package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WorkoraBgLight
import com.example.ui.theme.WorkoraBorder
import com.example.ui.theme.WorkoraNavy
import com.example.ui.theme.WorkoraOrange
import com.example.ui.theme.WorkoraTextDark
import com.example.ui.theme.WorkoraTextMuted

@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: () -> Unit = {}
) {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }

    WorkoraWelcomeContent(
        onGetStartedClick = {
            authPrefs.edit().putString("welcome_next_action", "SIGNUP").apply()
            onLanguageSelected()
        },
        onLoginClick = {
            authPrefs.edit().putString("welcome_next_action", "LOGIN").apply()
            onLanguageSelected()
        }
    )
}

/**
 * आपकी अपलोड की गई इमेज के जैसा हूबहू WORKORA Official Logo
 * (अगर drawable में workora_logo.png होगा तो सीधे वही इमेज दिखाएगा, वरना हूबहू वही डिज़ाइन बनाएगा)
 */
@Composable
fun WorkoraOfficialBrandLogo() {
    val context = LocalContext.current
    val logoResId = remember {
        context.resources.getIdentifier("workora_logo", "drawable", context.packageName)
    }

    Card(
        modifier = Modifier.size(220.dp),
        shape = RoundedCornerShape(38.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, WorkoraBorder)
    ) {
        if (logoResId != 0) {
            // अगर आपने res/drawable/workora_logo.png डाला है तो सीधे आपकी असली PNG फोटो दिखेगी
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = "Workora Official Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // हूबहू आपकी इमेज जैसा Custom Vector Logo (3 Workers + Blue/Orange 'W' Handshake + WORKORA)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 160.dp, height = 112.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // 3 Workers inside the Blue Arch (Wrench, Hard-Hat, Paint Roller)
                    Row(
                        modifier = Modifier
                            .padding(top = 18.dp)
                            .width(104.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color(0xFF062B6E),
                            modifier = Modifier.size(24.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = Color(0xFFFF8C00),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.FormatPaint,
                            contentDescription = null,
                            tint = Color(0xFF062B6E),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Blue Arch + Blue & Orange 'W' Handshake Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val navy = Color(0xFF062B6E)
                        val orange = Color(0xFFFF8C00)

                        // Top Navy Blue Arch framing the 3 workers
                        drawArc(
                            color = navy,
                            startAngle = 185f,
                            sweepAngle = 170f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.19f, size.height * 0.04f),
                            size = Size(size.width * 0.62f, size.height * 0.68f),
                            style = Stroke(width = 11f, cap = StrokeCap.Round)
                        )

                        // Left Navy Blue 'V' of the 'W' + Handshake arm
                        val leftWPath = Path().apply {
                            moveTo(size.width * 0.08f, size.height * 0.42f)
                            lineTo(size.width * 0.33f, size.height * 0.90f)
                            lineTo(size.width * 0.50f, size.height * 0.54f)
                            lineTo(size.width * 0.57f, size.height * 0.66f)
                        }
                        drawPath(
                            path = leftWPath,
                            color = navy,
                            style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Right Orange 'V' of the 'W' + Handshake clasp
                        val rightWPath = Path().apply {
                            moveTo(size.width * 0.44f, size.height * 0.62f)
                            lineTo(size.width * 0.51f, size.height * 0.54f)
                            lineTo(size.width * 0.67f, size.height * 0.90f)
                            lineTo(size.width * 0.92f, size.height * 0.42f)
                        }
                        drawPath(
                            path = rightWPath,
                            color = orange,
                            style = Stroke(width = 24f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Orange Handshake Fingers Grip dots in center
                        drawCircle(
                            color = orange,
                            radius = 7f,
                            center = Offset(size.width * 0.45f, size.height * 0.74f)
                        )
                        drawCircle(
                            color = orange,
                            radius = 7f,
                            center = Offset(size.width * 0.49f, size.height * 0.79f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // WORK (Navy) + ORA (Orange)
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF062B6E), fontWeight = FontWeight.ExtraBold)) {
                            append("WORK")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFFFF8C00), fontWeight = FontWeight.ExtraBold)) {
                            append("ORA")
                        }
                    },
                    fontSize = 26.sp,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Divider Line with Orange Dot in the Middle (— • —)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(0.82f)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.5.dp)
                            .background(Color(0xFF062B6E))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF8C00))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.5.dp)
                            .background(Color(0xFF062B6E))
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // FIND. HIRE. WORK.
                Text(
                    text = "FIND. HIRE. WORK.",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.2.sp,
                    color = Color(0xFF062B6E)
                )
            }
        }
    }
}

@Composable
fun WorkoraWelcomeContent(
    onGetStartedClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var showTermsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkoraBgLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        // 1. [ Workora Official Logo Card ]
        WorkoraOfficialBrandLogo()

        Spacer(modifier = Modifier.height(22.dp))

        // 2. [ Customer <-> Worker Illustration ]
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.5.dp, WorkoraBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Customer Node
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(WorkoraNavy.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Customer",
                                tint = WorkoraNavy,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Customer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraNavy
                        )
                        Text(
                            text = "Hire Workers",
                            fontSize = 11.sp,
                            color = WorkoraTextMuted
                        )
                    }

                    // Bi-directional Arrow Connection
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(WorkoraOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Connect",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF16A34A).copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Direct Connect",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }

                    // Worker Node
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(WorkoraOrange.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Worker",
                                tint = WorkoraOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Worker",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = WorkoraOrange
                        )
                        Text(
                            text = "Find Daily Jobs",
                            fontSize = 11.sp,
                            color = WorkoraTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Work When You Want. Hire When You Need.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Work When You Want.\nHire When You Need.",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WorkoraTextDark,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Connect with local workers or find work opportunities for your everyday needs.",
                fontSize = 14.sp,
                color = WorkoraTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        // 4. [ Get Started ] + Already have an account? Login + Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WorkoraOrange)
            ) {
                Text(
                    text = "Get Started",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = WorkoraTextMuted
                )
                Text(
                    text = "Login",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WorkoraNavy,
                    modifier = Modifier
                        .clickable { onLoginClick() }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Terms & Conditions • Privacy",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = WorkoraTextMuted,
                modifier = Modifier
                    .clickable { showTermsDialog = true }
                    .padding(6.dp)
            )
        }
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(text = "Terms & Privacy Policy", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Workora connects local customers directly with skilled workers and daily wage labourers.\n\n• 100% Verified Profiles & Work Portfolio\n• Direct Customer-to-Worker Communication\n• Secure Cloud Data Protection (workora-d8b51)",
                    fontSize = 13.sp,
                    color = WorkoraTextDark
                )
            },
            confirmButton = {
                OutlinedButton(onClick = { showTermsDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = WorkoraNavy)
                }
            }
        )
    }
}
