package com.example.appurale3.data.models

import java.util.Date

data class CalendarEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date? = null,
    val hour: String = "",
    val duration: Int = 0,
    val routineId: String = "",
    val routineName: String = "",
    val isCompleted: Boolean = false,
    val userId: String = ""
)