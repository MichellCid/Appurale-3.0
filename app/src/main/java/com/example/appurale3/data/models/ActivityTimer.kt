package com.example.appurale3.data.models

data class ActivityTimer(
    val activityId: String = "",
    val startTime: Long? = null,
    val accumulatedTime: Long = 0L,
    val isRunning: Boolean = false
)
