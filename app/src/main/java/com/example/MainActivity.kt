// MainActivity.kt
// NOTE: change the package line below if your project uses a different package name.
// Requires the androidx.navigation:navigation-compose dependency.
package com.workora.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val ROUTE_AUTH: String = "auth"
private const val ROUTE_WORKER_HOME: String = "worker_home"
private const val ROUTE_EMPLOYER_HOME: String = "employer_home"

private val BrandColor = Color(0xFF1565C0)
private val DarkText = Color(0xFF111111)
private val MutedText = Color(0xFF616161)
private val ErrorRed = Color(0xFFB00020)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // VERY FIRST: look for a crash report saved by WorkoraApplication (plain SharedPreferences).
        val pendingReport: String? = CrashStore.read(applicationContext)

        super.onCreate(savedInstanceState)

        setContent {
            var crashReport by rememberSaveable { mutableStateOf<String?>(pendingReport) }
            val report: String? = crashReport

            if (report != null) {
                CrashReportScreen(
                    report = report,
                    onContinue = {
                        // Clear the saved report, then continue into the normal app.
                        CrashStore.clear(applicationContext)
                        crashReport = null
                    }
                )
            } else {
                WorkoraApp()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Crash report screen (plain white screen)
// ---------------------------------------------------------------------------

@Composable
private fun CrashReportScreen(
    report: String,
    onContinue: () -> Unit
) {
    MaterialTheme(colorScheme = lightColorScheme(primary = BrandColor)) {
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
                    color = ErrorRed
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Use Copy or Share and send the full text below. Tap Continue to clear it and open the app.",
                    fontSize = 14.sp,
                    color = MutedText
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
                            containerColor = BrandColor,
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
                        color = DarkText
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// App navigation
// ---------------------------------------------------------------------------

// Roles are plain Strings: "WORKER" or "EMPLOYER" (ROLE_WORKER / ROLE_EMPLOYER live in AuthScreen.kt).
private fun routeForRole(role: String): String {
    return if (role == ROLE_EMPLOYER) ROUTE_EMPLOYER_HOME else ROUTE_WORKER_HOME
}

// navigate(...) { popUpTo(...) { inclusive = ... }; launchSingleTop = ... } only resolves inside
// the navigate lambda (NavOptionsBuilder), so the calls are kept inside these two helpers.
private fun NavHostController.goToHome(role: String) {
    navigate(routeForRole(role)) {
        popUpTo(ROUTE_AUTH) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.goToAuthAndClearBackStack() {
    navigate(ROUTE_AUTH) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun WorkoraApp() {
    MaterialTheme(colorScheme = lightColorScheme(primary = BrandColor)) {
        val navController = rememberNavController()

        // Session values mirror the User model fields: name, email, phoneNumber, role.
        // Replace this in-memory session with your ViewModel/repository call when the backend is wired in.
        var sessionName by rememberSaveable { mutableStateOf("") }
        var sessionEmail by rememberSaveable { mutableStateOf("") }
        var sessionPhoneNumber by rememberSaveable { mutableStateOf("") }
        var sessionRole by rememberSaveable { mutableStateOf(ROLE_WORKER) }

        val clearSessionAndLogout: () -> Unit = {
            sessionName = ""
            sessionEmail = ""
            sessionPhoneNumber = ""
            sessionRole = ROLE_WORKER
            navController.goToAuthAndClearBackStack()
        }

        NavHost(
            navController = navController,
            startDestination = ROUTE_AUTH
        ) {
            composable(ROUTE_AUTH) {
                AuthScreen(
                    onLogin = { email: String, _: String, role: String ->
                        sessionName = email.substringBefore("@")
                        sessionEmail = email
                        sessionPhoneNumber = ""
                        sessionRole = role
                        navController.goToHome(role)
                    },
                    onRegister = { name: String, email: String, phoneNumber: String, _: String, role: String ->
                        sessionName = name
                        sessionEmail = email
                        sessionPhoneNumber = phoneNumber
                        sessionRole = role
                        navController.goToHome(role)
                    }
                )
            }

            composable(ROUTE_WORKER_HOME) {
                RoleHomeScreen(
                    title = "Worker Home",
                    name = sessionName,
                    email = sessionEmail,
                    phoneNumber = sessionPhoneNumber,
                    role = sessionRole,
                    onLogout = clearSessionAndLogout
                )
            }

            composable(ROUTE_EMPLOYER_HOME) {
                RoleHomeScreen(
                    title = "Employer Home",
                    name = sessionName,
                    email = sessionEmail,
                    phoneNumber = sessionPhoneNumber,
                    role = sessionRole,
                    onLogout = clearSessionAndLogout
                )
            }
        }
    }
}

@Composable
private fun RoleHomeScreen(
    title: String,
    name: String,
    email: String,
    phoneNumber: String,
    role: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = BrandColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileLine(label = "Name", value = name)
            ProfileLine(label = "Email", value = email)
            ProfileLine(label = "Phone", value = phoneNumber)
            ProfileLine(label = "Role", value = role)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColor,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Log out")
            }
        }
    }
}

@Composable
private fun ProfileLine(label: String, value: String) {
    Text(
        text = "$label: ${value.ifEmpty { "-" }}",
        fontSize = 16.sp,
        color = if (value.isEmpty()) MutedText else DarkText,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun RoleHomeScreenPreview() {
    RoleHomeScreen(
        title = "Worker Home",
        name = "Test User",
        email = "test@example.com",
        phoneNumber = "9876543210",
        role = ROLE_WORKER,
        onLogout = {}
    )
}
