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

// ✅ जो फाइल्स हमने बना ली हैं, उनके सही नाम अपडेट कर दिए
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.WelcomeScreen 
import com.example.ui.screens.RoleSelectionScreen // AccountSelectScreen की जगह 
import com.example.ui.screens.AuthScreen // LoginScreen की जगह
import com.example.ui.screens.WorkerProfileSetupScreen

// ⏳ जो फाइल्स अभी आगे बनानी हैं, उन्हें कमेंट कर दिया है ताकि लाल Error ना आए
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ClientProfileSetupScreen
import com.example.ui.screens.CustomerHomeScreen
import com.example.ui.screens.LabourDashboardScreen
import com.example.ui.screens.PostJobScreen

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
                    var currentRole by remember { mutableStateOf(authPrefs.getString("saved_user_role", "") ?: "") }
                    
                    var currentScreen by remember { mutableStateOf(if (isLoggedIn && currentRole.isNotEmpty()) "DASHBOARD" else "SPLASH") }

                    when (currentScreen) {
                        "SPLASH" -> {
                            SplashScreen(onSplashFinished = { currentScreen = "WELCOME" })
                        }
                        "WELCOME" -> {
                            WelcomeScreen(
                                onGetStarted = { currentScreen = "ROLE_SELECT" },
                                onLoginClick = { currentScreen = "LOGIN" }
                            )
                        }
                        "ROLE_SELECT" -> {
                            RoleSelectionScreen( 
                                onWorkerSelected = {
                                    currentRole = "LABOUR"
                                    authPrefs.edit().putString("saved_user_role", "LABOUR").apply()
                                    currentScreen = "WORKER_SETUP" // Worker रजिस्ट्रेशन पर भेजो
                                },
                                onCustomerSelected = {
                                    currentRole = "CUSTOMER"
                                    authPrefs.edit().putString("saved_user_role", "CUSTOMER").apply()
                                    currentScreen = "LOGIN" 
                                }
                            )
                        }
                        "LOGIN" -> {
                            AuthScreen( 
                                onLoginClick = { 
                                    isLoggedIn = true
                                    authPrefs.edit().putBoolean("is_logged_in", true).apply()
                                    currentScreen = "DASHBOARD"
                                }
                            )
                        }
                        "WORKER_SETUP" -> {
                            WorkerProfileSetupScreen(
                                onSetupComplete = { currentScreen = "DASHBOARD" },
                                onBack = { currentScreen = "ROLE_SELECT" }
                            )
                        }
                        "DASHBOARD" -> {
                            // जब तक Dashboard फाइल नहीं बनती, ऐप यहाँ रुकेगा ताकि क्रैश ना हो
                            androidx.compose.material3.Text("Home Dashboard Coming Soon...", modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
