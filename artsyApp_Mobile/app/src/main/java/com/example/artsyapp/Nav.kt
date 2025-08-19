
package com.example.artsyapp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun Nav(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "favorites"
    ) {
        composable("favorites") {
            FavScreen(navController = navController)
        }
        composable("search") {
            SearchScreen(navController = navController)
        }
        composable("artistDetails/{artistName}/{artistId}") { backStackEntry ->
            val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
            val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
            OptionsBoxScreen(
                artistName = artistName,
                artistId = artistId,
                navController = navController
            )
        }
        composable("login") {
            LoginScreen(
                navController = navController,
                onRegisterClick = { navController.navigate("register") },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("register") {
            RegisterScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate("login") },
            )
        }
    }
}