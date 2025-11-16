package com.cotovicz.daseinandroid.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cotovicz.daseinandroid.ui.screens.CreateScreen
import com.cotovicz.daseinandroid.ui.screens.DetailScreen
import com.cotovicz.daseinandroid.ui.screens.EditProfileScreen
import com.cotovicz.daseinandroid.ui.screens.FeedScreen
import com.cotovicz.daseinandroid.ui.screens.FilterScreen
import com.cotovicz.daseinandroid.ui.screens.LoginScreen
import com.cotovicz.daseinandroid.ui.screens.ProfileScreen
import com.cotovicz.daseinandroid.ui.screens.SavedFeedScreen
import com.cotovicz.daseinandroid.ui.screens.SignUpScreen
import com.cotovicz.daseinandroid.ui.viewmodel.AuthViewModel
import com.cotovicz.daseinandroid.ui.viewmodel.AuthViewModelFactory
import com.cotovicz.daseinandroid.ui.viewmodel.CreationViewModel
import com.cotovicz.daseinandroid.ui.viewmodel.CreationViewModelFactory

@Composable
fun AppNavHost(
    authViewModelFactory: AuthViewModelFactory,
    creationViewModelFactory: CreationViewModelFactory,
    startDestination: String
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
    val creationViewModel: CreationViewModel = viewModel(factory = creationViewModelFactory)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("signup") {
            SignUpScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("feed") {
            FeedScreen(
                navController = navController,
                viewModel = creationViewModel,
                authViewModel = authViewModel
            )
        }
        composable("create") {
            CreateScreen(
                viewModel = creationViewModel,
                authViewModel = authViewModel,
                navController = navController,
                creationId = null
            )
        }
        composable("saved") {
            SavedFeedScreen(
                navController = navController,
                creationViewModel = creationViewModel,
                authViewModel = authViewModel
            )
        }

        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            id?.let {
                DetailScreen(
                    navController = navController,
                    viewModel = creationViewModel,
                    authViewModel = authViewModel,
                    creationId = it
                )
            }
        }

        composable(
            route = "edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            CreateScreen(
                viewModel = creationViewModel,
                authViewModel = authViewModel,
                navController = navController,
                creationId = id
            )
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                authViewModel = authViewModel,
                creationViewModel = creationViewModel
            )
        }

        composable("editProfile") {
            EditProfileScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable("filter") {
            FilterScreen(
                navController = navController,
                creationViewModel = creationViewModel
            )
        }
    }
}