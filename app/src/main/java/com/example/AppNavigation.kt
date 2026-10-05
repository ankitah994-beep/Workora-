        composable("worker_setup") {
            WorkerProfileSetupScreen(
                onSetupComplete = {
                    navController.navigate("home") { // Yahan "profile" ki jagah "home" kar diya hai
                        popUpTo("worker_setup") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
