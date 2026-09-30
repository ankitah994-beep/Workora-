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
import com.example.ui.screens.AdminDashboardScreen
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

                    // App States
                    var isLoggedIn by remember { mutableStateOf(authPrefs.getBoolean("is_logged_in", false)) }
                    var currentRole by remember { mutableStateOf(authPrefs.getString("saved_user_role", "CUSTOMER") ?: "CUSTOMER") }
                    
                    // App starts with SPLASH
                    var currentScreen by remember { mutableStateOf(if (isLoggedIn) "DASHBOARD" else "SPLASH") }

                    when (currentScreen) {
                        "SPLASH" -> {
                            SplashScreen(
                                onSplashFinished = {
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
                                    
                                    // 🔥 ADMIN CHECK LOGIC 🔥
                                    // अगर ईमेल आपका है, तो सीधा एडमिन डैशबोर्ड खुलेगा
                                    if (email.trim().equals("ankitah994@gmail.com", ignoreCase = true) || 
                                        email.trim().equals("admin@workora.com", ignoreCase = true)) {
                                        currentRole = "ADMIN"
                                        authPrefs.edit().putString("saved_user_role", "ADMIN").apply()
                                        currentScreen = "ADMIN_DASHBOARD"
                                    } else {
                                        currentScreen = "DASHBOARD"
                                    }
                                },
                                onNavigateToSignUp = { },
                                onBack = { currentScreen = "WELCOME" }
                            )
                        }
                        "ADMIN_DASHBOARD" -> {
                            // आपका नया एडमिन पैनल यहाँ से खुलेगा
                            AdminDashboardScreen(
                                onNavigateToUsers = { /* Handle Users */ },
                                onNavigateToJobs = { /* Handle Jobs */ },
                                onNavigateToReports = { /* Handle Reports */ },
                                onNavigateToAppControl = { /* Handle App Control */ },
                                onNavigateToStateControl = { /* Handle State Control */ }
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
                            if (currentRole == "ADMIN") {
                                currentScreen = "ADMIN_DASHBOARD" // Safety check
                            } else if (currentRole == "LABOUR") {
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
