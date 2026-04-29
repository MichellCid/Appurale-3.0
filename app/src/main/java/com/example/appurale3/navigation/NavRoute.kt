package com.example.appurale3.navigation

sealed class NavRoute(val route: String) {
    data object Login : NavRoute("login")
    data object Register : NavRoute("register")
    data object Home : NavRoute("home")
    data object AddRoutine : NavRoute("add_routine")
    /**data object DetailRoutine : NavRoute("detail_routine/{routineId}/{userId}") {
        fun pass(routineId: String, userId: String) = "detail_routine/$routineId/$userId"
    }
**/
    data object DetailRoutine : NavRoute("detail_routine/{routineId}") {
        fun pass(routineId: String) = "detail_routine/$routineId"
    }

    data object AddActivity : NavRoute("add_activity/{routineId}") {
        fun pass(routineId: String) = "add_activity/$routineId"
    }
}