package com.example.smartfridge.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Fridge : Screen("fridge")
    object Recipe : Screen("recipe/{recipe}") {
        fun createRoute(recipe: String): String {
            val encodedRecipe = URLEncoder.encode(recipe, StandardCharsets.UTF_8.toString())
            return "recipe/$encodedRecipe"
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Fridge.route) {
        composable(Screen.Fridge.route) {
            FridgeScreen(
                onNavigateToRecipe = { recipe ->
                    navController.navigate(Screen.Recipe.createRoute(recipe))
                }
            )
        }
        composable(
            Screen.Recipe.route,
            arguments = listOf(navArgument("recipe") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipe = backStackEntry.arguments?.getString("recipe") ?: ""
            val decodedRecipe = URLDecoder.decode(recipe, StandardCharsets.UTF_8.toString())
            RecipeScreen(
                recipe = decodedRecipe,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
