package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// यहाँ हम आपकी सारी स्क्रीन्स को एक साथ इम्पोर्ट कर रहे हैं
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.WorkerProfileSetupScreen
import com.example.ui.screens.SearchFilterScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WorkerDetailScreen
import com.example.ui.screens.SettingsScreen
// अगर और कोई स्क्रीन है, तो उसे भी यहाँ import करें

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // startDestination "splash" है, यानी ऐप खुलते ही सबसे पहले Splash चलेगा
    NavHost(navController = navController, startDestination = "splash") {
        
        // 1. Splash Screen
        composable("splash") {
            SplashScreen(onSplashFinished = {
                navController.navigate("role_selection") {
                    popUpTo("splash") { inclusive = true } // वापस बैक करने पर स्प्लैश न खुले
                }
            })
        }
        
        // 2. Role Selection (यहाँ से दो रास्ते निकलेंगे)
        composable("role_selection") {
            RoleSelectionScreen(
                onWorkerSelected = {
                    navController.navigate("worker_setup") // वर्कर रजिस्ट्रेशन पर जाओ
                },
                onCustomerSelected = {
                    navController.navigate("auth") // कस्टमर लॉगिन/ऑथेंटिकेशन पर जाओ
                }
            )
        }

        // 3. Worker Profile Setup (वर्कर रजिस्ट्रेशन)
        composable("worker_setup") {
            // नोट: अगर आपके WorkerProfileSetupScreen में navController पैरामीटर नहीं है, 
            // तो ब्रैकेट () खाली छोड़ दें। 
            WorkerProfileSetupScreen(navController = navController)
        }

        // 4. Auth / Login Screen (कस्टमर या वर्कर का लॉगिन)
        composable("auth") {
            AuthScreen(navController = navController)
        }

        // 5. Customer Home / Search Filter (कस्टमर का होम पेज जहाँ वो वर्कर ढूंढेगा)
        composable("search_filter") {
            SearchFilterScreen(navController = navController)
        }

        // 6. Profile Screen (यूज़र की प्रोफाइल)
        composable("profile") {
            ProfileScreen(navController = navController)
        }

        // 7. Worker Detail Screen (किसी वर्कर की पूरी जानकारी)
        composable("worker_detail") {
            WorkerDetailScreen(navController = navController)
        }

        // 8. Settings Screen (सेटिंग्स)
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        
        // अगर आपके पास PostJobScreen, ReviewScreen या कोई और स्क्रीन भी है, 
        // तो बस उन्हें ऐसे ही यहाँ जोड़ दें:
        /*
        composable("post_job") {
            PostJobScreen(navController = navController)
        }
        */
    }
}
