package com.example.appurale3.data.models

data class Activity(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val duration: Int = 0,
    val isCompleted: Boolean = false,
    val active: Boolean = true
)