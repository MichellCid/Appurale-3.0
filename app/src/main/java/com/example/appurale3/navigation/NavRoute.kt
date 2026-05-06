package com.example.appurale3.navigation

sealed class NavRoute(val route: String) {
    data object Login : NavRoute("login")
    data object Register : NavRoute("register")
    data object Home : NavRoute("home")
    data object AddRoutine : NavRoute("add_routine")
    data object Calendar : NavRoute("calendar")
    data object Statistics : NavRoute("statistics")
    data object Settings : NavRoute("settings")

    data object DetailRoutine : NavRoute("detail_routine/{routineId}") {
        fun pass(routineId: String) = "detail_routine/$routineId"
    }

    data object AddActivity : NavRoute("add_activity/{routineId}/{activityJson}") {
        fun pass(routineId: String) = "add_activity/$routineId/"
        fun pass(routineId: String, activityJson: String) = "add_activity/$routineId/$activityJson"
    }

    data object ActivityProgress : NavRoute("activity_detail/{routineId}/{activityIndex}") {
        fun pass(routineId: String, activityIndex: Int) = "activity_detail/$routineId/$activityIndex"
    }
}