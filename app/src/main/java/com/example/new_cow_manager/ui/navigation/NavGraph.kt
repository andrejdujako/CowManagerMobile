package com.example.new_cow_manager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.new_cow_manager.ui.screens.AddEditCowScreen
import com.example.new_cow_manager.ui.screens.CowDetailsScreen
import com.example.new_cow_manager.ui.screens.CowListScreen

sealed class Screen(val route: String) {
    object CowList : Screen("cow_list")
    object CowDetails : Screen("cow_details/{cowId}") {
        fun createRoute(cowId: String) = "cow_details/$cowId"
    }
    object AddCow : Screen("add_cow")
    object EditCow : Screen("edit_cow/{cowId}") {
        fun createRoute(cowId: String) = "edit_cow/$cowId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.CowList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.CowList.route) {
            CowListScreen(
                onCowClick = { cowId ->
                    navController.navigate(Screen.CowDetails.createRoute(cowId))
                },
                onAddCowClick = {
                    navController.navigate(Screen.AddCow.route)
                }
            )
        }

        composable(
            route = Screen.CowDetails.route,
            arguments = listOf(navArgument("cowId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getString("cowId") ?: return@composable
            CowDetailsScreen(
                cowId = cowId,
                onEditClick = { navController.navigate(Screen.EditCow.createRoute(cowId)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.AddCow.route) {
            AddEditCowScreen(
                cowId = null,
                onSaveComplete = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditCow.route,
            arguments = listOf(navArgument("cowId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cowId = backStackEntry.arguments?.getString("cowId") ?: return@composable
            AddEditCowScreen(
                cowId = cowId,
                onSaveComplete = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
