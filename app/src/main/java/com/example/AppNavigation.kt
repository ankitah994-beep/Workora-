package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.ui.screens.SplashScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.WorkerProfileSetupScreen
import com.example.ui.screens.SearchFilterScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WorkerDetailScreen
import com.example.ui.screens.SettingsScreen

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
                    navController.navigate("worker_setup")
                },
                onCustomerSelected = {
                    navController.navigate("auth")
                }
            )
        }

        composable("worker_setup") {
            WorkerProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate("profile")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("auth") {
            // Agar AuthScreen mein parameters hain, toh baad mein add karenge
            AuthScreen() 
        }

        composable("search_filter") {
            SearchFilterScreen() 
        }

        composable("profile") {
            ProfileScreen() 
        }

        composable("worker_detail") {
            WorkerDetailScreen() 
        }

        composable("settings") {
            SettingsScreen() 
        }
    }
}
