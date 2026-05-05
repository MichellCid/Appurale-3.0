package com.example.appurale3.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appurale3.auth.presentation.addroutine.AddRoutineScreen
import com.example.appurale3.auth.presentation.detailroutine.ActivityProgressScreen
import com.example.appurale3.auth.presentation.estatistics.StatisticsScreen
import com.example.appurale3.auth.presentation.home.HomeScreen
import com.example.appurale3.auth.presentation.login.LoginScreen
import com.example.appurale3.auth.presentation.register.RegisterScreen
import com.example.appurale3.presentation.addactivity.AddActivityScreen
import com.example.appurale3.presentation.detailroutine.DetailRoutineScreen
import com.example.appurale3.presentation.detailroutine.DetailRoutineViewModel
import com.example.appurale3.data.models.Activity
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNaveHost(
    navController: NavHostController = rememberNavController(),
    startOnHome: Boolean,
    userId: String = "",
    deepLinkRutinaId: String? = null
) {
    // Crear UNA SOLA instancia del ViewModel para compartir
    val detailViewModel: DetailRoutineViewModel = hiltViewModel()

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
                },
                onNavigateToStatistics = {
                    navController.navigate("statistics")
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
                navArgument("routineId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
            DetailRoutineScreen(
                routineId = routineId,

                onNavigateBack = { navController.popBackStack() },

                onNavigateToAddActivity = { routineIdParam ->
                    navController.navigate(NavRoute.AddActivity.pass(routineIdParam))
                },

                onNavigateToEditActivity = { routineIdParam, activity ->
                    val activityJson = URLEncoder.encode(
                        Gson().toJson(activity),
                        StandardCharsets.UTF_8.toString()
                    )
                    navController.navigate(NavRoute.AddActivity.pass(routineIdParam, activityJson))
                },


                onNavigateToActivityProgress = { rId, aId ->
                    navController.navigate("activity_detail/$rId/$aId")
                },

                viewModel = detailViewModel
            )
        }

        composable(
            route = NavRoute.AddActivity.route,
            arguments = listOf(
                navArgument("routineId") { type = NavType.StringType },
                navArgument("activityJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
            val activityJson = backStackEntry.arguments?.getString("activityJson") ?: ""

            var existingActivity: Activity? = null
            if (activityJson.isNotEmpty()) {
                try {
                    val decoded = URLDecoder.decode(activityJson, StandardCharsets.UTF_8.toString())
                    existingActivity = Gson().fromJson(decoded, Activity::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            AddActivityScreen(
                routineId = routineId,
                viewModel = detailViewModel,
                existingActivity = existingActivity,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("statistics") {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable("activity_detail/{routineId}/{index}") { backStackEntry ->

            val routineId = backStackEntry.arguments?.getString("routineId") ?: return@composable
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull()

            val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(routineId) {
                detailViewModel.loadRoutine(routineId)
            }

            val routine = uiState.routine

            if (routine != null && index != null && index in routine.activities.indices) {

                ActivityProgressScreen(
                    routine = routine,
                    currentIndex = index,
                    onNext = { nextIndex ->
                        val nextActivity = routine.activities[nextIndex]

                        navController.navigate("activity_detail/$routineId/$nextIndex") {
                            popUpTo("activity_detail/$routineId/$index") {
                                inclusive = true
                            }
                        }
                    },
                    onFinish = {
                        navController.popBackStack()
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )

            } else {


                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    LaunchedEffect(deepLinkRutinaId) {
        if (startOnHome && !deepLinkRutinaId.isNullOrEmpty()) {
            navController.navigate(NavRoute.DetailRoutine.pass(deepLinkRutinaId))
        }
    }
}