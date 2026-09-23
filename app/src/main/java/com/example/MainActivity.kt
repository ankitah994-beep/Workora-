// MainActivity.kt
// NOTE: change the package line below if your project uses a different package name.
// Requires the androidx.navigation:navigation-compose dependency.
package com.workora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val ROUTE_AUTH: String = "auth"
private const val ROUTE_WORKER_HOME: String = "worker_home"
private const val ROUTE_EMPLOYER_HOME: String = "employer_home"

private val BrandColor = Color(0xFF1565C0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkoraApp()
        }
    }
}

private fun routeForRole(role: String): String {
    return if (role == ROLE_EMPLOYER) ROUTE_EMPLOYER_HOME else ROUTE_WORKER_HOME
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
                        navController.navigate(routeForRole(role)) {
                            popUpTo(ROUTE_AUTH) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onRegister = { name: String, email: String, phoneNumber: String, _: String, role: String ->
                        sessionName = name
                        sessionEmail = email
                        sessionPhoneNumber = phoneNumber
                        sessionRole = role
                        navController.navigate(routeForRole(role)) {
                            popUpTo(ROUTE_AUTH) { inclusive = true }
                            launchSingleTop = true
                        }
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
                    onLogout = {
                        sessionName = ""
                        sessionEmail = ""
                        sessionPhoneNumber = ""
                        sessionRole = ROLE_WORKER
                        navController.navigate(ROUTE_AUTH) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(ROUTE_EMPLOYER_HOME) {
                RoleHomeScreen(
                    title = "Employer Home",
                    name = sessionName,
                    email = sessionEmail,
                    phoneNumber = sessionPhoneNumber,
                    role = sessionRole,
                    onLogout = {
                        sessionName = ""
                        sessionEmail = ""
                        sessionPhoneNumber = ""
                        sessionRole = ROLE_WORKER
                        navController.navigate(ROUTE_AUTH) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
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
                color = BrandColor
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Name: $name",
                fontSize = 16.sp,
                color = Color(0xFF111111),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Email: $email",
                fontSize = 16.sp,
                color = Color(0xFF111111),
                textAlign = TextAlign.Center
            )
            if (phoneNumber.isNotBlank()) {
                Text(
                    text = "Phone: $phoneNumber",
                    fontSize = 16.sp,
                    color = Color(0xFF111111),
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = "Role: $role",
                fontSize = 16.sp,
                color = Color(0xFF111111),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColor,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Log Out")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoleHomeScreenPreview() {
    MaterialTheme(colorScheme = lightColorScheme(primary = BrandColor)) {
        RoleHomeScreen(
            title = "Worker Home",
            name = "Ravi Kumar",
            email = "ravi@example.com",
            phoneNumber = "9876543210",
            role = ROLE_WORKER,
            onLogout = {}
        )
    }
}
