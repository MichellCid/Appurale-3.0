package com.example.appurale3.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.appurale3.data.service.ActivityForegroundService

class ActivityAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val activityName = intent.getStringExtra("ACTIVITY_NAME") ?: "Actividad"
        val duration = intent.getLongExtra("DURATION_MS", 0L)

        val serviceIntent = Intent(context, ActivityForegroundService::class.java).apply {
            putExtra("ACTIVITY_NAME", activityName)
            putExtra("DURATION_MS", duration)
        }


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}