package com.example.appurale3.data.models

import java.util.Date

data class Routine(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val date: Date? = null,
    val hour: String = "",
    val duration: Int = 0,
    val soundUri: String = "",
    val activities: List<Activity> = emptyList(),  // Cambiar ActivityItem por Activity
    val userId: String = "",
    val createdAt: Date = Date(),
    val isActive: Boolean = true
)