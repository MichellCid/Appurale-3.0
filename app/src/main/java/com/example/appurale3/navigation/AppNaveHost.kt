package com.example.appurale3.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.appurale3.auth.presentation.addroutine.AddRoutineScreen
import com.example.appurale3.auth.presentation.home.HomeScreen
import com.example.appurale3.auth.presentation.login.LoginScreen
import com.example.appurale3.auth.presentation.register.RegisterScreen
import com.example.appurale3.presentation.addactivity.AddActivityScreen
import com.example.appurale3.presentation.detailroutine.DetailRoutineScreen
import com.example.appurale3.presentation.detailroutine.DetailRoutineViewModel

@Composable
fun AppNaveHost(
    navController: NavHostController = rememberNavController(),
    startOnHome: Boolean,
    userId: String = "",
    deepLinkRutinaId: String? = null
) {
    // Crear UNA SOLA instancia del ViewModel para compartir
    val detailViewModel: DetailRoutineViewModel = hiltViewModel()
/**
    LaunchedEffect(deepLinkRutinaId, userId) {
        if (!deepLinkRutinaId.isNullOrEmpty() && userId.isNotEmpty()) {
            navController.navigate(
                NavRoute.DetailRoutine.pass(deepLinkRutinaId, userId)
            )
        }
    }**/

    NavHost(
        navController = navController,
        startDestination = if (startOnHome) NavRoute.Home.route else NavRoute.Login.route
    ) {
        composable(NavRoute.Login.route) {
            LoginScreen(
                onGoToRegister = { navController.navigate(NavRoute.Register.route) },
                onLoggedIn = {
                    navController.navigate(NavRoute.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoute.Register.route) {
            RegisterScreen(
                onRegistered = {
                    navController.navigate(NavRoute.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(NavRoute.Home.route) {
            HomeScreen(
                onNavigateToAddRoutine = {
                    navController.navigate(NavRoute.AddRoutine.route)
                },
                onNavigateToDetailRoutine = { routineId ->
                    navController.navigate(NavRoute.DetailRoutine.pass(routineId))
                }
            )
        }

        composable(NavRoute.AddRoutine.route) {
            AddRoutineScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoute.DetailRoutine.route,
            arguments = listOf(
                navArgument("routineId") { type = NavType.StringType },
                //navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val routineId = backStackEntry.arguments?.getString("routineId").orEmpty()
            //val userIdParam = backStackEntry.arguments?.getString("userId").orEmpty()

            DetailRoutineScreen(
                routineId = routineId,
                //userId = userIdParam,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddActivity = { id ->
                    navController.navigate(NavRoute.AddActivity.pass(id))
                },
                viewModel = detailViewModel
            )
        }


        composable(
            route = NavRoute.AddActivity.route,
            arguments = listOf(
                navArgument("routineId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""

            AddActivityScreen(
                routineId = routineId,
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

    LaunchedEffect(deepLinkRutinaId, startOnHome) {
        if (startOnHome && !deepLinkRutinaId.isNullOrEmpty()) {
            navController.navigate(
                NavRoute.DetailRoutine.pass(deepLinkRutinaId)
            )
        }
    }
}