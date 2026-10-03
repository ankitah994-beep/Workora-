package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "splash") {
        
        composable("splash") {
            SplashScreen(onSplashFinished = {
                navController.navigate("role_selection") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        
        composable("role_selection") {
            RoleSelectionScreen(
                onWorkerSelected = {
                    navController.navigate("worker_registration")
                },
                onCustomerSelected = {
                    navController.navigate("customer_home")
                }
            )
        }

        composable("worker_registration") {
            // यहाँ हम वर्कर का OTP और Portfolio वाला पेज जोड़ेंगे
        }
        
        composable("customer_home") {
            // यहाँ कस्टमर का पेज आएगा
        }
    }
}
