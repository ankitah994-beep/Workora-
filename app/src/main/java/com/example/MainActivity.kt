package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.screens.AccountSelectScreen
import com.example.ui.screens.ClientProfileSetupScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.LabourDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PostJobScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.WorkerProfileSetupScreen
import com.example.ui.theme.WorkoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    val context = applicationContext
                    val authPrefs = remember { context.getSharedPreferences("workora_real_auth", Context.MODE_PRIVATE) }

                    var isLoggedIn by remember { mutableStateOf(authPrefs.getBoolean("is_logged_in", false)) }
                    var currentRole by remember { mutableStateOf(authPrefs.getString("saved_user_role", "CUSTOMER") ?: "CUSTOMER") }
                    
                    // App starts with SPLASH
                    var currentScreen by remember { mutableStateOf(if (isLoggedIn) "DASHBOARD" else "SPLASH") }

                    when (currentScreen) {
                        "SPLASH" -> {
                            SplashScreen(
                                onSplashFinished = {
                                    // Fix: Now it goes to Welcome Screen
                                    currentScreen = "WELCOME" 
                                }
                            )
                        }
                        "WELCOME" -> {
                            WelcomeScreen(
                                onGetStarted = { currentScreen = "ROLE_SELECT" },
                                onLoginClick = { currentScreen = "LOGIN" }
                            )
                        }
                        "ROLE_SELECT" -> {
                            AccountSelectScreen(
                                onSelectRole = { role ->
                                    currentRole = role.name
                                    authPrefs.edit().putString("saved_user_role", role.name).apply()
                                    currentScreen = "LOGIN"
                                },
                                onOpenAdmin = { },
                                onLogout = {
                                    isLoggedIn = false
                                    authPrefs.edit().putBoolean("is_logged_in", false).apply()
                                    currentScreen = "WELCOME"
                                }
                            )
                        }
                        "LOGIN" -> {
                            LoginScreen(
                                onLogin = { email, pass ->
                                    isLoggedIn = true
                                    authPrefs.edit().putBoolean("is_logged_in", true).apply()
                                    currentScreen = "DASHBOARD"
                                },
                                onNavigateToSignUp = { },
                                onBack = { currentScreen = "WELCOME" }
                            )
                        }
                        "WORKER_SETUP" -> {
                            WorkerProfileSetupScreen(
                                onSetupComplete = { currentScreen = "DASHBOARD" },
                                onBack = { currentScreen = "ROLE_SELECT" }
                            )
                        }
                        "CLIENT_SETUP" -> {
                            ClientProfileSetupScreen(
                                onSetupComplete = { currentScreen = "DASHBOARD" },
                                onBack = { currentScreen = "ROLE_SELECT" }
                            )
                        }
                        "POST_JOB" -> {
                            PostJobScreen(
                                onBack = { currentScreen = "DASHBOARD" },
                                onJobPosted = { currentScreen = "DASHBOARD" }
                            )
                        }
                        "DASHBOARD" -> {
                            if (currentRole == "LABOUR") {
                                LabourDashboardScreen()
                            } else {
                                CustomerHomeScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
