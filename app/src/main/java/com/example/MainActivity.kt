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
import com.example.ui.screens.CustomerDashboardScreen
import com.example.ui.screens.LabourDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PostJobScreen
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

                    // App Navigation States
                    var isLoggedIn by remember { mutableStateOf(authPrefs.getBoolean("is_logged_in", false)) }
                    var currentRole by remember { mutableStateOf(authPrefs.getString("saved_user_role", "CUSTOMER")) }
                    var currentScreen by remember { mutableStateOf(if (isLoggedIn) "DASHBOARD" else "ROLE_SELECT") }

                    when (currentScreen) {
                        "ROLE_SELECT" -> {
                            AccountSelectScreen(
                                onSelectRole = { role ->
                                    currentRole = role.name
                                    currentScreen = "LOGIN"
                                },
                                onOpenAdmin = {
                                    // Admin panel handled inside AccountSelectScreen
                                },
                                onLogout = {
                                    isLoggedIn = false
                                    authPrefs.edit().putBoolean("is_logged_in", false).apply()
                                    currentScreen = "ROLE_SELECT"
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
                                onNavigateToSignUp = {
                                    // Registration handled within LoginScreen via toggle
                                },
                                onBack = {
                                    currentScreen = "ROLE_SELECT"
                                }
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
                                onJobPosted = { currentScreen = "DASHBOARD" } // <-- ERROR FIXED HERE
                            )
                        }
                        "DASHBOARD" -> {
                            if (currentRole == "LABOUR") {
                                LabourDashboardScreen()
                            } else {
                                CustomerHomeScreen()
                                    onPostJobClick = { currentScreen = "POST_JOB" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
