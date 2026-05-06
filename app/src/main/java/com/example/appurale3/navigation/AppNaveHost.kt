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
import com.example.appurale3.auth.presentation.addroutine.AddRoutineScreen
import com.example.appurale3.auth.presentation.detailroutine.ActivityProgressScreen
import com.example.appurale3.auth.presentation.estatistics.StatisticsScreen
import com.example.appurale3.auth.presentation.home.HomeScreen
import com.example.appurale3.auth.presentation.login.LoginScreen
import com.example.appurale3.auth.presentation.register.RegisterScreen
import com.example.appurale3.auth.presentation.settings.SettingsScreen
import com.example.appurale3.presentation.addactivity.AddActivityScreen
import com.example.appurale3.auth.presentation.calendar.CalendarScreen
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
        // ========== PANTALLAS DE AUTENTICACIÓN ==========
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

        // ========== PANTALLA PRINCIPAL ==========
        composable(NavRoute.Home.route) {
            HomeScreen(
                onNavigateToAddRoutine = {
                    navController.navigate(NavRoute.AddRoutine.route)
                },
                onNavigateToDetailRoutine = { routineId ->
                    navController.navigate(NavRoute.DetailRoutine.pass(routineId))
                },
                onNavigateToCalendar = {
                    navController.navigate(NavRoute.Calendar.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(NavRoute.Statistics.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoute.Settings.route)
                }
            )
        }

        // ========== PANTALLA DE CALENDARIO ==========
        composable(NavRoute.Calendar.route) {
            CalendarScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddActivity = { routineId ->
                    // Navegar a agregar actividad
                }
            )
        }

        // ========== PANTALLA DE ESTADÍSTICAS ==========
        composable(NavRoute.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== PANTALLA DE CONFIGURACIÓN ==========
        composable(NavRoute.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== AGREGAR RUTINA ==========
        composable(NavRoute.AddRoutine.route) {
            AddRoutineScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ========== DETALLE DE RUTINA ==========
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
                onNavigateToActivityProgress = { rId, aIndex ->
                    navController.navigate(
                        route = NavRoute.ActivityProgress.pass(
                            routineId = rId,
                            activityIndex = aIndex.toIntOrNull() ?: 0
                        )
                    )
                },
                viewModel = detailViewModel
            )
        }

        // ========== AGREGAR/EDITAR ACTIVIDAD ==========
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

        // ========== PROGRESO DE ACTIVIDAD ==========
        composable(
            route = NavRoute.ActivityProgress.route,
            arguments = listOf(
                navArgument("routineId") { type = NavType.StringType },
                navArgument("activityIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
            val activityIndex = backStackEntry.arguments?.getInt("activityIndex") ?: 0

            val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(routineId) {
                detailViewModel.loadRoutine(routineId)
            }

            val routine = uiState.routine

            if (routine != null && activityIndex in routine.activities.indices) {
                ActivityProgressScreen(
                    routine = routine,
                    currentIndex = activityIndex,
                    onNext = { nextIndex ->
                        navController.navigate(NavRoute.ActivityProgress.pass(routineId, nextIndex)) {
                            popUpTo(NavRoute.ActivityProgress.pass(routineId, activityIndex)) {
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

    // Deep Link para abrir rutina directamente
    LaunchedEffect(deepLinkRutinaId) {
        if (startOnHome && !deepLinkRutinaId.isNullOrEmpty()) {
            navController.navigate(NavRoute.DetailRoutine.pass(deepLinkRutinaId))
        }
    }
}