package com.example.appurale3.data.models

data class TodayActivity(
    val id: String,
    val name: String,
    val description: String = "",
    val duration: Int = 0,
    val isCompleted: Boolean = false,
    val active: Boolean = true
)

data class RoutineUiModel(
    val id: String,
    val name: String,
    val description: String = "",
    val category: String = "",
    val totalActivities: Int,
    val completedActivities: Int = 0,
    val active: Boolean = true
)