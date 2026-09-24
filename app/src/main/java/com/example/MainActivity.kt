package com.workora.app

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// This screen deliberately uses its own fixed colors (not the app theme),
// so a problem in the theme can never hide a crash report.
private val CrashBlue = Color(0xFF1565C0)
private val CrashDark = Color(0xFF111111)
private val CrashMuted = Color(0xFF616161)
private val CrashRed = Color(0xFFB00020)

@Composable
fun CrashReportScreen(
    report: String,
    onContinue: () -> Unit
) {
    MaterialTheme(colorScheme = lightColorScheme(primary = CrashBlue)) {
        val context = LocalContext.current
        val clipboard = LocalClipboardManager.current

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Workora crashed last time",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrashRed
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Use Copy or Share and send the full text below. Tap Continue to clear it and open the app.",
                    fontSize = 14.sp,
                    color = CrashMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { clipboard.setText(AnnotatedString(report)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Copy")
                    }
                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND)
                            sendIntent.type = "text/plain"
                            sendIntent.putExtra(Intent.EXTRA_TEXT, report)
                            context.startActivity(Intent.createChooser(sendIntent, "Share crash report"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Share")
                    }
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrashBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Continue")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = report,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CrashDark
                    )
                }
            }
        }
    }
}
