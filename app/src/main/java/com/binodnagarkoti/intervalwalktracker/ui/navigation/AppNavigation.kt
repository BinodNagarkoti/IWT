package com.binodnagarkoti.intervalwalktracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.binodnagarkoti.intervalwalktracker.ui.screens.*
import com.binodnagarkoti.intervalwalktracker.viewmodel.DashboardViewModel
import com.binodnagarkoti.intervalwalktracker.viewmodel.WorkoutViewModel

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Workout : Screen("workout/{sets}") {
        fun createRoute(sets: Int) = "workout/$sets"
    }
    object Summary : Screen("summary")
    object History : Screen("history")
    object ComingSoon : Screen("coming_soon/{featureName}") {
        fun createRoute(featureName: String) = "coming_soon/$featureName"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel,
    workoutViewModel: WorkoutViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = dashboardViewModel,
                onStartWorkout = { sets ->
                    navController.navigate(Screen.Workout.createRoute(sets))
                },
                onViewHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToFeature = { feature ->
                    navController.navigate(Screen.ComingSoon.createRoute(feature))
                }
            )
        }
        composable(Screen.Workout.route) { backStackEntry ->
            val sets = backStackEntry.arguments?.getString("sets")?.toIntOrNull() ?: 5
            WorkoutScreen(
                viewModel = workoutViewModel,
                sets = sets,
                onBack = {
                    navController.popBackStack()
                },
                onFinish = {
                    navController.navigate(Screen.Summary.route) {
                        popUpTo(Screen.Dashboard.route)
                    }
                }
            )
        }
        composable(Screen.Summary.route) {
            SummaryScreen(
                viewModel = workoutViewModel,
                onReturn = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = dashboardViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.ComingSoon.route) { backStackEntry ->
            val featureName = backStackEntry.arguments?.getString("featureName") ?: "Feature"
            ComingSoonScreen(
                featureName = featureName,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
