package com.example.appurale3.navigation

sealed class NavRoute(val route : String){

    data object Login : NavRoute("login")
    data object Register : NavRoute("register")
    data object Home : NavRoute("home")

}