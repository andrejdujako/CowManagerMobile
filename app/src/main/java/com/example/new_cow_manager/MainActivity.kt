package com.example.new_cow_manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.new_cow_manager.ui.screens.AddEditCowScreen
import com.example.new_cow_manager.ui.screens.CowDetailsScreen
import com.example.new_cow_manager.ui.screens.CowListScreen
import com.example.new_cow_manager.ui.theme.New_Cow_ManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            New_Cow_ManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "cow_list"
                    ) {
                        composable("cow_list") {
                            CowListScreen(
                                onCowClick = { cowId ->
                                    navController.navigate("cow_details/$cowId")
                                },
                                onAddCowClick = {
                                    navController.navigate("add_cow")
                                }
                            )
                        }

                        composable(
                            route = "cow_details/{cowId}"
                        ) { backStackEntry ->
                            val cowId = backStackEntry.arguments?.getString("cowId")
                            if (cowId != null) {
                                CowDetailsScreen(
                                    cowId = cowId,
                                    onEditClick = {
                                        navController.navigate("edit_cow/$cowId")
                                    },
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }

                        composable("add_cow") {
                            AddEditCowScreen(
                                cowId = null,
                                onSaveComplete = {
                                    navController.popBackStack()
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "edit_cow/{cowId}"
                        ) { backStackEntry ->
                            val cowId = backStackEntry.arguments?.getString("cowId")
                            if (cowId != null) {
                                AddEditCowScreen(
                                    cowId = cowId,
                                    onSaveComplete = {
                                        navController.popBackStack()
                                    },
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
