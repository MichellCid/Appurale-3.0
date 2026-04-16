package com.example.appurale3.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appurale3.auth.presentation.home.HomeScreen
import com.example.appurale3.auth.presentation.login.LoginScreen
import com.example.appurale3.auth.presentation.register.RegisterScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startOnHome : Boolean
){
    NavHost(
        navController = navController,
        startDestination = if (startOnHome) NavRoute.Home.route else NavRoute.Login.route
    ){
        composable(NavRoute.Login.route) {
            LoginScreen(
                onGoToRegister = {navController.navigate(NavRoute.Register.route)},
                onLoggedIn = {navController.navigate(NavRoute.Home.route){popUpTo(0)} }
            )
        }

        composable(NavRoute.Register.route){
            RegisterScreen(
                //onGoToLogin = {navController.navigate(NavRoute.Login.route)},
                onRegistered = {navController.navigate(NavRoute.Home.route){popUpTo(0)} },
                onBackToLogin = {navController.popBackStack()}
            )
        }

        composable(NavRoute.Home.route){
            HomeScreen()
        }

    }

}