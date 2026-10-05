package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Jitni bhi screens ban chuki hain, unhe yahan import karein
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.WorkerProfileSetupScreen
import com.example.ui.screens.ProfileScreen
// Agar neeche di gayi screens ban chuki hain toh inke comments (//) hata dein
// import com.example.ui.screens.HomeScreen
// import com.example.ui.screens.SearchFilterScreen
// import com.example.ui.screens.SettingsScreen
// import com.example.ui.screens.PostJobScreen
// import com.example.ui.screens.JobDetailsScreen
// import com.example.ui.screens.ChatScreen
// import com.example.ui.screens.NotificationsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "splash") {
        
        // 1. Splash Screen
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("role_selection") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        // 2. Role Selection (Worker ya Client)
        composable("role_selection") {
            RoleSelectionScreen(
                onWorkerSelected = {
                    navController.navigate("worker_setup")
                },
                onCustomerSelected = {
                    navController.navigate("auth") // Client ko pehle login par bhejte hain
                }
            )
        }

        // 3. Login / Auth Screen
        composable("auth") {
            AuthScreen(
                onLoginClick = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        // 4. Worker Profile Setup Form
        composable("worker_setup") {
            WorkerProfileSetupScreen(
                onSetupComplete = {
                    // Registration form bharne ke baad "Profile Review" par jayega (Masterplan Screen 8)
                    navController.navigate("worker_profile_review")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 5. Worker Profile Review (Yahan Edit aur Submit buttons honge)
        composable("worker_profile_review") {
            ProfileScreen(
                onBack = { 
                    // Edit button dabane par wapas setup form par jayega
                    navController.popBackStack() 
                },
                onSubmit = {
                    // Submit dabane par finally Home Dashboard par jayega
                    navController.navigate("home") {
                        popUpTo("worker_setup") { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // MAIN APP DASHBOARD & BOTTOM NAV SCREENS
        // ==========================================

        // 6. Home Dashboard (Worker / Client ka main page)
        composable("home") {
            // HomeScreen(navController = navController)
        }

        // 7. Search & Filters
        composable("search_filter") {
            // SearchFilterScreen(navController = navController)
        }

        // 8. Post a Work (Client ke liye)
        composable("post_job") {
            // PostJobScreen(navController = navController)
        }

        // 9. Job Details (Jab kisi kaam par click karein)
        composable("job_details") {
            // JobDetailsScreen(navController = navController)
        }

        // 10. My Applications / Job Status
        composable("applications") {
            // MyApplicationsScreen(navController = navController)
        }

        // 11. Chat Screen
        composable("chat") {
            // ChatScreen(navController = navController)
        }

        // 12. Notifications
        composable("notifications") {
            // NotificationsScreen(navController = navController)
        }

        // 13. Settings & Standard Profile View
        composable("settings") {
            // SettingsScreen(navController = navController)
        }
    }
}
