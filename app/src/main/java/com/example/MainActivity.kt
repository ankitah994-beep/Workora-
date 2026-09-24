// MainActivity.kt
// NOTE: change the package line below if your project uses a different package name.
// Requires the androidx.navigation:navigation-compose dependency.
package com.workora.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.workora.app.ui.theme.WorkoraTheme

private const val ROUTE_AUTH: String = "auth"
private const val ROUTE_WORKER_HOME: String = "worker_home"
private const val ROUTE_EMPLOYER_HOME: String = "employer_home"

// The crash report screen deliberately uses its own fixed colors (not WorkoraTheme),
// so a problem in the theme can never hide a crash report.
private val CrashBlue = Color(0xFF1565C0)
private val CrashDark = Color(0xFF111111)
private val CrashMuted = Color(0xFF616161)
private val CrashRed = Color(0xFFB00020)

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
                WorkoraTheme {
                    WorkoraApp()
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Crash report screen (plain white screen, independent of WorkoraTheme)
// ---------------------------------------------------------------------------

@Composable
private fun CrashReportScreen(
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

// ---------------------------------------------------------------------------
// App navigation (Auth, Worker Home, Employer Home)
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
                subtitle = "Find work that fits you.",
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
                subtitle = "Find the right people for your jobs.",
                name = sessionName,
                email = sessionEmail,
                phoneNumber = sessionPhoneNumber,
                role = sessionRole,
                onLogout = clearSessionAndLogout
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Home screen (shared by Worker Home and Employer Home)
// ---------------------------------------------------------------------------

@Composable
private fun RoleHomeScreen(
    title: String,
    subtitle: String,
    name: String,
    email: String,
    phoneNumber: String,
    role: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val displayName: String = name.ifEmpty { "Workora user" }
    val initial: String = displayName.first().uppercaseChar().toString()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Blue header band
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colors.primary,
                    shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column {
                Text(
                    text = "Workora",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onPrimary.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onPrimary.copy(alpha = 0.9f)
                )
            }
        }

        // Profile card
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleLarge,
                                color = colors.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = colors.primaryContainer
                            ) {
                                Text(
                                    text = role,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    DetailRow(label = "Email", value = email)
                    DetailRow(label = "Phone", value = phoneNumber)
                }
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
                .height(50.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text(
                text = "Log out",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value.ifEmpty { "Not provided" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoleHomeScreenPreview() {
    WorkoraTheme {
        RoleHomeScreen(
            title = "Worker Home",
            subtitle = "Find work that fits you.",
            name = "Test User",
            email = "test@example.com",
            phoneNumber = "9876543210",
            role = ROLE_WORKER,
            onLogout = {}
        )
    }
}
