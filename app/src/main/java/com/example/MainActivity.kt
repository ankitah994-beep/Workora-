package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// Apne naye navigation file ko yahan import karein (path check kar lein)
import com.aistudio.workora.app.ui.navigation.AppNavigation 

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Ab app seedhe naye navigation se start hogi, purani screens ka koi jhanjhat nahi
            AppNavigation()
        }
    }
}
