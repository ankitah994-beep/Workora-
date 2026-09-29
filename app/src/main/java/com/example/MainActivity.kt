package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.model.UserRole
import com.example.ui.screens.AccountSelectScreen
import com.example.ui.screens.CustomerDashboardScreen
import com.example.ui.screens.LabourDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.screens.SplashScreen

enum class ScreenState {
    SPLASH, ACCOUNT_SELECTION, LOGIN, SIGN_UP, CUSTOMER_HOME, LABOUR_HOME, PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WorkoraApp()
                }
            }
        }
    }
}

@Composable
fun WorkoraApp() {
    val context = LocalContext.current
    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }
    
    var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
    var currentRole by remember { mutableStateOf(UserRole.CUSTOMER) }

    LaunchedEffect(Unit) {
        val savedRoleStr = authPrefs.getString("saved_user_role", "CUSTOMER")
        currentRole = if (savedRoleStr == "LABOUR") UserRole.LABOUR else UserRole.CUSTOMER
    }

    // ✅ BACK BUTTON HANDLER (Fixes the issue of app closing unexpectedly)
    BackHandler {
        when (currentScreen) {
            ScreenState.PROFILE -> {
                currentScreen = if (currentRole == UserRole.CUSTOMER) ScreenState.CUSTOMER_HOME else ScreenState.LABOUR_HOME
            }
            ScreenState.SIGN_UP -> {
                currentScreen = ScreenState.LOGIN
            }
            ScreenState.LOGIN -> {
                currentScreen = ScreenState.ACCOUNT_SELECTION
            }
            ScreenState.CUSTOMER_HOME, ScreenState.LABOUR_HOME, ScreenState.ACCOUNT_SELECTION -> {
                // Let the system handle exit if we are at home or account selection
                val activity = context as? ComponentActivity
                activity?.finish()
            }
            else -> {
                val activity = context as? ComponentActivity
                activity?.finish()
            }
        }
    }

    // ✅ SCREEN ROUTING (Matches all parameters exactly 100%)
    when (currentScreen) {
        ScreenState.SPLASH -> {
            SplashScreen(
                onSplashFinished = {
                    val isLoggedIn = authPrefs.getBoolean("is_logged_in", false)
                    if (isLoggedIn) {
                        currentScreen = if (currentRole == UserRole.CUSTOMER) ScreenState.CUSTOMER_HOME else ScreenState.LABOUR_HOME
                    } else {
                        currentScreen = ScreenState.ACCOUNT_SELECTION
                    }
                }
            )
        }
        ScreenState.ACCOUNT_SELECTION -> {
            AccountSelectScreen(
                onSelectRole = { role ->
                    currentRole = role
                    currentScreen = ScreenState.LOGIN
                },
                onRoleSelected = { role ->
                    currentRole = role
                    currentScreen = ScreenState.LOGIN
                },
                toastMessage = null,
                onOpenAdmin = {
                    Toast.makeText(context, "Admin Access Granted", Toast.LENGTH_SHORT).show()
                },
                onLogout = {
                    authPrefs.edit().clear().apply()
                }
            )
        }
        ScreenState.LOGIN -> {
            LoginScreen(
                onLogin = { email, password ->
                    val savedRole = authPrefs.getString("saved_user_role", "CUSTOMER")
                    currentRole = if (savedRole == "LABOUR") UserRole.LABOUR else UserRole.CUSTOMER
                    currentScreen = if (currentRole == UserRole.CUSTOMER) ScreenState.CUSTOMER_HOME else ScreenState.LABOUR_HOME
                },
                onNavigateToSignUp = {
                    currentScreen = ScreenState.SIGN_UP
                },
                onNavigateToRegister = {
                    currentScreen = ScreenState.SIGN_UP
                },
                toastMessage = null,
                onBack = {
                    currentScreen = ScreenState.ACCOUNT_SELECTION
                }
            )
        }
        ScreenState.SIGN_UP -> {
            SignUpScreen(
                onNavigateToLogin = {
                    currentScreen = ScreenState.LOGIN
                },
                onSignUpSuccess = { email, password ->
                    val savedRole = authPrefs.getString("saved_user_role", "CUSTOMER")
                    currentRole = if (savedRole == "LABOUR") UserRole.LABOUR else UserRole.CUSTOMER
                    currentScreen = if (currentRole == UserRole.CUSTOMER) ScreenState.CUSTOMER_HOME else ScreenState.LABOUR_HOME
                }
            )
        }
        ScreenState.CUSTOMER_HOME -> {
            CustomerDashboardScreen(
                onNavigateToSearch = {
                    currentScreen = ScreenState.PROFILE
                },
                onOpenChat = {
                    Toast.makeText(context, "Live Chat Opening...", Toast.LENGTH_SHORT).show()
                }
            )
        }
        ScreenState.LABOUR_HOME -> {
            LabourDashboardScreen(
                onOpenChat = {
                    Toast.makeText(context, "Live Chat Opening...", Toast.LENGTH_SHORT).show()
                },
                onCompleteJob = { jobId ->
                    Toast.makeText(context, "Job Marked as Complete ✓", Toast.LENGTH_SHORT).show()
                },
                onNavigateProfile = {
                    currentScreen = ScreenState.PROFILE
                }
            )
        }
        ScreenState.PROFILE -> {
            ProfileScreen(
                role = currentRole,
                onBack = {
                    currentScreen = if (currentRole == UserRole.CUSTOMER) ScreenState.CUSTOMER_HOME else ScreenState.LABOUR_HOME
                },
                onSwitchRole = {
                    currentRole = if (currentRole == UserRole.CUSTOMER) UserRole.LABOUR else UserRole.CUSTOMER
                    authPrefs.edit().putString("saved_user_role", currentRole.name).apply()
                    currentScreen = if (currentRole == UserRole.CUSTOMER) ScreenState.CUSTOMER_HOME else ScreenState.LABOUR_HOME
                    Toast.makeText(context, "Role Switched to ${currentRole.name}", Toast.LENGTH_SHORT).show()
                },
                onLogout = {
                    authPrefs.edit().clear().apply()
                    context.getSharedPreferences("workora_real_profile", Context.MODE_PRIVATE).edit().clear().apply()
                    currentScreen = ScreenState.ACCOUNT_SELECTION
                }
            )
        }
    }
}
